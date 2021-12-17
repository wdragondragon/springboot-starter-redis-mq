package org.example.redis.mq.starter.annotations;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisListener {

    @AliasFor("queueName")
    String value() default "default_queue";

    @AliasFor("value")
    String queueName() default "default_queue";

    String group() default "default_group";

    String consumer() default "default_consumer";

}
