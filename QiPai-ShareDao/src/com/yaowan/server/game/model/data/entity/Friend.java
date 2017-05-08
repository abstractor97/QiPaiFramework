/**
 * 
 */
package com.yaowan.server.game.model.data.entity;

import java.util.List;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.annotation.Transient;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;




/**
 * 好友关系
 * @author zane
 *
 */
@Table(name = "friend", comment = "好友关系")
@Index(names = {"rid1","rid2"}, indexs = {"rid1","rid2"})
public class Friend {
	
	public Friend(){
		
	}
	
	public Friend(long rid1,long rid2){
		this.rid1 = rid1;
		this.rid2 = rid2;
		this.createTime = TimeUtil.time();
	}
	
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "流水id")
	private long id;
	
	@Column(comment = "玩家id")
	private long rid1;
	
	@Column(comment = "对方id")
	private long rid2;
	
	@Column( comment = "申请好友时间")
	private int createTime;
	
	@Column(comment = "状态 0申请中 1被申请 2好友  同时成为申请和被申请关系|分割  0|1")
	private String status;
	
	@Transient
	private List<Integer> statusList;	

	public List<Integer> getStatusList() {
		if(statusList==null){
			statusList=StringUtil.stringToList(status, "|", Integer.class);
		}
		return statusList;
	}

	public void setStatusList() {	
		this.status = StringUtil.listToString(statusList,"|");;
	}

	public long getRid1() {
		return rid1;
	}

	public void setRid1(long rid1) {
		this.rid1 = rid1;
	}

	public long getRid2() {
		return rid2;
	}

	public void setRid2(long rid2) {
		this.rid2 = rid2;
	}

	public int getCreateTime() {
		return createTime;
	}

	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	
}
