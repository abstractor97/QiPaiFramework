/**
 * 
 */
package com.yaowan.tools.dbsync;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.yaowan.core.base.GlobalConfig;
import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Index.Type;
import com.yaowan.framework.database.annotation.Index.Way;
import com.yaowan.framework.database.annotation.MappedSuperclass;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.annotation.Transient;
import com.yaowan.framework.database.orm.EntityMeta;

/**
 * @author huangyuyuan
 *
 */
public class TableCreator {
	
	/**
	 * 创建表
	 * 
	 * @param statement
	 * @param clazz
	 * @param tableName
	 * @throws SQLException
	 */
	public static void createTable(Statement statement, Class<?> clazz, String databaseName, String tableName) throws SQLException {
		
		List<String> lineList = new ArrayList<>();
		// 假设有超类注解
		Field[] array = clazz.getDeclaredFields();
		Class<?> superClazz = clazz.getSuperclass();
		com.yaowan.framework.database.annotation.MappedSuperclass mapped = superClazz
				.getAnnotation(com.yaowan.framework.database.annotation.MappedSuperclass.class);
		if (mapped != null) {
			array = ArrayUtils.addAll(array, superClazz.getDeclaredFields());
		}
		Field[] fields = clazz.getDeclaredFields();
		for(int i = 0; i < array.length; i++) {
			Field field = array[i];
			Transient transi = field.getAnnotation(Transient.class);
			//缓存的字段不需要存库
			if(transi != null) {
				continue;
			}
			lineList.add(formatFieldDefine(field));
		}
		//主键
		formatIdDefine(lineList, clazz);
		//索引
		formatIndexDefine(lineList, clazz);
		
		StringBuffer sql = new StringBuffer();
		sql.append("CREATE TABLE " + databaseName + "." + tableName + " (");
		for(int i = 0; i < lineList.size(); i++) {
			sql.append(lineList.get(i));
			if(i < lineList.size() - 1) {
				sql.append(",");
			}
		}
		sql.append(") ");
		sql.append("ENGINE=InnoDB DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=COMPACT ");
		
		Table table = clazz.getAnnotation(Table.class);
		
		if(!"".equals(table.comment())) {
			sql.append("COMMENT = '" + table.comment() + "'");
		}
		sql.append(";");

		System.out.println(sql.toString());
		System.out.println("");
		statement.execute(sql.toString());
		initAutoIncrementId(array,statement,tableName);//如果有自增id，执行自增id初始化，没有就不会执行
	}
	
	/**
	 * 获取主键
	 * 
	 * @param clazz
	 * @param tableName
	 * @return
	 * @throws SQLException 
	 */
	private static void formatIdDefine(List<String> lineList, Class<?> clazz) {
		StringBuffer buffer = new StringBuffer();
		// 假设有超类注解
		Field[] array = clazz.getDeclaredFields();
		Class<?> superClazz = clazz.getSuperclass();
		MappedSuperclass mapped = superClazz
				.getAnnotation(MappedSuperclass.class);
		if (mapped != null) {
			array = ArrayUtils.addAll(array, superClazz.getDeclaredFields());
		}
		Field[] fields = clazz.getDeclaredFields();
		
		List<Field> idFieldList = new ArrayList<>();
		//排序ID字段
		for(int i = 0; i < array.length; i++) {
			Field field = array[i];
			Transient transi = field.getAnnotation(Transient.class);
			if(transi != null) {
				continue;
			}
			Id id = field.getAnnotation(Id.class);
			if(id == null) {
				continue;
			}
			if(idFieldList.isEmpty()) {
				idFieldList.add(field);
			} else {
				//排序ID
				for(int j = 0; j < idFieldList.size(); j++) {
					Id tempId = idFieldList.get(j).getAnnotation(Id.class);
					if(id.sort() < tempId.sort()) {
						idFieldList.add(j, field);
						break;
					}
					if(j >= idFieldList.size() - 1) {
						idFieldList.add(field);
						break;
					}
				}
			}
		}
		
		if(idFieldList.isEmpty()) {
			return;
		}
		//组装SQL
		buffer.append("PRIMARY KEY (");
		for(int i = 0; i < idFieldList.size(); i++) {
			Field field = idFieldList.get(i);
			buffer.append("`");
			Column column = field.getAnnotation(Column.class);
			if(column == null) {
				buffer.append(EntityMeta.formatEntityName(field.getName()));
			} else {
				buffer.append(EntityMeta.formatEntityName(field.getName()));
			}
			buffer.append("`");
			
			if(i < idFieldList.size() - 1) {
				buffer.append(",");
			}
		}
		buffer.append(")");
		
		lineList.add(buffer.toString());
	}
	
	/**
	 * 获取索引
	 * 
	 * @param clazz
	 * @param tableName
	 * @return
	 * @throws SQLException 
	 */
	private static void formatIndexDefine(List<String> lineList, Class<?> clazz) {
		
		Index index = clazz.getAnnotation(Index.class);
		if(index == null) {
			return;
		}
		String[] names = index.names();
		String[] indexs = index.indexs();
		Type[] types = index.types();
		Way[] ways = index.ways();
		if(names.length != indexs.length) {
			return;
		}
		
		for(int i = 0; i < names.length; i++) {
			StringBuffer buffer = new StringBuffer();
			
			String name = names[i];
			String idx = indexs[i];
			
			String type = "";
			if(i >= types.length) {
				type = "";
			} else {
				type = types[i].value();
			}
			String way = "";
			if(i >= ways.length) {
				way = "";
			} else {
				way = ways[i].value();
			}
			
			buffer.append(type + " INDEX `" + name + "` (`" + idx + "`)");
			if(!"".equals(way)) {
				buffer.append(" USING " + way);
			}
			lineList.add(buffer.toString());
		}
	}
	
	/**
	 * 字段定义
	 * 
	 * @param field
	 * @return
	 */
	private static String formatFieldDefine(Field field) {
		StringBuffer buffer = new StringBuffer();
		
		Column column = field.getAnnotation(Column.class);
		Id id = field.getAnnotation(Id.class);
		if(column == null) {
			buffer.append("`" + EntityMeta.formatEntityName(field.getName()) + "` " + getFieldTypeLength(field) + " NOT NULL");
			if(id != null && id.strategy() == Strategy.AUTO) {
				buffer.append(" AUTO_INCREMENT ");
			}
		} else {
			buffer.append("`" + EntityMeta.formatEntityName(field.getName()) + "` " + getFieldTypeLength(field) + " NOT NULL");
			if(id != null && id.strategy() == Strategy.AUTO) {
				buffer.append(" AUTO_INCREMENT ");
			}
			buffer.append(" COMMENT '" + column.comment() + "'");
		}
		
		return buffer.toString();
	}
	
	/**
	 * 字段类型与长度定义
	 * 
	 * @param field
	 * @return
	 */
	public static String getFieldTypeLength(Field field) {
		String typeName = field.getType().getSimpleName().toLowerCase();
		if("byte".equals(typeName)) {
			return "tinyint(4)";
		} else if("short".equals(typeName)) {
			return "smallint(6)";
		} else if("int".equals(typeName)) {
			return "int(11)";
		} else if("long".equals(typeName)) {
			return "bigint(20)";
		} else if("float".equals(typeName)) {
			return "float(8,2)";
		} else if("double".equals(typeName)) {
			return "double(16,2)";
		} else if("string".equals(typeName)) {
			Column column = field.getAnnotation(Column.class);
			if(column == null) {
				return "varchar(255)";
			} else {
				return "varchar(" + column.length() + ")";
			}
		}
		return "tinyint(4)";
	}
	
	/**
	 * 如果有自增id，执行自增id初始化，没有就不会执行
	 * @author G_T_C
	 * @param array
	 * @param statement
	 * @param tableName
	 * @throws SQLException
	 */
	public static void initAutoIncrementId(Field[] array, Statement statement, String tableName) throws SQLException{
		for(int i = 0; i < array.length; i++) {
			Field field = array[i];
			Transient transi = field.getAnnotation(Transient.class);
			//缓存的字段不需要存库
			if(transi != null) {
				continue;
			}
			Id id = field.getAnnotation(Id.class);
			if(id != null && id.strategy() == Strategy.AUTO) {
				String sql = "alter table `"+tableName+"` auto_increment ="+GlobalConfig.initDBIncrementId;
				System.out.println(sql);
				statement.execute(sql);
				break;
			}	
		}
		
	}
}
