package net.jkcode.jkguard.annotation

import net.jkcode.jkguard.IMethodMeta
import net.jkcode.jkutil.common.getCachedAnnotation
import java.lang.reflect.Method

/**
 * 针对key的进行合并的注解
 *    异步执行, 注意Threadlocal无法传递
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class KeyCombine()

/**
 * 获得key合并的注解
 */
public val Method.keyCombine: KeyCombine?
    get(){
        return getCachedAnnotation()
    }

/**
 * 获得key合并的注解
 */
public val IMethodMeta<*>.keyCombine: KeyCombine?
    get(){
        return getAnnotation()
    }