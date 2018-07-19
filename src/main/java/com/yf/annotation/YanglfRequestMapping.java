package com.yf.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YanglfRequestMapping {

    /**
     * 方法映射的url
     * @return
     */
    String value() default "";

}
