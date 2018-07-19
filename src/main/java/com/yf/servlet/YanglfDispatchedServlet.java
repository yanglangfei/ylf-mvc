package com.yf.servlet;
import com.yf.annotation.YanglfController;
import com.yf.annotation.YanglfRequestMapping;
import com.yf.annotation.YanglfService;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class YanglfDispatchedServlet extends HttpServlet {

    private Properties properties = new Properties();
   private List<String> classNames=new ArrayList<>();
   private Map<String,Object> beans=new HashMap<>();
   // @RequestMapping
   private Map<String,Method> handleMap=new HashMap<>();
   // @Controller
   private Map<String,Object> controllerMap=new HashMap<>();


    @Override
    public void init(ServletConfig config) {
          //加载配置文件里的参数
       //   loadConfiguration(config);
          //将所有的.class 文件加入集合
          scanPackage("com.yf");
          //通过反射拿到class实例，放入IOC容器
          doInstance();
          for(Map.Entry<String,Object> entity : beans.entrySet()){
              System.out.println("key:"+entity.getKey()+";value:"+entity.getValue());
          }
          // 依赖注入  将url和method对应上
          doAutoWired();

    }

    private void loadConfiguration(ServletConfig config) {
        String configInitParameter = config.getInitParameter("contextConfigLocation");
        //把web.xml中的contextConfigLocation对应value值的文件加载到流里面
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(configInitParameter);
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //关流
            if(null!=resourceAsStream) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doAutoWired() {
        if(beans.size()<=0){
            System.out.println("找不到beans文件");
            return;
        }
        for (Map.Entry<String,Object> entity : beans.entrySet()){
            Class<?> clazz = entity.getValue().getClass();
            if(!clazz.isAnnotationPresent(YanglfRequestMapping.class)){
                continue;
            }

            String baseUrl ="";
            YanglfRequestMapping mapping = clazz.getAnnotation(YanglfRequestMapping.class);
            baseUrl = mapping.value();

            Method [] methods = clazz.getMethods();
            for (Method method : methods) {
                if(method.isAnnotationPresent(YanglfRequestMapping.class)){
                    YanglfRequestMapping annotation = method.getAnnotation(YanglfRequestMapping.class);
                    String url = annotation.value();
                    url =(baseUrl+"/"+url).replaceAll("/+", "/");
                    handleMap.put(url,method);
                    try {
                        controllerMap.put(url,clazz.newInstance());
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
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
                    beans.put(toLowerFirstWord(cName.getSimpleName()), instance);
                }else if(cName.isAnnotationPresent(YanglfService.class)){
                    Object instance = cName.newInstance();
                    beans.put(toLowerFirstWord(cName.getSimpleName()), instance);
                }else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 把字符串的首字母小写
     * @param name
     * @return
     */
    private String toLowerFirstWord(String name){
        char[] charArray = name.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
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
        this.doPost(req,resp);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatch(req,resp);
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
        if(handleMap.isEmpty()){
            return;
        }
        String url =req.getRequestURI();
        String contextPath = req.getContextPath();

        url=url.replace(contextPath, "").replaceAll("/+", "/");
        if(!this.handleMap.containsKey(url)){
            try {
                resp.getWriter().write("404");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        Method method =this.handleMap.get(url);
        //获取方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        //获取请求的参数
        Map<String, String[]> parameterMap = req.getParameterMap();

        //保存参数值
        Object [] paramValues= new Object[parameterTypes.length];
        //方法的参数列表
        for (int i=0;i<parameterTypes.length;i++ ) {
            //根据参数名称，做某些处理
            String requestParam = parameterTypes[i].getSimpleName();
            if(requestParam.equals("HttpServletRequest")){
                paramValues[i]=req;
                continue;
            }
            if(requestParam.equals("HttpServletResponse")){
                paramValues[i]=resp;
                continue;
            }
            if(requestParam.equals("String")){
                for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                    String value =Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
                    paramValues[i]=value;
                }
            }
        }

        try {
            method.invoke(this.controllerMap.get(url),paramValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
