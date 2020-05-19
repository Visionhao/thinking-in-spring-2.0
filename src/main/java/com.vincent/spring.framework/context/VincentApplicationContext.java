package com.vincent.spring.framework.context;

import com.vincent.spring.framework.annotation.VincentAutowried;
import com.vincent.spring.framework.annotation.VincentController;
import com.vincent.spring.framework.annotation.VincentService;
import com.vincent.spring.framework.beans.VincentBeanWrapper;
import com.vincent.spring.framework.beans.config.VincentBeanDefinition;
import com.vincent.spring.framework.beans.support.VincentBeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  完成 bean 的 创建 和 DI注入
 * @author vincent
 */
public class VincentApplicationContext {

    private VincentBeanDefinitionReader reader;

    //beandefinition 缓存
    private Map<String,VincentBeanDefinition> beanDefinitionMap = new HashMap<String, VincentBeanDefinition>();

    // beanwrapper 缓存
    private Map<String,VincentBeanWrapper> factoryBeanInstanceCache = new HashMap<String, VincentBeanWrapper>();

    private Map<String,Object> factoryBeanObjectCache = new HashMap<String, Object>();

    public VincentApplicationContext(String... configLocations){
        try {
            //1、加载配置文件
            reader = new VincentBeanDefinitionReader(configLocations);

            //2、解析配置文件，封装成BeanDefinition
            List<VincentBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

            //3、把 BeanDefinition 缓存起来
            doRegistBeanDefinition(beanDefinitions);

            //4、完成自动依赖注入
            doAutowirted();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //只处理非延迟加载的情况
    private void doAutowirted() {
        //调用getBean()
        //这一步，所有的Bean并没有真正的实例化，还只是配置阶段
        for(Map.Entry<String,VincentBeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()){
            // 获取 bean 的名称
            String beanName = beanDefinitionEntry.getKey();
            getBean(beanName);
        }
    }

    //依赖注入，从这里开始，通过读取BeanDefinition中的信息
    //然后，通过反射机制创建一个实例并返回
    //Spring的做法是：不会把最原始的对象放出去，会用一个 BeanWrapper 来进行一次包装
    // 装饰器模式：
    //1、保留原来的OOP关系
    //2、我需要对它进行扩展，增强（为了以后AOP打基础）
    public Object getBean(String beanName) {
        //1、先拿到BeanDefinition 配置信息
        VincentBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);

        //2、反射实例化newInstance()
        Object instance = instanceBean(beanName,beanDefinition);

        //3、封装成一个叫做BeanWrapper
        VincentBeanWrapper beanWrapper = new VincentBeanWrapper(instance);

        //4、保存到IoC容器中
        factoryBeanInstanceCache.put(beanName,beanWrapper);

        //5、执行依赖注入
        populateBean(beanName,beanDefinition,beanWrapper);

        return beanWrapper.getWrapperInstance();
    }

    private void populateBean(String beanName, VincentBeanDefinition beanDefinition, VincentBeanWrapper beanWrapper) {
        /**
         * 可能涉及到循环依赖
         * A { B b}
         * B { A a}
         * 用 两个缓存，循环两次
         * 1、把第一次读取结果为空的BeanDefinition 存到第一个缓存
         * 2、等第一次循环之后，第二次循环再检查第一次的缓存，再进行赋值
         */
        Object instance = beanWrapper.getWrapperInstance();

        Class<?> clazz = beanWrapper.getWrapperClass();

        //在Spring中@Component
        if(!(clazz.isAnnotationPresent(VincentController.class) || clazz.isAnnotationPresent(VincentService.class))){
            return;
        }

        // 把 所有的包括private / protected / default / public 修饰字段都取出来
        for(Field field : clazz.getDeclaredFields()){
            if(!field.isAnnotationPresent(VincentAutowried.class)){
                continue;
            }
            VincentAutowried autowried = field.getAnnotation(VincentAutowried.class);
            // 如果用户没有自定义的beanName,就默认根据类型注入
            String autowriedBeanName = autowried.value().trim();
            if("".equals(autowriedBeanName)){
                autowriedBeanName = field.getType().getName();
            }

            //暴力访问
            field.setAccessible(true);

            try{
                if(this.factoryBeanInstanceCache.get(autowriedBeanName) == null){
                    continue;
                }
                //ioc.get(beanName) 相当于通过接口的全名拿到接口的实现的实例
                field.set(instance,this.factoryBeanInstanceCache.get(autowriedBeanName).getWrapperInstance());
            }catch (Exception e){
                e.printStackTrace();
                continue;
            }
        }

    }

    //创建真正的实例对象
    private Object instanceBean(String beanName, VincentBeanDefinition beanDefinition) {
        String className = beanDefinition.getBeanClassName();
        Object instance = null;
        try {
            Class<?> clazz = Class.forName(className);
            // 默认的类名首字母小写
            instance = clazz.newInstance();
            // 放到缓存map中
            this.factoryBeanObjectCache.put(beanName,instance);
        }catch (Exception e){
            e.printStackTrace();
        }
        return instance;

    }

    //bean注册
    private void doRegistBeanDefinition(List<VincentBeanDefinition> beanDefinitions) throws Exception {
        for(VincentBeanDefinition beanDefinition : beanDefinitions){
            if(this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                //throw new Exception("The " + beanDefinition.getFactoryBeanName() + "is exists!!!");
                System.out.println("The " + beanDefinition.getFactoryBeanName() + " is exists!!!");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(),beanDefinition);
        }
    }
    public Object getBean(Class<?> beanClass){
        return getBean(beanClass.getName());
    }

}
