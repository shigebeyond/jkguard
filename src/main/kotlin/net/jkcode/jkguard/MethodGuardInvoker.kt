package net.jkcode.jkguard

import co.paralleluniverse.fibers.Suspendable
import net.jkcode.jkutil.common.currMillis
import net.jkcode.jkutil.common.resultFromFuture
import net.jkcode.jkutil.common.toExpr
import net.jkcode.jksoa.rpc.client.combiner.annotation.degrade
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * 带守护的方法调用者
 *    1. guardInvoke() -- 入口
 *    2. invokeAfterGuard() -- 子类实现, 就是真正的方法调用
 *    3. 其他方法 -- 被 MethodGuard 中的守护组件调用
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-11-08 7:25 PM
 */
abstract class MethodGuardInvoker : IMethodGuardInvoker {

    /**
     * 方法守护者
     */
    protected val methodGuards: ConcurrentHashMap<Method, MethodGuard> = ConcurrentHashMap();

    /**
     * 获得方法守护者
     * @param method
     * @return
     */
    public override fun getMethodGuard(method: Method): IMethodGuard {
        return methodGuards.getOrPut(method){
            MethodGuard(method, this)
        }
    }

    /**
     * 守护方法调用
     *
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @return 结果
     */
    @Suspendable
    public override fun guardInvoke(method: Method, proxy: Any, args: Array<Any?>): Any? {
        if(guardLogger.isDebugEnabled)
            guardLogger.debug(args.joinToString(", ", "{}调用方法: {}.{}(", ")") {
                it.toExpr()
            }, this::class.simpleName, method.declaringClass.name, method.name)

        // 1 合并调用
        // 1.1 根据group来合并请求
        val methodGuard = getMethodGuard(method) // 获得方法守护者
        if (methodGuard.groupCombiner != null) {
            val resFuture = methodGuard.groupCombiner!!.add(args.single()!!)
            return method.resultFromFuture(resFuture)
        }

        // 1.2 根据key来合并请求
        if (methodGuard.keyCombiner != null) {
            val resFuture = methodGuard.keyCombiner!!.add(args.single()!!)
            return method.resultFromFuture(resFuture)
        }

        // 2 合并之后的调用
        return invokeAfterCombine(methodGuard, method, proxy, args)
    }

    /**
     * 合并之后的调用
     *
     * @param methodGuard 方法守护者
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @return 结果
     */
    @Suspendable
    public override fun invokeAfterCombine(methodGuard: IMethodGuard, method: Method, obj: Any, args: Array<Any?>): Any? {
        // 1 断路
        if(methodGuard.circuitBreaker != null)
            if(!methodGuard.circuitBreaker!!.acquire())
                return handleException(methodGuard, method, args, GuardException("断路"))

        // 2 限流
        if(methodGuard.rateLimiter != null)
            if(!methodGuard.rateLimiter!!.acquire())
                return handleException(methodGuard, method, args, GuardException("限流"))

        // 3 缓存
        if(methodGuard.cacheHandler != null) {
            val resFuture = methodGuard.cacheHandler!!.cacheOrLoad(args)
            return method.resultFromFuture(resFuture)
        }

        // 4 缓存之后的调用
        return invokeAfterCache(methodGuard, method, obj, args)
    }

    /**
     * 缓存之后的调用
     *
     * @param methodGuard 方法守护者
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @return 结果
     */
    @Suspendable
    public override fun invokeAfterCache(methodGuard: IMethodGuard, method: Method, obj: Any, args: Array<Any?>): Any? {
        // 1 计量
        // 1.1 添加总计数
        val measurer = methodGuard.measurer
        measurer?.currentBucket()?.addTotal()
        val startTime = currMillis()

        // 2 真正的调用
        val resFuture = invokeAfterGuard(method, obj, args).whenComplete { r, e ->
            // 1.2 添加请求耗时
            measurer?.currentBucket()?.addRt(currMillis() - startTime)

            if (e == null) {
                // 1.3 添加成功计数
                measurer?.currentBucket()?.addSuccess()
                r
            } else {
                // 1.4 添加异常计数
                measurer?.currentBucket()?.addException()

                // 3 处理异常: 调用后备处理
                handleException(methodGuard, method, args, e!!)
            }
        }

        //处理结果
        return method.resultFromFuture(resFuture)
    }

    /**
     * 处理异常: 调用后备处理
     * @param methodGuard
     * @param method 方法
     * @param args 参数
     * @param r 异常
     * @return
     */
    protected fun handleException(methodGuard: IMethodGuard, method: Method, args: Array<Any?>, r: Throwable): Any? {
        if (methodGuard.degradeHandler == null)
            throw r

        if(guardLogger.isDebugEnabled)
            guardLogger.debug(args.joinToString(", ", "{}调用方法: {}.{}(", "), 发生异常{}, 进而调用后备方法 {}") {
                it.toExpr()
            }, this::class.simpleName, method.declaringClass.name, method.name, r.message, method.degrade?.fallbackMethod)
        return methodGuard.degradeHandler!!.handleFallback(r, args)
    }

}