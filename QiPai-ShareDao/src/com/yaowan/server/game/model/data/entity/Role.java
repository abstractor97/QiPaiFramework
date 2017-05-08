/**
 * 
 */
package com.yaowan.server.game.model.data.entity;

import java.io.Serializable;
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
 * 玩家信息表
 * @author zane
 *
 */
@Table(name = "role", comment="玩家表")
@Index(names = {"open_id","nick"}, indexs = {"open_id","nick"})
public class Role extends UpdateProperty implements Serializable{
	private static final long serialVersionUID = 8910144872675709094L;

	public Role(){
		this.sex = 0;
		this.level= 1;
		this.createTime = TimeUtil.time();
		this.screen ="";
		this.hasChargeInfo = "";
		this.reportInfo = "";
		this.favorGames = "";
		this.mailRead = "";
		this.mailReceive = "";
		this.lastLoginTime = this.createTime;
		this.lastOfflineTime = this.createTime;
		this.online=(byte)1;
		this.serverId = ServerConfig.serverId;
		this.lastLoginIp = "";
		this.signRewardGet="";
		this.agent = "";
		this.goldPot = 0;
		this.pushId = "";
		this.lastWeekMoney = 0;
		this.bureauCount = 0;
		this.voteInfo = "";
		this.u8id = 0;
		this.allMoney = 0;
		this.code = "";
		this.isRecommend = (byte)0;
	}
	
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "玩家id")
	private long rid;
	
	@Column(comment = "账号")
	private String openId;
	
	@Column(comment = "U8平台的userId")
	private int u8id = 0;
	
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
	
	@Column(comment = "破产 当日救助金次数")
	private int goldResuceTimes;
	
	@Column(comment = "破产 下次领取救助金的时间")
	private int goldResuceNextTime;
	
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
	
	@Column(comment = "玩家充值的总金额")
	private int allMoney;
	
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
	
	/**
	 * vip过期时间
	 */
	@Column(comment = "vip过期时间")
	private int vipTime;
	
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
	 * 禁言结束时间
	 */
	@Column(comment = "禁言结束时间")
	private int shutUp;
	/**
	 * 封号结束时间
	 */
	@Column(comment = "封号结束时间")
	private int forbid;
	
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
	 * 设备码
	 */
	@Column(comment = "设备码")
	private String imei;
	
	/**
	 * 手机号
	 */
	@Column(comment = "手机号")
	private int phone;
	
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
	 * 本周剩余举报次数
	 */
	@Column(comment = "本周剩余举报次数")
	private int requireCount;
	
	/**
	 * 每日破产领取次数
	 */
	@Column(comment = "每日破产领取次数")
	private int brokenCount;
	
	/**
	 * 已充值活动id|
	 */
	@Column(comment = "已充值活动 格式:充值id|")
	private String hasChargeInfo;
	

	
	/**
	 * 屏幕分辨率
	 */
	@Column(comment = "屏幕分辨率")
	private String screen;
	
	/**
	 * sdk渠道
	 */
	@Column(comment = "sdk渠道")
	private String platform;
	
	/**
	 * 客户端类型
	 * 1，邵通客户端
	 */
	@Column(comment = "客户端类型")
	private int appType;
	
	/**
	 * 举报信息|
	 */
	@Column(comment = "举报信息 格式 玩家id_举报类型|", length=600)
	private String reportInfo;
	
	/**
	 * 举报信息|
	 */
	@Transient
	private Map<Integer,Integer> reportInfoMap;
	
	/**
	 * 
	 */
	@Column(comment = "最近三个游戏 格式:游戏id_次数|", length=100)
	private String favorGames;
	
	
	/**
	 * 
	 */
	@Transient
	private Map<Integer,Integer> favorGamesMap;
	
	
	/**
	 * 上场比赛遇到的对手
	 */
	@Transient
	private final Set<Long> lastGameRids = new HashSet<>();
	
	/**
	 * 最近参加的游戏房间列表
	 */
	@Transient
	private Long latelyGames = 0l;
	/**
	 * 最近参加游戏类型
	 */
	@Transient
	private int lastGameType;
	
	/**
	 * 最近参加房间类型
	 */
	@Transient
	private int lastRoomType;
	
	@Column(comment = "正在玩的游戏房间")
	private long roomId;
	
	@Column(comment = "群发系统邮件已读编号")
	private String mailRead;
	
	@Column(comment = "群发系统邮件已领取编号")
	private String mailReceive;
	
	@Column(comment = "游戏开始时是否弹出邮件,0否1是")
	private int seeMail;
	
	@Column(comment = "昭通斗地主积分")
	private int ztDoudizhuScore;
	
	/**
	 * 
	 */
	@Column(comment = "是否改过昵称")
	private byte hasNick;
	
	@Column(comment = "最大连续签到次数")
	private int maxContinueSign;
	
	@Transient 
	private List<Integer> signRecordList;
	
	@Column(comment = "签到记录 |分割", length = 500)
	private String signRecord;

	@Transient 
	private List<Integer> signRewardGetList;
	
	@Column(comment = "已经获得的奖励 |分割")
	private String signRewardGet;
	
	
	/**
	 * 上次登录IP
	 */
	@Column(comment = "ip")
	private String lastLoginIp;
	
	@Column(comment = "推广标志")
	private String agent;
	
	@Column(comment = "离线推送开关")
	private byte pushSwitch;
	
	@Column(comment = "推送ID")
	private String pushId;
	
	@Column(comment = "上周的总金额")
	private int lastWeekMoney;
	
	@Column(comment = "一周的活跃次数")
	private int bureauCount;
	
	@Column(comment = "投票信息：投票/点击某活动id_投票、点击结果：0未投票。1,2,3...代表A,B,C...;如果是点击，0为未点击，1为点击")
	private String voteInfo;
	
	@Transient
	private Map<Long, Integer> voteInfoMap;
	
	@Column(comment = "设备类型， 1为安卓， 2为ios, 其它平台为0")
	private byte deviceType;
	
	@Column(comment = "登录方式 1 游客， 2手机，3微信 ，4 qq")
	private byte loginType; 
	
	@Column(comment = "登录是微信的时候：头像地址")
	private String headimgurl = "";
	
	@Column(comment = " 推送设备token")
	private String deviceToken = "";
	
	@Column(comment = "推荐码")
	private String code;
	
	@Column(comment = "推荐标志 0未推广 1已推广")
	private byte isRecommend; 

	

	/**
	 * 当局比赛奖券任务id，超过每天完成次数，就为-1, 如果没有玩过游戏，就为0
	 */
	@Transient
	private int lotteryTaskId = 0;
	
	public String getSignRewardGet() {
		if (signRewardGet == null) {
			signRewardGet = "";
		}
		return signRewardGet;
	}
	public void setSignRewardGet(String signRewardGet) {
		this.signRewardGet = signRewardGet;
	}
	public List<Integer> getSignRewardGetList() {
		if(signRewardGetList==null){
			signRewardGetList=StringUtil.stringToList(signRewardGet, "|", Integer.class);
		}
		return signRewardGetList;
	}
	public void setSignRewardGetList() {
		this.signRewardGet =StringUtil.listToString(signRewardGetList,"|");
	}
	public int getMaxContinueSign() {
		return maxContinueSign;
	}
	public void setMaxContinueSign(int maxContinueSign) {
		this.maxContinueSign = maxContinueSign;
	}

	public List<Integer> getSignRecordList() {
		if (signRecordList == null) {
			signRecordList = StringUtil.stringToList(signRecord, "|",
					Integer.class);
		}
		return signRecordList;
	}
	
	public void setSignRecordList() {
		//存储数据结构
		this.signRecord=StringUtil.listToString(signRecordList, "|");
	}
	public String getLastLoginIp() {
		return lastLoginIp;
	}
	public void setLastLoginIp(String lastLoginIp) {
		this.lastLoginIp = lastLoginIp;
	}
	public int getGoldResuceTimes() {
		return goldResuceTimes;
	}
	public void setGoldResuceTimes(int goldResuceTimes) {
		this.goldResuceTimes = goldResuceTimes;
	}
	public int getGoldResuceNextTime() {
		return goldResuceNextTime;
	}
	public void setGoldResuceNextTime(int goldResuceNextTime) {
		this.goldResuceNextTime = goldResuceNextTime;
	}
	public String getMailRead() {
		return mailRead;
	}
	public void setMailRead(String mailRead) {
		this.mailRead = mailRead;
	}
	public String getMailReceive() {
		return mailReceive;
	}
	public void setMailReceive(String mailReceive) {
		this.mailReceive = mailReceive;
	}
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
	
	public int getU8id() {
		return u8id;
	}
	public void setU8id(int u8id) {
		this.u8id = u8id;
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
	public int getVipTime() {
		return vipTime;
	}
	public void setVipTime(int vipTime) {
		this.vipTime = vipTime;
	}
	public String getAgent() {
		return agent;
	}
	public void setAgent(String agent) {
		this.agent = agent;
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
	public int getPhone() {
		return phone;
	}
	public void setPhone(int phone) {
		this.phone = phone;
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
	public int getSeeMail() {
		return seeMail;
	}
	public void setSeeMail(int seeMail) {
		this.seeMail = seeMail;
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
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public int getCreateTime() {
		return createTime;
	}

	public int getAppType() {
		return appType;
	}
	public void setAppType(int appType) {
		this.appType = appType;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public String getScreen() {
		return screen;
	}
	public void setScreen(String screen) {
		this.screen = screen;
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
	public byte getHasNick() {
		return hasNick;
	}
	public void setHasNick(byte hasNick) {
		this.hasNick = hasNick;
	}
	public int getRequireCount() {
		return requireCount;
	}
	public void setRequireCount(int requireCount) {
		this.requireCount = requireCount;
	}
	public int getBrokenCount() {
		return brokenCount;
	}
	public void setBrokenCount(int brokenCount) {
		this.brokenCount = brokenCount;
	}
	public String getHasChargeInfo() {
		return hasChargeInfo;
	}
	public void setHasChargeInfo(String hasChargeInfo) {
		this.hasChargeInfo = hasChargeInfo;
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

	public String getSignRecord() {
		if (signRecord == null) {
			signRecord = "";
		}
		return signRecord;
	}
	public void setSignRecord(String signRecord) {
		this.signRecord = signRecord;
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
	public int getShutUp() {
		return shutUp;
	}
	public void setShutUp(int shutUp) {
		this.shutUp = shutUp;
	}
	public int getForbid() {
		return forbid;
	}
	public void setForbid(int forbid) {
		this.forbid = forbid;
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
	
	public String getReportInfo() {
		return reportInfo;
	}
	public void setReportInfo(String reportInfo) {
		this.reportInfo = reportInfo;
	}
	public Map<Integer, Integer> getReportInfoMap() {
		if (reportInfoMap == null) {
			reportInfoMap = StringUtil.stringToMap(reportInfo, "|", "_",
					Integer.class, Integer.class);
		}
		return reportInfoMap;
	}
	public void setReportInfoMap() {
		this.reportInfo = StringUtil.mapToString(reportInfoMap);
	}
	
	public Map<Integer, Integer> getFavorGamesMap() {
		if (favorGamesMap == null) {
			favorGamesMap = StringUtil.stringToMap(favorGames, "|", "_",
					Integer.class, Integer.class);
		}
		return favorGamesMap;
	}
	public void setFavorGamesMap() {
		this.favorGames = StringUtil.mapToString(favorGamesMap);
	}
	public String getFavorGames() {
		return favorGames;
	}
	public void setFavorGames(String favorGames) {
		this.favorGames = favorGames;
	}
	public Set<Long> getLastGameRids() {
		return lastGameRids;
	}
	public Long getLatelyGames() {
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
	public byte getPushSwitch() {
		return pushSwitch;
	}
	public void setPushSwitch(byte pushSwitch) {
		this.pushSwitch = pushSwitch;
	}
	public String getPushId() {
		return pushId;
	}
	public void setPushId(String pushId) {
		this.pushId = pushId;
	}
	public int getLastWeekMoney() {
		return lastWeekMoney;
	}
	public int getBureauCount() {
		return bureauCount;
	}
	public void setLastWeekMoney(int lastWeekMoney) {
		this.lastWeekMoney = lastWeekMoney;
	}
	public void setBureauCount(int bureauCount) {
		this.bureauCount = bureauCount;
	}
	public String getVoteInfo() {
		return voteInfo;
	}
	public Map<Long, Integer> getVoteInfoMap() {
		if(voteInfoMap == null){
			voteInfoMap = StringUtil.stringToMap(voteInfo, StringUtil.DELIMITER_BETWEEN_ITEMS, StringUtil.DELIMITER_INNER_ITEM,Long.class, Integer.class);
		}
		return voteInfoMap;
	}
	public void setVoteInfo(String voteInfo) {
		this.voteInfo = voteInfo;
	}
	public int getLotteryTaskId() {
		return lotteryTaskId;
	}
	public void setLotteryTaskId(int lotteryTaskId) {
		this.lotteryTaskId = lotteryTaskId;
	}
	public void setLatelyGames(Long latelyGames) {
		this.latelyGames = latelyGames;
	}
	public byte getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(byte deviceType) {
		this.deviceType = deviceType;
	}
	public byte getLoginType() {
		return loginType;
	}
	public void setLoginType(byte loginType) {
		this.loginType = loginType;
	}
	public String getHeadimgurl() {
		return headimgurl;
	}
	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}
	public String getDeviceToken() {
		return deviceToken;
	}
	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}
	public int getAllMoney() {
		return allMoney;
	}
	public void setAllMoney(int allMoney) {
		this.allMoney = allMoney;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public byte getIsRecommend() {
		return isRecommend;
	}
	public void setIsRecommend(byte isRecommend) {
		this.isRecommend = isRecommend;
	}
    
	
	
	
}
