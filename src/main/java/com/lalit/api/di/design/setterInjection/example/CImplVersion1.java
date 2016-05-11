package com.lalit.api.di.design.setterInjection.example;

public class CImplVersion1 implements ICImpl {

	@Override
	public String test() {
		return "Called CImplVersion2 with instance variable as ::";
	}

}
