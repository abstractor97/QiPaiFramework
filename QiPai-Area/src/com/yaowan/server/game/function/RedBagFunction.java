package com.yaowan.server.game.function;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.MoneyEvent;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.MathUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GGame.GMsg_12006012;
import com.yaowan.protobuf.game.GGame.GMsg_12006013;
import com.yaowan.server.game.model.data.dao.RedBagDao;
import com.yaowan.server.game.model.data.dao.RoleRedBagDao;
import com.yaowan.server.game.model.data.entity.RedBag;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.data.entity.RoleRedBag;
import com.yaowan.server.game.model.struct.RedBagInfo;

/**
 * 
 * @author G_T_C
 */
@Component
public class RedBagFunction extends FunctionAdapter {

	@Autowired
	private RedBagDao redBagDao;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private RoleRedBagDao roleRedBagDao;

	private Map<Long, RedBag> redBagCache = new ConcurrentHashMap<>();

	private Map<Long, RoleRedBag> roleRedBagCache = new ConcurrentHashMap<>();

	private RedBag hasRedBag = null;

	@Override
	public void handleOnNextDay() {
		roleRedBagCache.clear();
		roleRedBagDao.truncate();

	}

	@Override
	public void handleOnServerStart() {
		List<RedBag> redBagList = redBagDao.selectRedBag();
		if (redBagList != null) {
			for (RedBag redBag : redBagList) {
				redBagCache.put(redBag.getId(), redBag);
			}
		}
	}

	@Override
	public void handleOnRoleLogin(Role role) {
		RoleRedBag roleRedBag = roleRedBagDao.findByKey(role.getRid());
		if (hasRedBag != null) {
			LogUtil.info(hasRedBag.toString());
			if (roleRedBag == null || roleRedBag != null
					&& roleRedBag.getDailyNum() < hasRedBag.getDayLimit()
					&& hasRedBag.getGoingLimit() > roleRedBag.getTimesNum()) {
				GMsg_12006012.Builder builder = GMsg_12006012.newBuilder();
				builder.setFlag(1);
				roleFunction
						.sendMessageToPlayer(role.getRid(), builder.build());

			}

		}
		if (roleRedBag != null) {
			roleRedBagCache.put(role.getRid(), roleRedBag);
		} else {
			roleRedBag = new RoleRedBag();
			roleRedBag.setDailyNum(0);
			roleRedBag.setRid(role.getRid());
			roleRedBag.setTimesNum(0);
			roleRedBagDao.insert(roleRedBag);
			roleRedBagCache.put(role.getRid(), roleRedBag);
		}
	}

	@Override
	public void handleOnRoleLogout(Role role) {
		roleRedBagCache.remove(role.getRid());
	}

	/**
	 * 红包推送
	 * 
	 * @author G_T_C
	 */
	public void checkRedBag() {
		int time = TimeUtil.time();
		for (Long key : redBagCache.keySet()) {
			RedBag redBag = redBagCache.get(key);
			if (redBag.getEndTime() > time && redBag.getStartTime() <= time) {
				List<RedBagInfo> infos = redBag.getInfoList();
				if (infos == null) {
					continue;
				}
				for (int i = 0; i < infos.size(); i++) {
					RedBagInfo info = infos.get(i);
					int stime = info.getStime();
					int etime = info.getEtime();
					//System.out.println(stime+","+etime+","+time);
					if (stime <= time && etime > time) {
					//	 System.out.println(info.getHasSend());
						if (info.getHasSend() == 0) {
							sedRedBag(1, redBag.getDayLimit());
							info.setHasSend(1);
							hasRedBag = redBag;
							hasRedBag.setGoingLimit(info.getLimit());
							LogUtil.info("本轮红包开始");
						} else if (info.getHasSend() == 1) {
							hasRedBag = redBag;
						}
						return;
					}
				}

			} else if (redBag.getEndTime() < time) {
				List<RedBagInfo> infos = redBag.getInfoList();
				if (infos.isEmpty()) {
					redBagCache.remove(key);
				}
			}
		}
	}
	
	public void checkRedBagEnd(){
		
		if(hasRedBag == null){
			return ;
		}
		long id = hasRedBag.getId();
		int time = TimeUtil.time();
		List<RedBagInfo> infos = hasRedBag.getInfoList();
		if (infos == null) {
			return;
		}
		for (int i = 0; i < infos.size(); i++) {
			RedBagInfo info = infos.get(i);
			int etime = info.getEtime();
			if (etime <= time) {
				if (info.getHasSend() == 1) {
					info.setHasSend(2);
					sedRedBag(2, hasRedBag.getDayLimit());
					hasRedBag = null;
					roleRedBagDao.resetEpicycle();// 本轮重置
					for (Long key2 : roleRedBagCache.keySet()) {
						RoleRedBag roleRedBag = roleRedBagCache
								.get(key2);
						roleRedBag.setTimesNum(0);
					}
					LogUtil.info("本轮红包结束");
				} else if (info.getHasSend() == 0) {
					info.setHasSend(2);
				}
				infos.remove(info);
				redBagCache.get(id).setInfoList(infos);
			}
		}
	}

	private void sedRedBag(int flag, int dayLimit) {
		GMsg_12006012.Builder builder = GMsg_12006012.newBuilder();
		builder.setFlag(flag);
		roleFunction.sendMessageToAll(builder.build());
		/*
		 * for(Long rid : roleRedBagCache.keySet()){ RoleRedBag roleRedBag =
		 * roleRedBagCache.get(rid); if(roleRedBag.getDailyNum() < dayLimit){
		 * 
		 * } }
		 */
	}

	/**
	 * 领取红包
	 * 
	 * @author G_T_C
	 * @param player
	 */
	public void reward(Player player) {
		int time = TimeUtil.time();
		Role role = player.getRole();
		if (role == null) {
			return;
		}
		if (hasRedBag == null) {
			sendRewardMessage(player, 4, 0, 0, 0, 0);
			return;
		}
		List<RedBagInfo> infos = hasRedBag.getInfoList();
		if (infos == null) {
			sendRewardMessage(player, 4, 0, 0, 0, 0);
			return;
		}
		RoleRedBag roleRedBag = getRoleRedBag(role.getRid());
		if (roleRedBag != null
				&& hasRedBag.getDayLimit() <= roleRedBag.getDailyNum()) {
			sendRewardMessage(player, 3, 0, 0, 0, 0);
			LogUtil.warn("玩家不存在，为空");
			return;
		}
		for (int i = 0; i < infos.size(); i++) {
			RedBagInfo info = infos.get(i);
			int stime = info.getStime();
			int etime = info.getEtime();
			if (stime <= time && etime > time && info.getHasSend() == 1) {// 进行中
				int limit = info.getLimit();
				if (roleRedBag != null && limit <= roleRedBag.getTimesNum()) {
					sendRewardMessage(player, 2, 0, 0, 0, 0);
					return;
				}
				// 领取
				randomReward(player, role, info, roleRedBag);
				return;

			} else if (etime <= time) {
				infos.remove(info);
			}
		}

	}

	private RoleRedBag getRoleRedBag(long rid) {
		RoleRedBag roleRedBag = roleRedBagCache.get(rid);
		if (roleRedBag == null) {
			roleRedBag = roleRedBagDao.findByKey(rid);
			if (roleRedBag != null) {
				roleRedBagCache.put(rid, roleRedBag);
			}
		}
		return roleRedBag;
	}

	private void randomReward(Player player, Role role, RedBagInfo info,
			RoleRedBag roleRedBag) {
		int weight = MathUtil.randomNumber(0, info.getWeightCount());
		List<RedBagInfo.RewardInfo> list = info.getInfos();
		if (list != null && !list.isEmpty()) {
			int minWeight = 0;
			int maxWeight = 0;
			for (RedBagInfo.RewardInfo reward : list) {
				maxWeight += reward.getWeight();
				if (minWeight <= weight && weight < maxWeight) {
					int num = MathUtil.randomNumber(reward.getMinNum(),
							reward.getMaxNum());
					switch (reward.getType()) {
					case 1:// 金币
						roleFunction
								.goldAdd(role, num, MoneyEvent.REDBAG, true);
						break;
					case 2:// 钻石
						roleFunction.diamondAdd(role, num, MoneyEvent.REDBAG);
						break;
					case 3:// 奖券
						roleFunction.crystalAdd(role, num, MoneyEvent.REDBAG);
						break;
					}
					// 领取
					if (roleRedBag == null) {
						sendRewardMessage(player, 1, reward.getType(), num, 1,
								info.getLimit());
						roleRedBag = new RoleRedBag();
						roleRedBag.setDailyNum(1);
						roleRedBag.setRid(role.getRid());
						roleRedBag.setTimesNum(1);
						roleRedBagDao.insert(roleRedBag);
						roleRedBagCache.put(role.getRid(), roleRedBag);
					} else {
						roleRedBag.setTimesNum(roleRedBag.getTimesNum() + 1);
						roleRedBag.setDailyNum(roleRedBag.getDailyNum() + 1);
						sendRewardMessage(player, 1, reward.getType(), num,
								roleRedBag.getTimesNum(), info.getLimit());
						roleRedBagDao.updateTimesNum(role.getRid());
					}
					return;
				} else {
					minWeight += reward.getWeight();
				}
			}
		} else {
			sendRewardMessage(player, 4, 0, 0, 0, 0);
		}
	}

	private void sendRewardMessage(Player player, int flag, int type, int num,
			int value, int limit) {
		GMsg_12006013.Builder builder = GMsg_12006013.newBuilder();
		builder.setFlag(flag);
		builder.setType(type);
		builder.setRewardNum(num);
		builder.setReward(value);
		builder.setLimit(limit);
		player.write(builder.build());
	}

	public void update(RedBag redBag) {
		redBagDao.update(redBag);
		redBagCache.put(redBag.getId(), redBag);
		/*if (hasRedBag != null) {
			List<RedBagInfo> infos = hasRedBag.getInfoList();
			for (int i = 0; i < infos.size(); i++) {
				RedBagInfo info = infos.get(i);
				if (info.getHasSend() == 1) {
					info.setHasSend(2);
					hasRedBag = null;
					sedRedBag(2, redBag.getDayLimit());
					roleRedBagDao.resetEpicycle();// 本轮重置
					for (Long key2 : roleRedBagCache.keySet()) {
						RoleRedBag roleRedBag = roleRedBagCache.get(key2);
						roleRedBag.setTimesNum(0);
					}
					LogUtil.info("本轮红包结束");
				} else if (info.getHasSend() == 0) {
					info.setHasSend(2);
				}
			}
		}*/
	}

	public void add(RedBag redBag) {
		redBagDao.insert(redBag);
		redBagCache.put(redBag.getId(), redBag);
	}

}
