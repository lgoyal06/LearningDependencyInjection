package com.lalit.api.di.design.setterInjection.example;

public class BImpl {

	public BImpl(AInterface aClass, ICImpl cImpl, int x, boolean b) {
		this.aClass = aClass;
		this.cImpl = cImpl;
		this.x = x;
		this.b = b;
	}

	private AInterface aClass;

	private int x;

	private boolean b;

	private AInterface aClass1;

	private ICImpl cImpl;

	public void setaClass1(AInterface aClass1) {
		this.aClass1 = aClass1;
	}

	public void setaClass(AInterface aClass) {
		this.aClass = aClass;
	}

	public void generatePolicy() {
		System.out.println(aClass.createPolicy());
		System.out.println(cImpl.test());
		System.out.println(x);
	}

}
