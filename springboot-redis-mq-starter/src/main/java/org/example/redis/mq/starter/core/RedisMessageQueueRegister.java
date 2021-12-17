package org.example.redis.mq.starter.core;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.redis.mq.starter.core.domain.RedisListenerMethod;
import org.example.redis.mq.starter.core.domain.RedisMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RedisMessageQueueRegister implements ApplicationRunner, ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(RedisMessageQueueRegister.class);

    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("redisMQTemplate")
    private StringRedisTemplate redisTemplate;

    @Autowired
    private StreamMessageListenerContainer<String, ObjectRecord<String, String>> streamMessageListenerContainer;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 启动redis消息队列监听器
        Map<String, List<RedisListenerMethod>> candidates = RedisListenerAnnotationScanPostProcesser.getCandidates();

        for (Map.Entry<String, List<RedisListenerMethod>> entry : candidates.entrySet()) {
            String queueKey = entry.getKey();
            List<RedisListenerMethod> redisListenerMethodList = entry.getValue();
            String[] split = queueKey.split("-");
            String streamKey = split[0];
            String consumerGroupName = split[1];
            String consumerName = split[2];
            try {
                List<String> groupName = redisTemplate.opsForStream().groups(streamKey).stream().map(StreamInfo.XInfoGroup::groupName).collect(Collectors.toList());
                if (!groupName.contains(consumerGroupName)) {
                    redisTemplate.opsForStream().createGroup(streamKey, consumerGroupName);
                }
            } catch (Exception e) {
                redisTemplate.opsForStream().createGroup(streamKey, consumerGroupName);
            }

            streamMessageListenerContainer.receive(
                    Consumer.from(consumerGroupName, consumerName),
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                    message -> {
                        try {
                            logger.info("stream message。messageId={}, stream={}, body={}",
                                    message.getId(), message.getStream(), message.getValue());
                            String messageJson = message.getValue();
                            for (RedisListenerMethod rlm : redisListenerMethodList) {
                                Method targetMethod = rlm.getTargetMethod();
                                try {
                                    if (rlm.getMethodParameterClassName().equals(RedisMessage.class.getName())) {
                                        RedisMessage<?> redisMessage = objectMapper.readValue(messageJson, RedisMessage.class);
                                        targetMethod.invoke(rlm.getBean(applicationContext), redisMessage);
                                    } else {
                                        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(RedisMessage.class, rlm.getParameterClass());
                                        RedisMessage<?> redisMessage = objectMapper.readValue(messageJson, javaType);
                                        targetMethod.invoke(rlm.getBean(applicationContext), redisMessage.getData());
                                    }
                                } catch (IllegalAccessException | InvocationTargetException | IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            redisTemplate.opsForStream().acknowledge(consumerGroupName, message);
                        } catch (Exception e) {
                            logger.error("", e);
                        }
                    });
            logger.info("启动消息队列监听器：【" + streamKey + "." + consumerGroupName + "】");
        }
    }
}
