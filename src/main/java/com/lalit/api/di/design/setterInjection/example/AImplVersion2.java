package com.lalit.api.di.design.setterInjection.example;

public class AImplVersion2 implements AInterface {

	private int x;

	public AImplVersion2(ICImpl cmpl){
		
	}
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public String createPolicy() {
		// TODO Auto-generated method stub
		return "Called AImplVersion2 with instance variable as ::" + x;
	}

}
