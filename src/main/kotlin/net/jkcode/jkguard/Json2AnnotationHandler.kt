package net.jkcode.jkguard

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import net.jkcode.jkutil.common.exprTo
import java.lang.UnsupportedOperationException
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * json转注解的代理调用处理器
 */
class Json2AnnotationHandler(json: String) : InvocationHandler {

    companion object{

        /**
         * 构建注解实例
         * @param clazz 注解类
         * @param json
         * @return 注解实例
         */
        public fun <T> json2Annotation(clazz: Class<T>, json: String): T {
            return Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(clazz), Json2AnnotationHandler(json)) as T
        }

        /**
         * 构建注解实例
         * @param json
         * @return 注解实例
         */
        public inline fun <reified T> json2Annotation(json: String): T {
            return json2Annotation(T::class.java, json)
        }
    }

    /**
     * 属性值
     *   key是注解属性名, 也是注解接口的方法名
     *   value是注解属性值
     */
    protected val attrs: Map<String, Any?> = JSON.parse(json) as JSONObject

    /**
     * 方法代理处理
     */
    public override fun invoke(proxy: Any, method: Method, args: Array<Any?>?): Any? {
        // 属性不存在
        if(method.name !in attrs)
            throw UnsupportedOperationException("属性[${method.name}]不存在")

        // 获得属性值
        val value = attrs[method.name] as String?
        if(value == null)
            return null

        // 转换类型
        return value.exprTo(method.returnType.kotlin)
    }
}