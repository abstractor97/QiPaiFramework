/**
 * 
 */
package com.yaowan.server.game.model.data.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yaowan.ServerConfig;
import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.annotation.Transient;
import com.yaowan.framework.database.orm.UpdateProperty;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;



/**
 * 机器人信息表
 * @author zane
 *
 */
/**
 * @author G_T_C
 */
@Table(name = "npc", comment="机器人表")
@Index(names = {"open_id","nick"}, indexs = {"open_id","nick"})
public class Npc extends UpdateProperty{


	public Npc(){
		this.sex = 0;
		this.level= 1;
		this.createTime = TimeUtil.time();

		this.lastLoginTime = this.createTime;
		this.lastOfflineTime = this.createTime;
		this.online=(byte)1;
		this.serverId = ServerConfig.serverId;
		this.goldPot = 0;
		this.status = 3;

	}
	
	
	
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "玩家id")
	private long rid;
	
	@Column(comment = "账号")
	private String openId;
	
	@Column(comment = "玩家名")
	private String nick;
	
	@Column(comment = "性别 -1 未设置，0男 1女")
	private byte sex;
	
	@Column(comment = "头像")
	private byte head;
		
	@Column(comment = "等级")
	private short level;
	
	@Column(comment = "经验")
	private int exp;
	
	@Column(comment = "储钱罐 存储金币")
	private int goldPot;
	
	@Column(comment = "金币 游戏货币")
	private int gold;
	
	
	@Column(comment = "钻石 充值货币 ")
	private int diamond;
	
	@Column(comment = "水晶 比赛货币")
	private int crystal; 
	
	@Column(comment = "省 0 未设置")
	private short province;
	
	@Column(comment = "市 0 未设置")
	private short city;
	
	@Column(comment = "在线")
	private byte online;
	
	@Column(comment = "服务器ID")
	private int serverId;
	
	/**
	 * 创建时间
	 */
	@Column(comment = "创建时间")
	private int createTime;
	
	/**
	 * vip等级
	 */
	@Column(comment = "vip等级")
	private short vipLevel;
	
	@Transient
	private int limitTime;
	
	
	/**
	 * 最后登录时间
	 */
	@Column(comment = "最后登录时间")
	private int lastLoginTime;
	/**
	 * 最后离线时间
	 */
	@Column(comment = "最后离线时间")
	private int lastOfflineTime;
	/**
	 * 玩家数据0点刷新时间，用于每日重置
	 */
	@Column(comment = "玩家数据0点刷新时间，用于每日重置")
	private int zeroRefreshTime;
	/**
	 * 当日在线时长
	 */
	@Column(comment = "当日在线时长")
	private int onlineTime;
	/**
	 * 上次结算在线时长的时间
	 */
	@Column(comment = "上次结算在线时长的时间")
	private int computeOnlineTime;
	/**
	 * 登录次数
	 */
	@Column(comment = "登录次数")
	private int loginCount;
	
	/**
	 * 每日登录次数
	 */
	@Column(comment = "每日登录次数")
	private int dailyLogin;
	
	
	/**
	 * 角色
	 */
	@Column(comment = "角色 格式:avatarid_解锁_使用|")
	private String avatar;
	
	/**
	 * 角色
	 */
	@Transient
	private Map<Integer,List<Integer>> avatarMap;
	
	
	
	/**
	 * 上场比赛遇到的对手
	 */
	@Transient
	private final Set<Long> lastGameRids = new HashSet<>();
	
	/**
	 * 最近参加的游戏房间列表
	 */
	@Transient
	private final List<Long> latelyGames = new ArrayList<>();
	/**
	 * 生涯胜场数
	 */
	@Column(comment = "生涯胜场数")
	private int winTotal;
	
	/**
	 * 一周胜场数
	 */
	@Column(comment = "一周胜场数")
	private int winWeek;
	/**
	 * 生涯总场数
	 */
	@Column(comment = "生涯总场数")
	private int countTotal;
	/**
	 * 一周总场数
	 */
	@Column(comment = "一周总场数")
	private int countWeek;
	
	/**
	 * 最近参加房间类型
	 */
	@Transient
	private int lastRoomType;
	
	/**
	 * 最近参加游戏类型
	 */
	@Transient
	private int lastGameType;
	
	@Column(comment = "正在玩的游戏房间")
	private long roomId;

	@Column(comment = "昭通斗地主积分")
	private int ztDoudizhuScore;
	
	/**
	 * (comment = "ai 状态：0待准备，1游戏中 2空闲 3等待上线 ")
	 */
	@Column(comment = "ai 状态：0待准备，1游戏中 2空闲 3等待上线 ")
	private int status;//AI的状态 0待准备，1游戏中
	
	@Column(comment = "盈亏")
	private int gainOrLoss;
	
	@Column(comment = "ai开关，1启用  0为禁止")
	private int  isOpen;
	
	@Column(comment = "ai调度开始时间")
	private int  doStartTime;
	
	@Column(comment = "ai调度结束时间")
	private int  doEndTime;
	
	@Column(comment = "ai调度开始日期")
	private int  doStartDate;
	
	@Column(comment = "ai调度结束日期")
	private int  doEndDate;
	
	@Column(comment = "游戏类型")
	private int gameType;
	
	@Column(comment = "房间类型")
	private int roomType;
	
	
	public int getDiamond() {
		return diamond;
	}
	public void setDiamond(int diamond) {
		if(diamond<0){
			diamond = 0;
		}
		this.diamond = diamond;
	}
	public long getRid() {
		return rid;
	}
	public byte getSex() {
		return sex;
	}
	public void setSex(byte sex) {
		this.sex = sex;
	}
	public void setRid(long rid) {
		this.rid = rid;
	}
	public int getCrystal() {
		return crystal;
	}
	public void setCrystal(int crystal) {
		this.crystal = crystal;
	}
	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public short getLevel() {
		return level;
	}
	public void setLevel(short level) {
		this.level = level;
	}
	public int getExp() {
		return exp;
	}
	public void setExp(int exp) {
		this.exp = exp;
	}
	public int getGoldPot() {
		return goldPot;
	}
	public void setGoldPot(int goldPot) {
		this.goldPot = goldPot;
	}
	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}
	public byte getHead() {
		return head;
	}
	public void setHead(byte head) {
		this.head = head;
	}
	public short getProvince() {
		return province;
	}

	public void setProvince(short province) {
		this.province = province;
	}
	public short getCity() {
		return city;
	}
	public int getLastGameType() {
		return lastGameType;
	}
	public void setLastGameType(int lastGameType) {
		this.lastGameType = lastGameType;
	}
	public int getLastRoomType() {
		return lastRoomType;
	}
	public void setLastRoomType(int lastRoomType) {
		this.lastRoomType = lastRoomType;
	}
	public void setCity(short city) {
		this.city = city;
	}
	public byte getOnline() {
		return online;
	}
	public long getRoomId() {
		return roomId;
	}
	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}
	public void setOnline(byte online) {
		this.online = online;
	}

	public Map<Integer, List<Integer>> getAvatarMap() {
		if (avatarMap == null) {
			avatarMap = StringUtil.stringToMapList(avatar, "|", "_",
					Integer.class, Integer.class);
		}
		return avatarMap;
	}
	public void setAvatarMap() {
		this.avatar = StringUtil.mapListToString(avatarMap, "|", "_");
	}

	public int getCreateTime() {
		return createTime;
	}


	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}
	public short getVipLevel() {
		return vipLevel;
	}
	public void setVipLevel(short vipLevel) {
		this.vipLevel = vipLevel;
	}
	public int getLastLoginTime() {
		return lastLoginTime;
	}


	public void setLastLoginTime(int lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}
	public int getLastOfflineTime() {
		return lastOfflineTime;
	}
	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public void setLastOfflineTime(int lastOfflineTime) {
		this.lastOfflineTime = lastOfflineTime;
	}
	public int getZeroRefreshTime() {
		return zeroRefreshTime;
	}
	public void setZeroRefreshTime(int zeroRefreshTime) {
		this.zeroRefreshTime = zeroRefreshTime;
	}
	public int getOnlineTime() {
		return onlineTime;
	}
	public void setOnlineTime(int onlineTime) {
		this.onlineTime = onlineTime;
	}
	public int getComputeOnlineTime() {
		return computeOnlineTime;
	}
	public void setComputeOnlineTime(int computeOnlineTime) {
		this.computeOnlineTime = computeOnlineTime;
	}

	public int getLoginCount() {
		return loginCount;
	}
	public void setLoginCount(int loginCount) {
		this.loginCount = loginCount;
	}
	public int getDailyLogin() {
		return dailyLogin;
	}
	public void setDailyLogin(int dailyLogin) {
		this.dailyLogin = dailyLogin;
	}

	public Set<Long> getLastGameRids() {
		return lastGameRids;
	}
	public List<Long> getLatelyGames() {
		return latelyGames;
	}
	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
	public int getZtDoudizhuScore() {
		return ztDoudizhuScore;
	}
	public void setZtDoudizhuScore(int ztDoudizhuScore) {
		this.ztDoudizhuScore = ztDoudizhuScore;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getGainOrLoss() {
		return gainOrLoss;
	}
	public void setAvatarMap(Map<Integer, List<Integer>> avatarMap) {
		this.avatarMap = avatarMap;
	}
	public void setGainOrLoss(int gainOrLoss) {
		this.gainOrLoss = gainOrLoss;
	}
	public int getWinTotal() {
		return winTotal;
	}
	public void setWinTotal(int winTotal) {
		this.winTotal = winTotal;
	}
	public int getWinWeek() {
		return winWeek;
	}
	public void setWinWeek(int winWeek) {
		this.winWeek = winWeek;
	}
	public int getCountTotal() {
		return countTotal;
	}
	public void setCountTotal(int countTotal) {
		this.countTotal = countTotal;
	}
	public int getCountWeek() {
		return countWeek;
	}
	public void setCountWeek(int countWeek) {
		this.countWeek = countWeek;
	}

	public int getIsOpen() {
		return isOpen;
	}
	public int getDoStartTime() {
		return doStartTime;
	}
	public int getDoEndTime() {
		return doEndTime;
	}
	public void setIsOpen(int isOpen) {
		this.isOpen = isOpen;
	}
	public void setDoStartTime(int doStartTime) {
		this.doStartTime = doStartTime;
	}
	public void setDoEndTime(int doEndTime) {
		this.doEndTime = doEndTime;
	}
/*	public String getGoldInterval() {
		return goldInterval;
	}
	public void setGoldInterval(String goldInterval) {
		this.goldInterval = goldInterval;
	}*/
	public int getGameType() {
		return gameType;
	}
	public int getRoomType() {
		return roomType;
	}
	public void setGameType(int gameType) {
		this.gameType = gameType;
	}
	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}
	public int getDoStartDate() {
		return doStartDate;
	}
	public int getDoEndDate() {
		return doEndDate;
	}
	public void setDoStartDate(int doStartDate) {
		this.doStartDate = doStartDate;
	}
	public void setDoEndDate(int doEndDate) {
		this.doEndDate = doEndDate;
	}
	
	public int getLimitTime() {
		return limitTime;
	}
	
	public void setLimitTime(int limitTime) {
		this.limitTime = limitTime;
	}
	
	
}
