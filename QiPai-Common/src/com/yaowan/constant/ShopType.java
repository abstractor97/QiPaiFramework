package com.yaowan.constant;

import java.util.HashMap;

/**
 * @author YW0824
 *	商店类型
 */
public enum ShopType {
	
	CoinShop(1),//金币商店
	
	DiaMondShop(2),//砖石商店
	
	VipShop(3),//vip商店
	
	ItemShop(4)//道具商店
	
	;
	
	private final int value;
	
	private static HashMap<Integer, ShopType>	map	= new HashMap<Integer, ShopType>();
	
	static {
		for (ShopType elem : ShopType.values()) {
			map.put(elem.getValue(), elem);
		}
	}

	private ShopType(int value) {
		this.value = value;
	}

	public int getValue() {
		return  this.value;
	}

	public static final ShopType valueOf(int itemEvent) {
		return map.get(itemEvent);
	}
	
}
