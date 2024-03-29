package net.jkcode.jkguard.combiner

import net.jkcode.jkutil.common.getInheritProperty
import net.jkcode.jkutil.flusher.RequestQueueFlusher
import net.jkcode.jkguard.annotation.GroupCombine
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KProperty1

/**
 * 针对每个group的(单参数)取值操作合并, 每个group攒够一定数量/时间的请求才执行
 *    如请求合并/cache合并等
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-10 9:47 AM
 */
open class GroupFutureSupplierCombiner<RequestArgumentType /* 请求参数类型 */, ResponseType /* 响应类型 */, BatchItemType: Any /* 批量取值操作的返回列表的元素类型 */>(
        public val reqArgField: String, // 请求参数对应的响应字段名
        public val respField: String = "", // 要返回的响应字段名, 如果为空则取响应对象
        public val one2one: Boolean = true, // 请求对响应是一对一(ResponseType是非List), 还是一对多(ResponseType是List)
        flushQuota: Int = 100, // 触发刷盘的队列大小
        flushTimeoutMillis: Long = 100, // 触发刷盘的定时时间
        public val batchFutureSupplier:(List<RequestArgumentType>) -> CompletableFuture<List<BatchItemType>> // 批量取值操作
): RequestQueueFlusher<RequestArgumentType, ResponseType>(flushQuota, flushTimeoutMillis){

    /**
     * 构造函数, 使用注解传参
     */
    public constructor(annotation: GroupCombine, batchFutureSupplier:(List<RequestArgumentType>) -> CompletableFuture<List<BatchItemType>>):this(annotation.reqArgField, annotation.respField, annotation.one2one, annotation.flushQuota, annotation.flushTimeoutMillis, batchFutureSupplier)

    /**
     * 处理刷盘的请求
     *     如果 ResponseType != Void/Unit, 则需要你主动设置异步响应
     * @param reqs
     * @param req2ResFuture
     * @return
     */
    protected override fun handleRequests(reqs: List<RequestArgumentType>, req2ResFuture: Collection<Pair<RequestArgumentType, CompletableFuture<ResponseType>>>): CompletableFuture<*> {
        // 1 执行批量操作
        val resultFuture: CompletableFuture<List<BatchItemType>> = batchFutureSupplier.invoke(reqs)

        // 2 设置异步响应
        resultFuture.thenAccept { result ->
            // 空响应
            if(result.isEmpty()) {
                req2ResFuture.forEach { ( req, resFuture) ->
                    resFuture.complete(null)
                }
                return@thenAccept
            }

            // 非空响应
            val clazz = result.first().javaClass

            // 获得的响应字段的getter
            var reqArgGetter: (Any) -> RequestArgumentType = getGetter(clazz, reqArgField) // 请求参数的getter
            var respGetter: (Any) -> Any? = getGetter(clazz, respField) // 返回值的getter

            // 根据请求参数来分组响应
            var req2res: Map<RequestArgumentType, Any?>
            var defaultReps: Any?
            if(one2one) { // 一对一
                req2res = result.associate {
                    reqArgGetter(it)!! to respGetter(it)
                }
                defaultReps = null
            }else { // 一对多
                req2res = result.groupBy(reqArgGetter, respGetter)
                defaultReps = emptyList<Any?>()
            }


            // 设置异步响应
            req2ResFuture.forEach { (req, resFuture) ->
                // 根据请求参数来获得响应
                val res = req2res.getOrDefault(req, defaultReps) as ResponseType
                resFuture.complete(res)
            }
        }

        return resultFuture
    }

    /**
     * 获得getter
     * @param clazz
     * @param field
     * @return
     */
    protected fun <T> getGetter(clazz: Class<BatchItemType>, field: String?): (Any) -> T {
        if(field.isNullOrEmpty())
            return { item: Any ->
                item as T
            }

        if (Map::class.java.isAssignableFrom(clazz)) {
            return { item: Any ->
                (item as Map<*, *>)[field] as T
            }
        }

        val prop = clazz.kotlin.getInheritProperty(field) as KProperty1<Any, T>
        return prop::get
    }
}