package com.yaowan.server.game.function;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameType;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.LotteryTaskCache;
import com.yaowan.csv.cache.LotteryTaskTimesCache;
import com.yaowan.csv.cache.MissionCache;
import com.yaowan.csv.cache.MissionGroupCache;
import com.yaowan.csv.entity.LotteryTaskCsv;
import com.yaowan.csv.entity.LotteryTaskTimesCsv;
import com.yaowan.csv.entity.MissionCsv;
import com.yaowan.csv.entity.MissionGroupCsv;
import com.yaowan.framework.util.JSONObject;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.ItemGet;
import com.yaowan.protobuf.game.GMenJi.MJCardType;
import com.yaowan.protobuf.game.GMission.GMsg_12016004;
import com.yaowan.protobuf.game.GRole.GMsg_12002015;
import com.yaowan.protobuf.game.GRole.GRedPoint;
import com.yaowan.server.game.model.data.dao.RoleLotteryTaskDao;
import com.yaowan.server.game.model.data.dao.TaskDailyDao;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.data.entity.RoleLotteryTask;
import com.yaowan.server.game.model.data.entity.TaskDaily;
import com.yaowan.server.game.model.log.dao.TaskLogDao;
import com.yaowan.server.game.model.log.entity.TaskLog;
import com.yaowan.server.game.model.struct.CardType;
import com.yaowan.server.game.model.struct.MissionType;
import com.yaowan.server.game.model.struct.TaskType;

/**
 * 
 * @author zane
 *
 */
@Component
public class MissionFunction extends FunctionAdapter {

	@Autowired
	private ItemFunction itemFunction;

	@Autowired
	private TaskDailyDao taskDailyDao;

	@Autowired
	private MissionCache missionCache;

	@Autowired
	private MissionGroupCache missionGroupCache;

	@Autowired
	RoleFunction roleFunction;

	@Autowired
	private LotteryTaskCache lotteryTaskCache;

	@Autowired
	private LotteryTaskTimesCache lotteryTaskTimesCache;

	@Autowired
	private RoleLotteryTaskDao roleLotteryTaskDao;
	
	@Autowired
	private TaskLogDao taskLogDao;

	/* 缓存玩家对应的已经完成任务列表 */
	private ConcurrentHashMap<Long, Map<Integer, TaskDaily>> dbTaskCacheMap = new ConcurrentHashMap<>();

	/**
	 * 奖券任务、、wan
	 */
	private Map<Long, Map<Integer, Integer>> lottryCountCache = new ConcurrentHashMap<>();

	/**
	 * 主线任务
	 */
	private Map<Long, Map<Integer, TaskDaily>> mainTaskCache = new ConcurrentHashMap<>();

	// 用户登录内存加载
	@Override
	public void handleOnRoleLogin(Role role) {
		/************************** 日常任务 *********************************/
		int ymd = TimeUtil.getTodayYmd();
		for (Map.Entry<Integer, TaskDaily> subEntry : listRoleMissions(
				role.getRid()).entrySet()) {
			if (subEntry.getValue().getYmd() != ymd) {
				taskDailyDao.resetRoleTask(role.getRid());
				dbTaskCacheMap.put(role.getRid(), genTask(role.getRid()));
			}
			break;
		}

		/************************** 奖券任务数量 *********************************/
		RoleLotteryTask lotteryTask = roleLotteryTaskDao.findByRidGameType(
				role.getRid(), GameType.DOUDIZHU);
		doLotteryCountCache(role, lotteryTask, GameType.DOUDIZHU);
		lotteryTask = roleLotteryTaskDao.findByRidGameType(role.getRid(),
				GameType.MAJIANG);
		doLotteryCountCache(role, lotteryTask, GameType.MAJIANG);

		/************************** 主线任务 *********************************/
		crateMainTask(role);
		
	}

	private void doLotteryCountCache(Role role, RoleLotteryTask lotteryTask,
			int gameType) {
		Map<Integer, Integer> countMap = lottryCountCache.get(role.getRid());
		if (countMap == null) {
			countMap = new HashMap<>();
		}
		if (lotteryTask == null) {
			lotteryTask = new RoleLotteryTask();
			lotteryTask.setComplementTimes(0);
			lotteryTask.setGameType(gameType);
			lotteryTask.setRid(role.getRid());
			roleLotteryTaskDao.insert(lotteryTask);
			role.setLotteryTaskId(0);
			countMap.put(gameType, 0);
		} else {
			LotteryTaskTimesCsv lotteryTaskTimesCsv = lotteryTaskTimesCache
					.getConfig(gameType);
			if (lotteryTaskTimesCsv == null) {
				role.setLotteryTaskId(0);
			} else if (lotteryTask.getComplementTimes() >= lotteryTaskTimesCsv
					.getCompletionTimes()) {
				role.setLotteryTaskId(-1);
			} else {
				role.setLotteryTaskId(0);
			}
			countMap.put(gameType, lotteryTask.getComplementTimes());
		}
		lottryCountCache.put(role.getRid(), countMap);
	}

	@Override
	public void handleOnRoleLogout(Role role) {
		dbTaskCacheMap.remove(role.getRid());
		lottryCountCache.remove(role.getRid());
		mainTaskCache.remove(role.getRid());
	}

	//
	@Override
	public void handleOnNextDay() {
		LogUtil.info("start refresh task");
		// 在线的更新
		taskDailyDao.resetAllTask();
		for (Map.Entry<Long, Map<Integer, TaskDaily>> entry : dbTaskCacheMap
				.entrySet()) {
			entry.setValue(genTask(entry.getKey()));
		}
		LogUtil.info("refresh task done");

		roleLotteryTaskDao.resetAllTask();
		for (Map.Entry<Long, Map<Integer, Integer>> entry : lottryCountCache
				.entrySet()) {
			Role role = roleFunction.getRoleByRid(entry.getKey());
			doLotteryCountCache(role, null, GameType.DOUDIZHU);
			doLotteryCountCache(role, null, GameType.MAJIANG);
		}
		LogUtil.info("refresh  lottery task done");

	}

	/**
	 * 路由主线任务放置到缓存。配置表新加主线任务就添加到数据库。
	 * 
	 * @author G_T_C
	 * @param rid
	 * @return
	 */
	public void crateMainTask(Role role) {
		long rid = role.getRid();
		Map<Integer, TaskDaily> result = mainTaskCache.get(rid);
		if (result != null) {
			return;
		}
		result = new HashMap<Integer, TaskDaily>();
		Map<Integer, MissionCsv> maincsvCache = missionCache.getMainTaskCache();

		if (maincsvCache.isEmpty()) {
			mainTaskCache.put(rid, result);
			return;
		}
		Map<Integer, TaskDaily> map = taskDailyDao.getTodayTaskList(rid,
				TaskType.main_task);
		boolean hasLevelTask = false;
		boolean hasGoldTask = false;
		boolean hasDomaindTask = false;
		boolean hasLotteryTask = false;
		// 判断配置表是否新的任务。有插进数据库，没有，把已经完成的任务移除
		for (Integer key : maincsvCache.keySet()) {
			MissionCsv csv = maincsvCache.get(key);
			TaskDaily task = map.get(key);
			if (task == null && csv.getFrontTask() == 0) {
				// 配置表新加的主线任务。
				createMainTask(role, result, csv);
				if (csv.getMission() == MissionType.LEVEL.ordinal()) {
					hasLevelTask = true;
				}
				if (csv.getMission() == MissionType.GOLD_NUM.ordinal()) {
					hasGoldTask = true;
				}
				if (csv.getMission() == MissionType.DIAMOND.ordinal()) {
					hasDomaindTask = true;
				}
				if (csv.getMission() == MissionType.LOTTERIES.ordinal()) {
					hasLotteryTask = true;
				}
			}
			if (task != null && task.getReward() != 2) {
				result.put(task.getTaskId(), task);
			}
		}
		mainTaskCache.put(rid, result);
		if (hasLevelTask) {
			checkTaskFinish(rid, TaskType.main_task, MissionType.LEVEL,
					role.getLevel());
		}
		if (hasGoldTask) {
			checkTaskFinish(rid, TaskType.main_task, MissionType.GOLD_NUM,
					role.getGold());
		}
		if (hasDomaindTask) {
			checkTaskFinish(rid, TaskType.main_task, MissionType.DIAMOND,
					role.getDiamond());
		}
		if (hasLotteryTask) {
			checkTaskFinish(rid, TaskType.main_task, MissionType.LOTTERIES,
					role.getCrystal());
		}
	}

	public Map<Integer, TaskDaily> getMainTaskByRid(long rid) {
		Map<Integer, TaskDaily> map = mainTaskCache.get(rid);
		return map;
	}

	private void createMainTask(Role role, Map<Integer, TaskDaily> result,
			MissionCsv csv) {
		long rid = role.getRid();
		TaskDaily t = new TaskDaily(rid, csv.getID(),
				(short) TaskType.main_task);
		taskDailyDao.insert(t);
		result.put(csv.getID(), t);
		
		//插入日志
		if(taskLogDao.findCount(rid,t.getTaskId(),0) == 0){
			TaskLog log = new TaskLog(rid, t.getTaskId(), (byte)TaskType.main_task, TimeUtil.time(), (byte)0);
			taskLogDao.insert(log);
		}
	}

	public Map<Integer, TaskDaily> genTask(long rid) {
		Map<Integer, TaskDaily> map = new HashMap<Integer, TaskDaily>();
		MissionGroupCsv missionGroupCsv = missionGroupCache.rand();
		for (Integer id : missionGroupCsv.getMissionList()) {
			MissionCsv csv = missionCache.getConfig(id);
			if (csv != null) {
				TaskDaily t = new TaskDaily(rid, csv.getID(),
						(short) TaskType.daily_task);
				taskDailyDao.insert(t);
				TaskLog log = new TaskLog(rid, t.getTaskId(), (byte)TaskType.daily_task, TimeUtil.time(), (byte)0);
				taskLogDao.insert(log);
				map.put(csv.getID(), t);
			}

		}
		return map;
	}

	/** 玩家缓存的任务列表数据 */
	public Map<Integer, TaskDaily> listRoleMissions(long rid) {
		Map<Integer, TaskDaily> map = dbTaskCacheMap.get(rid);
		if (map == null) {
			Role role = roleFunction.getRoleByRid(rid);
			if (role == null) {
				map = new HashMap<Integer, TaskDaily>();
			} else {
				map = taskDailyDao.getTodayTaskList(rid, TaskType.daily_task);
				// TODO 今日任务要有插入
				if (map.size() == 0) {
					map = genTask(rid);
				}
			}

			dbTaskCacheMap.put(rid, map);

		}
		return dbTaskCacheMap.get(rid);
	}

	/**
	 * 更新玩家日常任务次数 外部模块调用方法！
	 * 
	 * @param rid
	 *            角色编号
	 * @param dailyId
	 *            配表任务编号
	 * @param status
	 *            状态参数： 0 ：通用 1: 胜利 2: 失败
	 */

	public boolean checkTaskFinish(long rid, int taskType, MissionType type,
			Object... param) {
		// 保存数据库
		boolean save = false;
		int redType = 1;
		switch (taskType) {
		case TaskType.daily_task:
			save = checkDailyTaskCompleted(rid, type, save, param);
			redType = 3;
			break;
		case TaskType.main_task:
			save = checkMainTaskCompleted(rid, type, save, param);
			break;
		}

		if (save) {
			sendRedPoint(rid,redType,1);

		}

		return save;

	}

	private void sendRedPoint(long rid,int type, int status) {
		GMsg_12002015.Builder builder = GMsg_12002015.newBuilder();
		GRedPoint.Builder red = GRedPoint.newBuilder();
		red.setType(type);
		red.setStatus(status);
		builder.setRedPoint(red);
		roleFunction.sendMessageToPlayer(rid, builder.build());
	}

	/**
	 * 检测主线任务是否完成
	 * 
	 * @author G_T_C
	 * @param rid
	 * @param type
	 * @param save
	 * @param param
	 * @return
	 */
	private boolean checkMainTaskCompleted(long rid, MissionType type,
			boolean save, Object[] param) {
		Map<Integer, TaskDaily> map = getMainTaskByRid(rid);
		if (map == null) {
			return save;
		}
		for (Integer key : map.keySet()) {
			TaskDaily taskDaily = map.get(key);
			if (taskDaily.getReward() != 0) {
				continue;
			}
			MissionCsv missionCsv = missionCache.getConfig(taskDaily
					.getTaskId());
			if (missionCsv == null) {
				continue;
			}
			if (missionCsv.getMission() != type.ordinal()) {
				continue;
			}
			int oldProcess = taskDaily.getProcess();
			int csvMission = missionCsv.getMission();
			if (csvMission == MissionType.LEVEL.ordinal()) {
				short level = (short) param[0];
				taskDaily.setProcess(level);
			} else if (csvMission == MissionType.PHOTO.ordinal()) {
				if (taskDaily.getProcess() == 0) {
					taskDaily.setProcess(1);
				}
			} else if (csvMission == MissionType.GOLD_NUM.ordinal()) {
				int gold = (Integer) param[0];
				taskDaily.setProcess(gold);
			} else if (csvMission == MissionType.DIAMOND.ordinal()) {
				int diamond = (Integer) param[0];
				taskDaily.setProcess(diamond);
			} else if (csvMission == MissionType.LOTTERIES.ordinal()) {
				int lottery = (Integer) param[0];
				taskDaily.setProcess(lottery);
			} else if (csvMission == MissionType.SHOP_BUY.ordinal()) {
				taskDaily.setProcess(taskDaily.getProcess() + 1);
			} else if (csvMission == MissionType.LOTTER_EXCHANGE.ordinal()) {
				taskDaily.setProcess(taskDaily.getProcess() + 1);
			}
			save = updateProcess(save, taskDaily, oldProcess, missionCsv,TaskType.main_task);
		}
		return save;
	}

	/**
	 * 检测日常任务是否完成
	 * 
	 * @param rid
	 * @param type
	 * @param save
	 * @param param
	 * @return
	 */
	private boolean checkDailyTaskCompleted(long rid, MissionType type,
			boolean save, Object... param) {
		Map<Integer, TaskDaily> map = listRoleMissions(rid);
		for (Map.Entry<Integer, TaskDaily> entry : map.entrySet()) {
			TaskDaily taskDaily = entry.getValue();
			int oldProcess = taskDaily.getProcess();
			if (taskDaily.getReward() == 0) {
				MissionCsv missionCsv = missionCache.getConfig(taskDaily
						.getTaskId());
				if (missionCsv == null) {
					continue;
				}
				if (missionCsv.getMission() == type.ordinal()) {// 类型匹配
					if (missionCsv.getMission() == MissionType.SHARE_WX_QUAN
							.ordinal()) {
						taskDaily.setProcess(taskDaily.getProcess() + 1);
					} else if (missionCsv.getMission() == MissionType.SHARE_WX_FRIEND
							.ordinal()) {
						taskDaily.setProcess(taskDaily.getProcess() + 1);
					} else if (missionCsv.getMission() == MissionType.SHARE_QQ
							.ordinal()) {
						taskDaily.setProcess(taskDaily.getProcess() + 1);
					} else if (missionCsv.getMission() == MissionType.WIN
							.ordinal()) {
						Integer gameType = (Integer) param[0];
						if (gameType == missionCsv.getGameType()) {
							taskDaily.setProcess(taskDaily.getProcess() + 1);
						}
					} else if (missionCsv.getMission() == MissionType.CONTINUE_WIN
							.ordinal()) {
						Integer gameType = (Integer) param[0];
						if (gameType == missionCsv.getGameType()) {
							Boolean flag = (Boolean) param[1];
							if (flag) {
								taskDaily
										.setProcess(taskDaily.getProcess() + 1);
							} else {
								taskDaily.setProcess(0);
							}
						}
					} else if (missionCsv.getMission() == MissionType.TIMES
							.ordinal()) {
						Integer gameType = (Integer) param[0];
						if (gameType == missionCsv.getGameType()) {
							taskDaily.setProcess(taskDaily.getProcess() + 1);
						}
					} else if (missionCsv.getMission() == MissionType.CARD_TYPE
							.ordinal()) {
						Integer gameType = (Integer) param[0];
						if (gameType == missionCsv.getGameType()) {
							if (gameType == GameType.MENJI) {
								MJCardType cardType = (MJCardType) param[1];
								if (missionCsv.getHandPatterns() == cardType
										.getNumber()) {
									taskDaily
											.setProcess(taskDaily.getProcess() + 1);
								}
							} else if (gameType == GameType.DOUDIZHU) {
								CardType cardType = (CardType) param[1];
								if (missionCsv.getHandPatterns() == cardType
										.ordinal()) {
									taskDaily
											.setProcess(taskDaily.getProcess() + 1);
								}
							} else if (gameType == GameType.MAJIANG || gameType == GameType.ZXMAJIANG || gameType == GameType.CDMAJIANG) {
								List<Integer> list = (List<Integer>) param[1];
								for (Integer winType : list) {
									if (missionCsv.getHandPatterns() == winType
											.intValue()) {
										taskDaily.setProcess(taskDaily
												.getProcess() + 1);
										break;
									}
								}
							}
						}
					} else if (missionCsv.getMission() == MissionType.LOGIN
							.ordinal()) {
						// 登录时间任务
						List<Integer[]> list = missionCsv.getTimeQuantumList();
						Calendar startTime = Calendar.getInstance();
						startTime.set(Calendar.HOUR_OF_DAY, list.get(0)[0]);
						startTime.set(Calendar.MINUTE, list.get(0)[1]);

						Calendar endTime = Calendar.getInstance();
						endTime.set(Calendar.HOUR_OF_DAY, list.get(1)[0]);
						endTime.set(Calendar.MINUTE, list.get(1)[1]);

						long time = System.currentTimeMillis();
						if (time >= startTime.getTimeInMillis()
								&& time <= endTime.getTimeInMillis()) {
							taskDaily.setProcess(taskDaily.getProcess() + 1);
						}
					} else if (missionCsv.getMission() == MissionType.GANG
							.ordinal()) {
						taskDaily.setProcess(taskDaily.getProcess() + 1);
					}else if(missionCsv.getMission() == MissionType.SHARE_WX_FRIEND
							.ordinal()){
						taskDaily.setProcess(1);
					}else if(missionCsv.getMission() == MissionType.SHARE_WX_QUAN
							.ordinal()){
						taskDaily.setProcess(1);
					}
				}

				save = updateProcess(save, taskDaily, oldProcess, missionCsv,TaskType.daily_task);
			}

		}
		return save;
	}

	/**
	 * 检查是否完成后，更新进度到数据库
	 * 
	 * @author G_T_C
	 * @param save
	 * @param taskDaily
	 * @param oldProcess
	 * @param missionCsv
	 * @return
	 */
	private boolean updateProcess(boolean save, TaskDaily taskDaily,
			int oldProcess, MissionCsv missionCsv, int type) {
		if (taskDaily.getProcess() >= missionCsv.getCount()) {
			taskDaily.setReward((byte) 1);
			save = true;
			//插入日志
			//if(type == TaskType.main_task){
				TaskLog log = new TaskLog(taskDaily.getRid(), taskDaily.getTaskId(), (byte)type, TimeUtil.time(), (byte)1);
				taskLogDao.insert(log);
			//}
			
		}
		if (oldProcess != taskDaily.getProcess()) {
			// 更新到数据库
			taskDailyDao.updateTaskDaily(taskDaily.getRid(),
					taskDaily.getTaskId(), taskDaily.getProcess(),
					taskDaily.getReward());
		
		}
		return save;
	}

	/**
	 * 查询玩家已完成的任务数据
	 * 
	 * @param rid
	 * @param taskId
	 * @return
	 */
	private TaskDaily getTask(long rid, int taskId, int taskType) {
		TaskDaily task = null;
		switch (taskType) {
		case TaskType.daily_task:
			task = dbTaskCacheMap.get(rid).get(taskId);
			break;
		case TaskType.main_task:
			task = mainTaskCache.get(rid).get(taskId);
			break;
		}
		return task;
	}

	/**
	 * 客户端请求领取奖励 检查玩家的task是否完成，如果完成，则进行奖励发放 1、金币类型 2、钻石类型 3、背包道具物品类型
	 * 
	 * @param rid
	 * @param taskId
	 */
	public boolean giveRewards(Role role, int taskId) {
		long rid = role.getRid();

		// 检查用户基础任务数据

		MissionCsv missionCsv = missionCache.getConfig(taskId);
		if (missionCsv == null) {
			LogUtil.warn("没有该任务 id=" + taskId);
			return false;
		}
		int taskType = missionCsv.getTaskType();
		ItemGet item = new ItemGet();
		item.setId(missionCsv.getRewards());
		item.setNum(missionCsv.getRewardsCount());

		// 1、调用公共接口发放奖励
		List<ItemGet> list = new LinkedList<ItemGet>();
		list.add(item);
		MoneyEvent event = null;
		switch (taskType) {
		case  TaskType.daily_task:{
			event = MoneyEvent.DAILYTASK; 
			break;
		}
		case  TaskType.main_task:{
			event = MoneyEvent.MAINTASK; 
			break;
		}
		}
		boolean b = itemFunction.getItems(role, JSONObject.encode(list),
				event);
		/* 更新缓存数据 更新数据库 */
		if (b) {
			TaskDaily task = getTask(rid, taskId, taskType);
			if (task.getReward() == 1) {
				task.setReward((byte) 2);
				taskDailyDao.updateTaskDaily(rid, taskId, task.getProcess(),
						task.getReward());
			} else if (task.getReward() == 0
					&& (missionCsv.getMission() == MissionType.SHARE_WX_QUAN
							.ordinal()
							|| missionCsv.getMission() == MissionType.SHARE_WX_FRIEND
									.ordinal() || missionCsv.getMission() == MissionType.SHARE_QQ
							.ordinal())) {
				task.setReward((byte) 2);
				task.setProcess((byte) 1);
				taskDailyDao.updateTaskDaily(rid, taskId, task.getProcess(),
						task.getReward());
			}

			boolean flag = true;
			int redType = 1;
			switch (taskType) {
			case TaskType.daily_task: {
				Map<Integer, TaskDaily> result = dbTaskCacheMap.get(rid);
				if (result == null) {
					result = new HashMap<>();
				} else {
					//插入日志
					TaskLog log = new TaskLog(task.getRid(), task.getTaskId(), (byte)TaskType.daily_task, TimeUtil.time(), (byte)2);
					taskLogDao.insert(log);
					result.remove(task.getTaskId());
				}
				for (Map.Entry<Integer, TaskDaily> entry : listRoleMissions(
						role.getRid()).entrySet()) {
					if (entry.getValue().getReward() == 1) {
						flag = false;
						break;
					}
				}
				if (missionCsv.getPostTask() != 0) {
					// 配置表新加的日常任务。
					MissionCsv csv = missionCache.getConfig(missionCsv
							.getPostTask());
					TaskDaily t = new TaskDaily(rid, csv.getID(),
							(short) TaskType.daily_task);
					taskDailyDao.insert(t);
					result.put(t.getTaskId(), t);
					TaskLog log = new TaskLog(t.getRid(), t.getTaskId(), (byte)TaskType.daily_task, TimeUtil.time(), (byte)0);
					taskLogDao.insert(log);
				}
				redType = 3;
				break;
			}
			case TaskType.main_task: {
				Map<Integer, TaskDaily> result = mainTaskCache.get(rid);
				if (result == null) {
					result = new HashMap<>();
				} else {
					//插入日志
					TaskLog log = new TaskLog(task.getRid(), task.getTaskId(), (byte)TaskType.main_task, TimeUtil.time(), (byte)2);
					taskLogDao.insert(log);
					result.remove(task.getTaskId());
				}
				for (Map.Entry<Integer, TaskDaily> entry : getMainTaskByRid(
						role.getRid()).entrySet()) {
					if (entry.getValue().getReward() == 1) {
						flag = false;
						break;
					}
				}
				if (missionCsv.getPostTask() != 0) {
					// 配置表新加的主线任务。
					MissionCsv csv = missionCache.getConfig(missionCsv
							.getPostTask());
					TaskDaily t = new TaskDaily(rid, csv.getID(),
							(short) TaskType.main_task);
					taskDailyDao.insert(t);
					result.put(t.getTaskId(), t);
					LogUtil.info("next main taskId="+t.getTaskId()+", rid = "+rid);
					//插入日志
					TaskLog log = new TaskLog(t.getRid(), t.getTaskId(), (byte)TaskType.main_task, TimeUtil.time(), (byte)0);
					taskLogDao.insert(log);
				}
				break;
			}

			}

			if (flag) {
				sendRedPoint(rid,redType,0);
				LogUtil.info("no reward" + role.getNick());
			}

		} else {
			LogUtil.warn("领取奖励 > 调用getItems接口异常,rid:" + rid + " ， json:"
					+ item.toString());
		}

		return b;
	}

	/**
	 * 检测奖券任务
	 * 
	 * @author G_T_C
	 * @param taskId
	 *            奖券的任务id
	 * @param taskType
	 *            奖券的任务类型
	 * @param countCondition
	 *            奖券的数量条件
	 */
	public void checkLotteryTaskCompleted(Role role, int taskType,
			int countCondition) {
		int taskId = role.getLotteryTaskId();
		Long rid = role.getRid();
		if (taskId == -1 || taskId == 0) {// 如果等于-1，代表今天已经完成了，0为没有奖券任务
			return;
		}
		LotteryTaskCsv taskcsv = lotteryTaskCache.getConfig(taskId);
		if (taskcsv == null) {
			LogUtil.warn("没有该任务 id=" + taskId);
			return;
		}
		// 判断是不是本场任务
		if (taskType != taskcsv.getTaskType()) {
			return;
		}
		/*
		 * 开始处理任务。 1.检查数量条件是否满足 2.发送任务完成信息， 3.发送奖励
		 * 4.更新完成次数，判断是否已经达到今日最大次数。是就set为-1
		 */
		if (countCondition < taskcsv.getCount()) {
			return;
		}

		GMsg_12016004.Builder builder = GMsg_12016004.newBuilder();
		builder.setFlag(0);
		builder.setTaskId(taskId);
		roleFunction.sendMessageToPlayer(rid, builder.build());
		ItemGet item = new ItemGet();
		item.setId(taskcsv.getRewards());
		item.setNum(taskcsv.getRewardsCount());

		// 1、调用公共接口发放奖励
		List<ItemGet> list = new LinkedList<ItemGet>();
		list.add(item);
		itemFunction.getItems(role, JSONObject.encode(list), MoneyEvent.LOTTERYTASK);
		Map<Integer, Integer> countMap = lottryCountCache.get(role.getRid());
		int gameType = taskcsv.getAscriptionGameId();
		if (countMap.get(gameType) == null) {
			RoleLotteryTask lotteryTask = roleLotteryTaskDao.findByRidGameType(
					role.getRid(), gameType);
			doLotteryCountCache(role, lotteryTask, gameType);
		}
		int times = countMap.get(gameType) + 1;
		if (times >= lotteryTaskTimesCache.getConfig(gameType)
				.getCompletionTimes()) {
			role.setLotteryTaskId(-1);
		} else {
			role.setLotteryTaskId(0);
		}
		LogUtil.info("奖券任务完成 id=" + taskId + ", rid=" + role.getRid());
		countMap.put(gameType, times);
		roleLotteryTaskDao.updateCompletmetTimes(role.getRid(), gameType);
	}

}
