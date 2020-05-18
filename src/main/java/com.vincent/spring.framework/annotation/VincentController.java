package com.vincent.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * 页面交互
 * @author vincent
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VincentController {

    String value() default "";
}
