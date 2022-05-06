package net.jkcode.jkguard.annotation

import net.jkcode.jkguard.IMethodMeta
import net.jkcode.jkutil.common.getCachedAnnotation
import java.lang.reflect.Method

/**
 * 降级注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Degrade(
        public val fallbackMethod: String // 后备方法
)

/**
 * 获得降级的注解
 */
public val Method.degrade: Degrade?
    get(){
        return getCachedAnnotation()
    }

/**
 * 获得降级的注解
 */
public val IMethodMeta.degrade: Degrade?
    get(){
        return getAnnotation()
    }