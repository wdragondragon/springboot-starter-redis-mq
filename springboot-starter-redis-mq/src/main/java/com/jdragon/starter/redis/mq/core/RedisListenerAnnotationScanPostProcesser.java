package com.jdragon.starter.redis.mq.core;

import com.jdragon.starter.redis.mq.annotations.RedisListener;
import com.jdragon.starter.redis.mq.core.domain.RedisListenerMethod;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 注册redis消息队列处理方法，@RedisListener注解扫描器
 */
public class RedisListenerAnnotationScanPostProcesser implements BeanPostProcessor {

    private static final Map<String, List<RedisListenerMethod>> candidates = new HashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(clazz);
        for (Method method : methods) {
            AnnotationAttributes annotationAttributes = AnnotatedElementUtils
                    .findMergedAnnotationAttributes(method, RedisListener.class, false, false);
            if (null != annotationAttributes) {
                Class<?>[] parameterTypes = method.getParameterTypes();
//                if (parameterTypes.length == 1 && RedisMessage.class.isAssignableFrom(parameterTypes[0])) {
                if (parameterTypes.length == 1) {
                    String queueName = (String) annotationAttributes.get("queueName");
                    String group = (String) annotationAttributes.get("group");
                    String consumer = (String) annotationAttributes.get("consumer");
                    Type[] genericParameterTypes = method.getGenericParameterTypes();
                    RedisListenerMethod rlm = new RedisListenerMethod();
                    rlm.setBeanName(beanName);
                    rlm.setTargetMethod(method);
                    rlm.setMethodParameterClassName(parameterTypes[0].getName());
                    rlm.setParameterClass(parameterTypes[0]);
                    rlm.setParameterType(genericParameterTypes[0]);
                    String key = queueName + "-" + group + "-" + consumer;
                    if (!candidates.containsKey(key)) {
                        candidates.put(key, new LinkedList<>());
                    }
                    candidates.get(key).add(rlm);
                } else {
                    throw new RuntimeException("有@RedisListener注解的方法有且仅能包含一个参数");
                }
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public static Map<String, List<RedisListenerMethod>> getCandidates() {
        return candidates;
    }
}
