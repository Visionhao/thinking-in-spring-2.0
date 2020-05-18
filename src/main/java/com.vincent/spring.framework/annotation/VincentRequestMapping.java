package com.vincent.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * 请求url
 * @author vincent
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VincentRequestMapping {

    String value() default "";
}
