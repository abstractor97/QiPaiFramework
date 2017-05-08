package com.yaowan.server.game.model.data.entity;

import java.util.Map;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.annotation.Transient;
import com.yaowan.framework.database.orm.UpdateProperty;
import com.yaowan.framework.util.StringUtil;

/**
 * 
 * @author G_T_C
 */
@Table(name = "activity_role", comment = "用户活动参与表")
public class ActivityRole extends UpdateProperty{

	/**
	 * 角色ID
	 */
	@Id(sort = 1)
	@Column(comment = "角色ID")
	private long rid;

	/**
	 * 活动编号
	 */
	@Id(sort = 3)
	@Column(comment = "活动编号")
	private long aid;

	@Column(comment = "活动礼薄信息：格式：礼包id_是否领奖0未领奖 1可领奖 2已经领奖|")
	private String giftInfo;
	
	@Transient
	private Map<Long, Integer> giftInfoMap;
	
	/**
	 * 活动进度 未完成 0 完成 1
	 */
	@Column(comment = "活动进度 ：未完成 0 ，大于0完成次数")
	private int process;

	@Column(comment = "活动进行情况状态")
	private String status;

	public long getRid() {
		return rid;
	}

	public long getAid() {
		return aid;
	}

	public int getProcess() {
		return process;
	}

	public String getStatus() {
		return status;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public void setAid(long aid) {
		this.aid = aid;
	}

	public void setProcess(int process) {
		this.process = process;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getGiftInfo() {
		return giftInfo;
	}

	public Map<Long, Integer> getGiftInfoMap() {
		if(giftInfoMap == null){
			giftInfoMap = StringUtil.stringToMap(giftInfo, StringUtil.DELIMITER_BETWEEN_ITEMS, StringUtil.DELIMITER_INNER_ITEM,Long.class, Integer.class);
		}
		return giftInfoMap;
	}

	public void setGiftInfo(String giftInfo) {
		this.giftInfo = giftInfo;
	}

	public void setGiftInfoMap(Map<Long, Integer> giftInfoMap) {
		this.giftInfoMap = giftInfoMap;
		if(giftInfoMap != null && giftInfoMap.size()>0){
			this.giftInfo = StringUtil.mapToString(giftInfoMap);
		}
	}

	

}
