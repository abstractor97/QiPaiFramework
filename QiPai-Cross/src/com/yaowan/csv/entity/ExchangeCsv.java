package com.yaowan.csv.entity;

//cfg_item.csv
public class ExchangeCsv {
	
	private int GoodsId;

	private int ShopType;

	private int ItemOrder;
	
	private String ItemName;
	
	private int ItemPrice;
	
	private int ItemId;
	
	private int Stock;
	
	private int Quantity;
	
	private String Iteminfo;
	
	private String ItemIcon;

	public int getGoodsId() {
		return GoodsId;
	}

	public void setGoodsId(int goodsId) {
		GoodsId = goodsId;
	}

	public int getShopType() {
		return ShopType;
	}

	public void setShopType(int shopType) {
		ShopType = shopType;
	}

	public int getItemOrder() {
		return ItemOrder;
	}

	public void setItemOrder(int itemOrder) {
		ItemOrder = itemOrder;
	}

	public String getItemName() {
		return ItemName;
	}

	public void setItemName(String itemName) {
		ItemName = itemName;
	}

	public int getItemPrice() {
		return ItemPrice;
	}

	public void setItemPrice(int itemPrice) {
		ItemPrice = itemPrice;
	}

	public int getItemId() {
		return ItemId;
	}

	public void setItemId(int itemId) {
		ItemId = itemId;
	}

	public int getStock() {
		return Stock;
	}

	public void setStock(int stock) {
		Stock = stock;
	}

	public int getQuantity() {
		return Quantity;
	}

	public void setQuantity(int quantity) {
		Quantity = quantity;
	}

	public String getItemInfo() {
		return Iteminfo;
	}

	public void setItemInfo(String iteminfo) {
		Iteminfo = iteminfo;
	}

	public String getItemIcon() {
		return ItemIcon;
	}

	public void setItemIcon(String itemIcon) {
		ItemIcon = itemIcon;
	}

	@Override
	public String toString() {
		return "ExchangeCsv [GoodsId=" + GoodsId + ", ShopType=" + ShopType
				+ ", ItemOrder=" + ItemOrder + ", ItemName=" + ItemName
				+ ", ItemPrice=" + ItemPrice + ", ItemId=" + ItemId
				+ ", Stock=" + Stock + ", Quantity=" + Quantity + ", Iteminfo="
				+ Iteminfo + ", ItemIcon=" + ItemIcon + "]";
	}
	
	
}
