package net.jkcode.jkguard

import com.alibaba.fastjson.JSON
import net.jkcode.jkguard.annotation.RateLimit
import org.junit.Test
import java.lang.reflect.Proxy

class AnnotationReflectTests{


    @Test
    fun testAnnotationProxy(){
        // 注解类，其实就是接口类
        // 注解属性，其实就是接口的方法
//        val c = RateLimit::class.java
//        print(c)

        val attrs = "{\"permitsPerSecond\":\"100.0\",\"stablePeriodSeconds\":\"0\",\"warmupPeriodSeconds\":\"0\"}"
        val clazz = RateLimit::class.java
        val o = Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(clazz), Map2AnnotationHandler(clazz, JSON.parse(attrs) as Map<String, Any?>)) as RateLimit
        println(o.permitsPerSecond)
        println(o.stablePeriodSeconds)
    }

}