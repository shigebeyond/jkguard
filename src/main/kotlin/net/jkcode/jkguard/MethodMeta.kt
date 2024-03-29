package net.jkcode.jkguard

import co.paralleluniverse.fibers.Suspendable
import net.jkcode.jkutil.common.*
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * 基于Method实现的方法元数据
 *   基本上就是代理Method，为了兼容php方法，才抽取的IMethodMeta
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-4-27 7:25 PM
 */
class MethodMeta(
        protected val method: Method, // java方法
        handler: IMethodGuardInvoker // 带守护的方法调用者
): IMethodMeta<Any?>(handler) {

    /**
     * 类名
     */
    override val clazzName: String
        get() = method.declaringClass.name

    /**
     * 方法名
     */
    override val methodName: String
        get() = method.name

    /**
     * 方法签名(rpc用到)
     */
    override val methodSignature: String
        get() = method.getSignature()

    /**
     * 方法参数类型
     *    会在 degradeHandler/groupCombiner/keyCombiner 用来检查方法的参数与返回值类型
     */
    override val parameterTypes: Array<Class<*>>
        get() = method.parameterTypes

    /**
     * 返回值类型
     */
    override val returnType: Class<*>
        get() = method.returnType

    /**
     * 是否纯php实现
     *    用来决定是否在 degradeHandler/groupCombiner/keyCombiner 用来检查方法的参数与返回值类型
     */
    override val isPurePhp: Boolean
        get() = false

    /**
     * 获得方法注解
     * @param annotationClass 注解类
     * @return
     */
    override fun <A : Annotation> getAnnotation(annotationClass: Class<A>): A? {
        return method.getCachedAnnotation(annotationClass)
    }

    /**
     * 方法处理
     *    在server端的IMethodGuardInvoker#invokeAfterGuard()/两端的降级处理中调用
     *    实现：server端实现是调用包装的本地方法, client端实现是发rpc请求
     */
    override fun invoke(obj: Any, vararg args: Any?): Any? {
        return method.invoke(obj, *args)
    }

    /**
     * 从CompletableFuture获得方法结果值
     *    根据java方法返回类型(是否CompletableFuture)来决定返回异步or同步结果
     * @param resFuture
     * @return
     */
    @Suspendable
    override fun getResultFromFuture(resFuture: CompletableFuture<*>): Any?{
        return method.getResultFromFuture(resFuture)
    }

    /**
     * 获得兄弟方法, 用在获得降级或合并的兄弟方法
     * @param name 兄弟方法名
     * @return
     */
    override fun getBrotherMethod(name: String): IMethodMeta<Any?> {
        val brotherMethod = method.declaringClass.getMethodByName(name)!!
        return MethodMeta(brotherMethod, handler)
    }
}