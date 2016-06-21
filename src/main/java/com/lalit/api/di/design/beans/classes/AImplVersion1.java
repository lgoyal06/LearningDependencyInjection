package com.lalit.api.di.design.beans.classes;

public class AImplVersion1 implements AInterface {

	private CImplVersion1 cClass;

	public CImplVersion1 getcClass() {
		return cClass;
	}

	public void setcClass(CImplVersion1 cClass) {
		this.cClass = cClass;
	}

	@Override
	public String createPolicy() {
		// TODO Auto-generated method stub
		return "Called AImplVersion1 with field ::>" + cClass;
	}

}
