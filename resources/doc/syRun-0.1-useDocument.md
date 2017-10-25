# 简介
This document is applicable to version syRun-0.1.X.
# 启动
- 服务器端启动
通过SyRunTCPServer.init(String serverIP,int serverPort),传入你的服务器ip地址和tcp端口号.
此方法默认即使重复调用也只会生成一个服务器实例于内存中运行.
- 客户端启动
客户端无需手动启动,默认调用api方法的时候系统会自动启动客户端并进行连接.

# 接口使用说明
目前客户端的功能都通过SyRunClientUtil类的静态方法调用实现.
通过传入不同的参数和得到的返回值来实现功能.
- 同步的方法通过返回值来获得服务器响应信息
- 异步的方法(如果有)和同步的方法的方法名相同,但是会多传入一个接口回调来得到服务器的相应.
在test目录下的测试类中有一些测试用例,可以当成Demo查看.
- 返回值中有的是返回的消息或者操作的状态,其值可以在
com.tingfeng.syRun.common.ResponseStatus中枚举的value值查看到.

# 方法说明
1. doSingeStepWorkByCounter
public static <T> T doSingeStepWorkByCounter(final FrequencyControlHelper<T> fc,String key,long sleepTime);
此方法是通过计数器模拟的实现同步锁功能的方法,效率低下,仅仅作为测试使用,不推荐使用.

## 计数器counter相关
计数器的所有方法都是跨客户端的线程安全的.
全局过期时间单位毫秒,
1. initCounter
int initCounter(final String key,final long value,final long expireTime)
初始化计数器,其中key是当前计数器的自定的key非空字符串,value是计数器的初始化值.expireTime是过期的具体时间的毫秒数.
默认的返回值是操作状态,0表示成功.
2. setCounterValue
int setCounterValue(final String key,final long value)
设置计数器的值,如果计数器不存在,会自动初始化一个默认超时时间的计数器并设置对应值.
返回操作状态.
3. setCounterExpireTime
int setCounterExpireTime(final String key,final long expireTime)
设置计数器过期时间,返回操作状态.
4. getCounterExpireTime
long getCounterExpireTime(final String key)
得到一个计数器的过期时间.
5. getCounterValue
long getCounterValue(String key)
得到一个计数器的值.
6.addCounterValue
long addCounterValue(String key,long value)
给一个计数器加上指定值.可以加上一个负值.

## 锁相关
锁相关的方法都是跨客户端的线程安全的.
getLock和releaseLock模拟了一个线程中常用的同步锁的功能,通过客户端和服务器端的方式
来实现了跨客户端的锁功能,在分布式环境中具有更大的用处.
1. getLock
String getLock(String key)
获得指定key(名称)的一个锁.成功则返回此锁的解锁id字符串.
直到调用解锁方法并传入返回的解锁id字符串解锁前为止,对其它调用此方法并传入相同key的方法实现阻塞.
失败则抛出异常.
2. releaseLock
int releaseLock(String key,String lockId)
释放锁,传入key(锁的名称),lockId(调用getLock方法得到的解锁id字符串).
调用此方法可以释放名称和key内容相同的锁,传入相同key的getLock在服务器改变阻塞状态.
返回操作状态.