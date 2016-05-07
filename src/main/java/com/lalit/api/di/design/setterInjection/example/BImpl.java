package com.lalit.api.di.design.setterInjection.example;

public class BImpl {

	private AInterface aClass;

	private AInterface aClass1;

	public void setaClass1(AInterface aClass1) {
		this.aClass1 = aClass1;
	}

	public void setaClass(AInterface aClass) {
		this.aClass = aClass;
	}

	public void generatePolicy() {
		System.out.println(aClass.createPolicy());
		System.out.println(aClass1.createPolicy());
	}

}
