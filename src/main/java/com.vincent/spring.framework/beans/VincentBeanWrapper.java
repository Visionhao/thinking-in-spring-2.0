package com.vincent.spring.framework.beans;

/**
 *  bean的包装
 * @author vincent
 */
public class VincentBeanWrapper {

    private Object wrapperInstance;

    private Class<?> wrapperClass;

    public VincentBeanWrapper(Object instance){
        this.wrapperInstance = instance;
        this.wrapperClass = instance.getClass();
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public Class<?> getWrapperClass() {
        return wrapperClass;
    }
}
