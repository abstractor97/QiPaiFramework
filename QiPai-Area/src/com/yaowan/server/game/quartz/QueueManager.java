package com.yaowan.server.game.quartz;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameType;
import com.yaowan.core.function.FunctionManager;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.push.PushFunction;
import com.yaowan.scheduler.SchedulerBean;
import com.yaowan.server.game.function.CDMajiangFunction;
import com.yaowan.server.game.function.DouniuFunction;
import com.yaowan.server.game.function.FriendRoomFunction;
import com.yaowan.server.game.function.MatchFunction;
import com.yaowan.server.game.function.NoticeFunction;
import com.yaowan.server.game.function.RankingListFunction;
import com.yaowan.server.game.function.RedBagFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.function.RoomLogFunction;
import com.yaowan.server.game.function.ZTDoudizhuFunction;
import com.yaowan.server.game.function.ZTMajiangFunction;
import com.yaowan.server.game.function.ZTMenjiFunction;
import com.yaowan.server.game.function.ZXMajiangFunction;
import com.yaowan.server.game.main.NettyClient;
import com.yaowan.server.game.model.log.dao.SlowLogicDao;

/**
 * 定时调度器
 */
@Component
public class QueueManager {

	@Autowired
	private SchedulerBean schedulerBean;

	@Autowired
	private ZTMenjiFunction menjiFunction;
	
	@Autowired
	private DouniuFunction douniuFunction;

	@Autowired
	private ZTMajiangFunction majiangFunction;
	
	@Autowired
	private ZXMajiangFunction zxmajiangFunction;
	
	@Autowired
	private CDMajiangFunction cdmajiangFunction;

	@Autowired
	private ZTDoudizhuFunction doudizhuFunction;

	@Autowired
	private RoomFunction roomFunction;

	@Autowired
	private SlowLogicDao slowLogicDao;

	@Autowired
	private NoticeFunction noticeFunction;

	@Autowired
	private ZTMenjiFunction ztMenjiFunction;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private RoomLogFunction roomLogFunction;
	
	@Autowired
	private RankingListFunction rankingListFunction;
	
	@Autowired
	private FriendRoomFunction friendRoomFunction;

	@Autowired
	private MatchFunction matchFunction;
	
	@Autowired
	private RedBagFunction redBagFunction;
	
	@Autowired
	private PushFunction pushFunction;
	
	public void start() {
		// 注册各个扫描线程
		submitQueueThread();
		submitNoticeThread();
		submitMenjiThread();
		submitRoomThread();
		submitAIThread();
		submitFriendRoomThread();
		submitRedbagThread();
		submitPushThread();
	}

	public void stop() {
		schedulerBean.cancel("MATCH_THREAD");
		schedulerBean.cancel("MATCHING_THREAD");
		schedulerBean.cancel("DAILY_THREAD");
		schedulerBean.cancel("NOTICE_THREAD");
		schedulerBean.cancel("MENJI_RESET");
		schedulerBean.cancel("MENJILOOK_THREAD");
		schedulerBean.cancel("ROOM_THREAD");
		schedulerBean.cancel("AI_THREAD");
		schedulerBean.cancel("GAMEMATCH_THREAD");
		schedulerBean.cancel("FRIENDROOM_THREAD");
		schedulerBean.cancel("FRIENDROOM_CLEAR_THREAD");
		schedulerBean.cancel("PUSH_THREAD");
		
	}

	static int index = 1;
	
	
	/**
	 * AI线程
	 */
	public void submitAIThread(){
		schedulerBean.submit("AITHREAD", TimeUtil.ONE_SECOND * 1000,
				new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						roomFunction.AIBye();
					}

				});
	}
	
	/**
	 * 房间线程
	 */
	private void submitRoomThread() {
		schedulerBean.submit("ROOM_THREAD", TimeUtil.ONE_SECOND * 1000,
				new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						//roomFunction.removeReadyDeque();
					}

				});
	}

	/**
	 * 公告线程
	 */
	private void submitNoticeThread() {
		schedulerBean.submit("NOTICE_THREAD", TimeUtil.ONE_SECOND * 5000,
				new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						noticeFunction.systemSendNOticeToAll();
					}

				});
	}

	/**
	 * 焖鸡线程
	 */
	private void submitMenjiThread() {
		schedulerBean.submit("MENJI_RESET", TimeUtil.ONE_HOUR * 1000,
				new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						ztMenjiFunction.resetBonus();
					}

				});
	}
	
	/**
	 * 好友房线程
	 */
	private void submitFriendRoomThread(){
		schedulerBean.submit("FRIENDROOM_CLEAR_THREAD", TimeUtil.ONE_SECOND * 1000,
				new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						friendRoomFunction.clearScheduler();
						friendRoomFunction.clearRoomOvertime();
					}
					
				});
	}
	
	/**
	 * 红包线程
	 */
	private void submitRedbagThread(){
		schedulerBean.submit("RED_BAG_THREAD", TimeUtil.ONE_SECOND * 1000,
				new Runnable() {
					@Override
					public void run() {
						redBagFunction.checkRedBag();
						redBagFunction.checkRedBagEnd();
					}

				});
	}
	
	/**
	 * 定时推送线程
	 */
	private void submitPushThread(){
		schedulerBean.submit("PUSH_THREAD",  PushFunction.getPushTime()
				- System.currentTimeMillis() + 5000,
				TimeUtil.ONE_DAY * 1000,
				new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						pushFunction.sendMessage();
					}
				});
	}

	/**
     *
     */
	private void submitQueueThread() {
		// 游戏处理线程
		schedulerBean.submit("FRIENDROOM_THREAD", TimeUtil.ONE_SECOND * 1000,
				new Runnable() {
					@Override
					public void run() {
						long time = System.currentTimeMillis();
						roomFunction.dealRuningGames();
						friendRoomFunction.dealRunningGames();
						
						// 麻将处理

						menjiFunction.autoAction();
						if (System.currentTimeMillis() - time > 3000) {
							slowLogicDao
									.addSlowLogic(
											"MATCH_THREAD",
											time,
											(int) ((System.currentTimeMillis() - time) / 1000),
											0);
							LogUtil.info("循环消耗"
									+ (System.currentTimeMillis() - time));
						}

					}
				});
		
		schedulerBean.submit("MENJILOOK_THREAD", TimeUtil.ONE_SECOND * 1000,
				new Runnable() {
					@Override
					public void run() {
						long time = System.currentTimeMillis();

						//menjiFunction.autoAction2();
						douniuFunction.autoAction();
						if (System.currentTimeMillis() - time > 3000) {
							slowLogicDao
									.addSlowLogic(
											"MATCH_THREAD",
											time,
											(int) ((System.currentTimeMillis() - time) / 1000),
											0);
							LogUtil.info("循环消耗"
									+ (System.currentTimeMillis() - time));
						}

					}
				});

		// 游戏处理线程
		schedulerBean.submit("MAJIANG_THREAD", TimeUtil.ONE_SECOND * 650,
				new Runnable() {
					@Override
					public void run() {
						long time = System.currentTimeMillis();
						// 麻将处理
						majiangFunction.autoAction();
						zxmajiangFunction.autoAction();
						cdmajiangFunction.autoAction();
						doudizhuFunction.autoAction();

						if (System.currentTimeMillis() - time > 3000) {
							slowLogicDao
									.addSlowLogic(
											"MAJIANG_THREAD",
											time,
											(int) ((System.currentTimeMillis() - time) / 1000),
											0);
							LogUtil.info("循环消耗"
									+ (System.currentTimeMillis() - time));
						}

					}
				});

		// 游戏房间匹配线程
		schedulerBean.submit("MATCHING_THREAD", TimeUtil.ONE_SECOND * 1000,
				new Runnable() {
					@Override
					public void run() {
						Channel channel = NettyClient.getChannel();
						if (channel == null || !channel.isRegistered()) {

						}
						roomFunction.doGameMatching();
//						for (int i = 1; i <= 3; i++) {
							/*
							 * for (int j = 1; j <= 3; j++) { int count =
							 * roomFunction.getWaitTime(i, j); if (index <
							 * 100000000&&count%2==0) { if (i == 3) { for (int k
							 * = 0; k < 2; k++) { Role role = new Role();
							 * role.setNick("你等待2轮了" + index);
							 * role.setRid(100000000 + index);
							 * role.setPlatform(-1); role.setGold(78999);
							 * role.setSex((byte) 1); role.setAvatar("2_1_1");
							 * int realType = roomFunction .getRealType(i, j);
							 * roomFunction.getReadyToGame(role, realType);
							 * index++; } }
							 * 
							 * } }
							 */

//						}

						roomFunction.doGameEnd();

					}
				});

		// 每日的凌晨1秒开始的线程
		schedulerBean.submit(
				"DAILY_THREAD",
				TimeUtil.getDateStartTime(TimeUtil.getDateAdd(1))
						- System.currentTimeMillis() + 5000,
				TimeUtil.ONE_DAY * 1000, new Runnable() {
					@Override
					public void run() {
						FunctionManager.doHandleOnNextDay();
					}
				});

		// 游戏处理线程
		schedulerBean.submit("STA_THREAD", TimeUtil.ONE_MINUTE * 1000,
				TimeUtil.ONE_MINUTE * 5000, new Runnable() {
					@Override
					public void run() {
						roleFunction.onlineSta();
					}
				});

		// 统计房间在线人数日志处理线程
		schedulerBean.submit("ROOM_LOG_THREAD", TimeUtil.ONE_MINUTE * 1000,
				TimeUtil.ONE_MINUTE * 5000, new Runnable() {
					@Override
					public void run() {
						// G_T_C 统计房间在线人数
						roomLogFunction.onLineSta();
						// 统计房间数量
						Map<Integer, AtomicIntegerArray> menjiRoomCount = menjiFunction
								.getRoomCountCache();
						Map<Integer, AtomicIntegerArray> doudizhuRoomCount = doudizhuFunction
								.getRoomCountCache();
						Map<Integer, AtomicIntegerArray> majiangRoomCount = majiangFunction
								.getRoomCountCache();
						Map<Integer, AtomicIntegerArray> zxmajiangRoomCount = zxmajiangFunction
								.getRoomCountCache();
						Map<Integer, AtomicIntegerArray> cdmajiangRoomCount = cdmajiangFunction
								.getRoomCountCache();
						
						Map<Integer, AtomicIntegerArray> douniuRoomCount = douniuFunction
								.getRoomCountCache();
						
						for (Integer key : menjiRoomCount.keySet()) {
							AtomicIntegerArray array = menjiRoomCount.get(key);
							roomLogFunction.dealRoomCountLog(
									array.getAndSet(0, 0),
									array.getAndSet(1, 0),
									array.getAndSet(2, 0),
									(byte) GameType.MENJI, key.byteValue(),
									array.getAndSet(3, 0),
									array.getAndSet(4, 0),
									array.getAndSet(5, 0),
									array.getAndSet(6, 0),
									array.getAndSet(7, 0));
						}
						// menjiRoomCount.clear();

						for (Integer key : douniuRoomCount.keySet()) {
							AtomicIntegerArray array = douniuRoomCount
									.get(key);
							roomLogFunction.dealRoomCountLog(
									array.getAndSet(0, 0),
									array.getAndSet(1, 0),
									array.getAndSet(2, 0),
									(byte) GameType.DOUNIU, key.byteValue(),
									array.getAndSet(3, 0),
									array.getAndSet(4, 0),
									array.getAndSet(5, 0),
									array.getAndSet(6, 0),
									array.getAndSet(7, 0));
						}
						// douniuRoomCount.clear();
						
						for (Integer key : doudizhuRoomCount.keySet()) {
							AtomicIntegerArray array = doudizhuRoomCount
									.get(key);
							roomLogFunction.dealRoomCountLog(
									array.getAndSet(0, 0),
									array.getAndSet(1, 0),
									array.getAndSet(2, 0),
									(byte) GameType.DOUDIZHU, key.byteValue(),
									array.getAndSet(3, 0),
									array.getAndSet(4, 0),
									array.getAndSet(5, 0),
									array.getAndSet(6, 0),
									array.getAndSet(7, 0));
						}
						// doudizhuRoomCount.clear();

						for (Integer key : majiangRoomCount.keySet()) {
							AtomicIntegerArray array = majiangRoomCount
									.get(key);
							roomLogFunction.dealRoomCountLog(
									array.getAndSet(0, 0),
									array.getAndSet(1, 0),
									array.getAndSet(2, 0),
									(byte) GameType.MAJIANG, key.byteValue(),
									array.getAndSet(3, 0),
									array.getAndSet(4, 0),
									array.getAndSet(5, 0),
									array.getAndSet(6, 0),
									array.getAndSet(7, 0));
						}
						// majiangRoomCount.clear();
						
						for (Integer key : zxmajiangRoomCount.keySet()) {
							AtomicIntegerArray array = zxmajiangRoomCount
									.get(key);
							roomLogFunction.dealRoomCountLog(
									array.getAndSet(0, 0),
									array.getAndSet(1, 0),
									array.getAndSet(2, 0),
									(byte) GameType.ZXMAJIANG, key.byteValue(),
									array.getAndSet(3, 0),
									array.getAndSet(4, 0),
									array.getAndSet(5, 0),
									array.getAndSet(6, 0),
									array.getAndSet(7, 0));
						}
						// zxmajiangRoomCount.clear();
						
						for (Integer key : cdmajiangRoomCount.keySet()) {
							AtomicIntegerArray array = cdmajiangRoomCount
									.get(key);
							roomLogFunction.dealRoomCountLog(
									array.getAndSet(0, 0),
									array.getAndSet(1, 0),
									array.getAndSet(2, 0),
									(byte) GameType.CDMAJIANG, key.byteValue(),
									array.getAndSet(3, 0),
									array.getAndSet(4, 0),
									array.getAndSet(5, 0),
									array.getAndSet(6, 0),
									array.getAndSet(7, 0));
						}
						// cdmajiangRoomCount.clear();
						
						
						for (Integer key : douniuRoomCount.keySet()) {
							AtomicIntegerArray array = douniuRoomCount
									.get(key);
							roomLogFunction.dealRoomCountLog(
									array.getAndSet(0, 0),
									array.getAndSet(1, 0),
									array.getAndSet(2, 0),
									(byte) GameType.DOUNIU, key.byteValue(),
									array.getAndSet(3, 0),
									array.getAndSet(4, 0),
									array.getAndSet(5, 0),
									array.getAndSet(6, 0),
									array.getAndSet(7, 0));
						}
						// douniuRoomCount.clear();
					}
				});
		
		//排行榜
		schedulerBean.submit("RANK_THREAD", TimeUtil.ONE_MINUTE * 5000,
				TimeUtil.ONE_MINUTE * 5000, new Runnable(){

					@Override
					public void run() {
						rankingListFunction.statistics();
						
					}
			
		});
		
		// 比赛
		schedulerBean.submit("GAMEMATCH_THREAD", TimeUtil.ONE_SECOND * 1000,
			TimeUtil.ONE_SECOND * 1000, new Runnable() {
				@Override
				public void run() {
					matchFunction.update();
				}
			});
	}
}
