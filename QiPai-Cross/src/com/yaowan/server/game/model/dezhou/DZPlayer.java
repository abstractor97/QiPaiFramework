package com.yaowan.server.game.model.dezhou;

import com.yaowan.protobuf.game.DZCardProto;




/**
 * 德州游戏玩家
 * @author YW0941
 *
 */
public class DZPlayer {
	//玩家标识编号
	private long rid;
	
	//所坐的位置编号,从1开始
	private int position;
	//是否加入了游戏
	private boolean joined=false;
	
	//赢的筹码
	private int winJeton;
	
	//筹码
	private int jeton;
	
	//抓的一手牌
	private DZHandCard handCard = new DZHandCard();
	
	//单次下注筹码总量,即当有参与者首次下注时，会清理所有参与者此值
	private int betValue;
	
	//可用来处理分池的筹码量
	private int useSplitPoolBetValue;
	
	//可以参与的最大筹码池分配的索引号
	private int jetonPoolMaxIndex = 0;
	
	private DZPlayerStatus status;
	
	//最后次所进行的操作
	private DZPlayerOP op;
	//所进入的房间
	private DZRoom room;
	
	public DZPlayer(long rid,int jeton) {
		super();
		this.rid = rid;
		this.jeton = jeton;
	}
	
	public void setRoom(DZRoom room) {
		this.room = room;
	}
	public DZRoom getRoom() {
		return room;
	}

	public long getRid() {
		return rid;
	}

	public DZHandCard getHandCard() {
		return handCard;
	}

	public DZPlayerStatus getStatus() {
		return status;
	}

	public void setStatus(DZPlayerStatus status) {
		this.status = status;
	}
	public DZPlayerOP getOp() {
		return op;
	}

	public void setOp(DZPlayerOP op) {
		this.op = op;
	}
	
	public int getWinJeton() {
		return winJeton;
	}
	//增加赢得的筹码
	public void incrementWinJeton(int winJeton) {
		this.winJeton += winJeton;
		jeton += winJeton;//累加到自己的筹码上
	}

	public int getUseSplitPoolBetValue() {
		return useSplitPoolBetValue;
	}
	
	public int getJetonPoolMaxIndex() {
		return jetonPoolMaxIndex;
	}

	public void cleanUseSplitPoolBetValue(int jetonPoolIndex){
		useSplitPoolBetValue = 0;
		this.jetonPoolMaxIndex = jetonPoolIndex;
	}
	
	private void checkCanOP(){
		if(op == DZPlayerOP.GiveUp){
			throw new RuntimeException("非法操作， have giveup"); 
		}
		
		if(status != DZPlayerStatus.Joined){
			throw new RuntimeException("非法操作， not joined game"); 
		}
	}
	/**
	 * 进行重置，每当游戏开始时，会调用此函数进行重置
	 * @link DZRoom.start()
	 */
	public void reset() {
		
		handCard = new DZHandCard();
		betValue = 0;
		useSplitPoolBetValue = 0;
		jetonPoolMaxIndex = 0;
		op = DZPlayerOP.Common;
		winJeton = 0;
	}
	
	/**
	 * 进行下注
	 * @param currValue 本次下注值
	 */
	private void putBet(int currValue){
		//1.当先下注者是否为此参与者
		DZPlayer currPlayer = room.getBetPlayer();
		if( currPlayer!= this){
			throw new RuntimeException("非法处理");
		}
		
		//2.判断筹码是否足够
		if(currValue<=0){
			throw new RuntimeException("非法处理 -- 消耗筹码为负数");
		}
		if(currValue > jeton){
			throw new RuntimeException("非法处理 -- 筹码不足");
		}
		
		
		//2.判断下注值是否合理
		int value = currValue+betValue;
		if(value < room.getBetMax() && op != DZPlayerOP.AllIn){
			throw new RuntimeException("非下注值");
		}
		
		betValue = value;
		jeton -= currValue;
		room.setNextPlayer();
		room.changeBetMax(value);
		
		if(jeton == 0){//需要增加分池分割量
			useSplitPoolBetValue += currValue;
			room.addSplitValue(useSplitPoolBetValue);
		}
		
		//下注信息广播
		opBroadcast(0, currPlayer);
		
		//发牌处理
		if(room.canFapaiByPutBetOrGiveup(this)){
			room.splitPool();//发牌前先进行分池处理
			room.fapaiOrCompareCard();
		}
	}
	/**
	 * 下注
	 * @param currValue
	 */
	public void chipIn(int currValue){
		//判断是否满足下注条件
		//指此轮之前的玩家没有进行过下的行为，此时该玩家可以按照自己的意愿率先下一定数量的德州豆到游戏中。
		
		checkCanOP();
		
		if(!room.canChipIn(this)){
			throw new RuntimeException("非法操作， chipin");
		}
		op = DZPlayerOP.ChipIn;
		//下注时改变最大下注值
		room.chipInset(currValue);
		putBet(currValue);
	}
	
	/**
	 * 加注
	 * @param currValue
	 */
	public void addUp(int currValue){
		checkCanOP();
		int value = currValue+betValue;
		if(value<room.getBetMax()){
			throw new RuntimeException("非法操作， addUp"); 
		}
		op = DZPlayerOP.AddUp;
		putBet(currValue);
	}
	/**
	 * 跟注
	 * @param currValue
	 */
	public void follow(int currValue){
		checkCanOP();
		int value = currValue+betValue;
		if(value == jeton){
			allIn(currValue);
			return;
		}
		if(value != room.getBetMax()){
			throw new RuntimeException("非法操作， follow"); 
		}
		op = DZPlayerOP.Follow;
		putBet(currValue);
	}
	/**
	 * 全下
	 * @param currValue
	 */
	public void allIn(int currValue){
		checkCanOP();
		int value = currValue+betValue;
		if(value != jeton){
			throw new RuntimeException("非法操作， allIn"); 
		}
		op = DZPlayerOP.AllIn;
		
		
		putBet(currValue);
	}
	/**
	 * 让牌
	 */
	public void yieldPai(){
		checkCanOP();
		//1.当先下注者是否为此参与者
		DZPlayer currPlayer = room.getBetPlayer();
		if( currPlayer!= this){
			throw new RuntimeException("非法处理");
		}
		if(!room.canYield(this)){
			throw new RuntimeException("非法操作");
		}
		
		op = DZPlayerOP.Yield;
		room.setNextPlayer();
		//发牌处理
		if(room.canFapaiByYield(this)){
			room.fapaiOrCompareCard();
		}
		
		//下注信息广播
		opBroadcast(0, currPlayer);
	}
	/**
	 * 弃牌
	 */
	public void giveupPai(){
		checkCanOP();
		//1.当先下注者是否为此参与者
		DZPlayer currPlayer = room.getBetPlayer();
		if( currPlayer!= this){
			throw new RuntimeException("非法处理");
		}
		op = DZPlayerOP.GiveUp;
		room.setNextPlayer();
		//发牌处理
		if(room.canFapaiByPutBetOrGiveup(this)){
			room.fapaiOrCompareCard();
		}
		//下注信息广播
		opBroadcast(0, currPlayer);
	}
	
	private void opBroadcast(int currJeton,DZPlayer currPlayer){
		//下注信息广播
		DZCardProto.GMsg_12051004.Builder builder = DZCardProto.GMsg_12051004.newBuilder();
		builder.setJeton(currJeton);
		builder.setOp(op.getOp());
		builder.setRemainJeton(jeton);
		builder.setRid(currPlayer.getRid());
		builder.setPutbetRid(room.getBetPlayer().getRid());
		room.sendMessageToAll(builder.build(), null);
	}
	
	
	
	
	public int getJeton() {
		return jeton;
	}

	public void setJeton(int jeton) {
		this.jeton = jeton;
	}

	public int getBetValue() {
		return betValue;
	}

	public void setBetValue(int betValue) {
		this.betValue = betValue;
	}

	public int getPosition() {
		return position;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}

	public boolean isJoined() {
		return joined;
	}

	public void setJoined(boolean joined) {
		status = joined ? DZPlayerStatus.Joined : DZPlayerStatus.NoJoin;
		this.joined = joined;
	}
	
	
	@Override
	public String toString() {
		return "DZPlayer [position=" + position + ", joined=" + joined
				+ ", winJeton=" + winJeton + ", jeton=" + jeton + ", "
				+ ", betValue=" + betValue
				+ ", useSplitPoolBetValue=" + useSplitPoolBetValue
				+ ", jetonPoolMaxIndex=" + jetonPoolMaxIndex + ", status="
				+ status + ", op=" + op + "]";
	}


	/**
	 * 所进行的操作
	 * @author YW0941
	 *
	 */
	public enum DZPlayerOP{
		//初始状态，每发完牌后，会将状态初始化为此状态
		Common(0),
		//弃牌
		GiveUp(1),
		//让牌
		Yield(2),
		//跟牌
		Follow(3),
		//加注
		AddUp(4),
		//下注
		ChipIn(5),
		//全下
		AllIn(6),
		;
		private int op;
		
		private DZPlayerOP(int op){
			this.op = op;
		}

		public int getOp() {
			return op;
		}
		
	}
	/**
	 * 状态
	 * @author YW0941
	 *
	 */
	public enum DZPlayerStatus{
		//已加入游戏
		Joined(1),
		//未加入游戏
		NoJoin(2),
		
		
		;
		private int status;
		private DZPlayerStatus(int status){
			this.status = status;
		}
		public int getStatus() {
			return status;
		}
		
	}
	
}
