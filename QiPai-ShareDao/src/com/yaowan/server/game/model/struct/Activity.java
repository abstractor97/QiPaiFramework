package com.yaowan.server.game.model.struct;

import java.util.Map;

import com.yaowan.server.game.model.data.entity.ActivityData;
import com.yaowan.server.game.model.data.entity.ActivityGiftBag;

public class Activity {

	private ActivityData activityData;
	
	private Map<Long, ActivityGiftBag> giftBagMap;
	
	//private int activityType = ;
	
	private int saveType;//1为配置表。2为数据库
	
	public Activity(ActivityData activityData, Map<Long, ActivityGiftBag> giftBagMap, int saveType) {
		this.activityData = activityData;
		this.giftBagMap = giftBagMap;
		this.saveType = saveType;
	}



	public ActivityData getActivityData() {
		return activityData;
	}



	public void setActivityData(ActivityData activityData) {
		this.activityData = activityData;
	}



	public int getSaveType() {
		return saveType;
	}

	public void setSaveType(int saveType) {
		this.saveType = saveType;
	}


	public Map<Long, ActivityGiftBag> getGiftBagMap() {
		return giftBagMap;
	}


	public void setGiftBagMap(Map<Long, ActivityGiftBag> giftBagMap) {
		this.giftBagMap = giftBagMap;
	}
	
	
}
