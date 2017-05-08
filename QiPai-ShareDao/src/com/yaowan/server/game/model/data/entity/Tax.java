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
@Table(name = "tax",comment = "邮件")
public class Tax {
	
	@Id(strategy = Strategy.AUTO)
	@Column(comment ="主键id")
	private long id;
	
	@Column(comment ="游戏类型")
	private byte gameType;
	
	@Column(comment ="房间类型")
	private int roomType;
	
	@Column(comment = "抽水比例，万分比")
	private int taxCount;

	public byte getGameType() {
		return gameType;
	}

	public void setGameType(byte gameType) {
		this.gameType = gameType;
	}

	public int getRoomType() {
		return roomType;
	}

	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}

	public int getTaxCount() {
		return taxCount;
	}

	public void setTaxCount(int taxCount) {
		this.taxCount = taxCount;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	

	
}
