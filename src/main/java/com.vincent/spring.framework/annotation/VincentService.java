package com.vincent.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * 业务逻辑，注入接口
 *
 * @author vincent
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VincentService {

    String value() default "";
}
