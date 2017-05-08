/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.model.struct;

import io.netty.util.internal.chmv8.ConcurrentHashMapV8;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.yaowan.constant.GameStatus;
import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.protobuf.game.GBaseMahJong;
import com.yaowan.protobuf.game.GBaseMahJong.GBillsInfo;
import com.yaowan.protobuf.game.GBaseMahJong.GMajiangGold;
import com.yaowan.protobuf.game.GBaseMahJong.OptionsType;

/**
 * 昭通麻将牌局
 *
 * @author zane
 *
 */
public class ZTMaJongTable implements ISingleData {

	
	public ZTMaJongTable(){
	}
	
	public ZTMaJongTable(Game game){
		this();
		this.game = game;
		this.owner = 1;
	}
	public void reset() {
		LogUtil.info("重置table.....");
//		inited = false;
		this.game.setStatus(GameStatus.WAIT_READY);
		this.owner = 1;

		this.pais.clear();
		this.lastPai = -1;
		this.lastPlaySeat = 0;
		this.nextSeat = 0;
		this.lastOutPai = 0;
		this.laiZiNum = 0;
		this.queueWaitType = 0;
		this.coolDownTime = -1;
		this.lastOutPai = 0;
		this.waitAction = null;
		this.game.setStartTime(System.currentTimeMillis());
		this.game.setEndTime(0);
		
		this.point1 = 0;
		this.point2 = 0;
		
		this.setMoPai(0);
		
		this.setLastRealSeat(0);
		winners.clear();
		waiter.clear();
		
		bills.clear();
		waitSeat = 0;
		moPai = 0;
		lastMoPai = 0;
		for(ZTMajiangRole role:members){
			role.reset();
		}
	}
	
	private volatile boolean inited = false;
   // 牌的类型
   // 一万-九万 11 - 19
   // 一筒-九筒 21 - 29
    // 一索-九索 31 - 39
   // 座位号从1开始按照逆时针递增, 1是东的方向
    private Game game;
  

	private int owner;//庄家
	
    private int laiZiNum = 0;//赖子牌
    //牌局玩家(从游戏开始初始化到游戏结束一直不变)
    private List<ZTMajiangRole> members = new ArrayList<>(4);
    
    //牌局玩家位置(从游戏开始初始化到游戏结束,随着玩家的离开而改变)
    private List<Integer> remainMembers = new CopyOnWriteArrayList<>();
    
    
    //玩家可进行的操作
    private Map<Integer, List<GBaseMahJong.OptionsType>> canOptions = new ConcurrentHashMapV8<>();
    //本轮牌局玩家已经进行的操作
    private Map<Integer, GBaseMahJong.OptionsType> yetOptions = new ConcurrentHashMapV8<>();
    //牌信息(剩余的牌)
    private List<Integer> pais = new LinkedList<>();
    private int point1;//色子1
    private int point2;//色子2
    private int lastPlaySeat;//当前出牌玩家座位
    private int nextSeat;//下个出牌玩家座位
    private int lastPai = -1;//上一张打出的牌
    
    private int moPai = 0;//最后摸牌的座位
    
    private int lastRealSeat = 0;//最后操作的玩家
    
    private int lastMoPai = 0;//最后摸的牌
    //队列等待类型 1
    private int queueWaitType = 0;//
    //出牌倒计时
    private long coolDownTime = -1;
    
    //非自动出牌倒计时
    private long targetTime = -1;

    private List<Integer> winners = new ArrayList<Integer>();//赢家或者被踢出去的玩家
    
    //牌局玩家
    private Map<Integer,GBillsInfo.Builder> bills = new HashMap<Integer, GBillsInfo.Builder>();
    
    private int lastOutPai;// 最后打牌玩家座位
    
    //当前可操作玩家 同时操作玩家 要判断优先级
    private List<Integer> waiter = new ArrayList<Integer>();
    
    // 等待的操作
    private GBaseMahJong.OptionsType waitAction;
    
    // 等待操作的最高优先级座位
    private int waitSeat =0;
    
    //双等待时间（第一轮最长等待时间）
    private int turnDuration=0;
    
    //双等待时间（第二轮最长等待时间）
    private int turn2Duration=0;
    
    //操作时间（如：碰，杠时间最长等待时间）
    private int actionDuration=0;
    
    //运行超时次数
    private int otpPunishment=0;
    
    private int gmMoPai;//测试时可以直接摸到想要的
    
    private long gmrid;//测试时可以直接摸到想要的
    
    private int maxFanShu;//最大番数
    
    private Map<Integer, GBaseMahJong.OptionsType> receiveQueue = new LinkedHashMap<Integer, GBaseMahJong.OptionsType>();//玩家已发命令队列
    
    private int buGangPai = 0;//记录玩家补杠时候操作的牌  用于判断其他玩家能不能胡
    
    private boolean isQiangGangHu = false; //标记抢杠胡
    
    private Map<Integer, GMajiangGold.Builder> piaoZi = new HashMap<Integer, GMajiangGold.Builder>();//玩家赢的时候 把飘字存进来  统一发送
    
    private boolean isManyOperate = false;//是否多人操作
    
    private int beiQiangGangHuSeat;//被抢杠胡的座位
    
    private int qiangGangHuPai = 0;//抢杠胡的牌
    
    private boolean isGamePause = false;//有玩家在充值
    
    private boolean isKickPlay = false;//是否能踢人
    
    //破产玩家
    private List<ZTMajiangRole> poChangRoles = new ArrayList<ZTMajiangRole>();
    
    //破产时 可以充值的玩家
    private List<ZTMajiangRole> RechargePlayer = new ArrayList<ZTMajiangRole>();
    
    
    
    public int getTurnDuration() {
		return turnDuration;
	}

	public void setTurnDuration(int turnDuration) {
		this.turnDuration = turnDuration;
	}
	
	public int getTurn2Duration() {
		return turn2Duration;
	}

	public void setTurn2Duration(int turn2Duration) {
		this.turn2Duration = turn2Duration;
	}

	public int getActionDuration() {
		return actionDuration;
	}

	public void setActionDuration(int actionDuration) {
		this.actionDuration = actionDuration;
	}

	public int getOtpPunishment() {
		return otpPunishment;
	}

	public void setOtpPunishment(int otpPunishment) {
		this.otpPunishment = otpPunishment;
	}
	
	public boolean isInited() {
		return inited;
	}

	public void setInited(boolean inited) {
		this.inited = inited;
	}

	public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public int getLaiZiNum() {
        return laiZiNum;
    }

    public void setLaiZiNum(int laiZiNum) {
        this.laiZiNum = laiZiNum;
    }
    public List<ZTMajiangRole> getMembers() {
        return members;
    }

    public void setMembers(List<ZTMajiangRole> members) {
        this.members = members;
    }

    public List<Integer> getPais() {
        return pais;
    }

    public void setPais(List<Integer> pais) {
        this.pais = pais;
    }

    public int getPoint1() {
        return point1;
    }

    public void setPoint1(int point1) {
        this.point1 = point1;
    }

    public int getPoint2() {
        return point2;
    }

    public void setPoint2(int point2) {
        this.point2 = point2;
    }

    public int getLastPlaySeat() {
        return lastPlaySeat;
    }

    public void setLastPlaySeat(int lastPlaySeat) {
        this.lastPlaySeat = lastPlaySeat;
    }

    public int getLastPai() {
        return lastPai;
    }

    public void setLastPai(int lastPai) {
        this.lastPai = lastPai;
    }

    public long getCoolDownTime() {
        return coolDownTime;
    }

    public void setCoolDownTime(long coolDownTime) {
        this.coolDownTime = coolDownTime;
    }

    public int getQueueWaitType() {
        return queueWaitType;
    }

    public void setQueueWaitType(int queueWaitType) {
        this.queueWaitType = queueWaitType;
    }

    public Map<Integer, List<GBaseMahJong.OptionsType>> getCanOptions() {
        return canOptions;
    }

    public void setCanOptions(Map<Integer, List<GBaseMahJong.OptionsType>> canOptions) {
        this.canOptions = canOptions;
    }

    public Map<Integer, GBaseMahJong.OptionsType> getYetOptions() {
        return yetOptions;
    }

    public void setYetOptions(Map<Integer, GBaseMahJong.OptionsType> yetOptions) {
        this.yetOptions = yetOptions;
    }

    /**
     * 获取正常出牌情况下 下一的出牌座位
     * 剔除胡牌
     *
     * @return
     */
	public int getNextPlaySeat() {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 1; i <= members.size(); i++) {
			if (!winners.contains(i)) {
				list.add(i);
			}
		}
//		LogUtil.info("剩余玩家列表..."+list);
		Integer seat = lastPlaySeat;
//		LogUtil.info("seat..."+seat);
		List<Integer> youXianJiSeat = getYouXianJiSeat(list, seat);
		int index = youXianJiSeat.indexOf(seat);
//		LogUtil.info("index..."+index);
		if (index == -1) {//list不包含 最后操作玩家
			index = 0;
		}
		if (index == youXianJiSeat.size() - 1) {//最后操作玩家在list最后
			return youXianJiSeat.get(0);
		} else {
			if (list.size() == 1) {//list只有一个玩家
				return youXianJiSeat.get(0);
			} else if (youXianJiSeat.size() == 0) {//list为空
				return 0;
			} else {//最后操作玩家不在list最后并且list的大小大于等于2
				return youXianJiSeat.get(index);
			}
		}

	}
    
	/**
	 * 非胡的玩家
	 * @param lastSeat
	 * @param mySeat
	 * @return
	 */
	public List<Integer> getSeatOrder(Integer lastSeat, Integer mySeat) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 1; i <= members.size(); i++) {
			if (i == lastSeat) {
				list.add(i);
			} else if (!winners.contains(i) && canOptions.containsKey(i)&&!canOptions.get(i).contains(OptionsType.ANNOUNCE_WIN)) {
				list.add(i);
			}
		}
		List<Integer> order = new ArrayList<Integer>();
		int index = list.indexOf(lastSeat);
		for(int i =index+1;i<list.size();i++){
			order.add(i);
		}
		if(index!=0){
			for(int i =0;i<index;i++){
				order.add(i);
			}
		}
		return order;
	}

	/**
	 * 返回某个玩家出牌后  剩下玩家的优先级座位list
	 */
	public List<Integer> getYouXianJiSeat(List<Integer> list, Integer lastSeat){
		List<Integer> newList = new ArrayList<Integer>();
		if (list != null && list.size() > 0) {
			for (int i = 0 ,seat = lastSeat + 1; i < 4; i++,seat ++) {
				if (seat > 4) {
					seat = seat % 4;
				}
				if (list.contains(seat)) {
					newList.add(seat);
				}
			}
		}
		return newList;
	}
    @Override
    public Number getSingleId() {
        return game.getRoomId();
    }
    
    public Game getGame() {
  		return game;
  	}

  	public void setGame(Game game) {
  		this.game = game;
  	}


	public int getNextSeat() {
		return nextSeat;
	}

	public void setNextSeat(int nextSeat) {
		this.nextSeat = nextSeat;
	}

	public List<Integer> getWinners() {
		return winners;
	}

	public void setWinners(List<Integer> winners) {
		this.winners = winners;
	}

	public Map<Integer,GBillsInfo.Builder> getBills() {
		return bills;
	}

	public void setBills(Map<Integer,GBillsInfo.Builder> bills) {
		this.bills = bills;
	}

	public int getLastOutPai() {
		return lastOutPai;
	}

	public void setLastOutPai(int lastOutPai) {
		this.lastOutPai = lastOutPai;
	}

	public List<Integer> getWaiter() {
		return waiter;
	}

	public void setWaiter(List<Integer> waiter) {
		this.waiter = waiter;
	}

	public GBaseMahJong.OptionsType getWaitAction() {
		return waitAction;
	}

	public void setWaitAction(GBaseMahJong.OptionsType waitAction) {
		this.waitAction = waitAction;
	}

	public int getMoPai() {
		return moPai;
	}

	public void setMoPai(int moPai) {
		this.moPai = moPai;
	}

	public int getWaitSeat() {
		return waitSeat;
	}

	public void setWaitSeat(int waitSeat) {
		this.waitSeat = waitSeat;
	}

	public int getLastMoPai() {
		return lastMoPai;
	}

	public void setLastMoPai(int lastMoPai) {
		this.lastMoPai = lastMoPai;
	}

	public long getTargetTime() {
		return targetTime;
	}

	public void setTargetTime(long targetTime) {
		this.targetTime = targetTime;
	}

	public int getGmMoPai() {
		return gmMoPai;
	}

	public void setGmMoPai(int gmMoPai) {
		this.gmMoPai = gmMoPai;
	}

	public long getGmrid() {
		return gmrid;
	}

	public void setGmrid(long gmrid) {
		this.gmrid = gmrid;
	}

	public int getLastRealSeat() {
		return lastRealSeat;
	}

	public void setLastRealSeat(int lastRealSeat) {
		this.lastRealSeat = lastRealSeat;
	}

	public void resetHu(){
		for(ZTMajiangRole role : this.getMembers()){
			role.setHuFan(-1);
		}
	}

	public int getMaxFanShu() {
		return maxFanShu;
	}

	public void setMaxFanShu(int maxFanShu) {
		this.maxFanShu = maxFanShu;
	}

	public int getBuGangPai() {
		return buGangPai;
	}

	public void setBuGangPai(int buGangPai) {
		this.buGangPai = buGangPai;
	}

	public boolean isQiangGangHu() {
		return isQiangGangHu;
	}

	public void setQiangGangHu(boolean isQiangGangHu) {
		this.isQiangGangHu = isQiangGangHu;
	}

	public Map<Integer, GMajiangGold.Builder> getPiaoZi() {
		return piaoZi;
	}

	public void setPiaoZi(Map<Integer, GMajiangGold.Builder> piaoZi) {
		this.piaoZi = piaoZi;
	}

	public boolean isManyOperate() {
		return isManyOperate;
	}

	public void setManyOperate(boolean isManyOperate) {
		this.isManyOperate = isManyOperate;
	}

	public Map<Integer, GBaseMahJong.OptionsType> getReceiveQueue() {
		return receiveQueue;
	}

	public void setReceiveQueue(Map<Integer, GBaseMahJong.OptionsType> receiveQueue) {
		this.receiveQueue = receiveQueue;
	}

	public int getBeiQiangGangHuSeat() {
		return beiQiangGangHuSeat;
	}

	public void setBeiQiangGangHuSeat(int beiQiangGangHuSeat) {
		this.beiQiangGangHuSeat = beiQiangGangHuSeat;
	}

	public int getQiangGangHuPai() {
		return qiangGangHuPai;
	}

	public void setQiangGangHuPai(int qiangGangHuPai) {
		this.qiangGangHuPai = qiangGangHuPai;
	}

	public List<Integer> getRemainMembers() {
		return remainMembers;
	}

	public void setRemainMembers(List<Integer> remainMembers) {
		this.remainMembers = remainMembers;
	}

	public boolean isGamePause() {
		return isGamePause;
	}

	public void setGamePause(boolean isGamePause) {
		this.isGamePause = isGamePause;
	}

	public boolean isKickPlay() {
		return isKickPlay;
	}

	public void setKickPlay(boolean isKickPlay) {
		this.isKickPlay = isKickPlay;
	}

	public List<ZTMajiangRole> getRechargePlayer() {
		return RechargePlayer;
	}

	public void setRechargePlayer(List<ZTMajiangRole> rechargePlayer) {
		RechargePlayer = rechargePlayer;
	}

	public List<ZTMajiangRole> getPoChangRoles() {
		return poChangRoles;
	}

	public void setPoChangRoles(List<ZTMajiangRole> poChangRoles) {
		this.poChangRoles = poChangRoles;
	}

}
