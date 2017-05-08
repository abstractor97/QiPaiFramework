/**
 * 
 */
package com.yaowan.framework.database.orm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.MappedSuperclass;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.annotation.Id.Strategy;

/**
 * 实体对象的汇总信息
 * 
 * @author zane
 *
 */
public class EntityMeta {
	
    public EntityMeta(Class<?> clazz){
		this.clazz = clazz;
		Table table = clazz.getAnnotation(Table.class);
		this.tableName = table.name().equals("") ? formatEntityName(clazz.getSimpleName()) : table.name();
		this.catalog = table.catalog();
		this.catalogStart = table.catalog_();
		List<Field> idList = new ArrayList<Field>();
		// 假设有超类注解
		Field[] array = clazz.getDeclaredFields();
		
		Class<?> superClazz = clazz.getSuperclass();
		while(superClazz != null) {
			MappedSuperclass mapped = superClazz.getAnnotation(MappedSuperclass.class);
			if (mapped != null) {
				array = ArrayUtils.addAll(array, superClazz.getDeclaredFields());
			}
			superClazz = superClazz.getSuperclass();
		}
		
		for (Field field : array) {
			field.setAccessible(true);
			if (field.getName().equals(table.catalogby())) {
				this.catalogField = field;
			}
			Column column = field.getAnnotation(Column.class);
			if (column != null) {
				ColumnMeta columnMeta = new ColumnMeta(field);
				this.columnList.add(columnMeta);
				this.columnMap.put(columnMeta.getName(), columnMeta);
				this.columnMap.put(field.getName(), columnMeta);
			}

			Id id = field.getAnnotation(Id.class);
			if (id == null || id.sort() > 1) {// 只取第一主键
				continue;
			}
			idList.add(field);
		}
		// 多个主键首取sort0
		if (idList.size() == 1) {
			Field field = idList.get(0);
			
			Id id = field.getAnnotation(Id.class);
			
			this.idField = field;
			this.idName = formatEntityName(field.getName());
			this.idClass = field.getType();
			this.idType = id.strategy();
		} else if (idList.size() >= 2) {
			Field field = null;
			for (Field data : idList) {
				Id id = data.getAnnotation(Id.class);
				if (id.strategy() == Strategy.AUTO) {
					field = data;
					break;
				}
				if (id.sort() == 0) {
					field = data;
					break;
				}
			}
			if (field == null) {
				field = idList.get(0);
			}
			
			Id id = field.getAnnotation(Id.class);
			
			this.idField = field;
			this.idName = formatEntityName(field.getName());
			this.idClass = field.getType();
			this.idType = id.strategy();
		}
	}
    
    /**
     * 大写自动加下划线
     * @param name
     * @return
     */
    public static String formatEntityName(String name) {
		StringBuffer st = new StringBuffer();
		st.append(Character.toLowerCase(name.charAt(0)));
		// 分辨大寫為下劃綫
		for (int i = 1; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c >= 'A' && c <= 'Z') {
				st.append('_').append(Character.toLowerCase(c));
			} else {
				st.append(c);
			}
		}
		return st.toString();
	}

	/**
	 * 实体类
	 */
	private Class<?> clazz;
	/**
	 * 表名
	 */
	private String tableName;
	
	/**
	 * catalog分表数
	 */
	private Integer catalog;
	
	/**
	 * catalog分表起点
	 */
	private Integer catalogStart;
	
	/**
	 * catalog分表名根据字段
	 */
	private Field catalogField;
	
	/**
	 * id名称
	 */
	private String idName;
	
	/**
	 * id策略
	 */
	private Class<?> idClass;
	
	/**
	 * id策略
	 */
	private Strategy idType;
	
	/**
	 * id策略
	 */
	private Field idField;
	
	/**
	 * 所有字段列表
	 */
	private List<ColumnMeta> columnList = new ArrayList<ColumnMeta>();
	
	/**
	 * 字段（及数据库字段并列）索引
	 */
	private Map<String, ColumnMeta> columnMap = new HashMap<String, ColumnMeta>();
	

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getIdName() {
		return idName;
	}

	public void setIdName(String idName) {
		this.idName = idName;
	}

	public Strategy getIdType() {
		return idType;
	}

	public void setIdType(Strategy idType) {
		this.idType = idType;
	}


	public List<ColumnMeta> getColumnList() {
		return columnList;
	}

	public void setColumnList(List<ColumnMeta> columnList) {
		this.columnList = columnList;
	}

	public Map<String,ColumnMeta> getColumnMap() {
		return columnMap;
	}

	public void setColumnMap(Map<String,ColumnMeta> columnMap) {
		this.columnMap = columnMap;
	}

	public Class<?> getIdClass() {
		return idClass;
	}

	public void setIdClass(Class<?> idClass) {
		this.idClass = idClass;
	}

	public Field getIdField() {
		return idField;
	}

	public void setIdField(Field idField) {
		this.idField = idField;
	}

	public Integer getCatalog() {
		return catalog;
	}

	public void setCatalog(Integer catalog) {
		this.catalog = catalog;
	}


	public Integer getCatalogStart() {
		return catalogStart;
	}

	public void setCatalogStart(Integer catalogStart) {
		this.catalogStart = catalogStart;
	}

	public Field getCatalogField() {
		return catalogField;
	}

	public void setCatalogField(Field catalogField) {
		this.catalogField = catalogField;
	}
	
	/**
	 * 自动根据@Table 注解的catalogby 获得分表名
	 * @param t
	 * @return
	 */
	public String getRealTableName(Object t) {
		if (catalog > 0) {
			StringBuffer sb = new StringBuffer();
			Object value = null;
			try {
				value = catalogField.get(t);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 自动分表
			Number real = (Number) value;
			sb.append(tableName).append("_")
					.append(real.longValue() % catalog);
			return sb.toString();
		} else {
			return tableName;
		}
	}
}
