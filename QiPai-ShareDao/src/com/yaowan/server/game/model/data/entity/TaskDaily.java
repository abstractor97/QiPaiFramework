package com.yaowan.server.game.model.data.entity;

import java.io.Serializable;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.annotation.Transient;
import com.yaowan.framework.database.orm.UpdateProperty;
import com.yaowan.framework.util.TimeUtil;



/**
 * 
 * @author zane 2016年10月12日 下午9:48:04
 */
@Table(name = "task_daily", comment = "每日任务表")
public class TaskDaily extends UpdateProperty implements Serializable, Comparable<TaskDaily>{


	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = -4770578907762994675L;
	
	public TaskDaily(){
		
	}
	
    public TaskDaily(long rid,int taskId, short type){
		this.rid = rid;
		this.taskId = taskId;
		this.ymd = TimeUtil.getTodayYmd();
		this.type = type;
	}

	/**
	 * 角色ID
	 */
	@Id(sort = 1)
	@Column(comment = "角色ID")
	private long rid;
	
	
	/**
	 * 日期yyyymmdd
	 */
	@Id(sort = 2)
	@Column(comment = "日期yyyymmdd")
	private int ymd;
	
	/**
	 * 任务编号
	 */
	@Id(sort = 3)
	@Column(comment = "任务配表ID")
	private int taskId;

	/**
	 * 日常任务类型
	 */
	
	@Column( comment = "任务类型:1为主线任务，2为日常任务")
	private short type;
	
	
	/**
	 * 是否领奖0未领奖 1可领奖 2已经领奖
	 */
	@Column(comment = "是否领奖0未领奖 1可领奖 2已经领奖")
	private byte reward;
	
	/**
	 * 任务进度
	 * 未完成 0
	 * 完成 1
	 */
	@Column(comment = "任务进度 ：未完成 0 ，大于0完成次数")
	private int process;


	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	
	
	public long getRid() {
		return rid;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public int getYmd() {
		return ymd;
	}

	public void setYmd(int ymd) {
		this.ymd = ymd;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public byte getReward() {
		return reward;
	}

	public void setReward(byte reward) {
		this.reward = reward;
	}

	public int getProcess() {
		return process;
	}

	public void setProcess(int process) {
		this.process = process;
	}

	@Override
	public int compareTo(TaskDaily o) {
		
		if (this.getType() < o.getType()) {
			return 1;
		} else if (this.getType() == o.getType()) {
			return 0;
		} else {
			return -1;
		}
	}
	
}

