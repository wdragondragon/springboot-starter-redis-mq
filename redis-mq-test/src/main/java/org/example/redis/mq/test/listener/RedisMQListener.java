package org.example.redis.mq.test.listener;

import lombok.extern.slf4j.Slf4j;
import com.jdragon.starter.redis.mq.annotations.RedisListener;
import com.jdragon.starter.redis.mq.core.domain.RedisMessage;
import org.example.redis.mq.test.entity.Job;
import org.springframework.stereotype.Component;

/**
 * @Author JDragon
 * @Date 2021.12.17 上午 11:06
 * @Email 1061917196@qq.com
 * @Des:
 */
@Slf4j
@Component
public class RedisMQListener {
    @RedisListener(queueName = "streamKey")
    public void test(RedisMessage<String> redisMessage) {
        log.info("redis message 接受到信息:{}", redisMessage.getData());
    }

    @RedisListener(queueName = "streamKey2")
    public void test2(RedisMessage<Job> job) {
        log.info("redis message 接受到信息:{}", job.getData());
    }

    @RedisListener(queueName = "streamKey")
    public void test3(String redisMessage) {
        log.info("接受到信息:{}", redisMessage);
    }

    @RedisListener(queueName = "streamKey2")
    public void test4(Job job) {
        log.info("接受到信息:{}", job);
    }
}
