package net.jkcode.jkguard

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * 方法元数据
 *   为了兼容java方法与php方法，才抽取的IMethodMeta
 *   由于 getAnnotation() 是inline, 因此该接口类改为抽象类
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-4-27 7:25 PM
 */
abstract class IMethodMeta<Return>(
        public val handler: IMethodGuardInvoker // 带守护的方法调用者, 要传递给 MethodGuard
) {

    companion object{
        /**
         * 方法守护者
         */
        protected val methodGuards: ConcurrentHashMap<String, MethodGuard> = ConcurrentHashMap();
    }

    /**
     * 类名
     */
    abstract val clazzName: String

    /**
     * 方法名
     */
    abstract val methodName: String

    /**
     * 方法签名(rpc用到)
     */
    abstract val methodSignature: String

    /**
     * 带类名的方法签名
     */
    val fullSignature: String
        get() = clazzName + "#" + methodSignature

    /**
     * 方法参数类型
     *    会在 degradeHandler/groupCombiner/keyCombiner 用来检查方法的参数与返回值类型
     */
    abstract val parameterTypes: Array<Class<*>>

    /**
     * 返回值类型
     */
    abstract val returnType: Class<*>

    /**
     * 是否纯php实现
     *    用来决定是否在 degradeHandler/groupCombiner/keyCombiner 用来检查方法的参数与返回值类型
     */
    abstract val isPurePhp: Boolean

    /**
     * 方法的守护者
     *   有缓存
     */
    val methodGuard: IMethodGuard
        get(){
            return methodGuards.getOrPut(fullSignature){
                MethodGuard(this)
            }
        }

    /**
     * 获得方法注解
     * @param annotationClass 注解类
     * @return
     */
    abstract fun <A : Annotation> getAnnotation(annotationClass: Class<A>): A?

    /**
     * 获得方法注解
     * @return
     */
    public inline fun <reified A : Annotation> getAnnotation(): A?{
        return getAnnotation(A::class.java)
    }

    /**
     * 方法处理
     *    在server端的IMethodGuardInvoker#invokeAfterGuard()/两端的降级处理中调用
     *    实现：server端实现是调用包装的本地方法, client端实现是发rpc请求
     */
    abstract fun invoke(obj: Any, vararg args: Any?): Return

    /**
     * 从CompletableFuture获得方法结果值
     *
     * @param resFuture
     * @return
     */
    abstract fun getResultFromFuture(resFuture: CompletableFuture<*>): Any?

    /**
     * 获得兄弟方法, 用在获得降级或合并的兄弟方法
     * @param name 兄弟方法名
     * @return
     */
    abstract fun getBrotherMethod(name: String): IMethodMeta<Return>

}