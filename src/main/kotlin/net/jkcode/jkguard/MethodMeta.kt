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
        public override val handler: IMethodGuardInvoker //
): IMethodMeta {

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
     * 方法签名
     */
    override val methodSignature: String
        get() = method.getSignature()

    /**
     * 方法参数类型
     */
    override val parameterTypes: Array<Class<*>?>
        get() = method.parameterTypes

    /**
     * 返回值类型
     */
    override val returnType: Class<*>
        get() = method.returnType

    /**
     * 方法守护者
     */
    override val methodGuard: IMethodGuard
        get(){
            return methodGuards.getOrPut(method){
                MethodGuard(this)
            }
        }

    /**
     * 获得方法注解
     * @param annotationClass 注解类
     * @return
     */
    override fun <A : Annotation> getAnnotation(annotationClass: Class<A>): A? {
        return method.getCachedAnnotation(annotationClass)
    }

    /**
     * 调用方法
     */
    override fun invoke(obj: Any, vararg args: Any?): Any? {
        return method.invoke(obj, *args)
    }

    /**
     * 从CompletableFuture获得方法结果值
     *
     * @param resFuture
     * @return
     */
    @Suspendable
    override fun getResultFromFuture(resFuture: CompletableFuture<*>): Any?{
        return method.getResultFromFuture(resFuture)
    }

    /**
     * 获得兄弟方法
     * @param name 兄弟方法名
     * @return
     */
    override fun getBrotherMethod(name: String): IMethodMeta{
        val brotherMethod = method.declaringClass.methods.first {
            it.name == name
        }
        return MethodMeta(brotherMethod, handler)
    }


    companion object{

        /**
         * 方法守护者
         */
        protected val methodGuards: ConcurrentHashMap<Method, MethodGuard> = ConcurrentHashMap();
    }
}