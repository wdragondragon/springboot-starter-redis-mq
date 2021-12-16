package org.example.redis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author JDragon
 * @Date 2021.12.16 下午 4:47
 * @Email 1061917196@qq.com
 * @Des:
 */

@Api
@Slf4j
@RestController
@RequestMapping("test")
public class TestController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("pop")
    @ApiOperation("pop")
    public Object pop(){
        Object test = redisTemplate.opsForList().leftPop("test",0, TimeUnit.SECONDS);
        return test;
    }

    @GetMapping("push")
    @ApiOperation("push")
    public Long push(@RequestParam String value){
        Long aLong = redisTemplate.opsForList().rightPush("test", value);
        return aLong;
    }

    @GetMapping("stream")
    @ApiOperation("stream")
    public String stream() throws JsonProcessingException {
        String streamKey = "streamKey";
        Map<String, Object> data = new HashMap<>();
        data.putIfAbsent("date", new Date());
        data.putIfAbsent("age", 11);
        StringRecord stringRecord = StreamRecords.string(Collections.singletonMap("name", new ObjectMapper().writeValueAsString(data))).withStreamKey(streamKey);
        RecordId recordId = stringRedisTemplate.opsForStream().add(stringRecord);
        log.info(recordId.toString());
        return "成功";
    }
}
