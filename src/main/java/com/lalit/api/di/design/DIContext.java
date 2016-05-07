package com.lalit.api.di.design;

import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

public class DIContext {

	private static final Map<String, Class<?>> mapOfBeansClassObject = new LinkedHashMap<>();
	private static final Map<String, Object> mapOfBeansInstance = new LinkedHashMap<>();

	public static void main(String... s) throws Exception {
		DIContext context = new DIContext();
		System.out.println();
	}

	public Object getBean(String beanId) {
		return mapOfBeansClassObject.get(beanId);
	}

	public DIContext() throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {
		try {
			Document doc = new Builder().build(new File(
					"C:\\Users\\lalit goyal\\git\\LearningDependencyInjection\\src\\main\\java\\com\\lalit\\api\\di\\design\\setterInjection\\Config_file.xml"));

			Nodes beansNode = doc.query("//beans//bean");

			for (int parentClassIndex = 0; parentClassIndex < beansNode.size(); ++parentClassIndex) {
				Element element = (Element) beansNode.get(parentClassIndex);
				Class<?> parentClass = Class.forName(element.getFirstChildElement("name").getValue());
				mapOfBeansClassObject.put(element.getAttribute("id").getValue(), parentClass);
				mapOfBeansInstance.put(element.getAttribute("id").getValue(), parentClass.newInstance());
			}

			int index = 0;
			for (Map.Entry<String, Class<?>> entry : mapOfBeansClassObject.entrySet()) {
				Elements setterInjections = getSetterInjectionElements(beansNode, index);
				Object parentClassInstance = mapOfBeansInstance.get(entry.getKey());
				if (setterInjections != null) {
					for (int dependencyIndex = 0; dependencyIndex < setterInjections.size(); ++dependencyIndex) {
						Element elementInjection = ((Element) setterInjections.get(dependencyIndex));

						String valueRefAttValue = getAttributeValue(elementInjection, "value-ref");
						String idAttValue = getAttributeValue(elementInjection, "id");
						String dataTypeAttValue = getAttributeValue(elementInjection, "dataType");
						String valueAttValue = getAttributeValue(elementInjection, "value");

						setFieldValue(entry, parentClassInstance, valueRefAttValue, idAttValue, dataTypeAttValue,
								valueAttValue);
					}
				}
				++index;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setFieldValue(Map.Entry<String, Class<?>> entry, Object parentClassInstance, String valueRefAttValue,
			String idAttValue, String dataTypeAttValue, String valueAttValue)
					throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		if (valueRefAttValue != null && mapOfBeansClassObject.containsKey(valueRefAttValue))
			injectReferenceTypeFieldDependency(mapOfBeansInstance.get(valueRefAttValue), entry.getValue(),
					parentClassInstance, idAttValue);
		else
			injectPrimitiveFieldDependency(valueAttValue, dataTypeAttValue, entry.getValue(), parentClassInstance,
					idAttValue);
	}

	private Elements getSetterInjectionElements(Nodes beansNode, int index) {
		Element element = (Element) beansNode.get(index);
		Elements setterInjections = null;
		if (element.getFirstChildElement("SetterInjection") != null
				&& element.getFirstChildElement("SetterInjection").getChildElements("property") != null) {
			setterInjections = element.getFirstChildElement("SetterInjection").getChildElements("property");
		}
		return setterInjections;
	}

	private String getAttributeValue(Element element, String attributeName) {
		return element.getAttribute(attributeName) == null ? null : element.getAttribute(attributeName).getValue();
	}

	private void injectReferenceTypeFieldDependency(Object dependencyClassInstance, Class<?> parentClass,
			Object parentClassInstance, String fieldName)
					throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		Field field = parentClass.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(parentClassInstance, dependencyClassInstance);
	}

	private void injectPrimitiveFieldDependency(String value, String dataType, Class<?> parentClass,
			Object parentClassInstance, String fieldName)
					throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		Field field = parentClass.getDeclaredField(fieldName);
		field.setAccessible(true);
		switch (dataType) {
		case "int":
			field.set(parentClassInstance, Integer.valueOf(value));
			break;
		case "double":
			field.set(parentClassInstance, Double.valueOf(value));
			break;
		case "long":
			field.set(parentClassInstance, Long.valueOf(value));
			break;
		default:
			field.set(parentClassInstance, value);
			break;
		}
	}
}