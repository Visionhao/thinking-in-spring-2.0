package com.vincent.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * 自动注入
 * @author vincent
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VincentAutowired {

    String value() default "";
}
