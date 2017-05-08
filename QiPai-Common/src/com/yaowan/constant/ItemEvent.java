package com.yaowan.constant;

import java.util.HashMap;

/**
 * 道具事件
 * @author YW0824
 */
public enum ItemEvent {

	BuyItem(50001),//购买
	
	DropItem(50002),//丢弃
	
	GetItem(50003),//获得
	
	UseItem(50004),//使用
	
	ExchangeItem(50005),//兑换
	
	FriendRoom(50006)//好友房退回
	
	;
	
	private final int value;
	
	private static HashMap<Integer, ItemEvent>	map	= new HashMap<Integer, ItemEvent>();
	
	static {
		for (ItemEvent elem : ItemEvent.values()) {
			map.put(elem.getValue(), elem);
		}
	}

	private ItemEvent(int value) {
		this.value = value;
	}

	public int getValue() {
		return  this.value;
	}

	public static final ItemEvent valueOf(int itemEvent) {
		return map.get(itemEvent);
	}
}
