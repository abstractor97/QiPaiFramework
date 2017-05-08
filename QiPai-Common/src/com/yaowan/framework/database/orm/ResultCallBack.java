package com.yaowan.framework.database.orm;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ResultSet处理
 * 
 * @author zane 2016年8月25日 上午10:30:37
 *
 */
public interface ResultCallBack {
	
	 public void onResult(ResultSet rs) throws SQLException ;
}