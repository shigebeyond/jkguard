package net.jkcode.jkguard

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import net.jkcode.jkutil.common.exprTo
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * map转注解的代理调用处理器
 */
class Map2AnnotationHandler(
        protected val clazz: Class<*>, // 注解类
        protected val attrs: Map<String, Any?> // 注解属性值, key是注解属性名, 也是注解接口的方法名, value是注解属性值){}
) : InvocationHandler {

    companion object{

        /**
         * 构建注解实例
         * @param clazz 注解类
         * @param json
         * @return 注解实例
         */
        public fun <T> json2Annotation(clazz: Class<T>, json: String): T {
            return map2Annotation(clazz, JSON.parse(json) as JSONObject)
        }

        /**
         * 构建注解实例
         * @param clazz 注解类
         * @param map
         * @return 注解实例
         */
        public fun <T> map2Annotation(clazz: Class<T>, attrs: Map<String, Any?>): T {
            return Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(clazz), Map2AnnotationHandler(clazz, attrs)) as T
        }
    }

    /**
     * 方法代理处理
     */
    public override fun invoke(proxy: Any, method: Method, args: Array<Any?>?): Any? {
        // 1 输出描述
        if(method.name == "toString")
            return clazz.canonicalName + JSON.toJSON(attrs)

        // 2 读属性值
        // 属性不存在
        if(method.name !in attrs)
            throw NoSuchMethodException("属性[${method.name}]不存在")

        // 获得属性值
        val value = attrs[method.name] as String?
        if(value == null)
            return null

        // 转换类型
        return value.exprTo(method.returnType.kotlin)
    }
}