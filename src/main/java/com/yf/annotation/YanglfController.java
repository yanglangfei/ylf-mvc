package com.yf.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YanglfController {

    /**
     * controller 注册的别名
     * @return
     */
    String value() default "";

}
