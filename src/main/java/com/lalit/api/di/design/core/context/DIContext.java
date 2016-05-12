package com.lalit.api.di.design.core.context;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.lalit.api.di.design.setterInjection.example.BImpl;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

public class DIContext {

	private static final Map<String, Class<?>> mapOfBeansClassObject = new LinkedHashMap<>();
	private static final Map<String, Object> mapOfBeansInstance = new LinkedHashMap<>();

	public Object getBean(String beanId) {
		return mapOfBeansInstance.get(beanId);
	}

	public static void main(String... s) {
		try {
			((BImpl) new DIContext().getBean("bImpl")).generatePolicy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Using for Setter Injection **/
	public DIContext(String configFilePath) throws ClassNotFoundException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException, InstantiationException {
		try {
			Document doc = new Builder().build(new File(configFilePath));

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

	/**
	 * 
	 * Used for Constructor Dependency injection
	 * 
	 **/
	public DIContext() throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {
		try {
			Document doc = new Builder().build(new File(
					"C:\\Users\\lalit goyal\\git\\LearningDependencyInjection\\src\\main\\java\\com\\lalit\\api\\di\\design\\constructorInjection\\Config_file_constructor_injection.xml"));

			Nodes beansNode = doc.query("//beans//bean");
			for (int parentClassIndex = 0; parentClassIndex < beansNode.size(); ++parentClassIndex) {
				mapOfBeansInstance.put(((Element) beansNode.get(parentClassIndex)).getAttributeValue("id"),
						getArgumentValue(doc, (Element) beansNode.get(parentClassIndex)));
			}

			// TODO P1 Merge Constructor Injection wit Setter Injection
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This will be recursive function that will keep on calling itself until it
	 * gets all constructor dependency for a bean We will need to LIFO data
	 * structure. Tree Traverse will be Post -order (Left-Right-Root)
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * 
	 **/
	private Object getArgumentValue(Document doc, Element element)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException,
			SecurityException, IllegalArgumentException, InvocationTargetException {

		if ("bean".equalsIgnoreCase(element.getLocalName())) {
			Elements constructorInjection = getConstructorInjectionElements(element);
			if (constructorInjection != null) {
				Object[] argumentValue = new Object[constructorInjection.size()];
				Class<?>[] argumentType = new Class[constructorInjection.size()];
				for (int i = 0; i < constructorInjection.size(); ++i) {
					argumentValue[i] = getArgumentValue(doc, constructorInjection.get(i));
					argumentType[i] = getArgumentType(constructorInjection.get(i));
				}
				Class<?> parentClass = Class.forName(element.getFirstChildElement("name").getValue());
				return parentClass.getDeclaredConstructor(argumentType).newInstance(argumentValue);
			} else {
				Class<?> classObj = Class.forName(element.getFirstChildElement("name").getValue());
				return classObj.newInstance();
			}
		} else {
			if ((element).getAttributeValue("value-ref") != null) {
				Nodes beansNode = doc
						.query("//beans//bean[attribute::id='" + (element).getAttributeValue("value-ref") + "']");
				return getArgumentValue(doc, ((Element) beansNode.get(0)));
			} else {
				return convertFromStringToPrimitiveWrapperObjectType((element).getAttributeValue("type"),
						(element).getAttributeValue("value"));
			}
		}
	}

	private Class<?> getArgumentType(Element constructorInjection) throws ClassNotFoundException {
		switch (constructorInjection.getAttributeValue("type")) {
		case "int":
			return int.class;
		case "boolean":
			return boolean.class;
		case "long":
			return long.class;
		case "double":
			return double.class;
		case "byte":
			return byte.class;
		case "short":
			return short.class;
		case "float":
			return float.class;
		case "char":
			return char.class;
		default:
			return Class.forName(constructorInjection.getAttributeValue("type"));
		}
	}

	private Object convertFromStringToPrimitiveWrapperObjectType(String type, String value) {
		switch (type) {
		case "int":
			return Integer.valueOf(value);
		case "boolean":
			return Boolean.valueOf(value);
		case "long":
			return Long.valueOf(value);
		case "double":
			return Double.valueOf(value);
		case "byte":
			return Byte.valueOf(value);
		case "short":
			return Short.valueOf(value);
		case "float":
			return Float.valueOf(value);
		case "char":
			return value.toCharArray()[0];
		default:
			return value;
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

	private Elements getConstructorInjectionElements(Element element) {
		Elements constructorInjections = null;
		if (element.getFirstChildElement("ConstructorInjection") != null
				&& element.getFirstChildElement("ConstructorInjection").getChildElements("arg") != null) {
			constructorInjections = element.getFirstChildElement("ConstructorInjection").getChildElements("arg");
		}
		return constructorInjections;
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

	// TODO P2 Complete the Setter Injection primitive data type conversion
	// List,Set and Map conversion as well
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
		case "boolean":
			field.set(parentClassInstance, Boolean.valueOf(value));
			break;
		case "byte":
			field.set(parentClassInstance, Byte.valueOf(value));
			break;
		case "short":
			field.set(parentClassInstance, Short.valueOf(value));
			break;
		case "float":
			field.set(parentClassInstance, Float.valueOf(value));
			break;
		case "char":
			field.set(parentClassInstance, value.toCharArray()[0]);
			break;
		default:
			field.set(parentClassInstance, value);
			break;
		}
	}
}