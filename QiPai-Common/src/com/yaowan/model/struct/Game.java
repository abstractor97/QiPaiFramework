/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.model.struct;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.server.game.model.data.entity.Role;

/**
 * 游戏房间抽象
 *
 * @author zane
 *
 */
public class Game implements ISingleData {
	
	
	
	public Game(){
    	
    }
	
	public Game(long id, int realType) {
		this.roomId = id;
		this.roomType = realType % 10000000;
		this.gameType = (realType - roomType) / 10000000;
		this.realType = realType;
		this.startTime = System.currentTimeMillis();
	}

    private int realType;//桌子类型
    
    private long roomId;//桌号
    
    private int gameType;//游戏类型
    
    private int roomType;//房间类型
    
    private int count;//游戏次数
    
    private boolean canDelete = false;// 能否解散标志
    
    private long groupId = 0;// 编组ID（目前只有牛牛用到）
    
    /**
     * 0-未初始化
     * 1-准备前
     * 2-进行
     * 3-结束
     * 4-已经结算
     * 5-超时结束
     * 6-房间解散
     */
    private int status;//状态
    
    private long startTime;//开始时间
    
    private long endTime;//结束时间
    
    private int needCount;//还需要多少玩家
    
    private long lastRobotCreate;//机器人生产间隔
    
    private boolean isFriendRoom;//是否好友房
    
    //是否正有人退出游戏
    private volatile boolean quiting = false;
    
    /**
     * 顺序加入 没人为0
     */
    private List<Long> roles = new CopyOnWriteArrayList<Long>();//参与玩家
    
    /**
	 * 精灵
	 */
	private final ConcurrentMap<Long, GameRole> spriteMap = new ConcurrentHashMap<>(4);
   
	
    public boolean isQuiting() {
		return quiting;
	}

	public void setQuiting(boolean quiting) {
		this.quiting = quiting;
	}

	@Override
    public Number getSingleId() {
        return roomId;
    }

	public int getRealType() {
		return realType;
	}

	public void setRealType(int realType) {
		this.realType = realType;
	}

	public long getRoomId() {
		return roomId;
	}

	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}



	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public int getGameType() {
		return gameType;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public int getRoomType() {
		return roomType;
	}

	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}

	public int getNeedCount() {
		return needCount;
	}

	public void setNeedCount(int needCount) {
		this.needCount = needCount;
	}

	public Map<Long, GameRole> getSpriteMap() {
		return spriteMap;
	}

	public void setRoles(List<Long> roles) {
		this.roles = roles;
	}

	public List<Long> getRoles() {
		return roles;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getFriendRoomBaseChip() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getLastRobotCreate() {
		return lastRobotCreate;
	}

	public void setLastRobotCreate(long lastRobotCreate) {
		this.lastRobotCreate = lastRobotCreate;
	}

	public boolean isFriendRoom() {
		return isFriendRoom;
	}

	public void setFriendRoom(boolean isFriendRoom) {
		this.isFriendRoom = isFriendRoom;
	}
	
	public boolean canDelete() {
		return canDelete;
	}

	public void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
	}
	
	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}
	
	@Override
	public String toString() {
		return "Game [realType=" + realType + ", roomId=" + roomId
				+ ", gameType=" + gameType + ", roomType=" + roomType
				+ ", count=" + count + ", status=" + status + ", startTime="
				+ startTime + ", endTime=" + endTime + ", needCount="
				+ needCount + ", lastRobotCreate=" + lastRobotCreate
				+ ", roles=" + roles + ", spriteMap=" + spriteMap + "]";
	}
	
	public GameRole findEnterSeat(Role role) {
		// 初始化桌对象 查询空座位坐下
		GameRole gameRole = new GameRole(role, this.getRoomId());
		if (role.getPlatform() == null) {
			gameRole.setRobot(true);
			gameRole.setAuto(true);
			gameRole.setAICount(0);
		}
		int fitseat = 0;
		int index = 0;
		for (Long rid : this.getRoles()) {
			index++;
			if (rid == 0) {
				fitseat = index;
				break;
			}
		}
		if (fitseat == 0) {
			fitseat = this.getRoles().size() + 1;
		}
		gameRole.setSeat(fitseat);
		if (fitseat > this.getRoles().size()) {
			this.getRoles().add(role.getRid());
		} else {
			this.getRoles().set(fitseat - 1, role.getRid());

		}
		this.getSpriteMap().put(role.getRid(), gameRole);
		return gameRole;
	}

}
