package com.lalit.api.di.design.setterInjection.example;

import java.util.List;

public class AImplVersion2 implements AInterface {

	private int x;

	private List<String> listPrimitiveExam;

	public List<String> getListPrimitiveExam() {
		return listPrimitiveExam;
	}

	public void setListPrimitiveExam(List<String> listPrimitiveExam) {
		this.listPrimitiveExam = listPrimitiveExam;
	}

	public AImplVersion2(ICImpl cmpl) {

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
