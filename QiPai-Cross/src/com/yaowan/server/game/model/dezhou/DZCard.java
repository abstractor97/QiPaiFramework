package com.yaowan.server.game.model.dezhou;
/**
 * 扑克牌
 * @author YW0941
 *
 */
public class DZCard {
	// 牌大小
	private short value;
	// 牌名称
	private String name;
	// 牌的花色
	private Color color;

	public DZCard(short value, String name, Color color) {
		super();
		this.value = value;
		this.name = name;
		this.color = color;
	}

	public short getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	public Color getColor() {
		return color;
	}
	
	@Override
	public String toString() {
		return "Card [value=" + value + ", name=" + name + ", color=" + color
				+ "]";
	}

	public enum Color {
		// 黑桃
		Spade((byte) 1),
		// 红桃
		Hearts((byte) 2),
		// 梅花
		Clubs((byte) 3),
		// 方块
		Diamond((byte) 4), ;

		private byte color;

		private Color(byte color) {
			this.color = color;
		}

		public byte get() {
			return color;
		}
	}
}
