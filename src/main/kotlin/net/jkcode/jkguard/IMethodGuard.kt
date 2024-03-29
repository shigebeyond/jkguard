package net.jkcode.jkguard

import net.jkcode.jkguard.cache.ICacheHandler
import net.jkcode.jkguard.circuit.ICircuitBreaker
import net.jkcode.jkguard.combiner.GroupFutureSupplierCombiner
import net.jkcode.jkguard.combiner.KeyFutureSupplierCombiner
import net.jkcode.jkguard.degrade.IDegradeHandler
import net.jkcode.jkguard.measure.IMeasurer
import net.jkcode.jkguard.rate.IRateLimiter

/**
 * 方法调用的守护者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 12:26 PM
 */
interface IMethodGuard {

    /**
     * 守护的目标方法
     */
    val method: IMethodMeta<*>

    /**
     * 带守护的方法调用者
     */
    val handler: IMethodGuardInvoker
        get() = method.handler

    /**
     * 方法调用的对象
     *    合并后会异步调用其他方法, 原来方法的调用对象会丢失
     */
    val obj:Any
        get() = handler.getCombineInovkeObject(method)

    /**
     * 方法的key合并器
     *    兼容方法返回类型是CompletableFuture
     *    异步执行, 注意Threadlocal无法传递
     */
    val keyCombiner: KeyFutureSupplierCombiner<Any, Any?>?

    /**
     * 方法的group合并器
     *    兼容方法返回类型是CompletableFuture
     *    异步执行, 注意Threadlocal无法传递
     */
    val groupCombiner: GroupFutureSupplierCombiner<Any, Any?, Any>?

    /**
     * 缓存处理器
     *    异步执行, 注意Threadlocal无法传递
     */
    val cacheHandler: ICacheHandler?

    /**
     * 限流器
     */
    val rateLimiter: IRateLimiter?

    /**
     * 计量器
     */
    val measurer: IMeasurer?

    /**
     * 降级处理器
     */
    val degradeHandler: IDegradeHandler?

    /**
     * 断路器
     */
    val circuitBreaker: ICircuitBreaker?

}