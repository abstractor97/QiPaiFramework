package com.yaowan.framework.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 对象和字节工具类
 * 
 * @author Thomas Zheng
 * 
 */
public class ObjectUtil {
	private static final Log log = LogFactory.getLog(ObjectUtil.class);

	private ObjectUtil() {

	}

	/**
	 * 对象转换成字节数组
	 * 
	 * @param obj
	 *            对象
	 * @return byte[]
	 */
	public static byte[] object2ByteArray(Object obj) {
		if (obj == null) {
			return null;
		}

		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			(new ObjectOutputStream(bos)).writeObject(obj);

			return bos.toByteArray();
		} catch (IOException ex) {
			log.error("对象转换成字节数组出错", ex);

			return null;
		}

	}

	/**
	 * 字节数组转换成对象
	 * 
	 * @param buffer
	 * @return Object
	 */
	public static Object byteArray2Object(byte[] buffer) {
		if (buffer == null || buffer.length == 0) {
			return null;
		}

		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (Exception ex) {
			log.error("字节数组转换成对象出错", ex);
			return null;
		}
	}

	/**
	 * 根据源对象创建实例
	 * 
	 * @param <T>
	 * @param <V>
	 * @param type
	 * @param value
	 * @return
	 */
	public static <T, V> T createObject(Class<T> type, V value) {
		T typeInstance;
		try {
			typeInstance = type.newInstance();
			BeanUtil.copyNotNullProperties(typeInstance, value);
		} catch (Exception e) {
			typeInstance = null;
		}
		return typeInstance;
	}

	/**
	 * 复制对象值
	 * 
	 * @param dest
	 *            目标对象
	 * @param src
	 *            源对象
	 */
	private static void copyObjectValue(Object dest, Object src) {
		BeanUtil.copyNotNullProperties(dest, src);
	}

	/**
	 * 把Number转换为基本类型对象
	 * 
	 * @param str
	 * @param c
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T number2BasicTypes(Number num, Class<T> c) {
		if (Integer.class == c || int.class == c) {
			return (T) Integer.valueOf(num.intValue());
		} else if (Long.class == c || long.class == c) {
			return (T) Long.valueOf(num.longValue());
		} else if (Double.class == c || double.class == c) {
			return (T) Double.valueOf(num.doubleValue());
		} else if (Float.class == c || float.class == c) {
			return (T) Float.valueOf(num.floatValue());
		} else if (Short.class == c || short.class == c) {
			return (T) Short.valueOf(num.shortValue());
		} else {
			throw new IllegalArgumentException("无效的类型：" + c.getSimpleName());
		}
	}
	
	/**
	 * 把字符串转换为基本类型对象（String、Integer、Short、Double、Float） 前提条件：字符串是数字
	 * 
	 * @param str
	 * @param c
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T stringNumber2BasicTypes(String str, Class<T> c) {
		if (String.class.equals(c)) {
			return (T) str;
		} else if (Integer.class.equals(c)|| int.class == c) {
			return (T) Integer.valueOf(str);
		} else if (Long.class.equals(c)|| long.class == c) {
			return (T) Long.valueOf(str);
		} else if (Double.class.equals(c)|| double.class == c) {
			return (T) Double.valueOf(str);
		} else if (Float.class.equals(c)|| float.class == c) {
			return (T) Float.valueOf(str);
		} else if (Short.class.equals(c)|| short.class == c) {
			return (T) Short.valueOf(str);
		} else {
			return null;
		}
	}
	
	/** 
     * 利用反射实现对象之间属性复制 
     * @param from 
     * @param to 
     */  
    public static void copyProperties(Object from, Object to) {  
        try {
			copyPropertiesExclude(from, to, null);
		} catch (Exception e) {
			e.printStackTrace();
		}  
    }  
      
    /** 
     * 复制对象属性 
     * @param from 
     * @param to 
     * @param excludsArray 排除属性列表 
     * @throws Exception 
     */  
    @SuppressWarnings("unchecked")  
    public static void copyPropertiesExclude(Object from, Object to, String[] excludsArray) throws Exception {  
        List<String> excludesList = null;  
        if(excludsArray != null && excludsArray.length > 0) {  
            excludesList = Arrays.asList(excludsArray); //构造列表对象  
        }  
        Method[] fromMethods = from.getClass().getDeclaredMethods();  
        Method[] toMethods = to.getClass().getDeclaredMethods();  
        Method fromMethod = null, toMethod = null;  
        String fromMethodName = null, toMethodName = null;  

        for (int i = 0; i < fromMethods.length; i++) {  
            fromMethod = fromMethods[i];  
            fromMethodName = fromMethod.getName();  
            if (!fromMethodName.contains("get"))  
                continue;  
            //排除列表检测  
            if(excludesList != null && excludesList.contains(fromMethodName.substring(3).toLowerCase())) {  
                continue;  
            }  
            toMethodName = "set" + fromMethodName.substring(3);  
            toMethod = findMethodByName(toMethods, toMethodName);  
            if (toMethod == null)  
                continue;  
            Object value = fromMethod.invoke(from, new Object[0]);  
            if(value == null)  
                continue;  
            //集合类判空处理  
            if(value instanceof Collection) {  
                Collection newValue = (Collection)value;  
                if(newValue.size() <= 0)  
                    continue;  
            }  
            toMethod.setAccessible(true);
           //System.out.println(value+"-"+fromMethodName.substring(3));
           toMethod.invoke(to, new Object[] {value});  
        }  
    }  
      
    /** 
     * 对象属性值复制，仅复制指定名称的属性值 
     * @param from 
     * @param to 
     * @param includsArray 
     * @throws Exception 
     */  
    @SuppressWarnings("unchecked")  
    public static void copyPropertiesInclude(Object from, Object to, String[] includsArray) throws Exception {  
        List<String> includesList = null;  
        if(includsArray != null && includsArray.length > 0) {  
            includesList = Arrays.asList(includsArray); //构造列表对象  
        } else {  
            return;  
        }  
        Method[] fromMethods = from.getClass().getDeclaredMethods();  
        Method[] toMethods = to.getClass().getDeclaredMethods();  
        Method fromMethod = null, toMethod = null;  
        String fromMethodName = null, toMethodName = null;  
        for (int i = 0; i < fromMethods.length; i++) {  
            fromMethod = fromMethods[i];  
            fromMethodName = fromMethod.getName();  
            if (!fromMethodName.contains("get"))  
                continue;  
            //排除列表检测  
            String str = fromMethodName.substring(3);  
            if(!includesList.contains(str.substring(0,1).toLowerCase() + str.substring(1))) {  
                continue;  
            }  
            toMethodName = "set" + fromMethodName.substring(3);  
            toMethod = findMethodByName(toMethods, toMethodName);  
            if (toMethod == null)  
                continue;  
            Object value = fromMethod.invoke(from, new Object[0]);  
            if(value == null)  
                continue;  
            //集合类判空处理  
            if(value instanceof Collection) {  
                Collection newValue = (Collection)value;  
                if(newValue.size() <= 0)  
                    continue;  
            }  
            toMethod.invoke(to, new Object[] {value});  
        }  
    }  
      
      
  
    /** 
     * 从方法数组中获取指定名称的方法 
     *  
     * @param methods 
     * @param name 
     * @return 
     */  
    public static Method findMethodByName(Method[] methods, String name) {  
        for (int j = 0; j < methods.length; j++) {  
            if (methods[j].getName().equals(name))  
                return methods[j];  
        }  
        return null;  
    }  

}
