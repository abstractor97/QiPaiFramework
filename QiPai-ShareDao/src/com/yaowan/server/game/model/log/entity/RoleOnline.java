/**
 * Project Name:dfh3_server
 * File Name:RoleOnline.java
 * Package Name:data.log
 * Date:2016年5月24日下午3:50:42
 * Copyright (c) 2016, jiangming@qq.com All Rights Reserved.
 *
*/

package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;



/**
 * 实时在线 
 */
@Table(name = "role_online", comment = "实时在线 ")
@Index(names = {"date_time"}, indexs = {"date_time"})
public class RoleOnline {
	/**
	 * 记录时间
	 */
	@Column(comment = "记录时间")
	private int dateTime;
	/**
	 * 在线人数
	 */
	@Column(comment = "在线人数")
	private short num;
	
	public int getDateTime() {
		return dateTime;
	}
	public void setDateTime(int dateTime) {
		this.dateTime = dateTime;
	}
	public short getNum() {
		return num;
	}
	public void setNum(short num) {
		this.num = num;
	}
	
}

