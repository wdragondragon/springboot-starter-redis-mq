package com.jdragon.starter.redis.mq.core.domain;

import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class RedisListenerMethod {
    private Object bean;

    private String beanName;

    private Method targetMethod;

    private String methodParameterClassName;

    private Class<?> parameterClass;

    private Type parameterType;


    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    public void setParameterClass(Class<?> parameterClass){
        this.parameterClass = parameterClass;
    }

    public Class<?> getParameterClass() {
        return parameterClass;
    }

    public void setParameterType(Type parameterType) {
        this.parameterType = parameterType;
    }

    public Type getParameterType() {
        return parameterType;
    }

    public Object getBean(ApplicationContext applicationContext) {
        if (bean == null) {
            synchronized (this) {
                if (bean == null) {
                    bean = applicationContext.getBean(beanName);
                    if (bean == null) {
                        throw new RuntimeException("获取包含@RedisLister[" + targetMethod.getName() + "]方法的Bean实例失败");
                    }
                }
            }
        }
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }


    public String getMethodParameterClassName() {
        return methodParameterClassName;
    }

    public void setMethodParameterClassName(String methodParameterClassName) {
        this.methodParameterClassName = methodParameterClassName;
    }
}
