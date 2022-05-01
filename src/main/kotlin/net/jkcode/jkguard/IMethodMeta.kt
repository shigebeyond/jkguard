package net.jkcode.jkguard

import java.util.concurrent.CompletableFuture

/**
 * 方法元数据
 *   为了兼容java方法与php方法，才抽取的IMethodMeta
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-4-27 7:25 PM
 */
interface IMethodMeta {

    /**
     * 类名
     */
    val clazzName: String

    /**
     * 方法名
     */
    val methodName: String

    /**
     * 方法签名
     */
    val methodSignature: String

    /**
     * 带类名的方法签名
     */
    val fullSignature: String
        get() = clazzName + "#" + methodSignature

    /**
     * 方法参数类型
     */
    val parameterTypes: Array<Class<*>?>

    /**
     * 返回值类型
     */
    val returnType: Class<*>

    /**
     * 带守护的方法调用者
     *   传递给 MethodGuard
     */
    val handler: IMethodGuardInvoker

    /**
     * 方法守护者
     */
    val methodGuard: IMethodGuard

    /**
     * 获得方法注解
     * @param annotationClass 注解类
     * @return
     */
    fun <A : Annotation> getAnnotation(annotationClass: Class<A>): A?

    /**
     * 调用方法
     */
    fun invoke(obj: Any, vararg args: Any?): Any?

    /**
     * 从CompletableFuture获得方法结果值
     *
     * @param resFuture
     * @return
     */
    fun getResultFromFuture(resFuture: CompletableFuture<*>): Any?

    /**
     * 获得兄弟方法
     * @param name 兄弟方法名
     * @return
     */
    fun getBrotherMethod(name: String): IMethodMeta

}