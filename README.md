## RedisMQ简介

在SpringBoot-Starter上，基于RedisTemplate的Redis Stream API实现的消息队列，注解驱动编程，快速上手。



## Quick Start

#### 1、拉取编译

```shell
git clone http://gitlab.tyu.wiki/jdragon/spring-redis-mq.git

mvn install
```



#### 2、依赖导入

```xml
<dependency>
    <groupId>com.jdragon.starter</groupId>
    <artifactId>springboot-starter-redis-mq</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```



#### 3、添加配置

使用的是springboot自带的RedisTemplate，可通过spring自带参数设置redismq的配置

```yml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
```



####  4、生产消费示例

```java
@Data
public class Job {
    private Integer id;

    private Map<String,Object> param;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date createDate;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endDate;
}
```



##### 1）生产者

创建一个TestController类，注入`RedisMQSender`
PS：redis发送生产消息的方法`RedisMQSender.send(参数1，参数2)`：

- 参数1是String类型的消息队列名称
- 参数2是你想传递的任意数据。

```java
@RestController
@RequestMapping("test")
public class TestController {

    @Autowired
    private RedisMQSender redisMQSender;

    @GetMapping("redisMQSender")
    public String redisMQSender() {
        String streamKey = "streamKey";
        redisMQSender.send(streamKey, "你好");
        return "成功";
    }

    @GetMapping("redisMQSender2")
    public String redisMQSender2() {
        String streamKey = "streamKey2";

        HashMap<String, Object> param = new HashMap<>();
        param.put("id", 1);
        param.put("name", "张三");
        param.put("paramInline", new HashMap<>());

        Job job = new Job();
        job.setId(1);
        job.setParam(param);
        job.setCreateDate(new Date());
        job.setEndDate(LocalDateTime.now());
        redisMQSender.send(streamKey, job);
        return "成功";
    }
}
```



##### 2）消费者

创建一个RedisListenerContainer类用于定义redis队列消息监听处理方法。

@RedisListener注解支持参数

- queueName：监听队列名称，默认为default_queue，表示该方法你需要处理的哪个队列的消息。
- group：消费者组，默认为default_group，消费者组内存在竞争关系。
- customer：消费者名称，默认为consumer



PS： 实现redis队列监听只需在Spring容器所管理的Bean中的方法上添加注解`@RedisListener(queueName,group,customer)`，注意被@RedisListener修饰的方法只能包含一个参数，这个参数的可以一个`RedisMessage`类型的参数，也可以是你需要传递的直接消息类型。

```java
@Slf4j
@Component
public class RedisMQListener {
    @RedisListener(queueName = "streamKey")
    public void test(RedisMessage<String> redisMessage) {
        log.info("redis message 接受到信息:{}", redisMessage.getData());
    }

    @RedisListener(queueName = "streamKey")
    public void test2(String redisMessage) {
        log.info("接受到信息:{}", redisMessage);
    }
    
    @RedisListener(queueName = "streamKey2")
    public void test3(RedisMessage<Job> job) {
        log.info("redis message 接受到信息:{}", job.getData());
    }

    @RedisListener(queueName = "streamKey2")
    public void test4(Job job) {
        log.info("接受到信息:{}", job);
    }
}
```



#### 3）启动结果



见以下日志打印，即为启动监听成功

```powershell
2021-12-17 17:08:26.787 [main] INFO  c.j.s.r.m.c.RedisMessageQueueRegister [run 101] - 启动消息队列监听器：【streamKey2.default_group】
2021-12-17 17:08:26.871 [main] INFO  c.j.s.r.m.c.RedisMessageQueueRegister [run 101] - 启动消息队列监听器：【streamKey.default_group】
```



请求`/test/redisMQSender`

```powershell
2021-12-17 17:11:14.633 [SimpleAsyncTaskExecutor-2] INFO  c.j.s.r.m.c.RedisMessageQueueRegister [lambda$run$0 78] - stream message。messageId=1639732274611-0, stream=streamKey, body={"queueName":"streamKey","data":"你好","createTime":"2021-12-17 05:11:14"}
2021-12-17 17:11:14.634 [SimpleAsyncTaskExecutor-2] INFO  o.e.r.m.t.listener.RedisMQListener [test 20] - redis message 接受到信息:你好
2021-12-17 17:11:14.635 [SimpleAsyncTaskExecutor-2] INFO  o.e.r.m.t.listener.RedisMQListener [test3 30] - 接受到信息:你好
```



请求`/test/redisMQSender2`

```powershell
2021-12-17 17:11:20.547 [SimpleAsyncTaskExecutor-1] INFO  c.j.s.r.m.c.RedisMessageQueueRegister [lambda$run$0 78] - stream message。messageId=1639732280628-0, stream=streamKey2, body={"queueName":"streamKey2","data":{"id":1,"param":{"paramInline":{},"name":"张三","id":1},"createDate":"2021-12-17 05:11:20","endDate":"2021-12-17T17:11:20.461"},"createTime":"2021-12-17 05:11:20"}
2021-12-17 17:11:20.547 [SimpleAsyncTaskExecutor-1] INFO  o.e.r.m.t.listener.RedisMQListener [test2 25] - redis message 接受到信息:{id=1, param={paramInline={}, name=张三, id=1}, createDate=2021-12-17 05:11:20, endDate=2021-12-17T17:11:20.461}
2021-12-17 17:11:20.548 [SimpleAsyncTaskExecutor-1] INFO  o.e.r.m.t.listener.RedisMQListener [test4 35] - 接受到信息:Job(id=1, param={paramInline={}, name=张三, id=1}, createDate=Fri Dec 17 05:11:20 CST 2021, endDate=2021-12-17T17:11:20.461)
```

