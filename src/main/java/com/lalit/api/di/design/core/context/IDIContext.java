package com.lalit.api.di.design.core.context;

/**
 * @author lalit goyal
 * 
 *         TODO Apply design approach
 * 
 */
public interface IDIContext {

	public Object getBean(String beanId);

	public <T> T getBean(String beanId, Class<T> classType);

	public <T> T getBean(Class<T> classType);
}