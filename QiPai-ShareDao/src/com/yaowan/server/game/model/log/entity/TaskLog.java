package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Table;

/**
 * 任务日志
 * @author G_T_C
 */
@Table(name = "task_log", comment = "任务日志")
public class TaskLog {
	
	
	
	public TaskLog(long rid, int taskId, byte type, int time, byte status) {
		super();
		this.rid = rid;
		this.taskId = taskId;
		this.type = type;
		this.time = time;
		this.status = status;
	}

	@Column(comment = "玩家rid")
	private long rid;
	
	@Column(comment = "任务Id")
	private int taskId;
	
	@Column(comment = "任务类型:1为主线任务，2为日常任务")
	private byte type;
	
	@Column(comment = "时间")
	private int time;
	
	@Column(comment = "状态：0接受任务，1完成任务，2领取任务")
	private byte status;

	public long getRid() {
		return rid;
	}

	public int getTaskId() {
		return taskId;
	}

	public byte getType() {
		return type;
	}

	public int getTime() {
		return time;
	}

	public byte getStatus() {
		return status;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public void setStatus(byte status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "TaskLog [rid=" + rid + ", taskId=" + taskId + ", type=" + type
				+ ", time=" + time + ", status=" + status + "]";
	}



}
