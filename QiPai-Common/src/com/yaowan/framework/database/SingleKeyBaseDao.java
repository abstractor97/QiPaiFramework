/**
 * 
 */
package com.yaowan.framework.database;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.yaowan.framework.database.orm.ColumnMeta;
import com.yaowan.framework.database.orm.UpdateProperty;
import com.yaowan.framework.util.CommonUtils;
import com.yaowan.framework.util.LogUtil;

/**
 * @author huangyuyuan
 *
 */
public abstract class SingleKeyBaseDao<T, PK> extends BaseDao<T> {
	
	/**
	 * 默认根据主键更新
	 * @param t
	 * @return
	 */
	protected String getUpdateSql(T t){
		//木有id都不需要更新语句了、
		if (getEntityMeta().getIdField() == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		
		sb.append(" where ").append(getEntityMeta().getIdName()).append("=");
		Field field = getEntityMeta().getIdField();
		Object value = null;
		try {
			value = field.get(t);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (getEntityMeta().getIdClass() == String.class) {
			sb.append("'").append(value).append("'");
		} else {
			sb.append(value);
		}
		return getUpdateSql(t,sb.toString());
	}
	
	/**
	 * 根据实体更新
	 * @param t
	 * @return
	 */
	public boolean update(T t) {
		String sql = getUpdateSql(t);
		return executeSql(sql);
	}
	

	/**
	 * 使用addProperty执行部分字段更新
	 * 
	 * @param t
	 * @return
	 */
	public <V extends UpdateProperty> boolean updateProperty(V t) {
		String sql = getUpdatePropertySql(t);
		if (!sql.isEmpty()) {
			return executeSql(sql);
		}
		return false;
	}
	
	protected <V extends UpdateProperty> String getUpdatePropertySql(V t) {
		StringBuilder sb = new StringBuilder("update ");
		sb.append(getEntityMeta().getRealTableName(t));
		sb.append(" set ");
		int i = 0;
		Set<String> set = t.propertys();
		if (set == null || set.isEmpty()) {
			return "";
		}
		for (String name : set) {
			ColumnMeta columnMeta = getEntityMeta().getColumnMap().get(name);
			if (columnMeta == null) {
				LogUtil.error(name+" Not Found");
				continue;
			}
			if (i>0) {
				sb.append(",");
			}
			i++;
			sb.append(columnMeta.getName()).append("=");
			Field field = columnMeta.getField();
			Object value = null;
			try {
				value = field.get(t);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (field.getType() == String.class && value != null) {
				sb.append("'").append(value).append("'");
			} else {
				sb.append(value);
			}
		}
		sb.append(" where ").append(getEntityMeta().getIdName()).append("=");
		Field field = getEntityMeta().getIdField();
		Object value = null;
		try {
			value = field.get(t);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (getEntityMeta().getIdClass() == String.class) {
			sb.append("'").append(value).append("'");
		} else {
			sb.append(value);
		}
		t.clear();
		return sb.toString();
	}
	
	/**
	 * 删除语句
	 * @param t
	 * @return
	 */
	protected String getDeleteSql(T t) {
		StringBuilder sb = new StringBuilder("delete from ");
		sb.append(getEntityMeta().getRealTableName(t));
		sb.append(" where ").append(getEntityMeta().getIdName()).append("=");
		
		Field field = getEntityMeta().getIdField();
		Object value = null;
		try {
			value = field.get(t);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (getEntityMeta().getIdClass() == String.class) {
			sb.append("'").append(value).append("'");
		} else {
			sb.append(value);
		}
		return sb.toString();
	}
	
	/**
	 * 删除，一般表都应该设计个流水id主键
	 * @param t
	 * @return
	 */
	public boolean delete(T t) {
		String sql = getDeleteSql(t);
		return executeSql(sql);
	}
	
	/**
	 * 默认下主键名为id的对象获取
	 * 
	 * @param id
	 * @return
	 */
	public T findByKey(final PK id) {
		StringBuilder sb = new StringBuilder("select * from ");
		sb.append(getEntityMeta().getTableName());
		sb.append(" where ");
		sb.append(getEntityMeta().getIdName());
		sb.append("=");
		if (id.getClass() == String.class) {
			sb.append("'").append(id).append("'");
		} else {
			sb.append(id);
		}
		String sql = sb.toString();
		return find(sql);
	}
	
	/**
	 * 查询唯一值
	 * @param sql
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected PK findId(String sql) {
		try {
			Statement statement = getDB().getSession();
			ResultSet rs = statement.executeQuery(sql);
			PK key = null;
			if(rs.next()) {
				Object obj = rs.getObject(1);
				if (obj == null) {
					obj = CommonUtils.conver(0, getEntityMeta().getIdClass());
				}
				key = (PK)obj;
			} else {
				key = (PK) CommonUtils.conver(0, getEntityMeta().getIdClass());
			}
			rs.close();
			return key;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			getDB().closeSession();
		}
	}
	
	/**
	 * 查询主键map
	 * 
	 * @param sql
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map<PK,T> findForMap(String sql) {
		try {
			Statement statement = getDB().getSession();
			ResultSet rs = statement.executeQuery(sql);
			Map<PK,T> map = new HashMap<PK, T>();
			while (rs.next()) {
				ResultSetMetaData meta = rs.getMetaData();		
		        int cols = meta.getColumnCount();
		        T obj = getEntityClass().newInstance();
				for (int i = 1; i <= cols; i++) {
					try {
						Field field = null;
						if (meta.getColumnName(i).equals(getEntityMeta().getIdName())) {
							field = getEntityMeta().getIdField();
						} else {
							field = getEntityMeta().getColumnMap()
									.get(meta.getColumnName(i)).getField();
						}
						Object value = rs.getObject(i);
						if (!CommonUtils.setField(field, obj, value)) {
							// 类型不匹配
							continue;
						}
					} catch (Exception e) {
						// unnecessary do anything
					}
				}
				PK key = (PK)getEntityMeta().getIdField().get(obj);
				map.put(key, obj);		
			}
			rs.close();
			return map;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			getDB().closeSession();
		}
	}
}
