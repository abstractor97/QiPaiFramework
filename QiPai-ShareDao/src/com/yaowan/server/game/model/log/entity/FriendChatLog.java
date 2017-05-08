package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;

@Table(name = "friend_chat_log", comment = "好友聊天 ")
public class FriendChatLog {

	@Id(strategy = Strategy.AUTO)
	@Column(comment = "聊天流水id")
	private long id;
	
	@Column(comment = "玩家id")
	private long rid;

	@Column(comment = "玩家1id")
	private long senderId;

	@Column(comment = "玩家2id")
	private long getterId;
	
	@Column(comment = "文字记录")
	private String textLog;
	
	@Column(comment = "语音记录，目前不做")
	private String voiceLog;
	
	@Column(comment = "标志文字还是语音")
	private int type;
	
	@Column(comment = "记录时间")
	private int time;
	
	@Column(comment = "标志已读未读")
	private byte isRead;
	
	public long getRid() {
		return rid;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public long getSenderId() {
		return senderId;
	}

	public void setSenderId(long senderId) {
		this.senderId = senderId;
	}

	public long getGetterId() {
		return getterId;
	}

	public void setGetterId(long getterId) {
		this.getterId = getterId;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTextLog() {
		return textLog;
	}

	public void setTextLog(String textLog) {
		this.textLog = textLog;
	}

	public String getVoiceLog() {
		return voiceLog;
	}

	public void setVoiceLog(String voiceLog) {
		this.voiceLog = voiceLog;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public byte getIsRead() {
		return isRead;
	}

	public void setIsRead(byte isRead) {
		this.isRead = isRead;
	}
}
