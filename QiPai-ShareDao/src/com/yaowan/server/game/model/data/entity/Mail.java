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
@Table(name = "mail",comment = "邮件")
public class Mail {
	
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "邮件id")
	private long id;
	
	@Column(comment = "发送者")
	private long sendId;
	
	@Column(comment = "接收者")
	private long receiveId;
	
	@Column(comment = "过期时间")
	private int expire;
	
	@Column(comment = "发送时间")
	private int sendTime;
	
	@Column(comment = "内容")
	private String content;
	
	@Column(comment = "奖励内容")
	private String reward;
	
	@Column(comment = "1：已读，0：未读")
	private int readed;
	
	@Column(comment = "1：可领取，0：不可领取")
	private int receive;
	
	@Column(comment = "是否推荐，1推荐，0不推荐")
	private int recommend;
	
	@Column(comment = "标题")
	private String title;
	
	@Column(comment = "1回收站内，0没有回收")
	private int recycle;
	
	@Column(comment = "删除用的id")
	private int delId;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getSendId() {
		return sendId;
	}

	public void setSendId(long sendId) {
		this.sendId = sendId;
	}

	public long getReceiveId() {
		return receiveId;
	}

	public void setReceiveId(long receiveId) {
		this.receiveId = receiveId;
	}

	public int getExpire() {
		return expire;
	}

	public void setExpire(int expire) {
		this.expire = expire;
	}

	public int getSendTime() {
		return sendTime;
	}

	public void setSendTime(int sendTime) {
		this.sendTime = sendTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getReward() {
		return reward;
	}

	public void setReward(String reward) {
		this.reward = reward;
	}
 

	public int getReaded() {
		return readed;
	}

	public void setReaded(int readed) {
		this.readed = readed;
	}

	public int getReceive() {
		return receive;
	}

	public void setReceive(int receive) {
		this.receive = receive;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getRecommend() {
		return recommend;
	}

	public void setRecommend(int recommend) {
		this.recommend = recommend;
	}

	public int getRecycle() {
		return recycle;
	}

	public void setRecycle(int recycle) {
		this.recycle = recycle;
	}

	public int getDelId() {
		return delId;
	}

	public void setDelId(int delId) {
		this.delId = delId;
	}

	
}
