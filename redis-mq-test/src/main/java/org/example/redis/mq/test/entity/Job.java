package org.example.redis.mq.test.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

/**
 * @Author JDragon
 * @Date 2021.12.17 上午 11:29
 * @Email 1061917196@qq.com
 * @Des:
 */
@Data
public class Job implements Serializable {
    private Integer id;

    private Map<String,Object> param;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date createDate;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endDate;
}
