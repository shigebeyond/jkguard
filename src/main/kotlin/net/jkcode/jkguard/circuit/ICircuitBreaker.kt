package net.jkcode.jkguard.circuit

import net.jkcode.jkguard.rate.IRateLimiter

/**
 * 断路器
 *    继承限流器, 在断路状态下, 有限流作用
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 10:30 PM
 */
interface ICircuitBreaker: IRateLimiter {
}