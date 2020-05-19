package com.vincent.spring.framework.webmvc.servlet;

import com.vincent.spring.framework.annotation.VincentController;
import com.vincent.spring.framework.annotation.VincentRequestMapping;
import com.vincent.spring.framework.annotation.VincentRequestParam;
import com.vincent.spring.framework.context.VincentApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 委派模式
 * 职责： 负责任务调度，请求分发
 * @author vincent
 */
public class VincentDispatcherServlet extends HttpServlet {

    private VincentApplicationContext applicationContext;

    //IoC容器，key默认是类名首字母小写，value就是对应的实例对象
    private Map<String,Object> ioc = new HashMap<String, Object>();

    //请求映射
    private Map<String,Method> handlerMapping = new HashMap<String, Method>();



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 6、 委派，根据url 去找到一个对应的 Method 并通过 response 返回
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception , Detail : " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // 获取请求的uri
        String url = req.getRequestURI();
        // 获取请求的路径
        String contextPath =  req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        if(!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found!!!");
            return;
        }

        //获取请求参数的列表
        Map<String,String[]> params = req.getParameterMap();

        //获取方法列表
        Method method = this.handlerMapping.get(url);

        //获取形参列表
        Class<?>[] paramterTypes = method.getParameterTypes();
        // 新建一个数组，长度为形式参数的length
        Object[] paramValues = new Object[paramterTypes.length];
        for (int i = 0; i < paramterTypes.length; i++) {
            // 获取 class 的类型
            Class paramterType = paramterTypes[i];
            if(paramterType == HttpServletRequest.class){
                paramValues[i] = req;
            }else if(paramterType == HttpServletResponse.class){
                paramValues[i] = resp;
            }else if(paramterType == String.class){
                //通过运行时的状态去拿到你
                Annotation[] [] pa = method.getParameterAnnotations();
                for (int j = 0; j < pa.length; j++) {
                    for(Annotation a : pa[i]){
                        if(a instanceof VincentRequestParam){
                            String paramName = ((VincentRequestParam) a).value();
                            if(!"".equals(paramName.trim())){
                                String value = Arrays.toString(params.get(paramName))
                                        .replaceAll("\\[|\\]","")
                                        .replaceAll("\\s+",",");
                                paramValues[i] = value;
                            }
                        }
                    }
                }
            }
        }
        
        //暂时硬编码
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        method.invoke(ioc.get(beanName),paramValues);

    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 初始化Spring 核心 IoC 容器
        applicationContext = new VincentApplicationContext(config.getInitParameter("contextConfigLocation"));

        doInitHandlerMapping();

        System.out.println("Vincent Spring framework is init....");
    }

    private void doInitHandlerMapping() {
        if(ioc.isEmpty()){
            return;
        }
        for(Map.Entry<String,Object> entry : ioc.entrySet()){
            Class<?> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(VincentController.class)){
                continue;
            }

            //相当于提取 class 上配置的url
            String baseUrl = "";
            if(!clazz.isAnnotationPresent(VincentRequestMapping.class)){
                VincentRequestMapping requestMapping = clazz.getAnnotation(VincentRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //只获取 public 的方法
            for(Method method : clazz.getMethods()){
                if(!method.isAnnotationPresent(VincentRequestMapping.class)){
                    continue;
                }
                //提取每个方法上面配置的url
                VincentRequestMapping requestMapping = method.getAnnotation(VincentRequestMapping.class);
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+","/");
                handlerMapping.put(url,method);
                System.out.println(" Mapped :  " + url + "," + method);
            }
        }
    }
}
