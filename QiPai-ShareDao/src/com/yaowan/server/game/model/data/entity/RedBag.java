package com.yaowan.server.game.model.data.entity;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.annotation.Transient;
import com.yaowan.server.game.model.struct.RedBagInfo;

/**
 * 红包
 * 
 * @author G_T_C
 */
@Table(name = "red_bag", comment = "红包")
public class RedBag {

	@Transient
	private static Gson gson = new Gson();

	@Id(strategy = Strategy.AUTO)
	@Column(comment = "主键")
	private long id;

	@Column(comment = "开始时间")
	private int startTime;

	@Column(comment = "结束时间")
	private int endTime;

	@Column(comment = "json", length = 2048)
	private String info;

	@Column(comment = "每日限制")
	private int dayLimit;

	@Transient
	private List<RedBagInfo> infoList;
	
	@Transient
	private int goingLimit;//正在进行中次数限制

	public long getId() {
		return id;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public String getInfo() {
		return info;
	}

	public int getDayLimit() {
		return dayLimit;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public void setInfo(String info) {
		this.info = info;
		infoList = gson.fromJson(info, new TypeToken<List<RedBagInfo>>() {  
                }.getType()); 
	}

	public void setDayLimit(int dayLimit) {
		this.dayLimit = dayLimit;
	}
	
	public List<RedBagInfo> getInfoList() {
		if(infoList == null){
			infoList = gson.fromJson(info, new TypeToken<List<RedBagInfo>>() {  
            }.getType()); 
		}
		return infoList;
	}

	public int getGoingLimit() {
		return goingLimit;
	}
	

	public void setGoingLimit(int goingLimit) {
		this.goingLimit = goingLimit;
	}
	

	public void setInfoList(List<RedBagInfo> infoList) {
		this.infoList = infoList;
	}

	@Override
	public String toString() {
		return "RedBag [id=" + id + ", startTime=" + startTime + ", endTime="
				+ endTime + ", info=" + info + ", dayLimit=" + dayLimit + "]";
	}

}
