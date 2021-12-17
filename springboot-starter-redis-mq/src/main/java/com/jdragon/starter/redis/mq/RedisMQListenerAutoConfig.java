package com.jdragon.starter.redis.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdragon.starter.redis.mq.core.RedisListenerAnnotationScanPostProcesser;
import com.jdragon.starter.redis.mq.core.RedisMQSender;
import com.jdragon.starter.redis.mq.core.RedisMessageQueueRegister;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;

/**
 * @Author JDragon
 * @Date 2021.12.17 上午 9:58
 * @Email 1061917196@qq.com
 * @Des:
 */

@Configuration
//@ConditionalOnBean(RedisConnectionFactory.class)
//@ConditionalOnProperty(prefix = "redis.queue.listener", name = "enable", havingValue = "true", matchIfMissing = true)
public class RedisMQListenerAutoConfig {

    @Bean
    public RedisListenerAnnotationScanPostProcesser redisListenerAnnotationScanPostProcesser() {
        return new RedisListenerAnnotationScanPostProcesser();
    }

    @Bean
    public StringRedisTemplate redisMQTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        return stringRedisTemplate;
    }

    @Bean
    public RedisMessageQueueRegister redisMessageQueueRegister() {
        return new RedisMessageQueueRegister();
    }


    @Bean
    public RedisMQSender redisMQSender(StringRedisTemplate redisMQTemplate , ObjectMapper objectMapper) {
        return new RedisMQSender(redisMQTemplate, objectMapper);
    }

    @Bean
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> streamMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> options = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofSeconds(2L))
                .targetType(String.class)
                .executor(new SimpleAsyncTaskExecutor())
                .build();

        StreamMessageListenerContainer<String, ObjectRecord<String, String>> stringMapRecordStreamMessageListenerContainer =
                StreamMessageListenerContainer.create(redisConnectionFactory, options);
        stringMapRecordStreamMessageListenerContainer.start();
        return stringMapRecordStreamMessageListenerContainer;
    }
}
