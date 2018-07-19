package com.yf.controller;

import com.yf.annotation.YanglfController;
import com.yf.annotation.YanglfRequertParam;
import com.yf.annotation.YanglfRequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@YanglfController
@YanglfRequestMapping("/index")
public class IndexController {


    @YanglfRequestMapping("/add")
    public String toIndex(@YanglfRequertParam("name") String name, HttpServletResponse response){
        try {
            response.getWriter().write("hello,world!"+name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "hello world";
    }



}
