/**
 * 
 */
package com.yaowan.server.game.function;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.MoneyEvent;
import com.yaowan.constant.MoneyType;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.core.function.FunctionManager;
import com.yaowan.csv.cache.AppGameCache;
import com.yaowan.csv.cache.AvatarCache;
import com.yaowan.csv.cache.CumulativeDayCache;
import com.yaowan.csv.cache.HeadPortraitCache;
import com.yaowan.csv.entity.AppGameCsv;
import com.yaowan.csv.entity.AvatarCsv;
import com.yaowan.csv.entity.CumulativeDayCsv;
import com.yaowan.csv.entity.HeadPortraitCsv;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.dao.RoleDao;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.dao.MoneyDao;
import com.yaowan.server.game.model.log.dao.RegisterLogDao;
import com.yaowan.server.game.model.log.dao.RoleLoginDao;
import com.yaowan.server.game.model.struct.MissionType;
import com.yaowan.server.game.model.struct.TaskType;
import com.yaowan.util.RecommendUtil;

/**
 * @author zane
 *
 */
@Component
public class LoginFunction extends FunctionAdapter {

	@Autowired
	private ItemFunction itemFunction;

	@Autowired
	private RoleDao roleDao;

	@Autowired
	private MoneyDao moneyDao;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private CommonFunction commonFunction;

	@Autowired
	private DoudizhuDataFunction doudizhuDataFunction;

	@Autowired
	private MajiangDataFunction majiangDataFunction;

	@Autowired
	private ZXMajiangDataFunction zxmajiangDataFunction;

	@Autowired
	private MenjiDataFunction menjiDataFunction;

	@Autowired
	private AvatarCache avatarCache;

	@Autowired
	private HeadPortraitCache headPortraitCache;

	@Autowired
	private AppGameCache appGameCache;

	@Autowired
	private RegisterLogDao registerLogDao;

	@Autowired
	private RoleLoginDao roleLoginDao;

	@Autowired
	private MissionFunction missionFunction;

	@Autowired
	private CumulativeDayCache cumulativeDayCache;

	public Role loginRole(String openId, String imei, String platform,
			String ip, byte deviceType, byte loginType, String nickname,
			String imgurl, String deviceToken,int u8id) {
		long rid = roleDao.getByOpenId(openId);
		Role role = null;
		if (rid == 0) {
			int initGold = 10000;

			role = new Role();
			role.setNick(roleFunction.randomNewRoleName());
			role.setOpenId(openId);
			role.setGold(initGold);
			role.setDiamond(0);
			role.setExp(0);
			role.setImei(imei);
			role.setSignRecord("");
			role.setSignRewardGet("");
			role.setLastLoginIp(ip);
			role.setPlatform(platform);
			role.setDailyLogin(1);
			role.setDeviceType(deviceType);
			role.setLoginType(loginType);
			role.setDeviceToken(deviceToken == null ? "" : deviceToken);
			role.setU8id(u8id);
			for (AvatarCsv avatarCsv : avatarCache.getConfigList()) {
				// 初始化avatar
				if (avatarCsv.getUnlockMethod() == 1) {
					List<Integer> list = new ArrayList<Integer>();
					list.add(1);
					if (avatarCsv.getGender() == 0) {
						list.add(1);
					} else {
						list.add(0);
					}

					role.getAvatarMap().put(avatarCsv.getCharacterId(), list);
					role.setAvatarMap();
				}
			}

			// 初始化头像
			for (HeadPortraitCsv headPortraitCsv : headPortraitCache
					.getConfigList()) {
				if (headPortraitCsv.getGender() == 0) {
					role.setHead((byte) headPortraitCsv.getID());
					break;
				}
			}

			if (nickname != null && !"".equals(nickname)) {
				role.setNick(nickname);
			}
			if (imgurl != null && !"".equals(imgurl)) {
				role.setHeadimgurl(imgurl);
			}

			// 初始化最喜欢游戏
			AppGameCsv appGameCsv = appGameCache.getConfig(10001);
			if (appGameCsv != null) {
				List<Integer> gameList = StringUtil.stringToList(
						appGameCsv.getGameList(), "|", Integer.class);

				for (int i = 0; i < gameList.size() && i < 3; i++) {
					role.getFavorGamesMap().put(gameList.get(i), 0);
				}
				role.setFavorGamesMap();
			}
			role.setIsRecommend((byte)0);
			roleFunction.addRole(role);
			rid = role.getRid();
			//生成推荐码
			String code = RecommendUtil.createCode(rid);
			roleDao.updateRecommendNum(code, rid);
			
			doudizhuDataFunction.getDoudizhuData(rid);
			majiangDataFunction.getMajiangData(rid);
			zxmajiangDataFunction.getZXMajiangData(rid);
			menjiDataFunction.getMenjiData(rid);

			// 李培光 初始化新用户的背包道具
			// itemFunction.newRolePackItem(role);

			registerLogDao.addRegisterLog(role.getRid(), role.getCreateTime(),
					role.getNick(), role.getImei(), ip, platform, 1);

			roleFunction.addMoneyEvent(MoneyEvent.REGISTER, rid,
					MoneyType.Gold, initGold, 0, role.getGold());

		} else {
			role = roleFunction.getRoleByRid(rid);
			if(role.getCode().equals("")) {
				String code = RecommendUtil.createCode(rid);
				roleDao.updateRecommendNum(code, rid);
			}

			int time = TimeUtil.time();
			int today = TimeUtil.getTodayYmd();
			int lastYmd = TimeUtil.getDayYmd(role.getLastLoginTime());
			// 是否首次登陆
			if (today > lastYmd) {
				// 签到重置
				CumulativeDayCsv cumulativeDayCsv = cumulativeDayCache
						.getConfigList().get(
								cumulativeDayCache.getConfigList().size() - 1);
				if (role.getMaxContinueSign() >= cumulativeDayCsv
						.getCumulativeDay()) {
					role.setMaxContinueSign(0);

					role.getSignRewardGetList().clear();
					role.setSignRewardGetList();
					role.markToUpdate("maxContinueSign");

					role.markToUpdate("signRewardGet");
				}
				if (role.getSignRecordList().size() >= 14) {
					role.getSignRecordList().remove(0);
					role.setSignRecordList();
					role.markToUpdate("signRecord");
				}

			}
			role.setLastLoginTime(time);
			role.setDailyLogin(role.getDailyLogin() + 1);
			role.setOnline((byte) 1);
			role.setPlatform(platform);
			role.setLastLoginIp(ip);
			role.setDeviceType(deviceType);
			role.setLoginType(loginType);
			/*
			 * if(nickname != null && !"".equals(nickname)){
			 * role.setNick(nickname); role.markToUpdate("nick"); }
			 */
		/*	if (imgurl != null && !"".equals(imgurl)) {
				role.setHeadimgurl(imgurl);
				role.markToUpdate("headimgurl");
			}*/
			role.setDeviceToken(deviceToken == null ? "" : deviceToken);
			role.markToUpdate("deviceToken");
			role.markToUpdate("online");
			role.markToUpdate("lastLoginTime");
			role.markToUpdate("platform");
			role.markToUpdate("lastLoginIp");
			role.markToUpdate("dailyLogin");
			role.markToUpdate("deviceType");
			role.markToUpdate("loginType");
			roleFunction.updatePropertys(role);
		}
		missionFunction.checkTaskFinish(role.getRid(), TaskType.daily_task,
				MissionType.LOGIN);
		roleLoginDao.addRoleLogin(role.getRid(), role.getLastLoginTime(), ip,
				role.getImei(), role.getLoginType());

		return role;
	}

	public void logout(Role role, String ip) {
		// 将连接通道上的玩家对象置空
		role.setOnline((byte) 0);
		int currentTime = TimeUtil.time();
		role.setLastOfflineTime(currentTime);
		// 计算在线时长
		role.setOnlineTime(role.getOnlineTime() + currentTime
				- role.getLastLoginTime());

		if (role.getOnlineTime() < 0) {
			role.setOnlineTime(0);
		}

		role.markToUpdate("online");
		role.markToUpdate("lastOfflineTime");
		role.markToUpdate("onlineTime");
		roleFunction.updatePropertys(role);

		FunctionManager.doHandleOnRoleLogout(role);
		roleLoginDao.updateOnlineTime(role.getRid(), role.getLastLoginTime(),
				currentTime- role.getLastLoginTime());
	}

}
