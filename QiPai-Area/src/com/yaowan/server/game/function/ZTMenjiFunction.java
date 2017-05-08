package com.yaowan.server.game.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameRoom;
import com.yaowan.constant.GameStatus;
import com.yaowan.constant.GameType;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.constant.RoomType;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.AiRegulationCache;
import com.yaowan.csv.cache.ExpCache;
import com.yaowan.csv.cache.MenjiAIBasicCache;
import com.yaowan.csv.cache.MenjiAICache;
import com.yaowan.csv.cache.MenjiAICollectCache;
import com.yaowan.csv.cache.MenjiAIConfigCache;
import com.yaowan.csv.cache.MenjiAIVictoryCache;
import com.yaowan.csv.cache.MenjiCardValueCache;
import com.yaowan.csv.cache.MenjiRoomCache;
import com.yaowan.csv.entity.ExpCsv;
import com.yaowan.csv.entity.MenjiAICollectCsv;
import com.yaowan.csv.entity.MenjiAIConfigCsv;
import com.yaowan.csv.entity.MenjiAICsv;
import com.yaowan.csv.entity.MenjiAIVictoryCsv;
import com.yaowan.csv.entity.MenjiCardValueCsv;
import com.yaowan.csv.entity.MenjiRoomCsv;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.DispatchEvent;
import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.core.thread.SingleThreadManager;
import com.yaowan.framework.core.thread.SingleThreadTask;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.MathUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.protobuf.game.GGame.GGameInfo;
import com.yaowan.protobuf.game.GGame.GGameRole;
import com.yaowan.protobuf.game.GGame.GMsg_12006005;
import com.yaowan.protobuf.game.GGame.GMsg_12006008;
import com.yaowan.protobuf.game.GGame.PlayerState;
import com.yaowan.protobuf.game.GMenJi.GMenJiOpen;
import com.yaowan.protobuf.game.GMenJi.GMenJiPai;
import com.yaowan.protobuf.game.GMenJi.GMenJiPlayer;
import com.yaowan.protobuf.game.GMenJi.GMenJiPlayerEnd;
import com.yaowan.protobuf.game.GMenJi.GMsg_12013001;
import com.yaowan.protobuf.game.GMenJi.GMsg_12013002;
import com.yaowan.protobuf.game.GMenJi.GMsg_12013005;
import com.yaowan.protobuf.game.GMenJi.GMsg_12013006;
import com.yaowan.protobuf.game.GMenJi.GMsg_12013007;
import com.yaowan.protobuf.game.GMenJi.GMsg_12013008;
import com.yaowan.protobuf.game.GMenJi.GMsg_12013009;
import com.yaowan.protobuf.game.GMenJi.MJCardType;
import com.yaowan.protobuf.game.GMenJi.MenJiAction;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.model.data.entity.Npc;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.data.entity.Tax;
import com.yaowan.server.game.model.log.dao.MenjiLogDao;
import com.yaowan.server.game.model.log.dao.RoleBrokeLogDao;
import com.yaowan.server.game.model.log.entity.MenjiLog;
import com.yaowan.server.game.model.struct.MenjiBill;
import com.yaowan.server.game.model.struct.MissionType;
import com.yaowan.server.game.model.struct.TaskType;
import com.yaowan.server.game.model.struct.ZTMajiangRole;
import com.yaowan.server.game.model.struct.ZTMenjiAI;
import com.yaowan.server.game.model.struct.ZTMenjiRole;
import com.yaowan.server.game.model.struct.ZTMenjiTable;
import com.yaowan.server.game.rule.ZTMenji;

/**
 * 昭通翻金花
 *
 * @author zane
 */
@Component
public class ZTMenjiFunction extends FunctionAdapter {

	@Autowired
	private SingleThreadManager manager;

	@Autowired
	private MenjiDataFunction menjiDataFunction;

	@Autowired
	private RoleFunction roleFunction;
	
	@Autowired
	private NPCFunction npcFunction;

	@Autowired
	private RoomFunction roomFunction;

	@Autowired
	private MenjiAICache menjiAICache;
	
	@Autowired
	private TaxFunction taxFunction;

	@Autowired
	private MenjiCardValueCache menjiAICardValueCache;

	private Map<Long, ZTMenjiTable> tableMap = new ConcurrentHashMap<>();

	private Map<Long, ZTMenjiRole> roleMap = new ConcurrentHashMap<>();

	// 翻金花奖金池
	private static Map<Integer, Integer> menjiBonusMap = new ConcurrentHashMap<>();

	private static Map<Integer, AtomicIntegerArray> roomCountMap = new ConcurrentHashMap<>();

	//private static Map<ZTMenjiTable, ConcurrentHashMap<Long, ConcurrentLinkedDeque<ZTMenjiRole>>> dequeMap = new ConcurrentHashMap<>();

	private static Map<Long , ConcurrentHashMap<ZTMenjiRole,Integer>> AIOperationmap = new ConcurrentHashMap<Long, ConcurrentHashMap<ZTMenjiRole,Integer>>();
	
	@Autowired
	private MenjiAIBasicCache menjiAIBasicCache;
	
	@Autowired
	private MenjiRoomCache menjiRoomCache;

	@Autowired
	private MenjiAICollectCache menjiAICollectCache;

	@Autowired
	private ExpCache expCache;

	@Autowired
	MissionFunction missionFunction;

	// G_T_C 日志流水处理dao
	@Autowired
	private MenjiLogDao menjiLogDao;

	@Autowired
	private RoomLogFunction roomLogFunction;
	
	@Autowired
	private AiRegulationCache aiRegulationCache;
	
	@Autowired
	private MenjiAIVictoryCache menjiAIVictoryCache;
	
	@Autowired
	private RoleBrokeLogDao roleBrokeLogDao;
	
	@Autowired
	private MenjiAIConfigCache menjiAIConfigCache;

	public ZTMenjiTable getTable(Long id) {
		ZTMenjiTable menjiTable = tableMap.get(id);
		return menjiTable;
	}

	public ZTMenjiTable getTableByRole(Long id) {
		ZTMenjiRole menjiRole = getRole(id);
		return getTable(menjiRole.getRole().getRoomId());
	}

	public ZTMenjiRole getRole(Long id) {
		ZTMenjiRole menjiRole = roleMap.get(id);
		return menjiRole;
	}

	public void addRoleCache(ZTMenjiRole role) {
		roleMap.put(role.getRole().getRole().getRid(), role);
	}

	public void removeRoleCache(Long id) {
		roleMap.remove(id);
	}

	public void clear(long roomId) {
		ZTMenjiTable table = tableMap.remove(roomId);
		if (table != null) {
			for (ZTMenjiRole role : table.getMembers()) {
				if(role == null){
					continue;
				}
				if (role.getRole() != null) {
					removeRoleCache(role.getRole().getRole().getRid());
					if (role.getRole().getRole().getRid() < 100000) {
						int i = 0;
						int j = 0;
					}
				}

			}
		}

	}

	public void resetOverTable(Game game) {
		if (game.getStatus() == GameStatus.END_REWARD) {
			game.setStatus(GameStatus.WAIT_READY);
			ZTMenjiTable table = getTable(game.getRoomId());
			table.reset();
		}
	}

	public void exitTable(Game game, Long rid) {
		ZTMenjiTable table = getTable(game.getRoomId());
		if (table != null) {
			GameRole gameRole = game.getSpriteMap().get(rid);
			if (gameRole != null) {
				if (table.getGame().getStatus() == GameStatus.RUNNING
						&& gameRole.getStatus() != PlayerState.PS_WATCH_VALUE) {
					dealFold(table,
							table.getMembers().get(gameRole.getSeat() - 1));// 自动弃牌
				}
				for (ZTMenjiRole role : table.getMembers()) {
					if(role == null){
						continue;
					}
					if (role.getRole() != null) {

						GMsg_12006005.Builder builder = GMsg_12006005
								.newBuilder();
						builder.setCurrentSeat(gameRole.getSeat());
						roleFunction.sendMessageToPlayer(role.getRole()
								.getRole().getRid(), builder.build());


						// roleMap.remove(role.getRole().getRole().getRid());
						// table.getMembers().remove(role.getRole().getSeat()-1);
					}

				}
				// 可以是空
				table.getMembers().get(gameRole.getSeat() - 1).setRole(null);
				gameRole.setStatus(PlayerState.PS_EXIT_VALUE);

				game.getRoles().set(gameRole.getSeat() - 1, 0l);
				if(game.getStatus() == GameStatus.RUNNING && !table.getExitMap().containsKey(rid)){
					table.getExitMap().put(rid, gameRole.getSeat());
				}
				if (game.getSpriteMap().remove(rid) != null) {
					removeRoleCache(rid);
				}
				if (game.getSpriteMap().size() <= 0) {
					tableMap.remove(game.getRoomId());
				}
			}
		}

	}

	/**
	 * 获得翻金花奖金池剩余奖金
	 * 
	 * @param game
	 * @return
	 */
	public int getBonus(Game game) {
		int bonus;
		if (menjiBonusMap != null && !menjiBonusMap.isEmpty()
				&& menjiBonusMap.containsKey(game.getRoomType())) {
			bonus = menjiBonusMap.get(game.getRoomType());
		} else {
			LogUtil.info("roomType:" + game.getRoomType());
			bonus = menjiAICollectCache.getConfig(game.getRoomType())
					.getStandardLine();
			menjiBonusMap.put(game.getRoomType(), bonus);
		}
		return bonus;
	}

	/**
	 * 更新翻金花奖金池
	 * 
	 * @param game
	 * @param num
	 * @param add
	 */
	public void updateBonus(Game game, int num, boolean add) {
		int bonus = getBonus(game);
		if (add) {
			menjiBonusMap.put(game.getRoomType(), bonus + num);
		} else {
			menjiBonusMap.put(game.getRoomType(), bonus - num > 0 ? bonus - num
					: 0);
		}
	}

	/**
	 * 重置翻金花奖金池
	 */
	public void resetBonus() {
		for (MenjiAICollectCsv menjiAICollectCsv : menjiAICollectCache
				.getConfigList()) {
			menjiBonusMap.put(menjiAICollectCsv.getRoomId(),
					menjiAICollectCsv.getStandardLine());
		}
	}

	/**
	 * 调用需是已同步方法
	 * 
	 * @param game
	 */
	public void robotOutIn(Game game) {

		int count = 0;
		for (Long id : game.getRoles()) {
			if (id > 0) {
				count++;
			}
		}
		int num = 5 - count;

		boolean sub = false;
		if (count > 2 && Math.random() > (1 - count * 0.1)) {
			LogUtil.info("sub");
			for (GameRole role : game.getSpriteMap().values()) {
				if (role.isRobot()) {
					roomFunction.quitRole(game, role.getRole());

					break;
				}
			}
			sub = true;
		}
		if (!sub) {
			if (num > 1) {
				if (Math.random() < 0.7) {
					num = 1;
				} else {
					num = 2;
				}
			}
			if (num > 0) {
				LogUtil.info("add" + num);
				roomFunction.createRobotEnter(num, game);
			}
		}
		game.setLastRobotCreate(System.currentTimeMillis());
	}

	/**
	 * 定时检测所有人准备游戏开始 已被同步
	 */
	public void checkStart(Game game) {
		if (game.getStatus() != GameStatus.WAIT_READY) {
			return;
		}
		if(game.getSpriteMap().size() > 5){
			roomFunction.endGame(game);
		}
		boolean isRoleOK = true;

		long time = game.getStartTime();
		if (game.getEndTime() > game.getStartTime()) {
			time = game.getEndTime();
		}
		// 智能机器人进出场
		int dif = (int) (System.currentTimeMillis() - time) / 1000;
		// 5秒不准备
		if (dif >= 3
				&& System.currentTimeMillis() - game.getLastRobotCreate() > (MathUtil
						.randomNumber(3, 6)) * 1000) {
			robotOutIn(game);
		}

		Map<Long, GameRole> map = new HashMap<Long, GameRole>();
		map.putAll(game.getSpriteMap());
		for (Map.Entry<Long, GameRole> entry: map.entrySet()) {
			GameRole role = entry.getValue();
			if("".equals(role.getRole().getPlatform())){
				isRoleOK = true;
			}else if (!role.isRobot()) {
				if (role.getStatus() != PlayerState.PS_PREPARE_VALUE) {
					isRoleOK = false;
				}

			} else {

				// 自动退出房间
				MenjiRoomCsv menjiRoomCsv = menjiRoomCache.getConfig(game
						.getRoomType());
				boolean flag = false;

				if (menjiRoomCsv.getEnterUpperLimit() == -1) {
					if (role.getRole().getGold() < menjiRoomCsv
							.getEnterLowerLimit()) {
						flag = true;
					}
				} else {
					if (role.getRole().getGold() < menjiRoomCsv
							.getEnterLowerLimit()
							|| role.getRole().getGold() > menjiRoomCsv
									.getEnterUpperLimit()) {
						flag = true;
					}
				}
				if(!flag && role.isRobot()){
					if(npcFunction.robotByebye(role.getRole().getRid(), game.getGameType(), game.getRoomType())){
						//AI不在时间范围内就要走了
						LogUtil.error("焖鸡AI时间到，走了");
						flag = true;
					}
				}

				if (flag) {
					LogUtil.info("333333");
					roomFunction.quitRole(game, role.getRole());

					// 翻金花2人就可以开始
					int count = 0;
					for (Long id : game.getRoles()) {
						if (id > 0) {
							count++;
						}
					}

					if (count >= 2) {
						continue;
					} else {
						return;
					}
				}

				LogUtil.info("333344" + role.getRole().getGold());

				if (dif > role.getSeat()
						&& role.getStatus() != PlayerState.PS_PREPARE_VALUE
						&& System.currentTimeMillis() > role.getCreateTime() + 1500) {
					role.setStatus(PlayerState.PS_PREPARE_VALUE);

					GMsg_12013001.Builder builder = GMsg_12013001.newBuilder();
					builder.setSeat(role.getSeat());
					roleFunction.sendMessageToPlayers(game.getRoles(),
							builder.build());
				}
				if (role.getStatus() != PlayerState.PS_PREPARE_VALUE) {
					isRoleOK = false;
				}
			}

		}
		if (game.getSpriteMap().size() <= 1) {
			isRoleOK = false;
		}
		if (isRoleOK) {
			startTable(game);
		}
	}

	public void playerPrepare(Game game, GameRole role) {

		resetOverTable(game);

		MenjiRoomCsv menjiRoomCsv = menjiRoomCache
				.getConfig(game.getRoomType());
		boolean flag = false;
		if (menjiRoomCsv.getEnterUpperLimit() == -1) {
			if (role.getRole().getGold() < menjiRoomCsv.getEnterLowerLimit()) {
				flag = true;
			}
		} else {
			if (role.getRole().getGold() < menjiRoomCsv.getEnterLowerLimit()
					|| role.getRole().getGold() > menjiRoomCsv
							.getEnterUpperLimit()) {
				flag = true;
			}
		}
		if (flag) {

			roomFunction.quitRole(game, role.getRole());
			return;
		}

		role.setStatus(PlayerState.PS_PREPARE_VALUE);

		GMsg_12013001.Builder builder = GMsg_12013001.newBuilder();
		builder.setSeat(role.getSeat());
		roleFunction.sendMessageToPlayers(game.getRoles(), builder.build());
	}

	/**
	 * 开始游戏
	 */
	public void startTable(Game game) {
		if (game.getStatus() != GameStatus.WAIT_READY) {
			return;
		}
		// 开始游戏
		game.setStatus(GameStatus.RUNNING);
		for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			GameRole role = entry.getValue();
			role.setStatus(PlayerState.PS_PREPARE_VALUE);
		}
		ZTMenjiTable table = getTable(game.getRoomId());
		if(table == null || !table.isInited()){
			return;
		}

		List<Integer> initAllPai = initAllPai();
		// 洗牌 打乱顺序
		Collections.shuffle(initAllPai);
		table.setPais(initAllPai);

		List<Integer> pais = table.getPais();

		MenjiRoomCsv menjiRoomCsv = menjiRoomCache.getConfig(table.getGame()
				.getRoomType());
		MenjiAICollectCsv menjiAICollectCsv = menjiAICollectCache
				.getConfig(table.getGame().getRoomType());
		table.getWaiter().clear();
		table.getExitMap().clear();
		//int seat = 1;
		AtomicIntegerArray roomCountLog = getRoomCountCacheByRoomType(game
				.getRoomType());
		
		//给Members排序
//		List<ZTMenjiRole> ztMenjiRoles = new ArrayList<ZTMenjiRole>();
//		for (int i = 1; i <= 5; i++) {
//			ZTMenjiRole ztMenjiRole = new ZTMenjiRole();
//			ztMenjiRoles.add(ztMenjiRole);
//		}
//		for (ZTMenjiRole menjiMember : table.getMembers()) {
//			if (menjiMember.getRole() != null){
//				int seat = menjiMember.getRole().getSeat();
//				ztMenjiRoles.set(seat - 1, menjiMember);
//			}
//				
//		}
//		table.getMembers().clear();
//		table.getMembers().addAll(ztMenjiRoles);
		for (int i = 1; i <= 5; i++) {
			table.getWaiter().add(i);
		}
	
		for (ZTMenjiRole menjiMember : table.getMembers()) {
			if(menjiMember == null){
				continue;
			}
			if (menjiMember.getRole() != null) {
				table.getWaiter().remove(table.getWaiter().indexOf(menjiMember.getRole().getSeat()));
				menjiMember.getPai().clear();
				menjiMember.setWatch(false);
				int tax = 0;
				if (menjiRoomCsv == null) {
					
				} else {
					tax = menjiRoomCsv.getTaxPerGame();
					if(menjiMember.getRole().isRobot()){
						menjiMember.getRole().getRole().setGold(menjiMember.getRole().getRole().getGold() - tax);
					}else{
						roleFunction.goldSub(menjiMember.getRole().getRole(),
								menjiRoomCsv.getTaxPerGame(), MoneyEvent.MENJI_TAX,
								true);
						roomCountLog.addAndGet(2, tax);// 抽水
					}
				}
				
			}
//			else {
//				menjiMember.getPai().clear();
//				table.getWaiter().add(seat);
//			}
//			seat++;
		}
		roomCountLog.incrementAndGet(0);// 对战数

		// 机器人作弊概率
		int cheatProbability = 0;
		// 机器人位置
		int robotSeat = 0;
		//机器人的座位列表
		List<Integer> robotList = new ArrayList<Integer>();
		
		for (ZTMenjiRole role : table.getMembers()) {
			if (role != null && role.getRole() != null
					&& role.getRole().isRobot()) {
				// 机器人奖金池
				int robotBonus = getBonus(game);
				if (robotBonus >= menjiAICollectCsv.getTopLine()) {
					cheatProbability = menjiAICollectCsv.getTopAiWinRate() / 100;

				} else if (robotBonus <= menjiAICollectCsv.getBottomLine()) {
					cheatProbability = menjiAICollectCsv.getBottomAiWinRate() / 100;
				} else {
					cheatProbability = menjiAICollectCsv.getStandardAiWinRate() / 100;
				}
				robotSeat = role.getRole().getSeat();
				robotList.add(role.getRole().getSeat());
			}
		}
		List<List<Integer>> cardlists = new ArrayList<List<Integer>>();
		for (int i = 0; i < table.getMembers().size(); i++) {
			ZTMenjiRole menjiMember = table.getMembers().get(i);
			if(menjiMember ==null){
				continue;
			}
			if (menjiMember.getRole() != null) {
				if (menjiMember.getRole().isRobot()) {
					int menjiAINum = MathUtil.randomNumber(1, menjiAICache.getConfigList().size());
					LogUtil.error("第几个AI:" + menjiAINum + ",id号：" + menjiAICache.getConfigList().get(menjiAINum-1).getAiId());
					MenjiAICsv menjiAICsv = menjiAICache.getConfigList().get(menjiAINum-1);
					ZTMenjiAI ztMenjiAI = new ZTMenjiAI();
					ztMenjiAI
							.setAddProbability(menjiAICsv.getBasicRaiserate() / 100);
					ztMenjiAI.setCompeteProbability(menjiAICsv
							.getBasicCombatrate() / 100);
					ztMenjiAI
							.setFoldProbability(menjiAICsv.getBasicFoldrate() / 100);
					ztMenjiAI.setId(menjiAICsv.getAiId());
					ztMenjiAI
							.setLookProbability(menjiAICsv.getBasicCheckrate() / 100);
					ztMenjiAI.setLookValue(menjiAICsv.getBasicCheckPoint());
					ztMenjiAI.setDarkFollowRate((int) Math.ceil(Math.random() * 100));
					ztMenjiAI.setDarkFollowRate(MathUtil.randomNumber(1, 100));
					menjiMember.setZtMenjiAI(ztMenjiAI);
				} else {
					menjiMember.setZtMenjiAI(new ZTMenjiAI());
				}
				menjiMember.setLook(0);
				menjiMember.setCompete(0);
				menjiMember.setChip(menjiRoomCsv.getPotOdds());
				menjiMember.setGoodPai(false);
				List<Integer> cards = new ArrayList<Integer>();
				for (int j = 0; j < 3; j++) {
					Integer remove = pais.remove(0);
					cards.add(remove);
				}
				cardlists.add(cards);
			}
		}
		int oneAiCheatProbability = 0;
		int complete1 = (int) (Math.random() * 100); //设置用于跟全局作比较是否作弊的概率
		int complete2 = (int) (Math.random() * 100); //设置用于跟单个AI比较是否作弊的概率
		if(cheatProbability <= complete1){
			for(ZTMenjiRole role : table.getMembers()){
				if(role != null && role.getRole() != null && role.getRole().isRobot()){
					int victoryId = npcFunction.getDrawCardId(game.getGameType(), game.getRoomType(), role.getRole().getRole().getRid());
					LogUtil.info("victoryId:"+victoryId);
					 MenjiAIVictoryCsv csv = menjiAIVictoryCache.getConfig(victoryId);
					if(csv != null){
						if(csv.getVictoryProbability() / 100 >= oneAiCheatProbability){
							oneAiCheatProbability = csv.getVictoryProbability() / 100;
						}
					}
				}
				
			}
		}
		//最大牌型是第 maxPai+1 部
		int maxPai = ZTMenji.sortCard(cardlists);
		if (robotList.size() >= 1 
				&& (cheatProbability > complete1 || oneAiCheatProbability > complete2)
				) {
			if(cheatProbability > complete1){
				int r = MathUtil.randomNumber(1, robotList.size());
				robotSeat = robotList.get(r - 1);
			}else{
				if(robotList.size() == 1){
					robotSeat = robotList.get(0);
				}else{
					int gainOrLoss = 0;
					int seatIndex = 0;
					for (int i = 0; i < robotList.size(); i++) {
						ZTMenjiRole role = table.getMembers().get(robotList.get(i) - 1);
						Npc npc1 = npcFunction.getNpcById(game.getGameType(), game.getRoomType(), role.getRole().getRole().getRid());
						if(npc1.getGainOrLoss() <= gainOrLoss){
							seatIndex = i;
							gainOrLoss = npc1.getGainOrLoss();
						}
					}
					robotSeat = robotList.get(seatIndex);
				}
				
				
			}
			
			LogUtil.info("本局机器人有作弊");
			// TOOO 需要作弊
			
			int j = 0;
			for (int i = 0; i < table.getMembers().size(); i++) {
				ZTMenjiRole menjiMember = table.getMembers().get(i);
				if(menjiMember ==null){
					continue;
				}
				if (menjiMember.getRole() != null) {
					if (menjiMember.getRole().getSeat() == robotSeat) {
						menjiMember.setGoodPai(true);
						LogUtil.info("作弊机器人的座位为：" + robotSeat);
						menjiMember.getPai().addAll(cardlists.get(maxPai));
					} else {
						if (j == maxPai) {
							j++;
						}
						menjiMember.getPai().addAll(cardlists.get(j));
						j++;
					}
				}

			}
		} else {
			int i = 0;
			for (ZTMenjiRole menjiMember : table.getMembers()) {
				if(menjiMember ==null){
					continue;
				}
				if (menjiMember.getRole() != null) {
					menjiMember.getPai().addAll(cardlists.get(i));
					i++;
				}

			}
		}
		// for (int i = 0; i < 3; i++) {
		// for (int j = 0; j < table.getMembers().size(); j++) {
		// ZTMenjiRole menjiMember = table.getMembers().get(j);
		// if(menjiMember.getRole()!=null){
		// Integer remove = pais.remove(0);
		// menjiMember.setLook(0);
		// menjiMember.setCompete(0);
		// menjiMember.getPai().add(remove);
		// menjiMember.setChip(menjiRoomCsv.getPotOdds());
		// }
		// }
		// }

		// 需要支持空座位
		LogUtil.error("庄家座位号："+table.getOwner());
		List<Integer> seatlist = new ArrayList<Integer>();
		for(GameRole role : game.getSpriteMap().values()){
			seatlist.add(role.getSeat());
		}
		if(table.getOwner() == 0){
			int index = MathUtil.randomNumber(1, seatlist.size());
			table.setOwner(seatlist.get(index-1));
//			table.setLastPlaySeat(index);
//			for (int i = 1; i <= index; i++) {
//				
//			}
		}else{
			if(table.getWaiter().contains(table.getOwner())){
				LogUtil.error("重新生成庄家");
				int index = MathUtil.randomNumber(1, seatlist.size());
				table.setOwner(seatlist.get(index-1));
//				int index = MathUtil.randomNumber(1, table.getMembers().size());
//				table.setLastPlaySeat(index);
//				for (int i = 1; i <= index; i++) {
//					table.setOwner(table.getNextPlaySeat());
//				}
			}
		}
		// 庄家的下一个开始
		table.setLastPlaySeat(table.getOwner());
		table.setLastPlaySeat(table.getNextPlaySeat());
		
		
		//记录第一个操作的人，标记第一个人还没有操作过
		table.setFirstOperation(table.getLastPlaySeat()); 
		table.setOperation(false);
		table.setRound(1);

		table.setBetsNum(menjiRoomCsv.getPotOdds());

		// 第一轮参与轮局的座位号
		table.getRoundList().clear();
		for (ZTMenjiRole menjiMember : table.getMembers()) {
			if(menjiMember ==null){
				continue;
			}
			if (menjiMember.getRole() != null) {
				table.setAllBetsNum(menjiRoomCsv.getPotOdds()
						+ table.getAllBetsNum());
				table.getRoundList().add(menjiMember.getRole().getSeat());
			}
		}

		game.setCount(game.getCount() + 1);

		for (ZTMenjiRole menjiMember : table.getMembers()) {
			if(menjiMember ==null){
				continue;
			}
			if (menjiMember.getRole() != null) {
				GMsg_12013002.Builder builder = GMsg_12013002.newBuilder();
				GMenJiOpen.Builder builder2 = GMenJiOpen.newBuilder();

				builder2.setHostSeat(table.getOwner());

				builder.setOpenInfo(builder2);
				roleFunction.sendMessageToPlayer(menjiMember.getRole()
						.getRole().getRid(), builder.build());
				// 增加活跃度
				GameRole gameRole = menjiMember.getRole();
				Role role = gameRole.getRole();
				if (null != role) {
					role.setBureauCount(role.getBureauCount() + 1);
					role.markToUpdate("bureauCount");
				}
			}
		}
		tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(),
				HandleType.MENJI_GAME, System.currentTimeMillis() + 6000);
	}

	/**
	 * 重新进入房间
	 *
	 */
	public void enterTable(final Game game, final Role role) {

		ZTMenjiTable table = getTable(game.getRoomId());
		if (game.getStatus() == GameStatus.RUNNING && table == null) {
			game.setStatus(GameStatus.NO_INIT);
			game.getRoles().clear();
			game.getSpriteMap().clear();
			game.setStartTime(System.currentTimeMillis());
			table = initTable(game);
			LogUtil.error("table null" + game.getRoomId() + role.getNick());
		}
		if (table == null) {
			LogUtil.error(game.getRoles() + " table null" + game.getRoomId()
					+ role.getNick());
			game.setStatus(GameStatus.CLEAR);
			return;

		}
		
		if(table.getGame().getSpriteMap().size() >= 5){
			LogUtil.info("超过5个人要被踢掉" + role.getNick() + "," + game.getRoomId());
			return;
		}
		// 初始化桌对象 查询空座位坐下
		GameRole gameRole = game.findEnterSeat(role);
		if(gameRole.getSeat() > 5 || gameRole.getSeat() <= 0){
			return;
		}
		LogUtil.info("原有座位号"+gameRole.getSeat());
		for(Entry<Long, Integer> entry :table.getExitMap().entrySet()){
			LogUtil.info("entry key " + entry.getKey() + " value " + entry.getValue());
		}
		if(table.getExitMap().containsKey(role.getRid())){
			if(table.getMembers().get(table.getExitMap().get(role.getRid()) - 1) == null){
				gameRole.setSeat(table.getExitMap().get(role.getRid()));
				LogUtil.info("现有座位号"+gameRole.getSeat());
			}
		}
		//座位号有人了，踢走
		if(table.getMembers().get(gameRole.getSeat() - 1) != null && table.getMembers().get(gameRole.getSeat() - 1).getRole() != null){
			LogUtil.info(""+table.getMembers().get(gameRole.getSeat() - 1).getRole().getRole().getNick());
			LogUtil.info("座位号有人了，踢掉" + role.getNick() + "," + game.getRoomId());
			return;
		}
		/*
		 * if (role.getPlatform() == -1) { gameRole.setRobot(true);
		 * gameRole.setAuto(true); } int fitseat = 0; int index = 0; for (Long
		 * rid : game.getRoles()) { index++; if (rid == 0) { fitseat = index;
		 * break; } } if (fitseat == 0) { fitseat = game.getRoles().size() + 1;
		 * } gameRole.setSeat(fitseat); if (fitseat > game.getRoles().size()) {
		 * game.getRoles().add(role.getRid()); } else {
		 * game.getRoles().set(fitseat - 1, role.getRid());
		 * 
		 * } game.getSpriteMap().put(role.getRid(), gameRole);
		 */


		GMsg_12013009.Builder builder = GMsg_12013009.newBuilder();

		GMenJiOpen.Builder builder2 = GMenJiOpen.newBuilder();
		builder2.setHostSeat(table.getOwner());
		builder.setOpenInfo(builder2);

		for (ZTMenjiRole menjiRole : table.getMembers()) {
			if(menjiRole == null){
				continue;
			}
			if (menjiRole.getRole() != null) {
				GMenJiPlayer.Builder value = GMenJiPlayer.newBuilder();
				if (menjiRole.getLookedPai() > 0) {
					GMenJiPai.Builder pai = GMenJiPai.newBuilder();

					for (Integer data : menjiRole.getPai()) {
						pai.addPaiValue(data % 13 == 0 ? 13 : data % 13);
						pai.addPaiColor(data % 13 == 0 ? (data / 13 - 1)
								: data / 13);
					}
					pai.setPaiType(ZTMenji.getCardType(menjiRole.getPai()));
					value.setHandPai(pai);
				}
				value.setSeat(menjiRole.getRole().getSeat());
				value.setLookedPai(menjiRole.getLook());
				if(game.getStatus() == GameStatus.RUNNING){
					value.setStatus(table.getWaiter().contains(
							menjiRole.getRole().getSeat()) ? 1 : 0);
				}else{
					value.setStatus(0);
				}
				
				value.setTotalBetChips(menjiRole.getChip());
				builder.addPlayers(value);
			} 
			

		}
		// 新的桌面对象
		ZTMenjiRole menjiRole = new ZTMenjiRole(gameRole);
		addRoleCache(menjiRole);
		gameRole.setStatus(PlayerState.PS_WATCH_VALUE);
		//标记为旁观者
		menjiRole.setWatch(true);
		
		if(gameRole.isRobot()){
			//设置机器人每周以及生涯的胜场数，总场数
			Npc npc = npcFunction.getNpcById(game.getGameType(), game.getRoomType(), gameRole.getRole().getRid());
			if(npc != null){
				gameRole.setWinTotal(npc.getWinTotal());
				gameRole.setWinWeek(npc.getWinWeek());
				gameRole.setCountTotal(npc.getCountTotal());
				gameRole.setCountWeek(npc.getCountWeek());
				gameRole.setDiamond(role.getDiamond());
			}
			
		}
		//把用户添加到member里面
		table.getMembers().set(gameRole.getSeat() - 1,menjiRole);
		if(game.getStatus() == GameStatus.WAIT_READY && table.getWaiter().contains(gameRole.getSeat())){
			table.getWaiter().remove(table.getWaiter().indexOf(gameRole.getSeat()));
		}
		for(ZTMenjiRole ztMenjiRole : table.getMembers()){
			if(ztMenjiRole == null){
				continue;
			}
			if(ztMenjiRole.getRole() != null){
				LogUtil.info("member里面的玩家：" + ztMenjiRole.getRole().getRole().getNick() + "," + ztMenjiRole.getRole().getSeat());
			}
			
		}
		for(GameRole gameRole1 : game.getSpriteMap().values()){
			LogUtil.info("game里面的玩家：" + gameRole1.getRole().getNick() + "," + gameRole1.getSeat());
		}
		LogUtil.info("table.getWaiter()" + table.getWaiter());
				
		GGameInfo.Builder info = GGameInfo.newBuilder();
		info.setGameType(game.getGameType());
		info.setRoomId(game.getRoomId());
		info.setRoomType(game.getRoomType());

		GGameRole.Builder temp = null;
		for (ZTMenjiRole ztMenjiRole : table.getMembers()) {
			if(ztMenjiRole == null){
				continue;
			}
			if(ztMenjiRole.getRole() != null){
				GGameRole.Builder builder3 = GGameRole.newBuilder();
				GameRole gameRole2 = ztMenjiRole.getRole();
				Role target = gameRole2.getRole();
				builder3.setRid(target.getRid());
				builder3.setNick(target.getNick());
				builder3.setGold(target.getGold());
				builder3.setHead(target.getHead());
				builder3.setLevel(target.getLevel());
				builder3.setSeat(gameRole2.getSeat());
				builder3.setAvatarId(gameRole2.getAvatarId());
				builder3.setSex(target.getSex());
				if(table.getGame().getStatus() == GameStatus.WAIT_READY 
						&& gameRole2.getStatus() == PlayerState.PS_PREPARE_VALUE){
					builder3.setIsReady(1);
				}else{
					builder3.setIsReady(0);
				}
				if(table.getGame().getStatus() == GameStatus.RUNNING && ztMenjiRole.isWatch()){
					LogUtil.error(target.getNick() + "是旁观者");
					builder3.setWatch(1);
				}else{
					builder3.setWatch(0);
				}
				info.addSprites(builder3);

				if (target.getRid() == role.getRid()) {
					temp = builder3;
				}
			}
			
		}
		builder.setGame(info);

		builder.setInfo(table.serialize());

		roleFunction.sendMessageToPlayer(role.getRid(), builder.build());

		// 其他人收到进入房间
		GMsg_12006008.Builder msg = GMsg_12006008.newBuilder();
		GGameRole.Builder builder3 = GGameRole.newBuilder();

		builder3.setRid(role.getRid());
		builder3.setNick(role.getNick());
		builder3.setGold(role.getGold());
		builder3.setHead(role.getHead());
		builder3.setLevel(role.getLevel());
		builder3.setSeat(gameRole.getSeat());
		builder3.setAvatarId(gameRole.getAvatarId());
		builder3.setSex(role.getSex());
		msg.setRoleInfo(temp);
		List<Long> otherList = new ArrayList<Long>();
		otherList.addAll(game.getRoles());
		otherList.remove(role.getRid());
		roleFunction.sendMessageToPlayers(otherList, msg.build());
		LogUtil.info("enterTable getGameType" + otherList);

	}

	/**
	 * 初始化翻金花牌
	 *
	 * @param owner
	 * @return
	 */
	public ZTMenjiTable initTable(Game game) {
		if (game.getStatus() > 0) {
			return null;
		}
		ZTMenjiTable menjiTable = new ZTMenjiTable(game);
		// mahJiangTable.setOwner(owner.getId());

		//设置5个座位号都为空
		for (int i = 0; i < 5; i++) {
			menjiTable.getMembers().add(null);
		}
		
		for (int i = 1; i <= 5; i++) {
			menjiTable.getWaiter().add(i);
		}
		
		for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			ZTMenjiRole member = new ZTMenjiRole(entry.getValue());
			member.setWatch(false);
			menjiTable.getMembers().set(entry.getValue().getSeat() - 1,member);
			menjiTable.getWaiter().remove(menjiTable.getWaiter().indexOf(entry.getValue().getSeat()));
			addRoleCache(member);
		}

		tableMap.put(game.getRoomId(), menjiTable);

		//
		game.setStatus(GameStatus.WAIT_READY);

		return menjiTable;
	}

	/**
	 * 初始化翻金花牌
	 *
	 * @return
	 */
	public List<Integer> initAllPai() {
		List<Integer> allPai = MathUtil.generateDifNums(52, 1, 52);
		return allPai;
	}

	/**
	 * 处理超时
	 */
	public void tableToWait(ZTMenjiTable table, int targetSeat, int nextSeat,
			int hanlerType, long coolDownTime) {
		table.setLastPlaySeat(targetSeat);
		table.setNextSeat(nextSeat);
		table.setQueueWaitType(hanlerType);
		table.setCoolDownTime(coolDownTime);
	}

	/**
	 * 处理比牌后结算
	 */
	public void tableToWait1(ZTMenjiTable table, int hanlerType,
			long coolDownTime) {
		table.setQueueWaitType(hanlerType);
		table.setCoolDownTime(coolDownTime);
	}

	/**
	 * 流程处理
	 */
	public void autoAction() {
		for (Map.Entry<Long, ZTMenjiTable> entry : tableMap.entrySet()) {
			ZTMenjiTable table = entry.getValue();
			if (table.getGame().getStatus() > GameStatus.RUNNING
					|| table.getQueueWaitType() == 0) {
				continue;
			}
			manager.executeTask(new SingleThreadTask(table) {
				@Override
				public void doTask(ISingleData singleData) {
					ZTMenjiTable table = (ZTMenjiTable) singleData;

					// 还在倒计时中
					long currentTimeMillis = System.currentTimeMillis();
					long coolDownTime = table.getCoolDownTime();
					if (coolDownTime > currentTimeMillis) {
						return;
					}
					int tableState = table.getQueueWaitType();
					try {
						// 处理完变成没事件
						table.setQueueWaitType(0);
						Event event = new Event(tableState, table);
						DispatchEvent.dispacthEvent(event);
					} catch (Exception e) {
						table.setQueueWaitType(tableState);
						e.printStackTrace();
						LogUtil.error("翻金花流程处理异常", e);
					}
				}
			});
		}
	}
	
	public void autoAction2() {
		for (Map.Entry<Long , ConcurrentHashMap<ZTMenjiRole,Integer>> entry : AIOperationmap.entrySet()) {
			ZTMenjiTable table = getTable(entry.getKey());
			if(table != null){
				manager.executeTask(new SingleThreadTask(table) {
					@Override
					public void doTask(ISingleData singleData) {
						ZTMenjiTable table = (ZTMenjiTable) singleData;
						ConcurrentHashMap<ZTMenjiRole,Integer> ztMenjiRoleMap = getZTMenjiRoleMap(table);
						if(ztMenjiRoleMap != null){
							for( Map.Entry<ZTMenjiRole,Integer> entry1 : ztMenjiRoleMap.entrySet()){
								ZTMenjiRole role = entry1.getKey();
								if(role != null && role.getRole() != null 
										&& !table.getWaiter().contains(role.getRole().getSeat())
										&& table.getLastPlaySeat() != role.getRole().getSeat()){
									int operation = entry1.getValue();
									ztMenjiRoleMap.keySet().remove(entry1.getKey());
									LogUtil.error(role.getRole().getRole().getNick() + "操作" + operation + "操作" + ",时间：" + System.currentTimeMillis());
									if(operation == 2){
										if(role.getLook() == 1){
											//弃牌
											LogUtil.info("回合外弃牌");
											dealFold(table, role);
										}else{
											//看牌
											LogUtil.info("回合外看牌");
											dealLook(table, role);
										}
										
									}else{
										//看牌
										LogUtil.info("回合外看牌");
										dealLook(table, role);
									}
									break;
								}else{
									ztMenjiRoleMap.keySet().remove(entry1.getKey());
								}
								if(ztMenjiRoleMap.size() <= 0){
									removeAIOperation(table);
								}
							}
						}
					}
				});
			}
		}
	}
	
	public ConcurrentHashMap<ZTMenjiRole,Integer> getZTMenjiRoleMap(ZTMenjiTable table){
		return AIOperationmap.get(table.getGame().getRoomId());
	}
	
	public void removeAIOperation(ZTMenjiTable table){
		AIOperationmap.remove(table.getGame().getRoomId());
	}

	/**
	 * 通知操作结果并提交下一操作
	 */
	public void processAction(ZTMenjiTable table, MenJiAction action,
			int nextSeat, int handleType, long time, int look, int gold) {
		// table.setWaitAction(action);

		GMsg_12013007.Builder builder = GMsg_12013007.newBuilder();
		builder.setGold(gold);
		builder.setLook(look);
		builder.setAction(action);
		LogUtil.info("注数：" + table.getBetsNum());
		builder.setCurrentChips(table.getBetsNum());
		builder.setCurrentSeat(table.getLastPlaySeat());

		ZTMenjiRole member = table.getMembers()
				.get(table.getLastPlaySeat() - 1);
		builder.setChip(member.getChip());
		builder.setParam(table.getParam());// setTable(table.serialize());
		builder.setAllChips(table.getAllBetsNum());

		LogUtil.info("提交-----" + table.getParam());

		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());
		LogUtil.info(Thread.currentThread().getName() + ":"
				+ table.getGame().getRoomId() + table.getLastPlaySeat()
				+ "!!!!!" + nextSeat);

		table.setLastPlaySeat(nextSeat);
		table.setNextSeat(table.getNextPlaySeat());

		tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(),
				handleType, time);
	}

	/**
	 * 处理看牌
	 *
	 * @param menjiTable
	 * @param member
	 */
	public void dealLook(ZTMenjiTable table, ZTMenjiRole role) {
		// table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();

		MenJiAction action = MenJiAction.MJ_LOOK;
		if (role.getLook() == 1) {
			return;
		}
		role.setLook(1);
		GMsg_12013005.Builder builder = GMsg_12013005.newBuilder();
		GMenJiPai.Builder builder2 = GMenJiPai.newBuilder();

		// int[] rids = new int[3];
		// int[] color = new int[3];
		// int i = 0;
		// for (Integer data : role.getPai()) {
		// rids[i] = data % 13 == 0 ? 13 : data % 13;
		// color[i] = rids[i] == 13 ? (data / 13 - 1) : data / 13;
		// i = i + 1;
		// }
		// ZTMenji.sortBigAndSmallCard(color, rids);
		// for (int j = 0; j < color.length; j++) {
		// builder2.addPaiColor(color[j]);
		// builder2.addPaiValue(rids[j]);
		// }
		for (Integer data : role.getPai()) {
			int value = data % 13 == 0 ? 13 : data % 13;
			builder2.addPaiValue(value);
			builder2.addPaiColor(data % 13 == 0 ? (data / 13 - 1) : data / 13);
		}
		builder2.setPaiType(ZTMenji.getCardType(role.getPai()));
		System.out.println("牌型：" + builder2.getPaiType());
		builder.setCurrentPai(builder2);
		roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
				builder.build());

		GMsg_12013007.Builder builder3 = GMsg_12013007.newBuilder();
		builder3.setGold(role.getRole().getRole().getGold() - role.getChip());
		builder3.setLook(role.getLook());
		builder3.setAction(action);
		builder3.setCurrentSeat(seat);
		LogUtil.info("注数：" + table.getBetsNum());
		builder3.setCurrentChips(table.getBetsNum());
		builder3.setParam(table.getParam());// setTable(table.serialize());
		builder3.setChip(role.getChip());
		builder3.setAllChips(table.getAllBetsNum());

		List<Long> rolesList = new ArrayList<Long>();
		for (long rid : table.getGame().getRoles()) {
			if (rid != role.getRole().getRole().getRid()) {
				rolesList.add(rid);
			}
		}
		roleFunction.sendMessageToPlayers(rolesList, builder3.build());

		//AIUpdateByLook(table, role);
		//AIOutsideOperation(table, role);

		// 如果是机器人，操作完成后修改机器人自己的概率值
		if (role.getRole().isRobot()) {
//			AIUpdateBySelfLook(table, builder2.getPaiType(), role,
//					builder2.getPaiValueList());
		}
		/*
		 * processAction(table, action, table.getNextPlaySeat(), handleType,
		 * System.currentTimeMillis() + 8000);
		 */
	}

	/**
	 * 处理跟
	 *
	 * @param menjiTable
	 * @param member
	 */
	public void dealFollow(ZTMenjiTable table, ZTMenjiRole role) {
		LogUtil.error("玩家"+role.getRole().getRole().getNick() + "操作前的金币：" + role.getRole().getRole().getGold());
		MenjiRoomCsv menjiRoomCsv = menjiRoomCache.getConfig(table.getGame()
				.getRoomType());
		table.setQueueWaitType(0);
		// 是否看牌，若看牌倍数为2
		int multiple = 1;
		if (role.getLook() == 1) {
			multiple = 2;
		}
		// 已经操作过，重新计算本轮的座位号
		boolean isAddRound = table.setRoundLists(role);

		int seat = table.getLastPlaySeat();

		MenJiAction action = MenJiAction.MJ_FOLLOW;
		int handleType = HandleType.MENJI_GAME;

		// 用户本局到目前投注的总金额
		role.setChip(role.getChip() + table.getBetsNum() * multiple);
		// 本局到目前投注的总金额
		table.setAllBetsNum(table.getAllBetsNum() + table.getBetsNum()
				* multiple);

		// 用户做出跟注操作影响AI数值
		//AIUpdateByFollow(table, role);
		if (isAddRound) {// 结束一轮，跟新AI的概率值
			//AIUpdateByEndRound(table);
		}
		// AI做出看牌或者弃牌的判定
		//AIOutsideOperation(table, role);
		LogUtil.error("玩家..."+role.getRole().getRole().getNick() + "跟注，减少了"+(table.getBetsNum() * multiple)+"剩余金币" + (role.getRole().getRole().getGold() - role.getChip()));
		if (!needCompete(table)) {
			processAction(table, action, table.getNextPlaySeat(), handleType,
					System.currentTimeMillis() + 500, role.getLook(), role
							.getRole().getRole().getGold()
							- role.getChip());
		}

	}

	/**
	 * 处理加注
	 *
	 * @param menjiTable
	 * @param member
	 */
	public void dealAdd(ZTMenjiTable table, ZTMenjiRole role, int type) {
		LogUtil.error("玩家"+role.getRole().getRole().getNick() + "操作前的金币：" + role.getRole().getRole().getGold());
		MenjiRoomCsv menjiRoomCsv = menjiRoomCache.getConfig(table.getGame()
				.getRoomType());
		table.setQueueWaitType(0);
		table.setParam(type);

		int seat = table.getLastPlaySeat();
		// 已经操作过，重新计算本轮的座位号
		boolean isAddRound = table.setRoundLists(role);

		List<Integer> list = StringUtil.stringToList(
				menjiRoomCsv.getRaiseOdds(),
				StringUtil.DELIMITER_BETWEEN_ITEMS, Integer.class);
		// 是否看牌，若看牌倍数为2
		int multiple = 1;
		if (role.getLook() == 1) {
			multiple = 2;
		}
		// 与上次注额的差级
		int differenceBets = 0;
		if (!list.contains(table.getBetsNum())) {
			differenceBets = type;
		} else {
			differenceBets = type - (list.indexOf(table.getBetsNum()) + 1);
			if (type < (list.indexOf(table.getBetsNum()) + 1)) {
				differenceBets = 1;
			}
		}
		// 获得投注类型对应的金额
		table.setBetsNum(list.get(type - 1));
		// 本轮到目前投注的总金额
		table.setAllBetsNum(table.getAllBetsNum() + table.getBetsNum()
				* multiple);
		// 用户目前为止投注的总金额
		role.setChip(role.getChip() + table.getBetsNum() * multiple);
		MenJiAction action = MenJiAction.MJ_ADD;
		int handleType = HandleType.MENJI_GAME;

		// 用户做出加注操作影响AI数值
		for (int i = 0; i < differenceBets; i++) {
			//AIUpdateByAdd(table, role);
		}
		if (isAddRound) {// 结束一轮，跟新AI的概率值
			//AIUpdateByEndRound(table);
		}
		// AI做出看牌或者弃牌的判定
		//AIOutsideOperation(table, role);
		LogUtil.error("玩家..."+role.getRole().getRole().getNick() + "加注，减少了"+(table.getBetsNum() * multiple)+"剩余金币" + (role.getRole().getRole().getGold() - role.getChip()));
		if (!needCompete(table)) {
			processAction(table, action, table.getNextPlaySeat(), handleType,
					System.currentTimeMillis() + 500, role.getLook(), role
							.getRole().getRole().getGold()
							- role.getChip());

		}

	}

	/**
	 * 处理弃牌
	 * 
	 * @param table
	 * @param member
	 */
	public void dealFold(ZTMenjiTable table, ZTMenjiRole member) {
		//LogUtil.error("玩家"+member.getRole().getRole().getNick() + "操作前的金币：" + member.getRole().getRole().getGold());
		if (member.getRole().getStatus() == PlayerState.PS_WATCH_VALUE) {
			return;
		}
		if (table.getWaiter().size() >= table.getMembers().size() - 1) {
			endTable(table);
			return;
		}
		int seat = table.getLastPlaySeat();
		if (seat == member.getRole().getSeat()) {
			table.setQueueWaitType(0);
		}

		MenjiBill menjiBill = new MenjiBill();
		menjiBill.setRid(member.getRole().getRole().getRid());
		menjiBill.setHandPai(member.getPai());
		menjiBill.setGold(member.getChip());
		menjiBill.setCompete(member.getCompete());
		menjiBill.setName(member.getRole().getRole().getNick());
		menjiBill.setRobot(member.getRole().isRobot());

		MenJiAction action = MenJiAction.MJ_FOLD;
		int handleType = HandleType.MENJI_GAME;

		member.setLookedPai(2);

		GMsg_12013007.Builder builder = GMsg_12013007.newBuilder();
		LogUtil.info("注数：" + table.getBetsNum());
		builder.setCurrentChips(table.getBetsNum());
		builder.setAllChips(table.getAllBetsNum());
		builder.setGold(member.getRole().getRole().getGold() - member.getChip());
		builder.setLook(member.getLook());
		builder.setAction(action);
		if (seat == member.getRole().getSeat()) {
			builder.setCurrentSeat(seat);
			menjiBill.setSeat(seat);

		} else {
			builder.setCurrentSeat(member.getRole().getSeat());
			menjiBill.setSeat(member.getRole().getSeat());
		}
		builder.setChip(member.getChip());

		if (member.getRole().getStatus() != PlayerState.PS_WATCH_VALUE) {
			table.getMenjiBillList().add(menjiBill);
		}
		builder.setParam(table.getParam());
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		member.getRole().setStatus(PlayerState.PS_WATCH_VALUE);
		table.getGame().getSpriteMap().get(member.getRole().getRole().getRid()).setStatus(PlayerState.PS_WATCH_VALUE);

		ExpCsv expCsv = expCache.getConfig(member.getRole().getRole()
				.getLevel());
		if (member.getRole().isRobot()) {
			updateBonus(table.getGame(), member.getChip(), false);
		}

		if (!member.getRole().isRobot() && roleFunction.isPoChan(member.getRole().getRole().getGold(), member
				.getRole().getRole().getGoldPot(), member.getChip())) {
			AtomicIntegerArray log = getRoomCountCacheByRoomType(table
					.getGame().getRoomType());
			log.incrementAndGet(1);// 破产数
			roleBrokeLogDao.insertLog(member.getRole().getRole().getRid(), table.getGame().getGameType(), table.getGame().getRoomType());
		}
		
		if(member.getRole().isRobot()){
			member.getRole().getRole().setGold(member.getRole().getRole().getGold() - member.getChip());
//			npcFunction.updateGainOrLoss(member.getRole().getRole().getRid(),- member.getChip(),
//					table.getGame().getGameType(),table.getGame().getRoomType(),MoneyEvent.MENJI);
		}else{
			roleFunction.goldSub(member.getRole().getRole(), member.getChip(),
					MoneyEvent.MENJI, false);
			roleFunction.expAdd(member.getRole().getRole(),
					expCsv.getMenjiLoseExp(), true);
		}
		LogUtil.info(member.getRole().getRole().getNick() + "减少了"
				+ member.getChip() + "金币,当前金币为"
				+ member.getRole().getRole().getGold());
		menjiDataFunction.updateMenjiData(member.getRole().getRole().getRid(),
				ZTMenji.getCardType(member.getPai()), 0, false);
		if(member.getRole().isRobot()){
			//设置机器人场数信息
			member.getRole().setRobotLost();
			npcFunction.setRobotLost(member.getRole().getRole().getRid(), table.getGame().getGameType(), table.getGame().getRoomType());
		}

		// 任务检测
		missionFunction.checkTaskFinish(member.getRole().getRole().getRid(),TaskType.daily_task,
				MissionType.TIMES, GameType.MENJI);

		if (seat == member.getRole().getSeat()) {
			table.setLastPlaySeat(table.getNextPlaySeat());
			table.setNextSeat(table.getNextPlaySeat());
			if (!table.getWaiter().contains(member.getRole().getSeat())) {
				table.getWaiter().add(member.getRole().getSeat());
			}
			// 已经操作过，重新计算本轮的座位号
			boolean isAddRound = table.setRoundLists(member);
			if (table.getWaiter().size() >= table.getMembers().size() - 1) {
				endTable(table);
				return;
			}
			// 用户做出弃牌操作影响AI数值
			//AIUpdateByFold(table, member);
			if (isAddRound) {// 结束一轮，跟新AI的概率值
				//AIUpdateByEndRound(table);
			}
			// AI做出看牌或者弃牌的判定
			//AIOutsideOperation(table, member);
			LogUtil.error("玩家..."+member.getRole().getRole().getNick() + "弃牌，剩余金币" + (member.getRole().getRole().getGold() - member.getChip()));
			if (!needCompete(table)) {
				tableToWait(table, table.getLastPlaySeat(),
						table.getNextPlaySeat(), handleType,
						System.currentTimeMillis() + 500);
			}

		} else {
			if (!table.getWaiter().contains(member.getRole().getSeat())) {
				table.getWaiter().add(member.getRole().getSeat());
			}
			// 已经操作过，重新计算本轮的座位号
			table.setRoundLists(member);
			if (table.getWaiter().size() >= table.getMembers().size() - 1) {
				endTable(table);
				return;
			} else {
				//AIUpdateByFold(table, member);
				//AIOutsideOperation(table, member);
			}
		}

	}

	/**
	 * 处理比牌
	 * 
	 * @param table
	 * @param role
	 * @param target
	 */
	public void dealCompete(ZTMenjiTable table, ZTMenjiRole role, int target) {
		LogUtil.error("玩家"+role.getRole().getRole().getNick() + "操作前的金币：" + role.getRole().getRole().getGold());
		table.setQueueWaitType(0);
		table.setParam(target);

		// 用户是否看牌的倍数
		int multiple = 1;
		// 用户比牌需要投入的金额
		int roleNeedInput = 0;
		// 用户拥有的金额是否比需投入的金额大
		boolean haveGold;
		if (role.getLook() == 1) {
			multiple = 2;
		}
		if (role.getRole().getRole().getGold() - role.getChip() >= table
				.getBetsNum() * 2 * multiple) {
			roleNeedInput = table.getBetsNum() * 2 * multiple;
			haveGold = true;
		} else {
			roleNeedInput = role.getRole().getRole().getGold() - role.getChip();
			haveGold = false;
		}
		// 用户截止当前投注的注额
		role.setChip(role.getChip() + roleNeedInput);
		// 奖池总注额
		table.setAllBetsNum(table.getAllBetsNum() + roleNeedInput);

		ZTMenjiRole targetRole = new ZTMenjiRole();
		for(ZTMenjiRole ztMenjiRole : table.getMembers()){
			if(ztMenjiRole == null){
				continue;
			}
			if(ztMenjiRole.getRole() == null){
				LogUtil.info("ztMenjiRole is null");
			}
			if(ztMenjiRole.getRole() != null && ztMenjiRole.getRole().getSeat() == target){
				targetRole = ztMenjiRole;
			}
		}
		
		int seat = role.getRole().getSeat();
		LogUtil.error("发动比牌的玩家座位号为：" + seat + ",玩家的名称为："
				+ role.getRole().getRole().getNick());
		LogUtil.error("	被比牌的玩家座位号为：" + target + ",玩家的名称为："
				+ targetRole.getRole().getRole().getNick());
		int lastNext = table.getNextPlaySeat();

		MenJiAction action = MenJiAction.MJ_COMPETE;
		int handleType = HandleType.MENJI_GAME;

		
		// 设置玩家1和玩家2为已比牌
		role.setCompete(1);
		targetRole.setCompete(1);
		GMsg_12013006.Builder builder = GMsg_12013006.newBuilder();
		GMenJiPai.Builder builder2 = GMenJiPai.newBuilder();
		for (Integer data : role.getPai()) {
			builder2.addPaiValue(data % 13 == 0 ? 13 : data % 13);
			builder2.addPaiColor(data % 13 == 0 ? (data / 13 - 1) : data / 13);
		}
		builder2.setPaiType(ZTMenji.getCardType(role.getPai()));
		builder.setCurrentPai(builder2);
		builder.setLook(role.getLook());
		builder.setAllChips(table.getAllBetsNum());
		builder.setCurrentChips(table.getBetsNum());

		GMenJiPai.Builder builder3 = GMenJiPai.newBuilder();
		for (Integer data : targetRole.getPai()) {
			int value = data % 13 == 0 ? 13 : data % 13;
			builder3.addPaiValue(value);
			builder3.addPaiColor(data % 13 == 0 ? (data / 13 - 1) : data / 13);
		}
		builder3.setPaiType(ZTMenji.getCardType(targetRole.getPai()));
		builder.setTargetPai(builder3);
		List<MJCardType> menJiCardTypeList = new ArrayList<MJCardType>();
		menJiCardTypeList.add(builder2.getPaiType());
		menJiCardTypeList.add(builder3.getPaiType());
		boolean whoBigFals;
		LogUtil.error("不存在特殊牌最大");
		whoBigFals = ZTMenji.getWhoBig(menJiCardTypeList,
		builder2.getPaiValueList(), builder3.getPaiValueList());
		if (whoBigFals) {
			builder.setResult(1);
			if (!table.getWaiter().contains(target)) {
				table.getWaiter().add(target);
			}
			// 已经操作过，重新计算本轮的座位号
			if (haveGold) {
				table.setRoundLists(role);
				table.setRoundLists(targetRole);
			} else {
				table.setRoundLists(targetRole);
			}
			// 座位要重新计算
			// lastNext = seat;
			ExpCsv expCsv = expCache.getConfig(targetRole.getRole().getRole()
					.getLevel());
			if (targetRole.getRole().isRobot()) {
				updateBonus(table.getGame(), targetRole.getChip(), false);
			}
            
			//插入破产日志
			if ( !targetRole.getRole().isRobot() && roleFunction.isPoChan(targetRole.getRole().getRole().getGold(),
					targetRole.getRole().getRole().getGoldPot(),
					targetRole.getChip())) {
				AtomicIntegerArray log = getRoomCountCacheByRoomType(table
						.getGame().getRoomType());
				log.incrementAndGet(1);// 破产数
				roleBrokeLogDao.insertLog(targetRole.getRole().getRole().getRid(), table.getGame().getGameType(), table.getGame().getRoomType());
			}

			
			if(targetRole.getRole().isRobot()){
				targetRole.getRole().getRole().setGold(targetRole.getRole().getRole().getGold() - targetRole.getChip());
//				npcFunction.updateGainOrLoss(targetRole.getRole().getRole().getRid(), -targetRole.getChip(),
//						table.getGame().getGameType(),table.getGame().getRoomType(),MoneyEvent.MENJI);
			}else{
				roleFunction.goldSub(targetRole.getRole().getRole(),
						targetRole.getChip(), MoneyEvent.MENJI, false);
				roleFunction.expAdd(targetRole.getRole().getRole(),
						expCsv.getMenjiLoseExp(), true);
				// 任务检测
				missionFunction.checkTaskFinish(targetRole.getRole().getRole().getRid(),TaskType.daily_task,
						MissionType.TIMES, GameType.MENJI);
			}
			
			LogUtil.info(targetRole.getRole().getRole().getNick() + "减少了"
					+ targetRole.getChip() + "金币,当前金币为"
					+ targetRole.getRole().getRole().getGold());
			MenjiBill menjiBill = new MenjiBill();
			menjiBill.setRid(targetRole.getRole().getRole().getRid());
			menjiBill.setHandPai(targetRole.getPai());
			menjiBill.setGold(targetRole.getChip());
			menjiBill.setCompete(targetRole.getCompete());
			menjiBill.setSeat(target);
			menjiBill.setName(targetRole.getRole().getRole().getNick());
			menjiBill.setRobot(targetRole.getRole().isRobot());
			table.getMenjiBillList().add(menjiBill);
			menjiDataFunction.updateMenjiData(targetRole.getRole().getRole()
					.getRid(), builder3.getPaiType(), 0, false);
			if(targetRole.getRole().isRobot()){
				//设置机器人场数信息
				targetRole.getRole().setRobotLost();
				npcFunction.setRobotLost(targetRole.getRole().getRole().getRid(), table.getGame().getGameType(), table.getGame().getRoomType());

			}

		} else {
			table.setRoundLists(role);
			builder.setResult(2);
			if (!table.getWaiter().contains(seat)) {
				table.getWaiter().add(seat);
			}
			// 已经操作过，重新计算本轮的座位号
			table.setRoundLists(role);
			ExpCsv expCsv = expCache.getConfig(role.getRole().getRole()
					.getLevel());
			
			//插入破产日志
			if ( !role.getRole().isRobot() && roleFunction.isPoChan(role.getRole().getRole().getGold(),
					role.getRole().getRole().getGoldPot(),
					role.getChip())) {
				AtomicIntegerArray log = getRoomCountCacheByRoomType(table
						.getGame().getRoomType());
				log.incrementAndGet(1);// 破产数
				roleBrokeLogDao.insertLog(role.getRole().getRole().getRid(), table.getGame().getGameType(), table.getGame().getRoomType());
			}
			
			if (role.getRole().isRobot()) {
				updateBonus(table.getGame(), role.getChip(), false);
				role.getRole().getRole().setGold(role.getRole().getRole().getGold() - role.getChip());
//				npcFunction.updateGainOrLoss(role.getRole().getRole().getRid(), -role.getChip()
//						,table.getGame().getGameType(),table.getGame().getRoomType(),MoneyEvent.MENJI);
			}else{
				roleFunction.goldSub(role.getRole().getRole(), role.getChip(),
						MoneyEvent.MENJI, false);
				// 任务检测
				missionFunction.checkTaskFinish(targetRole.getRole().getRole().getRid(),TaskType.daily_task,
						MissionType.TIMES, GameType.MENJI);
				if(expCsv != null){
					LogUtil.info("exp have");
				}else{
					LogUtil.info("exp null");
				}
				if(role.getRole().getRole() != null){
					LogUtil.info("targetRole.getRole().getRole() have");
				}else{
					LogUtil.info("targetRole.getRole().getRole() null");
				}
				if(targetRole.getRole().isRobot()){
					LogUtil.info("robot");
				}else{
					LogUtil.info("not robot");
				}
				roleFunction.expAdd(role.getRole().getRole(),
						expCsv.getMenjiLoseExp(), true);
			}
			
			LogUtil.info(role.getRole().getRole().getNick() + "减少了"
					+ role.getChip() + "金币,当前金币为"
					+ role.getRole().getRole().getGold());
			MenjiBill menjiBill = new MenjiBill();
			menjiBill.setRid(role.getRole().getRole().getRid());
			menjiBill.setHandPai(role.getPai());
			menjiBill.setGold(role.getChip());
			menjiBill.setCompete(role.getCompete());
			menjiBill.setSeat(seat);
			menjiBill.setName(role.getRole().getRole().getNick());
			menjiBill.setRobot(targetRole.getRole().isRobot());
			table.getMenjiBillList().add(menjiBill);
			menjiDataFunction.updateMenjiData(
					role.getRole().getRole().getRid(), builder2.getPaiType(),
					0, false);
			if(role.getRole().isRobot()){
				//设置机器人场数信息
				role.getRole().setRobotLost();
				npcFunction.setRobotLost(role.getRole().getRole().getRid(), table.getGame().getGameType(), table.getGame().getRoomType());

			}
		}

		builder.setCurrentSeat(seat);
		builder.setTargetSeat(target);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());
		LogUtil.error("玩家..."+role.getRole().getRole().getNick() + "比牌，减少了"+roleNeedInput+"剩余金币" + (role.getRole().getRole().getGold() - role.getChip()));
		if (table.getWaiter().size() >= table.getMembers().size() - 1) {
			if (whoBigFals) {
				if (haveGold) {
					if (target == lastNext) {
						lastNext = table.getNextPlaySeat();
					}
				} else {
					lastNext = seat;
				}
			}

			processAction(table, action, lastNext, handleType,
					System.currentTimeMillis() + 500, role.getLook(), role
							.getRole().getRole().getGold()
							- role.getChip());
			int handlerType = HandleType.MENJI_END;
			tableToWait1(table, handlerType,
					System.currentTimeMillis() + 500 + 3000);
			return;
		}
		if (!needCompete(table)) {
			if (whoBigFals) {
				if (haveGold) {
					if (target == lastNext) {
						lastNext = table.getNextPlaySeat();
					}
				} else {
					lastNext = role.getRole().getSeat();
				}
			}
			processAction(table, action, lastNext, handleType,
					System.currentTimeMillis() + 3500, role.getLook(), role
							.getRole().getRole().getGold()
							- role.getChip());
		}

	}

	/**
	 * 结算
	 * 
	 * @param table
	 */
	public void endTable(ZTMenjiTable table) {
		table.setQueueWaitType(0);
		table.getGame().setStatus(GameStatus.END);
		table.getExitMap().clear();
		LogUtil.info(table.getGame().getRoomId() + " end");
		GMsg_12013008.Builder builder = GMsg_12013008.newBuilder();

		// G_T_C 得到该角色的对局信息
		List<Object[]> roleInfos = new ArrayList<>();
		int aiGold = 0;
		//统计所有玩家输的钱
		int playLoss = 0;
		Long winRid = 0l;
		int realPlayerCount = 0;
		int exchange = 0;
		Role winRole = new Role();
		//赢的人是否机器人
		MenjiRoomCsv menjiRoomCsv = menjiRoomCache.getConfig(table.getGame().getRoomType());
		boolean winIsRobot = false;
		for (ZTMenjiRole ztMenjiRole : table.getMembers()) {
			if(ztMenjiRole == null){
				continue;
			}
			if (ztMenjiRole.getRole() != null) {
				if (ztMenjiRole.getRole().getStatus() != PlayerState.PS_WATCH_VALUE) {
					if (ztMenjiRole.getRole().getSeat() == table
							.getNextPlaySeat()) {
						if(ztMenjiRole.getRole().isRobot()){
							winIsRobot = true;
							winRole = ztMenjiRole.getRole().getRole();
							break;
						}
						
					}
				}
			}
		}
		int totalPlayerCount = table.getMenjiBillList().size();
		for (ZTMenjiRole ztMenjiRole : table.getMembers()) {
			if(ztMenjiRole == null){
				continue;
			}
			if (ztMenjiRole.getRole() != null) {
				if (ztMenjiRole.getRole().getStatus() != PlayerState.PS_WATCH_VALUE) {
					if (ztMenjiRole.getRole().getSeat() == table
							.getNextPlaySeat()) {
						menjiDataFunction.updateMenjiData(ztMenjiRole.getRole()
								.getRole().getRid(),
								ZTMenji.getCardType(ztMenjiRole.getPai()),
								table.getAllBetsNum(), true);
						if(ztMenjiRole.getRole().isRobot()){
							//设置机器人场数信息
							ztMenjiRole.getRole().setRobotWin();
							npcFunction.setRobotWin(ztMenjiRole.getRole().getRole().getRid(), table.getGame().getGameType(), table.getGame().getRoomType());
						}else{
							realPlayerCount++;
						}
						totalPlayerCount++;

						ExpCsv expCsv = expCache.getConfig(ztMenjiRole
								.getRole().getRole().getLevel());
						if (ztMenjiRole.getRole().isRobot()) {
							updateBonus(table.getGame(), table.getAllBetsNum()
									- ztMenjiRole.getChip(), true);
						}
						
						if(table.getGame().getRoomType() == RoomType.MENJI_EXCHANGE_ONE || table.getGame().getRoomType() == RoomType.MENJI_EXCHANGE_TWO ){
							//兑换成奖劵，AI不需要存库
							exchange = (int) Math.floor(table.getAllBetsNum()/ menjiRoomCsv.getExchangeLottery());
							exchange = exchange > 0 ? exchange : 1;
						}
						if(ztMenjiRole.getRole().isRobot()){
							LogUtil.info(""+ztMenjiRole.getRole().getRole().getGold());
							LogUtil.info(""+(table.getAllBetsNum() - ztMenjiRole.getChip()));
							LogUtil.info(""+ztMenjiRole.getRole().getRole().getGold() + (table.getAllBetsNum() - ztMenjiRole.getChip()));
							ztMenjiRole.getRole().getRole().setGold(ztMenjiRole.getRole().getRole().getGold() + (table.getAllBetsNum() - ztMenjiRole.getChip()));
							aiGold += (table.getAllBetsNum() - ztMenjiRole.getChip());
//							if(winIsRobot){
//								npcFunction.updateGainOrLoss(ztMenjiRole.getRole().getRole().getRid(), table.getAllBetsNum() - ztMenjiRole.getChip()
//										,table.getGame().getGameType(),table.getGame().getRoomType(),MoneyEvent.MENJI);
//							}
						}else{
							Tax tax = taxFunction.getTaxByTypes((byte)table.getGame().getGameType(), table.getGame().getRoomType());
							double taxCount = 0;
							if(tax != null){
								taxCount = (double)tax.getTaxCount() / 10000;
								LogUtil.info("抽水比例：" + taxCount);
							}
							if(taxCount != 0){
								int choushui = (int)((table.getAllBetsNum() - ztMenjiRole.getChip()) * taxCount);
								LogUtil.info("焖鸡抽水金额："+choushui);
								
								roleFunction.goldSub(ztMenjiRole.getRole().getRole(), choushui,
										MoneyEvent.MENJI_CHOUSHUI, true);
								AtomicIntegerArray roomCountLog = getRoomCountCacheByRoomType(
										table.getGame().getRoomType());
								roomCountLog.addAndGet(7,choushui);// 抽水
							}
							
							if(table.getGame().getRoomType() == RoomType.MENJI_EXCHANGE_ONE || table.getGame().getRoomType() == RoomType.MENJI_EXCHANGE_TWO){
								roleFunction.crystalAdd(ztMenjiRole.getRole().getRole(), exchange, MoneyEvent.MENJI);
							}else{
								roleFunction.goldAdd(ztMenjiRole.getRole().getRole(),
										table.getAllBetsNum() - ztMenjiRole.getChip(),
										MoneyEvent.MENJI, false);
							}
							
						}
						LogUtil.error("设置庄家座位号为：" + ztMenjiRole.getRole().getSeat());
						table.setOwner(ztMenjiRole.getRole().getSeat());
						if(!ztMenjiRole.getRole().isRobot()){
							roleFunction.expAdd(ztMenjiRole.getRole().getRole(),
									expCsv.getMenjiWinExp(), true);
						}
						winRid = ztMenjiRole.getRole().getRole().getRid();
						LogUtil.info(ztMenjiRole.getRole().getRole().getNick()
								+ "增加了"
								+ (table.getAllBetsNum() - ztMenjiRole
										.getChip()) + "金币,当前金币为"
								+ ztMenjiRole.getRole().getRole().getGold());

						GMenJiPlayerEnd.Builder builder2 = GMenJiPlayerEnd
								.newBuilder();
						GMenJiPai.Builder builder3 = GMenJiPai.newBuilder();

						builder2.setExp(0);

						if (table.getWaiter().contains(
								ztMenjiRole.getRole().getSeat())) {
							builder2.setGold(-ztMenjiRole.getChip());
							builder2.setType(1);
						} else {
							if(table.getGame().getRoomType() == RoomType.MENJI_EXCHANGE_ONE || table.getGame().getRoomType() == RoomType.MENJI_EXCHANGE_TWO ){
								builder2.setType(2);
								builder2.setGold(exchange);
							}else{
								builder2.setType(1);
								Tax tax = taxFunction.getTaxByTypes((byte)table.getGame().getGameType(), table.getGame().getRoomType());
								double taxCount = 0;
								if(tax != null){
									taxCount = (double)tax.getTaxCount() / 10000;
								}
								builder2.setGold(table.getAllBetsNum() - (int)((table.getAllBetsNum() - ztMenjiRole.getChip()) * taxCount));
							}
							
						}
						if (ztMenjiRole.getCompete() == 1) {
							builder2.setShow(1);
						} else {
							builder2.setShow(0);
						}
						// int[] rids = new int[3];
						// int[] color = new int[3];
						// int i = 0;
						// for (Integer data : ztMenjiRole.getPai()) {
						// rids[i] = data % 13 == 0 ? 13 : data % 13;
						// color[i] = rids[i] == 13 ? (data / 13 - 1) : data /
						// 13;
						// i = i + 1;
						// }
						// ZTMenji.sortBigAndSmallCard(color, rids);
						// for (int j = 0; j < 3; j++) {
						// builder3.addPaiColor(color[j]);
						// builder3.addPaiValue(rids[j]);
						// LogUtil.error("rids[j]"+rids[j]);
						// }
						for (Integer data : ztMenjiRole.getPai()) {
							int value = data % 13 == 0 ? 13 : data % 13;
							builder3.addPaiValue(value);
							builder3.addPaiColor(data % 13 == 0 ? (data / 13 - 1)
									: data / 13);
						}
						builder2.setName(ztMenjiRole.getRole().getRole()
								.getNick());
						builder3.setPaiType(ZTMenji.getCardType(ztMenjiRole
								.getPai()));
						builder2.setHandPai(builder3);
						builder2.setSeat(ztMenjiRole.getRole().getSeat());
						builder.addEndInfo(builder2);

						long rid = ztMenjiRole.getRole().getRole().getRid();
						// 牌型
						missionFunction.checkTaskFinish(rid,TaskType.daily_task,
								MissionType.CARD_TYPE, GameType.MENJI,
								builder3.getPaiType());
						// 胜
						missionFunction.checkTaskFinish(rid,TaskType.daily_task, MissionType.WIN,
								GameType.MENJI);
						// 连胜
						missionFunction.checkTaskFinish(rid,TaskType.daily_task,
								MissionType.CONTINUE_WIN, GameType.MENJI, true);

						// 任务检测
						missionFunction.checkTaskFinish(rid,TaskType.daily_task, MissionType.TIMES,
								GameType.MENJI);

						// G_T_C add 日志处理
						GameRole gRole = ztMenjiRole.getRole();
						Role role = gRole.getRole();
						Object[] objs = menjiLogDao.getRoleInfo(role.getRid(),
								role.getNick(), gRole.getSeat(),
								gRole.isRobot(), ztMenjiRole.getChip(),
								table.getAllBetsNum() - ztMenjiRole.getChip(),
								ztMenjiRole.getPai(), table.getOwner());
						roleInfos.add(objs);

						break;
					}

				}

			}

		}
		for (int i = 0; i < table.getMenjiBillList().size(); i++) {
			MenjiBill menjiBill = table.getMenjiBillList().get(i);
			GMenJiPlayerEnd.Builder builder2 = GMenJiPlayerEnd.newBuilder();
			GMenJiPai.Builder builder3 = GMenJiPai.newBuilder();
			builder2.setExp(0);
			builder2.setGold(-menjiBill.getGold());
			builder2.setType(1);
			builder2.setName(menjiBill.getName());
			if (menjiBill.getCompete() == 1) {
				builder2.setShow(1);
			} else {
				builder2.setShow(0);
			}
			// G_T_C add 日志处理
			if(menjiBill.isRobot()){
				aiGold -= menjiBill.getGold();
				if(!winIsRobot){
					npcFunction.updateGainOrLoss(menjiBill.getRid(), -menjiBill.getGold(), table.getGame().getGameType(), table.getGame().getRoomType(), MoneyEvent.MENJI);
				}
			}else{
				playLoss += menjiBill.getGold();
				realPlayerCount++;
			}
			Object[] objs = menjiLogDao.getRoleInfo(menjiBill.getRid(),
					menjiBill.getName(), menjiBill.getSeat(),
					menjiBill.isRobot(), menjiBill.getGold(),
					-menjiBill.getGold(), menjiBill.getHandPai(),
					table.getOwner());
			roleInfos.add(objs);

			Role role = roleFunction.getRoleByRid(menjiBill.getRid());
			
			Game game = table.getGame();
			if (role != null && game != null) {
				roomLogFunction.dealRoomRoleLoginLog(game.getGameType(), role,
						role.getLastLoginIp(), 1);
			}
			// int[] rids = new int[3];
			// int[] color = new int[3];
			// int h = 0;
			// for (Integer data : menjiBill.getHandPai()) {
			// rids[h] = data % 13 == 0 ? 13 : data % 13;
			// color[h] = data / 13;
			// h = h + 1;
			// }
			// ZTMenji.sortBigAndSmallCard(color, rids);
			// for (int j = 0; j < 3; j++) {
			// builder3.addPaiColor(color[j]);
			// builder3.addPaiValue(rids[j]);
			// LogUtil.error("rids[j]"+rids[j]);
			// }
			for (Integer data : menjiBill.getHandPai()) {
				int value = data % 13 == 0 ? 13 : data % 13;
				builder3.addPaiValue(value);
				builder3.addPaiColor(data % 13 == 0 ? (data / 13 - 1)
						: data / 13);
			}
			builder3.setPaiType(ZTMenji.getCardType(menjiBill.getHandPai()));
			builder2.setHandPai(builder3);
			builder2.setSeat(menjiBill.getSeat());
			builder.addEndInfo(builder2);

			// 牌型
			missionFunction.checkTaskFinish(menjiBill.getRid(),TaskType.daily_task,
					MissionType.CARD_TYPE, GameType.MENJI,
					builder3.getPaiType());
			// 连胜终止
			missionFunction.checkTaskFinish(menjiBill.getRid(),TaskType.daily_task,
					MissionType.CONTINUE_WIN, GameType.MENJI, false);
		}
		if(winIsRobot && playLoss != 0){
			npcFunction.updateGainOrLoss(winRole.getRid(), playLoss, table.getGame().getGameType(), table.getGame().getRoomType(), MoneyEvent.MENJI);
		}
		builder.setPower(table.getAllBetsNum());
		builder.setSeat(table.getNextPlaySeat());

		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		table.getGame().setStatus(GameStatus.END_REWARD);
		table.getGame().setEndTime(System.currentTimeMillis());

		// G_T_C 日志插入
		String roleInfo = StringUtil.listArrayToString(roleInfos,
				StringUtil.DELIMITER_BETWEEN_ITEMS,
				StringUtil.DELIMITER_INNER_ITEM);
		LogUtil.debug(roleInfo);
		MenjiLog log = menjiLogDao.getMenjiLog(table.getGame(), roleInfo,
				winRid + "", aiGold,totalPlayerCount,realPlayerCount);
		menjiLogDao.addLog(log);

		for (ZTMenjiRole ztMenjiRole : table.getMembers()) {
			if(ztMenjiRole == null){
				continue;
			}
			if (ztMenjiRole.getRole() != null
					&& ztMenjiRole.getRole().isRobot()) {
				roomFunction.doGameEnd();

			}
			if (ztMenjiRole.getRole() != null
					&& !ztMenjiRole.getRole().isRobot()) {
				// G_T_C 处理房间登录日志
				Role role2 = ztMenjiRole.getRole().getRole();
				Game game = table.getGame();
				if (role2 != null && game != null) {
					roomLogFunction.dealRoomRoleLoginLog(game.getGameType(),
							role2, "", 1);
				}
			}

		}
		if(table.getMembers().size() >= 2){
			boolean allRobot = true;
			for (ZTMenjiRole ztMenjiRole : table.getMembers()) {
				if(ztMenjiRole == null){
					continue;
				}
				if(ztMenjiRole != null && ztMenjiRole.getRole() != null && !ztMenjiRole.getRole().isRobot()){
					allRobot = false;
					break;
				}
				
			}
			if(allRobot){
				LogUtil.info("焖鸡剩下多个人并且全是AI " + table.getGame().getRoomId() + " END!");
				roomFunction.endGame(table.getGame());
				return;
			}
		}
	}

	/**
	 * 处理轮局结束
	 * 
	 * @param table
	 */
	public boolean needCompete(ZTMenjiTable table) {
		boolean fals = false;
		MenjiRoomCsv menjiRoomCsv = menjiRoomCache.getConfig(table.getGame()
				.getRoomType());
		if (table.getRound() - 1 == menjiRoomCsv.getRound()) {// 轮数满了
			table.setBetsNum(0);
			table.setRound(table.getRound() - 1);
			boolean teshuFals = false;// 是否存在特殊
			boolean baoziFals = false;// 是否存在豹子
			int index = 0;
			if (table.getMembers().size() - table.getWaiter().size() == 1) {
				endTable(table);
			} else {
				for (int i = 0; i < table.getMembers().size()
						- table.getWaiter().size() - 1; i++) {
					table.setLastPlaySeat(table.getNextPlaySeat());
					table.setNextSeat(table.getNextPlaySeat());
				}
				if (teshuFals && baoziFals) {
					for (ZTMenjiRole ztMenjiRole : table.getMembers()) {
						if(ztMenjiRole == null){
							continue;
						}
						if (ztMenjiRole.getRole() != null
								&& ztMenjiRole.getRole().getSeat() != index) {
							if (!table.getWaiter().contains(
									ztMenjiRole.getRole().getSeat())) {
								table.getWaiter().add(
										ztMenjiRole.getRole().getSeat());
							}
						}
					}
					endTable(table);
				}
				for (int i = 0; i < table.getMembers().size()
						- table.getWaiter().size() - 1; i++) {
					ZTMenjiRole targetRole1 = table.getMembers().get(
							table.getLastPlaySeat() - 1);
					if (targetRole1 != null && targetRole1.getRole() != null) {
						dealCompete(table, targetRole1, table.getNextPlaySeat());
					}

				}
			}
			fals = true;
		}
		return fals;
	}

	/**
	 * AI跟注
	 * 
	 * @param table
	 * @param role
	 */
	public boolean AIDealFollow(ZTMenjiTable table, ZTMenjiRole role) {
		LogUtil.error("AI进行跟注判定");
		boolean falg = false;
		if (role.getRole().isRobot()) {
			//isToDealFollow(table, role);
			falg = true;
		}
		return falg;
	}

	/**
	 * AI加注
	 */
	public boolean AIDealAdd(ZTMenjiTable table, ZTMenjiRole role) {
		LogUtil.error("AI进行加注判定");
		boolean falg = false;
		if (role.getRole().isRobot()) {
			MenjiRoomCsv menjiRoomCsv = menjiRoomCache.getConfig(table
					.getGame().getRoomType());
			List<Integer> list = StringUtil.stringToList(
					menjiRoomCsv.getRaiseOdds(),
					StringUtil.DELIMITER_BETWEEN_ITEMS, Integer.class);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) == table.getBetsNum()) {
					if (i + 1 < list.size()) {
						LogUtil.error("有加注操作");
						dealAdd(table, role, list.get(i + 1));
						falg = true;
					}
				}
			}
		}
		return falg;
	}

	/**
	 * AI比牌
	 * 
	 * @param table
	 * @param role
	 */
	public boolean AIDealCompete(ZTMenjiTable table, ZTMenjiRole role) {
		LogUtil.error("AI进行比牌判定");
		boolean falg = false;
		if (role.getRole().isRobot()) {
			int target;
			if (table.getMembers().size() - table.getWaiter().size() >= 3) {
				if (table.getNextPlaySeat() == table.getOwner()) {
					table.setLastPlaySeat(table.getNextPlaySeat());
					target = table.getNextPlaySeat();
					table.setLastPlaySeat(role.getRole().getSeat());
				} else {
					target = table.getNextPlaySeat();
				}
				dealCompete(table, role, target);
				falg = true;
			} else if (table.getMembers().size() - table.getWaiter().size() == 2) {
				target = table.getNextPlaySeat();
				dealCompete(table, role, target);
				falg = true;
			} else {
				endTable(table);
			}
		}
		return falg;
	}

	/**
	 * AI回合外操作
	 * 
	 * @param table
	 * @param role
	 */
	public void AIOutsideOperation(ZTMenjiTable table, ZTMenjiRole member) {
		for (ZTMenjiRole role : table.getMembers()) {
			if (role != null && role.getRole() != null && !member.equals(role)) {
				if (role.getRole().isRobot()) {
					if (!table.getWaiter().contains(role.getRole().getSeat())) {
						ZTMenjiAI ztMenjiAI = role.getZtMenjiAI();
						if ((ztMenjiAI.getLookProbability() >= 100
								|| ztMenjiAI.getLookProbability() >= (int) Math
										.ceil(Math.random() * 100) || ztMenjiAI
								.getLookValue() < 1) && role.getLook() != 1) {
							LogUtil.error("增加AI看牌");
							ConcurrentHashMap<ZTMenjiRole, Integer> ztmenjiRoleMap = new ConcurrentHashMap<ZTMenjiRole, Integer>();
							ztmenjiRoleMap.put(role, 1);
							AIOperationmap.put(table.getGame().getRoomId(), ztmenjiRoleMap);
						}

						if ((ztMenjiAI.getFoldProbability() >= 100 || ztMenjiAI
								.getFoldProbability() >= (int) Math.ceil(Math
								.random() * 100))
								&& !role.isGoodPai()) {
							LogUtil.error("增加AI弃牌");
							ConcurrentHashMap<ZTMenjiRole, Integer> ztmenjiRoleMap = new ConcurrentHashMap<ZTMenjiRole, Integer>();
							ztmenjiRoleMap.put(role, 2);
							AIOperationmap.put(table.getGame().getRoomId(), ztmenjiRoleMap);
						}
					}

				}
			}
		}

	}

	/**
	 * 其他玩家看牌后修改概率值
	 */
	public void AIUpdateByLook(ZTMenjiTable table, ZTMenjiRole member) {
		for (ZTMenjiRole role : table.getMembers()) {
			if (role != null && role.getRole() != null && !member.equals(role)) {
				if (!table.getWaiter().contains(role.getRole().getSeat())) {
					if (role.getRole().isRobot()) {
						ZTMenjiAI ztMenjiAI = role.getZtMenjiAI();
						MenjiAICsv menjiAICsv = menjiAICache
								.getConfig(ztMenjiAI.getId());
						ztMenjiAI.setAddProbability(ztMenjiAI
								.getAddProbability()
								+ menjiAICsv.getRrFix4()
								/ 100);
						ztMenjiAI.setCompeteProbability(ztMenjiAI
								.getCompeteProbability()
								+ menjiAICsv.getComrFix4() / 100);
						ztMenjiAI.setFoldProbability(ztMenjiAI
								.getFoldProbability()
								+ menjiAICsv.getFrFix4()
								/ 100);
						ztMenjiAI.setLookProbability(ztMenjiAI
								.getLookProbability()
								+ menjiAICsv.getCrFix4()
								/ 100);
						ztMenjiAI.setLookValue(ztMenjiAI.getLookValue() - 1);
					}
				}
			}
		}
	}

	/**
	 * 其他玩家弃牌后修改概率值
	 */
	public void AIUpdateByFold(ZTMenjiTable table, ZTMenjiRole member) {
		LogUtil.error("玩家弃牌后修改概率值");
		for (ZTMenjiRole role : table.getMembers()) {
			if (role != null && role.getRole() != null && !member.equals(role)) {
				if (!table.getWaiter().contains(role.getRole().getSeat())) {
					if (role.getRole().isRobot()) {
						ZTMenjiAI ztMenjiAI = role.getZtMenjiAI();
						MenjiAICsv menjiAICsv = menjiAICache
								.getConfig(ztMenjiAI.getId());
//						LogUtil.error("修改加注率、比牌率、棋牌率、看牌率以及看牌值，分别从 "
//								+ ztMenjiAI.getAddProbability() + "、"
//								+ ztMenjiAI.getCompeteProbability() + "、"
//								+ ztMenjiAI.getFoldProbability() + "、"
//								+ ztMenjiAI.getLookProbability() + "、"
//								+ ztMenjiAI.getLookValue() + "变为");
						ztMenjiAI.setAddProbability(ztMenjiAI
								.getAddProbability()
								+ menjiAICsv.getRrFix1()
								/ 100);
						ztMenjiAI.setCompeteProbability(ztMenjiAI
								.getCompeteProbability()
								+ menjiAICsv.getComrFix1() / 100);
						ztMenjiAI.setFoldProbability(ztMenjiAI
								.getFoldProbability()
								+ menjiAICsv.getFrFix1()
								/ 100);
						ztMenjiAI.setLookProbability(ztMenjiAI
								.getLookProbability()
								+ menjiAICsv.getCrFix1()
								/ 100);
						ztMenjiAI.setLookValue(ztMenjiAI.getLookValue() - 1);
//						LogUtil.error(ztMenjiAI.getAddProbability() + "、"
//								+ ztMenjiAI.getCompeteProbability() + "、"
//								+ ztMenjiAI.getFoldProbability() + "、"
//								+ ztMenjiAI.getLookProbability() + "、"
//								+ ztMenjiAI.getLookValue());

					}
				}
			}
		}
	}

	/**
	 * 其他玩家看牌后加注修改概率值
	 */
	public void AIUpdateByAdd(ZTMenjiTable table, ZTMenjiRole member) {
		LogUtil.error("其他玩家看牌后加注修改概率值");
		for (ZTMenjiRole role : table.getMembers()) {
			if (role != null && role.getRole() != null && !member.equals(role)) {
				if (!table.getWaiter().contains(role.getRole().getSeat())) {
					if (role.getRole().isRobot()) {
						ZTMenjiAI ztMenjiAI = role.getZtMenjiAI();
						MenjiAICsv menjiAICsv = menjiAICache
								.getConfig(ztMenjiAI.getId());
//						LogUtil.error("修改加注率、比牌率、棋牌率、看牌率以及看牌值，分别从 "
//								+ ztMenjiAI.getAddProbability() + "、"
//								+ ztMenjiAI.getCompeteProbability() + "、"
//								+ ztMenjiAI.getFoldProbability() + "、"
//								+ ztMenjiAI.getLookProbability() + "、"
//								+ ztMenjiAI.getLookValue() + "变为");
						ztMenjiAI.setAddProbability(ztMenjiAI
								.getAddProbability()
								+ menjiAICsv.getRrFix2()
								/ 100);
						ztMenjiAI.setCompeteProbability(ztMenjiAI
								.getCompeteProbability()
								+ menjiAICsv.getComrFix2() / 100);
						ztMenjiAI.setFoldProbability(ztMenjiAI
								.getFoldProbability()
								+ menjiAICsv.getFrFix2()
								/ 100);
						ztMenjiAI.setLookProbability(ztMenjiAI
								.getLookProbability()
								+ menjiAICsv.getCrFix2()
								/ 100);
						ztMenjiAI.setLookValue(ztMenjiAI.getLookValue() - 1);
//						LogUtil.error(ztMenjiAI.getAddProbability() + "、"
//								+ ztMenjiAI.getCompeteProbability() + "、"
//								+ ztMenjiAI.getFoldProbability() + "、"
//								+ ztMenjiAI.getLookProbability() + "、"
//								+ ztMenjiAI.getLookValue());
					}
				}
			}
		}
	}

	/**
	 * 其他玩家看牌后跟注修改概率值
	 */
	public void AIUpdateByFollow(ZTMenjiTable table, ZTMenjiRole member) {
		LogUtil.error("其他玩家看牌后跟注修改概率值");
		for (ZTMenjiRole role : table.getMembers()) {
			if (role != null && role.getRole() != null && !member.equals(role)) {
				if (!table.getWaiter().contains(role.getRole().getSeat())) {
					if (role.getRole().isRobot()) {
						ZTMenjiAI ztMenjiAI = role.getZtMenjiAI();
//						LogUtil.error("修改加注率、比牌率、棋牌率、看牌率以及看牌值，分别从 "
//								+ ztMenjiAI.getAddProbability() + "、"
//								+ ztMenjiAI.getCompeteProbability() + "、"
//								+ ztMenjiAI.getFoldProbability() + "、"
//								+ ztMenjiAI.getLookProbability() + "、"
//								+ ztMenjiAI.getLookValue() + "变为");
						MenjiAICsv menjiAICsv = menjiAICache
								.getConfig(ztMenjiAI.getId());
						ztMenjiAI.setAddProbability(ztMenjiAI
								.getAddProbability()
								+ menjiAICsv.getRrFix3()
								/ 100);
						ztMenjiAI.setCompeteProbability(ztMenjiAI
								.getCompeteProbability()
								+ menjiAICsv.getComrFix3() / 100);
						ztMenjiAI.setFoldProbability(ztMenjiAI
								.getFoldProbability()
								+ menjiAICsv.getFrFix3()
								/ 100);
						ztMenjiAI.setLookProbability(ztMenjiAI
								.getLookProbability()
								+ menjiAICsv.getCrFix3()
								/ 100);
						ztMenjiAI.setLookValue(ztMenjiAI.getLookValue() - 1);
//						LogUtil.error(ztMenjiAI.getAddProbability() + "、"
//								+ ztMenjiAI.getCompeteProbability() + "、"
//								+ ztMenjiAI.getFoldProbability() + "、"
//								+ ztMenjiAI.getLookProbability() + "、"
//								+ ztMenjiAI.getLookValue());
					}
				}
			}
		}
	}

	/**
	 * AI自己看完牌后修改概率值
	 */
	public void AIUpdateBySelfLook(ZTMenjiTable table, MJCardType mjCardType,
			ZTMenjiRole role, List<Integer> cardlist) {
		LogUtil.error("AI自己看完牌后修改概率值");
		int sum = 0;
		int peopleNum = 0;
		if (mjCardType.equals(MJCardType.BAO_ZI)) {
			sum = sum + 6 * 10000;
		} else if (mjCardType.equals(MJCardType.TONG_HUA_SHUN)) {
			sum = sum + 5 * 10000;
		} else if (mjCardType.equals(MJCardType.JIN_HUA)) {
			sum = sum + 4 * 10000;
		} else if (mjCardType.equals(MJCardType.SHUN_ZI)) {
			sum = sum + 3 * 10000;
		} else if (mjCardType.equals(MJCardType.DUI_ZI)) {
			sum = sum + 2 * 10000;
		} else if (mjCardType.equals(MJCardType.SAN_PAI)) {
			sum = sum + 1 * 10000;
		} else if (mjCardType.equals(MJCardType.TE_SHU)) {
			sum = sum + 0 * 10000;
		}
		if (ZTMenji.towCardWhoBug(cardlist.get(0), cardlist.get(1)) != 2) {
			if (ZTMenji.towCardWhoBug(cardlist.get(0), cardlist.get(2)) != 2) {
				sum = sum + cardlist.get(0);
			} else {
				sum = sum + cardlist.get(2);
			}
		} else {
			if (ZTMenji.towCardWhoBug(cardlist.get(1), cardlist.get(2)) != 2) {
				sum = sum + cardlist.get(1);
			} else {
				sum = sum + cardlist.get(2);
			}
		}
		for (int i = 1; i <= table.getMembers().size(); i++) {
			if (!table.getWaiter().contains(i)) {
				peopleNum = peopleNum + 1;
			}
		}
		sum = sum + peopleNum * 100;
		MenjiCardValueCsv menjiCardValueCsv = menjiAICardValueCache
				.getConfig(sum);
		if (menjiCardValueCsv != null) {
			ZTMenjiAI ztMenjiAI = role.getZtMenjiAI();
			LogUtil.error("AI三个数分别为：" + cardlist.get(0) + "," + cardlist.get(1)
					+ "," + cardlist.get(2));
			LogUtil.error("索引值为：" + sum);
//			LogUtil.error("修改加注率、比牌率、棋牌率、看牌率以及看牌值，分别从 "
//					+ ztMenjiAI.getAddProbability() + "、"
//					+ ztMenjiAI.getCompeteProbability() + "、"
//					+ ztMenjiAI.getFoldProbability() + "、"
//					+ ztMenjiAI.getLookProbability() + "、"
//					+ ztMenjiAI.getLookValue() + "变为");
			ztMenjiAI.setAddProbability(ztMenjiAI.getAddProbability()
					+ menjiCardValueCsv.getRrCardValueFix() / 100);
			ztMenjiAI.setCompeteProbability(ztMenjiAI.getCompeteProbability()
					+ menjiCardValueCsv.getComrCardValueFix() / 100);
			ztMenjiAI.setFoldProbability(ztMenjiAI.getFoldProbability()
					+ menjiCardValueCsv.getFrCardValueFix() / 100);
//			LogUtil.error(ztMenjiAI.getAddProbability() + "、"
//					+ ztMenjiAI.getCompeteProbability() + "、"
//					+ ztMenjiAI.getFoldProbability() + "、"
//					+ ztMenjiAI.getLookProbability() + "、"
//					+ ztMenjiAI.getLookValue());
		}

	}

	/**
	 * 一轮结束后修改概率值
	 * 
	 * @param table
	 */
	public void AIUpdateByEndRound(ZTMenjiTable table) {
		LogUtil.error("一轮结束后修改概率值");
		for (ZTMenjiRole role : table.getMembers()) {
			if (role != null && role.getRole() != null) {
				if (!table.getWaiter().contains(role.getRole().getSeat())) {
					if (role.getRole().isRobot()) {
						ZTMenjiAI ztMenjiAI = role.getZtMenjiAI();
//						LogUtil.error("修改加注率、比牌率、棋牌率、看牌率以及看牌值，分别从 "
//								+ ztMenjiAI.getAddProbability() + "、"
//								+ ztMenjiAI.getCompeteProbability() + "、"
//								+ ztMenjiAI.getFoldProbability() + "、"
//								+ ztMenjiAI.getLookProbability() + "、"
//								+ ztMenjiAI.getLookValue() + "变为");
						MenjiAICsv menjiAICsv = menjiAICache
								.getConfig(ztMenjiAI.getId());
						ztMenjiAI.setAddProbability(ztMenjiAI
								.getAddProbability()
								+ menjiAICsv.getRrFix5()
								/ 100);
						ztMenjiAI.setCompeteProbability(ztMenjiAI
								.getCompeteProbability()
								+ menjiAICsv.getComrFix5() / 100);
						ztMenjiAI.setFoldProbability(ztMenjiAI
								.getFoldProbability()
								+ menjiAICsv.getFrFix5()
								/ 100);
						ztMenjiAI.setLookProbability(ztMenjiAI
								.getLookProbability()
								+ menjiAICsv.getCrFix5()
								/ 100);
						ztMenjiAI.setLookValue(ztMenjiAI.getLookValue() - 1);
//						LogUtil.error(ztMenjiAI.getAddProbability() + "、"
//								+ ztMenjiAI.getCompeteProbability() + "、"
//								+ ztMenjiAI.getFoldProbability() + "、"
//								+ ztMenjiAI.getLookProbability() + "、"
//								+ ztMenjiAI.getLookValue());
					}
				}
			}
		}
	}

	public void isToDealFollow(ZTMenjiTable table, ZTMenjiRole role) {
		int target;
		if (role.getRole().getRole().getGold() - role.getChip() > (role
				.getLook() == 1 ? table.getBetsNum() * 2 : table.getBetsNum())) {
			dealFollow(table, role);
		} else if (table.getRound() == 1) {
			dealFold(table, role);
		} else {
			if (table.getMembers().size() - table.getWaiter().size() >= 3) {
				if (table.getNextPlaySeat() == table.getOwner()) {
					table.setLastPlaySeat(table.getNextPlaySeat());
					target = table.getNextPlaySeat();
					table.setLastPlaySeat(role.getRole().getSeat());
				} else {
					target = table.getNextPlaySeat();
				}
				dealCompete(table, role, target);
			} else if (table.getMembers().size() - table.getWaiter().size() == 2) {
				target = table.getNextPlaySeat();
				dealCompete(table, role, target);
			} else {
				endTable(table);
			}
		}
	}

	static {
		// 低级房统计初始化,中级房统计初始化,高级房统计初始化
		for (int i = 1; i <= GameRoom.MENJIROOMNUM; i++) {
			AtomicIntegerArray log = new AtomicIntegerArray(8);
			log.set(0, 0);// 对战数
			log.set(1, 0);// 破产数
			log.set(2, 0);// 房费数
			log.set(3, 0);// 庄家破产数
			log.set(4, 0);// 闲家破产数
			log.set(5, 0);// 普通破产数
			log.set(6, 0);// 困难破产数
			log.set(7, 0);// 抽水数
			roomCountMap.put(i, log);
		}
	}

	/**
	 * 根据房间号码拿到统计实体。由于出现并发获取，需要同步
	 * 
	 * @author G_T_C
	 * @param roomType
	 * @return
	 */
	private AtomicIntegerArray getRoomCountCacheByRoomType(int roomType) {
		// synchronized (this) {
		AtomicIntegerArray log = roomCountMap.get(roomType);
		/*
		 * if (log == null) { log = new AtomicIntegerArray(3); log.set(0, 0);//
		 * 对战数 log.set(1, 0);// 破产数 log.set(2, 0);// 抽水数
		 * roomCountMap.put(roomType, log); }
		 */
		return log;
		// }

	}

	public Map<Integer, AtomicIntegerArray> getRoomCountCache() {
		return roomCountMap;
	}
	
	/**
	 * 蒙牌比牌
	 */
	public boolean menCompete(ZTMenjiTable table , ZTMenjiRole ztMenjiRole){
		if(ztMenjiRole.getLook() == 1){
			return false; 
		}
		 MenjiAIConfigCsv menjiAIConfigCsv = menjiAIConfigCache.getConfig(table.getGame().getRoomType());
		int pkRate = 0;
		if(table.getRound() < menjiAIConfigCsv.getBlindRoundA()){
			return false;
		}else if(table.getRound() >= menjiAIConfigCsv.getBlindRoundA() && ztMenjiRole.getZtMenjiAI().getDarkFollowRate() >= 10 &&  ztMenjiRole.getZtMenjiAI().getDarkFollowRate() <= 20){
			pkRate = 30;
		}else if(table.getRound() >= menjiAIConfigCsv.getBlindRoundB() && ztMenjiRole.getZtMenjiAI().getDarkFollowRate() >= 20 &&  ztMenjiRole.getZtMenjiAI().getDarkFollowRate() <= 40){
			pkRate = 30;
		}else if(table.getRound() >= menjiAIConfigCsv.getBlindRoundC() && ztMenjiRole.getZtMenjiAI().getDarkFollowRate() >= 40 &&  ztMenjiRole.getZtMenjiAI().getDarkFollowRate() <= 70){
			pkRate = 30;
		}else if(table.getRound() >= menjiAIConfigCsv.getBlindRoundD()){
			pkRate = 30;
		}
		if((int)Math.ceil(Math.random() * 100) <= pkRate){
			return true;
		}
		return false;
		
	}
	
	/**
	 * 选一个被pk的座位号
	 */
	public int pkSeat(ZTMenjiTable table , GameRole gameRole){
		List<Integer> seatlist = new ArrayList<Integer>();
		for (ZTMenjiRole role : table.getMembers()) {
			if (role != null && role.getRole() != null && !gameRole.equals(role.getRole())) {
				if (!table.getWaiter().contains(role.getRole().getSeat())) {
					seatlist.add(role.getRole().getSeat());
				}
			}
		}
		int pkseat = 0;
		if(seatlist.size() == 0){
			endTable(table);
		}else if (seatlist.size() == 1){
			pkseat = seatlist.get(0);
		}else{
			int index = MathUtil.randomNumber(1, seatlist.size());
			pkseat = seatlist.get(index - 1);
		}
		return pkseat;
	}
	
	/**
	 * 是否弃牌类型
	 * @param ztMenjiRole
	 * @return
	 */
	public boolean isFold(ZTMenjiRole ztMenjiRole){
		List<Integer> systemList = new ArrayList<Integer>();
		List<MJCardType> menJiCardTypeList = new ArrayList<MJCardType>();
		systemList.add(2);
		systemList.add(4);
		systemList.add(27);
		menJiCardTypeList.add(ZTMenji.getCardType(systemList));
		menJiCardTypeList.add(ZTMenji.getCardType(ztMenjiRole.getPai()));
		
		return ZTMenji.getWhoBig(menJiCardTypeList, systemList,ztMenjiRole.getPai());
		
	}
	
	/**
	 * 看牌后比牌
	 * @param table
	 * @param ztMenjiRole
	 * @return
	 */
	public boolean lookCompete(ZTMenjiTable table , ZTMenjiRole ztMenjiRole){
		if(ztMenjiRole.getLook() == 0){
			return false;
		}
		if(ztMenjiRole.isGoodPai()){
			return false;
		}
		int pkChect = 0;
		//获得玩家手上的牌类型
		MJCardType mjCardType = ZTMenji.getCardType(ztMenjiRole.getPai());
		MenjiAIConfigCsv menjiAIConfigCsv = menjiAIConfigCache.getConfig(table.getGame().getRoomType());
		if(mjCardType == MJCardType.SAN_PAI){
			if(table.getRound() >= menjiAIConfigCsv.getStatusLibraryRoundAMin() && table.getRound() <= menjiAIConfigCsv.getStatusLibraryRoundAMax()){
				pkChect = menjiAIConfigCsv.getStatusLibraryRoundAProbability();
			}else if(table.getRound() > menjiAIConfigCsv.getStatusLibraryRoundAMax()){
				pkChect = 100;
			}
		}else if(mjCardType == MJCardType.DUI_ZI){
			if(table.getRound() >= menjiAIConfigCsv.getStatusLibraryRoundBMin() && table.getRound() <= menjiAIConfigCsv.getStatusLibraryRoundBMax()){
				pkChect = menjiAIConfigCsv.getStatusLibraryRoundBProbability();
			}else if(table.getRound() > menjiAIConfigCsv.getStatusLibraryRoundBMax()){
				pkChect = 100;
			}
		}else if(mjCardType == MJCardType.SHUN_ZI){
			if(table.getRound() >= menjiAIConfigCsv.getStatusLibraryRoundCMin() && table.getRound() <= menjiAIConfigCsv.getStatusLibraryRoundCMax()){
				pkChect = menjiAIConfigCsv.getStatusLibraryRoundCProbability();
			}else if(table.getRound() > menjiAIConfigCsv.getStatusLibraryRoundCMax()){
				pkChect = 100;
			}
		}else if(mjCardType == MJCardType.JIN_HUA){
			if(table.getRound() >= menjiAIConfigCsv.getStatusLibraryRoundDMin() && table.getRound() <= menjiAIConfigCsv.getStatusLibraryRoundDMax()){
				pkChect = menjiAIConfigCsv.getStatusLibraryRoundDProbability();
			}else if(table.getRound() > menjiAIConfigCsv.getStatusLibraryRoundDMax()){
				pkChect = 100;
			}
		}else if(mjCardType == MJCardType.TONG_HUA_SHUN || mjCardType == MJCardType.TONG_HUA_SHUN){
			if(table.getRound() >= menjiAIConfigCsv.getStatusLibraryRoundEMin() && table.getRound() <= menjiAIConfigCsv.getStatusLibraryRoundEMax()){
				pkChect = menjiAIConfigCsv.getStatusLibraryRoundEProbability();
			}else{
				pkChect = 100;
			}
			
		}
		if(pkChect >= (int)(Math.ceil(Math.random() * 100))){
			return true;
		}
		return false;
	}
	
	/**
	 * 是否看牌
	 * @return
	 */
	public boolean isLookPai(ZTMenjiTable table , ZTMenjiRole ztMenjiRole){
		if(ztMenjiRole.getLook() == 1){
			return false;
		}
		if(table.getFirstOperation() == ztMenjiRole.getRole().getSeat() && table.isOperation()){
			return false;
		}
		MenjiAIConfigCsv menjiAIConfigCsv = menjiAIConfigCache.getConfig(table.getGame().getRoomType());
		if(table.getAllBetsNum() >= MathUtil.randomNumber(menjiAIConfigCsv.getLookBetMin(), menjiAIConfigCsv.getLookBetMax())){
			return true;
		}
		if(MathUtil.randomNumber(menjiAIConfigCsv.getLookRoundMin(), menjiAIConfigCsv.getLookRoundMax()) <= table.getRound()){
			return true;
		}
		if(table.getRound() > menjiAIConfigCsv.getLookRoundMax()){
			return true;
		}else{
			return false;
		}
		
	}
		
	/**
	 * 是否加注
	 */
	public boolean isAddBet(ZTMenjiTable table,ZTMenjiRole ztMenjiRole){
		MenjiRoomCsv menjiRoomCsv = menjiRoomCache.getConfig(table.getGame().getRoomType());
		List<Integer> list = StringUtil.stringToList(menjiRoomCsv.getRaiseOdds(),StringUtil.DELIMITER_BETWEEN_ITEMS,Integer.class);
		int inGameCount = 0;
		int addChect = 0;
		int index = 0;
		if(list.contains(table.getBetsNum())){
			index = list.indexOf(table.getBetsNum());
			if(index + 1 == list.size()){
				return false;
			}
		}
		if((ztMenjiRole.getLook() == 1 ? list.get(index+1) * 2 : list.get(index + 1) ) > ztMenjiRole.getRole().getRole().getGold() - ztMenjiRole.getChip()){
			return false;
		}
		for (ZTMenjiRole role : table.getMembers()) {
			if (role != null && role.getRole() != null) {
				if (!table.getWaiter().contains(role.getRole().getSeat())) {
					inGameCount++;
				}
			}
		}
		MenjiAIConfigCsv menjiAIConfigCsv = menjiAIConfigCache.getConfig(table.getGame().getRoomType());
		switch (inGameCount) {
		case 1:
			endTable(table);
			break;
		case 2:
			addChect = menjiAIConfigCsv.getFillingPeopleTwo();
			break;
		case 3:
			addChect = menjiAIConfigCsv.getFillingPeopleThree();
			break;
		case 4:
			addChect = menjiAIConfigCsv.getFillingPeopleFour();
			break;
		case 5:
			addChect = menjiAIConfigCsv.getFillingPeopleFive();
			break;
		default:
			addChect = 0;
			break;
		}
		if(addChect >= (int)Math.ceil(Math.random() * 100)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 作弊比牌
	 * @param table
	 * @param ztMenjiRole
	 * @return
	 */
	public boolean isChectCompete(ZTMenjiTable table,ZTMenjiRole ztMenjiRole){
		if(!ztMenjiRole.isGoodPai()){
			return false;
		}
		MenjiAIConfigCsv menjiAIConfigCsv = menjiAIConfigCache.getConfig(table.getGame().getRoomType());
		if(MathUtil.randomNumber(menjiAIConfigCsv.getVictoryBetMin(), menjiAIConfigCsv.getVictoryBetMax()) <= ztMenjiRole.getChip()){
			return true;
		}
		if(MathUtil.randomNumber(menjiAIConfigCsv.getVictoryRoundMin(), menjiAIConfigCsv.getVictoryRoundMax()) <= table.getRound()){
			return true;
		}
		return false;
	}
	
	/**
	 * 是否跟注
	 * @param table
	 * @param role
	 * @return
	 */
	public boolean isFollow(ZTMenjiTable table, ZTMenjiRole role) {
		// TODO Auto-generated method stub
		int needGold = table.getBetsNum();
		if(role.getLook() == 1){
			needGold = needGold * 2;
		}
		if(role.getRole().getRole().getGold() - role.getChip() >= needGold){
			return true;
		}else{
			return false;
		}
	}
	
}
