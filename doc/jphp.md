# 整合jphp -- IMethodMeta 抽象
抽象 IMethodMeta 体系, 以便兼容java方法与php方法, 从而将java/php方法调用都纳入 jkguard的守护体系中

## 1. IMethodMeta 概念
IMethodMeta是方法元数据，代表一个java或php的方法，包含以下的属性与操作

属性 | 作用
--- | ---
clazzName | 类名
methodName | 方法名
methodSignature | 方法签名(rpc用到)
parameterTypes | 方法参数类型
returnType | 返回值类型
isPurePhp | 是否纯php实现

操作 | 作用
--- | ---
getAnnotation | 获得方法注解
invoke | 方法处理
getResultFromFuture | 从CompletableFuture获得方法结果值
getBrotherMethod | 获得兄弟方法, 用在获得降级或合并的兄弟方法

## 2. IMethodMeta 类族
```
IMethodMeta (net.jkcode.jkguard)
    MethodMeta (net.jkcode.jkguard) -- 代表java方法
    PhpMethodMeta (net.jkcode.jphp.ext) -- 代表纯php方法 
    PhpRefererMethodMeta (net.jkcode.jksoa.rpc.client.jphp) -- 代表PhpRefererMethod(负责将 PhpReferer 的php方法调用转为发java rpc请求, 因此不算纯php方法)
```

## 3. IMethodMeta 使用
在方法守护者`MethodGuard`中，被守护的方法对象指定的是 IMethodMeta 类型，也就代表jkguard中的熔断/降级/限流/合并请求等守护功能都是应用在 IMethodMeta 的API级别，因此也就能守护java/php方法，甚至php的rpc方法(PhpRefererMethod) 
```
open class MethodGuard(
        public override val method: IMethodMeta // 被守护的方法
) : IMethodGuard
```