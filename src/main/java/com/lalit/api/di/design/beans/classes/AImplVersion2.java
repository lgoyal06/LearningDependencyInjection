package com.lalit.api.di.design.beans.classes;

import java.util.List;

public class AImplVersion2 implements AInterface {

	private int x;

	private List<Integer> listPrimitiveExam;

	private List<AImplVersion1> listRefExam;

	private List<List<Double>> listOflistPrimitiveExam;;

	private List<List<AImplVersion1>> listOflistRefExam;

	public AImplVersion2(ICImpl cmpl) {

	}

	public List<List<AImplVersion1>> getListOflistRefExam() {
		return listOflistRefExam;
	}

	public void setListOflistRefExam(List<List<AImplVersion1>> listOflistRefExam) {
		this.listOflistRefExam = listOflistRefExam;
	}

	public List<List<Double>> getListOflistPrimitiveExam() {
		return listOflistPrimitiveExam;
	}

	public void setListOflistPrimitiveExam(List<List<Double>> listOflistPrimitiveExam) {
		this.listOflistPrimitiveExam = listOflistPrimitiveExam;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return x;
	}

	public List<AImplVersion1> getListRefExam() {
		return listRefExam;
	}

	public void setListRefExam(List<AImplVersion1> listRefExam) {
		this.listRefExam = listRefExam;
	}

	public List<Integer> getListPrimitiveExam() {
		return listPrimitiveExam;
	}

	public void setListPrimitiveExam(List<Integer> listPrimitiveExam) {
		this.listPrimitiveExam = listPrimitiveExam;
	}

	@Override
	public String createPolicy() {
		// TODO Auto-generated method stub
		return "Called AImplVersion2 with instance variable as ::" + x;
	}

}
