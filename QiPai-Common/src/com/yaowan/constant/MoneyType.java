package com.yaowan.constant;

import java.util.HashMap;

/**
 * 货币类型
 * 
 * @author zane 2016年10月14日 下午5:13:35
 */
public enum MoneyType {
	/**
	 * 金币(1)
	 */
	Gold(1),
	/**
	 * 钻石(2)
	 */
	Diamond(2),
	/**
	 * 水晶(3)
	 */
	Cristal(3),
	
	;

	private final int value;

	private static HashMap<Byte, MoneyType>	map	= new HashMap<Byte, MoneyType>();

	static {
		for (MoneyType elem : MoneyType.values()) {
			map.put(elem.byteValue(), elem);
		}
	}

	private MoneyType(int value) {
		this.value = value;
	}

	public byte byteValue() {
		return (byte) this.value;
	}

	public static final MoneyType valueOf(byte moneyType) {
		return map.get(moneyType);
	}

}
