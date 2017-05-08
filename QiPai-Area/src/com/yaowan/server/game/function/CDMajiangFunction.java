/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameStatus;
import com.yaowan.constant.GameType;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.DealProbabilityCache;
import com.yaowan.csv.cache.ExpCache;
import com.yaowan.csv.cache.MaJiangChengDuValueCache;
import com.yaowan.csv.cache.MajiangChengDuRoomCache;
import com.yaowan.csv.entity.DealProbabilityCsv;
import com.yaowan.csv.entity.ExpCsv;
import com.yaowan.csv.entity.MaJiangChengDuValueCsv;
import com.yaowan.csv.entity.MajiangChengDuRoomCsv;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.DispatchEvent;
import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.core.thread.SingleThreadManager;
import com.yaowan.framework.core.thread.SingleThreadTask;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.MathUtil;
import com.yaowan.framework.util.Probability;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.protobuf.game.GBaseMahJong;
import com.yaowan.protobuf.game.GBaseMahJong.GBillsInfo;
import com.yaowan.protobuf.game.GBaseMahJong.GDetailBillsInfo;
import com.yaowan.protobuf.game.GBaseMahJong.GMajiangGold;
import com.yaowan.protobuf.game.GBaseMahJong.GMajiangPlayer;
import com.yaowan.protobuf.game.GBaseMahJong.GOpenInfo;
import com.yaowan.protobuf.game.GBaseMahJong.GPaiInfo;
import com.yaowan.protobuf.game.GBaseMahJong.GPaiQiang;
import com.yaowan.protobuf.game.GBaseMahJong.GTingPaiInfo;
import com.yaowan.protobuf.game.GBaseMahJong.OptionsType;
import com.yaowan.protobuf.game.GCDMahJong.ChengDuWinType;
import com.yaowan.protobuf.game.GCDMahJong.GCDMajiangDingQue;
import com.yaowan.protobuf.game.GCDMahJong.GMsg_12042001;
import com.yaowan.protobuf.game.GCDMahJong.GMsg_12042002;
import com.yaowan.protobuf.game.GCDMahJong.GMsg_12042006;
import com.yaowan.protobuf.game.GCDMahJong.GMsg_12042007;
import com.yaowan.protobuf.game.GCDMahJong.GMsg_12042008;
import com.yaowan.protobuf.game.GCDMahJong.GMsg_12042009;
import com.yaowan.protobuf.game.GCDMahJong.GMsg_12042010;
import com.yaowan.protobuf.game.GCDMahJong.GMsg_12042012;
import com.yaowan.protobuf.game.GCDMahJong.GMsg_12042013;
import com.yaowan.protobuf.game.GCDMahJong.GMsg_12042020;
import com.yaowan.protobuf.game.GGame.GGameInfo;
import com.yaowan.protobuf.game.GGame.GGameRole;
import com.yaowan.protobuf.game.GGame.GMsg_12006005;
import com.yaowan.protobuf.game.GGame.PlayerState;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.dao.CDMajiangLogDao;
import com.yaowan.server.game.model.log.entity.CDMajiangLog;
import com.yaowan.server.game.model.struct.MissionType;
import com.yaowan.server.game.model.struct.TaskType;
import com.yaowan.server.game.model.struct.ZTMaJongTable;
import com.yaowan.server.game.model.struct.ZTMajiangRole;
import com.yaowan.server.game.rule.CDMahJongRule;

/**
 * 成都麻将
 *
 * @author yangbin
 */
@Component
public class CDMajiangFunction extends FunctionAdapter {

	@Autowired
	private SingleThreadManager manager;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private RoomFunction roomFunction;

	@Autowired
	private CDMajiangDataFunction majiangDataFunction;

	@Autowired
	private MajiangChengDuRoomCache majiangRoomCache;

	@Autowired
	private MaJiangChengDuValueCache maJiangValueCache;

	@Autowired
	private ExpCache expCache;

	@Autowired
	MissionFunction missionFunction;

	@Autowired
	private CDMajiangLogDao majiangLogDao;
	
	@Autowired
	private RoomLogFunction roomLogFunction;
	
	@Autowired
	private DealProbabilityCache maJiangProbabilityCache;
	
	@Autowired
	private NPCFunction npcFunction;
	
	
	private Map<Long, ZTMaJongTable> tableMap = new ConcurrentHashMap<>();

	private Map<Long, ZTMajiangRole> roleMap = new ConcurrentHashMap<>();

	// 统计房间当日对战数，破产数，抽水数
	private static Map<Integer, AtomicIntegerArray> roomCountMap = new ConcurrentHashMap<>();

	public Map<Long, ZTMaJongTable> getAllTable() {
		return tableMap;
	}

	public void clear(long roomId) {
		ZTMaJongTable table = tableMap.remove(roomId);
		if (table != null) {
			for (ZTMajiangRole role : table.getMembers()) {
				if (role.getRole() != null
						&& role.getRole().getStatus() != PlayerState.PS_EXIT_VALUE) {
					removeRoleCache(role.getRole().getRole().getRid());
					if (role.getRole().getRole().getRid() < 100000) {
						int i = 0;
						int j = 0;
					}
				}

			}
		}

	}

	public ZTMaJongTable getTable(Long id) {
		ZTMaJongTable ZTMaJongTable = tableMap.get(id);
		return ZTMaJongTable;
	}

	public ZTMaJongTable getTableByRole(Long id) {
		ZTMajiangRole majiangRole = getRole(id);
		if (majiangRole == null) {
			return null;
		}
		return getTable(majiangRole.getRole().getRoomId());
	}

	public ZTMajiangRole getRole(Long id) {
		ZTMajiangRole majiangRole = roleMap.get(id);
		// ZTMaJongTable jongTable
		// =tableMap.entrySet().iterator().next().getValue();

		return majiangRole;
	}

	public void addRoleCache(ZTMajiangRole role) {
		roleMap.put(role.getRole().getRole().getRid(), role);
	}

	public void removeRoleCache(Long id) {
		roleMap.remove(id);
	}
	
	public void resetOverTable(Game game){
		if (game.getStatus() == GameStatus.END_REWARD) {
			game.setStatus(GameStatus.WAIT_READY);
			ZTMaJongTable table = getTable(game.getRoomId());
			table.reset();
		}
	}

	/**
	 * 定时检测所有人准备游戏开始
	 */
	public void checkStart(Game game) {
		boolean isRoleOK = true;

		for (GameRole role : game.getSpriteMap().values()) {
			
			if (!role.isRobot()) {
				if("".equals(role.getRole().getPlatform())){
					isRoleOK = true;
				}else if (role.getStatus() != PlayerState.PS_PREPARE_VALUE) {
					isRoleOK = false;
				}

			} else {
				//准备时检测
				MajiangChengDuRoomCsv majiangRoomCsv = majiangRoomCache.getConfig(game
						.getRoomType());
				boolean flag = false;
				if (majiangRoomCsv.getEnterUpperLimit() == -1) {
					if (role.getRole().getGold() < majiangRoomCsv
							.getEnterLowerLimit()) {
						flag = true;
					}
				} else {
					if (role.getRole().getGold() < majiangRoomCsv
							.getEnterLowerLimit()
							|| role.getRole().getGold() > majiangRoomCsv
									.getEnterUpperLimit()) {
						flag = true;
					}
				}
				if(!flag && role.isRobot()){
					if(!npcFunction.robotByebye(role.getRole().getRid(), game.getGameType(), game.getRoomType())){
						//AI不在时间范围内就要走了
						LogUtil.error("麻将AI时间到，走了");
						flag = true;
					}
				}		
				if (flag) {
					LogUtil.info("maj");
					GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
					builder.setCurrentSeat(role.getSeat());
					roleFunction.sendMessageToPlayers(game.getRoles(), builder.build());
					//roomFunction.endGame(game);
					roomFunction.quitRole(game, role.getRole());
					return;
				}
				long time = game.getStartTime();
				if (game.getEndTime() > game.getStartTime()) {
					time = game.getEndTime();
				}
				int dif = (int) (System.currentTimeMillis() - time) / 1000;
				if (game.getCount() == 0&&role.getStatus() != PlayerState.PS_PREPARE_VALUE){
					if (dif > MathUtil.randomNumber(1, 3)
							&& role.getStatus() != PlayerState.PS_PREPARE_VALUE) {

						role.setStatus(PlayerState.PS_PREPARE_VALUE);

						GMsg_12042001.Builder builder = GMsg_12042001.newBuilder();
						builder.setSeat(role.getSeat());
						roleFunction.sendMessageToPlayers(game.getRoles(),
								builder.build());
					}
				}
                if (game.getCount() > 0 && role.getStatus() != PlayerState.PS_PREPARE_VALUE){
                	if (dif > MathUtil.randomNumber(3, 5)
    						&& role.getStatus() != PlayerState.PS_PREPARE_VALUE) {

    					role.setStatus(PlayerState.PS_PREPARE_VALUE);

    					GMsg_12042001.Builder builder = GMsg_12042001.newBuilder();
    					builder.setSeat(role.getSeat());
    					roleFunction.sendMessageToPlayers(game.getRoles(),
    							builder.build());
    				}
				}
				
				//}
				
				
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

	/**
	 * 开始游戏
	 */
	public void startTable(Game game) {
		if (game.getStatus() != GameStatus.WAIT_READY) {
			return;
		}

		ZTMaJongTable table = getTable(game.getRoomId());
		table.getWaiter().clear();
		AtomicIntegerArray roomCountLog = getRoomCountCacheByRoomType(game
				.getRoomType());
		for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			GameRole role = entry.getValue();
			role.setStatus(PlayerState.PS_PREPARE_VALUE);

			GBillsInfo.Builder billsInfo = GBillsInfo.newBuilder();
			billsInfo.setRid(entry.getValue().getRole().getRid());
			billsInfo.setNick(entry.getValue().getRole().getNick());
			billsInfo.setSeat(entry.getValue().getSeat());
			billsInfo.setGold(0);
			billsInfo.setExp(0);
			table.getBills().put(entry.getValue().getSeat(), billsInfo);
//			System.out.println("initTable数量" + table.getMembers().size());

			// 扣税
			int tax = majiangRoomCache.getConfig(game.getRoomType())
					.getTaxPerGame();
			int gold = role.getRole().getGold();
			if (gold < tax) {
				tax = gold;
			}
		
			if(role.isRobot()){
				role.getRole().setGold(role.getRole().getGold() - tax);
			}else{
				roleFunction.goldSub(role.getRole(), tax, MoneyEvent.CDMAJIANG_TAX, true);
				roomCountLog.addAndGet(2, tax);// 抽水
			}

		}
		roomCountLog.incrementAndGet(0);// 对战数

		List<Integer> initAllPai = initAllPai();
		// 洗牌 打乱顺序
		Collections.shuffle(initAllPai);
		table.setPais(initAllPai);
		
		//发牌 目前根据概率配 保留随机发牌
		faPai(table);
		//faPaiTest(table);

		// 随机庄家
		int host = MathUtil.randomNumber(1, table.getMembers().size());
		table.setOwner(host);

		// 转骰子
		int point1 = MathUtil.randomNumber(1, 6);
		int point2 = MathUtil.randomNumber(1, 6);
		table.setPoint1(point1);
		table.setPoint2(point2);

		// 镇雄麻将没有坨牌(癞子)
		table.setLaiZiNum(0);

		// 开始游戏
		game.setStatus(GameStatus.RUNNING);
		game.setCount(game.getCount() + 1);
		
		List<Integer> pais = table.getPais();
		for (ZTMajiangRole maJongMember : table.getMembers()) {
			LogUtil.info(maJongMember.getRole().getRole().getNick()
					+ "maJongMember:" + maJongMember.getPai());
			
			GPaiInfo.Builder info = GPaiInfo.newBuilder();
			info.addAllPai(maJongMember.getPai());
		
			GOpenInfo.Builder builder2 = GOpenInfo.newBuilder();
			builder2.setDeskRest(pais.size());
			builder2.setDicePoint1(point1);
			builder2.setDicePoint2(point2);
			builder2.setTuoPai(0);
			builder2.setHostSeat(table.getOwner());
			builder2.setHandPai(info);
			
			GMsg_12042002.Builder builder = GMsg_12042002.newBuilder();
			builder.setOpenInfo(builder2);
			roleFunction.sendMessageToPlayer(maJongMember.getRole().getRole()
					.getRid(), builder.build());
			
			//增加活跃度
			GameRole gameRole = maJongMember.getRole();
			if(gameRole != null){
				Role role = gameRole.getRole();
				if(null != role){
					role.setBureauCount(role.getBureauCount()+1);
					role.markToUpdate("bureauCount");
				}
			}
		}
		// table.getCanOptions().put(table.getOwner(),
		// OptionsType.ANNOUNCE_WIN);
		LogUtil.info("startTable数量" + table.getMembers().size());
		
		LogUtil.info(table.getGame().getRoomId() + " 等待前端播放抓牌动画后显示定缺  等待8秒");
		tableToWait(table, 0, 0, HandleType.MAJIANG_WAIT_SHOW_DING_QUE,
				System.currentTimeMillis() + 8000);
	}

	// 测试时 发想要的手牌
	private void faPaiTest(ZTMaJongTable table) {
		List<Integer> initAllPai = table.getPais();
		// TODO Auto-generated method stub
		// List<ZTMajiangRole> paiXuMembers = new ArrayList<ZTMajiangRole>();
		// for(int i = 0 ; i < table.getMembers().size() ; i++){
		// if (!table.getMembers().get(i).getRole().isRobot()) {
		// paiXuMembers.add(table.getMembers().get(i));
		// }
		// }
		// for(int i = 0 ; i < table.getMembers().size() ; i++){
		// if (table.getMembers().get(i).getRole().isRobot()) {
		// paiXuMembers.add(table.getMembers().get(i));
		// }
		// }
		// table.getMembers().clear();
		// table.getMembers().addAll(paiXuMembers);
		Integer remove = null;
		for (int j = 0; j < table.getMembers().size(); j++) {
			ZTMajiangRole maJongMember = table.getMembers().get(j);
			if (table.getMembers().get(j).getRole().isRobot()) {
				continue;
			}

			for (int i = 0; i < 13; i++) {
				remove = getCeShiPai(i);
				initAllPai.remove(remove);
				maJongMember.getPai().add(remove);
			}
			
//			System.out.println();
//			System.out.println("手牌" + maJongMember.getPai());
//			System.out.println("剩余牌" + initAllPai);
//			System.out.println("剩余牌数量" + initAllPai.size());
//			System.out.println();
		}
		
		for (int j = 0; j < table.getMembers().size(); j++) {
			ZTMajiangRole maJongMember = table.getMembers().get(j);
			if (!table.getMembers().get(j).getRole().isRobot()) {
				continue;
			}
			
			for (int i = 0; i < 13; i++) {
				remove = Probability.getRand(initAllPai);
				initAllPai.remove(remove);
				maJongMember.getPai().add(remove);
			}		
		
//			System.out.println();
//			System.out.println("手牌" + maJongMember.getPai());
//			System.out.println("剩余牌" + initAllPai);
//			System.out.println("剩余牌数量" + initAllPai.size());
//			System.out.println();
		}
	}

	/**
	 * 发牌 	特定发牌
	 * @param table
	 */
	public void faPai(ZTMaJongTable table)
	{	
		int type=1;
		List<Integer> initAllPai = table.getPais();
		if(type==0){
			for (int i = 0; i < 13; i++) {
				for (int j = 0; j < table.getMembers().size(); j++) {
					ZTMajiangRole maJongMember = table.getMembers().get(j);
					Integer remove = initAllPai.remove(0);
					maJongMember.getPai().add(remove);
				}
			}
			/*for (int j = 0; j < table.getMembers().size(); j++) {
				ZTMajiangRole maJongMember = table.getMembers().get(j);
				System.out.println(j+"的牌"+maJongMember.getPai());
			}*/
		}else{
			//特定发牌
			List<Integer> wangPai = new ArrayList<Integer>();
			List<Integer> tongPai = new ArrayList<Integer>();
			List<Integer> tiaoPai = new ArrayList<Integer>();
			for (Integer integer : initAllPai) {
				if (integer > 10 && integer < 20) {
					wangPai.add(integer);
				} else if (integer > 20 && integer < 30) {
					tongPai.add(integer);
				} else if (integer > 30 && integer < 40) {
					tiaoPai.add(integer);
				}
			}
			// 读出所有牌
//			System.out.println("手牌" + initAllPai);
//			System.out.println(initAllPai);
//			System.out.println("***********");
			// 1代表万 2代表筒 3代表筒
			Integer wang = 1;
			Integer tong = 2;
			Integer tiao = 3;
			// 万，筒，索概率			
			DealProbabilityCsv dealProbabilityCsv=maJiangProbabilityCache.getConfig(1);	
			double wangProbability = dealProbabilityCsv.getProbabilityA();
			double tongProbability = dealProbabilityCsv.getProbabilityB();
			double tiaoProbability = dealProbabilityCsv.getProbabilityC();
			int[][] majiang = CDMahJongRule.conversionType(initAllPai);
			int wangNum = majiang[0][0];// 万数量
			int tongNum = majiang[1][0];// 筒数量
			int tiaoNum = majiang[2][0];// 索数量
			// 一开始的发牌
			HashMap<Integer, Double> hm = new HashMap<Integer, Double>();			
			for (int j = 0; j < table.getMembers().size(); j++) {
				//分配概率问题
				if(j==0){
//					System.out.println("key="+wang+" value"+wangProbability);
//					System.out.println("key="+tong+" value"+tongProbability);
//					System.out.println("key="+tiao+" value"+tiaoProbability);
				}else{
					HashMap<Integer, Integer> hmSort = new HashMap<Integer, Integer>();
					hmSort.put(wang, wangNum);
					hmSort.put(tong, tongNum);
					hmSort.put(tiao, tiaoNum);
					//使用 Map按value进行排序
					Map<Integer, Integer> bb = CDMahJongRule.sortMapByValue(hmSort);
					int i=0;
					for (Map.Entry<Integer, Integer> entry : bb.entrySet()) {			
						Integer key = entry.getKey();
						//判断位置
						int gailu=0;
						switch (i) {
						case 0:	
							gailu=dealProbabilityCsv.getProbabilityA();
							break;
						case 1:	
							gailu=dealProbabilityCsv.getProbabilityB();
							break;
						case 2:	
							gailu=dealProbabilityCsv.getProbabilityC();
							break;
						default:
							break;
						}
						//判断概率
						switch (key) {
						case 1:	
							wangProbability=gailu;
							break;
						case 2:	
							tongProbability=gailu;
							break;
						case 3:
							tiaoProbability=gailu;
							break;
						default:
							break;
						}
						i++;
//						System.out.println("key=" + entry.getKey() + " value=" + entry.getValue());
					}				
				}
//				System.out.println("wangProbability"+wangProbability+",tongProbability"+tongProbability+",tiaoProbability"+tiaoProbability+"");
				//分配概率问题
				ZTMajiangRole maJongMember = table.getMembers().get(j);
				for (int i = 0; i < 13; i++) {
					if (wangNum > 0) {
						hm.put(wang, wangProbability);
					}
					if (tongNum > 0) {
						hm.put(tong, tongProbability);
					}
					if (tiaoNum > 0) {
						hm.put(tiao, tiaoProbability);
					}
					Integer t = Probability.getRand(hm,1);				
					System.out.print("随机到" + t + "类型  ");
					Integer remove = null;
					switch (t) {
					case 1:// 随机到万
						remove = Probability.getRand(wangPai);
						wangPai.remove(remove);
						wangNum--;
						break;
					case 2:// 随机到筒
						remove = Probability.getRand(tongPai);
						tongPai.remove(remove);
						tongNum--;
						break;
					case 3:// 随机到条
						remove = Probability.getRand(tiaoPai);
						tiaoPai.remove(remove);
						tiaoNum--;
						break;
					default:
						break;
					}
					initAllPai.remove(remove);
					maJongMember.getPai().add(remove);
				}
//				System.out.println();
//				System.out.println("万的数量剩余"+wangNum+"筒的数量剩余"+tongNum+"条的数量剩余"+tiaoNum);
//				System.out.println("手牌" + maJongMember.getPai());
//				System.out.println("剩余牌" + initAllPai);
//				System.out.println();
			}			
		}
		
	}
	
	public Integer moPai(ZTMaJongTable table,ZTMajiangRole ZTMajiangRole){
		// 测听牌，根据听牌走不同的道路
		List<Integer> tingPai = new ArrayList<Integer>();
		List<Integer> shouPai = ZTMajiangRole.getPai();
		
		int[][] b = CDMahJongRule.conversionType(shouPai);
		tingPai = CDMahJongRule.tingPai(b,ZTMajiangRole.getShowPai());
		
		List<Integer> pais = table.getPais();
		Integer remove = null;
		DealProbabilityCsv dealProbabilityCsv = maJiangProbabilityCache
				.getConfig(1);
		if (tingPai.size() > 0) {// 听牌
			List<Integer> huPais = new ArrayList<Integer>();
			List<Integer> noHuPais = new ArrayList<Integer>();
			int totalNum = 0;
			// 牌桌剩余牌
			int[][] leftPais = CDMahJongRule.conversionType(pais);
			// 打印牌牌组的牌
			for (int i = 0; i <= 3; i++) {
				for (int j = 0; j <= 9; j++) {
					System.out.print(leftPais[i][j] + " ");
				}
//				System.out.println();
			}
			// 打印牌
			// 能胡的牌
			for (Integer integer : tingPai) {
				int front = integer / 10 - 1;
				int next = integer % 10;
				int num = leftPais[front][next];
				totalNum += num;
				for (int i = 0; i < num; i++) {
					huPais.add(integer);
				}
			}
			// 能胡的牌
			// 不能胡的牌
			for (Integer integer : pais) {
				if (!huPais.contains(integer)) {
					noHuPais.add(integer);
				}
			}
			/*
			 * int pai=0; int num=0; for (int i = 1; i <= 3; i++) { for (int j =
			 * 1; j <= 9; j++) { pai = 10 * i + j; if(!huPais.contains(pai)){
			 * num=leftPais[i-1][j]; for (int k = 0; k < num; k++) {
			 * noHuPais.add(pai); } } } }
			 */
			// 不能胡的牌
//			System.out.println("数量为" + totalNum + "可胡的有" + huPais);
			// X随机因子
			int x = dealProbabilityCsv.getEathuProbability();
			if (totalNum > 0) {
				double p1 = (x + totalNum) / (double) pais.size();
				if (p1 >= 1) {// 必能胡牌，从胡牌中摸出
					remove = Probability.getRand(huPais);
				} else {// 根据概率得出是否能胡牌
					double p2 = MathUtil.randomDouble(0f, 1f);
					if (p2 < p1) {// 能够胡牌
						remove = Probability.getRand(huPais);
					} else {// 不能胡牌
						remove = Probability.getRand(noHuPais);
					}
				}
			}
			// 没有胡牌，则随机给一个牌
			if (remove == null) {
				remove = Probability.getRand(pais);
			}
		} else {// 没有听（方式一：随机在剩余的牌库中抽一张牌。方式二：根据既定的套路给玩家发牌。）
			double p = MathUtil.randomDouble(0f, 10000f);
			// p<dealProbabilityCsv.getSuijiProbability()
			if (p < dealProbabilityCsv.getSuijiProbability()) {// 方式一
				remove = Probability.getRand(pais);
			} else {// 方式二，有5种摸牌方式
				remove = CDMahJongRule.moPai(shouPai, pais);
				if (remove == 0) {
					remove = CDMahJongRule.randomMoPai(shouPai, pais);
					if (remove == 0) {
						remove = Probability.getRand(pais);
					}
				}
			}
		}
		// 防止出现null空指针
		if (remove == null) {
			remove = Probability.getRand(pais);
		}
		table.getPais().remove(remove);
		return remove;
	}

	// 李培光测试
	public static void main(String[] args) {
				
	}

	// 李培光测试
	public void dealTest2() {
		ZTMaJongTable table=new ZTMaJongTable();
		table.setLaiZiNum(38);
		Role r=new Role();
		r.setNick("12");
		ZTMajiangRole ZTMajiangRole=new ZTMajiangRole(new GameRole(r,1));
		List<Integer> a=new ArrayList<Integer>();
		a.add(11);
		a.add(11);
		a.add(13);
		a.add(13);
		a.add(14);
		a.add(14);
		a.add(31);
		a.add(31);
		a.add(32);
		a.add(32);
		a.add(33);
		a.add(33);
		a.add(36);
		ZTMajiangRole.setPai(a);
		
		Map<Integer,List<Integer>> showPai=new HashMap<Integer, List<Integer>>();
		/*List<Integer> show1=new ArrayList<Integer>();
		show1.add(11);
		show1.add(11);
		show1.add(11);
		showPai.put(11, show1);
		List<Integer> show2=new ArrayList<Integer>();
		show1.add(15);
		show1.add(15);
		show1.add(15);
		showPai.put(15, show2);*/
		ZTMajiangRole.setShowPai(showPai);
		
		boolean b=canHu(table, ZTMajiangRole, 37);
//		System.out.println(b);
	}


	/**
	 * 
	 *
	 * @param owner
	 * @return
	 */
	public void enterTable(final Game game, final GameRole role) {
		ZTMaJongTable table = getTable(game.getRoomId());
		if (table != null) {
			manager.executeTask(new SingleThreadTask(table) {
				@Override
				public void doTask(ISingleData singleData) {
					ZTMaJongTable table = (ZTMaJongTable) singleData;

					role.setStatus(PlayerState.PS_PLAY_VALUE);
					role.setAuto(false);

					GMsg_12042010.Builder builder = GMsg_12042010.newBuilder();
					if (table.getWaitAction() == null) {
							
						if (table.getQueueWaitType() == HandleType.MAJIANG_WAIT_SHOW_DING_QUE
							|| table.getQueueWaitType() == HandleType.MAJIANG_WAIT_DING_QUE_TYPE) {
							table.setWaitAction(OptionsType.SET_QUE_TYPE);
						} else {
							table.setWaitAction(OptionsType.DISCARD_TILE);
						}		
					}
					builder.setAction(table.getWaitAction());
					builder.setCurrentSeat(table.getLastPlaySeat());
					builder.setNextSeat(table.getNextPlaySeat());
					builder.setWaitTime((int) (table.getCoolDownTime() / 1000));
					
					
					// 房间信息
					GGameInfo.Builder info = GGameInfo.newBuilder();
					info.setGameType(game.getGameType());
					info.setRoomId(game.getRoomId());
					info.setRoomType(game.getRoomType());

					ZTMajiangRole majiangRole = table.getMembers().get(
							role.getSeat() - 1);

					for (ZTMajiangRole other : table.getMembers()) {
						GMajiangPlayer.Builder player = GMajiangPlayer.newBuilder();
						GCDMajiangDingQue.Builder dingQueBuilder = GCDMajiangDingQue.newBuilder();
						dingQueBuilder.setSeat(other.getRole().getSeat());
						dingQueBuilder.setQuePaiType(other.getQueType());
						builder.addDingQue(dingQueBuilder.build());
						
						GPaiInfo.Builder destPaiBuilder = GPaiInfo.newBuilder();
						destPaiBuilder.addAllPai(other.getRecyclePai());
						player.setDestPai(destPaiBuilder);
						player.setPaiNum(other.getPai().size());
						player.setSeat(other.getRole().getSeat());
						player.setTargetHu(0);
						if (other.getHuType() != 0) {
							GPaiInfo.Builder huPaiBuilder = GPaiInfo
									.newBuilder();
							huPaiBuilder.addAllPai(other.getPai());
							player.setHuPai(huPaiBuilder);
						}
						// 牌墙
						for (Map.Entry<Integer, List<Integer>> entry : other
								.getShowPai().entrySet()) {
							GPaiQiang.Builder restPaiBuilder = GPaiQiang
									.newBuilder();
							GPaiInfo.Builder paiBuilder = GPaiInfo.newBuilder();
							paiBuilder.addAllPai(entry.getValue());
							restPaiBuilder.setPaiType(entry.getKey());
							restPaiBuilder.setPai(paiBuilder);
							player.addOperatePai(restPaiBuilder);
						}

						builder.addPlayer(player);
						
						
						GGameRole.Builder gameRole = GGameRole.newBuilder();
						Role target = other.getRole().getRole();
						gameRole.setRid(target.getRid());
						gameRole.setNick(target.getNick());
						gameRole.setGold(target.getGold());
						gameRole.setHead(target.getHead());
						gameRole.setLevel(target.getLevel());
						gameRole.setSeat(other.getRole().getSeat());
						gameRole.setAvatarId(other.getRole().getAvatarId());
						gameRole.setSex(target.getSex());
						info.addSprites(gameRole);
					}
					//角色信息
					builder.setGame(info);

					// 开局信息
					GOpenInfo.Builder openBuilder = GOpenInfo.newBuilder();

					GPaiInfo.Builder paiBuilder = GPaiInfo.newBuilder();
					paiBuilder.addAllPai(majiangRole.getPai());
					openBuilder.setDeskRest(table.getPais().size());
					openBuilder.setDicePoint1(table.getPoint1());
					openBuilder.setDicePoint2(table.getPoint2());
					openBuilder.setTuoPai(0);
					openBuilder.setHostSeat(table.getOwner());
					openBuilder.setHandPai(paiBuilder);
					builder.setOpenInfo(openBuilder);
					
					LogUtil.info("majiangRole.getPai()" + majiangRole.getPai());
					
					
					roleFunction.sendMessageToPlayer(role.getRole().getRid(),
							builder.build());
					LogUtil.info("getGameType" + game.getGameType() + " ："
							+ table.getWaitAction());
				}
			});

		} else {
			GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
			builder.setCurrentSeat(0);
			roleFunction.sendMessageToPlayer(role.getRole().getRid(),
					builder.build());
		}
	}

	/**
	 * 初始化麻将牌局
	 *
	 * @param owner
	 * @return
	 */
	public ZTMaJongTable initTable(Game game) {
		if (game.getStatus() > 0) {
			return null;
		}
		ZTMaJongTable mahJiangTable = new ZTMaJongTable(game);

		MajiangChengDuRoomCsv majiangRoomCsv = majiangRoomCache.getConfig(game
				.getRoomType());

		mahJiangTable.setTurnDuration(majiangRoomCsv.getTurnDuration());// 第一轮时间
		mahJiangTable.setTurn2Duration(majiangRoomCsv.getTurn2Duration());// 第二轮时间
		mahJiangTable.setActionDuration(majiangRoomCsv.getActionDuration());// 碰杠胡操作时间
		mahJiangTable.setOtpPunishment(majiangRoomCsv.getOTPunishment());// 最多超时次数
		mahJiangTable.setMaxFanShu(majiangRoomCsv.getMaxFanShu());// 最大番数

		// mahJiangTable.setOwner(owner.getId());
		for (Map.Entry<Long, GameRole> entry : game.getSpriteMap().entrySet()) {
			ZTMajiangRole member = new ZTMajiangRole(entry.getValue());
			mahJiangTable.getMembers().add(member);
			addRoleCache(member);

			// 修改前的
			/*
			 * MajiangBill value = new MajiangBill();
			 * value.setRid(entry.getValue().getRole().getRid());
			 * value.setSeat(entry.getValue().getSeat()); value.setWinTimes(1);
			 * mahJiangTable.getBills().put(entry.getValue().getSeat(), value);
			 */
			// 修改前的
		}

		tableMap.put(game.getRoomId(), mahJiangTable);
		//
		game.setStatus(GameStatus.WAIT_READY);

		// tableToWait(mahJiangTable, 0, 0, HandleType.MAJIANG_INIT,
		// TimeUtil.time()+15000);
		return mahJiangTable;
	}

	/**
	 * 初始化麻将
	 *
	 * @return
	 */
	public List<Integer> initAllPai() {

		List<Integer> allPai = new LinkedList<>();
		for (int i = 11; i < 40; i++) {
			if (i % 10 == 0) {
				continue;
			}
			for (int j = 0; j < 4; j++) {
				allPai.add(i);
			}
		}
		return allPai;
	}

	public void exitTable(Game game, Long rid) {
		ZTMaJongTable table = getTable(game.getRoomId());
		if (table != null) {
			GameRole gameRole = game.getSpriteMap().get(rid);
			if (gameRole != null) {

				for (ZTMajiangRole role : table.getMembers()) {
					if (gameRole.getStatus() != PlayerState.PS_EXIT_VALUE) {
						GMsg_12006005.Builder builder = GMsg_12006005
								.newBuilder();
						builder.setCurrentSeat(gameRole.getSeat());
						roleFunction.sendMessageToPlayer(role.getRole()
								.getRole().getRid(), builder.build());


					} /*else if (gameRole == role.getRole()) {
						GMsg_12006005.Builder builder = GMsg_12006005
								.newBuilder();
						builder.setCurrentSeat(gameRole.getSeat());
						roleFunction.sendMessageToPlayer(role.getRole()
								.getRole().getRid(), builder.build());
						LogUtil.error("自己赢了离开房间？");
					}*/

					// roleMap.remove(role.getRole().getRole().getRid());
					// table.getMembers().remove(role.getRole().getSeat()-1);
				}
				gameRole.setStatus(PlayerState.PS_EXIT_VALUE);
				game.getRoles().set(gameRole.getSeat() - 1, 0l);
				game.getSpriteMap().remove(rid);
				removeRoleCache(rid);
				LogUtil.error(gameRole.getRole().getNick() + "rid" + rid);
				if (game.getSpriteMap().size() <= 0) {
					tableMap.remove(game.getRoomId());
				}
			}
		}

	}

	/**
	 * 处理超时
	 */
	/**
	 * @param table
	 */
	public void endTable(ZTMaJongTable table) {
		table.setQueueWaitType(0);
		table.getGame().setStatus(GameStatus.END);
		LogUtil.info(table.getGame().getRoomId() + " end");
		LogUtil.info("endTable数量" + table.getMembers().size());
		LogUtil.info("endTable数量" + table.getBills().size());
		
		// 全部玩家推牌（全部牌都变成可见）
		for (ZTMajiangRole role : table.getMembers()) {
			int seat = role.getRole().getSeat();
			GBillsInfo.Builder billsInfo = null;
			if (table.getBills().get(seat) != null) {
				billsInfo = table.getBills().get(seat);
			} else {
				billsInfo = GBillsInfo.newBuilder();
			}
			
			GPaiInfo.Builder paiinfo = GPaiInfo.newBuilder();
			paiinfo.addAllPai(role.getPai());
			billsInfo.setPai(paiinfo);
			billsInfo.setRid(role.getRole().getRole().getRid());
			billsInfo.setSeat(seat);
			table.getBills().put(seat, billsInfo);
		}
		
		int aiGold = 0;
		GMsg_12042009.Builder endBuilder = GMsg_12042009.newBuilder();

		for (ZTMajiangRole role : table.getMembers()) {
			if (!table.getWinners().contains(role.getRole().getSeat())) {
				ExpCsv csv = expCache.getConfig(role.getRole().getRole().getLevel() + 1);
				roleFunction.expAdd(role.getRole().getRole(), csv.getMajiangLoseExp(), true);
			}
			// 任务检测
			missionFunction.checkTaskFinish(role.getRole().getRole().getRid(),
					TaskType.daily_task, MissionType.TIMES, GameType.CDMAJIANG);
		}
		// 结算增加花猪和无听（判断所有的牌）
		HashMap<Integer, Integer> huaZhuMemberMap = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> noTingMemberMap = new HashMap<Integer, Integer>();
		List<Integer> tingMember = new ArrayList<Integer>();
		List<Integer> allPai = new ArrayList<Integer>();
		int realPlayerCount = 0;
		int que_pai_type = 0;
		for (ZTMajiangRole role : table.getMembers()) {
			if (!table.getWinners().contains(role.getRole().getSeat())) {// 赢家之外
				allPai.clear();
				allPai.addAll(role.getPai());
				que_pai_type = role.getQueType();
				if (CDMahJongRule.huaZhu(allPai, que_pai_type) == false) {
					huaZhuMemberMap.put(role.getRole().getSeat(), ChengDuWinType.CD_HUA_ZHU_VALUE);// 判断花猪
					continue;
				}

				List<Integer> pai = role.getPai();
				int[][] shouPai = CDMahJongRule.conversionType(pai);
				if (CDMahJongRule.tingPai(shouPai, role.getShowPai()).size() > 0) {
					tingMember.add(role.getRole().getSeat());// 听牌
				} else {
					noTingMemberMap.put(role.getRole().getSeat(), ChengDuWinType.CD_WU_JIAO_VALUE);// 不听
				}

				// 连胜中断
				missionFunction.checkTaskFinish(role.getRole().getRole()
						.getRid(), TaskType.daily_task, MissionType.TIMES,
						GameType.CDMAJIANG);
			}
			if (!role.getRole().isRobot()) {
				realPlayerCount++;
			}
		}
		int loseSize = noTingMemberMap.size() + huaZhuMemberMap.size();// 花猪或者无听的人
		int tingSize = tingMember.size();// 听的人
		if (loseSize > 0 && tingSize > 0) {// 都有才能执行
			// 查花猪 赔不听 赔听 钱够则一对一赔付 钱不够扣则平分
			// 查无听 赔不听 钱够则一对一赔付 钱不够扣则平分
			if (huaZhuMemberMap.size() > 0) {// 查花猪
				for (Map.Entry<Integer, Integer> huaZhuEntry : huaZhuMemberMap
						.entrySet()) {
					int huaZhuseat = huaZhuEntry.getKey();
					ZTMajiangRole huaZhuMJRole = table.getMembers().get(
							huaZhuseat - 1);
					Role huaZhuRole = huaZhuMJRole.getRole().getRole();
					// 花猪需要赔的番数和金币
					int huTypeId = huaZhuEntry.getValue();
					MaJiangChengDuValueCsv maJiangValueCsv = maJiangValueCache
							.getConfig(huTypeId);

					// 番数
					int winTimes = maJiangValueCsv.getHuTimes();
					int diZhu = majiangRoomCache.getConfig(table.getGame().getRoomType()).getPotOdds();
					// 单个赔付的金币
					int singleLoseGold = (int) Math.pow(2, winTimes) * diZhu;
					// 需要赔付的人数
					int winNum = noTingMemberMap.size() + tingSize;
					int totalLoseGold = winNum * singleLoseGold;
					if (roleFunction.isPoChan(huaZhuRole.getGold(), huaZhuRole.getGoldPot(), totalLoseGold)) {
						AtomicIntegerArray log = getRoomCountCacheByRoomType(table.getGame().getRoomType());
						log.incrementAndGet(1);// 破产数
					}
					// 每个赢家可以分到的钱
					int actualLoseGold = 0;
					// 判断钱是否做够 赔给所有的人
					if (totalLoseGold <= huaZhuRole.getGold()) {// 钱够赔给所有的人
						actualLoseGold = singleLoseGold;
					} else {// 钱不够赔给所有的人 则平分
						actualLoseGold = huaZhuRole.getGold() / winNum;
					}

					if (noTingMemberMap.size() > 0) {// 花猪赔无听
						for (Map.Entry<Integer, Integer> noTingEntry : noTingMemberMap
								.entrySet()) {
							Integer noTingSeat = noTingEntry.getKey();
							ZTMajiangRole noTingMJRole = table.getMembers().get(noTingSeat - 1);
							Role noTingRole = noTingMJRole.getRole().getRole();
							// 花猪减钱
							if (huaZhuMJRole.getRole().isRobot()) {
								huaZhuMJRole.getRole()
										.getRole()
										.setGold(
												huaZhuMJRole.getRole()
														.getRole().getGold()
														- actualLoseGold);
								// 记录ai和玩家之间的盈亏情况
								if (!noTingMJRole.getRole().isRobot()) {
									npcFunction.updateGainOrLoss(huaZhuMJRole
											.getRole().getRole().getRid(),
											-actualLoseGold,table.getGame().getGameType(),table.getGame().getRoomType(),MoneyEvent.CDMAJIANG);
								}
							} else {
								roleFunction.goldSub(huaZhuRole,
										actualLoseGold, MoneyEvent.CDMAJIANG,
										true);
								// 记录ai和玩家之间的盈亏情况
								if (noTingMJRole.getRole().isRobot()) {
									npcFunction.updateGainOrLoss(noTingMJRole
											.getRole().getRole().getRid(),
											actualLoseGold,table.getGame().getGameType(),table.getGame().getRoomType(),MoneyEvent.CDMAJIANG);
								}
							}
							
							// billInfo 上面处理推牌已经创建了，保证不会为null
							GBillsInfo.Builder billsInfo = table.getBills().get(huaZhuseat);
							billsInfo.setGold(billsInfo.getGold() - actualLoseGold);
							GDetailBillsInfo.Builder detailBillsInfo = GDetailBillsInfo.newBuilder();
							detailBillsInfo.setNick(noTingRole.getNick());// 输显示别人的名称
							detailBillsInfo.setWinTimes(winTimes);
							detailBillsInfo.addWinTypes(huTypeId);
							detailBillsInfo.setGoldDetail(-actualLoseGold);
							billsInfo.addDetailBillsInfo(detailBillsInfo);
							table.getBills().put(huaZhuseat, billsInfo);
							// 无听赢钱
							if (noTingMJRole.getRole().isRobot()) {
								noTingRole.setGold(noTingRole.getGold() + actualLoseGold);
								// 记录ai和玩家之间的盈亏情况
								if (!huaZhuMJRole.getRole().isRobot()) {
									npcFunction.updateGainOrLoss(noTingMJRole
											.getRole().getRole().getRid(),
											actualLoseGold,table.getGame().getGameType(),table.getGame().getRoomType(),MoneyEvent.CDMAJIANG);
								}
							} else {
								roleFunction.goldAdd(noTingRole, actualLoseGold, MoneyEvent.CDMAJIANG, true);
								// 记录ai和玩家之间的盈亏情况
								if (huaZhuMJRole.getRole().isRobot()) {
									npcFunction.updateGainOrLoss(huaZhuMJRole
											.getRole().getRole().getRid(),
											-actualLoseGold,table.getGame().getGameType(),table.getGame().getRoomType(),MoneyEvent.CDMAJIANG);
								}
							}
							
							// billsInfo1 上面处理推牌已经创建了，保证不会为null
							GBillsInfo.Builder billsInfo1 = table.getBills().get(noTingSeat);
							billsInfo1.setGold(billsInfo1.getGold() + actualLoseGold);
							GDetailBillsInfo.Builder detailBillsInfo1 = GDetailBillsInfo.newBuilder();
							detailBillsInfo1.setNick(huaZhuRole.getNick());// 输显示别人的名称
							detailBillsInfo1.setWinTimes(winTimes);
							detailBillsInfo1.addWinTypes(huTypeId);
							detailBillsInfo1.setGoldDetail(actualLoseGold);
							billsInfo1.addDetailBillsInfo(detailBillsInfo1);
							table.getBills().put(noTingSeat, billsInfo1);
						}
					}
					for (Integer tingSeat : tingMember) {// 花猪赔听
						ZTMajiangRole tingMJRole = table.getMembers().get(tingSeat - 1);
						Role tingRole = tingMJRole.getRole().getRole();
						// 花猪减钱
						if (huaZhuMJRole.getRole().isRobot()) {
							huaZhuMJRole
									.getRole()
									.getRole()
									.setGold(
											huaZhuMJRole.getRole().getRole()
													.getGold()
													- actualLoseGold);
							// 记录ai和玩家之间的盈亏情况
							if (!tingMJRole.getRole().isRobot()) {
								npcFunction.updateGainOrLoss(huaZhuMJRole
										.getRole().getRole().getRid(),
										-actualLoseGold,table.getGame().getGameType(),table.getGame().getRoomType(),MoneyEvent.CDMAJIANG);
							}

						} else {
							roleFunction.goldSub(huaZhuRole, actualLoseGold,
									MoneyEvent.CDMAJIANG, true);
							// 记录ai和玩家之间的盈亏情况
							if (tingMJRole.getRole().isRobot()) {
								npcFunction.updateGainOrLoss(tingMJRole
										.getRole().getRole().getRid(),
										actualLoseGold,table.getGame().getGameType(),table.getGame().getRoomType(),MoneyEvent.CDMAJIANG);
							}
						}
						
						// billsInfo 上面处理推牌已经创建了，保证不会为null
						GBillsInfo.Builder billsInfo = table.getBills().get(huaZhuseat);
						billsInfo.setGold(billsInfo.getGold() - actualLoseGold);
						GDetailBillsInfo.Builder detailBillsInfo = GDetailBillsInfo.newBuilder();
						detailBillsInfo.setNick(tingRole.getNick());// 输显示别人的名称
						detailBillsInfo.setWinTimes(winTimes);
						detailBillsInfo.addWinTypes(huTypeId);
						detailBillsInfo.setGoldDetail(-actualLoseGold);
						billsInfo.addDetailBillsInfo(detailBillsInfo);
						table.getBills().put(huaZhuseat, billsInfo);
						// 听赢钱
						if (tingMJRole.getRole().isRobot()) {
							tingRole.setGold(tingRole.getGold() + actualLoseGold);
							// 记录ai和玩家之间的盈亏情况
							if (!huaZhuMJRole.getRole().isRobot()) {
								npcFunction.updateGainOrLoss(tingMJRole
										.getRole().getRole().getRid(),
										actualLoseGold,table.getGame().getGameType(),table.getGame().getRoomType(),MoneyEvent.CDMAJIANG);
							}
						} else {
							roleFunction.goldAdd(tingRole, actualLoseGold, MoneyEvent.CDMAJIANG, true);
							// 记录ai和玩家之间的盈亏情况
							if (huaZhuMJRole.getRole().isRobot()) {
								npcFunction.updateGainOrLoss(huaZhuMJRole
										.getRole().getRole().getRid(),
										actualLoseGold,table.getGame().getGameType(),table.getGame().getRoomType(),MoneyEvent.CDMAJIANG);
							}
						}
						
						// billsInfo1 上面处理推牌已经创建了，保证不会为null
						GBillsInfo.Builder billsInfo1 = table.getBills().get(tingSeat);
						billsInfo1.setGold(billsInfo1.getGold() + actualLoseGold);
						GDetailBillsInfo.Builder detailBillsInfo1 = GDetailBillsInfo.newBuilder();
						detailBillsInfo1.setNick(huaZhuRole.getNick());// 输显示别人的名称
						detailBillsInfo1.setWinTimes(winTimes);
						detailBillsInfo1.addWinTypes(huTypeId);
						detailBillsInfo1.setGoldDetail(actualLoseGold);
						billsInfo1.addDetailBillsInfo(detailBillsInfo1);
						table.getBills().put(tingSeat, billsInfo1);
					}
				}
			}
			if (noTingMemberMap.size() > 0) {// 查无听
				// 查无听
				for (Map.Entry<Integer, Integer> noTingEntry : noTingMemberMap
						.entrySet()) {
					int noTingseat = noTingEntry.getKey();
					ZTMajiangRole noTingMJRole = table.getMembers().get(
							noTingseat - 1);
					Role noTingRole = noTingMJRole.getRole().getRole();
					// 无听需要赔的番数和金币
					int huTypeId = noTingEntry.getValue();
					MaJiangChengDuValueCsv maJiangValueCsv = maJiangValueCache
							.getConfig(huTypeId);

					// 番数
					int winTimes = maJiangValueCsv.getHuTimes();
					int diZhu = majiangRoomCache.getConfig(table.getGame().getRoomType()).getPotOdds();
					// 单个赔付的金币
					int singleLoseGold = (int) Math.pow(2, winTimes) * diZhu;
					// 总共需要赔付的钱
					int totalLoseGold = tingSize * singleLoseGold;
					if (roleFunction.isPoChan(noTingRole.getGold(),
							noTingRole.getGoldPot(), totalLoseGold)) {
						AtomicIntegerArray log = getRoomCountCacheByRoomType(table
								.getGame().getRoomType());
						log.incrementAndGet(1);// 破产数
					}
					// 每个赢家可以分到的钱
					int actualLoseGold = 0;
					// 判断钱是否做够 赔给所有的人
					if (totalLoseGold <= noTingRole.getGold()) {// 钱够赔给所有的人
						actualLoseGold = singleLoseGold;
					} else {// 钱不够赔给所有的人
						actualLoseGold = noTingRole.getGold() / tingSize;
					}
					for (Integer tingSeat : tingMember) {// 无听赔听
						ZTMajiangRole tingMJRole = table.getMembers().get(tingSeat - 1);
						Role tingRole = tingMJRole.getRole().getRole();
						// 无听减钱
						if (noTingMJRole.getRole().isRobot()) {
							noTingMJRole
									.getRole()
									.getRole()
									.setGold(
											noTingMJRole.getRole().getRole()
													.getGold()
													- actualLoseGold);
							// 记录ai和玩家之间的盈亏情况
							if (!tingMJRole.getRole().isRobot()) {
								npcFunction.updateGainOrLoss(noTingMJRole
										.getRole().getRole().getRid(),
										-actualLoseGold,table.getGame().getGameType(),table.getGame().getRoomType(),MoneyEvent.CDMAJIANG);
							}
						} else {
							roleFunction.goldSub(noTingRole, actualLoseGold,
									MoneyEvent.CDMAJIANG, true);
							// 记录ai和玩家之间的盈亏情况
							if (tingMJRole.getRole().isRobot()) {
								npcFunction.updateGainOrLoss(tingMJRole
										.getRole().getRole().getRid(),
										actualLoseGold,table.getGame().getGameType(),table.getGame().getRoomType(),MoneyEvent.CDMAJIANG);
							}
						}
						
						// billsInfo 上面处理推牌已经创建了，保证不会为null
						GBillsInfo.Builder billsInfo = table.getBills().get(noTingseat);
						billsInfo.setGold(billsInfo.getGold() - actualLoseGold);
						GDetailBillsInfo.Builder detailBillsInfo = GDetailBillsInfo.newBuilder();
						detailBillsInfo.setNick(tingRole.getNick());// 输显示别人的名称
						detailBillsInfo.setWinTimes(winTimes);
						detailBillsInfo.addWinTypes(huTypeId);
						detailBillsInfo.setGoldDetail(-actualLoseGold);
						billsInfo.addDetailBillsInfo(detailBillsInfo);
						table.getBills().put(noTingseat, billsInfo);
						// 听赢钱
						if (tingMJRole.getRole().isRobot()) {
							tingRole.setGold(tingRole.getGold()
									+ actualLoseGold);
							// 记录ai和玩家之间的盈亏情况
							if (!noTingMJRole.getRole().isRobot()) {
								npcFunction.updateGainOrLoss(tingMJRole
										.getRole().getRole().getRid(),
										actualLoseGold,table.getGame().getGameType(),table.getGame().getRoomType(),MoneyEvent.CDMAJIANG);
							}

						} else {
							roleFunction.goldAdd(tingRole, actualLoseGold,
									MoneyEvent.CDMAJIANG, true);
							// 记录ai和玩家之间的盈亏情况
							if (noTingMJRole.getRole().isRobot()) {
								npcFunction.updateGainOrLoss(noTingMJRole
										.getRole().getRole().getRid(),
										-actualLoseGold,table.getGame().getGameType(),table.getGame().getRoomType(),MoneyEvent.CDMAJIANG);
							}
						}
						
						// billsInfo1 上面处理推牌已经创建了，保证不会为null
						GBillsInfo.Builder billsInfo1 = table.getBills().get(tingSeat);
						billsInfo1.setGold(billsInfo1.getGold() + actualLoseGold);
						GDetailBillsInfo.Builder detailBillsInfo1 = GDetailBillsInfo.newBuilder();
						detailBillsInfo1.setNick(noTingRole.getNick());// 输显示别人的名称
						detailBillsInfo1.setWinTimes(winTimes);
						detailBillsInfo1.addWinTypes(huTypeId);
						detailBillsInfo1.setGoldDetail(actualLoseGold);
						billsInfo1.addDetailBillsInfo(detailBillsInfo1);
						table.getBills().put(tingSeat, billsInfo1);
					}
				}
			}

		}

		// 检测流局
		for (Map.Entry<Integer, GBillsInfo.Builder> entry : table.getBills()
				.entrySet()) {
			GBillsInfo.Builder billsInfo = entry.getValue();
			if (billsInfo.getDetailBillsInfoCount() == 0) {
				GDetailBillsInfo.Builder value = GDetailBillsInfo.newBuilder();
				value.setGoldDetail(0);
				value.setNick("");
				value.setWinTimes(0);
				value.addWinTypes(ChengDuWinType.CD_NO_WINNER_VALUE);
				billsInfo.addDetailBillsInfo(value);
			}
			if (billsInfo.getNick() == null) {
				billsInfo.setNick("");
			}
			LogUtil.info("endTable1 胡的黄金" + billsInfo.getNick()
					+ billsInfo.getGold() + "座位" + billsInfo.getSeat());
			endBuilder.addBills(billsInfo);

		}
		// 广播房间所有
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(), endBuilder.build());

		table.getGame().setStatus(GameStatus.END_REWARD);
		table.getGame().setEndTime(System.currentTimeMillis());

		// G_T_C 插入日志
		List<Object[]> roleInfoList = new ArrayList<>();
		// 拼接：角色信息流水明细；格式：角色id:番数_金币变化_另外玩家,|
		List<String> roleInfoDetailList = new ArrayList<>();
		majiangLogDao.crateRoleInfoAndDetail(table, roleInfoList,
				roleInfoDetailList);// 处理role info 和 detail 组装到list，为了拼接字符串
		String roleInfoDetails = StringUtil.listToString(roleInfoDetailList,
				StringUtil.DELIMITER_BETWEEN_ITEMS);
		LogUtil.debug("role_info_detail=" + roleInfoDetails);
		String roleInfo = StringUtil.listArrayToString(roleInfoList,
				StringUtil.DELIMITER_BETWEEN_ITEMS,
				StringUtil.DELIMITER_INNER_ITEM);
		List<Long> winRoleIdList = majiangLogDao.getWinRoleList(table);
		String winners = StringUtil.listToString(winRoleIdList,
				StringUtil.DELIMITER_BETWEEN_ITEMS);
		LogUtil.debug("role_info=" + roleInfo);
		CDMajiangLog majiangLog = majiangLogDao.getMajiangLog(table.getGame(),
				roleInfo, roleInfoDetails, winners, aiGold, table.getMembers()
						.size(), realPlayerCount);
		majiangLogDao.addLog(majiangLog);

		for (ZTMajiangRole role : table.getMembers()) {
			if (role.getRole() != null) {
				// G_T_C 处理房间登录日志
				Role role2 = role.getRole().getRole();
				Game game = table.getGame();
				if (role2 != null && game != null && !role.getRole().isRobot()) {
					roomLogFunction.dealRoomRoleLoginLog(game.getGameType(),
							role2, "", 1);
				}
			}
		}

		for (ZTMajiangRole role : table.getMembers()) {
			if (role.getRole().getStatus() == PlayerState.PS_EXIT_VALUE) {
				// TODO
				// table.getGame().setEndTime(System.currentTimeMillis() -
				// 60000);
				GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
				builder.setCurrentSeat(role.getRole().getSeat());
				roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
						builder.build());
				roomFunction.endGame(table.getGame());
				return;
			}
		}
	}
	/**
	 * 处理超时
	 */
	/**
	 * @param table
	 */
	public void endTable2(ZTMaJongTable table) {
		table.setQueueWaitType(0);
		table.getGame().setStatus(GameStatus.END);
		LogUtil.info(table.getGame().getRoomId() + " end");
		LogUtil.info("endTable数量" + table.getMembers().size());
		LogUtil.info("endTable数量" + table.getBills().size());
		
		// 全部玩家推牌（全部牌都变成可见）
		for (ZTMajiangRole role : table.getMembers()) {
			int seat = role.getRole().getSeat();
			GBillsInfo.Builder billsInfo = null;
			if (table.getBills().get(seat) != null) {
				billsInfo = table.getBills().get(seat);
			} else {
				billsInfo = GBillsInfo.newBuilder();
			}
			
			GPaiInfo.Builder paiinfo = GPaiInfo.newBuilder();
			paiinfo.addAllPai(role.getPai());
			billsInfo.setPai(paiinfo);
			billsInfo.setRid(role.getRole().getRole().getRid());
			billsInfo.setSeat(seat);
			table.getBills().put(seat, billsInfo);
		}
		
		int aiGold = 0;
		GMsg_12042009.Builder endBuilder = GMsg_12042009.newBuilder();
		for (ZTMajiangRole role : table.getMembers()) {
			if (!table.getWinners().contains(role.getRole().getSeat())) {
				ExpCsv csv = expCache.getConfig(role.getRole().getRole().getLevel() + 1);
				roleFunction.expAdd(role.getRole().getRole(), csv.getMajiangLoseExp(), true);
			}
			// 任务检测
			missionFunction.checkTaskFinish(role.getRole().getRole().getRid(),TaskType.daily_task,
					MissionType.TIMES, GameType.CDMAJIANG);
		}
		// 结算增加花猪和无听（判断所有的牌）
		HashMap<Integer, Integer> loseMemberMap = new HashMap<Integer, Integer>();
		List<Integer> getMember = new ArrayList<Integer>();
		List<Integer> allPai = new ArrayList<Integer>();
		Map<Integer, List<Integer>> showPai = new HashMap<Integer, List<Integer>>();
		int realPlayerCount = 0;
		int que_pai_type = 0;
		for (ZTMajiangRole role : table.getMembers()) {
			if (!table.getWinners().contains(role.getRole().getSeat())) {// 赢家之外
				allPai.clear();
				showPai.clear();
				allPai.addAll(role.getPai());
				showPai = role.getShowPai();
				que_pai_type = role.getQueType();
				for (Entry<Integer, List<Integer>> entry : showPai.entrySet()) {
					allPai.addAll(entry.getValue());
				}
				if (CDMahJongRule.huaZhu(allPai, que_pai_type) == false) {
					loseMemberMap.put(role.getRole().getSeat(), ChengDuWinType.CD_HUA_ZHU_VALUE);// 判断花猪
					continue;
				}

				List<Integer> pai = role.getPai();
				int[][] shouPai = CDMahJongRule.conversionType(pai);
				if (CDMahJongRule.tingPai(shouPai,role.getShowPai()).size() > 0) {
					getMember.add(role.getRole().getSeat());// 听牌
				} else {
					loseMemberMap.put(role.getRole().getSeat(), ChengDuWinType.CD_WU_JIAO_VALUE);// 不听
				}
					
				// 连胜中断
				missionFunction.checkTaskFinish(role.getRole().getRole()
						.getRid(), TaskType.daily_task, MissionType.CONTINUE_WIN, GameType.MAJIANG,
						false);
			}
			if(!role.getRole().isRobot()){
				realPlayerCount++;
			}
		}
		int loseSize = loseMemberMap.size();// 花猪或者无听的人
		int getSize = getMember.size();// 听的人
		if (loseSize > 0 && getSize > 0) {// 都有才能执行
			for (Map.Entry<Integer, Integer> entry : loseMemberMap.entrySet()) {
				int seat = entry.getKey();
				int huTypeId = entry.getValue(); 
				MaJiangChengDuValueCsv maJiangValueCsv = maJiangValueCache.getConfig(huTypeId);
				// 输家
				int winTimes = maJiangValueCsv.getHuTimes();
				int diZhu = majiangRoomCache.getConfig(table.getGame().getRoomType()).getPotOdds();
				int loseGold = (int) Math.pow(2, winTimes) * diZhu;

				ZTMajiangRole ZTMajiangRole = table.getMembers().get(seat - 1);
				Role role = ZTMajiangRole.getRole().getRole();

				if (roleFunction.isPoChan(role.getGold(), role.getGoldPot(),loseGold)) {
					AtomicIntegerArray log = getRoomCountCacheByRoomType(table.getGame().getRoomType());
					log.incrementAndGet(1);// 破产数
				}
				if (role.getGold() < loseGold) {
					loseGold = role.getGold();
				}
				
				if(ZTMajiangRole.getRole().isRobot()){
					ZTMajiangRole.getRole().getRole().setGold(ZTMajiangRole.getRole().getRole().getGold() - loseGold);
					aiGold -= loseGold;
				}else{
					roleFunction.goldSub(role, loseGold, MoneyEvent.CDMAJIANG, true);
				}
				
				// billInfo- 上面处理推牌已经创建了，保证不会为null
				GBillsInfo.Builder billsInfo = table.getBills().get(seat);	
				billsInfo.setGold(billsInfo.getGold() - loseGold);
				GDetailBillsInfo.Builder detailBillsInfo = GDetailBillsInfo.newBuilder();
				detailBillsInfo.setNick(role.getNick());// 输显示别人的名称
				detailBillsInfo.setWinTimes(winTimes);
				detailBillsInfo.addWinTypes(huTypeId);
				detailBillsInfo.setGoldDetail(-loseGold);
				billsInfo.addDetailBillsInfo(detailBillsInfo);
				table.getBills().put(seat, billsInfo);
				
				int getGold = loseGold / getMember.size();
				// 赢家
				for (Integer seat2 : getMember) {
					ZTMajiangRole ZTMajiangRole2 = table.getMembers().get(seat - 1);
					Role role2 = ZTMajiangRole2.getRole().getRole();
					
					if(ZTMajiangRole2.getRole().isRobot()){
						role2.setGold(role2.getGold() + getGold);
						aiGold += getGold;
					}else{
						roleFunction.goldAdd(role2, getGold, MoneyEvent.CDMAJIANG, true);
					}
					
					// billInfo 上面处理推牌已经创建了，保证不会为null
					GBillsInfo.Builder billsInfo2 = table.getBills().get(seat2);			
					billsInfo2.setGold(billsInfo2.getGold() + getGold);
					GDetailBillsInfo.Builder detailBillsInfo2 = GDetailBillsInfo.newBuilder();
					detailBillsInfo2.setNick(role.getNick());// 输显示别人的名称
					detailBillsInfo2.setWinTimes(winTimes);
					detailBillsInfo2.addWinTypes(huTypeId);
					detailBillsInfo2.setGoldDetail(getGold);
					billsInfo2.addDetailBillsInfo(detailBillsInfo2);
					table.getBills().put(seat2, billsInfo2);
				}
			}
		}
			
		for (Map.Entry<Integer, GBillsInfo.Builder> entry : table.getBills().entrySet()) {
			GBillsInfo.Builder billsInfo = entry.getValue();
			if (billsInfo.getDetailBillsInfoCount() == 0) {
				// 没有一个赢家
				GDetailBillsInfo.Builder value = GDetailBillsInfo.newBuilder();
				value.setGoldDetail(0);
				value.setNick("");
				value.setWinTimes(0);
				value.addWinTypes(ChengDuWinType.CD_NO_WINNER.ordinal());
				billsInfo.addDetailBillsInfo(value);
			}
			if (billsInfo.getNick() == null) {
				billsInfo.setNick("");
			}
			
			LogUtil.info("endTable1 胡的黄金" + billsInfo.getNick()
					+ billsInfo.getGold() + "座位" + billsInfo.getSeat());
			endBuilder.addBills(billsInfo);

		}
		
		// 广播房间所有
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				endBuilder.build());
		
		table.getGame().setStatus(GameStatus.END_REWARD);
		table.getGame().setEndTime(System.currentTimeMillis());

		for (ZTMajiangRole role : table.getMembers()) {
			if (role.getRole() != null) {
				// G_T_C 处理房间登录日志
				Role role2 = role.getRole().getRole();
				Game game = table.getGame();
				if (role2 != null && game != null && ! role.getRole().isRobot()) {
					roomLogFunction.dealRoomRoleLoginLog(
							game.getGameType(), role2, role2.getLastLoginIp(), 1);
				}
			}
		}
		
		for (ZTMajiangRole role : table.getMembers()) {
			if (role.getRole().getStatus() == PlayerState.PS_EXIT_VALUE) {
				// TODO
				//table.getGame().setEndTime(System.currentTimeMillis() - 60000);
				GMsg_12006005.Builder builder = GMsg_12006005
						.newBuilder();
				builder.setCurrentSeat(role.getRole().getSeat());
				roleFunction.sendMessageToPlayers(
						table.getGame().getRoles(), builder.build());
				roomFunction.endGame(table.getGame());
				return;
			}
		}
	}

	/**
	 * 处理超时
	 */
	public void tableToWait(ZTMaJongTable table, int targetSeat, int nextSeat,
			int handleType, long coolDownTime, Long... targetTime) {
		if (targetTime != null && targetTime.length > 0) {
			table.setTargetTime(targetTime[0]);
		}
		table.setLastPlaySeat(targetSeat);
		table.setNextSeat(nextSeat);
		table.setQueueWaitType(handleType);
		table.setCoolDownTime(coolDownTime);
	}

	/**
	 * 流程处理
	 */
	public void autoAction() {
		for (Map.Entry<Long, ZTMaJongTable> entry : tableMap.entrySet()) {
			ZTMaJongTable table = entry.getValue();
			if (table.getGame().getStatus() > GameStatus.RUNNING) {
				// table.setQueueWaitType(HandleType.MAJIANG_HU);
				// table.setCoolDownTime(System.currentTimeMillis()+5000);
				continue;
			}
			if (table.getQueueWaitType() == 0) {
				// continue;
				// table.setQueueWaitType(HandleType.MAJIANG_HU);
				// table.setCoolDownTime(System.currentTimeMillis() + 1000);
				/*for (GameRole gameRole : table.getGame().getSpriteMap()
						.values()) {
					if (!gameRole.isRobot()) {
						int i = 0;
						// table.setQueueWaitType(HandleType.MAJIANG_GET_PAI);
						// table.setCoolDownTime(System.currentTimeMillis() +
						// 1000);
					}
				}*/
				continue;
			}
			manager.executeTask(new SingleThreadTask(table) {
				@Override
				public void doTask(ISingleData singleData) {
					ZTMaJongTable table = (ZTMaJongTable) singleData;

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
						ZTMajiangRole role = null;
						if (table.getLastPlaySeat() != 0) {
							role = table.getMembers().get(
									table.getLastPlaySeat() - 1);
						}

						Event event = new Event(tableState, table, role);
						DispatchEvent.dispacthEvent(event);
					} catch (Exception e) {
						table.setQueueWaitType(tableState);
						table.setCoolDownTime(currentTimeMillis + 5000);
						LogUtil.error("麻将流程处理异常", e);
					}
				}
			});
		}
	}

	/**
	 * 处理出牌
	 *
	 * @param player
	 * @param maJongTable
	 * @param member
	 * @param pai
	 */
	public void dealDisCard(ZTMaJongTable table, Integer pai) {
		table.setQueueWaitType(0);
		int seat = table.getLastPlaySeat();
		ZTMajiangRole role = table.getMembers().get(seat - 1);
		if (!role.getRole().isRobot()) {
			LogUtil.info("！！！麻将玩家" + role.getRole().getRole().getNick()
					+ " 正在出牌！！！");
		}
		if (!role.getPai().contains(pai)) {
			LogUtil.info("！！！麻将玩家" + role.getRole().getRole().getNick()
					+ " 没有这张牌！！！" + pai);
		}

		LogUtil.info(table.getGame().getRoomId() + " seat-" + seat + " pai"
				+ pai);

		table.setLastOutPai(seat);
		table.setLastRealSeat(seat);

		role.getPai().remove(pai);
		table.setLastPai(pai);
		role.getRecyclePai().add(pai);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addPai(pai);
		GMsg_12042008.Builder builder = GMsg_12042008.newBuilder();
		builder.setNextSeat(table.getNextPlaySeat());
		builder.setOption(OptionsType.DISCARD_TILE);
		builder.setTargetSeat(seat);
		builder.setOperatePai(info);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());
		boolean flag = true;

		boolean onlyRobot = true;

		table.getCanOptions().clear();
		table.getWaiter().clear();
		table.getYetOptions().remove(role.getRole().getSeat());

		for (ZTMajiangRole other : table.getMembers()) {
			if (other != role && other.getHuType() == 0) {

				GMsg_12042007.Builder waitBuilder = GMsg_12042007.newBuilder();
				
				if (table.getPais().size() > 0) {
					if (canGang(table, other)) {
						waitBuilder.addOption(OptionsType.EXPOSED_GANG);
						LogUtil.info(table.getGame().getRoomId() + " EXPOSED_GANG "
								+ other.getRole().getRole().getNick() + "明杠 " + pai);
					}
					if (canPeng(table, other)) {
						int lastQiPai = other.getLastQiPai();
						if (lastQiPai != pai) {	// 曾经弃过这张牌，不能再碰				
							waitBuilder.addOption(OptionsType.PENG);
							LogUtil.info(table.getGame().getRoomId() + " PENG "
									+ other.getRole().getRole().getNick() + "碰 " + pai);
						}
					}
				}
	
				if (canHu(table, other, table.getLastPai())) {
					// 弃牌的番数肯定一样的，直接检查番数就行了	
					List<Integer> huFanType2 = dealFanType2(other, table);
					int FanNum = dealFanNum(huFanType2, table);		
					if (null != other.getHuFan() && other.getHuFan() != 0) {
					//	System.out.println("玩家："+other.getRole().getSeat()+"上一轮能胡没胡  所以不能胡");
					//	System.out.println("上次没胡的番数:"+other.getHuFan());
					//	System.out.println("这次胡的番数:"+FanNum);
						if (FanNum > other.getHuFan() ) {
							//System.out.println("玩家："+other.getRole().getSeat()+"上一轮能胡没胡  但番数大所以能胡");
							waitBuilder.addOption(OptionsType.ANNOUNCE_WIN);
							LogUtil.info(table.getGame().getRoomId() + " ANNOUNCE_WIN " + other.getRole().getRole().getNick() + "胡 " + pai);
							other.setHuFan(FanNum);
						}
					}else {
						waitBuilder.addOption(OptionsType.ANNOUNCE_WIN);
						LogUtil.info(table.getGame().getRoomId() + " ANNOUNCE_WIN " + other.getRole().getRole().getNick() + "胡 " + pai);
						other.setHuFan(FanNum);
					}			
				}
				if (waitBuilder.getOptionCount() > 0) {
					if (!other.getRole().isRobot()) {
						onlyRobot = false;
					}
					table.getCanOptions().put(other.getRole().getSeat(),
							waitBuilder.getOptionList());

					// 可进行操作
					roleFunction.sendMessageToPlayer(other.getRole().getRole()
							.getRid(), waitBuilder.build());
					flag = false;
				}

			}

		}

		if (flag) {
			tableToWait(table, table.getLastPlaySeat(),
					table.getNextPlaySeat(), HandleType.MAJIANG_WAIT,
					System.currentTimeMillis() + 0);
		} else {

			// 优先级胡(AI手上有两张或以上癞子时 优先自摸)
			for (ZTMajiangRole other : table.getMembers()) {
				if (other != role && other.getHuType() == 0) {
					List<OptionsType> data = table.getCanOptions().get(
							other.getRole().getSeat());

					if (data != null && data.contains(OptionsType.ANNOUNCE_WIN)) {

						// 有别人操作
						GMsg_12042006.Builder builder3 = GMsg_12042006.newBuilder();
						/*builder3.setAction(OptionsType.PENG);
						builder3.setCurrentSeat(seat);
						builder3.setWaitTime(TimeUtil.time() + table.getActionDuration());
						builder3.setOverTime(TimeUtil.time() + table.getActionDuration());
						roleFunction.sendMessageToPlayers(table.getGame()
								.getRoles(), builder3.build());*/

						if (other.getRole().isRobot()) {
							int interval = MathUtil.randomNumber(1000, 4000);
							tableToWait(table, other.getRole().getSeat(),
										table.getNextPlaySeat(),
										HandleType.MAJIANG_HU,
										System.currentTimeMillis() + interval);

						}else {
							if (other.getRole().isAuto()) {
								int interval = MathUtil.randomNumber(1500, 2500);
								tableToWait(table, other.getRole().getSeat(),
										table.getNextPlaySeat(),
										HandleType.MAJIANG_HU,
										System.currentTimeMillis() + interval);
							} else {
								tableToWait(table, other.getRole().getSeat(),
										table.getNextPlaySeat(),
										HandleType.MAJIANG_HU,
										System.currentTimeMillis() + (table.getActionDuration()+1) * 1000);
							}
						}
						return;
					}
				}
			}
			// 其他根据响应排队
			for (ZTMajiangRole other : table.getMembers()) {
				if (other != role
						&& !table.getWinners().contains(
								other.getRole().getSeat())) {
					List<OptionsType> data = table.getCanOptions().get(
							other.getRole().getSeat());
					if (data != null) {
						if (other.getRole().isAuto()) {
							if (onlyRobot) {
								// 机器人默认就第一个就操作了
								int pai_type = pai / 10;
								if (data.contains(OptionsType.EXPOSED_GANG)) {
									table.setWaitSeat(other.getRole().getSeat());

									int interval = MathUtil.randomNumber(850,
											1200);
									if (Math.random() > 0.9) {
										interval = MathUtil.randomNumber(1250,
												3200);
									}
									if (pai_type == other.getQueType()) {
										tableToWait(table,
												table.getLastPlaySeat(),
												table.getNextPlaySeat(),
												HandleType.MAJIANG_WAIT,
												System.currentTimeMillis()
														+ interval);
									} else {
										tableToWait(table, other.getRole()
												.getSeat(),
												table.getNextPlaySeat(),
												HandleType.MAJIANG_GANG,
												System.currentTimeMillis()
														+ interval);
									}

								} else if (data.contains(OptionsType.PENG)) {
									table.setWaitSeat(other.getRole().getSeat());

									int interval = MathUtil.randomNumber(1050,
											1300);
									if (Math.random() > 0.9) {
										interval = MathUtil.randomNumber(1350,
												3200);
									}
									if (pai_type == other.getQueType()) {
										tableToWait(table,
												table.getLastPlaySeat(),
												table.getNextPlaySeat(),
												HandleType.MAJIANG_WAIT,
												System.currentTimeMillis()
														+ interval);
									} else {
										tableToWait(table, other.getRole()
												.getSeat(),
												table.getNextPlaySeat(),
												HandleType.MAJIANG_PENG,
												System.currentTimeMillis()
														+ interval);
									}

								} else {
									tableToWait(table, table.getLastPlaySeat(),
											table.getNextPlaySeat(),
											HandleType.MAJIANG_WAIT,
											System.currentTimeMillis() + 500);
								}

								break;
							} else {
								tableToWait(table, table.getLastPlaySeat(),
										table.getNextPlaySeat(),
										HandleType.MAJIANG_WAIT,
										System.currentTimeMillis() + (table.getActionDuration()) * 1000 - 500);
								if (other.getRole().isAuto()){
									dealPass(table, other);
								}
								
							}

						} else {
							tableToWait(table, table.getLastPlaySeat(),
									table.getNextPlaySeat(),
									HandleType.MAJIANG_WAIT,
									System.currentTimeMillis() + (table.getActionDuration()+1) * 1000);
						}
					}

				}
			}
			// 通知等待别人操作
			GMsg_12042006.Builder builderWait = GMsg_12042006.newBuilder();
			/*builderWait.setAction(OptionsType.PENG);
			builderWait.setCurrentSeat(seat);
			builderWait
					.setWaitTime(TimeUtil.time() + table.getActionDuration());
			builderWait.setOverTime(TimeUtil.time() + table.getActionDuration());
			roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
					builderWait.build());*/
		}

		/*
		 * if (table.getPais().size() == 0) { //TODO
		 * 
		 * GMsg_12011009.Builder endBuilder = GMsg_12011009.newBuilder(); for
		 * (ZTMajiangRole majiangRole : table.getMembers()) { GBillsInfo.Builder
		 * end = GBillsInfo.newBuilder();
		 * end.setWinTimes(majiangRole.getCurrentPower()); end.setGold(333);
		 * end.setSeat(majiangRole.getRole().getSeat());
		 * endBuilder.addBills(end);
		 * 
		 * } roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
		 * endBuilder.build());
		 * 
		 * table.getGame().setStatus(GameStatus.END_REWARD);
		 * 
		 * } else{
		 * 
		 * 
		 * }
		 */

	}

	/**
	 * 通知操作结果并提交下一操作
	 */
	public void processAction(ZTMaJongTable table, OptionsType action,
			int nextSeat, int handleType, long time) {
		GMsg_12042008.Builder builder = GMsg_12042008.newBuilder();

		GPaiInfo.Builder value = GPaiInfo.newBuilder();
		builder.setOption(action);
		builder.setNextSeat(nextSeat);
		builder.setTargetSeat(table.getLastPlaySeat());
		builder.setOperatePai(value);

		if (action == OptionsType.DISCARD_TILE) {
			value.addPai(table.getLastPai());
		}

		builder.setOperatePai(value);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());
		/*
		 * LogUtil.info(table.getGame().getRoomId() + " seat " +
		 * table.getLastPlaySeat() + " DoudizhuEvent " + handleType);
		 */

		table.setLastPlaySeat(nextSeat);
		table.setNextSeat(table.getNextPlaySeat());

		tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(),
				handleType, time);
	}

	/**
	 * 能否胡
	 *
	 * @param maJongTable
	 * @param member
	 */
	public boolean canHu(ZTMaJongTable table, ZTMajiangRole role, Integer destPai) {
		// [36, 14, 39, 35, 34, 26, 37, 26, 13, 38, 35, 12, 37, 34]
		List<Integer> memberShouPai = new ArrayList<Integer>();
		memberShouPai.addAll(role.getPai());
		if (destPai != null) {
			memberShouPai.add(destPai);
		}
		int[][] memberPai = CDMahJongRule.conversionType(memberShouPai);
		if (!CDMahJongRule.isQueYiMen(memberPai, role.getQueType())) {
			return false;
		}

		boolean huPai = CDMahJongRule.fitHu(memberPai, role.getShowPai());

 		LogUtil.info(memberShouPai + "data:"
       				+ role.getRole().getRole().getNick() + "canHu" + memberShouPai);
		return huPai;
	}
	
	

	/**
	 * 能否碰
	 *
	 * @param maJongTable
	 * @param member
	 */
	public boolean canPeng(ZTMaJongTable table, ZTMajiangRole member) {
		int lastPai = table.getLastPai();		
		List<Integer> memberShouPai = member.getPai();
		int[][] memberPai = CDMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		if (yetNum >= 2) {
			return true;
		}
		return false;
	}

	/**
	 * 能否暗杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public int canDarkGang(ZTMaJongTable table, ZTMajiangRole member) {
		List<Integer> memberShouPai = member.getPai();
		int[][] memberPai = CDMahJongRule.conversionType(memberShouPai);
		for (int i = 0; i < 3; i++) {
			for (int j = 1; j < 10; j++) {
				int pai = 10 * (i + 1) + j;
				int count = memberPai[i][j];
				if (count > 3) {
					return (i + 1) * 10 + j;
				}
			}
		}
		return 0;
	}

	/**
	 * 能否杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public boolean canGang(ZTMaJongTable table, ZTMajiangRole member) {

		int lastPai = table.getLastPai();
		List<Integer> memberShouPai = member.getPai();
		int[][] memberPai = CDMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		if (yetNum >= 3) {
			return true;
		}
		return false;
	}

	/**
	 * 能否飞碰
	 *
	 * @param maJongTable
	 * @param member
	 */
	public boolean canFreePeng(ZTMaJongTable table, ZTMajiangRole member) {
		return false;
	}

	/**
	 * 能否飞杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public boolean canFreeGang(ZTMaJongTable table, ZTMajiangRole member) {
		return false;
	}

	/**
	 * 能否飞补杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public boolean canExtraFreeGang(ZTMaJongTable table, ZTMajiangRole member) {
		return false;
	}

	/**
	 * 能否补杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public Integer canExtraGang(ZTMaJongTable table, ZTMajiangRole member) {
		List<Integer> memberShouPai = member.getPai();
		int[][] memberPai = CDMahJongRule.conversionType(memberShouPai);
		for (Map.Entry<Integer, List<Integer>> entry : member.getShowPai().entrySet()) {
			List<Integer> data = entry.getValue();
			if (data.size() == 3) {
				int yetNum = memberPai[entry.getKey() / 10 - 1][entry.getKey() % 10];
				if (yetNum >= 1) {
					return entry.getKey();
				}
			}
		}
		return 0;
	}

	/**
	 * 处理碰
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealPeng(ZTMaJongTable table, ZTMajiangRole role) {
		if(checkOrder(table, role, OptionsType.PENG)){
			
		}
		table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();
		table.setLastRealSeat(seat);
		
		Integer lastPai = table.getLastPai();
		List<Integer> memberShouPai = role.getPai();
		int[][] memberPai = CDMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		if (yetNum >= 2) {
			table.getYetOptions().put(role.getRole().getSeat(),
					OptionsType.PENG);
		} else {
			LogUtil.error("处理碰 请求错误 条件检查不通过");
		}

		ZTMajiangRole lastRole = table.getMembers().get(
				table.getLastOutPai() - 1);
		lastRole.getRecyclePai().remove(lastPai);

		List<Integer> rest = new ArrayList<Integer>();
		rest.add(lastPai);
		rest.add(lastPai);
		rest.add(lastPai);

		role.getShowPai().put(lastPai, rest);
		role.getPai().remove(lastPai);
		role.getPai().remove(lastPai);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addAllPai(rest);
		GMsg_12042008.Builder builder = GMsg_12042008.newBuilder();
		builder.setNextSeat(seat);
		builder.setOption(OptionsType.PENG);
		builder.setTargetSeat(table.getLastOutPai());
		builder.setOperatePai(info);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		List<OptionsType> list = new ArrayList<OptionsType>();
		list.add(OptionsType.DISCARD_TILE);

		table.getCanOptions().put(seat, list);

		// 重置操作座位
		table.setLastPlaySeat(seat);
		// 直接出牌的流程
		GMsg_12042006.Builder chuBuilder = GMsg_12042006.newBuilder();

		// 读取麻将配置的操作时间
		int waitTime = table.getTurnDuration();//等待第1轮时间
		int overTime = table.getTurnDuration();//等待第2轮时间
		if (role.getTimeOutNum() < table.getOtpPunishment()) {
			overTime += table.getTurn2Duration();
		}
		chuBuilder.setOverTime(TimeUtil.time() +overTime);
		// 读取麻将配置的操作时间

		chuBuilder.setAction(OptionsType.DISCARD_TILE);
		chuBuilder.setCurrentSeat(seat);
		chuBuilder.setWaitTime(TimeUtil.time() + waitTime);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				chuBuilder.build());

		GMsg_12042007.Builder builder4 = GMsg_12042007.newBuilder();
		builder4.addAllOption(list);
		roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
				builder4.build());
		LogUtil.info(table.getGame().getRoomId() + " seat " + seat + "碰 "
				+ lastPai);

		if (role.getRole().isAuto()) {
			tableToWait(table, seat, table.getNextPlaySeat(),
					HandleType.MAJIANG_OUT_PAI,
					System.currentTimeMillis() + 1000,System.currentTimeMillis()
					+ (overTime+1) * 1000);
		} else {
			tableToWait(table, seat, table.getNextPlaySeat(),
					HandleType.MAJIANG_OUT_PAI, System.currentTimeMillis()
							+ (overTime+1)*1000);
		}
		
		// 取消其他人的弃牌/弃胡
		for (ZTMajiangRole role2 : table.getMembers()) {
			role2.setLastQiPai(0);
			role2.setHuFan(0);
		}
	}

	public boolean checkOrder(ZTMaJongTable table, ZTMajiangRole role,
			OptionsType type) {
		int seat = role.getRole().getSeat();

		role.setOptionsType(type);
		table.getWaiter().add(seat);
		
		if (table.getCanOptions().size() >= 2) {
			List<Integer> huSeat = new ArrayList<Integer>();
			List<Integer> otherSeat = new ArrayList<Integer>();
			for (Map.Entry<Integer, List<GBaseMahJong.OptionsType>> entry : table
					.getCanOptions().entrySet()) {
				if (entry.getValue().contains(OptionsType.ANNOUNCE_WIN)) {
					huSeat.add(entry.getKey());
				}else{
					otherSeat.add(entry.getKey());
				}
			}
			if (huSeat.size() == 0) {
				for(Integer otherSeatIndex : otherSeat){
					ZTMajiangRole target = table.getMembers().get(otherSeatIndex - 1);
					target.setLastQiPai(table.getLastPai()); // 设置弃牌
				}
				CDMahJongRule.getNextPlaySeat(table.getGame().getRoles().size(), table.getWinners(), table.getLastOutPai());
			} else if (huSeat.size() == 1) {

			} else {

			}
		}
		if (type == OptionsType.ANNOUNCE_WIN) {
			

			return true;
		}
		if(table.getWaiter().size()>=table.getCanOptions().size()){
			return true;
		}
		return false;
	}

	public boolean manyHu(ZTMaJongTable table, int seat) {
		List<Integer> orderList = new ArrayList<Integer>();
		boolean flag = false;
		int huNum = 0;
		for (Map.Entry<Integer, List<GBaseMahJong.OptionsType>> entry : table
				.getCanOptions().entrySet()) {
			if (entry.getValue().contains(OptionsType.ANNOUNCE_WIN)) {
				huNum++;
				orderList.add(entry.getKey());
			}
		}
		if (orderList.size() > 0 && orderList.contains(seat)) {
			if (table.getWaiter().containsAll(orderList)) {

			}
			for (Integer order : orderList) {
				dealHu(table, table.getMembers().get(order - 1));
			}
			table.setLastPlaySeat(table.getLastOutPai());
			// 一炮多响是
			ZTMajiangRole target = table.getMembers().get(
					table.getLastOutPai() - 1);
			if (target.getRole().isAuto()) {
				tableToWait(table, table.getLastPlaySeat(),
						table.getNextPlaySeat(), HandleType.MAJIANG_GET_PAI,
						System.currentTimeMillis() + 4000);
			} else {
				tableToWait(table, table.getLastPlaySeat(),
						table.getNextPlaySeat(), HandleType.MAJIANG_GET_PAI,
						System.currentTimeMillis() + 10000);
			}
		}
		return false;
	}

	/**
	 * 处理杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealGang(ZTMaJongTable table, ZTMajiangRole role) {
		table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();
		Integer lastPai = table.getLastPai();
		List<Integer> memberShouPai = role.getPai();
		int[][] memberPai = CDMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		if (yetNum >= 3) {
			table.getYetOptions().put(role.getRole().getSeat(),
					OptionsType.EXPOSED_GANG);
		} else {
			LogUtil.error("处理杠 请求错误 条件检查不通过");
		}

		ZTMajiangRole lastRole = table.getMembers().get(
				table.getLastOutPai() - 1);
		lastRole.getRecyclePai().remove(lastPai);

		List<Integer> rest = new ArrayList<Integer>();
		rest.add(lastPai);
		rest.add(lastPai);
		rest.add(lastPai);
		rest.add(lastPai);

		role.getShowPai().put(lastPai, rest);
		role.getPai().remove(lastPai);
		role.getPai().remove(lastPai);
		role.getPai().remove(lastPai);

		// 取消其他人的弃牌/弃胡
		for (ZTMajiangRole role2 : table.getMembers()) {
			role2.setLastQiPai(0);
			role2.setHuFan(0);
		}
		
		//Integer pai = table.getPais().remove(table.getPais().size() - 1);
		//概率摸牌
		Integer pai=moPai(table, lastRole);
		//概率摸牌
		
		table.setMoPai(seat);
		table.setLastMoPai(pai);
		role.getPai().add(pai);
		
		table.setLastRealSeat(seat);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addAllPai(rest);
		info.addPai(pai);

		GMsg_12042008.Builder builder = GMsg_12042008.newBuilder();
		builder.setNextSeat(seat);
		builder.setOption(OptionsType.EXPOSED_GANG);
		builder.setTargetSeat(table.getLastOutPai());
		builder.setOperatePai(info);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		// 重置操作座位
		table.setLastPlaySeat(seat);

		LogUtil.info(table.getGame().getRoomId() + " seat " + seat + "杠 "
				+ lastPai);

		checkSelfOption(table, role);

		/*** Mission-16* 麻将牌型任务上报:杠 ***********************************/
		if (!role.getRole().isRobot()) {
			long rid = role.getRole().getRole().getRid();

			// 任务检测
			missionFunction.checkTaskFinish(rid, TaskType.daily_task, MissionType.GANG);
			LogUtil.debug(rid + " > gang .........");
		}
		/* 玩家牌型任务上报 *************************************************** */

		// 直接出牌的流程
		/*
		 * List<OptionsType> list = new ArrayList<OptionsType>();
		 * list.add(OptionsType.DISCARD_TILE); table.getCanOptions().put(seat,
		 * list);
		 * 
		 * GMsg_12011006.Builder chuBuilder = GMsg_12011006.newBuilder();
		 * chuBuilder.setAction(OptionsType.DISCARD_TILE);
		 * chuBuilder.setCurrentSeat(seat);
		 * chuBuilder.setWaitTime(TimeUtil.time() + 12);
		 * roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
		 * chuBuilder.build());
		 * 
		 * GMsg_12011007.Builder builder4 = GMsg_12011007.newBuilder();
		 * builder4.addAllOption(list);
		 * roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
		 * builder4.build());
		 * 
		 * 
		 * if (role.getRole().isAuto()) { tableToWait(table, seat,
		 * table.getNextPlaySeat(), HandleType.MAJIANG_OUT_PAI,
		 * System.currentTimeMillis() + 1000); } else { tableToWait(table, seat,
		 * table.getNextPlaySeat(), HandleType.MAJIANG_OUT_PAI,
		 * System.currentTimeMillis() + 12000); }
		 */

	}

	/**
	 * 处理飞碰
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealFreePeng(ZTMaJongTable table, ZTMajiangRole role) {
		table.setQueueWaitType(0);
	}

	/**
	 * 处理飞杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealFreeGang(ZTMaJongTable table, ZTMajiangRole role) {
		table.setQueueWaitType(0);
	}

	/**
	 * 处理胡
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealHu(ZTMaJongTable table, ZTMajiangRole role) {
		table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();
		
		
		Integer destPai = null;

		if (table.getLastRealSeat()== seat) {
			role.setHuType(-1);// 自摸		
		} else {
			destPai = table.getLastPai();
			role.setHuType(destPai);
		}
		table.setLastRealSeat(seat);
		
		// 手牌
		List<Integer> memberShouPai = new ArrayList<Integer>();
		memberShouPai.addAll(role.getPai());
		if (destPai != null) {
			memberShouPai.add(destPai);
			ZTMajiangRole lastRole = table.getMembers().get(
					table.getLastOutPai() - 1);
			lastRole.getRecyclePai().remove(destPai);
		}

		// 摆牌
		List<Integer> memberShowPai = new ArrayList<Integer>();
		Map<Integer, List<Integer>> showPai = role.getShowPai();
		for (Map.Entry<Integer, List<Integer>> entry : showPai.entrySet()) {
			List<Integer> listPai = entry.getValue();
			for (int j = 0; j < listPai.size(); j++) {
				memberShowPai.add(listPai.get(j));
			}
		}
		// 全牌
		List<Integer> allPai = new ArrayList<Integer>();
		allPai.addAll(memberShouPai);// 手牌
		allPai.addAll(memberShowPai);// 摆牌

		LogUtil.info(seat + "------胡牌-----" + memberShouPai);

		List<Integer> listTypeId = dealFanType(role, table, memberShouPai,
				memberShowPai, allPai, showPai);// 番的类型
		dealWin(role, table, listTypeId);// 处理赢,流水id设置

		// 重置操作座位 为胡家
		table.setLastPlaySeat(seat);
		// processAction(table, action, nextSeat, handleType, time);
		table.setLastPlaySeat(table.getNextPlaySeat());
		table.setNextSeat(table.getNextPlaySeat());

		table.getWinners().add(role.getRole().getSeat());

		table.getYetOptions().put(role.getRole().getSeat(),
				OptionsType.ANNOUNCE_WIN);

		// table.getBills().put(role.getRole().getSeat(), majiangBill);

		/*
		 * if(destPai!=null){ info.addPai(destPai); }
		 */
		if (role.getHuType() == -1) {
			Integer obj = table.getLastMoPai();
			role.getPai().remove(obj);
			role.getPai().add(0, obj);
		} else {
			Integer obj = table.getLastPai();
			role.getPai().add(0, obj);
		}
		LogUtil.info("role.getPai()" + role.getPai());

		// 胡牌后不亮出所有牌（牌桌上的玩家都不可以看），剩下的继续血战。亮出来的只是玩家胡的那一张牌
		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addPai(role.getPai().get(0));

		GMsg_12042008.Builder builder = GMsg_12042008.newBuilder();
		builder.setNextSeat(seat);
		builder.setOption(OptionsType.ANNOUNCE_WIN);

		if (role.getHuType() == -1) {
			builder.setTargetSeat(seat);
		} else {
			ZTMajiangRole lastRole = table.getMembers().get(
					table.getLastOutPai() - 1);
			builder.setTargetSeat(lastRole.getRole().getSeat());
		}

		builder.setOperatePai(info);
		//胡的时候返回胡的类型
		GDetailBillsInfo.Builder detailBillsInfo = GDetailBillsInfo.newBuilder();
		List<Integer> winTypeList = new ArrayList<Integer>();
		for (Integer integer : listTypeId) {
			winTypeList.add(integer);
		}
		detailBillsInfo.setNick(role.getRole().getRole().getNick());
		detailBillsInfo.addAllWinTypes(winTypeList);
		detailBillsInfo.setGoldDetail(0);
		detailBillsInfo.setWinTimes(0);
		builder.setHuStyle(detailBillsInfo);
			
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		/*
		 * List<OptionsType> list = new ArrayList<OptionsType>();
		 * list.add(OptionsType.DISCARD_TILE);
		 * table.getCanOptions().put(table.getLastPlaySeat(), list);
		 */

		/*
		 * // 胡家下家出牌的流程 GMsg_12011006.Builder chuBuilder =
		 * GMsg_12011006.newBuilder();
		 * chuBuilder.setAction(OptionsType.DISCARD_TILE);
		 * chuBuilder.setCurrentSeat(table.getLastPlaySeat());
		 * chuBuilder.setWaitTime(TimeUtil.time() + 10);
		 * roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
		 * chuBuilder.build());
		 * 
		 * GMsg_12011007.Builder builder4 = GMsg_12011007.newBuilder();
		 * builder4.addAllOption(list);
		 * roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
		 * builder4.build());
		 */

		LogUtil.info(table.getGame().getRoomId() + " seat " + seat + "胡 "
				+ role.getPai());

		role.getRole().setStatus(PlayerState.PS_WATCH_VALUE);

		if (table.getPais().size() > 0) {
			if (table.getWinners().size() >= table.getMembers().size() - 1) {
				endTable(table);
			} else {
				tableToWait(table, table.getLastPlaySeat(),
						table.getNextPlaySeat(), HandleType.MAJIANG_GET_PAI,
						System.currentTimeMillis() + 1500);
			}
		} else {
			endTable(table);
		}

		table.getCanOptions().clear();
		table.getWaiter().clear();
	}
	
	/**
	 * 处理多人胡
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealManyHu(ZTMaJongTable table, ZTMajiangRole... roleList) {
		table.setQueueWaitType(0);
		for(ZTMajiangRole role:roleList){
			int seat = role.getRole().getSeat();
			table.setLastRealSeat(seat);
			
			Integer destPai = table.getLastPai();

			// 手牌
			List<Integer> memberShouPai = new ArrayList<Integer>();
			memberShouPai.addAll(role.getPai());


			memberShouPai.add(destPai);
			ZTMajiangRole lastRole = table.getMembers().get(
					table.getLastOutPai() - 1);
			lastRole.getRecyclePai().remove(destPai);
			
			// 摆牌(要判断坨子)
			List<Integer> memberShowPai = new ArrayList<Integer>();
			Map<Integer, List<Integer>> showPai = role.getShowPai();
			for (Map.Entry<Integer, List<Integer>> entry : showPai.entrySet()) {
				List<Integer> listPai = entry.getValue();
				for (int j = 0; j < listPai.size(); j++) {
					memberShowPai.add(listPai.get(j));
				}
			}
			// 全牌
			List<Integer> allPai = new ArrayList<Integer>();
			allPai.addAll(memberShouPai);// 手牌
			allPai.addAll(memberShowPai);// 摆牌

			LogUtil.info(seat + "------胡牌-----" + memberShouPai);

			// 李培光添加******************
			List<Integer> listTypeId = dealFanType(role, table, memberShouPai,
					memberShowPai, allPai, showPai);// 番的类型
			dealWin(role, table, listTypeId);// 处理赢,流水id设置
			// 李培光添加******************

			// 重置操作座位 为胡家
			table.setLastPlaySeat(seat);
			// processAction(table, action, nextSeat, handleType, time);
			table.setLastPlaySeat(table.getNextPlaySeat());
			table.setNextSeat(table.getNextPlaySeat());

			table.getWinners().add(role.getRole().getSeat());

			table.getYetOptions().put(role.getRole().getSeat(),
					OptionsType.ANNOUNCE_WIN);

			// table.getBills().put(role.getRole().getSeat(), majiangBill);

			// 通知胡
			GPaiInfo.Builder info = GPaiInfo.newBuilder();
			/*
			 * if(destPai!=null){ info.addPai(destPai); }
			 */
			if (role.getHuType() == -1) {
				Integer obj = table.getLastMoPai();
				memberShouPai.remove(obj);
				memberShouPai.add(obj);
				role.getPai().remove(obj);
				role.getPai().add(0, obj);
			} else {
				Integer obj = table.getLastPai();
				memberShouPai.remove(obj);
				memberShouPai.add(obj);
				role.getPai().add(0, obj);
			}
			LogUtil.info("role.getPai()" + role.getPai());
			// role.getPai().remove(obj);
			// role.getPai().add(0,table.getLastMoPai());

			info.addAllPai(role.getPai());

			GMsg_12042008.Builder builder = GMsg_12042008.newBuilder();
			builder.setNextSeat(seat);
			builder.setOption(OptionsType.ANNOUNCE_WIN);

			builder.setTargetSeat(lastRole.getRole().getSeat());

			builder.setOperatePai(info);
			//胡的时候返回胡的类型
			GDetailBillsInfo.Builder detailBillsInfo = GDetailBillsInfo.newBuilder();
			List<Integer> winTypeList = new ArrayList<Integer>();
			for (Integer integer : listTypeId) {
				winTypeList.add(integer);
			}
			detailBillsInfo.setNick(role.getRole().getRole().getNick());
			detailBillsInfo.addAllWinTypes(winTypeList);
			detailBillsInfo.setGoldDetail(0);
			detailBillsInfo.setWinTimes(0);
			builder.setHuStyle(detailBillsInfo);
			
			
			roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
					builder.build());

			/*
			 * List<OptionsType> list = new ArrayList<OptionsType>();
			 * list.add(OptionsType.DISCARD_TILE);
			 * table.getCanOptions().put(table.getLastPlaySeat(), list);
			 */

			/*
			 * // 胡家下家出牌的流程 GMsg_12011006.Builder chuBuilder =
			 * GMsg_12011006.newBuilder();
			 * chuBuilder.setAction(OptionsType.DISCARD_TILE);
			 * chuBuilder.setCurrentSeat(table.getLastPlaySeat());
			 * chuBuilder.setWaitTime(TimeUtil.time() + 10);
			 * roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
			 * chuBuilder.build());
			 * 
			 * GMsg_12011007.Builder builder4 = GMsg_12011007.newBuilder();
			 * builder4.addAllOption(list);
			 * roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
			 * builder4.build());
			 */

			LogUtil.info(table.getGame().getRoomId() + " seat " + seat + "胡 "
					+ role.getPai());

			role.getRole().setStatus(PlayerState.PS_WATCH_VALUE);

			if (table.getPais().size() > 0) {
				if (table.getWinners().size() >= table.getMembers().size() - 1) {
					endTable(table);
				} else {
					tableToWait(table, table.getLastPlaySeat(),
							table.getNextPlaySeat(), HandleType.MAJIANG_GET_PAI,
							System.currentTimeMillis() + 1500);
				}
			} else {
				endTable(table);
			}

			table.getCanOptions().clear();
			table.getWaiter().clear();
		}
		
	}

	public List<Integer> dealFanType2(ZTMajiangRole role, ZTMaJongTable table) {

		int seat = role.getRole().getSeat();
		Integer destPai = null;

		if (table.getLastRealSeat() == seat) {
			role.setHuType(-1);// 自摸
		} else {
			destPai = table.getLastPai();
		}

		// 手牌
		List<Integer> memberShouPai = new ArrayList<Integer>();
		memberShouPai.addAll(role.getPai());
		if (destPai != null) {
			memberShouPai.add(destPai);
		}

		// 摆牌
		List<Integer> memberShowPai = new ArrayList<Integer>();
		Map<Integer, List<Integer>> showPai = role.getShowPai();
		for (Map.Entry<Integer, List<Integer>> entry : showPai.entrySet()) {
			List<Integer> listPai = entry.getValue();
			for (int j = 0; j < listPai.size(); j++) {
				memberShowPai.add(listPai.get(j));
			}
		}
		// 全牌
		List<Integer> allPai = new ArrayList<Integer>();
		allPai.addAll(memberShouPai);// 手牌
		allPai.addAll(memberShowPai);// 摆牌
	
		role.setHuType(0);
		return dealFanType(role, table, memberShouPai, memberShowPai, allPai, showPai);
	}
	
	/**
	 * 处理胡的类型
	 * 
	 * @param role
	 * @param table
	 * @param memberShouPai手牌
	 * @param memberShowPai摆牌癞子作为普通牌设进去
	 *            ,经过转换)
	 * @param allPai所有牌
	 * @param showPai摆拍
	 *            （用户摆出的牌直接set进去）
	 * @return
	 */
	public List<Integer> dealFanType(ZTMajiangRole role, ZTMaJongTable table,
			List<Integer> memberShouPai, List<Integer> memberShowPai,
			List<Integer> allPai, Map<Integer, List<Integer>> showPai) {
		// 打印
//		for (Integer integer : allPai) {
//			System.out.print(" all" + integer);
//		}
//		System.out.println("****");
//		for (Integer integer : memberShouPai) {
//			System.out.print(" ShouPai" + integer);
//		}
//		System.out.println("****");
//		for (Integer integer : memberShowPai) {
//			System.out.print(" ShowPai" + integer);
//		}
//		System.out.println("****");
		// 一副手牌
		List<Integer> shouPai = new ArrayList<Integer>();
		shouPai.addAll(memberShouPai);

		// 胡的类型（大类）
		List<Integer> listHuTypeId = new ArrayList<Integer>();// 胡大类的集合

		if (memberShouPai.size() == 14) {
			
			// 门清
			listHuTypeId.add(ChengDuWinType.CD_MEN_QING_VALUE);
			
			if (CDMahJongRule.judegeSevenPairs(shouPai)) {// 7对(手牌一定要14张)
				listHuTypeId.add(ChengDuWinType.CD_QIAO_QI_DUI_VALUE);
			}
			if (CDMahJongRule.judegeLongSevenPairs(shouPai)) {// 龙7对(手牌一定要14张)
				listHuTypeId.add(ChengDuWinType.CD_LONG_QI_DUI_VALUE);
			}
		}
		if (CDMahJongRule.judgeFourTriple(memberShouPai)) {// 大对子
			listHuTypeId.add(ChengDuWinType.CD_DA_DUI_ZI_VALUE);
		}
			
		if (CDMahJongRule.judgeFlush(allPai)) {// 清一色
			listHuTypeId.add(ChengDuWinType.CD_QING_YI_SE_VALUE);
		}
		if (table.getPais().size() == 54 && role.getHuType() == -1)// 天胡
		{
			listHuTypeId.add(ChengDuWinType.CD_TIAN_HU_VALUE);
		}
		if (table.getPais().size() == 54 && role.getHuType() != -1)// 地胡
		{
			listHuTypeId.add(ChengDuWinType.CD_DI_HU_VALUE);
		}
		
		int show_gang_num = CDMahJongRule.judegeWithGang(showPai);
		if (show_gang_num == 1) // 一杠
		{
			listHuTypeId.add(ChengDuWinType.CD_1_GNAG_VALUE);
		} else if (show_gang_num == 2) {
			listHuTypeId.add(ChengDuWinType.CD_2_GANG_VALUE);
		} else if (show_gang_num == 3) {
			listHuTypeId.add(ChengDuWinType.CD_3_GANG_VALUE);
		}
		
		if (listHuTypeId.contains(ChengDuWinType.CD_QIAO_QI_DUI_VALUE)
			&& listHuTypeId.contains(ChengDuWinType.CD_QING_YI_SE_VALUE))// 清小队
		{
			listHuTypeId.add(ChengDuWinType.CD_QING_XIAO_DUI_VALUE);
		}
		if (listHuTypeId.contains(ChengDuWinType.CD_DA_DUI_ZI_VALUE) 
			&& listHuTypeId.contains(ChengDuWinType.CD_QING_YI_SE_VALUE))// 清大对子
		{
			listHuTypeId.add(ChengDuWinType.CD_QING_DA_DUI_ZI_VALUE);
		}
		// 胡小类（判断全部牌）
		listHuTypeId.add(ChengDuWinType.CD_PING_HU_VALUE);
		
		// 杠上花
		if ((table.getYetOptions().get(role.getRole().getSeat()) == OptionsType.EXPOSED_GANG)) {// 明杠
			listHuTypeId.add(ChengDuWinType.CD_GANG_SHANG_HUA_VALUE);
		}
		if ((table.getYetOptions().get(role.getRole().getSeat()) == OptionsType.EXTRA_GANG)) {
			listHuTypeId.add(ChengDuWinType.CD_GANG_SHANG_HUA_VALUE);
		}
		if ((table.getYetOptions().get(role.getRole().getSeat()) == OptionsType.DARK_GANG)) {
			listHuTypeId.add(ChengDuWinType.CD_GANG_SHANG_HUA_VALUE);
		}
		
		// 杠上炮
		if ((table.getYetOptions().get(table.getLastOutPai()) == OptionsType.EXPOSED_GANG)
				&& role.getHuType() != -1) {
			listHuTypeId.add(ChengDuWinType.CD_GANG_SHANG_PAO_VALUE);
		}
		if ((table.getYetOptions().get(table.getLastOutPai()) == OptionsType.EXTRA_GANG)
				&& role.getHuType() != -1) {
			listHuTypeId.add(ChengDuWinType.CD_GANG_SHANG_PAO_VALUE);
		}
		if ((table.getYetOptions().get(table.getLastOutPai()) == OptionsType.DARK_GANG)
				&& role.getHuType() != -1) {
			listHuTypeId.add(ChengDuWinType.CD_GANG_SHANG_PAO_VALUE);
		}

		// 抢杠
		if (table.getYetOptions().get(table.getLastPlaySeat()) == OptionsType.EXTRA_GANG) {// 当前角色明杠
			ZTMajiangRole ZTMajiangRole = table.getMembers().get(
					table.getMoPai() - 1);
			Map<Integer, List<Integer>> pai = ZTMajiangRole.getShowPai();
			for (Map.Entry<Integer, List<Integer>> entry : pai.entrySet()) {
				if (entry.getValue().size() == 4) {// 四个值为杠
					if (role.getHuType() == entry.getKey()) {// 胡的牌是杠的牌
						listHuTypeId.add(ChengDuWinType.CD_QIANG_GANG_VALUE);
						break;
					}
				}
			}
		}
		// 只剩下最后一张牌, 别人放炮胡-全求大对
		if (role.getPai().size() == 2 && role.getHuType() != -1) {
			listHuTypeId.add(ChengDuWinType.CD_QUAN_QIU_DA_DUI_VALUE);
		}
		// 自摸
		if (role.getHuType() == -1 && listHuTypeId.contains(ChengDuWinType.CD_QIAO_QI_DUI_VALUE)) {
			listHuTypeId.add(ChengDuWinType.CD_QIAO_QI_DUI_ZIMO_VALUE);
		}
//		System.out.println("listHuTypeId" + listHuTypeId);
		return listHuTypeId;
	}

	/**
	 * 处理番的数目
	 * 
	 * @param listHuTypeId
	 *            （存的是胡的类型）
	 * @return
	 */
	public int dealFanNum(List<Integer> listHuTypeId, ZTMaJongTable table) {
		HashMap<Integer, Integer> fanMap = new HashMap<Integer, Integer>();// 大番map
		List<Integer> add = new ArrayList<Integer>();// 小番list
		int addfan = 0;// 小番的番数
		// 去除大番的记录
		for (Integer integer : listHuTypeId) {
			MaJiangChengDuValueCsv maJiangValueCsv = maJiangValueCache
					.getConfig(integer);
			if (maJiangValueCsv.getHuAdd() == 0) {
				fanMap.put(integer, maJiangValueCsv.getHuTimes());
			} else {
				add.add(integer);
				addfan += maJiangValueCsv.getHuTimes();
			}
		}
		int huBigTypeId = 0;
		int maxfan = 0;
		for (Map.Entry<Integer, Integer> entry : fanMap.entrySet()) {
			if (entry.getValue() > maxfan) {
				huBigTypeId = entry.getKey();
				maxfan = entry.getValue();
			}
		}
		listHuTypeId.clear();// 清除数据
		listHuTypeId.add(huBigTypeId);// 设置大番（只能显示一个）
		listHuTypeId.addAll(add);// 设置小番（显示多个）

//		System.out.println(listHuTypeId);		
//		System.out.println("实际番数 ：" + (maxfan + addfan));
		int maxFanShu = table.getMaxFanShu();
		return (maxfan + addfan) > maxFanShu ? maxFanShu : maxfan + addfan;
	}

	/**
	 * 摸牌后检测自摸杠
	 * 
	 * @param role
	 * @param table
	 */
	public void checkSelfOption(ZTMaJongTable table, ZTMajiangRole role) {
		int seat = role.getRole().getSeat();

		table.getCanOptions().clear();
		table.getWaiter().clear();

		List<OptionsType> list = new ArrayList<OptionsType>();
		list.add(OptionsType.DISCARD_TILE);
		if (table.getPais().size() > 0) {
			if (canDarkGang(table, role) > 0) {
				list.add(OptionsType.DARK_GANG);
				LogUtil.info(table.getGame().getRoomId() + " DARK_GANG "
						+ role.getRole().getRole().getNick() + "按杠 "
						+ table.getLastMoPai());
			}
			if (canExtraGang(table, role) > 0) {
				list.add(OptionsType.EXTRA_GANG);
				LogUtil.info(table.getGame().getRoomId() + " EXTRA_GANG "
						+ role.getRole().getRole().getNick() + "补杠 "
						+ table.getLastMoPai());
			}
			if (canExtraFreeGang(table, role)) {
				list.add(OptionsType.EXTRA_FREE_GANG);
				LogUtil.info(table.getGame().getRoomId() + " EXTRA_FREE_GANG "
						+ role.getRole().getRole().getNick() + "补飞杠 "
						+ table.getLastMoPai());
			}
		}
		

		if (canHu(table, role, null)) {
			list.add(OptionsType.ANNOUNCE_WIN);
			LogUtil.info(table.getGame().getRoomId() + " ANNOUNCE_WIN "
					+ role.getRole().getRole().getNick() + "摸胡 "
					+ table.getLastMoPai());
		}

		table.getCanOptions().put(seat, list);

		GMsg_12042006.Builder builder3 = GMsg_12042006.newBuilder();
		// 读取麻将配置的操作时间
		int waitTime = table.getTurnDuration();//等待第1轮时间
		int overTime = table.getTurnDuration();//等待第2轮时间
		if (role.getTimeOutNum() < table.getOtpPunishment()) {
			overTime += table.getTurn2Duration();
		}

		// 读取麻将配置的操作时间

		if (list.contains(OptionsType.ANNOUNCE_WIN)) {
			if (role.getRole().isRobot()) {
				int interval = MathUtil.randomNumber(1000, 4000);

				tableToWait(table, seat, table.getNextPlaySeat(),
						HandleType.MAJIANG_HU, System.currentTimeMillis()
								+ interval);
			} else {
				if (role.getRole().isAuto()) {
					tableToWait(table, seat, table.getNextPlaySeat(),
							HandleType.MAJIANG_HU, System.currentTimeMillis()
									+ 1 * 1000);
				}else {
					tableToWait(table, seat, table.getNextPlaySeat(),
							HandleType.MAJIANG_HU, System.currentTimeMillis()
									+ (overTime+1) * 1000);
				}
			}

		} else {
			if (role.getRole().isAuto()) {
				if (role.getRole().isRobot()){
					int interval = MathUtil.randomNumber(1000, 4000);
					if (list.contains(OptionsType.DARK_GANG)) {
						table.setWaitSeat(seat);
						tableToWait(table, seat, table.getNextPlaySeat(),
								HandleType.MAJIANG_AN_GANG,
								System.currentTimeMillis() + interval);
					}else if (list.contains(OptionsType.EXTRA_GANG)) {
						table.setWaitSeat(seat);
						tableToWait(table, seat, table.getNextPlaySeat(),
								HandleType.MAJIANG_EXTRA_GANG,
								System.currentTimeMillis() + interval);
					}else{
						
						tableToWait(table, seat, table.getNextPlaySeat(),
								HandleType.MAJIANG_OUT_PAI, System.currentTimeMillis()+ interval);
					}
				}else{
					if (list.contains(OptionsType.DARK_GANG)) {
						tableToWait(table, seat, table.getNextPlaySeat(),
								HandleType.MAJIANG_AN_GANG,
								System.currentTimeMillis() + 2000,
								System.currentTimeMillis() + (overTime + 1) * 1000);
					}else{
						tableToWait(table, seat, table.getNextPlaySeat(),
								HandleType.MAJIANG_OUT_PAI, System.currentTimeMillis()
										+ 1 * 1000);
					}
				}
				
				
			} else {
				tableToWait(table, seat, table.getNextPlaySeat(),
						HandleType.MAJIANG_OUT_PAI, System.currentTimeMillis()
								+ (overTime + 1) * 1000);
			}
			// wait = 7;
		}

		builder3.setAction(OptionsType.DISCARD_TILE);
		builder3.setCurrentSeat(seat);
		builder3.setWaitTime(TimeUtil.time() + waitTime);
		builder3.setOverTime(TimeUtil.time() + overTime);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder3.build());

		long rid = role.getRole().getRole().getRid();
		GMsg_12042007.Builder builder4 = GMsg_12042007.newBuilder();
		builder4.addAllOption(list);
		roleFunction.sendMessageToPlayer(rid, builder4.build());

	}

	/**
	 * 处理赢得返回结果 如果输的人黄金不够，直接扣除到0
	 * 
	 * @param role
	 * @param table
	 */
	public void dealWin(ZTMajiangRole role, ZTMaJongTable table,
			List<Integer> listHuTypeId) {
		boolean FirstWeek = false;
		if (table.getWinners().size() == 0) {
			FirstWeek = false;
		}
		// 计算番数并且排除重复的番数
		int winTimes = dealFanNum(listHuTypeId, table);
		// 胡的类型(HuAdd为0的胡类型只能显示)
		ArrayList<Integer> winTypeList = new ArrayList<Integer>();
		for (Integer integer : listHuTypeId) {
			winTypeList.add(integer);
		}

		// 任务检测
		missionFunction.checkTaskFinish(role.getRole().getRole().getRid(),TaskType.daily_task,
				MissionType.WIN, GameType.CDMAJIANG);
		missionFunction.checkTaskFinish(role.getRole().getRole().getRid(),TaskType.daily_task,
				MissionType.CONTINUE_WIN, GameType.CDMAJIANG, true);
		missionFunction.checkTaskFinish(role.getRole().getRole().getRid(),TaskType.daily_task,
				MissionType.CARD_TYPE, GameType.CDMAJIANG, winTypeList);

		int diZhu = majiangRoomCache.getConfig(table.getGame().getRoomType())
				.getPotOdds();
		// int gold = diZhu * winTimes;// 失去黄金=底注*番数
		int gold = (int) Math.pow(2, winTimes) * diZhu;
		GMsg_12042012.Builder goldBuilder = GMsg_12042012.newBuilder();

		if (role.getHuType() == -1)// 自摸(自己加金币,其他人扣金币)
		{
			int goldActualTotal = 0;
			// 其他人扣金币,在winner里面的不扣,自己不扣
			List<ZTMajiangRole> ZTMajiangRoles = table.getMembers();
			for (ZTMajiangRole ZTMajiangRole : ZTMajiangRoles) {
				Role role2 = ZTMajiangRole.getRole().getRole();
				int seat = ZTMajiangRole.getRole().getSeat();
				if (!table.getWinners().contains(seat) && seat != role.getRole().getSeat()) {
					// 判定用户的金币数量是否满足
					int goldActualOne = gold;
					if (role2.getGold() < gold) {
						goldActualOne = role2.getGold();
					}
					goldActualTotal += goldActualOne;
					
					if(ZTMajiangRole.getRole().isRobot()){
						role2.setGold(role2.getGold() - goldActualOne);
					}else{
						// 扣除金币
						roleFunction.goldSub(role2, goldActualOne,
								MoneyEvent.CDMAJIANG, true);
					}

					GMajiangGold.Builder mgold = GMajiangGold.newBuilder();
					mgold.setSeat(seat);
					mgold.setGold(-goldActualOne);
					goldBuilder.addChangeGold(mgold);

					// 输家的流水清单列表
					GBillsInfo.Builder billsInfo = null;
					if (table.getBills().get(seat) != null) {
						billsInfo = table.getBills().get(seat);
					} else {
						billsInfo = GBillsInfo.newBuilder();
					}
					billsInfo.setRid(role2.getRid());
					billsInfo.setSeat(seat);
					billsInfo.setGold(billsInfo.getGold() - goldActualOne);
					// 输家具体的流水统计
					GDetailBillsInfo.Builder detailBillsInfo = GDetailBillsInfo
							.newBuilder();
					detailBillsInfo.setNick(role.getRole().getRole().getNick());// 输显示别人的名称
					detailBillsInfo.setWinTimes(winTimes);
					detailBillsInfo.addAllWinTypes(winTypeList);
					detailBillsInfo.setGoldDetail(-goldActualOne);
					billsInfo.addDetailBillsInfo(detailBillsInfo);
					table.getBills().put(seat, billsInfo);
					// 输家的麻将数据数据库记录
					majiangDataFunction.updateMajiangData(role2.getRid(),
							winTimes, goldActualTotal, listHuTypeId, false,
							false);
					if(ZTMajiangRole.getRole().isRobot()){
						//设置机器人场数信息
						ZTMajiangRole.getRole().setRobotLost();
					}
				}
			}
			// 赢家加金币
			Role role1 = role.getRole().getRole();
			ExpCsv csv = expCache.getConfig(role1.getLevel() + 1);
			if(role.getRole().isRobot()){
				role1.setGold(role1.getGold() + goldActualTotal);
			}else{
				roleFunction.goldAdd(role1, goldActualTotal, MoneyEvent.CDMAJIANG,
						false);
			}
			roleFunction.expAdd(role1, csv.getMajiangWinExp(), true);
			// 飘字
			GMajiangGold.Builder mgold = GMajiangGold.newBuilder();
			mgold.setSeat(role.getRole().getSeat());
			mgold.setGold(goldActualTotal);
			goldBuilder.addChangeGold(mgold);

			// 赢家清单
			GBillsInfo.Builder billsInfo = null;
			if (table.getBills().get(role.getRole().getSeat()) != null) {
				billsInfo = table.getBills().get(role.getRole().getSeat());
			} else {
				billsInfo = GBillsInfo.newBuilder();
			}
			billsInfo.setSeat(role.getRole().getSeat());
			billsInfo.setNick(role1.getNick());
			billsInfo.setGold(billsInfo.getGold() + goldActualTotal);
			billsInfo.setRid(role1.getRid());
			table.getBills().put(role.getRole().getSeat(), billsInfo);
			// 赢家细节清单
			GDetailBillsInfo.Builder detailBillsInfo = GDetailBillsInfo
					.newBuilder();
			detailBillsInfo.setNick(role.getRole().getRole().getNick());
			detailBillsInfo.setWinTimes(winTimes);
			detailBillsInfo.addAllWinTypes(winTypeList);
			detailBillsInfo.setGoldDetail(goldActualTotal);
			billsInfo.addDetailBillsInfo(detailBillsInfo);
			table.getBills().put(role.getRole().getSeat(), billsInfo);
			// 赢家的麻将数据数据库记录
			majiangDataFunction.updateMajiangData(role1.getRid(), winTimes,
					goldActualTotal, listHuTypeId, FirstWeek, true);
			if(role.getRole().isRobot()){
				//设置机器人场数信息
				role.getRole().setRobotWin();
			}

		} else// (被胡扣金币)
		{
			int goldActualTotal = gold;
			// 输家扣金币
			List<ZTMajiangRole> members = table.getMembers();
			ZTMajiangRole role2 = members.get(table.getLastOutPai() - 1);
			if (role2.getRole().getRole().getGold() < goldActualTotal) {
				goldActualTotal = role2.getRole().getRole().getGold();// 检测金币不足状态
			}
			int seat = role2.getRole().getSeat();
			
			if(role2.getRole().isRobot()){
				role2.getRole().getRole().setGold(role2.getRole().getRole().getGold() - goldActualTotal);
			}else{
				roleFunction.goldSub(role2.getRole().getRole(), goldActualTotal,
						MoneyEvent.CDMAJIANG, true);
			}

			// 飘字
			GMajiangGold.Builder mgold = GMajiangGold.newBuilder();
			mgold.setSeat(role2.getRole().getSeat());
			mgold.setGold(-goldActualTotal);
			goldBuilder.addChangeGold(mgold);

			// 输家的流水清单列表
			GBillsInfo.Builder billsInfo = null;
			if (table.getBills().get(seat) != null) {
				billsInfo = table.getBills().get(seat);
			} else {
				billsInfo = GBillsInfo.newBuilder();
			}
			billsInfo.setNick(role2.getRole().getRole().getNick());
			billsInfo.setRid(role2.getRole().getRole().getRid());
			billsInfo.setSeat(seat);
			billsInfo.setGold(billsInfo.getGold() - goldActualTotal);
			// 输家的流水细节清单
			GDetailBillsInfo.Builder detailBillsInfo2 = GDetailBillsInfo
					.newBuilder();
			detailBillsInfo2.setNick(role.getRole().getRole().getNick());
			detailBillsInfo2.setWinTimes(winTimes);
			detailBillsInfo2.addAllWinTypes(winTypeList);
			detailBillsInfo2.setGoldDetail(-goldActualTotal);
			billsInfo.addDetailBillsInfo(detailBillsInfo2);
			table.getBills().put(role2.getRole().getSeat(), billsInfo);
			// 输家的麻将数据数据库记录
			majiangDataFunction.updateMajiangData(role2.getRole().getRole()
					.getRid(), winTimes, goldActualTotal, listHuTypeId, false,
					false);
			if(role2.getRole().isRobot()){
				//设置机器人场数信息
				role2.getRole().setRobotLost();
			}

			// 赢家加金币
			Role role1 = role.getRole().getRole();
			ExpCsv csv = expCache.getConfig(role1.getLevel() + 1);
			
			if(role.getRole().isRobot()){
				role1.setGold(role1.getGold() + goldActualTotal);
			}else{
				roleFunction.goldAdd(role1, goldActualTotal, MoneyEvent.CDMAJIANG,
						true);
			}
			roleFunction.expAdd(role1, csv.getMajiangWinExp(), true);

			// 飘字
			mgold = GMajiangGold.newBuilder();
			mgold.setSeat(role.getRole().getSeat());
			mgold.setGold(goldActualTotal);
			goldBuilder.addChangeGold(mgold);

			// 赢家的流水清单
			GBillsInfo.Builder billsInfo2 = null;
			if (table.getBills().get(role.getRole().getSeat()) != null) {
				billsInfo2 = table.getBills().get(role.getRole().getSeat());
			} else {
				billsInfo2 = GBillsInfo.newBuilder();
			}
			billsInfo2.setNick(role.getRole().getRole().getNick());
			billsInfo2.setSeat(role.getRole().getSeat());
			billsInfo2.setGold(billsInfo2.getGold() + goldActualTotal);
			billsInfo2.setRid(role1.getRid());
			table.getBills().put(role.getRole().getSeat(), billsInfo2);
			// 赢家流水清单
			GDetailBillsInfo.Builder detailBillsInfo = GDetailBillsInfo
					.newBuilder();
			detailBillsInfo.setNick(role.getRole().getRole().getNick());
			detailBillsInfo.setWinTimes(winTimes);
			detailBillsInfo.addAllWinTypes(winTypeList);
			detailBillsInfo.setGoldDetail(goldActualTotal);
			billsInfo2.addDetailBillsInfo(detailBillsInfo);
			table.getBills().put(role.getRole().getSeat(), billsInfo2);
			// 赢家的麻将数据数据库记录
			majiangDataFunction.updateMajiangData(role1.getRid(), winTimes,
					goldActualTotal, listHuTypeId, FirstWeek, true);
			if(role.getRole().isRobot()){
				role.getRole().setRobotWin();
			}

		}
		
		//检测胡了玩家金币情况
		for (int i = 0; i < goldBuilder.getChangeGoldList().size(); i++) {
			GMajiangGold gMajiangGold = goldBuilder.getChangeGoldList().get(i);
			ZTMajiangRole roel = table.getMembers().get(gMajiangGold.getSeat() - 1);
			LogUtil.error(roel.getRole().getRole().getNick() + "金币：" + gMajiangGold.getGold() + "当前玩家gold为：" + 
					table.getBills().get(gMajiangGold.getSeat()).getGold());
		}
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				goldBuilder.build());
	}
	
	/**
	 * 处理显示定缺
	 * @param table
	 */
	public void dealShowDingQue(ZTMaJongTable table)
	{
		table.setQueueWaitType(0);
		
		for (int i = 0; i < table.getMembers().size(); i++) {
			ZTMajiangRole maJongMember = table.getMembers().get(i);
			if (maJongMember.getQueType() == 0) {
				GMsg_12042007.Builder waitBuilder = GMsg_12042007.newBuilder();
				waitBuilder.addOption(OptionsType.SET_QUE_TYPE);
				roleFunction.sendMessageToPlayer(maJongMember.getRole().getRole().getRid(), waitBuilder.build());;
			}
		}

		GMsg_12042006.Builder chuBuilder = GMsg_12042006.newBuilder();
		int waitTime = table.getTurnDuration();//等待第1轮时间
		chuBuilder.setAction(OptionsType.SET_QUE_TYPE);
		chuBuilder.setCurrentSeat(0); // 所有人
		chuBuilder.setWaitTime(TimeUtil.time() + waitTime);
		chuBuilder.setOverTime(TimeUtil.time() + waitTime);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(), chuBuilder.build());
		// 针对所有人的, 座位传0
		tableToWait(table, 0, 0, HandleType.MAJIANG_WAIT_DING_QUE_TYPE, 
				System.currentTimeMillis() + (waitTime+1) * 1000);
	}
	
	/**
	 * 设置 成员定缺
	 * @param table
	 */
	public void setMemberQueType(ZTMaJongTable table)
	{
		table.setQueueWaitType(0);
		
		for (int i = 0; i < table.getMembers().size(); i++) {
			ZTMajiangRole maJongMember = table.getMembers().get(i);
			if (maJongMember.getQueType() == 0) {
				int que_type_index = CDMahJongRule.choseQueType(CDMahJongRule.conversionType(maJongMember.getPai()));
				maJongMember.setQueType(que_type_index + 1);
				
				GMsg_12042020.Builder builder = GMsg_12042020.newBuilder();
				builder.setSeat(maJongMember.getRole().getSeat());
				builder.setQuePaiType(que_type_index + 1);
				roleFunction.sendMessageToPlayers(table.getGame().getRoles(), builder.build());
			}
		}
		
		LogUtil.info(table.getGame().getRoomId() + " 开始打麻将 等待5秒");
		tableToWait(table, table.getOwner(), table.getNextPlaySeat(),
				HandleType.MAJIANG_GET_PAI, System.currentTimeMillis() + 5000);
	}
	
	/**
	 * 处理 设置缺一门类型
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void setQueType(ZTMaJongTable table, ZTMajiangRole member, Integer que_type) {
		member.setQueType(que_type);
	
		GMsg_12042020.Builder builder = GMsg_12042020.newBuilder();
		builder.setSeat(member.getRole().getSeat());
		builder.setQuePaiType(que_type);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(), builder.build());
		
		// 检测是否所有人都设置了类型
		for (int i = 0; i < table.getMembers().size(); i++) {
			ZTMajiangRole maJongMember = table.getMembers().get(i);
			if (maJongMember.getQueType() == 0) {
				return;
			}
		}
		
		// 所有人都设置了，准备开始游戏
		table.setQueueWaitType(0);
		LogUtil.info(table.getGame().getRoomId() + " 开始打麻将 等待4秒");
		tableToWait(table, table.getOwner(), table.getNextPlaySeat(),
				HandleType.MAJIANG_GET_PAI, System.currentTimeMillis() + 4000);
	}
	/**
	 * 处理过
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealPass(ZTMaJongTable table, ZTMajiangRole member) {
		if (table.getQueueWaitType() == HandleType.MAJIANG_OUT_PAI) {

		} else if (table.getQueueWaitType() == HandleType.MAJIANG_WAIT) {
			if (table.getCanOptions().size() == 1) {
				table.setQueueWaitType(0);
				tableToWait(table, table.getLastPlaySeat(),
						table.getNextPlaySeat(), HandleType.MAJIANG_WAIT,
						System.currentTimeMillis() + 10);
			}else{
				if(checkOrder(table, member, OptionsType.PASS)){
					table.setQueueWaitType(0);
					tableToWait(table, table.getLastPlaySeat(),
							table.getNextPlaySeat(), HandleType.MAJIANG_WAIT,
							System.currentTimeMillis() + 10);
				}
			}
		} else if (table.getQueueWaitType() == HandleType.MAJIANG_HU) {
			List<GBaseMahJong.OptionsType> list = table.getCanOptions().get(
					member.getRole().getSeat());
			if (list != null && list.contains(OptionsType.ANNOUNCE_WIN)) {
				table.setQueueWaitType(0);
				if (table.getLastRealSeat() == member.getRole().getSeat()) {
					// 自摸取消
					tableToWait(table, table.getMoPai(),
							table.getNextPlaySeat(),
							HandleType.MAJIANG_OUT_PAI,
							System.currentTimeMillis() + 6000);
				} else {
					table.setLastPlaySeat(table.getLastOutPai());
					// table.setLastPlaySeat(table.getNextPlaySeat());
					tableToWait(table, table.getLastPlaySeat(),
							table.getNextPlaySeat(), HandleType.MAJIANG_WAIT,
							System.currentTimeMillis() + 50);
				}

			}
		}

	}

	/**
	 * 处理暗杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealAnGang(ZTMaJongTable table, ZTMajiangRole role,
			Integer lastPai) {
		table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();

		List<Integer> memberShouPai = role.getPai();
		int[][] memberPai = CDMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		if (yetNum == 4) {
			table.getYetOptions().put(role.getRole().getSeat(),
					OptionsType.EXPOSED_GANG);
		} else {
			LogUtil.error("处理杠 请求错误 条件检查不通过");
		}
		
		// 四张一样牌的暗杠
		List<Integer> rest = new ArrayList<Integer>();
		rest.add(lastPai);
		rest.add(lastPai);
		rest.add(lastPai);
		rest.add(lastPai);

		role.getShowPai().put(lastPai, rest);
		role.getPai().removeAll(rest);

		// 取消所有人的弃牌/弃胡
		for (ZTMajiangRole role2 : table.getMembers()) {
			role2.setLastQiPai(0);
			role2.setHuFan(-1);
		}
		
		//Integer pai = table.getPais().remove(table.getPais().size() - 1);		
		//概率摸牌
		Integer pai = moPai(table, role);
		//概率摸牌
		
		role.getPai().add(pai);
		table.setMoPai(seat);
		table.setLastMoPai(pai);
		
		table.setLastRealSeat(seat);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addAllPai(rest);
		info.addPai(pai);

		GMsg_12042008.Builder builder = GMsg_12042008.newBuilder();
		builder.setNextSeat(seat);
		builder.setOption(OptionsType.DARK_GANG);
		builder.setTargetSeat(seat);
		builder.setOperatePai(info);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		LogUtil.info(table.getGame().getRoomId() + " seat " + seat + "杠 "
				+ lastPai);

		// 重置操作座位
		table.setLastPlaySeat(seat);
		
		checkSelfOption(table, role);
		
		/*** Mission-16* 麻将牌型任务上报:杠 ***********************************/
		if (!role.getRole().isRobot()) {
			long rid = role.getRole().getRole().getRid();

			// 任务检测
			missionFunction.checkTaskFinish(rid, TaskType.daily_task, MissionType.GANG);
			LogUtil.debug(rid + " > gang .........");
		}

		/*
		 * List<OptionsType> list = new ArrayList<OptionsType>();
		 * list.add(OptionsType.DISCARD_TILE); table.getCanOptions().put(seat,
		 * list); // 直接出牌的流程 GMsg_12011006.Builder chuBuilder =
		 * GMsg_12011006.newBuilder();
		 * chuBuilder.setAction(OptionsType.DISCARD_TILE);
		 * chuBuilder.setCurrentSeat(seat);
		 * chuBuilder.setWaitTime(TimeUtil.time() + 12);
		 * roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
		 * chuBuilder.build());
		 * 
		 * GMsg_12011007.Builder builder4 = GMsg_12011007.newBuilder();
		 * builder4.addAllOption(list);
		 * roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
		 * builder4.build());
		 * 
		 * 
		 * if (role.getRole().isAuto()) { tableToWait(table, seat,
		 * table.getNextPlaySeat(), HandleType.MAJIANG_OUT_PAI,
		 * System.currentTimeMillis() + 3000); } else { tableToWait(table, seat,
		 * table.getNextPlaySeat(), HandleType.MAJIANG_OUT_PAI,
		 * System.currentTimeMillis() + 10000); }
		 */
	}

	/**
	 * 处理补杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealExtraGang(ZTMaJongTable table, ZTMajiangRole role,
			Integer lastPai) {
		table.setQueueWaitType(0);
		int seat = role.getRole().getSeat();
		table.setLastRealSeat(seat);
		List<Integer> memberShouPai = role.getPai();
		int[][] memberPai = CDMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		if (yetNum >= 1) {
			table.getYetOptions().put(role.getRole().getSeat(),
					OptionsType.EXTRA_GANG);
		} else {
			LogUtil.error("处理杠 请求错误 条件检查不通过");
		}
		List<Integer> data = role.getShowPai().get(lastPai);

		List<Integer> rest = new ArrayList<Integer>();
		rest.addAll(data);
		rest.add(lastPai);

		role.getShowPai().put(lastPai, rest);
		role.getPai().remove(lastPai);

		
		// 取消其他人的弃牌/弃胡
		for (ZTMajiangRole role2 : table.getMembers()) {
			role2.setLastQiPai(0);
			role2.setHuFan(0);
		}
		
		//Integer pai = table.getPais().remove(table.getPais().size() - 1);
		//概率摸牌
		Integer pai=moPai(table, role);
		//概率摸牌
		
		role.getPai().add(pai);
		table.setLastMoPai(pai);

		GPaiInfo.Builder info = GPaiInfo.newBuilder();
		info.addAllPai(rest);
		info.addPai(pai);

		GMsg_12042008.Builder builder = GMsg_12042008.newBuilder();
		builder.setNextSeat(seat);
		builder.setOption(OptionsType.EXTRA_GANG);
		builder.setTargetSeat(seat);
		builder.setOperatePai(info);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());
		
		
		LogUtil.info(table.getGame().getRoomId() + " seat " + seat + "补杠 "
				+ lastPai);

		// 重置操作座位
		table.setLastPlaySeat(seat);
		checkSelfOption(table, role);

		/*** Mission-16* 麻将牌型任务上报:杠 ***********************************/
		if (!role.getRole().isRobot()) {
			long rid = role.getRole().getRole().getRid();

			// 任务检测
			missionFunction.checkTaskFinish(rid, TaskType.daily_task, MissionType.GANG);
			LogUtil.debug(rid + " > gang .........");
		}

		/*List<OptionsType> list = new ArrayList<OptionsType>();
		list.add(OptionsType.DISCARD_TILE);
		table.getCanOptions().put(seat, list);

		// 重置操作座位
		table.setLastPlaySeat(seat);
		// 直接出牌的流程
		GMsg_12011006.Builder chuBuilder = GMsg_12011006.newBuilder();

		// 读取麻将配置的操作时间
		int waitTime = table.getTurnDuration();//等待第1轮时间
		int overTime = table.getTurnDuration();//等待第2轮时间
		if (role.getTimeOutNum() < table.getOtpPunishment()) {
			overTime += table.getTurn2Duration();			
		}
		
		chuBuilder.setOverTime(TimeUtil.time()+overTime);
		// 读取麻将配置的操作时间

		chuBuilder.setAction(OptionsType.DISCARD_TILE);
		chuBuilder.setCurrentSeat(seat);
		chuBuilder.setWaitTime(TimeUtil.time() + waitTime);
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				chuBuilder.build());

		GMsg_12011007.Builder builder4 = GMsg_12011007.newBuilder();
		builder4.addAllOption(list);
		roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
				builder4.build());
		

		if (role.getRole().isAuto()) {
			tableToWait(table, seat, table.getNextPlaySeat(),
					HandleType.MAJIANG_OUT_PAI,
					System.currentTimeMillis() + 3000,System.currentTimeMillis()
					+ (overTime+1) * 1000);
		} else {
			tableToWait(table, seat, table.getNextPlaySeat(),
					HandleType.MAJIANG_OUT_PAI, System.currentTimeMillis()
							+ (overTime+1)*1000);
		}*/
	}

	/**
	 * 处理飞补杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public void dealExtraFreeGang(ZTMaJongTable table, ZTMajiangRole role, Integer lastPai) {
		table.setQueueWaitType(0);
	}

	static {
		// 低级房统计初始化,中级房统计初始化,高级房统计初始化
		for (int i = 1; i <= 3; i++) {
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
	 * 摸完牌之后   检查出哪张牌能听牌   以及听的牌的剩余数量   和胡的番数
	 * @author xueshangyu
	 * @param table
	 */
	public void checkTingPai(ZTMaJongTable table, ZTMajiangRole role) {
		// 手牌
		List<Integer> memberShouPai = new ArrayList<Integer>();
		memberShouPai.addAll(role.getPai());

		// 摆牌
		List<Integer> memberShowPai = new ArrayList<Integer>();
		Map<Integer, List<Integer>> showPai = role.getShowPai();
		for (Map.Entry<Integer, List<Integer>> entry : showPai.entrySet()) {
			List<Integer> listPai = entry.getValue();
			for (int j = 0; j < listPai.size(); j++) {
				memberShowPai.add(listPai.get(j));
			}
		}
		
		// 全牌
		List<Integer> allPai = new ArrayList<Integer>();

		allPai.addAll(memberShowPai);// 摆牌
		//拿到玩家摸完牌后的手牌
		List<Integer> pais = new ArrayList<Integer>();
		pais.addAll(role.getPai());
		List<Integer> paistemp = new ArrayList<Integer>() ;//遍历 打每一张手牌
		//去掉手牌重复的数 
		for (Integer pai : role.getPai()) {
			if (!paistemp.contains(pai)) {
				paistemp.add(pai);
			}
		}
		List<Integer> ting_pai = new ArrayList<Integer>();
		if (!role.getRole().isRobot()) {
			GMsg_12042013.Builder builder = GMsg_12042013.newBuilder();
			if(null != pais && pais.size() > 0 ){
				//遍历 出手牌上的每一张  判断能不能听牌  能听牌继续
				for (int i = 0; i < paistemp.size(); i++) {
					Integer temp = paistemp.get(i);
					pais.remove(temp);
					int [][] shuZuallPai = CDMahJongRule.conversionType(pais);//玩家手牌

					if (CDMahJongRule.isQueYiMen(shuZuallPai, role.getQueType())) {	
						ting_pai = CDMahJongRule.tingPai(shuZuallPai,role.getShowPai());
						if (null != ting_pai && ting_pai.size() > 0) {
							for (int j = 0; j < ting_pai.size(); j++) {
								Integer pai_num = 0;
								Integer temp2 = ting_pai.get(j);
								pai_num = getCardNum(table, role, temp2);
								pais.add(temp2);
								allPai.addAll(pais);
								List<Integer> huFanTypeId = dealFanType(role, table, pais , memberShouPai, allPai, showPai);
								int fan_shu = dealFanNum(huFanTypeId, table);
								allPai.removeAll(pais);
								GTingPaiInfo.Builder tingPaiInfo = GTingPaiInfo.newBuilder();
								tingPaiInfo.setPai(temp);//将要出的牌
								tingPaiInfo.setPaiNum(pai_num); //听的牌剩余数
								tingPaiInfo.setFanShu(fan_shu); //胡的番数
								tingPaiInfo.setTingPai(temp2);//听的牌
								builder.addTingPaiInfo(tingPaiInfo);
								pais.remove(temp2);
							}
						}
						
					}
					pais.add(temp);
				}
				roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(), builder.build());
			}
		}
		
	}
	/**
	 * 听牌的时候  返回听牌以及其剩余数量  和   胡的番数
	 * @author xueshangyu
	 * @param table
	 * @param role
	 */
	public void tingPai(ZTMaJongTable table, ZTMajiangRole role){

		// 手牌
		List<Integer> memberShouPai = new ArrayList<Integer>();
		memberShouPai.addAll(role.getPai());

		// 摆牌(要判断坨子)
		List<Integer> memberShowPai = new ArrayList<Integer>();
		Map<Integer, List<Integer>> showPai = role.getShowPai();
		for (Map.Entry<Integer, List<Integer>> entry : showPai.entrySet()) {
			List<Integer> listPai = entry.getValue();
			for (int j = 0; j < listPai.size(); j++) {
				memberShowPai.add(listPai.get(j));
			}
		}
		// 全牌
		List<Integer> allPai = new ArrayList<Integer>();
		allPai.addAll(memberShouPai);// 手牌
		allPai.addAll(memberShowPai);// 摆牌
		//拿到玩家的手牌
		List<Integer> pais = new ArrayList<Integer>();
		pais.addAll(role.getPai());
		List<Integer> ting_pai = new ArrayList<Integer>();
		int [][] shuZuallPai = CDMahJongRule.conversionType(pais);

		ting_pai = CDMahJongRule.tingPai(shuZuallPai, showPai);

		GMsg_12042013.Builder builder = GMsg_12042013.newBuilder();
		if (null != ting_pai && ting_pai.size() > 0) {
			if (null != ting_pai
					&& ting_pai.size() > 0
					&& CDMahJongRule.isQueYiMen(shuZuallPai, role.getQueType())) {
				for (int j = 0; j < ting_pai.size(); j++) {
					Integer pai_num = 0;
					Integer temp2 = ting_pai.get(j);
					pai_num = getCardNum(table, role, temp2);
					pais.add(temp2);
					List<Integer> huFanTypeId = dealFanType(role, table, pais,
							memberShouPai, allPai, showPai);
					int fan_shu = dealFanNum(huFanTypeId, table);
					GTingPaiInfo.Builder tingPaiInfo = GTingPaiInfo
							.newBuilder();
					tingPaiInfo.setPaiNum(pai_num); // 听的牌剩余数
					tingPaiInfo.setFanShu(fan_shu); // 胡的番数
					tingPaiInfo.setTingPai(temp2);// 听的牌
					builder.addTingPaiInfo(tingPaiInfo);
					pais.remove(temp2);
				}
			}

			roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
					builder.build());
		}

	}
	
	// 玩家可见牌的剩余数量
	private int getCardNum(ZTMaJongTable table, ZTMajiangRole role, Integer pai) {
		int card_num = 0;
		List<Integer> allCard = new ArrayList<Integer>();
		// allCard.addAll(table.get);
		for (ZTMajiangRole member : table.getMembers()) {
			if (role.getRole().getSeat() != member.getRole().getSeat()) {
				if (table.getWinners().contains(member.getRole().getSeat())) {
					allCard.addAll(member.getPai());
					LogUtil.info(pai+ " 0 allCard"+allCard);
					allCard.addAll(member.getRecyclePai());
					//LogUtil.info("薛 弃牌堆 ："+member.getRecyclePai());
					for (Map.Entry<Integer, List<Integer>> entry : member
							.getShowPai().entrySet()) {
						allCard.addAll(entry.getValue());
					}
				}else{
					allCard.addAll(member.getRecyclePai());
					for (Map.Entry<Integer, List<Integer>> entry : member
							.getShowPai().entrySet()) {
						allCard.addAll(entry.getValue());
					}
				}

				LogUtil.info("1 allCard"+allCard);
			} else {
				allCard.addAll(member.getPai());
				allCard.addAll(member.getRecyclePai());
				LogUtil.info("2 allCard"+allCard);
				for (Map.Entry<Integer, List<Integer>> entry : member
						.getShowPai().entrySet()) {
					allCard.addAll(entry.getValue());
					LogUtil.info("3 allCard"+allCard);
				}
			}
		}
		int[][] allPai = CDMahJongRule.conversionType(allCard);
	//	LogUtil.info("牌的二维数组");
//		for (int i = 0; i < allPai.length; i++) {
//			for (int j = 0; j < allPai[i].length; j++) {
//				System.out.print(allPai[i][j]);
//			}
//			System.out.println();
//		}
		card_num = 4 - allPai[pai / 10 - 1][pai % 10];
	//	LogUtil.info("薛 "+pai +"的数量："+allPai[pai / 10 - 1][pai % 10]);
		LogUtil.info("card_num"+card_num);
//		if(card_num<0){
//			card_num = 0;
//		}
	//	LogUtil.info("薛 剩余牌"+card_num);
		return card_num;
		
	}
	

	// 得到玩家想要的测试牌
	public static Integer getCeShiPai(int t) {
		List<Integer> list = new ArrayList<Integer>();
		list.add(21);
		list.add(21);
		list.add(21);
		list.add(21);
		list.add(24);
		list.add(24);
		list.add(24);
		list.add(24);
		list.add(29);
		list.add(29);
		list.add(29);
		list.add(29);
		list.add(38);
		return list.get(t);
	}
}
