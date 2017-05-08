/**
 * 
 */
package com.yaowan.tools.dbsync;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.MappedSuperclass;
import com.yaowan.framework.database.orm.EntityMeta;

/**
 * @author huangyuyuan
 *
 */
public class TableRebuilder {
	
	public static void changeTable(Statement statement, Class<?> clazz, String dataBase, String tableName) {
		try {
			Map<String, String> nameTypeMap = getDbFieldMap(statement, dataBase, tableName);
			
			Map<String, String> classDataMap = new HashMap<>();
			Map<String, Field> fieldMap = new HashMap<>();
			Map<String, Field> dbFieldMap = new HashMap<>();
			// 假设有超类注解
			Field[] array = clazz.getDeclaredFields();
			Class<?> superClazz = clazz.getSuperclass();
			MappedSuperclass mapped = superClazz.getAnnotation(MappedSuperclass.class);
			if (mapped != null) {
				array = ArrayUtils.addAll(array, superClazz.getDeclaredFields());
			}
			for(Field field : array) {
				Column column = field.getAnnotation(Column.class);
				if(column == null) {
					continue;
				}
				String columnName = null;
				columnName = EntityMeta.formatEntityName(field.getName());
				classDataMap.put(field.getName(), columnName);
				fieldMap.put(field.getName(), field);
				dbFieldMap.put(columnName, field);
			}

			List<String> updateList = new ArrayList<>();
			
			for(String fieldName : classDataMap.keySet()) {
				String dbFieldName = classDataMap.get(fieldName);
				Field field = fieldMap.get(fieldName);
				if(!nameTypeMap.containsKey(dbFieldName)) {
					//添加数据库字段
					updateList.add(getAddSql(statement, dataBase, tableName, field, clazz));
					
				} else {
					String dbType = nameTypeMap.get(dbFieldName);
					if(!compareType(tableName, field, dbType)) {
						//修改数据库字段类型
						updateList.add(getModifySql(dataBase, tableName, field));
					}
					nameTypeMap.remove(dbFieldName);
				}
			}
			//删除数据库中的无用字段
			for(String dbFieldName : nameTypeMap.keySet()) {
				updateList.add(getDropSql(dataBase, tableName, dbFieldName));
			}
			fixIndex(statement, clazz, dataBase, tableName);
			
			if(updateList.size() > 0) {
				for(String str : updateList) {
					System.out.println(str);
					statement.addBatch(str);
				}
				System.out.println("");
				statement.executeBatch();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean compareType(String tableName, Field field, String dbType) {
		String typeName = field.getType().getSimpleName();
		boolean equals = false;
		if("byte".equals(typeName) && dbType.startsWith("tinyint")) {
			equals = true;
		} else if("short".equals(typeName) && dbType.startsWith("smallint")) {
			equals = true;
		} else if("int".equals(typeName) && dbType.startsWith("int")) {
			equals = true;
		} else if("long".equals(typeName) && dbType.startsWith("bigint")) {
			equals = true;
		} else if("float".equals(typeName) && dbType.startsWith("float")) {
			equals = true;
		} else if("String".equals(typeName) && dbType.startsWith("varchar")) {
			Column column = field.getAnnotation(Column.class);
			if(column == null) {
				equals = true;
			} else {
				int length = column.length();
				int dataLength = Integer.parseInt(dbType.substring(dbType.indexOf('(') + 1, dbType.indexOf(")")));
				if(length == dataLength) {
					equals = true;
				}
			}
			//TODO text类型的判断
		}
		return equals;
	}
	
	public static String getAddSql(Statement statement, String dataBase, String tableName, Field field, Class<?> clazz) throws SQLException {
		Column column = field.getAnnotation(Column.class);
		if(column == null) {
			return "";
		}
		String dbFieldName = null;
		dbFieldName = EntityMeta.formatEntityName(field.getName());
		StringBuilder sql = new StringBuilder();
		sql.append("ALTER TABLE " + dataBase + "." + tableName);
		sql.append(" ADD COLUMN ").append(dbFieldName).append(" ");
		sql.append(TableCreator.getFieldTypeLength(field));
		sql.append(" NOT NULL ");
		Id id = field.getAnnotation(Id.class);
		if(id != null && id.strategy() == Strategy.AUTO) {
			sql.append(" AUTO_INCREMENT ");
		}
		sql.append(" COMMENT '").append(column.comment()).append("'");
		//修改主键
		if(id != null) {
			boolean hasPrimary = DataBaseHandler.isTableHasPrimary(statement, dataBase, tableName);
			sql.append(",");
			if(hasPrimary) {
				sql.append("DROP PRIMARY KEY,");
			}
			sql.append("ADD PRIMARY KEY (");
			sql.append(getPrimaryKey(clazz));
			sql.append(")");
		}
		sql.append(";");
		return sql.toString();
	}
	
	public static String getModifySql(String dataBase, String tableName, Field field) {
		Column column = field.getAnnotation(Column.class);
		if(column == null) {
			return "";
		}
		String dbFieldName = null;
		dbFieldName = EntityMeta.formatEntityName(field.getName());
		StringBuilder sql = new StringBuilder();
		sql.append("ALTER TABLE " + dataBase + "." + tableName);
		sql.append(" MODIFY COLUMN ").append(dbFieldName).append(" ");
		sql.append(TableCreator.getFieldTypeLength(field));
		if("String".equals(field.getType().getSimpleName())) {
			sql.append(" CHARACTER SET utf8 ");
		}
		Id id = field.getAnnotation(Id.class);
		if(id != null && id.strategy() == Strategy.AUTO) {
			sql.append(" AUTO_INCREMENT ");
		}
		sql.append(" NOT NULL COMMENT '").append(column.comment()).append("';");
		return sql.toString();
	}
	
	public static String getDropSql(String dataBase, String tableName, String dbFieldName) {
		StringBuilder sql = new StringBuilder();
		sql.append("ALTER TABLE " + dataBase + "." + tableName);
		sql.append(" DROP COLUMN ").append(dbFieldName).append(";");
		return sql.toString();
	}
	
	public static String getPrimaryKey(Class<?> clazz) {
		Map<Integer, String> idMap = new HashMap<>();
		
		Field[] fields = clazz.getDeclaredFields();
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if(column == null) {
				continue;
			}
			Id tempId = field.getAnnotation(Id.class);
			if(tempId == null) {
				continue;
			}
			String dbFieldName = null;
			dbFieldName = EntityMeta.formatEntityName(field.getName());
			idMap.put(tempId.sort(), dbFieldName);
		}
		
		List<Integer> list = new ArrayList<>(idMap.keySet());
		Collections.sort(list);
		StringBuilder sql = new StringBuilder();
		for(int i = 0; i < list.size(); i++) {
			Integer sort = list.get(i);
			sql.append(idMap.get(sort));
			if(i < list.size() - 1) {
				sql.append(",");
			}
		}
		return sql.toString();
	}
	
	public static Map<String, String> getDbFieldMap(Statement statement,
			String dataBase, String tableName) throws SQLException {
		String sql = "DESC " + dataBase + "." + tableName;
		ResultSet rs = statement.executeQuery(sql);
		Map<String, String> nameTypeMap = new HashMap<>();
		while(rs.next()) {
			nameTypeMap.put(rs.getString("Field"), rs.getString("Type"));
		}
		rs.close();
		return nameTypeMap;
	}
	
	public static Map<String, Set<String>> getDbIndexMap(Statement statement,
			String dataBase, String tableName) throws SQLException {
		String sql = "SHOW KEYS FROM " + dataBase + "." + tableName + " WHERE key_name <> 'PRIMARY'";
		ResultSet rs = statement.executeQuery(sql);
		Map<String, Set<String>> indexMap = new HashMap<>();
		while(rs.next()) {
			String indexName = rs.getString("key_name");
			Set<String> columnSet = null;
			if(indexMap.containsKey(indexName)) {
				columnSet = indexMap.get(indexName);
			} else {
				columnSet = new HashSet<>();
				indexMap.put(indexName, columnSet);
			}
			columnSet.add(rs.getString("column_name"));
		}
		rs.close();
		return indexMap;
	}
	
	public static List<String> fixIndex(Statement statement, Class<?> clazz,
			String dataBase, String tableName) throws SQLException {
		Map<String, Set<String>> indexMap = getDbIndexMap(statement, dataBase, tableName);
		Index index = clazz.getAnnotation(Index.class);
		if(index == null) {
			return null;
		}
		if(index.names().length != index.indexs().length) {
			return null;
		}
		List<String> updateList = new ArrayList<>();
		for(int i = 0; i < index.names().length; i++) {
			String indexName = index.names()[i];
			String indexStr = index.indexs()[i];
			String[] indexColumns = indexStr.split(",");
			
			String type = "";
			if(i >= index.types().length) {
				type = "";
			} else {
				type = index.types()[i].value();
			}
			String way = "";
			if(i >= index.ways().length) {
				way = "BTREE";
			} else {
				way = index.ways()[i].value();
			}
			
			Set<String> indexSet = indexMap.get(indexName);
			if(indexSet == null) {
				//添加索引
				updateList.add(getAddIndexSql(dataBase, tableName, indexName, indexStr, type, way));
			} else {
				if(indexSet.size() != indexColumns.length) {
					//删除老索引，添加新索引
					updateList.add(getDropIndexSql(dataBase, tableName, indexName));
					updateList.add(getAddIndexSql(dataBase, tableName, indexName, indexStr, type, way));
				} else {
					boolean same = true;
					for(String tempColumn : indexColumns) {
						if(indexSet.contains(tempColumn.trim())) {
							continue;
						}
						same = false;
					}
					if(!same) {
						//删除老索引，添加新索引
						updateList.add(getDropIndexSql(dataBase, tableName, indexName));
						updateList.add(getAddIndexSql(dataBase, tableName, indexName, indexStr, type, way));
					}
				}
				indexMap.remove(indexName);
			}
		}
		//删除索引
		for(String indexName : indexMap.keySet()) {
			updateList.add(getDropIndexSql(dataBase, tableName, indexName));
		}
		
		for(String sql : updateList) {
			System.out.println(sql);
		}
		
		return updateList;
	}
	
	public static String getAddIndexSql(String dataBase, String tableName, String indexName, String indexColumns, String type, String way) {
		StringBuilder sql = new StringBuilder();
		sql.append("ALTER TABLE " + dataBase + "." + tableName);
		sql.append(" ADD " + type + " INDEX " + indexName + "(" + indexColumns + ") USING " + way + ";");
		return sql.toString();
	}
	
	public static String getDropIndexSql(String dataBase, String tableName, String indexName) {
		StringBuilder sql = new StringBuilder();
		sql.append("ALTER TABLE " + dataBase + "." + tableName);
		sql.append(" DROP INDEX " + indexName + ";");
		return sql.toString();
	}
}
