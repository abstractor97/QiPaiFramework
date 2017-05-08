package com.yaowan.server.game.model.dezhou;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.yaowan.server.game.model.dezhou.DZCard.Color;

/**
 * 一手德州牌，共7张
 * 
 * @author YW0941
 *
 */
public class DZHandCard {
	//一手牌最多拥有的牌数量
	public static final int HandCardCount = 7;
	// 花色种类数量,按黑红梅方顺序存入数组
	private int[] colorCount = new int[]{0,0,0,0,} ;
	
	// 所有的牌
	private List<DZCard> cards;
	
	//牌所属类型
	private CardResult result;
	
	//顺子中最大的那张牌
	private DZCard shunZiCard;
	
	//四条
	private List<DZCard> fourCards = new LinkedList<DZCard>();
	
	//三条
	private List<DZCard> threeCards = new LinkedList<DZCard>();
	
	//两条
	private List<DZCard> twoCards = new LinkedList<DZCard>();
	
	//单张
	private List<DZCard> singleCards = new LinkedList<DZCard>();
	
	//最后的成手牌，用于比输赢
	private List<DZCard> myCards;
	
	public List<DZCard> getCards() {
		return cards;
	}
	public CardResult getResult() {
		return result;
	}
	public DZCard getShunZiCard() {
		return shunZiCard;
	}
	public List<DZCard> getFourCards() {
		return fourCards;
	}
	public List<DZCard> getThreeCards() {
		return threeCards;
	}
	public List<DZCard> getTwoCards() {
		return twoCards;
	}
	public List<DZCard> getSingleCards() {
		return singleCards;
	}
	/**
	 * 牌类型
	 * 皇家同花顺>同花顺>四条>葫芦>同花>顺子>三条>两对>一对>高牌
	 * @author YW0941
	 *
	 */
	public enum CardResult{
		//皇家同花顺>
		RoyalFlush((byte)10),
		//同花顺>
		Flush((byte)9),
		//四条>
		Four((byte)8),
		//葫芦>
		gourd((byte)7),
		//同花>
		SameColor((byte)6),
		//顺子>
		ShunZi((byte)5),
		//三条>
		Three((byte)4),
		//两对>
		Two((byte)3),
		//一对>
		One((byte)2),
		//高牌
		Single((byte)1),
		;
		private byte result ;
		private CardResult(byte result){
			this.result = result;
		}
		public byte get(){
			return result;
		}
		
	}

	public DZHandCard() {
		cards = new ArrayList<DZCard>();
	}
	/**
	 * 摸一张牌
	 * @param card
	 */
	public void put(DZCard card) {
		if (card != null){
			if(cards.size()>= HandCardCount){
				throw new RuntimeException("一手牌已抓齐，无须再抓牌");
			}
			cards.add(card);
			putInColor(card);
		}
	}
	/**
	 * 连续摸多张牌
	 * @param cards
	 */
	public void put(List<DZCard> cards){
		for (DZCard card : cards) {
			put(card);
		}
	}
	
	private void putInColor(DZCard card){
		switch (card.getColor()) {
		case Spade:// 黑桃
			colorCount[0]++;
			break;
		case Hearts:// 红桃
			colorCount[1]++;
			break;
		case Clubs:// 梅花
			colorCount[2]++;
			break;
		case Diamond:// 方块
			colorCount[3]++;
			break;

		default:
			throw new RuntimeException("未知的花色牌......");
		}
	}
	/**
	 * 获取自己的一手牌
	 * @return
	 */
	public List<DZCard> getHandCard(){
		if(myCards != null){
			return myCards;
		}
		List<DZCard> handCards = null;
		switch (result) {
		case RoyalFlush://皇家同花顺
			handCards = cards.subList(0, 5);
			break;
	
		case Flush://同花顺>
		case ShunZi://顺子>
			handCards = new ArrayList<DZCard>();
			int start = 0;
			int i = 0;
			for (DZCard card : cards) {
				if(card == shunZiCard){
					start = i;
					break;
				}
				i++;
			}
			handCards = cards.subList(start, start+5);
			break;
		case Four://四条>
			handCards = new ArrayList<DZCard>();
			handCards.addAll(fourCards);
			handCards.add(singleCards.get(0));
			break;
		case gourd://葫芦>
			handCards = new ArrayList<DZCard>();
			handCards.addAll(threeCards);
			handCards.addAll(twoCards);
			break;
		case SameColor://同花>
			if(colorCount[0]>=5){//黑桃
				handCards = getSameColorCards(Color.Spade).subList(0, 5);
			}else if(colorCount[1]>=5){
				handCards = getSameColorCards(Color.Hearts).subList(0, 5);
			}else if(colorCount[2]>=5){
				handCards = getSameColorCards(Color.Clubs).subList(0, 5);
			}else {
				handCards = getSameColorCards(Color.Diamond).subList(0, 5);
			}
			break;
		case Three://三条>
			handCards = new ArrayList<DZCard>();
			handCards.addAll(threeCards);
			handCards.addAll(singleCards.subList(0, 2));
			break;
		case Two://两对>
			handCards = new ArrayList<DZCard>();
			handCards.addAll(twoCards);
			handCards.add(singleCards.get(0));
			break;
		case One://一对>
			handCards = new ArrayList<DZCard>();
			handCards.addAll(twoCards);
			handCards.addAll(singleCards.subList(0, 3));
			break;
		case Single://高牌
			handCards = singleCards.subList(0,5);
			break;
		default:
			break;
		}
		myCards = handCards;
		return handCards;
	}
	
	private List<DZCard> getSameColorCards(Color color){
		List<DZCard> sameColorCards = new ArrayList<DZCard>();
		for (DZCard card : cards) {
			if(card.getColor() == color){
				sameColorCards.add(card);
			}
		}
		return sameColorCards;
	}
	/**
	 * 比牌
	 */
	public void compareCard(){
		//判断是否为同花
		for (int count : colorCount) {
			if(count>=5){
				//至少为同花
				result = CardResult.SameColor;
				break;
			}
		}
		//按牌面值进行排序,从大到小
		Collections.sort(cards, new Comparator<DZCard>(){
			public int compare(DZCard o1, DZCard o2) {
				return o2.getValue()-o1.getValue();
			}
		});
		
		//判断是否为顺子
		calculateCard();
		if(shunZiCard!=null && result != CardResult.SameColor){//不同花色时
			result = CardResult.ShunZi;
		}
		
		//判断是否为同花顺
		if(result == CardResult.SameColor){
			if(shunZiCard!=null){
				//同花顺
				result = CardResult.Flush;
				
				if("A".equals(shunZiCard.getName())){
					//皇家同花顺
					result = CardResult.RoyalFlush;
					return;
				}
				return;
			}
		}else if(result == CardResult.ShunZi){
			return;
		}
		//判断是否为四条
		if(fourCards.size()>=4){
			result = CardResult.Four;
			return;
		}
		
		if(threeCards.size()>3){//有两个三条，需要拆一个小的三条为一个对子
			twoCards.add(threeCards.remove(3));
			twoCards.add(threeCards.remove(3));
			threeCards.remove(3);
			//两队需重新排序，确定大小
			Collections.sort(twoCards,new Comparator<DZCard>() {
				public int compare(DZCard o1, DZCard o2) {
					return o2.getValue()-o1.getValue();
				}
			});
		}
		
		if(twoCards.size()>4){//有三个两条，需要拆一个小的到单张
			singleCards.add(twoCards.remove(4));
			twoCards.remove(4);
			//两队需重新排序，确定大小
			Collections.sort(singleCards,new Comparator<DZCard>() {
				public int compare(DZCard o1, DZCard o2) {
					return o2.getValue()-o1.getValue();
				}
			});
		}
		
		//葫芦
		if(threeCards.size()>0 && twoCards.size()>0){
			result = CardResult.gourd;
			return;
		}
		
		if(result == CardResult.SameColor){
			return;
		}
		//三条
		if(threeCards.size()>0){
			result = CardResult.Three;
			return;
		}
		//两队
		if(twoCards.size()>2){
			result = CardResult.Two;
			return;
		}
		
		//一队
		if(twoCards.size()>1){
			result = CardResult.One;
			return;
		}
		//单张
		result = CardResult.Single;
	}
	/**
	 * 算牌
	 * @return
	 */
	private void calculateCard(){
		//保存最大的那张顺子牌
		shunZiCard = cards.get(0);
		//连牌的数量
		int lianpaiCount = 1;
		List<DZCard> tmpCards = new ArrayList<DZCard>();
		tmpCards.add(cards.get(0));
		for(int i=1;i<cards.size();i++){
			DZCard card = cards.get(i);
			if(card.getValue()+1!= cards.get(i-1).getValue()){//非连牌，则需求切换连牌
				if (lianpaiCount<5) {
					shunZiCard = card;
					lianpaiCount= 1;
				}
			}else {//是连牌
				lianpaiCount++;
			}
			if(card.getValue() == tmpCards.get(0).getValue()){//凑成对
				tmpCards.add(card);
			}else {//不相等
				if(tmpCards.size() == 1){
					singleCards.addAll(tmpCards);
				}else if(tmpCards.size() ==2){
					twoCards.addAll(tmpCards);
				}else if(tmpCards.size() == 3){
					threeCards.addAll(tmpCards);
				}else if(tmpCards.size() == 4){
					fourCards.addAll(tmpCards);
				}
				tmpCards.clear();
				tmpCards.add(card);
			}	
		}
		
		if(tmpCards.size()>0){
			if(tmpCards.size() == 1){
				singleCards.addAll(tmpCards);
			}else if(tmpCards.size() ==2){
				twoCards.addAll(tmpCards);
			}else if(tmpCards.size() == 3){
				threeCards.addAll(tmpCards);
			}else if(tmpCards.size() == 4){
				fourCards.addAll(tmpCards);
			}
			tmpCards.clear();
		}
		
		if(lianpaiCount<5){
			shunZiCard = null;
		}
	}
	@Override
	public String toString() {
		return "HandCard [colorCount=" + Arrays.toString(colorCount)
				+ ", cards=" + cards + ", result=" + result + ", shunZiCard="
				+ shunZiCard + ", fourCards=" + fourCards + ", threeCards="
				+ threeCards + ", twoCards=" + twoCards + ", singleCards="
				+ singleCards + ", myCards=" + myCards + "]";
	}
	
	
}
