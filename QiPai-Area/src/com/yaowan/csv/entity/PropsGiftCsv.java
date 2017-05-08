package com.yaowan.csv.entity;

/**
 * 道具礼物赠送配置表实体类
 * 
 * @author lijintao 2017年2月24日
 */

public class PropsGiftCsv {
	private int SerialNumber;
	private int ExpressionId;
	private String ExpressionPrice;
	private String GiftPrice;
	private int Difference;

	public int getSerialNumber() {
		return SerialNumber;
	}

	public void setSerialNumber(int serialNumber) {
		SerialNumber = serialNumber;
	}

	public int getExpressionId() {
		return ExpressionId;
	}

	public void setExpressionId(int expressionId) {
		ExpressionId = expressionId;
	}

	public String getExpressionPrice() {
		return ExpressionPrice;
	}

	public void setExpressionPrice(String expressionPrice) {
		ExpressionPrice = expressionPrice;
	}

	public String getGiftPrice() {
		return GiftPrice;
	}

	public void setGiftPrice(String giftPrice) {
		GiftPrice = giftPrice;
	}

	public int getDifference() {
		return Difference;
	}

	public void setDifference(int difference) {
		Difference = difference;
	}

}
