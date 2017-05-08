/**
 * 
 */
package com.yaowan.server.center.model.data;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.orm.UpdateProperty;



/**
 * @author zane
 *
 */
@Table(name = "role", comment = "游戏服玩家")
public class CenterRole extends UpdateProperty{
	
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "id")
	private long id;

	@Column(comment = "id")
	private String openId;
	
	@Column(comment = "角色")
	private long rid;
	
	@Column(comment = "名称")
	private String nickname;
	
	@Column(comment = "创建")
	private int createTime;
	
	@Column(comment = "端口")
	private int serverId;
	
	
	@Column(comment = "渠道")
	private int loginTime;
	
	
	@Column(comment = "细分渠道")
	private int channel;
	
	@Column(comment = "大渠道")
	private String platform;
	
	@Column(comment = "ip")
	private String ip;
	
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public long getRid() {
		return rid;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getCreateTime() {
		return createTime;
	}

	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}



	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public int getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(int loginTime) {
		this.loginTime = loginTime;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	



	
}
