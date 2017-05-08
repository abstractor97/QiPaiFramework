/**
 * 
 */
package com.yaowan.server.game.service;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameError;
import com.yaowan.constant.ItemEvent;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.csv.cache.AvatarCache;
import com.yaowan.csv.cache.CheckInCache;
import com.yaowan.csv.cache.CumulativeDayCache;
import com.yaowan.csv.cache.PoChanCache;
import com.yaowan.csv.entity.AvatarCsv;
import com.yaowan.csv.entity.CheckInCsv;
import com.yaowan.csv.entity.CumulativeDayCsv;
import com.yaowan.csv.entity.PoChanCsv;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.ObjectUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GRole.GDoudizhuData;
import com.yaowan.protobuf.game.GRole.GMajiangData;
import com.yaowan.protobuf.game.GRole.GMenjiData;
import com.yaowan.protobuf.game.GRole.GMsg_11002017;
import com.yaowan.protobuf.game.GRole.GMsg_12002002;
import com.yaowan.protobuf.game.GRole.GMsg_12002003;
import com.yaowan.protobuf.game.GRole.GMsg_12002004;
import com.yaowan.protobuf.game.GRole.GMsg_12002005;
import com.yaowan.protobuf.game.GRole.GMsg_12002006;
import com.yaowan.protobuf.game.GRole.GMsg_12002007;
import com.yaowan.protobuf.game.GRole.GMsg_12002009;
import com.yaowan.protobuf.game.GRole.GMsg_12002010;
import com.yaowan.protobuf.game.GRole.GMsg_12002011;
import com.yaowan.protobuf.game.GRole.GMsg_12002012;
import com.yaowan.protobuf.game.GRole.GMsg_12002013;
import com.yaowan.protobuf.game.GRole.GMsg_12002014;
import com.yaowan.protobuf.game.GRole.GMsg_12002016;
import com.yaowan.protobuf.game.GRole.GMsg_12002017;
import com.yaowan.protobuf.game.GRole.GOtherRoleBaseInfo;
import com.yaowan.protobuf.game.GRole.GRoleAvatar;
import com.yaowan.protobuf.game.GRole.GZXMajiangData;
import com.yaowan.server.game.function.DoudizhuDataFunction;
import com.yaowan.server.game.function.ItemFunction;
import com.yaowan.server.game.function.MajiangDataFunction;
import com.yaowan.server.game.function.MenjiDataFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.function.ZTMajiangFunction;
import com.yaowan.server.game.function.ZXMajiangDataFunction;
import com.yaowan.server.game.function.ZXMajiangFunction;
import com.yaowan.server.game.model.data.dao.PackItemDao;
import com.yaowan.server.game.model.data.entity.DoudizhuData;
import com.yaowan.server.game.model.data.entity.MajiangData;
import com.yaowan.server.game.model.data.entity.MenjiData;
import com.yaowan.server.game.model.data.entity.PackItem;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.data.entity.ZXMajiangData;
import com.yaowan.server.game.model.struct.ZTMaJongTable;
import com.yaowan.server.game.model.struct.ZTMajiangRole;
import com.yaowan.util.KeywordUtil;

/**
 * @author zane
 *
 */
@Component
public class RoleService {

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private AvatarCache avatarCache;

	@Autowired
	private DoudizhuDataFunction doudizhuDataFunction;

	@Autowired
	private MajiangDataFunction majiangDataFunction;

	@Autowired
	private ZXMajiangDataFunction zxmajiangDataFunction;

	@Autowired
	private MenjiDataFunction menjiDataFunction;

	@Autowired
	private ZTMajiangFunction zTMajiangFunction;

	@Autowired
	private ZXMajiangFunction zXMajiangFunction;

	@Autowired
	private RoomFunction roomFunction;

	/*
	 * @Autowired private NCMenjiDataFunction ncmenjiDataFunction;
	 */

	@Autowired
	private PoChanCache poChanCache;

	@Autowired
	private CheckInCache checkInCache;

	@Autowired
	private ItemFunction itemFunction;

	@Autowired
	private CumulativeDayCache cumulativeDayCache;

	@Autowired
	private PackItemDao packItemDao;

	public void updateNick(Player player, String nick) {

		Role role = player.getRole();
		// 判断是否有非法的字
		if (!KeywordUtil.checkWord(nick)) {
			GMsg_12002002.Builder builder = GMsg_12002002.newBuilder();
			builder.setFlag(GameError.NICK_ILLEGALITY);
			builder.setNick(role.getNick());
			player.write(builder.build());
			return;
		}
		role.setNick(nick);
		role.markToUpdate("nick");
		if (role.getHasNick() == 0) {
			role.setHasNick((byte) 1);
			role.markToUpdate("hasNick");
		} else {
			// roleFunction.diamondSub(role, 100, MoneyEvent.NICK,false);

		}
		roleFunction.updatePropertys(role);

		GMsg_12002002.Builder builder = GMsg_12002002.newBuilder();
		builder.setFlag(0);
		builder.setNick(nick);
		player.write(builder.build());
	}

	public void updateSex(Player player, int sex) {

		Role role = player.getRole();
		role.setSex((byte) sex);
		role.markToUpdate("sex");
		roleFunction.updatePropertys(role);
		GMsg_12002003.Builder builder = GMsg_12002003.newBuilder();
		builder.setFlag(0);
		builder.setSex(sex);
		player.write(builder.build());
	}

	public void updateHead(Player player, int head) {

		Role role = player.getRole();
		role.setHead((byte) head);
		role.markToUpdate("head");
		roleFunction.updatePropertys(role);
		GMsg_12002004.Builder builder = GMsg_12002004.newBuilder();
		builder.setFlag(0);
		builder.setHead(head);
		player.write(builder.build());
	}

	public void updateLocation(Player player, int province, int city) {

		Role role = player.getRole();
		role.setProvince((short) province);
		role.setCity((short) city);
		role.markToUpdate("province");
		role.markToUpdate("city");
		roleFunction.updatePropertys(role);
		GMsg_12002005.Builder builder = GMsg_12002005.newBuilder();
		builder.setFlag(0);
		builder.setProvince(province);
		builder.setCity(city);
		player.write(builder.build());
	}

	public void unlockAvatar(Player player, int id) {
		AvatarCsv avatarCsv = avatarCache.getConfig(id);
		List<Integer> costList = StringUtil.stringToList(
				avatarCsv.getChaPrice(), "_", Integer.class);
		Role role = player.getRole();

		if (role.getAvatarMap().containsKey(id)) {
			GMsg_12002006.Builder builder = GMsg_12002006.newBuilder();
			builder.setFlag(GameError.AVATAR_HAS_UNLOCK);
			builder.setDiamond(role.getDiamond());
			for (Map.Entry<Integer, List<Integer>> entry : role.getAvatarMap()
					.entrySet()) {
				GRoleAvatar.Builder avatarBuilder = GRoleAvatar.newBuilder();
				avatarBuilder.setId(entry.getKey());
				avatarBuilder.setIslock(entry.getValue().get(0));
				avatarBuilder.setIsfight(entry.getValue().get(1));

				builder.addRoleAvatar(avatarBuilder);
			}

			player.write(builder.build());
			return;
		}

		/*
		 * for (Map.Entry<Integer, List<Integer>> entry : role.getAvatarMap()
		 * .entrySet()) { entry.getValue().set(1, 0); }
		 */
		role.getAvatarMap().put(id, new ArrayList<Integer>());
		role.getAvatarMap().get(id).add(1);
		role.getAvatarMap().get(id).add(0);
		role.setAvatarMap();

		role.markToUpdate("avatar");
		roleFunction
				.diamondSub(role, costList.get(1), MoneyEvent.AVATAR, false);
		roleFunction.updatePropertys(role);

		LogUtil.info("" + role.getAvatar());
		GMsg_12002006.Builder builder = GMsg_12002006.newBuilder();
		builder.setFlag(0);
		builder.setDiamond(role.getDiamond());
		for (Map.Entry<Integer, List<Integer>> entry : role.getAvatarMap()
				.entrySet()) {
			GRoleAvatar.Builder avatarBuilder = GRoleAvatar.newBuilder();
			avatarBuilder.setId(entry.getKey());
			avatarBuilder.setIslock(entry.getValue().get(0));
			avatarBuilder.setIsfight(entry.getValue().get(1));

			builder.addRoleAvatar(avatarBuilder);
		}

		player.write(builder.build());
	}

	public void useAvatar(Player player, int id) {
		Role role = player.getRole();
		for (Map.Entry<Integer, List<Integer>> entry : role.getAvatarMap()
				.entrySet()) {
			entry.getValue().set(1, 0);
		}
		AvatarCsv avatarCsv = avatarCache.getConfig(id);
		role.getAvatarMap().get(id).set(1, 1);
		role.setSex((byte) avatarCsv.getGender());
		role.setAvatarMap();
		role.markToUpdate("sex");
		role.markToUpdate("avatar");
		roleFunction.updatePropertys(role);
		GMsg_12002007.Builder builder = GMsg_12002007.newBuilder();
		builder.setFlag(0);
		for (Map.Entry<Integer, List<Integer>> entry : role.getAvatarMap()
				.entrySet()) {
			GRoleAvatar.Builder avatarBuilder = GRoleAvatar.newBuilder();
			avatarBuilder.setId(entry.getKey());
			avatarBuilder.setIslock(entry.getValue().get(0));
			avatarBuilder.setIsfight(entry.getValue().get(1));
			builder.addRoleAvatar(avatarBuilder);
		}

		player.write(builder.build());
	}

	public void listGameSta(Player player) {
		Role role = player.getRole();

		GMsg_12002009.Builder builder = GMsg_12002009.newBuilder();
		GDoudizhuData.Builder doudizhuBuilder = GDoudizhuData.newBuilder();
		DoudizhuData doudizhuData = doudizhuDataFunction.getDoudizhuData(role
				.getRid());
		ObjectUtil.copyProperties(doudizhuData, doudizhuBuilder);
		builder.setDoudizhuData(doudizhuBuilder);

		GMenjiData.Builder menjiDataBuilder = GMenjiData.newBuilder();
		MenjiData menjiData = menjiDataFunction.getMenjiData(role.getRid());
		ObjectUtil.copyProperties(menjiData, menjiDataBuilder);
		builder.setMenjiData(menjiDataBuilder);

		/** G_T_C add */
		/*
		 * GNCMenjiData.Builder ncmenjiDataBuilder = GNCMenjiData.newBuilder();
		 * NCMenjiData ncmenjiData =
		 * ncmenjiDataFunction.getMenjiData(role.getRid());
		 * ObjectUtil.copyProperties(ncmenjiData, ncmenjiDataBuilder);
		 * builder.setNcMenjiData(ncmenjiDataBuilder);
		 */
		/**********************************************/

		GMajiangData.Builder majiangDataBuilder = GMajiangData.newBuilder();
		MajiangData majiangData = majiangDataFunction.getMajiangData(role
				.getRid());
		ObjectUtil.copyProperties(majiangData, majiangDataBuilder);
		builder.setMajiangData(majiangDataBuilder);

		// 镇雄麻将
		GZXMajiangData.Builder zxmajiangDataBuilder = GZXMajiangData
				.newBuilder();
		ZXMajiangData zxmajiangData = zxmajiangDataFunction
				.getZXMajiangData(role.getRid());
		ObjectUtil.copyProperties(zxmajiangData, zxmajiangDataBuilder);
		builder.setZxmajiangData(zxmajiangDataBuilder);

		builder.setFavorGames(role.getFavorGames());
		player.write(builder.build());
	}

	/**
	 * 处理存钱罐
	 * 
	 * @author G_T_C
	 * @param player
	 * @param gold
	 * @param goldPot
	 */
	public void updateGoldPot(Player player, int gold, int goldPot) {
		Role role = player.getRole();
		// 以前的和现在的要一样
		int moneyA = Math.abs(gold) + Math.abs(goldPot);
		int moneyB = role.getGold() + role.getGoldPot();
		if (moneyA == moneyB) {// 防止作弊
			if (goldPot > role.getGoldPot() && gold >= 20000)// 存钱
			{
				roleFunction.goldPotUpdate(role, gold, goldPot);
			} else if (goldPot < role.getGoldPot()) {// 取钱
				if (role.getGoldPot() < 1000) {
					gold += role.getGoldPot();
					goldPot = 0;
				}
				roleFunction.goldPotUpdate(role, gold, goldPot);
			}
		}
		GMsg_12002010.Builder builder = GMsg_12002010.newBuilder();
		builder.setFlag(0);
		builder.setGold(role.getGold());
		builder.setGoldPot(role.getGoldPot());
		player.write(builder.build());
	}

	/**
	 * 重置玩家领取救济金的相关信息
	 * 
	 * @param role
	 */
	public void updateRoleGoldInfo(Role role) {
		role.setGoldResuceTimes(0);
		role.setGoldResuceNextTime(TimeUtil.time());
		role.markToUpdate("goldResuceTimes");
		role.markToUpdate("goldResuceNextTime");
		roleFunction.updatePropertys(role);
	}

	/**
	 * 申请领取救济金
	 * 
	 * @param player
	 * @param type
	 *            1查看救济信息 2领取救济金
	 */
	public void ApplyGoldResuce(Player player, int type) {
		LogUtil.info("type类型..." + type);
		Game game = roomFunction.getGameByRole(player.getRole().getRid());

		GMsg_12002011.Builder builder = GMsg_12002011.newBuilder();

		PoChanCsv poChanCsv = poChanCache.getConfig(1);// 破产配置表
		Role role = player.getRole();

		int nowTime = TimeUtil.time();// 获取系统当前时间戳
		int lastGetGoldTime = role.getGoldResuceNextTime();// 获取玩家上次的领取时间时间戳
		int intervalDays = TimeUtil.apartDay(lastGetGoldTime, nowTime);// 获取两个时间之间相隔的天数

		if (intervalDays > 0) {
			updateRoleGoldInfo(role);
		}

		String[] quantityArray = StringUtil.split(
				poChanCsv.getReceiveQuantity(), "|");
		int goldResuceTimes = role.getGoldResuceTimes();
		// 领取黄金
		int gold = 0;
		if (role.getGoldResuceTimes() < poChanCsv.getReceiveTime()) {
			 gold = Integer.parseInt(quantityArray[goldResuceTimes]);
		}
		
		if (type == 2) {
			if (role.getGold() + role.getGoldPot() < poChanCsv
					.getGoldLowerLimit()) {
				if (role.getGoldResuceTimes() < poChanCsv.getReceiveTime()) {

					role.setGoldResuceTimes(role.getGoldResuceTimes() + 1);
					role.setGoldResuceNextTime(nowTime);
					role.markToUpdate("goldResuceTimes");
					role.markToUpdate("goldResuceNextTime");
					roleFunction.updatePropertys(role);

					roleFunction.goldAdd(role, gold, MoneyEvent.GOLDRESUCE,
							true);
					builder.setGoldGet(gold);
					zTMajiangFunction.dealPay(game, player, type, gold);
					zXMajiangFunction.dealPay(game, player, type);
				}

			}
		}
		if (type == 1 || type == 2) {
			// 1今日领取结束 0今日领取尚未没结束
			int isOver = role.getGoldResuceTimes() == poChanCsv
					.getReceiveTime() ? 1 : 0;
			builder.setIsOver(isOver);
			builder.setGoldGet(gold);
			builder.setFlag(0);
			builder.setType(type);
			builder.setGoldResuceTimes(role.getGoldResuceTimes());
			builder.setGoldTimes(poChanCsv.getReceiveTime()
					- role.getGoldResuceTimes());
			player.write(builder.build());
			LogUtil.info("rid" + role.getRid() + "builder.getIsOver()..."
					+ builder.getIsOver());
		}
		if (type == 3) {
			// 玩家没领救济金
			zTMajiangFunction.dealPay(game, player, type, gold);
			zXMajiangFunction.dealPay(game, player, type);
		}

	}

	// public void updateGoldResuceTime(Player player, int type) {
	// GMsg_12002011.Builder builder = GMsg_12002011.newBuilder();
	// PoChanCsv poChanCsv = poChanCache.getConfig(1);// 破产配置表
	// Role role = player.getRole();
	// int time = TimeUtil.time();
	// if (role.getGold() + role.getGoldPot() < poChanCsv.getGoldLowerLimit())//
	// 条件一：小于最低限度
	// {
	//
	// if (role.getGoldResuceTimes() < poChanCsv.getReceiveTime())//
	// 条件二：次数小于n次可领
	// {
	// String[] CDArray = StringUtil.split(poChanCsv.getReceiveCD(),
	// "|");
	// // if (role.getGoldResuceTimes() < poChanCsv.getReceiveTime()) {
	//
	// if (role.getGoldResuceNextTime() == 0) {
	// int CD = Integer
	// .parseInt(CDArray[role.getGoldResuceTimes()]);//
	// CD(第一次破产的CD在使用金币或者扣除金币的情况)
	// System.out.println(CD);
	// role.setGoldResuceNextTime(time + CD);
	// role.markToUpdate("goldResuceNextTime");
	// }
	//
	// // }
	// if (type == 2)// type为2表示领取救济金
	// {
	//
	// if (role.getGoldResuceNextTime() <= TimeUtil.time())// 条件4：到达
	// {
	//
	// String[] quantityArray = StringUtil.split(
	// poChanCsv.getReceiveQuantity(), "|");
	// // 领取黄金
	// int gold = Integer.parseInt(quantityArray[role
	// .getGoldResuceTimes()]);
	// roleFunction.goldAdd(role, gold, MoneyEvent.GOLDRESUCE,
	// true);
	// // 领取次数
	// role.setGoldResuceTimes(role.getGoldResuceTimes() + 1);
	// role.markToUpdate("goldResuceTimes");
	// role.setGoldResuceNextTime(0);// 领取后，把时间重置
	// role.markToUpdate("goldResuceNextTime");
	// // 下次领取时间（3次则不能再领取）
	// /*
	// * if (role.getGoldResuceTimes() < poChanCsv
	// * .getReceiveTime()) { int CD =
	// * Integer.parseInt(CDArray[role
	// * .getGoldResuceTimes()]);// CD(第一次破产的CD在使用金币或者扣除金币的情况)
	// * role.setGoldResuceNextTime(TimeUtil.time() + CD);
	// * role.markToUpdate("goldResuceNextTime"); }
	// */
	// // 修改信息
	// roleFunction.updatePropertys(role);
	// builder.setGoldGet(gold);// 获得黄金
	// }
	// }
	// }
	// }
	// // 1今日领取结束 0今日领取尚未没结束
	// int isOver = role.getGoldResuceTimes() == poChanCsv.getReceiveTime() ? 1
	// : 0;
	// builder.setIsOver(isOver);
	// builder.setFlag(0);
	// builder.setType(type);
	// builder.setGoldResuceTimes(role.getGoldResuceTimes());
	// int cd = role.getGoldResuceNextTime() - time;
	// builder.setGoldResuceNextTime(cd <= 0 ? 0 : cd);
	// player.write(builder.build());
	// }

	// role签到
	public void userSign(Player player, int wk) {

		int day = TimeUtil.getDayOfWeek();
		if (day != wk) {
			GMsg_12002012.Builder builder = GMsg_12002012.newBuilder();
			builder.setFlag(1);
			player.write(builder.build());
			return;
		}

		Role role = player.getRole();
		int date = TimeUtil.getTodayYmd();
		List<Integer> recordList = role.getSignRecordList();
		if (!recordList.contains(date)) {
			recordList.add(date);
			Collections.sort(recordList);
			int maxContinueSign = role.getMaxContinueSign() + 1;// maxContinueDay(recordList);

			role.setSignRecordList();
			if (maxContinueSign > role.getMaxContinueSign()) {
				role.setMaxContinueSign(maxContinueSign);
			}
			role.markToUpdate("maxContinueSign");
			role.markToUpdate("signRecord");

			day = day == 0 ? 7 : day;

			CheckInCsv checkInCsv = checkInCache.getConfig(day);
			String rewardItem = checkInCsv.getRewardItem();// 本日签到所得
			// 签到获得相关物品
			updateData(role, rewardItem);
			// 签到获得相关物品
		}
		GMsg_12002012.Builder builder = GMsg_12002012.newBuilder();
		builder.setFlag(0);
		builder.setMaxContinueSign(role.getMaxContinueSign());
		builder.setSignRecord(role.getSignRecord());
		player.write(builder.build());
	}

	// role补签(要计算出最长的部分),补签哪一天
	public void userSignAgain(Player player, int date) {
		int today = TimeUtil.getDay();
		if (date >= today) {
			return;
		}
		Role role = player.getRole();
		List<Integer> recordList = role.getSignRecordList();
		if (!recordList.contains(date)) {
			// 扣除道具
			PackItem packItem = packItemDao.getPackItemByIds(role.getRid(),
					1010001);
			if (packItem == null) {
				LogUtil.info(player.getRole().getRid() + "参数错误,date:" + date);
				return;
			} else {
				itemFunction.usePackItem(role, packItem.getId(), true);// 使用道具
			}
			recordList.add(date);
			Collections.sort(recordList);
			role.setSignRecordList();
			// 重新计算最大天数
			role.setMaxContinueSign(maxContinueDay(recordList));
			// 重新计算最大天数
			role.markToUpdate("maxContinueSign");
			role.markToUpdate("signRecord");
			CheckInCsv checkInCsv = checkInCache.getConfig(date);
			String rewardItem = checkInCsv.getRewardItem();// 本日补签所得
			// 签到获得相关物品
			updateData(role, rewardItem);
			// 签到获得相关物品
		}
		GMsg_12002013.Builder builder = GMsg_12002013.newBuilder();
		builder.setFlag(0);
		builder.setMaxContinueSign(role.getMaxContinueSign());
		builder.setSignRecord(role.getSignRecord());
		player.write(builder.build());
	}

	/**
	 * 补签算法：补签后签到数据重新计算,该方法是获得补签后最大的签到天数
	 * 
	 * @param list
	 * @return
	 */
	public int maxContinueDay(List<Integer> list) {
		Collections.sort(list);
		int maxContnueDay = 1;
		int continueDay = 1;
		for (int i = 1; i < list.size(); i++) {
			LogUtil.info("a1.get(i)" + list.get(i) + "****" + "a1.get(i-1)"
					+ list.get(i - 1));
			if (list.get(i) - list.get(i - 1) == 1) {
				continueDay++;
				maxContnueDay = continueDay > maxContnueDay ? continueDay
						: maxContnueDay;
			} else {
				continueDay = 1;
			}
		}
		return maxContnueDay;
	}

	/**
	 * 连续签到的奖励
	 * 
	 * @param player
	 */
	public void getContinueSignReward(Player player, int id) {
		Role role = player.getRole();
		int MaxContinue = role.getMaxContinueSign();
		CumulativeDayCsv cumulativeDayCsv = cumulativeDayCache.getConfig(id);
		List<Integer> signRewardGetList = role.getSignRewardGetList();
		// 奖励不为null 并且 用户没有领取过奖励
		if (MaxContinue >= cumulativeDayCsv.getCumulativeDay()
				&& (!signRewardGetList.contains(id))) {
			signRewardGetList.add(id);
			role.setSignRewardGetList();
			role.markToUpdate("signRewardGet");
			// 连续签到获得相关物品
			updateData(role, cumulativeDayCsv.getRewardItem());
			// 连续签到获得相关物品
			GMsg_12002014.Builder builder = GMsg_12002014.newBuilder();
			builder.setFlag(0);
			builder.setSignRewardGet(role.getSignRewardGet());
			player.write(builder.build());
		}

	}

	public void updateData(Role role, String rewardItem) {
		String[] rewardItems = StringUtil.split(rewardItem, "|");// 所有物品
		for (String myReward : rewardItems) {// 单个物品
			String[] item = StringUtil.split(myReward, "_");// 单个物品
			String itemId = item[0];// 物品id
			int num = Integer.parseInt(item[1]);// 物品数量
			if (itemId.startsWith("10"))// 获得物品
			{
				itemFunction.addPackItem(role, Integer.parseInt(itemId), num,
						ItemEvent.GetItem, true);
			}
			if (itemId.startsWith("20"))// 获得金币，砖石，水晶，经验
			{
				switch (itemId) {
				case "2010001":// 金币
					roleFunction
							.goldAdd(role, num, MoneyEvent.SIGNREWARD, true);
					break;
				case "2020002":// 砖石
					roleFunction.diamondAdd(role, num, MoneyEvent.SIGNREWARD);
					break;
				case "2030003":// 水晶
					roleFunction.crystalAdd(role, num, MoneyEvent.SIGNREWARD);
					break;
				case "2030004":// 经验
					roleFunction.expAdd(role, num, true);
					break;
				default:
					break;
				}
			}
		}
	}

	/**
	 * 获取玩家详细信息
	 * 
	 * @author G_T_C
	 * @param msg
	 */
	public void getGOtherRoleInfo(long rid, Player player) {
		Role role = roleFunction.getRoleByRid(rid);
		if (role != null) {
			GMsg_12002016.Builder builder = GMsg_12002016.newBuilder();
			GOtherRoleBaseInfo.Builder roleInfoBuilder = GOtherRoleBaseInfo
					.newBuilder();
			roleInfoBuilder.setCity(role.getCity());
			roleInfoBuilder.setDiamond(role.getDiamond());
			roleInfoBuilder.setExp(role.getExp());
			roleInfoBuilder.setGame(role.getLastGameType());
			roleInfoBuilder.setGold(role.getGold());
			roleInfoBuilder.setLevel(role.getLevel());
			roleInfoBuilder.setHead(role.getHead());
			roleInfoBuilder.setNick(role.getNick());
			roleInfoBuilder.setOnline(role.getOnline());
			roleInfoBuilder.setProvince(role.getProvince());
			roleInfoBuilder.setRid(role.getRid());
			roleInfoBuilder.setSex(role.getSex());
			builder.setRoleInfo(roleInfoBuilder.build());
			player.write(builder.build());
		}
	}

	/**
	 * 处理投票和日志
	 * 
	 * @author G_T_C
	 * @param player
	 * @param msg
	 */
	public void doVoteResult(Player player, GMsg_11002017 msg) {
		if (null == player.getRole()) {
			return;
		}
		Role role = player.getRole();
		roleFunction.doVoteResult(role, msg.getVoteActivityId(),
				msg.getVoteResult());
		GMsg_12002017.Builder builder = GMsg_12002017.newBuilder();
		builder.setFlag(0);
		builder.setVoteActivityId(msg.getVoteActivityId());
		builder.setVoteResult(msg.getVoteResult());
		player.write(builder.build());
	}

}
