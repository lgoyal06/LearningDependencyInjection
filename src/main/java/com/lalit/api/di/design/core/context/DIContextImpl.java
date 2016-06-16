package com.lalit.api.di.design.core.context;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
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
/**
*
* TODO Add another function that returns Bean object based upon the Class object passed to method
* by 20th June
**/
	public Object getBean(String beanId) {
		return mapOfBeansInstance.get(beanId);
	}
/**
 * TODO Add code to dynamically pick xml file 
 * Code to pass the File path in Constructor by 20th June
 * */
	public DIContextImpl() throws ClassNotFoundException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException, InstantiationException {
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
				return populateConstructorInjectionOfBeanInMap(mapOfBeansInstance, doc, ((Element) beansNode.get(0)));
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

	private void populateFieldDependenciesOfBeans(Nodes beansNode, int parentClassIndex)
			throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		Elements setterInjections = getSetterInjectionElements((Element) beansNode.get(parentClassIndex));
		Object parentClassInstance = mapOfBeansInstance
				.get(((Element) beansNode.get(parentClassIndex)).getAttributeValue("id"));
		if (setterInjections != null) {
			for (int dependencyIndex = 0; dependencyIndex < setterInjections.size(); ++dependencyIndex) {
				Element elementInjected = ((Element) setterInjections.get(dependencyIndex));
				String valueRefAttValue = getAttrValue(elementInjected, "value-ref");
				String idAttValue = getAttrValue(elementInjected, "id");
				String dataTypeAttValue = getAttrValue(elementInjected, "dataType");
				String valueAttValue = getAttrValue(elementInjected, "value");

				setFieldValue(parentClassInstance, valueRefAttValue, idAttValue, dataTypeAttValue, valueAttValue,
						elementInjected);
			}
		}
	}

	private String getAttrValue(Element element, String attributeName) {
		return element.getAttribute(attributeName) == null ? null : element.getAttribute(attributeName).getValue();
	}

	private void setFieldValue(Object parentClassInstance, String valueRefAttValue, String idAttValue,
			String dataTypeAttValue, String valueAttValue, Element elementInjected)
					throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		// User Defined class fields i.e. other bean class
		if (valueRefAttValue != null)
			injectReferenceTypeFieldDependency(mapOfBeansInstance.get(valueRefAttValue), parentClassInstance,
					idAttValue);
		// TODO Collection Data type fields
		else if (dataTypeAttValue.equalsIgnoreCase("List"))
			try {
				injectCollectionTypeFieldDependency(valueAttValue, dataTypeAttValue, parentClassInstance, idAttValue,
						elementInjected);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
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

	private void injectReferenceTypeFieldDependency(Object dependencyClassInstance, Object parentClassInstance,
			String fieldName) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		Field field = parentClassInstance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(parentClassInstance, dependencyClassInstance);
	}

	private void injectPrimitiveFieldDependency(String value, String dataType, Object parentClassInstance,
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

	/** TODO :Collection can be List, Map and Set
	* First we are targeting List then Map ............then Set
	*
	* 
	**/
	private void injectCollectionTypeFieldDependency(String value, String dataType, Object parentClassInstance,
			String fieldName, Element elementInjected) throws NoSuchFieldException, IllegalAccessException,
					InstantiationException, ClassNotFoundException {
		String collectionClassName = elementInjected.getChildElements("list").get(0).getAttribute("type").getValue();

		Object collectionObjInstance = Class.forName(collectionClassName).newInstance();
		if (collectionObjInstance instanceof List) {
			for (int index = 0; index < elementInjected.getChildElements("list").get(0).getChildElements("element")
					.size(); ++index) {

				// In case the Element contains primitive values
				// Refactor code to set the generic type of list elements
				((List) collectionObjInstance).add(elementInjected.getChildElements("list").get(0)
						.getChildElements("element").get(index).getValue());

				// In case Element contains Another beans
				// In case Element further contains collection
			}
		}
		Field field = parentClassInstance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		fetchGenericTypeOfField(field);
		field.set(parentClassInstance, collectionObjInstance);
	}

	/** TODO: Generic type can be
	// > primitive TODO by 16th June
	// > other bean object TODO by 18th June
	// > collection object TODO by 20 th June
	**/
	private String fetchGenericTypeOfField(Field field) {

		return null;

	}
}