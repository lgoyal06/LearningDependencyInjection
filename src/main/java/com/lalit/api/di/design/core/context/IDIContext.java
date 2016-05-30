package com.lalit.api.di.design.core.context;

/**
 * @author lalit goyal
 * 
 *         TODO Apply design approah
 * 
 */
public interface IDIContext {

	public Object getBean(String beanId);

	// Return the generic type i.e. Specific Class Object
	// public T getBean(String beanId, Class<T> clazz);
}