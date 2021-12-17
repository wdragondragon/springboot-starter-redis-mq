package org.example.redis.mq.test.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.example.redis.mq.starter.core.RedisMQSender;
import org.example.redis.mq.test.entity.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;

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
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("stream")
    @ApiOperation("stream")
    public String stream() throws JsonProcessingException {
        String streamKey = "streamKey2";
        Job job = new Job();
        job.setId(1);
        job.setParam(new HashMap<>());
        job.setCreateDate(new Date());
        job.setEndDate(LocalDateTime.now());
        String json = objectMapper.writeValueAsString(job);
        ObjectRecord<String, String> objectRedisMessageObjectRecord = StreamRecords.objectBacked(json)
                .withStreamKey(streamKey);
        stringRedisTemplate.opsForStream().add(objectRedisMessageObjectRecord);
        return "成功";
    }

    @Autowired
    private RedisMQSender redisMQSender;

    @GetMapping("redisMQSender")
    @ApiOperation("redisMQSender")
    public String redisMQSender() {
        String streamKey = "streamKey";
        redisMQSender.send(streamKey, "你好");
        return "成功";
    }

    @GetMapping("redisMQSender2")
    @ApiOperation("redisMQSender2")
    public String redisMQSender2() {
        String streamKey = "streamKey2";
        Job job = new Job();
        job.setId(1);
        HashMap<String, Object> param = new HashMap<>();
        param.put("id", 1);
        param.put("name", "张三");
        param.put("paramInline", new HashMap<>());
        job.setParam(param);
        job.setCreateDate(new Date());
        job.setEndDate(LocalDateTime.now());
        redisMQSender.send(streamKey, job);
        return "成功";
    }
}
