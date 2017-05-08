/**
 * 
 */
package com.yaowan.framework.database.orm;

import java.lang.reflect.Field;

import com.yaowan.framework.database.annotation.Column;

/**
 * 表列的实体信息
 * @author zane
 *
 */
public class ColumnMeta {
	
	/**
	 * 字段类型
	 */
	private Class<?> clazz;
	/**
	 * 字段对应的Field
	 */
	private Field field;
	/**
	 * 字段名
	 */
	private String name;
	/**
	 * 字段长度
	 */
	private int length;
	
	public ColumnMeta(Field field) {
		this.field = field;		
		this.field.setAccessible(true);
		this.clazz = field.getType();
		this.name = EntityMeta.formatEntityName(field.getName());	
		Column column = field.getAnnotation(Column.class);
		this.length = column.length();
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}
}
