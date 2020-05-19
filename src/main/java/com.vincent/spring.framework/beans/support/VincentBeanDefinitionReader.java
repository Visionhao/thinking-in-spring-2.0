package com.vincent.spring.framework.beans.support;

import com.vincent.spring.framework.beans.config.VincentBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * bean 的 reader
 * @author vincent
 */
public class VincentBeanDefinitionReader {

    //保存扫描的结果
    private List<String> regitryBeanClasses = new ArrayList<String>();

    private Properties contextConfig = new Properties();

    public VincentBeanDefinitionReader(String... configLocations){
        //获取配置文件的信息
        doLoadConfig(configLocations[0]);

        //扫描配置文件中的配置的相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
    }

    public List<VincentBeanDefinition> loadBeanDefinitions(){
        List<VincentBeanDefinition> result = new ArrayList<VincentBeanDefinition>();
        try{
            for (String className : regitryBeanClasses){
                //反射获取class
                Class<?> beanClass = Class.forName(className);
                //保存类对应的ClassName(全类名),beanName
                //1、默认是类名首字母小写
                result.add(doCreateBeanDefinition(doLowerFirstCase(beanClass.getSimpleName()),beanClass.getName()));

                //2、自定义
                //3、接口注入
                for(Class<?> i : beanClass.getInterfaces()){
                    result.add(doCreateBeanDefinition(i.getName(),beanClass.getName()));
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return result;

    }

    private String doLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private VincentBeanDefinition doCreateBeanDefinition(String beanName,String beanClassName){
        VincentBeanDefinition beanDefinition = new VincentBeanDefinition();
        beanDefinition.setFactoryBeanName(beanName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());

        //当成是一个classPath文件夹
        for(File file : classPath.listFiles()){
            if(file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else{
                if(!file.getName().endsWith(".class")){
                    continue;
                }
                //全类名 = 包名 + 类名
                String className = (scanPackage + "." + file.getName().replace(".class",""));
                regitryBeanClasses.add(className);
            }
        }
    }

    private void doLoadConfig(String configLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configLocation.replaceAll("classpath:",""));
        try {
            contextConfig.load(is);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
