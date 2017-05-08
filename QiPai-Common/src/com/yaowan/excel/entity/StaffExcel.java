/**
 * 
 */
package com.yaowan.excel.entity;

import com.yaowan.framework.excel.ExcelObject;

/**
 * @author huangyuyuan
 *
 */
public class StaffExcel implements ExcelObject {
	private short id;
	private byte sex;
	private byte star;
	private String name;
	
	public int getId() {
		return id;
	}
	public void setId(short id) {
		this.id = id;
	}
	public byte getSex() {
		return sex;
	}
	public void setSex(byte sex) {
		this.sex = sex;
	}
	public byte getStar() {
		return star;
	}
	public void setStar(byte star) {
		this.star = star;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return "StaffExcel [id=" + id + ", sex=" + sex + ", star=" + star
				+ ", name=" + name + "]";
	}
}
