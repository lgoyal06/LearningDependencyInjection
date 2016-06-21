package com.lalit.api.di.design.beans.classes;

import java.util.Stack;

public class BImpl {

	public BImpl(AInterface aClass, ICImpl cImpl, int x, boolean b, Stack stack) {
		this.aClass = aClass;
		this.cImpl = cImpl;
		this.x = x;
		this.b = b;
		this.stack = stack;
	}

	private AInterface aClass;

	private Stack stack;

	private int x;

	private boolean b;

	private AInterface aClass1;

	private ICImpl cImpl;

	public Stack getStack() {
		return stack;
	}

	public void setStack(Stack stack) {
		this.stack = stack;
	}

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
