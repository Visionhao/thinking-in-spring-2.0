package com.vincent.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * 请求参数映射
 * @author vincent
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VincentRequestParam {

    String value() default "";

    boolean required() default true;
}
