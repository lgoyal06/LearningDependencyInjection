package com.lalit.api.di.design.core.context;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

	@Override
	public Object getBean(String beanId) {
		return mapOfBeansInstance.get(beanId);
	}

	@Override
	public <T> T getBean(String beanId, Class<T> classObj) {
		return classObj.cast(mapOfBeansInstance.get(beanId));
	}

	@Override
	public <T> T getBean(Class<T> classObj) {
		Iterator<Entry<String, Object>> iterator = mapOfBeansInstance.entrySet().iterator();

		while (iterator.hasNext()) {
			Object beanInstance = iterator.next().getValue();
			if (classObj.getTypeName().equals(beanInstance.getClass().getTypeName())) {
				return classObj.cast(beanInstance);
			}
		}
		return null;
	}

	/**
	 * TODO Add code to load xml file via context path like in spring ioc
	 */
	public DIContextImpl(String xmlFilePath) throws ClassNotFoundException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException, InstantiationException {
		try {

			Document doc = new Builder().build(new File(xmlFilePath));

			Nodes beansNode = doc.query("//beans//bean");
			for (int index = 0; index < beansNode.size(); ++index) {
				populateConstructorInjectionOfBean(mapOfBeansInstance, doc, (Element) beansNode.get(index));
			}

			for (int index = 0; index < beansNode.size(); ++index) {
				populateSetterDependenciesOfBeans((Element) beansNode.get(index));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This will be recursive function that will keep on calling itself until it
	 * gets all constructor dependency for a bean . Tree Traverse will be Post
	 * -order (Left-Right-Root)
	 * 
	 * As of now Constructor can have primitive or other bean as dependency
	 * Please use setter injection for Collection objects
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
	private Object populateConstructorInjectionOfBean(Map<String, Object> mapOfBeansInstance, Document doc,
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
					argumentValue[i] = populateConstructorInjectionOfBean(mapOfBeansInstance, doc,
							constructorInjection.get(i));
					argumentType[i] = getArgumentType(constructorInjection.get(i));
				}
				Class<?> parentClass = Class.forName(element.getFirstChildElement("name").getValue().trim());
				Object obj = parentClass.getDeclaredConstructor(argumentType).newInstance(argumentValue);
				mapOfBeansInstance.put(element.getAttributeValue("id"), obj);
				return obj;
			} else {
				Class<?> classObj = Class.forName(element.getFirstChildElement("name").getValue().trim());
				Object obj = classObj.newInstance();
				mapOfBeansInstance.put(element.getAttributeValue("id"), obj);
				return obj;
			}
		} else {
			if ((element).getAttributeValue("value-ref") != null) {
				Nodes beansNode = doc
						.query("//beans//bean[attribute::id='" + (element).getAttributeValue("value-ref") + "']");
				return populateConstructorInjectionOfBean(mapOfBeansInstance, doc, ((Element) beansNode.get(0)));
			} else {
				return convertFromStringToPrimitiveWrapperObjectType((element).getAttributeValue("type"),
						(element).getAttributeValue("value"));
			}
		}
	}

	private Elements getConstructorInjectionElements(Element element) {
		Elements constructorInjections = null;
		if (element.getFirstChildElement("ConstructorInjection") != null
				&& element.getFirstChildElement("ConstructorInjection").getChildElements("arg") != null) {
			constructorInjections = element.getFirstChildElement("ConstructorInjection").getChildElements("arg");
		}
		return constructorInjections;
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

	private void populateSetterDependenciesOfBeans(Element currentBeanElement)
			throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		Elements setterInjections = getSetterInjectionElements(currentBeanElement);
		Object beanClassInstance = mapOfBeansInstance.get(currentBeanElement.getAttributeValue("id"));
		if (setterInjections != null) {
			for (int dependencyIndex = 0; dependencyIndex < setterInjections.size(); ++dependencyIndex) {
				Element elementInjected = ((Element) setterInjections.get(dependencyIndex));
				String valueRefAttValue = getAttrValue(elementInjected, "value-ref");
				String idAttValue = getAttrValue(elementInjected, "id");
				String dataTypeAttValue = getAttrValue(elementInjected, "dataType");
				String valueAttValue = getAttrValue(elementInjected, "value");

				setFieldValue(beanClassInstance, valueRefAttValue, idAttValue, dataTypeAttValue, valueAttValue,
						elementInjected);
			}
		}
	}

	private String getAttrValue(Element element, String attributeName) {
		return element.getAttribute(attributeName) == null ? null : element.getAttribute(attributeName).getValue();
	}

	/**
	 * TODO : Change the if else condition to start using common
	 * isElementTypeCollection and isElementTypePrimitiveOrPrimitiveWrapper
	 * function to check for element type
	 **/
	private void setFieldValue(Object parentClassInstance, String valueRefAttValue, String idAttValue,
			String dataTypeAttValue, String valueAttValue, Element elementInjected)
					throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		if (valueRefAttValue != null)
			injectReferenceTypeSetterDependency(mapOfBeansInstance.get(valueRefAttValue), parentClassInstance,
					idAttValue);
		else if (dataTypeAttValue.equalsIgnoreCase("List"))
			try {
				injectCollectionTypeSetterDependency(parentClassInstance, idAttValue, elementInjected);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		else
			injectPrimitiveSetterDependency(valueAttValue, dataTypeAttValue, parentClassInstance, idAttValue);
	}

	private Elements getSetterInjectionElements(Element element) {
		Elements setterInjections = null;
		if (element.getFirstChildElement("SetterInjection") != null
				&& element.getFirstChildElement("SetterInjection").getChildElements("property") != null) {
			setterInjections = element.getFirstChildElement("SetterInjection").getChildElements("property");
		}
		return setterInjections;
	}

	private void injectReferenceTypeSetterDependency(Object dependencyClassInstance, Object parentClassInstance,
			String fieldName) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		Field field = parentClassInstance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(parentClassInstance, dependencyClassInstance);
	}

	private void injectPrimitiveSetterDependency(String value, String dataType, Object parentClassInstance,
			String fieldName) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		Field field = parentClassInstance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		switch (dataType) {
		case "int":
			field.setInt(parentClassInstance, Integer.valueOf(value));
			break;
		case "double":
			field.setDouble(parentClassInstance, Double.valueOf(value));
			break;
		case "long":
			field.setLong(parentClassInstance, Long.valueOf(value));
			break;
		case "boolean":
			field.setBoolean(parentClassInstance, Boolean.valueOf(value));
			break;
		case "byte":
			field.setByte(parentClassInstance, Byte.valueOf(value));
			break;
		case "short":
			field.setShort(parentClassInstance, Short.valueOf(value));
			break;
		case "float":
			field.setFloat(parentClassInstance, Float.valueOf(value));
			break;
		case "char":
			field.setChar(parentClassInstance, value.toCharArray()[0]);
			break;
		default:
			field.set(parentClassInstance, value);
			break;
		}
	}

	/**
	 * TODO :Collection can be List, Map and Set First we are targeting List
	 * then Map ............then Set
	 *
	 * 
	 **/
	private void injectCollectionTypeSetterDependency(Object parentClassInstance, String fieldName,
			Element elementInjected) throws NoSuchFieldException, IllegalAccessException, InstantiationException,
					ClassNotFoundException {
		String collectionClassName = elementInjected.getChildElements("list").get(0).getAttribute("type").getValue();
		Object collectionDependencyObj = Class.forName(collectionClassName).newInstance();

		if (collectionDependencyObj instanceof List) {
			collectionDependencyObj = getCollectionTypeSetterDependencyObj(elementInjected, collectionDependencyObj);
		}
		// TODO For Map 22nd June
		else if (collectionDependencyObj instanceof Map) {
			// collectionDependencyObj =
			// getCollectionTypeFieldDependencyObj(elementInjected,
			// collectionDependencyObj);
		}

		// TODO For Set
		else if (collectionDependencyObj instanceof Set) {
			// collectionDependencyObj =
			// getCollectionTypeFieldDependencyObj(elementInjected,
			// collectionDependencyObj);
		}
		Field field = parentClassInstance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(parentClassInstance, collectionDependencyObj);
	}

	/**
	 * TODO: Refactor method
	 * 
	 * Remove Redundant code
	 * 
	 * 
	 * Make it more generic to handle All type of Collection not just List Type
	 * 
	 **/
	@SuppressWarnings("unchecked")
	private Object getCollectionTypeSetterDependencyObj(Element elementInjected, Object collectionObjInstance)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (isElementTypePrimitiveOrPrimitiveWrapper(
				elementInjected.getChildElements("list").get(0).getAttributeValue("elementType"))) {
			for (int index = 0; index < elementInjected.getChildElements("list").get(0).getChildElements("element")
					.size(); ++index) {

				((List<Object>) collectionObjInstance).add(convertFromStringToPrimitiveWrapperType(
						elementInjected.getChildElements("list").get(0).getChildElements("element").get(index)
								.getValue(),
						elementInjected.getChildElements("list").get(0).getAttributeValue("elementType")));
			}

			return collectionObjInstance;
		} else if (isElementTypeCollection(
				elementInjected.getChildElements("list").get(0).getAttributeValue("elementType"))) {
			for (int index = 0; index < elementInjected.getChildElements("list").get(0).getChildElements("element")
					.size(); ++index) {
				Element element = elementInjected.getChildElements("list").get(0).getChildElements("element")
						.get(index);
				((List<Object>) collectionObjInstance).add(getCollectionTypeSetterDependencyObj(element, Class
						.forName(element.getChildElements("list").get(0).getAttributeValue("type")).newInstance()));

			}
			return collectionObjInstance;
		} else {
			for (int index = 0; index < elementInjected.getChildElements("list").get(0).getChildElements("element")
					.size(); ++index) {
				((List<Object>) collectionObjInstance).add(mapOfBeansInstance.get(elementInjected
						.getChildElements("list").get(0).getChildElements("element").get(index).getValue()));
			}
			return collectionObjInstance;
		}
	}

	/**
	 * TODO: See whether enum or constant class is good option to have all
	 * primitive or primitive wrapper
	 * 
	 **/
	private boolean isElementTypePrimitiveOrPrimitiveWrapper(String type) {
		List<String> list = new ArrayList<>();
		list.add("Double");
		list.add("Integer");
		return list.contains(type);
	}

	/**
	 * TODO: See whether enum or constant class is good option to have all
	 * primitive or primitive wrapper
	 * 
	 **/

	private boolean isElementTypeCollection(String type) {
		List<String> list = new ArrayList<>();
		list.add("list");
		list.add("map");
		list.add("set");
		return list.contains(type);
	}

	private Object convertFromStringToPrimitiveWrapperType(String value, String outType) {
		switch (outType) {
		case "Integer":
			return Integer.valueOf(value);
		case "Boolean":
			return Boolean.valueOf(value);
		case "Long":
			return Long.valueOf(value);
		case "Double":
			return Double.valueOf(value);
		case "Byte":
			return Byte.valueOf(value);
		case "Short":
			return Short.valueOf(value);
		case "Float":
			return Float.valueOf(value);
		case "Char":
			return value.toCharArray()[0];
		default:
			return value;
		}
	}

}
