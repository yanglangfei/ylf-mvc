package com.yf.servlet;

import com.yf.annotation.YanglfController;
import com.yf.annotation.YanglfRequestMappering;
import com.yf.annotation.YanglfService;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispatchedServlet extends HttpServlet {

   private List<String> classNames=new ArrayList<>();
   private Map<String,Object> beans=new HashMap<>();
   private Map<String,Object> handleMap=new HashMap<>();


    @Override
    public void init() {
          //将所有的.class 文件加入集合
          scanPackage("com.yf");
          //IOC容器
          doInstance();
          for(Map.Entry<String,Object> entity : beans.entrySet()){
              System.out.println("key:"+entity.getKey()+";value:"+entity.getValue());

          }
          // 依赖注入
          doAutoWired();

    }

    private void doAutoWired() {
        if(beans.size()<=0){
            System.out.println("找不到beans文件");
            return;
        }
        for (Map.Entry<String,Object> entity : beans.entrySet()){
             Field[] fields = entity.getKey().getClass().getDeclaredFields();
            for (Field field : fields) {
                //可访问私有属性
                field.setAccessible(true);
                if(field.isAnnotationPresent(YanglfRequestMappering.class)){

                }
            }
        }

    }

    private void doInstance() {
        if(classNames.size()<=0){
            System.out.println("找不到class文件");
            return;
        }
        for (String className : classNames) {
            try {
                Class<?> cName = Class.forName(className.replace(".class", "").trim());
                if(cName.isAnnotationPresent(YanglfController.class)){
                    Object instance = cName.newInstance();
                    YanglfController annotation = cName.getAnnotation(YanglfController.class);
                    String key=annotation.value();
                    beans.put(key, instance);
                }else if(cName.isAnnotationPresent(YanglfService.class)){
                    Object instance = cName.newInstance();
                    YanglfService annotation = cName.getAnnotation(YanglfService.class);
                    String key=annotation.value();
                    beans.put(key, instance);
                }else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void scanPackage(String basePackage) {
        //扫描编译好的类路径下的所有类 .class
        URL url = this.getClass().getClassLoader().getResource("/" + basePackage.replaceAll("\\.", "/"));
        String fileUrl = url.getFile();
        File file=new File(fileUrl);

        String [] fileList=file.list();
        for (String path : fileList) {
            System.out.println("path:"+path);
            File filePath=new File(fileUrl+"/"+path);
            System.out.println("filePath:"+filePath.getAbsolutePath());
            if(filePath.isDirectory()){
                scanPackage(basePackage+"."+path);
            }else {
                //获取到 .class 文件
                //使用Map 存储class的路径
                classNames.add(basePackage+"."+filePath.getName());
            }
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
