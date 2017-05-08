package com.yaowan.server.game.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameType;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.constant.MoneyType;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.AiRegulationCache;
import com.yaowan.csv.cache.AvatarCache;
import com.yaowan.csv.cache.ExpCache;
import com.yaowan.csv.cache.HeadPortraitCache;
import com.yaowan.csv.cache.NiuniuRoomCache;
import com.yaowan.csv.entity.AiRegulationCsv;
import com.yaowan.csv.entity.AvatarCsv;
import com.yaowan.csv.entity.ExpCsv;
import com.yaowan.csv.entity.HeadPortraitCsv;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.MathUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.dao.NpcDao;
import com.yaowan.server.game.model.data.entity.Npc;
import com.yaowan.server.game.model.log.dao.NpcMoneyDao;
import com.yaowan.server.game.model.log.entity.NpcMoney;

/**
 * 虚拟玩家操作类
 * 
 * @author G_T_C
 */
@Component
public class NPCFunction extends FunctionAdapter {
	@Autowired
	private RoomFunction roomFunction;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private HeadPortraitCache headPortraitCache;

	@Autowired
	private AvatarCache avatarCache;
	
	@Autowired
	private NiuniuRoomCache niuniuRoomCache;

	@Autowired
	private ExpCache expCache;

	/**
	 * map<realType, map<id,npc>> 真实类型， npc的id ，npc， 存放开启的ai
	 */
	private Map<Integer, Map<Long, Npc>> npcCache = new ConcurrentHashMap<Integer, Map<Long, Npc>>();

	/**
	 * map<id,npc>> npc的id ，npc
	 */
	private Map<Long, Npc> allNpcMap = new HashMap<>();

	@Autowired
	private NpcDao npcDao;
	
	@Autowired
	private AiRegulationCache aiRegulationCache;

	@Autowired
	private NpcMoneyDao npcMoneyDao;
	
	/**
	 * 获取发牌
	 * 
	 * @author G_T_C
	 * @param gameType
	 * @param roomType
	 * @param id
	 * @return
	 */
	public int getDrawCardId(int gameType, int roomType, long id) {
		int realType = roomFunction.getRealType(gameType, roomType);
		Map<Integer, List<AiRegulationCsv>> csvMap = aiRegulationCache
				.getAiRegulationCache();
		Npc npc = getNpcById(gameType, roomType, id);
		if (csvMap == null || npc == null) {
			LogUtil.warn("aiRegulationCache集合是空！gameType=" + gameType
					+ ", roomType" + roomType);
			return 1;
		}
		List<AiRegulationCsv> csvs = csvMap.get(realType);
		if (csvs == null) {
			LogUtil.warn("AiRegulationCsv是空！gameType=" + gameType
					+ ", roomType" + roomType);
			return 1;
		}
		int gainOrLoss = npc.getGainOrLoss();
		LogUtil.info("该ai盈亏！gainOrLoss=" + gainOrLoss + ", rid =" + id);
		for (AiRegulationCsv csv : csvs) {
			float nimval = csv.getMinimumValue();
			float higval = csv.getHighestValue();
			int cm = csv.getControlMoney();
			int realMoney = cm + gainOrLoss;
			if (nimval == 0) {
				if (realMoney < higval) {
					return csv.getDrawCardId();
				}
			} else if (higval == 0) {
				if (realMoney >= nimval) {
					return csv.getDrawCardId();
				}
			} else if (nimval <= realMoney && realMoney < higval) {
				return csv.getDrawCardId();
			}
		}
		return 1;
	}

	public Map<Long, Npc> getNpcMap(int gameType, int roomType) {
		int realType = roomFunction.getRealType(gameType, roomType);
		return npcCache.get(realType);
	}

	/**
	 * 随机一个npc。随机是根据亏损最严重来排序获取第一个
	 * 
	 * @author G_T_C
	 * @param gameType
	 * @param roomType
	 * @return
	 */
	public Npc getNpc(int gameType, int roomType, long roomId) {
		LogUtil.info("gameType="+gameType+",roomType="+roomType+", roomId="+roomId);
		Map<Long, Npc> map = getNpcMap(gameType, roomType);
		if (map == null) {
			LogUtil.warn("该游戏类型的Ai集合是空！gameType=" + gameType + ", roomType"
					+ roomType);
			return null;
		}
		if (map.size() == 0) {
			LogUtil.warn("该游戏类型的Ai集合为0！gameType=" + gameType + ", roomType"
					+ roomType);
			return null;
		}
		Npc npc = map.get(getSortNpcId(map, roomId));
		if (npc == null) {
			LogUtil.warn("没有空闲的Ai！gameType=" + gameType + ", roomType"
					+ roomType);
			return null;
		}
		npc.setStatus(0);// 设置为待准备
		// updateDbNpcStatus(npc);
		LogUtil.info("npcId="+npc.getRid()+", beforeRoomId="+npc.getRoomId());
		npc.setRoomId(roomId);
		return npc;
	}

	/**
	 * 更改状态
	 * 
	 * @param id
	 * @param gameType
	 * @param roomType
	 * @param status
	 */
	public void updateStatus(long id, int gameType, int roomType, int status) {
		Map<Long, Npc> map = getNpcMap(gameType, roomType);
		Npc npc = map.get(id);
		if (npc != null) {
			LogUtil.info("AI" + npc.getNick() + "更新状态，从" + npc.getStatus()
					+ "变为" + status);
			npc.setStatus(status);
			if(status == 2){
				npc.setLimitTime(TimeUtil.time()+10);
			}
			map.put(id, npc);
			updateDbNpcStatus(npc);
		}

	}

	private void updateDbNpcStatus(Npc npc) {
		npc.markToUpdate("status");
		npcDao.updateProperty(npc);
	}

	/**
	 * 根据id获取一个npc
	 * 
	 * @author G_T_C
	 * @param gameType
	 * @param roomType
	 * @param id
	 * @return
	 */
	public Npc getNpcById(int gameType, int roomType, long id) {
		Map<Long, Npc> map = getNpcMap(gameType, roomType);
		if (map == null) {
			LogUtil.warn("该游戏类型的Ai集合是空！gameType=" + gameType + ", roomType"
					+ roomType);
			return null;
		}
		return map.get(id);
	}

	@Override
	public void handleOnServerStart() {
	//	init(); // 初始化一百五十个ai
		allNpcMap = npcDao.findAllNpcMap();
		Map<Long, Npc> npcMap = npcDao.findNpcForMap();
		// 设置ai上线为空闲
		npcDao.updateLoginStatus(2);
		if (npcMap == null) {
			LogUtil.warn("数据库没有可用的AI");
			return;
		}
		LogUtil.info("初始化AI的数量为=" + npcMap.size());
		for (Long key : npcMap.keySet()) {
			Npc npc = npcMap.get(key);
			/*
			 * if (checkNpcIsTimeOut(npc)) { npc.setStatus(3);// 设置为下线 } else {
			 * npc.setStatus(2);// 设置为空闲 }
			 */
			// updateDbNpcStatus(npc);
			int realType = roomFunction.getRealType(npc.getGameType(),
					npc.getRoomType());
			Map<Long, Npc> cacheMap = npcCache.get(realType);
			if (cacheMap == null) {
				cacheMap = new HashMap<Long, Npc>();
				cacheMap.put(npc.getRid(), npc);
				npcCache.put(realType, cacheMap);
			} else {
				cacheMap.put(npc.getRid(), npc);
			}
		}
	}

	@Override
	public void handleOnServerShutdown() {
		npcDao.updateLoginStatus(3);
		npcCache.clear();
		allNpcMap.clear();
	}

	/**
	 * 初始化一百五十个ai
	 * 
	 * @author G_T_C
	 */
	private void init() {
		LogUtil.info("AI录入信息");
		if (npcDao.aiCount() >= 120) {
			return;
		} else {
			 npcDao.truncate();
			insert(GameType.DOUDIZHU, 1, 20);
			insert(GameType.DOUDIZHU, 2, 20);
			//insert(GameType.DOUDIZHU, 3, 30);
			//insert(GameType.DOUDIZHU, 4, 20);
			
			/*insert(GameType.MENJI, 1, 30);
			insert(GameType.MENJI, 2, 20);*/
			
			insert(GameType.MAJIANG, 1, 20);
			insert(GameType.MAJIANG, 2, 20);
			//insert(GameType.MAJIANG, 3, 30);
			//insert(GameType.MAJIANG, 4, 20);
			
			insert(GameType.ZXMAJIANG, 1, 20);	
			insert(GameType.ZXMAJIANG, 2, 20);
			/*insert(GameType.ZXMAJIANG, 3, 30);
			insert(GameType.ZXMAJIANG, 4, 30);
			
			insert(GameType.CDMAJIANG, 1, 30);
			insert(GameType.CDMAJIANG, 2, 20);*/

	/*		for(NiuniuRoomCsv csv:niuniuRoomCache.getConfigList()){
				insert(GameType.DOUNIU, csv.getId(), 30);
			}*/
			
			LogUtil.info("AI录入信息完毕");
		}

	}

	public void insert(int gameType, int roomType, int size) {
		int nowTime = (int) (System.currentTimeMillis() / 1000);
		List<Npc> list = new ArrayList<>();
		for (int i = 0; i <= size - 1; i++) {
			Npc npc = new Npc();
			// npc.setRid((long)(rid + i));
			npc.setOpenId(UUID.randomUUID().toString());
			if (Math.random() > 0.5) {
				npc.setNick(roleFunction.randomNewRoleName());
			} else {
				npc.setNick(roleFunction.randomRoleName(1, 1));
			}
			// 随机性别头像模型
			int sex = MathUtil.randomNumber(0, 1);
			List<Integer> head = new ArrayList<Integer>();
			for (HeadPortraitCsv headPortraitCsv : headPortraitCache
					.getConfigList()) {
				if (sex == headPortraitCsv.getGender()) {
					head.add(headPortraitCsv.getID());
				}
			}
			int chose = head.get(MathUtil.randomNumber(0, head.size() - 1));
			npc.setHead((byte) chose);

			List<Integer> avatar = new ArrayList<Integer>();
			for (AvatarCsv avatarCsv : avatarCache.getConfigList()) {
				if (sex == avatarCsv.getGender()) {
					avatar.add(avatarCsv.getCharacterId());
				}
			}
			chose = avatar.get(MathUtil.randomNumber(0, avatar.size() - 1));
			npc.setSex((byte) sex);
			npc.setAvatar(chose + "_1_1");
			npc.setLevel((short) 1);
			npc.setExp(0);
			npc.setGoldPot(0);
			npc.setGold(0);
			npc.setDiamond(0);
			npc.setCrystal(0);
			npc.setProvince((short) 0);
			npc.setCity((short) 0);
			npc.setOnline((byte) 1);
			npc.setServerId(2);
			npc.setCreateTime(nowTime);
			npc.setVipLevel((short) 1);
			npc.setLastLoginTime(nowTime);
			npc.setLastOfflineTime(nowTime);
			// npc.setStatus(0);
			npc.setGainOrLoss(0);
			npc.setDoEndDate(TimeUtil.dayBreak() + 24 * 60 * 60 * 30);
			npc.setDoEndTime(24 * 60 * 60);
			npc.setDoStartDate(TimeUtil.dayBreak());
			npc.setDoStartTime(0);
			npc.setGameType(gameType);
			npc.setRoomType(roomType);
			npc.setIsOpen(1);
			list.add(npc);
		}
		npcDao.insertAll(list);
	}

	public void insert(String nick, int diamond, int lottery, int exp,
			String serverId) {
		int nowTime = (int) (System.currentTimeMillis() / 1000);
		Npc npc = new Npc();
		npc.setOpenId(UUID.randomUUID().toString());
		npc.setNick(nick);
		// 随机性别头像模型
		int sex = MathUtil.randomNumber(0, 1);
		List<Integer> head = new ArrayList<Integer>();
		for (HeadPortraitCsv headPortraitCsv : headPortraitCache
				.getConfigList()) {
			if (sex == headPortraitCsv.getGender()) {
				head.add(headPortraitCsv.getID());
			}
		}
		int chose = head.get(MathUtil.randomNumber(0, head.size() - 1));
		npc.setHead((byte) chose);
		List<Integer> avatar = new ArrayList<Integer>();
		for (AvatarCsv avatarCsv : avatarCache.getConfigList()) {
			if (sex == avatarCsv.getGender()) {
				avatar.add(avatarCsv.getCharacterId());
			}
		}
		chose = avatar.get(MathUtil.randomNumber(0, avatar.size() - 1));
		npc.setSex((byte) sex);
		npc.setAvatar(chose + "_1_1");
		npc.setLevel((short) 1);
		npc.setExp(exp);
		addLevel(npc);
		npc.setGoldPot(0);
		npc.setGold(3000);
		npc.setDiamond(diamond);
		npc.setCrystal(lottery);
		npc.setProvince((short) 0);
		npc.setCity((short) 0);
		npc.setOnline((byte) 1);
		npc.setServerId(Integer.parseInt(serverId));
		npc.setCreateTime(nowTime);
		npc.setVipLevel((short) 0);
		npc.setLastLoginTime(nowTime);
		npc.setLastOfflineTime(nowTime);
		npc.setStatus(3);
		npc.setGainOrLoss(0);
		npc.setDoEndDate(0);
		npc.setDoEndTime(0);
		npc.setDoStartDate(0);
		npc.setDoStartTime(0);
		npc.setGameType(0);
		npc.setRoomType(0);
		npc.setIsOpen(0);
		npcDao.insert(npc);
		allNpcMap.put(npc.getRid(), npc);
	}

	/**
	 * 检测AI是不是在该时间段内
	 * 
	 * @author G_T_C
	 * @param npc
	 * @return
	 */
	private boolean checkNpcIsTimeOut(Npc npc) {
		int currtime = TimeUtil.time();
		int date = TimeUtil.dayBreak();
		int time = currtime - date;
		if (npc.getDoStartDate() <= date && npc.getDoStartTime() <= time
				&& npc.getDoEndDate() >= date && npc.getDoEndTime() > time) {
			return false;// 不过期
		} else {
			return true;// 过期
		}
	}

	/**
	 * 获取一个没参与游戏，亏损最严重的npc
	 * 
	 * @author G_T_C
	 * @param mpcMap
	 * @return
	 */
	private long getSortNpcId(Map<Long, Npc> npcMap, long roomId) {
		// 排序
		Object[] key = npcMap.keySet().toArray();
		int length = key.length;
		long lasserKey = 0l;
		for (int i = 0; i < length; i++) {

			Npc leisureNpc = npcMap.get(key[i]);
			boolean checkFlag = checkNpc(leisureNpc, npcMap, roomId);
			if (!checkFlag) {
				continue;
			}
			for (int j = i + 1; j < length; j++) {
				Npc afterNpc = npcMap.get(key[j]);
				checkFlag = checkNpc(afterNpc, npcMap, roomId);
				if (!checkFlag) {
					continue;
				}
				if (leisureNpc.getGainOrLoss() > afterNpc.getGainOrLoss()) {
					// 换位
					leisureNpc = afterNpc;
				}
			}
			lasserKey = leisureNpc.getRid();
			break;
		}
		if (lasserKey == 0) {
			LogUtil.warn("没有找到空闲的AI");
		}
		LogUtil.info("获取的AI的id=" + lasserKey);
		return lasserKey;

	}

	/**
	 * 检查拿出当前盈亏最亏的那个ai的条件是否满足
	 * 
	 * @author G_T_C
	 * @param npc
	 * @param npcMap
	 * @param roomId
	 * @return
	 */
	private boolean checkNpc(Npc npc, Map<Long, Npc> npcMap, long roomId) {
		if (npc.getStatus() == 1 || npc.getStatus() == 0) {
			return false;
		}
		if (roomId != -1 && roomId == npc.getRoomId()) {
			return false;
		}
		if(npc.getLimitTime() >TimeUtil.time()){
			return false;
		}

		if (checkNpcIsTimeOut(npc) || npc.getIsOpen() == 0) {
			if (npc.getIsOpen() == 0
					&& (npc.getStatus() == 2 || npc.getStatus() == 3)) {
				npcMap.remove(npc.getRid());
				npc.setStatus(3);
				updateDbNpcStatus(npc);
			}
			LogUtil.info("ai 时间在线完毕，需要下线ID=" + npc.getRid());
			return false;
		}
		return true;
	}

	/**
	 * 更新金币的值
	 * 
	 * @author G_T_C
	 * @param npcId
	 * @param gold
	 *            分正负值。正为加。负为减
	 */
	public void updateGold(long npcId, int gold) {
		npcDao.updateMoneryPackge(npcId, gold, 1);
	}

	/**
	 * 更新钻石的值
	 * 
	 * @author G_T_C
	 * @param npcId
	 * @param gold
	 *            分正负值。正为加。负为减
	 */
	public void updateDiamond(long npcId, int diamond) {
		npcDao.updateMoneryPackge(npcId, diamond, 2);
	}

	/**
	 * 更新盈亏
	 * 
	 * @author G_T_C
	 * @param npcId
	 * @param value
	 */
	public void updateGainOrLoss(long npcId, int value, int gameType,
			int roomType, MoneyEvent event) {
		if(value != 0){
			npcDao.updateGainOrLoss(npcId, value);
		}
		int realType = roomFunction.getRealType(gameType, roomType);
		Map<Long, Npc> npcMap = npcCache.get(realType);
		if (npcMap == null) {
			return;
		}
		Npc npc = npcMap.get(npcId);
		if (npc == null) {
			return;
		}
		int beforeGainOrLoss = npc.getGainOrLoss();
		npc.setGainOrLoss( beforeGainOrLoss + value);
		NpcMoney log = new NpcMoney();
		log.setRid(npc.getRid());
		log.setEvent(event.getValue());
		log.setTime(TimeUtil.time());
		log.setBeforeGainOrLoss(beforeGainOrLoss);
		log.setType(MoneyType.Gold.byteValue());
		log.setValue(value);
		npcMoneyDao.insert(log);
	}

	/**
	 * 更新奖券的值
	 * 
	 * @author G_T_C
	 * @param npcId
	 * @param gold
	 *            分正负值。正为加。负为减
	 */
	public void updateCrystal(long npcId, int crystal) {
		npcDao.updateMoneryPackge(npcId, crystal, 3);
	}

	public Map<Integer, Map<Long, Npc>> getNpcCache() {
		return npcCache;
	}

	public void setNpcCache(Map<Integer, Map<Long, Npc>> npcCache) {
		this.npcCache = npcCache;
	}

	public void setRobotWin(long id, int gameType, int roomType) {
		Map<Long, Npc> map = getNpcMap(gameType, roomType);
		Npc npc = map.get(id);
		if (npc != null) {
			npc.setWinWeek(npc.getWinWeek() + 1);
			npc.setCountWeek(npc.getCountWeek() + 1);
			npc.setWinTotal(npc.getWinTotal() + 1);
			npc.setCountTotal(npc.getCountTotal() + 1);
			npc.markToUpdate("winWeek");
			npc.markToUpdate("countWeek");
			npc.markToUpdate("countTotal");
			npc.markToUpdate("winTotal");
			npcDao.updateProperty(npc);
		}
	}

	public void setRobotLost(long id, int gameType, int roomType) {
		Map<Long, Npc> map = getNpcMap(gameType, roomType);
		Npc npc = map.get(id);
		if (npc != null) {
			npc.setCountWeek(npc.getCountWeek() + 1);
			npc.setCountTotal(npc.getCountTotal() + 1);
			npc.markToUpdate("countWeek");
			npc.markToUpdate("countTotal");
			npcDao.updateProperty(npc);
		}
	}

	public boolean robotByebye(long id, int gameType, int roomType) {
		// "{\"201010\":\"14:00:00-16:00:00\",\"201011\":\"9:00-12:00\"}"
		Map<Long, Npc> map = getNpcMap(gameType, roomType);
		Npc npc = map.get(id);
		if (npc != null) {
			return checkNpcIsTimeOut(npc);
		} else {
			return true;
		}

	}

	/**
	 * 根据经验计算等级
	 * 
	 * @author G_T_C
	 * @param exp
	 * @return
	 */
	public void addLevel(Npc npc) {
		ExpCsv expCsv = expCache.getConfig(npc.getLevel());
		int max = expCache.getConfigList().size();
		int upgradeExp = expCsv.getExp();
		// 自动升级
		while (npc.getExp() >= upgradeExp) {
			// addLevel += 1;
			if (npc.getLevel() < max) {
				npc.setExp(npc.getExp() - upgradeExp);
				npc.setLevel((short) (npc.getLevel() + 1));
				expCsv = expCache.getConfig(npc.getLevel() + 1);
				upgradeExp = expCsv.getExp();
			} else {
				npc.setExp(upgradeExp);
				break;
			}
		}
	}

	/**
	 * 修改ai
	 * 
	 * @author G_T_C
	 * @param npc
	 * @throws Exception
	 */
	public void updateNpc(Npc npc) throws Exception {
		if (npc.getExp() != 0) {
			addLevel(npc);
			npc.markToUpdate("level");
		}
		npcDao.updateProperty(npc);
		// 缓存
		long rid = npc.getRid();
		Npc oldnpc = allNpcMap.get(rid);
		if (oldnpc == null) {
			oldnpc = npcDao.findByKey(rid);
		}
		if (npc.getNick() != null && npc.getNick().equals("")) {
			oldnpc.setNick(npc.getNick());
		}
		if (npc.getHead() != 0) {
			oldnpc.setHead(npc.getHead());
		}
		if (npc.getDiamond() != 0) {
			oldnpc.setDiamond(npc.getDiamond());
		}
		if (npc.getCrystal() != 0) {
			oldnpc.setCrystal(npc.getCrystal());
		}
		if (npc.getExp() != 0) {
			oldnpc.setExp(npc.getExp());
			oldnpc.setLevel(npc.getLevel());
		}
		if (npc.getServerId() != 0) {
			oldnpc.setServerId(npc.getServerId());
		}
		allNpcMap.put(rid, oldnpc);
		if (oldnpc.getGameType() == 0 || oldnpc.getRoomId() == 0
				|| oldnpc.getIsOpen() == 0) {
			return;
		}
		int realType = roomFunction.getRealType(oldnpc.getGameType(),
				npc.getRoomType());
		Map<Long, Npc> npcMap = npcCache.get(realType);
		if (npcMap != null && npcMap.get(rid) != null) {
			oldnpc.setStatus(npcMap.get(rid).getStatus());
			npcMap.put(rid, oldnpc);
		}
	}

	/**
	 * Ai的调度
	 * 
	 * @author G_T_C
	 * @param isOpen
	 * @param ids
	 * @param map
	 * @throws Exception
	 */
	public void dispatch(String[] ids, Map<String, Object> map)
			throws Exception {
		npcDao.updateDispatch(ids, map);
		// 更新缓存
		List<Npc> npcs = npcDao.findByIds(ids);
		if (npcs == null) {
			return;
		}
		for (Npc npc : npcs) {
			long rid = npc.getRid();
			int gameType = npc.getGameType();
			int roomType = npc.getRoomType();
			if (gameType == 0 || roomType == 0) {
				continue;
			}
			Npc oldNpc = allNpcMap.get(rid);
			int oldgameType = oldNpc.getGameType();
			int oldroomType = oldNpc.getRoomType();
			// 处理更改前的缓存
			if (oldgameType != 0 && oldroomType != 0) {
				int realType = roomFunction.getRealType(oldgameType,
						oldroomType);
				Map<Long, Npc> npcMap = npcCache.get(realType);
				if (npcMap == null) {
					continue;
				}
				oldNpc = npcMap.get(rid);
				if(oldNpc == null){
					continue;
				}
				oldNpc.setIsOpen(0);// 把该房间的ai关了。如果处于游戏中，等待它空闲，将它清除
				if (oldNpc.getStatus() == 2 || oldNpc.getStatus() == 3) {
					npcMap.remove(rid);
				}
			}
			// 新的调度
			int realType = roomFunction.getRealType(gameType, roomType);
			Map<Long, Npc> npcMap = npcCache.get(realType);
			if (npcMap == null) {
				npcMap = new HashMap<>();
			}
			int status =oldNpc.getStatus();
			if(status == 3){
				status = 2;
			}
			npc.setStatus(status);
			updateDbNpcStatus(npc);
			npcMap.put(rid, npc);
			allNpcMap.put(rid, npc);
			npcCache.put(realType, npcMap);
		}
	}

	/**
	 * Ai的调度
	 * 
	 * @author G_T_C
	 * @param isOpen
	 * @param ids
	 * @param map
	 * @throws Exception
	 */
	public void onOrOff(int isOpen, String[] ids, String logId) throws Exception {
		npcDao.updateIsOpen(ids, isOpen, logId);
		// 更新缓存
		int length = ids.length;
		for (int i = 0; i < length; i++) {
			long rid = Long.parseLong(ids[i]);
			Npc npc = allNpcMap.get(rid);
			int gameType = npc.getGameType();
			int roomType = npc.getRoomType();
			// 处理更改前的缓存
			if (gameType != 0 && roomType != 0) {
				int realType = roomFunction.getRealType(gameType, roomType);
				Map<Long, Npc> npcMap = npcCache.get(realType);
				Npc npc2 = npcMap.get(rid);
				npc.setIsOpen(isOpen);
				if (isOpen == 0) {
					if(npc2 != null){
						npc2.setIsOpen(0);// 把该房间的ai关了。如果处于游戏中，等待它空闲，将它清除
					}else{
						continue;
					}
					if (npc2.getStatus() == 2 || npc2.getStatus() == 3) {
						npcMap.remove(rid);
					}
				} else {
					if (npc2 == null) {
						npc.setStatus(2);
						updateDbNpcStatus(npc);
					}
					npcMap.put(rid, npc);
				}
				allNpcMap.put(rid, npc);
			}
		}
	}

	/**
	 * 修改npc缓存的roomId
	 * @author G_T_C
	 * @param gameType
	 * @param roomType
	 * @param rid
	 * @param roomId
	 */
	public void updateRoomId(int gameType, int roomType, long rid, long roomId) {
		int realType = roomFunction.getRealType(gameType, roomType);
		Map<Long, Npc> npcMap = npcCache.get(realType);
		if (npcMap != null && npcMap.get(rid) != null) {
			Npc npc = npcMap.get(rid);
			npc.setRoomId(roomId);
			npcMap.put(npc.getRid(), npc);
		}
	}

}
