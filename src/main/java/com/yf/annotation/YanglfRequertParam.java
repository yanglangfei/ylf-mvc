package com.yf.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YanglfRequertParam {

    /**
     * 参数名
     * @return
     */
    String value() ;
}
