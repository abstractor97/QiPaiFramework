/**
 * 
 */
package com.yaowan.server.game.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.protobuf.GeneratedMessageLite;
import com.yaowan.constant.GameError;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.constant.MoneyType;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.AvatarCache;
import com.yaowan.csv.cache.ExpCache;
import com.yaowan.csv.cache.PoChanCache;
import com.yaowan.csv.cache.RandomNameCnCache;
import com.yaowan.csv.entity.AvatarCsv;
import com.yaowan.csv.entity.ExpCsv;
import com.yaowan.csv.entity.PoChanCsv;
import com.yaowan.csv.entity.RandomNameCnCsv;
import com.yaowan.framework.netty.Message;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.MathUtil;
import com.yaowan.framework.util.Probability;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GLogin.GMsg_12001003;
import com.yaowan.protobuf.game.GRole.GMsg_12002006;
import com.yaowan.protobuf.game.GRole.GMsg_12002008;
import com.yaowan.protobuf.game.GRole.GRoleAvatar;
import com.yaowan.server.game.model.data.dao.RoleDao;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.dao.MoneyDao;
import com.yaowan.server.game.model.log.dao.RoleOnlineDao;
import com.yaowan.server.game.model.log.dao.RoleRemainMoneyLogDao;
import com.yaowan.server.game.model.log.dao.RoleUpLogDao;
import com.yaowan.server.game.model.log.dao.VoteActivityLogDao;
import com.yaowan.server.game.model.log.entity.Money;
import com.yaowan.server.game.model.log.entity.RoleRemainMoneyLog;
import com.yaowan.server.game.model.log.entity.VoteActivityLog;
import com.yaowan.server.game.model.struct.MissionType;
import com.yaowan.server.game.model.struct.TaskType;

/**
 * @author zane
 *
 */

@Component
public class RoleFunction extends FunctionAdapter {

	@Autowired
	private RoleDao roleDao;

	@Autowired
	private MoneyDao moneyDao;

	@Autowired
	private ExpCache expCache;

	@Autowired
	private AvatarCache avatarCache;

	@Autowired
	private RandomNameCnCache randomNameCnCache;

	@Autowired
	private PoChanCache poChanCache;

	@Autowired
	private RoleOnlineDao roleOnlineDao;

	@Autowired
	private RoleUpLogDao roleUpLogDao;

	@Autowired
	private VoteActivityLogDao voteActivityLogDao;

	@Autowired
	private MissionFunction missionFunction;

	@Autowired
	private RoleRemainMoneyLogDao remainMoneyLogDao;

	/**
	 * 在线玩家
	 */
	private ConcurrentHashMap<Long, Player> playerMap = new ConcurrentHashMap<>();

	/**
	 * 角色缓存
	 */
	private final ConcurrentHashMap<Long, Role> roleCacheMap = new ConcurrentHashMap<>();

	@Override
	public void handleOnServerStart() {
		// 设置所有玩家为下线状态
		roleDao.offlineAll();

		// 加载所有角色信息
		/*
		 * List<Role> roleList = roleDao.findAll(); for (Role role : roleList) {
		 * addCache(role); }
		 */
		LogUtil.info("Load Role size " + roleCacheMap.size());
	}

	@Override
	public void handleOnNextDay() {
		// TODO Auto-generated method stub
		// 每天凌晨进行救济金的更新
		roleDao.deleteGoldResuceLoginCountInfo();
		// 一个月的第一填凌晨,重置所有用户的签到信息
		if (TimeUtil.getDay() == 1) {
			roleDao.deleteSignInfo();
		}
		int time = TimeUtil.yesterdayBreak() + (23 * 60 + 59) * 60 + 59;
		RoleRemainMoneyLog log = roleDao.sumRemain();
		log.setTime(time);
		LogUtil.info("插入当天玩家货币剩余数量" + log.toString());
		remainMoneyLogDao.insert(log);
	}

	@Override
	public void handleOnRoleLogin(Role role) {
		// TODO Auto-generated method stub

	}

	public Player getPlayer(long rid) {
		return playerMap.get(rid);
	}

	public Map<Long, Player> getPlayerMap() {
		return playerMap;
	}

	/**
	 * 绑定playe和增加进缓存
	 * 
	 * @param player
	 * @param role
	 */
	public void playerOnline(Player player, Role role) {
		player.setRole(role);
		if (!playerMap.containsKey(player.getRole().getRid())) {
			playerMap.put(player.getRole().getRid(), player);
		}
		role.setOnline((byte) 1);
	}

	/**
	 * 删除缓存
	 * 
	 * @param player
	 * @param role
	 */
	public void playerOffline(Player player, Role role) {
		if (player.getRole() != null) {
			playerMap.remove(player.getRole().getRid());
			// player.setRole(null);
		}
	}

	/**
	 * 把角色添加到缓存中
	 * 
	 * @param role
	 */
	private void addCache(Role role) {
		roleCacheMap.put(role.getRid(), role);
	}

	public Role getRoleByOpenId(String openId) {
		long rid = roleDao.getByOpenId(openId);

		return getRoleByRid(rid);
	}

	/**
	 * 查询角色信息
	 *
	 * @param rid
	 * @return
	 */
	public Role getRoleByRid(long rid) {

		Role role = roleCacheMap.get(rid);
		if (role != null) {
			return role;
		}
		role = roleDao.findByKey(rid);
		if (role != null) {
			addCache(role);
		}
		return role;
	}

	public void addRole(Role role) {
		roleDao.insert(role);
		addCache(role);
	}

	public void updateRoleAll(Role role) {
		roleDao.update(role);
	}

	public void updatePropertys(Role role) {
		roleDao.updateProperty(role);

	}

	/**
	 * 踢全部
	 * 
	 * @param player
	 * @param role
	 */
	public void allOffline() {
		// 封号发送重登错误
		GMsg_12001003.Builder res = GMsg_12001003.newBuilder();
		res.setFlag(GameError.SYSTEM_MAINTAIN);
		sendMessageToAll(res.build());
		List<Player> list = new ArrayList<Player>();
		list.addAll(playerMap.values());
		for (Player player : list) {
			player.getChannel().close();
		}
	}

	/**
	 * 玩家是否在线
	 * 
	 * @param rid
	 * @return
	 */
	public boolean isOnline(long rid) {
		Player player = getPlayer(rid);
		if (player != null && player.getRole() != null
				&& player.getRole().getOnline() == 1) {
			return true;
		}
		return false;
	}

	public void sendMessageToPlayer(long rid, GeneratedMessageLite msg) {
		Player player = getPlayer(rid);
		if (player == null) {
			return;
		}
		player.write(msg);
	}

	/**
	 * 群发消息
	 * 
	 * @param rids
	 * @param message
	 */
	public void sendMessageToPlayers(Collection<Long> rids,
			GeneratedMessageLite msg) {
		Message message = Message.build(msg);
		for (Long rid : rids) {
			Player player = getPlayer(rid);
			if (player == null) {
				continue;
			}
			player.write(message);
		}
	}

	/**
	 * 全服消息
	 * 
	 * @param message
	 */
	public void sendMessageToAll(GeneratedMessageLite res) {
		Message message = Message.build(res);
		for (Player player : playerMap.values()) {
			player.write(message);
		}
	}

	/**
	 * 经验
	 */
	public boolean expAdd(Role role, int exp, boolean update) {
		if (exp < 0) {
			return false;
		}
		int oldLevel = role.getLevel();
		role.setExp(role.getExp() + exp);
		role.markToUpdate("exp");

		ExpCsv expCsv = expCache.getConfig(role.getLevel() + 1);
		int max = expCache.getConfigList().size();
		int upgradeExp = expCsv.getExp();
		if (role.getLevel() < max) {
			// 自动升级
			while (role.getExp() >= upgradeExp) {
				// addLevel += 1;
				if (role.getLevel() < max) {
					role.setExp(role.getExp() - upgradeExp);
					role.setLevel((short) (role.getLevel() + 1));// LV1是没有用的
					// 升级并处理当前级开放
					dealUpgradeLevelData(role);
					expCsv = expCache.getConfig(role.getLevel() + 1);
					upgradeExp = expCsv.getExp();
					role.markToUpdate("level");
				} else {
					role.setExp(upgradeExp);
					break;
				}
			}
			role.markToUpdate("exp");
		} else {
			if (role.getExp() > upgradeExp) {
				role.setExp(upgradeExp);
				role.markToUpdate("exp");
			}
		}

		if (update) {
			updatePropertys(role);

		}
		if (oldLevel < role.getLevel()) {
			missionFunction.checkTaskFinish(role.getRid(), TaskType.main_task,
					MissionType.LEVEL, role.getLevel());
			// 添加升级日志 G_T_C
			roleUpLogDao.addRoleUpLog(role);
		}

		sendBaseChangeMsg(role);
		return true;
	}

	private void dealUpgradeLevelData(Role role) {
		// 自动解锁avatar
		for (AvatarCsv avatarCsv : avatarCache.getConfigList()) {
			if (avatarCsv.getUnlockMethod() == role.getLevel()) {
				/*
				 * for (Map.Entry<Integer, List<Integer>> entry : role
				 * .getAvatarMap().entrySet()) { entry.getValue().set(1, 0); }
				 */
				role.getAvatarMap().put(avatarCsv.getCharacterId(),
						new ArrayList<Integer>());
				role.getAvatarMap().get(avatarCsv.getCharacterId()).add(1);
				role.getAvatarMap().get(avatarCsv.getCharacterId()).add(0);
				role.setAvatarMap();

				role.markToUpdate("avatar");

				LogUtil.info("" + role.getAvatar());
				GMsg_12002006.Builder builder = GMsg_12002006.newBuilder();
				builder.setFlag(0);
				builder.setDiamond(role.getDiamond());
				for (Map.Entry<Integer, List<Integer>> entry : role
						.getAvatarMap().entrySet()) {
					GRoleAvatar.Builder avatarBuilder = GRoleAvatar
							.newBuilder();
					avatarBuilder.setId(entry.getKey());
					avatarBuilder.setIslock(entry.getValue().get(0));
					avatarBuilder.setIsfight(entry.getValue().get(1));

					builder.addRoleAvatar(avatarBuilder);
				}
				sendMessageToPlayer(role.getRid(), builder.build());
			}
		}
	}

	/**
	 * 金币
	 */
	public boolean goldAdd(Role role, int gold, MoneyEvent moneyEvent,
			boolean update) {
		if (gold < 0) {
			return false;
		}
		int before = role.getGold();
		if (role.getGold() < 1000000000) {// 不能超过十亿
			role.setGold(role.getGold() + gold);
			role.markToUpdate("gold");

			if (update) {
				// 检测任务
				missionFunction.checkTaskFinish(role.getRid(),
						TaskType.main_task, MissionType.GOLD_NUM,
						role.getGold() + role.getGoldPot());
				updatePropertys(role);
			}

			addMoneyEvent(moneyEvent, role.getRid(), MoneyType.Gold, gold,
					before, role.getGold());
		} else {

			addMoneyEvent(MoneyEvent.GOLD_FULL, role.getRid(), MoneyType.Gold,
					gold, before, role.getGold());
		}

		sendBaseChangeMsg(role);
		return true;
	}

	/**
	 * 金币(减少)
	 */
	public boolean goldSub(Role role, int gold, MoneyEvent moneyEvent,
			boolean update) {
		if (gold < 0) {
			return false;
		}
		int before = role.getGold();
		//
		if (role.getGold() < gold) {
			gold = role.getGold();
		}

		role.setGold(role.getGold() - gold);

		role.markToUpdate("gold");

		// 破产监听设置
		updateGoldResuceInfo(role);
		// 破产监听设置

		if (update) {
			updatePropertys(role);
		}

		// sendMoneyInfo(role);
		addMoneyEvent(moneyEvent, role.getRid(), MoneyType.Gold, -gold, before,
				role.getGold());

		sendBaseChangeMsg(role);
		return true;
	}

	public void addMoneyEvent(MoneyEvent moneyEvent, long rid, MoneyType type,
			int value, int beforeValue, int afterValue) {
		Money money = new Money();
		money.setEvent(moneyEvent.getValue());
		money.setRid(rid);
		money.setTime(TimeUtil.time());
		money.setType(type.byteValue());
		money.setValue(value);
		money.setBeforeValue(beforeValue);
		money.setAfterValue(afterValue);
		moneyDao.addMoney(money);
	}

	/**
	 * 更新破产救济金的信息
	 * 
	 * @param role
	 */
	public void updateGoldResuceInfo(Role role) {
		PoChanCsv poChanCsv = poChanCache.getConfig(1);
		if (role.getGold() < poChanCsv.getGoldLowerLimit()
				&& role.getGoldResuceTimes() == 0) {
			String[] receiveCDArray = StringUtil.split(
					poChanCsv.getReceiveCD(), "|");
			int CD = Integer.parseInt(receiveCDArray[0]);// CD
			role.setGoldResuceNextTime(TimeUtil.time() + CD);
			role.markToUpdate("goldResuceNextTime");
		}
	}

	/**
	 * 存钱罐增加，钱减少
	 * 
	 * @return
	 */
	public void goldPotUpdate(Role role, int gold, int goldPot) {
		role.setGoldPot(goldPot);
		role.setGold(gold);
		role.markToUpdate("goldPot");
		role.markToUpdate("gold");
		updatePropertys(role);
	}

	/**
	 * 钻石
	 */
	public boolean diamondAdd(Role role, int diamond, MoneyEvent moneyEvent) {
		if (diamond < 0) {
			return false;
		}
		int before = role.getDiamond();
		role.setDiamond(role.getDiamond() + diamond);
		role.markToUpdate("diamond");
		updatePropertys(role);
		// 检测任务
		missionFunction.checkTaskFinish(role.getRid(), TaskType.main_task,
				MissionType.DIAMOND, role.getDiamond());
		// sendMoneyInfo(role);

		addMoneyEvent(moneyEvent, role.getRid(), MoneyType.Diamond, diamond,
				before, role.getDiamond());

		sendBaseChangeMsg(role);
		return true;
	}

	/**
	 * 钻石
	 */
	public boolean diamondSub(Role role, int diamond, MoneyEvent moneyEvent,
			boolean update) {
		if (diamond < 0) {
			return false;
		}
		int before = role.getDiamond();
		role.setDiamond(role.getDiamond() - diamond);
		role.markToUpdate("diamond");

		if (update) {
			updatePropertys(role);
		}

		// sendMoneyInfo(role);

		addMoneyEvent(moneyEvent, role.getRid(), MoneyType.Diamond, -diamond,
				before, role.getDiamond());

		sendBaseChangeMsg(role);
		return true;
	}

	/**
	 * 水晶
	 */
	public boolean crystalAdd(Role role, int crystal, MoneyEvent moneyEvent) {
		if (crystal < 0) {
			return false;
		}
		int before = role.getCrystal();
		role.setCrystal(role.getCrystal() + crystal);
		role.markToUpdate("crystal");
		updatePropertys(role);
		// 检测任务
		missionFunction.checkTaskFinish(role.getRid(), TaskType.main_task,
				MissionType.LOTTERIES, role.getCrystal());
		// sendMoneyInfo(role);

		addMoneyEvent(moneyEvent, role.getRid(), MoneyType.Cristal, crystal,
				before, role.getCrystal());

		sendBaseChangeMsg(role);
		return true;
	}

	/**
	 * 返回用户常变信息
	 * 
	 * @param player
	 */
	public void sendBaseChangeMsg(Role role) {
		GMsg_12002008.Builder builder = GMsg_12002008.newBuilder();
		builder.setGold(role.getGold());
		builder.setDiamond(role.getDiamond());
		builder.setCrystal(role.getCrystal());
		builder.setExp(role.getExp());
		builder.setLevel(role.getLevel());
		builder.setFlag(0);
		sendMessageToPlayer(role.getRid(), builder.build());
	}

	/**
	 * 水晶
	 */
	public boolean crystalSub(Role role, int crystal, MoneyEvent moneyEvent) {
		if (crystal < 0) {
			return false;
		}
		int before = role.getCrystal();
		if (role.getCrystal() < crystal) {
			crystal = role.getCrystal();
		}
		role.setCrystal(role.getCrystal() - crystal);
		role.markToUpdate("crystal");
		updatePropertys(role);

		// sendMoneyInfo(role);
		addMoneyEvent(moneyEvent, role.getRid(), MoneyType.Cristal, -crystal,
				before, role.getCrystal());

		sendBaseChangeMsg(role);
		return true;
	}

	// 昭通斗地主积分增加
	public boolean ztDoudizhuScoreAdd(Role role, int ztDoudizhuScore) {
		role.setZtDoudizhuScore(role.getZtDoudizhuScore() + ztDoudizhuScore);
		role.markToUpdate("ztDoudizhuScore");
		updatePropertys(role);
		return true;
	}

	// 昭通斗地主积分减少
	public boolean ztDoudizhuScoreSub(Role role, int ztDoudizhuScore) {
		role.setZtDoudizhuScore(role.getZtDoudizhuScore() - ztDoudizhuScore);
		role.markToUpdate("ztDoudizhuScore");
		updatePropertys(role);
		return true;
	}

	/**
	 * 新手名称
	 * 
	 * @param sex
	 * @param language
	 * @return
	 */
	public String randomNewRoleName() {

		int one = MathUtil.randomNumber(0, 9);
		int two = MathUtil.randomNumber(0, 9);
		int three = MathUtil.randomNumber(0, 9);
		int four = MathUtil.randomNumber(0, 9);
		String zhimu = "" + (char) (Math.random() * 26 + 'A');
		;
		return new StringBuilder().append("新手").append(one).append(two)
				.append(three).append(four).append(zhimu).toString();
	}

	/**
	 * 随机角色名称
	 * 
	 * @param sex
	 * @param language
	 * @return
	 */
	public String randomRoleName(int lang, int sex) {
		List<RandomNameCnCsv> firstNameList = null;
		if (sex % 1 == 0) {
			firstNameList = randomNameCnCache.getRandomNameList((byte) 1);
		} else {
			firstNameList = randomNameCnCache.getRandomNameList((byte) 1);
		}
		if (firstNameList == null) {
			return "";
		}
		// 姓氏
		RandomNameCnCsv firstName1 = firstNameList.get(Probability.rand(0,
				firstNameList.size() - 1));
		// RandomNameCnCsv firstName2 = firstNameList.get(Probability.rand(0,
		// firstNameList.size() - 1));

		// 名字
		List<RandomNameCnCsv> lastNameList = randomNameCnCache
				.getRandomNameList((byte) 2);
		if (lastNameList == null) {
			return "";
		}
		RandomNameCnCsv lastName = lastNameList.get(Probability.rand(0,
				lastNameList.size() - 1));

		return firstName1.getValue() + lastName.getValue();
	}

	/**
	 * 在线记录
	 */
	public void onlineSta() {
		roleOnlineDao.addRoleOnline(TimeUtil.time(), playerMap.size());
	}

	public boolean isPoChan(int gold, int goldPot, int subGold) {
		return (gold + goldPot - subGold) <= 0 ? true : false;
	}

	/**
	 * 
	 * 判断玩家是否可以领取救济金
	 * 
	 * @param goldResuceTimes 玩家已经领取救济金的次数 
	 * @return true 不能领取， false 可领取
	 */
	public boolean isHaveGoldResuceTimes(int goldResuceTimes) {
		PoChanCsv poChanCsv = poChanCache.getConfig(1);
		boolean isHaveGold = goldResuceTimes != poChanCsv.getReceiveTime();
		return isHaveGold;
	}

	public void doVoteResult(Role role, long voteId, int resultValue) {
		Map<Long, Integer> voteInfoMap = role.getVoteInfoMap();
		voteInfoMap.put(voteId, resultValue);
		String voteInfo = StringUtil.mapToString(voteInfoMap);
		role.setVoteInfo(voteInfo);
		role.markToUpdate("voteInfo");
		roleDao.updateProperty(role);
		VoteActivityLog log = new VoteActivityLog();
		log.setAtypeId((int) voteId);
		log.setRid(role.getRid());
		log.setVoteResult(resultValue);
		log.setNick(role.getNick());
		log.setTime(TimeUtil.time());
		voteActivityLogDao.insert(log);
	}

	/**
	 * 更新玩家充值的总金额
	 * 
	 * @param rid
	 * @param money
	 */
	public void updatePlayerMoney(long rid, int money) {
		Role role = getRoleByRid(rid);
		if (role != null) {
			role.setAllMoney(role.getAllMoney() + money);
			role.markToUpdate("allMoney");
			updatePropertys(role);
		} else {
			LogUtil.error("玩家不存在，充值金额更新有误，rid=" + rid + ",money=" + money);
		}
	}
}
