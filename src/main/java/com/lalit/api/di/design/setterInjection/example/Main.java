package com.lalit.api.di.design.setterInjection.example;

import com.lalit.api.di.design.core.context.DIContextImpl;

public class Main {
	public static void main(String... s) {
		try {
			new DIContextImpl().getBean("aImplVersion2");
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException
				| IllegalAccessException | InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
