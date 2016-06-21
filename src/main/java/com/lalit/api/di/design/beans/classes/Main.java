package com.lalit.api.di.design.beans.classes;

import java.util.List;

import com.lalit.api.di.design.core.context.DIContextImpl;

public class Main {
	public static void main(String... s) {
		try {
			AImplVersion2 aImplVersion2 = new DIContextImpl(
					"C:\\Users\\lalit goyal\\git\\LearningDependencyInjection\\src\\main\\java\\com\\lalit\\api\\di\\design\\config\\sample\\xml\\Sample1.xml")
							.getBean(AImplVersion2.class);
			System.out.println(aImplVersion2.getX());

			List<Integer> list = aImplVersion2.getListPrimitiveExam();
			int totalSum = 0;
			for (int i = 0; i < list.size(); ++i) {
				totalSum = totalSum + list.get(i);
			}
			System.out.println(totalSum);

			List<AImplVersion1> listRef = aImplVersion2.getListRefExam();
			for (int i = 0; i < listRef.size(); ++i) {
				System.out.println(listRef.get(i).getcClass().hashCode());
			}

			List<List<Double>> list2 = aImplVersion2.getListOflistPrimitiveExam();
			double totalSumDouble = 0;
			for (int i = 0; i < list2.size(); ++i) {
				for (int j = 0; j < list2.get(i).size(); ++j) {
					totalSumDouble = totalSumDouble + list2.get(i).get(j);
				}
			}
			System.out.println(totalSumDouble);

		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException
				| IllegalAccessException | InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
