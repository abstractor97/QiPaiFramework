package com.yaowan.server.game.model.struct;

import java.util.List;

/**
 * 红包领取的信息类
 * 
 * @author G_T_C
 */
public class RedBagInfo{

	private int stime;

	private int etime;

	private int limit;

	private int weightCount;
	
	private int hasSend = 0;//0未发送，1已经发送， 2,结束

	private List<RewardInfo> infos;

	public int getStime() {
		return stime;
	}

	public int getEtime() {
		return etime;
	}

	public int getLimit() {
		return limit;
	}

	public void setStime(int stime) {
		this.stime = stime;
	}

	public void setEtime(int etime) {
		this.etime = etime;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public List<RewardInfo> getInfos() {
		return infos;
	}

	public void setInfos(List<RewardInfo> infos) {
		this.infos = infos;
	}

	public int getWeightCount() {
		return weightCount;
	}

	public void setWeightCount(int weightCount) {
		this.weightCount = weightCount;
	}

	public int getHasSend() {
		return hasSend;
	}

	public void setHasSend(int hasSend) {
		this.hasSend = hasSend;
	}
	
	
	/**
	 * 
	 * @author G_T_C
	 */
	public class RewardInfo {

		private int minNum;// 随机数最低值

		private int maxNum;

		private int weight;

		private int type;//1金币，2钻石，3奖券

		public int getMinNum() {
			return minNum;
		}

		public int getMaxNum() {
			return maxNum;
		}

		public int getWeight() {
			return weight;
		}

		public int getType() {
			return type;
		}

		public void setMinNum(int minNum) {
			this.minNum = minNum;
		}

		public void setMaxNum(int maxNum) {
			this.maxNum = maxNum;
		}

		public void setWeight(int weight) {
			this.weight = weight;
		}

		public void setType(int type) {
			this.type = type;
		}
	}
}

