/**
 * 
 */
package com.yaowan.server.game.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.csv.cache.MissionCache;
import com.yaowan.csv.entity.MissionCsv;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GMission.GMissionData;
import com.yaowan.protobuf.game.GMission.GMsg_12016001;
import com.yaowan.protobuf.game.GMission.GMsg_12016002;
import com.yaowan.server.game.function.MissionFunction;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.data.entity.TaskDaily;
import com.yaowan.server.game.model.struct.MissionType;
import com.yaowan.server.game.model.struct.TaskType;

/**
 * 
 * @author YW0861
 *
 */
@Component
public class MissionService {

	@Autowired
	private MissionFunction missionFunction;

	@Autowired
	private MissionCache missionCache;

	/**
	 * 返回玩家角色对应的任务列表
	 * 
	 * @param player
	 */
	public void requestRoleMissions(Player player, int type)
	{
		Role role = player.getRole();
		Map<Integer, TaskDaily> map = null;
		switch (type)
		{
		case TaskType.daily_task:
			map = missionFunction.listRoleMissions(role.getRid());
			break;
		case TaskType.main_task:
			map = missionFunction.getMainTaskByRid(role.getRid());
			break;
		}

		GMsg_12016001.Builder builder = GMsg_12016001.newBuilder();
		if (map != null)
		{
			if (type == TaskType.main_task)
			{
				for (Map.Entry<Integer, TaskDaily> entry : map.entrySet())
				{ // 玩家缓存中所有的任务数据
					GMissionData.Builder build = GMissionData.newBuilder();
					List<Integer> missionList = new ArrayList<>();
					MissionCsv missionCsv = missionCache.getConfig(entry.getValue().getTaskId());
					if (missionCsv != null)
					{
						int mission = missionCsv.getMission();
						if (mission == MissionType.DIAMOND.ordinal())
						{
							build.setProcess(player.getRole().getDiamond());
							missionFunction.checkTaskFinish(role.getRid(), TaskType.main_task,
									MissionType.DIAMOND, role.getDiamond());
						}
						if (mission == MissionType.GOLD_NUM.ordinal())
						{
							build.setProcess(player.getRole().getGold());
							missionFunction.checkTaskFinish(role.getRid(), TaskType.main_task,
									MissionType.GOLD_NUM, role.getGold());
						}

						if (mission == MissionType.LOTTERIES.ordinal())
						{
							build.setProcess(player.getRole().getCrystal());
							missionFunction.checkTaskFinish(role.getRid(), TaskType.main_task,
									MissionType.LOTTERIES, role.getCrystal());
						}
						if (mission == MissionType.LEVEL.ordinal())
						{
							build.setProcess(player.getRole().getCrystal());
							missionFunction.checkTaskFinish(role.getRid(), TaskType.main_task,
									MissionType.LEVEL, role.getLevel());
						}
					}
					build.setTaskId(entry.getKey());
					build.setReward(entry.getValue().getReward());
					build.setProcess(entry.getValue().getProcess());
					builder.addMissionList(build);
				}
			} else
			{//日常任务需要排序
				Map<Integer, TaskDaily> missionMap = new TreeMap<Integer, TaskDaily>(
						new Comparator<Integer>() {
							public int compare(Integer obj1, Integer obj2)
							{
								return obj1.compareTo(obj2);
							}
						});
				missionMap.putAll(map);
				for (Map.Entry<Integer, TaskDaily> entry : missionMap.entrySet())
				{
					GMissionData.Builder build = GMissionData.newBuilder();
					build.setTaskId(entry.getKey());
					build.setReward(entry.getValue().getReward());
					build.setProcess(entry.getValue().getProcess());
					builder.addMissionList(build);
				}
			}
		}
		builder.setType(type);
		player.write(builder.build());
	}

	/**
	 * 用户上传taskId,领取任务奖励
	 * 
	 * @param player
	 */
	public void requestRewards(Player player, int taskId, int type)
	{
		LogUtil.info("TASK REWARDS :::: TASKID ->>>" + taskId);

		boolean b = missionFunction.giveRewards(player.getRole(), taskId); // 发放奖励
		int status = 0;
		if (b)
			status = 1; // 成功

		// 下发奖励领取结果
		GMsg_12016002.Builder builder = GMsg_12016002.newBuilder();
		builder.setStatus(status);
		builder.setTaskId(taskId);
		builder.setType(type);

		player.write(builder.build());

	}

	/* 任务类型判定常量 */
	public enum TYPE {

		/*
		 * 斗地主 1{ 1：王炸； 3：炸弹； }
		 */
		DDZ_WIN(1, 1), // 胜利
		DDZ_WIN_CONTINUED(1, 2), // 连胜
		DDZ_WIN_COUNTS(1, 3), // 完成次数
		DDZ_WANGZHA(1, 1), DDZ_WANGZHA_AND_ZHADAN(1, 2), DDZ_ZHADAN(1, 3),

		/*
		 * 麻将2{ 1：自摸； 2：胡； 3：杠； }
		 */
		MJ_WIN(2, 0), MJ_ZIMO(2, 1), MJ_HU(2, 2), MJ_GANG(2, 3),

		/*
		 * 闷鸡3{ 1：豹子； 2：金花； 3：同花顺； }
		 */
		MENJI_WIN(3, 0), MENJI_BAOZI(3, 1), MENJI_JINHUA(3, 2), MENGJI_TONGHUA(3, 3);

		// 成员变量
		private int t01;
		private int t02;

		// 构造方法
		private TYPE(int t01, int t02)
		{
			this.t01 = t01;
			this.t02 = t02;
		}

		/* 任务达成通知： 根据类型获得内部定义的任务编号，做计算用途 */
		public int $getNotifyTaskId()
		{
			Iterator<Integer> keys = TYPE_MAP_ENUM.keySet().iterator();
			int taskId = -1;
			while (keys.hasNext())
			{
				if (TYPE_MAP_ENUM.get(taskId = keys.next()).equals(this))
				{
					break;
				} else
				{
					taskId = -1;
				}
			}
			return taskId;

		}

		/**
		 * 胜利后关联的任务标号
		 * 
		 * @param type
		 * @return
		 */
		public List<Integer> $getWinTaskIds(TYPE type)
		{
			List<Integer> list = new LinkedList<Integer>();
			list.add(-1);
			int[] ids = WIN_IDS_MAP.get(type);
			if (ids != null && ids.length > 0)
				for (int id : ids)
					list.add(id);
			return list;

		}

		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * 任务类型数据关系维护 NOTE:日常任务配表数据有更新 需要同步更新定义到TYPE和map对象中
		 * 
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 */
		private final static ConcurrentMap<Integer, TYPE> TYPE_MAP_ENUM = new ConcurrentHashMap();
		private final static ConcurrentMap<TYPE, int[]> WIN_IDS_MAP = new ConcurrentHashMap();
		static
		{

			/* taskId,TYPE */// TODO ,数据和配表一致
			TYPE_MAP_ENUM.put(1/* "1_1" */, DDZ_WIN);
			TYPE_MAP_ENUM.put(1/* "1_1" */, DDZ_WANGZHA);
			TYPE_MAP_ENUM.put(2/* "1_2" */, TYPE.DDZ_WANGZHA_AND_ZHADAN);
			TYPE_MAP_ENUM.put(3/* "1_3" */, TYPE.DDZ_ZHADAN);

			TYPE_MAP_ENUM.put(4/* "2_1" */, TYPE.MJ_WIN);
			TYPE_MAP_ENUM.put(4/* "2_1" */, TYPE.MJ_ZIMO);
			TYPE_MAP_ENUM.put(5/* "2_2" */, TYPE.MJ_HU);
			TYPE_MAP_ENUM.put(6/* "2_3" */, TYPE.MJ_GANG);

			TYPE_MAP_ENUM.put(7/* "3_1" */, TYPE.MENJI_WIN);
			TYPE_MAP_ENUM.put(7/* "3_1" */, TYPE.MENJI_BAOZI);
			TYPE_MAP_ENUM.put(8/* "3_2" */, TYPE.MENJI_JINHUA);
			TYPE_MAP_ENUM.put(9/* "3_3" */, TYPE.MENGJI_TONGHUA);

			// 胜利条件下的关联任务集合
			WIN_IDS_MAP.put(DDZ_WIN, new int[] { 1, 2, 3 });
			WIN_IDS_MAP.put(MJ_WIN, new int[] { 4, 5, 6 });
			WIN_IDS_MAP.put(MENJI_WIN, new int[] { 7, 8, 9 });
		}
	}

	/* 任务计数器 */
	public static class MissionCounter {
		long rid;
		List<MissionCsv> list = null; // 我的任务列表
		// 当天游戏次数
		public int ymd; // 日期
		public int counts; // 游戏次数
		public boolean lastWin;// 上次胜利
		// 胜利次数,key:gameType,value:count
		ConcurrentMap<Integer, Integer> winCounts = new ConcurrentHashMap<Integer, Integer>();
		// 连胜次数，游戏，次数
		ConcurrentMap<Integer, Integer> winContinuedCounts = new ConcurrentHashMap<Integer, Integer>();

		public MissionCounter()
		{
			this.ymd = TimeUtil.getTodayYmd();
		}

		/* 获取连胜次数 */
		public int getWinContinuedCounts(int taskId)
		{
			return winContinuedCounts.get(taskId);
		}

		/* 更新数据 status :1 ：胜利 */
		public void updateCounts(int taskId, int status)
		{
			/* 数据初始化、检查******************************* */
			if (TimeUtil.getTodayYmd() > this.ymd)
			{
				LogUtil.debug("/////////taskId : " + taskId + ",status :" + status
						+ "MissionCounter init ");
				winCounts.clear();
				winContinuedCounts.clear();

			}

			if (winCounts.get(taskId) == null)
			{
				winCounts.put(taskId, 0);
				LogUtil.debug("//// init winCounts >>>taskId : " + taskId);
			}

			if (winContinuedCounts.get(taskId) == null)
			{
				winContinuedCounts.put(taskId, 0);
				LogUtil.debug("//// init winContinuedCounts >>>gameId : " + taskId);
			}
			/******************************************/

			counts++;

			if (status != 1)
			{
				lastWin = false;
				// 连胜次数归零
				winContinuedCounts.remove(taskId);
			} else if (status == 1)
			{
				int c = winCounts.get(taskId) + 1;
				winCounts.remove(taskId);
				winCounts.put(taskId, c);
				// 胜利次数，连续胜利次数
				if (lastWin)
				{
					c = winContinuedCounts.get(taskId) + 1;
					winContinuedCounts.remove(taskId);
					winContinuedCounts.put(taskId, c);
				}

				lastWin = true;
			}
		}

	}

	public void uploadPhotoTask(Player player)
	{
		missionFunction.checkTaskFinish(player.getRole().getRid(), TaskType.main_task,
				MissionType.PHOTO, "");
	}

	public void weiXinShare(Player player, int type)
	{
		switch (type)
		{
		case 1:
		{
			missionFunction.checkTaskFinish(player.getRole().getRid(), TaskType.daily_task,
					MissionType.SHARE_WX_FRIEND, "");
			break;
		}
		case 2:
		{
			missionFunction.checkTaskFinish(player.getRole().getRid(), TaskType.daily_task,
					MissionType.SHARE_WX_QUAN, "");
			break;
		}

		}

	}

}
