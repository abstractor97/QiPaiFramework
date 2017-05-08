/**
 * 
 */
package com.yaowan.server.game.model.data.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;

/**
 * @author zane
 *
 */
@Table(name = "notice")
public class Notice {

	@Id(strategy = Strategy.IDENTITY)
	@Column(comment = "id")
	private int id;

	@Column(comment = "开始时间")
	private int stime;

	@Column(comment = "结束时间")
	private int etime;

	@Column(comment = "间隔时间")
	private int diff_time;

	@Column(comment = "状态1为开启，2为关闭")
	private byte status;

	@Column(comment = "类型,1为广播 ，2为活动公告，3为登录公告")
	private byte type;

	@Column(comment = "时间")
	private int time;

	@Column(comment = "内容", length = 600)
	private String content;

	@Column(comment = "公告图片地址")
	private String img = "";

	@Column(comment = "用于访问http展示页面内容")
	private String contentUrl = "";

	@Column(comment = "标题")
	private String title;

	@Column(comment = "登录推送类型。1为每日首次登录，2为每次登录")
	private int loginNoticeType;

	@Column(comment = "按登录公告该字段排序，数字越小越前")
	private int orderBy;
	
	@Column(comment = "公告等级，1为高级，2为低级")
	private int level;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStime() {
		return stime;
	}

	public int getEtime() {
		return etime;
	}

	public void setStime(int stime) {
		this.stime = stime;
	}

	public void setEtime(int etime) {
		this.etime = etime;
	}

	public int getDiff_time() {
		return diff_time;
	}

	public void setDiff_time(int diff_time) {
		this.diff_time = diff_time;
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getImg() {
		return img;
	}

	public String getContentUrl() {
		return contentUrl;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public void setContentUrl(String contentUrl) {
		this.contentUrl = contentUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getLoginNoticeType() {
		return loginNoticeType;
	}

	public int getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(int orderBy) {
		this.orderBy = orderBy;
	}

	public void setLoginNoticeType(int loginNoticeType) {
		this.loginNoticeType = loginNoticeType;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

}
