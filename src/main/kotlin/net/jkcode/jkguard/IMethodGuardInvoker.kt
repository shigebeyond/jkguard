package net.jkcode.jkguard

import co.paralleluniverse.fibers.Suspendable
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture

/**
 * 带守护的方法调用者
 *    1. guardInvoke() -- 入口
 *    2. invokeAfterGuard() -- 子类实现, 就是真正的方法调用
 *    3. 其他方法 -- 被 MethodGuard 中的守护组件调用
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-7-27 7:25 PM
 */
interface IMethodGuardInvoker {

    /**
     * 获得方法调用的对象
     *    合并后会异步调用其他方法, 原来方法的调用对象会丢失
     *
     * @param method
     * @return
     */
    fun getCombineInovkeObject(method: IMethodMeta<*>): Any

    /**
     * 守护方法调用 -- 入口
     *
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @return 结果
     */
    @Suspendable
    fun guardInvoke(method: Method, proxy: Any, args: Array<Any?>): Any?{
        return guardInvoke(MethodMeta(method, this), proxy, args)
    }

    /**
     * 守护方法调用 -- 入口
     *
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @return 结果
     */
    fun guardInvoke(method: IMethodMeta<*>, proxy: Any, args: Array<Any?>): Any?

    /**
     * 合并之后的调用
     *
     * @param methodGuard 方法守护者
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @return 结果
     */
    fun invokeAfterCombine(methodGuard: IMethodGuard, method: IMethodMeta<*>, obj: Any, args: Array<Any?>): Any?

    /**
     * 缓存之后的调用
     *
     * @param methodGuard 方法守护者
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @return 结果
     */
    fun invokeAfterCache(methodGuard: IMethodGuard, method: IMethodMeta<*>, obj: Any, args: Array<Any?>): Any?

    /**
     * 守护之后真正的调用
     *    实现：server端实现是调用原生方法, client端实现是发rpc请求
     *
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @return
     */
    fun invokeAfterGuard(method: IMethodMeta<*>, obj: Any, args: Array<Any?>): CompletableFuture<Any?>
}