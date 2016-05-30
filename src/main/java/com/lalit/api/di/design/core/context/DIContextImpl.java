package com.lalit.api.di.design.core.context;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

/**
 * @author lalit goyal
 * 
 *         TODO Apply the OOPS Design Concepts and make it API like
 * 
 *         use Design Pattern
 * 
 *         SOLID Principal
 *
 */
public class DIContextImpl implements IDIContext {

	private static final Map<String, Object> mapOfBeansInstance = new LinkedHashMap<>();

	public Object getBean(String beanId) {
		return mapOfBeansInstance.get(beanId);
	}

	public DIContextImpl() throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {
		try {
			Document doc = new Builder().build(new File(
					"C:\\Users\\lalit goyal\\git\\LearningDependencyInjection\\src\\main\\java\\com\\lalit\\api\\di\\design\\constructorInjection\\Config_file_constructor_injection.xml"));

			Nodes beansNode = doc.query("//beans//bean");
			for (int parentClassIndex = 0; parentClassIndex < beansNode.size(); ++parentClassIndex) {
				populateConstructorInjectionOfBeanInMap(mapOfBeansInstance, doc,
						(Element) beansNode.get(parentClassIndex));
			}

			for (int parentClassIndex = 0; parentClassIndex < beansNode.size(); ++parentClassIndex) {
				populateFieldDependenciesOfBeans(beansNode, parentClassIndex);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void populateFieldDependenciesOfBeans(Nodes beansNode, int parentClassIndex)
			throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		Elements setterInjections = getSetterInjectionElements((Element) beansNode.get(parentClassIndex));
		Object parentClassInstance = mapOfBeansInstance
				.get(((Element) beansNode.get(parentClassIndex)).getAttributeValue("id"));
		if (setterInjections != null) {
			for (int dependencyIndex = 0; dependencyIndex < setterInjections.size(); ++dependencyIndex) {
				Element elementInjection = ((Element) setterInjections.get(dependencyIndex));

				String valueRefAttValue = getAttributeValue(elementInjection, "value-ref");
				String idAttValue = getAttributeValue(elementInjection, "id");
				String dataTypeAttValue = getAttributeValue(elementInjection, "dataType");
				String valueAttValue = getAttributeValue(elementInjection, "value");

				setFieldValue(parentClassInstance, valueRefAttValue, idAttValue, dataTypeAttValue, valueAttValue);
			}
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
	private Object populateConstructorInjectionOfBeanInMap(Map<String, Object> mapOfBeansInstance, Document doc,
			Element element) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
					NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {

		if ("bean".equalsIgnoreCase(element.getLocalName())) {
			if (mapOfBeansInstance.containsKey(element.getAttributeValue("id"))) {
				return mapOfBeansInstance.get(element.getAttributeValue("id"));
			}
			Elements constructorInjection = getConstructorInjectionElements(element);
			if (constructorInjection != null) {
				Object[] argumentValue = new Object[constructorInjection.size()];
				Class<?>[] argumentType = new Class[constructorInjection.size()];
				for (int i = 0; i < constructorInjection.size(); ++i) {
					argumentValue[i] = populateConstructorInjectionOfBeanInMap(mapOfBeansInstance, doc,
							constructorInjection.get(i));
					argumentType[i] = getArgumentType(constructorInjection.get(i));
				}
				Class<?> parentClass = Class.forName(element.getFirstChildElement("name").getValue());
				Object obj = parentClass.getDeclaredConstructor(argumentType).newInstance(argumentValue);
				mapOfBeansInstance.put(element.getAttributeValue("id"), obj);
				return obj;
			} else {
				Class<?> classObj = Class.forName(element.getFirstChildElement("name").getValue());
				Object obj = classObj.newInstance();
				mapOfBeansInstance.put(element.getAttributeValue("id"), obj);
				return obj;
			}
		} else {
			if ((element).getAttributeValue("value-ref") != null) {
				Nodes beansNode = doc
						.query("//beans//bean[attribute::id='" + (element).getAttributeValue("value-ref") + "']");
				return populateConstructorInjectionOfBeanInMap(mapOfBeansInstance, doc, ((Element) beansNode.get(0)));
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

	private void setFieldValue(Object parentClassInstance, String valueRefAttValue, String idAttValue,
			String dataTypeAttValue, String valueAttValue)
					throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		if (valueRefAttValue != null)
			injectReferenceTypeFieldDependency(mapOfBeansInstance.get(valueRefAttValue), parentClassInstance,
					idAttValue);
		else
			injectPrimitiveFieldDependency(valueAttValue, dataTypeAttValue, parentClassInstance, idAttValue);
	}

	private Elements getSetterInjectionElements(Element element) {
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

	private void injectReferenceTypeFieldDependency(Object dependencyClassInstance, Object parentClassInstance,
			String fieldName) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		Field field = parentClassInstance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(parentClassInstance, dependencyClassInstance);
	}

	// TODO P2 Complete the Setter Injection primitive data type conversion
	// List,Set and Map conversion as well
	private void injectPrimitiveFieldDependency(String value, String dataType, Object parentClassInstance,
			String fieldName) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		Field field = parentClassInstance.getClass().getDeclaredField(fieldName);
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