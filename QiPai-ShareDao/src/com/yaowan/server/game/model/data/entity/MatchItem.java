package com.yaowan.server.game.model.data.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.orm.UpdateProperty;
 
/**
 * 比赛奖品记录
 *
 */
@Table(name = "match_item", comment = "比赛奖品记录")
@Index(names = {"rid"}, indexs = {"rid"})
public class MatchItem extends UpdateProperty{

		@Id(strategy = Strategy.AUTO)
		@Column(comment = "流水id")
		private long id;
		
		@Column(comment = "玩家id")
		private long rid;
		
		@Column(comment = "道具id")
		private int itemId;
		
		@Column(comment = "道具数量")
		private int count;
		
		@Column(comment = "过期时间")
		private int expire;
		
		@Column(comment = "状态(0未领取 1审核中 2发送 3重新领取 4已领取)")
		private byte state;
		
		@Column(comment = "手机")
		private int phone;
		
		@Column(comment = "地址")
		private String addr;
		
		@Column(comment = "http地址")
		private String rewardAddr;

		public long getRid() {
			return rid;
		}

		public void setRid(long rid) {
			this.rid = rid;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public int getItemId() {
			return itemId;
		}

		public void setItemId(int itemId) {
			this.itemId = itemId;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		public int getExpire() {
			return expire;
		}

		public void setExpire(int expire) {
			this.expire = expire;
		}

		public byte getState() {
			return state;
		}

		public void setState(byte state) {
			this.state = state;
		}

		public int getPhone() {
			return phone;
		}

		public void setPhone(int phone) {
			this.phone = phone;
		}

		public String getAddr() {
			return addr;
		}

		public void setAddr(String addr) {
			this.addr = addr;
		}

		public String getRewardAddr() {
			return rewardAddr;
		}

		public void setRewardAddr(String rewardAddr) {
			this.rewardAddr = rewardAddr;
		}



		
	
}
