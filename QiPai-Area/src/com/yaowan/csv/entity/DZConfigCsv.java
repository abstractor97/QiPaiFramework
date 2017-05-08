package com.yaowan.csv.entity;
/**
 * 德州配置
 * @author YW0941
 *
 */
public class DZConfigCsv {
	//小房间ID
	private int ID;	
	//房间类型
	//1：代表该房间属于白手起家
	//2：代表该房间属于中产阶级
	//3：代表该房间属于德州大亨
	private int RoomType;
	//人数上限
	private int People;
	//最低限制
	//玩家进入该房间的最低持有的金币限制,填0代表不设限制
	private int LowestLimit;
	//最大限制
	//玩家进入房间的最高持有金币限制,填0代表不设限制
	private int HighestLimit;
	//筹码限制
	//玩家手上持有筹码若超过该值，则超过部分自动转化为金币,	填0代表无限制
	private int JettonLimit;
	//初始筹码数
	//玩家进入房间时所兑换的筹码数量
	private int InitialJetton;
	//大盲注
	//代表本房间中大盲玩家所必下的注
	private int BigBlind;
	//小盲注
	//代表本房间中小盲玩家所必下的注
	private int SmallBlind;
	//税金
	//进行牌局的玩家将会扣除对应的金币数量
	private int Taxes;
	//操作时间
	//该栏中单位为（秒）,填0代表无限时间
	private int OperationTime;
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public int getRoomType() {
		return RoomType;
	}
	public void setRoomType(int roomType) {
		RoomType = roomType;
	}
	public int getPeople() {
		return People;
	}
	public void setPeople(int people) {
		People = people;
	}
	public int getLowestLimit() {
		return LowestLimit;
	}
	public void setLowestLimit(int lowestLimit) {
		LowestLimit = lowestLimit;
	}
	public int getHighestLimit() {
		return HighestLimit;
	}
	public void setHighestLimit(int highestLimit) {
		HighestLimit = highestLimit;
	}
	public int getJettonLimit() {
		return JettonLimit;
	}
	public void setJettonLimit(int jettonLimit) {
		JettonLimit = jettonLimit;
	}
	public int getInitialJetton() {
		return InitialJetton;
	}
	public void setInitialJetton(int initialJetton) {
		InitialJetton = initialJetton;
	}
	public int getBigBlind() {
		return BigBlind;
	}
	public void setBigBlind(int bigBlind) {
		BigBlind = bigBlind;
	}
	public int getSmallBlind() {
		return SmallBlind;
	}
	public void setSmallBlind(int smallBlind) {
		SmallBlind = smallBlind;
	}
	public int getTaxes() {
		return Taxes;
	}
	public void setTaxes(int taxes) {
		Taxes = taxes;
	}
	public int getOperationTime() {
		return OperationTime;
	}
	public void setOperationTime(int operationTime) {
		OperationTime = operationTime;
	}
	
	
}
