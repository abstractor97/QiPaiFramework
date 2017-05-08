package com.yaowan.server.game.function;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.dao.RoleDao;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.dao.ChargeDao;
import com.yaowan.server.game.model.log.dao.RoomCountLogDao;
import com.yaowan.server.game.model.log.dao.RoomRoleActiveDao;
import com.yaowan.server.game.model.log.dao.RoomRoleLoginDao;
import com.yaowan.server.game.model.log.dao.RoomRoleOnlineDao;
import com.yaowan.server.game.model.log.entity.Charge;
import com.yaowan.server.game.model.log.entity.RoomCountLog;
import com.yaowan.server.game.model.log.entity.RoomRoleLogin;

/**
 * 
 * @author G_T_C
 */
@Service
public class RoomLogFunction extends FunctionAdapter {
	@Autowired
	private RoomRoleLoginDao roomRoleLoginDao;

	@Autowired
	private RoomRoleActiveDao roomRoleActiveDao;

	@Autowired
	private RoomRoleOnlineDao roleOnlineDao;

	@Autowired
	private RoomFunction roomFunction;

	@Autowired
	private ChargeDao chargeDao;

	@Autowired
	private RoomCountLogDao roomCountLogDao;

	private Map<Long, Map<Integer, Integer>> roomLoginCacheMap = new ConcurrentHashMap<>();

	@Autowired
	private RoleDao roleDao;

	/**
	 * 游戏房间在线人数日志
	 * 
	 * @author G_T_C
	 */
	public void onLineSta() {
		
		Map<Integer, Integer> map = roomFunction.onlineGet();
		
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			roleOnlineDao.addRoomRoleLineLog(entry.getKey(),
					entry.getValue());
		}
		
//		// 游戏房间在线人数日志
//		Field[] fields = GameType.class.getFields();
//		for (Field field : fields) {
//			field.setAccessible(true);
//			Integer gameType;
//			try {
//				gameType = (Integer) field.get(GameType.class);
//				if (gameType == GameType.DOUNIU) {
//					// 牛牛的房间可以增加的是
//					Map<Integer, Integer> map = new HashMap<Integer, Integer>();
//					for (Map.Entry<Integer, AtomicInteger> entry2 : roomFunction
//							.getRoomOnline(gameType).entrySet()) {
//						Integer roomType = entry2.getKey() / 10000;
//						Integer num = entry2.getValue().get();
//						Integer count = map.get(roomType);
//						if (count == null) {
//							map.put(roomType, num);
//						} else {
//							map.put(roomType, count + num);
//						}
//
//					}
//					int num = 0;
//					for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
//						num += entry.getValue().shortValue();
//					}
//					roleOnlineDao.addRoomRoleLineLog(gameType,
//							num);
//
//				} else {
//					int num = 0;
//					for (Map.Entry<Integer, AtomicInteger> entry2 : roomFunction
//							.getRoomOnline(gameType).entrySet()) {
//						num += entry2.getValue().get();
//						
//					}
//					roleOnlineDao.addRoomRoleLineLog(gameType,
//							num);
//				}
//
//			} catch (IllegalArgumentException e) {
//				LogUtil.error(e);
//			} catch (IllegalAccessException e) {
//				LogUtil.error(e);
//			}
//		}
	}

	/**
	 * 处理房间活跃
	 * 
	 * @author G_T_C
	 * @param player
	 * @param gameType
	 * @param roomType
	 */
	public void dealRoomRoleActiveLog(Role role, int gameType, int roomType) {
		long rid = role.getRid();
		// RoomRoleActive log = roomRoleActiveDao.getRoomRoleLog(gameType, rid,
		// roomType);
		// if (log != null) {
		// roomRoleActiveDao.updateJoinTime(gameType, rid, roomType,
		// TimeUtil.time());
		// } else {
		roomRoleActiveDao.addRoomRoleActiveLog((byte) gameType, rid, roomType);
		// }
	}

	/**
	 * 处理玩家登录房间日志
	 * 
	 * @author G_T_C
	 * @param gameType
	 * @param role
	 * @param cmd
	 *            0 为登录， 1为游戏结束，计算时长 2为更新匹配时间
	 */
	public void dealRoomRoleLoginLog(int gameType, Role role, String ip, int cmd) {
		RoomRoleLogin log = roomRoleLoginDao.findTodayRoomLoginLog(gameType,
				role.getRid());
		if (log == null) {
			int time = TimeUtil.time();
			roomRoleLoginDao.addRoomRoleLoginLog((byte) gameType, ip, time, 0,
					role.getRid(), (byte) 0,
					role.getVipTime() > time ? (byte) 1 : (byte) 0);
			Map<Integer, Integer> map = roomLoginCacheMap.get(role.getRid());
			if (map == null) {
				map = new HashMap<Integer, Integer>();
			}
			map.put(gameType, time);
			roomLoginCacheMap.put(role.getRid(), map);
		} else {
			if (cmd == 1) {
				// 更新时长和是否充值过和更新ip
				// log.setIp(ip);
				Map<Integer, Integer> map = roomLoginCacheMap
						.get(role.getRid());
				int lasttime = 0;
				if (map == null) {
					lasttime = log.getLoginTime();
				} else {
					if (map.get(gameType) != null) {
						lasttime = map.get(gameType);
					}

				}
				log.setOnlineTime(log.getOnlineTime()
						+ (TimeUtil.time() - lasttime));
				// 判断今天是否充值过
				Charge charge = chargeDao.getTodayChargeByRid(role.getRid());
				log.setTodayCharge(charge == null ? (byte) 0 : (byte) 1);
				log.setLoginTime(TimeUtil.time());
				roomRoleLoginDao.updateRoomRoleLog(log);
			} else if (cmd == 0) {
				// 更新登录时间更新ip
				/*
				 * log.setIp(ip); log.setLoginTime(TimeUtil.time());
				 * roomRoleLoginDao.updateRoomRoleLog(log);
				 */
				Map<Integer, Integer> map = roomLoginCacheMap
						.get(role.getRid());
				if (map == null) {
					map = new HashMap<Integer, Integer>();
				}
				map.put(gameType, TimeUtil.time());
				roomLoginCacheMap.put(role.getRid(), map);
			}
		}
	}

	/**
	 * 处理玩家进入房间日志
	 * 
	 */
	public void updateRoleMatchTime(long rid) {
		roomRoleActiveDao.updateMatchTime(rid);
	}


	@Override
	public void handleOnNextDay() {
		roomLoginCacheMap.clear();
	}

	/**
	 * 当日对局统计
	 * 
	 * @author G_T_C
	 * @param log
	 */
	public void dealRoomCountLog(int drawCount, int brokenCount, int tax,
			byte gameType, byte roomType,int generalBankerCount,int difficultyBankerCount,
			int generalPlayerCount,int difficultyPlayerCount,int choushui) {
		if (drawCount == 0 && brokenCount == 0 && tax == 0 && choushui == 0) {
			return;
		}

		RoomCountLog countLog = roomCountLogDao.findTodayRoomCountLog(gameType,
				roomType);
		if (countLog == null) {
			
			countLog = new RoomCountLog();
			countLog.setGameType(gameType);
			countLog.setRoomType(roomType);
			countLog.setDrawCount(drawCount);
			countLog.setBrokenCount(brokenCount);
			countLog.setTax(tax);
			countLog.setCreateTime(TimeUtil.time());
			countLog.setGeneralBankerCount(generalBankerCount);
			countLog.setDifficultyBankerCount(difficultyBankerCount);
			countLog.setGeneralPlayerCount(generalPlayerCount);
			countLog.setDifficultyPlayerCount(difficultyPlayerCount);
			countLog.setChoushui(choushui);
			roomCountLogDao.addRoomCountLog(countLog);
		} else {
			countLog.setCreateTime(TimeUtil.time());
			countLog.setBrokenCount(brokenCount + countLog.getBrokenCount());
			countLog.setDrawCount(drawCount + countLog.getDrawCount());
			countLog.setTax(tax + countLog.getTax());
			countLog.setGeneralBankerCount(generalBankerCount + countLog.getGeneralBankerCount());
			countLog.setDifficultyBankerCount(difficultyBankerCount + countLog.getDifficultyBankerCount());
			countLog.setGeneralPlayerCount(generalPlayerCount + countLog.getGeneralPlayerCount());
			countLog.setDifficultyPlayerCount(difficultyPlayerCount + countLog.getDifficultyPlayerCount());
			countLog.setChoushui(choushui + countLog.getChoushui());
			roomCountLogDao.updateCountLog(countLog);
		}
	}

	@Override
	public void handleOnRoleLogout(Role role) {
		roomLoginCacheMap.remove(role.getRid());
	}
}
