/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.model.struct;

import io.netty.util.internal.chmv8.ConcurrentHashMapV8;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.yaowan.constant.GameStatus;
import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.model.struct.Game;
import com.yaowan.protobuf.game.GDouDiZhu;
import com.yaowan.protobuf.game.GDouDiZhu.GDouDiZhuPai;
import com.yaowan.protobuf.game.GDouDiZhu.GDouDiZhuPlayer;
import com.yaowan.protobuf.game.GDouDiZhu.GDouDiZhuTable;

/**
 * 昭通麻斗地主牌局
 *
 * @author zane
 *
 */
public class ZTDoudizhuTable implements ISingleData {

	public ZTDoudizhuTable() {
	}

	public ZTDoudizhuTable(Game game) {
		this();
		this.game = game;
		this.owner = 0;
	}
	
	
	public boolean isInited() {
		return inited;
	}

	public void setInited(boolean inited) {
		this.inited = inited;
	}

	public void reset() {
//		inited = false;
		this.game.setStatus(GameStatus.WAIT_READY);
		this.owner = 0;
		this.startSeat = 1;
		this.pais.clear();
		this.lastPai.clear();
		this.lastPlaySeat = 0;
		this.nextSeat = 0;
		this.zhuaType = 0;
		this.noZhuaCount = 0;
		this.queueWaitType = 0;
		this.coolDownTime = -1;
		this.lastOutPai = 0;
		this.daoCount = 0;
		this.lastDao = 0;
		this.passCount = 0;
		this.waitAction = null;
		this.recyclePai = new ArrayList<Integer>();
		this.game.setStartTime(System.currentTimeMillis());
		this.game.setEndTime(0);
		for(ZTDoudizhuRole role:members){
			if(role != null) {
				role.reset();
			}
		}
	}

	// 牌的类型
	// 座位号从1开始按照逆时针递增, 1是东的方向
	private Game game;

	private int owner;// 庄家
	
	private int xiajia; //下家农民

	private int shangjia; //上家农民

	private int startSeat = 1;// 
	// 牌局玩家
	private List<ZTDoudizhuRole> members = new ArrayList<>(4);
	// 玩家可进行的操作
	private Map<Integer, List<GDouDiZhu.DouDiZhuAction>> canOptions = new ConcurrentHashMapV8<>();
	// 本轮牌局玩家已经进行的操作
	private Map<Integer, GDouDiZhu.DouDiZhuAction> yetOptions = new ConcurrentHashMapV8<>();
	// 牌信息(剩余的牌)
	private List<Integer> pais = new LinkedList<>();
	
	// 弃牌堆(倒计时结束则丢弃这张牌)
	private List<Integer>  recyclePai=new ArrayList<Integer>();
	
	// 最后出得牌
	private List<Integer> lastPai = new LinkedList<>();
	
	private volatile boolean inited = false;
	/**
	 * 旁观状态
	 */
	private List<Integer> watchers = new ArrayList<Integer>();//旁观
	
	/**
	 * 不抓
	 */
	private List<Integer> zhuaNo = new ArrayList<Integer>();
	
	
	private int lastPlaySeat;// 当前出牌玩家座位
	
	private int nextSeat;//下个出牌玩家座位
	
	private int zhuaType;//0- 1闷抓  2-抓
	
	private int noZhuaCount;//
	// 队列等待类型
	private int queueWaitType = 0;//
	// 出牌倒计时
	private long coolDownTime = -1;
	
	// 出牌非托管倒计时
	private long targetTime = -1;

	private int lastOutPai;// 最后打牌玩家座位
	
	// 倒次数
	private int daoCount;
	
	// 最后倒的人
	private int lastDao;
	
	// pass次数
    private int passCount;
    
    // 等待的操作
    private GDouDiZhu.DouDiZhuAction waitAction;
	
	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
		if(owner == 1){
			xiajia = 2;
			shangjia = 3;
		}else if(owner ==2){
			xiajia =3;
			shangjia= 1;
		}else {//=3
			xiajia =1;
			shangjia = 2;
		}
	}
	

	public int getXiajia() {
		return xiajia;
	}

	public int getShangjia() {
		return shangjia;
	}

	public List<ZTDoudizhuRole> getMembers() {
		return members;
	}

	public void setMembers(List<ZTDoudizhuRole> members) {
		this.members = members;
	}

	public List<Integer> getPais() {
		return pais;
	}

	public void setPais(List<Integer> pais) {
		this.pais = pais;
	}



	public int getLastPlaySeat() {
		return lastPlaySeat;
	}

	public void setLastPlaySeat(int lastPlaySeat) {
		this.lastPlaySeat = lastPlaySeat;
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

	public Map<Integer, List<GDouDiZhu.DouDiZhuAction>> getCanOptions() {
		return canOptions;
	}

	public void setCanOptions(Map<Integer, List<GDouDiZhu.DouDiZhuAction>> canOptions) {
		this.canOptions = canOptions;
	}

	public Map<Integer, GDouDiZhu.DouDiZhuAction> getYetOptions() {
		return yetOptions;
	}

	public void setYetOptions(Map<Integer, GDouDiZhu.DouDiZhuAction> yetOptions) {
		this.yetOptions = yetOptions;
	}


	/**
	 * 获取正常出牌情况下 对应的出牌座位
	 *
	 * @return
	 */
	public int getNextPlaySeat() {
		if (lastPlaySeat == members.size()) {
			return 1;
		} else {
			return lastPlaySeat + 1;
		}
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

	public int getStartSeat() {
		return startSeat;
	}

	public void setStartSeat(int startSeat) {
		this.startSeat = startSeat;
	}

	public int getNextSeat() {
		return nextSeat;
	}

	public void setNextSeat(int nextSeat) {
		this.nextSeat = nextSeat;
	}

	public List<Integer> getLastPai() {
		return lastPai;
	}

	public void setLastPai(List<Integer> lastPai) {
		this.lastPai = lastPai;
	}

	public int getZhuaType() {
		return zhuaType;
	}

	public void setZhuaType(int zhuaType) {
		this.zhuaType = zhuaType;
	}
	
	public GDouDiZhuTable.Builder serialize() {
		GDouDiZhuTable.Builder tbuilder = GDouDiZhuTable.newBuilder();
		tbuilder.setDiZhuSeat(owner);
		tbuilder.setLastSeat(lastPlaySeat);
		if (zhuaType == 1) {
			GDouDiZhuPai.Builder restbuilder = GDouDiZhuPai.newBuilder();
			restbuilder.addAllPai(getPais());
			tbuilder.setRestPai(restbuilder);
		} else if (zhuaType == 2) {
			GDouDiZhuPai.Builder restbuilder = GDouDiZhuPai.newBuilder();
			restbuilder.addAllPai(getPais());
			tbuilder.setRestPai(restbuilder);
		}
		
		GDouDiZhuPai.Builder paibuilder = GDouDiZhuPai.newBuilder();
		paibuilder.addAllPai(lastPai);
		tbuilder.setOutPai(paibuilder);
		for (ZTDoudizhuRole doudizhuRole : getMembers()) {
			if(doudizhuRole != null ) {
				GDouDiZhuPlayer.Builder pbuilder = GDouDiZhuPlayer.newBuilder();
				pbuilder.setCurrentPower(doudizhuRole.getCurrentPower());
				pbuilder.setLookedPai(doudizhuRole.getLookedPai());
				pbuilder.setPaiCount(doudizhuRole.getPai().size());
				pbuilder.setSeat(doudizhuRole.getRole().getSeat());
				tbuilder.addPlayer(pbuilder);
			}
		}
		return tbuilder;
	}

	public int getNoZhuaCount() {
		return noZhuaCount;
	}

	public void setNoZhuaCount(int noZhuaCount) {
		this.noZhuaCount = noZhuaCount;
	}

	public int getLastOutPai() {
		return lastOutPai;
	}

	public void setLastOutPai(int lastOutPai) {
		this.lastOutPai = lastOutPai;
	}

	public int getDaoCount() {
		return daoCount;
	}

	public void setDaoCount(int daoCount) {
		this.daoCount = daoCount;
	}

	public int getPassCount() {
		return passCount;
	}

	public void setPassCount(int passCount) {
		this.passCount = passCount;
	}

	public GDouDiZhu.DouDiZhuAction getWaitAction() {
		return waitAction;
	}

	public void setWaitAction(GDouDiZhu.DouDiZhuAction waitAction) {
		this.waitAction = waitAction;
	}

	public List<Integer> getWatchers() {
		return watchers;
	}

	public void setWatchers(List<Integer> watchers) {
		this.watchers = watchers;
	}

	public List<Integer> getZhuaNo() {
		return zhuaNo;
	}

	public void setZhuaNo(List<Integer> zhuaNo) {
		this.zhuaNo = zhuaNo;
	}

	public int getLastDao() {
		return lastDao;
	}

	public void setLastDao(int lastDao) {
		this.lastDao = lastDao;
	}

	public long getTargetTime() {
		return targetTime;
	}

	public void setTargetTime(long targetTime) {
		this.targetTime = targetTime;
	}

	public List<Integer> getRecyclePai() {
		return recyclePai;
	}

	public void setRecyclePai(List<Integer> recyclePai) {
		this.recyclePai = recyclePai;
	}

}
