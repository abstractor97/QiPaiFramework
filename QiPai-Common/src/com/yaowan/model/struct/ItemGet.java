package com.yaowan.model.struct;

/**
 * @author YW0824
 * 游戏赠送的物品或者奖励
 */
public class ItemGet {
		
	private int id;//1金币	2砖石	 3水晶      道具对应具体的id
	private int num;//数量
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	
	public  String toString(){
		return "id = " + id + " ,num = " + num;
	}
	
	
}
