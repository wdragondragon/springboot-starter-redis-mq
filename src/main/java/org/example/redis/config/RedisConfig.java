package org.example.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.util.Map;

@Slf4j
@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    /**
     * 默认是JDK的序列化策略，这里配置redisTemplate采用的是Jackson2JsonRedisSerializer的序列化策略
     *
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式）
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会抛出异常
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 配置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //使用StringRedisSerializer来序列化和反序列化redis的key值
        //redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setKeySerializer(jackson2JsonRedisSerializer);
        // 值采用json序列化
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /***
     * stringRedisTemplate默认采用的是String的序列化策略
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        return stringRedisTemplate;
    }

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> stringMapRecordStreamMessageListenerContainer = StreamMessageListenerContainer.create(redisConnectionFactory);
        stringMapRecordStreamMessageListenerContainer.start();
        return stringMapRecordStreamMessageListenerContainer;
    }


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Bean
    public Subscription subscription(StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer) {
        String streamKey = "streamKey";
        String consumerGroupName = "group";
        String consumerName = "consumer";
        try {
            stringRedisTemplate.opsForStream().groups(streamKey).stream().forEach(e -> log.info(e.groupName()));
        } catch (Exception e) {
            stringRedisTemplate.opsForStream().createGroup(streamKey, consumerGroupName);
        }

        return streamMessageListenerContainer.receive(
                Consumer.from(consumerGroupName, consumerName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                message -> {
                    log.info("stream message。messageId={}, stream={}, body={}",
                            message.getId(), message.getStream(), message.getValue());
                    Map<String, String> record = message.getValue();
                    log.info("[{}]", record);
                    stringRedisTemplate.opsForStream().acknowledge(consumerGroupName, message);
                });
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
}
