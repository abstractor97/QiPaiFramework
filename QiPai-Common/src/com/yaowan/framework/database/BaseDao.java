/**
 * 
 */
package com.yaowan.framework.database;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.db.BaseDB;
import com.yaowan.framework.database.orm.ColumnMeta;
import com.yaowan.framework.database.orm.EntityMeta;
import com.yaowan.framework.database.orm.ResultCallBack;
import com.yaowan.framework.database.orm.UpdateProperty;
import com.yaowan.framework.util.CommonUtils;
import com.yaowan.framework.util.LogUtil;

/**
 * 基础实体操作类
 * @author zane 2016年8月19日 下午6:02:10
 *
 */
public abstract class BaseDao<T> {
	
	private static final char DBSeparate = '`';
	
	protected Class<T> entityClass;
	
	protected EntityMeta entityMeta;
	
	protected abstract BaseDB getDB();
	
	@SuppressWarnings("unchecked")
	public BaseDao() {
		Class<?> c = getClass();
		Type t = c.getGenericSuperclass();
		if (t instanceof ParameterizedType) {
			Type[] p = ((ParameterizedType) t).getActualTypeArguments();
			entityClass = (Class<T>) p[0];
			entityMeta = new EntityMeta(entityClass);
		}
	}
	
	protected Class<T> getEntityClass(){
		return this.entityClass;
	}
	
	protected EntityMeta getEntityMeta(){
		return this.entityMeta;
	}
	protected String getInsertSql(T t) {
		StringBuilder sb = new StringBuilder("insert into ");
		StringBuilder valueSb = new StringBuilder(" values (");
		sb.append(getEntityMeta().getRealTableName(t));
		sb.append(" (");
		if (getEntityMeta().getIdType() == Strategy.IDENTITY) {
			sb.append(getEntityMeta().getIdName()).append(",");
			Field field = getEntityMeta().getIdField();
			Object value = null;
			try {
				value = field.get(t);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (getEntityMeta().getIdField().getType() == String.class && value != null) {
				valueSb.append("'").append(value).append("'");
			} else {
				valueSb.append(value);
			}
			valueSb.append(",");
		}
		boolean flag = false;
		for (ColumnMeta columnMeta : getEntityMeta().getColumnList()) {
			//所有列要排除id列
			if (columnMeta.getField() == getEntityMeta().getIdField()) {
				continue;
			}
			//第一列之后都要增加逗号
			if (flag) {
				valueSb.append(",");
				sb.append(",");
			}
			
			flag = true;
			sb.append(DBSeparate);
			sb.append(columnMeta.getName());
			sb.append(DBSeparate);
			Field field = columnMeta.getField();
			Object value = null;
			try {
				value = field.get(t);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (field.getType() == String.class && value != null) {
				valueSb.append("'").append(value).append("'");
			} else {
				valueSb.append(value);
			}		
		}
		valueSb.append(")");
		sb.append(") ").append(valueSb);
		return sb.toString();
	}
	
	/**
	 * 更新语句头部分表会复杂些
	 * @param t
	 * @return
	 */
	private String getUpdateHeadSql(T t,String tableName){
		StringBuilder sb = new StringBuilder("update ");
		sb.append(tableName);
		sb.append(" set ");
		boolean flag = false;
		for (ColumnMeta columnMeta : getEntityMeta().getColumnList()) {
			//所有列要排除id列
			if (columnMeta.getField() == getEntityMeta().getIdField()) {
				continue;
			}
			//第一列之后都要增加逗号
			if (flag) {
				sb.append(",");
			}
			
			flag = true;
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
		return sb.toString();
	}
	
	/**
	 * 更新语句头部 表名默认根据关键值查到分表名
	 * 子类可重新继承该方法自定义表名
	 * @param t
	 * @return
	 */
	private String getUpdateHeadSql(T t){
		StringBuilder sb = new StringBuilder();
		sb.append(getEntityMeta().getRealTableName(t));
		return getUpdateHeadSql(t,sb.toString());
	}
	
	/**
	 * 根据查询条件更新
	 * @param t
	 * @return
	 */
	protected String getUpdateSql(T t, String where) {
		//木有id都不需要更新语句了、
		if (getEntityMeta().getIdField() == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(getUpdateHeadSql(t));	
		sb.append(" ");
		sb.append(where);
		return sb.toString();
	}
	
	/**
	 * @author zane 2016年8月25日 下午6:03:20
	 * 自动装箱传入对象
	 * @param t
	 * @return
	 */
	private <V> V formObject(ResultSet rs, Class<V> clazz) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        try {
			V obj = clazz.newInstance();
			for (int i = 1; i <= cols; i++) {
				try {
					Field field = CommonUtils.getField(meta.getColumnName(i), obj);
					Object value = rs.getObject(i);
					if (!CommonUtils.setField(field, obj, value)) {
						// 类型不匹配
						continue;
					}
				} catch (Exception e) {
					// unnecessary do anything
				}
			}
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
		}      
		return null;
	}
	

	/**
	 * zane
	 * 自动装箱基本对象
	 * @param t
	 * @return
	 */
	protected  T formObject(ResultSet rs) throws SQLException{
		ResultSetMetaData meta = rs.getMetaData();		
        int cols = meta.getColumnCount();
        try {
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
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
		} 
        
		return null;
	}
	/**
	 * 带有自增ID的插入
	 * @param t
	 * @return
	 */
	private long insertReturnId(T t) {
		try {
			Statement statement = getDB().getSession();
			String sql = getInsertSql(t);
			int result = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			long id = 0;
			if(result > 0) {
				ResultSet rs = statement.getGeneratedKeys();
				if(rs.next()) {
					id = rs.getLong(1);
				}
				rs.close();
			}
			return id;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}finally{
			getDB().closeSession();
		}
	}
	/**
	 * 不带自增ID的插入
	 * @param t
	 * @return
	 */
	private boolean insertNoId(T t) {
		String sql = getInsertSql(t);
		return executeSql(sql);
		
	}
	
	public void insert(T t){
		if (getEntityMeta().getIdType() == Strategy.AUTO) {
			long id = insertReturnId(t);
			CommonUtils.setField(getEntityMeta().getIdField(), t, id);
		} else {
			insertNoId(t);
		}
	}
	
	public void insertAll(Collection<T> list) {
		try {
			Statement statement = getDB().getSession();
			for(T t : list) {
				statement.addBatch(getInsertSql(t));
			}
			int[] result = statement.executeBatch();
			if (getEntityMeta().getIdType() != Strategy.IDENTITY) {
				long id = 0;
				if(result[0] > 0) {
					ResultSet rs = statement.getGeneratedKeys();
					for(T t : list) {
						if(rs.next()){
							id = rs.getLong(1);
							CommonUtils.setField(getEntityMeta().getIdField(), t, id);
						}
					}
					rs.close();
				}
			}		
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}finally{
			getDB().closeSession();
		}
	}

	
	
	protected boolean executeBatch(List<String> list) {
		if (list == null || list.isEmpty()) {
			return false;
		}
		try {
			Statement statement = getDB().getSession();
			for (String t : list) {
//				LogUtil.info("executeSql"+t);
				statement.addBatch(t);
			}
			int[] result = statement.executeBatch();
			if (result[0] > 0) {
				return true;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			getDB().closeSession();
		}
		return false;
	}
	
	/**
	 * 个性化更新语句
	 * @param t
	 * @return
	 */
	protected boolean executeSql(String sql) {
		try {
			Statement statement = getDB().getSession();
			int result = statement.executeUpdate(sql);
//			LogUtil.info("executeSql"+sql);
			if(result > 0) {
				return true;
			}
			return false;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			getDB().closeSession();
		}
	}
	
	/**
	 * 根据语句查询一个实体
	 * 
	 * @param id
	 * @return
	 */
	protected T find(String sql) {
		try {
			Statement statement = getDB().getSession();
			ResultSet rs = statement.executeQuery(sql);
			T t = null;
			if(rs.next()) {
				t = formObject(rs);
			}
			rs.close();
			return t;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			getDB().closeSession();
		}
	}
	
	/**
	 * 查询语句外部处理ResultSet
	 * 
	 * @param id
	 * @return
	 */
	protected void find(String sql,ResultCallBack back) {
		try {
			Statement statement = getDB().getSession();
			ResultSet rs = statement.executeQuery(sql);
			back.onResult(rs);
			rs.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			getDB().closeSession();
		}
	}
	
	

	
	/**
	 * 查询多个对象
	 * @param sql
	 * @return
	 */
	protected List<T> findList(String sql) {
		try {
			Statement statement = getDB().getSession();
			ResultSet rs = statement.executeQuery(sql);
			List<T> list = new ArrayList<T>();
			while(rs.next()) {
				list.add(formObject(rs));
			}
			rs.close();
			return list;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			getDB().closeSession();
		}
	}
	
	/**
	 * 查询多个对象，列名与V对象字段一一对应
	 * @param sql
	 * @return
	 */
	protected <V> List<V> findList(String sql, Class<V> clazz) {
		try {
			Statement statement = getDB().getSession();
			ResultSet rs = statement.executeQuery(sql);
			List<V> list = new ArrayList<V>();
			while(rs.next()) {
				list.add(formObject(rs,clazz));
			}
			rs.close();
			return list;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			getDB().closeSession();
		}
	}
	
	/**
	 * 查询所有对象
	 * @param sql
	 * @return
	 */
	public List<T> findAll() {
		StringBuilder sb = new StringBuilder("select * from ");
		sb.append(getEntityMeta().getTableName());
		return findList(sb.toString());
	}
	
	/**
	 * 查询出符合条件的id,返回值应与字段所属类型相符
	 * @param sql
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <V> List<V> findIdList(String sql) {
		try {
			Statement statement = getDB().getSession();
			ResultSet rs = statement.executeQuery(sql);
			List<V> list = new ArrayList<V>();
			while(rs.next()) {
				list.add((V)rs.getObject(1));
			}
			rs.close();
			return list;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			getDB().closeSession();
		}
	}
	
	/**
	 * 查询多列
	 * 
	 * @param sql
	 * @return
	 */
	protected List<Object[]> findColumn(String sql) {
		try {
			Statement statement = getDB().getSession();
			ResultSet rs = statement.executeQuery(sql);
			List<Object[]> list = new ArrayList<Object[]>();
			while (rs.next()) {
				Object[] data = new Object[rs.getMetaData().getColumnCount()];
				for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
					data[i] = rs.getObject(i + 1);

				}
				list.add(data);
			}
			rs.close();
			return list;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			getDB().closeSession();
		}
	}
	
	/**
	 * 查询一个统计数
	 * 
	 * @param sql
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <V extends Number> V findNumber(String sql, Class<V> clazz) {
		try {
			Statement statement = getDB().getSession();
			ResultSet rs = statement.executeQuery(sql);
			V v = null;
			if (rs.next()) {
				Object obj = rs.getObject(1);
				if (obj == null) {
					obj = new Integer(0);
				}
				v = (V) CommonUtils.conver(obj, clazz);
			}else{
				v = (V) CommonUtils.conver(0, clazz);
			}
			rs.close();
			return v;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			getDB().closeSession();
		}
	}
	
	public void truncate(){
		StringBuilder sb = new StringBuilder("truncate ");
		sb.append(getEntityMeta().getTableName());
		executeSql(sb.toString());
	}
	
	/**
	 * 根据实体更新
	 * @param t
	 * @return
	 */
	public boolean updateAllColumn(T t) {
		String sql = getUpdateHeadSql(t);
		return executeSql(sql);
	}
}
