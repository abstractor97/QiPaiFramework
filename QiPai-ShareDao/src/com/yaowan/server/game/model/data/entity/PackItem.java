/**
 * 
 */
package com.yaowan.server.game.model.data.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;



/**
 * @author zane
 *
 */
@Table(name = "pack_item", comment = "背包数据")
@Index(names = {"rid"}, indexs = {"rid"})
public class PackItem {
	
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "背包id")
	private long id;
	
	@Column(comment = "玩家id")
	private long rid;
	
	@Column(comment = "道具id")
	private int itemId;
	
	@Column(comment = "过期时间")
	private int expire;
	
	@Column(comment = "道具数量")
	private int count;
	
	@Column(comment = "背包子类")
	private byte sort;

	public byte getSort() {
		return sort;
	}

	public void setSort(byte sort) {
		this.sort = sort;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getRid() {
		return rid;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getExpire() {
		return expire;
	}

	public void setExpire(int expire) {
		this.expire = expire;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	
	
}
