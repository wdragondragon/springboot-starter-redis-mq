package com.jdragon.starter.redis.mq.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdragon.starter.redis.mq.core.domain.RedisMessage;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Date;

public class RedisMQSender {

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    public RedisMQSender(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> void send(String queueName, T object) {
        if (object == null) return;
        try {
            RedisMessage<T> redisMessage = new RedisMessage<>();
            redisMessage.setQueueName(queueName);
            redisMessage.setCreateTime(new Date());
            redisMessage.setData(object);
            String json = objectMapper.writeValueAsString(redisMessage);
            ObjectRecord<String, String> objectRedisMessageObjectRecord = StreamRecords.objectBacked(json)
                    .withStreamKey(queueName);
            redisTemplate.opsForStream().add(objectRedisMessageObjectRecord);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
