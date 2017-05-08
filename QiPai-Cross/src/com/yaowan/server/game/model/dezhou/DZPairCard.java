package com.yaowan.server.game.model.dezhou;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.yaowan.server.game.model.dezhou.DZCard.Color;

/**
 * 一副牌
 * @author YW0941
 *
 */
public class DZPairCard {

	private List<DZCard> cards;
	
	private int maxPos;
	public DZPairCard(){
		init();
	}
	
	private void init(){
		cards = new ArrayList<DZCard>();
		initSameColorCard(Color.Hearts);
		initSameColorCard(Color.Clubs);
		initSameColorCard(Color.Diamond);
		initSameColorCard(Color.Spade);
		maxPos = cards.size();
		shuffle();
	}
	
	private void initSameColorCard(Color color){
		//2~10
		for(short value=2;value<=10;value++){
			cards.add(new DZCard(value, String.valueOf(value), color));
		}
		//J
		cards.add(new DZCard((short)11, "J", color));
		//Q
		cards.add(new DZCard((short)12, "Q", color));
		//K
		cards.add(new DZCard((short)13, "K", color));
		//A
		cards.add(new DZCard((short)14, "A", color));
	}
	/**
	 * 将扑克牌打散
	 */
	private void shuffle(){
		Collections.shuffle(cards);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PairCard Card Number="+cards.size()).append("\n");
		for (DZCard card : cards) {
			builder.append(card).append("\n");
		}
		return builder.toString();
	}
	/**
	 * 从剩下的牌中抽一张牌
	 * @param position 牌的位置，从1开始
	 * @return
	 */
	public DZCard drawCard(int position){
		if(position > maxPos || position<=0)
			throw new RuntimeException("提供的牌位置错误");
		DZCard card = cards.remove(position-1);
		maxPos--;
		return card;
	}
	
	/**
	 * 按顺序发牌
	 * @return
	 */
	public DZCard sendCard(){
		return drawCard(1);
	}
	
}
