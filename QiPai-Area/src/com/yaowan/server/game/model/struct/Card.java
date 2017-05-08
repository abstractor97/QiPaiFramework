package com.yaowan.server.game.model.struct;

import com.yaowan.server.game.rule.ZTDoudizhuRule;

public class Card {

	// 通过牌的整型id构造一张牌
	public Card(int id) {
		this.id = id;
		this.bigType = ZTDoudizhuRule.getBigType(id);
		this.smallType = ZTDoudizhuRule.getSmallType(id);
		this.grade = ZTDoudizhuRule.getGrade(id);
	}

	// 牌的数字ID,1到54 55是赖子
	public int id;

	// 牌的大类型，方块，梅花,红桃,黑桃,小王,大王
	public CardBigType bigType;

	// 牌的小类型，2_10,A,J,Q,K
	public CardSmallType smallType;

	// 牌的等级，对牌进行排序时会用到
	public int grade;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public CardBigType getBigType() {
		return bigType;
	}

	public void setBigType(CardBigType bigType) {
		this.bigType = bigType;
	}

	public CardSmallType getSmallType() {
		return smallType;
	}

	public void setSmallType(CardSmallType smallType) {
		this.smallType = smallType;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

}
