package com.yaowan.framework.util;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public final class JSONObject extends HashMap<String, Object> {

	private static final long serialVersionUID = 4838514457948241231L;

	private static Gson gson = null;

	/**
	 * 初始化gson
	 */
	private static void init() {
		if (gson != null) {
			return;
		}
		GsonBuilder builder = new GsonBuilder();
		gson = builder.create();
	}

	public JSONObject() {
		super();
	}

	/**
	 * json解码
	 * 
	 * @param json
	 * @return
	 * @throws JsonSyntaxException
	 */
	public JSONObject(String json) throws JsonSyntaxException {
		init();
		HashMap<String, Object> jsonMap = gson.fromJson(json,
				new TypeToken<HashMap<String, Object>>() {
				}.getType());
		if (jsonMap != null) {
			this.putAll(jsonMap);
		}
	}

	/**
	 * json解码
	 * 
	 * @param json
	 * @return
	 */
	public static JSONObject decode(String json) {
		return new JSONObject(json);
	}

	/**
	 * json解码json数组
	 * String str1="[{'name':2,'age':15},{'name':2,'age':25}]";
	 * @param json
	 * @param clazz
	 * @return
	 */
	public static <T> List<T> decodeJsonArray(String json, Class<T[]> clazz) {
		init();
		T[] array = gson.fromJson(json, clazz);
		return Arrays.asList(array);
	}

	/**
	 * json转任意类型
	 * 
	 * @param json
	 * @param typeOfT
	 *            类型
	 * @return T
	 * @throws JsonSyntaxException
	 */
	public static <T> T decode(String json, Type typeOfT)
			throws JsonSyntaxException {
		init();
		return gson.fromJson(json, typeOfT);
	}

	/**
	 * json编码
	 * 
	 * @param src
	 * @return String
	 */
	public static String encode(Object src) {
		init();
		return gson.toJson(src);
	}

	/**
	 * put
	 * 
	 * @param key
	 * @param value
	 */
	public JSONObject put(String key, Object value) {
		super.put(key, value);
		return this;
	}

	/**
	 * getObject
	 * 
	 * @param key
	 * @return
	 */
	public Object getObject(String key) {
		return super.get(key);
	}

	/**
	 * getString
	 * 
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		Object v = this.get(key);
		if (v != null) {
			return v.toString();
		}
		return "";
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public byte getByte(String key) {
		return (byte) getDouble(key);
	}

	/**
	 * getInt
	 * 
	 * @param key
	 * @return
	 */
	public int getInt(String key) {
		return (int) getDouble(key);
	}

	/**
	 * getLong
	 * 
	 * @param key
	 * @return
	 */
	public long getLong(String key) {
		return Long.valueOf(get(key).toString());
	}

	/**
	 * getDouble
	 * 
	 * @param key
	 * @return
	 */
	public double getDouble(String key) {
		return Double.valueOf(get(key).toString());
	}

	/**
	 * getShort
	 * 
	 * @author ruan
	 * @param key
	 * @return
	 */
	public short getShort(String key) {
		return (short) getDouble(key);
	}

	/**
	 * getFloat
	 * 
	 * @param key
	 * @return
	 */
	public float getFloat(String key) {
		return (float) getDouble(key);
	}

	/**
	 * getBoolean
	 * 
	 * @param key
	 * @return
	 */
	public boolean getBoolean(String key) {
		Object v = get(key);
		if (v != null) {
			try {
				return Boolean.parseBoolean(v.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * getJSON
	 * 
	 * @param key
	 * @return JSON
	 */
	@SuppressWarnings("unchecked")
	public JSONObject getJSON(String key) {
		Map<String, ?> obj = (Map<String, ?>) get(key);
		if (obj == null) {
			return null;
		}
		JSONObject value = new JSONObject();
		value.putAll(obj);
		return value;
	}

	/**
	 * toString
	 */
	public String toString() {
		return encode(this);
	}
}
