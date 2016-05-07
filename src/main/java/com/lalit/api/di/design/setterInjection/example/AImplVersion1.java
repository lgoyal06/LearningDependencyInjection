package com.lalit.api.di.design.setterInjection.example;

public class AImplVersion1 implements AInterface {

	private CImplVersion1 cClass;

	@Override
	public String createPolicy() {
		// TODO Auto-generated method stub
		return "Called AImplVersion1 with field ::>" + cClass;
	}

}
