/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.MathUtil;
import com.yaowan.protobuf.game.GDouniu.DNCardType;
import com.yaowan.protobuf.game.GDouniu.GDouniuPai;
import com.yaowan.server.game.model.struct.CardBigType;



/**
 * 斗牛算法
 *
 * @author zane
 */
public class DouniuRule {
	
	
	public static int DOUNIU_PAI_NUM = 5;
	/**
	 * 获取牌花色
	 * @param card
	 * @return
	 */
	public static int numToPaiColor(int card){
		return getBigType(card).ordinal();
	}
	
	/**
	 * 获取牌点
	 * @param card
	 * @return
	 */
	public static int numToPaiValue(int card){
		return card % 13 == 0 ? 13 : card % 13;
	}
	
	
	/**
	 * 获得牌的花色
	 * @param id
	 * @return
	 */
	public static CardBigType getBigType(int id){
		CardBigType bigType = null;
		if(1 <= id && id <= 13) {
			bigType = CardBigType.FANG_KUAI;
		}else if (14 <= id && id <= 26) {
			bigType = CardBigType.MEI_HUA;
		}else if (27 <= id && id <= 39) {
			bigType = CardBigType.HONG_TAO;
		}else if (40 <= id && id <= 52) {
			bigType = CardBigType.HEI_TAO;
		}else if (id == 53) {
			bigType = CardBigType.XIAO_WANG;
		}else if (id == 54) {
			bigType = CardBigType.DA_WANG;
		}
		return bigType;
	}
	
	/**
	 * 是否是 顺子
	 * 连续点数的牌，最小为A，最大为K，不包括大小王；
	 * @param card
	 * @return
	 */
	public static boolean getSmallTypeIsShunzi(int[] card){
		int size = card.length;
		boolean flag = true;
		for (int n = 0; n < size - 1; n++) {
			int prev = card[n];
			int next = card[n + 1];
			// 小王、大王、2不能加入顺子
			if (prev - next != -1) {
				flag = false;
				break;
			}
		}
		return flag;
	}
	
	/**
	 * 有没大小王
	 * @param card
	 * @return
	 */
	public static boolean getBigTypeIsWan(int[] card){
		
		for (int i = 0; i < card.length; i++) {
			if (card[i]==CardBigType.XIAO_WANG.ordinal()||card[i]==CardBigType.DA_WANG.ordinal()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 是否是五小牛
	 * 五小牛，五张牌的点数相加小于10；
	 * @param card
	 * @return
	 */
	public static boolean getSmallTypeIsWuXiaoNiu(int[] card) {
		Integer count = 0;
		for (int i = 0; i < card.length; i++) {
			count += card[i];
		}
		if (count <= 10) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否是金牛
	 * 金牛，五张花牌为金牛，花牌就是指带人的牌，大小王也算在内；
	 * @param card
	 * @return
	 */
	public static boolean getSmallTypeIsJinNiu(int[] card,int color[]) {
		for (int i = 0; i < card.length; i++) {
			if(color[i] == CardBigType.DA_WANG.ordinal() || color[i] == CardBigType.XIAO_WANG.ordinal()){
				continue;
			}
			if(card[i] < 11){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 是否是葫芦 
	 * 三张一样的牌，带一对
	 * @param card
	 * @return
	 */
	public static boolean getSmallTypeIsHuLu(int[] card) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < card.length; i++) {
			Integer count = map.get(card[i]);
			if (count == null) {
				map.put(card[i], 1);
			} else {
				map.put(card[i], count + 1);
			}
		}
		
		if (map.size() != 2) {
			return false;
		}
		
		for (Map.Entry<Integer, Integer> entry: map.entrySet()) {
			if(entry.getValue() != 2 && entry.getValue() != 3){
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 是否是四梅
	 * 四张点数一样的牌带任意一张牌；
	 * @param card
	 * @return
	 */
	public static boolean getSmallTypeIsSiMei(int[] card){
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < card.length; i++) {
			Integer count = map.get(card[i]);
			if (count == null) {
				map.put(card[i], 1);
			} else {
				map.put(card[i], count + 1);
			}
		}
		for (Map.Entry<Integer, Integer> entry: map.entrySet()) {
			if(entry.getValue()>=4){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 计算牛几
	 * 
	 * @param card
	 * @return
	 */
	public static DNCardType getNormalCardType(int[] card, int color[]) {
		int count = 0;
		int[] value = new int[card.length];
		List<Integer> srcList = new ArrayList<Integer>();
		for (int i = 0; i < card.length; i++) {
			value[i] = getCardPoint(card[i], color[i]);
			count += value[i];
			srcList.add(value[i]);
		}
		
		// 是筛选出来的组合
		List<Integer> tempList = new ArrayList<Integer>();
		List<List<Integer>> list = new ArrayList<List<Integer>>();
		MathUtil.getZuhe(value, 0, 3, tempList, list);
		
		//System.out.println("原牌：" + card[0] + " " + card[1] + " " + card[2] + " " + card[3] + " " + card[4]);
		//System.out.println("颜色：" + color[0] + " " + color[1] + " " + color[2] + " " + color[3] + " " + color[4]);
		//System.out.println("花牌转换后：" + value[0] + " " + value[1] + " " + value[2] + " " + value[3] + " " + value[4] + ", 共 " + list.size()+" 种牌型");
		
		int max = -1;
		for (List<Integer> data : list) {
			int preSum = 0;
			List<Integer> helpList = new ArrayList<Integer>();
			helpList.addAll(srcList);
			for (Integer num : data) {
				preSum += num;
				helpList.remove(num);
			}
			
			if (preSum % 10 == 0) {	
				int afterSum = 0;
				for (Integer num2 : helpList) {
					afterSum += num2;
				}
				
				afterSum = afterSum % 10;				
				if (afterSum > max) {
					max = afterSum;
				}
			}
		}

		DNCardType cardType = DNCardType.DN_MEI_NIU;
		switch (max) {
		case 9:
			cardType = DNCardType.DN_NIU_JIU;
			break;
		case 8:
			cardType = DNCardType.DN_NIU_BA;
			break;
		case 7:
			cardType = DNCardType.DN_NIU_QI;
			break;
		case 6:
			cardType = DNCardType.DN_NIU_LIU;
			break;
		case 5:
			cardType = DNCardType.DN_NIU_WU;
			break;
		case 4:
			cardType = DNCardType.DN_NIU_SI;
			break;
		case 3:
			cardType = DNCardType.DN_NIU_SAN;
			break;
		case 2:
			cardType = DNCardType.DN_NIU_ER;
			break;
		case 1:
			cardType = DNCardType.DN_NIU_YI;
			break;
		case 0:
			cardType = DNCardType.DN_NIU_NIU;
			break;
		default:
			cardType = DNCardType.DN_MEI_NIU;
			break;
		}

		//System.out.println("牛牛牌: " + cardType.toString());
		return cardType;
	}
	
	/**
	 * 牌型对应点数
	 * 
	 * @param card
	 * @return
	 */
	public static int getCardPoint(int card, int color) {
		if (color == CardBigType.DA_WANG.ordinal() || color == CardBigType.XIAO_WANG.ordinal() || card > 10) {
			return 10;	// 花牌 10点
		}
		return card;
	}
	
	/**
	 * 是否是同花
	 * 
	 * @param cardBigTypes
	 * @return
	 */
	public static boolean getBigTypeIsTonghua(int[] cardBigTypes) {
		if (cardBigTypes[0] == (cardBigTypes[1])
				&& cardBigTypes[1] == (cardBigTypes[2])
				&& cardBigTypes[2] == (cardBigTypes[3])
				&& cardBigTypes[3] == (cardBigTypes[4])) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 获得牌的类型
	 * @param ids
	 * @return
	 */
	@SuppressWarnings("unused")
	public	static DNCardType getCardType(GDouniuPai.Builder pais){
		if (pais.getPaiValueCount() != 5) {
			LogUtil.error("ids.size()"+pais.getPaiValueCount());
			return DNCardType.DN_MEI_NIU;
			//throw new RuntimeException("牌的数量不合法"+ids.size());
		}
		int[] cardNumTypes = new int[5];
		int[] cardColorTypes = new int[5];
		for (int i = 0 ; i < 5 ; i ++) {
			cardNumTypes[i] = pais.getPaiValueList().get(i);
		}
		for(int i = 0 ; i < 5 ; i ++) {
			cardColorTypes[i] = pais.getPaiColorList().get(i);
		}
		//有王的牌型
		if(getBigTypeIsWan(cardColorTypes)){
			if(getSmallTypeIsJinNiu(cardNumTypes, cardColorTypes)){
				return DNCardType.DN_JIN_NIU;
			}else{
				return getNormalCardType(cardNumTypes, cardColorTypes);
			}
		}else{
			//没王的牌型
			sortSamllCard(cardNumTypes);
			boolean isShunzi = getSmallTypeIsShunzi(cardNumTypes);
			boolean isTonghua = getBigTypeIsTonghua(cardColorTypes);
			if (isShunzi && isTonghua) {
				return DNCardType.DN_TONG_HUA_SHUN;
			} else{
				if(getSmallTypeIsWuXiaoNiu(cardNumTypes)){
					return DNCardType.DN_WU_XIAO_NIU;
				}else if(getSmallTypeIsJinNiu(cardNumTypes, cardColorTypes)){
					return DNCardType.DN_JIN_NIU;
				}else if(getSmallTypeIsSiMei(cardNumTypes)){
					return DNCardType.DN_SI_MEI;
				}else if(getSmallTypeIsHuLu(cardNumTypes)){
					return DNCardType.DN_HU_LU;
				}else if(isTonghua){
					return DNCardType.DN_TONG_HUA;
				}else if(isShunzi){
					return DNCardType.DN_SHUN_ZI;
				}
				return getNormalCardType(cardNumTypes, cardColorTypes);
			}
		}		
	}
	
	
	/**
	 * 两个用户比牌
	 * @param cardBigTypeList
	 * @param ids
	 */
	@SuppressWarnings("unused")
	public static boolean getWhoBig(GDouniuPai.Builder card1,GDouniuPai.Builder card2){
		if (card1.getPaiType().getNumber() == card2.getPaiType().getNumber()) {// 两个用户的牌型一致
			int[] card1Types = new int[DOUNIU_PAI_NUM];
			int[] card2Types = new int[DOUNIU_PAI_NUM];
			
			int flag1 = 0;
			int flag2 = 0;
			
			for (int i = 0 ; i < DOUNIU_PAI_NUM ; i ++) {
				card1Types[i] = card1.getPaiValueList().get(i);
				card2Types[i] = card2.getPaiValueList().get(i);
				
				if (card1.getPaiColorList().get(i)==CardBigType.XIAO_WANG.ordinal()||card1.getPaiColorList().get(i)==CardBigType.DA_WANG.ordinal()) {
					flag1 = card1.getPaiColorList().get(i);
				}
				if (card2.getPaiColorList().get(i)==CardBigType.XIAO_WANG.ordinal()||card2.getPaiColorList().get(i)==CardBigType.DA_WANG.ordinal()) {
					flag2 = card2.getPaiColorList().get(i);
				}
			}
			
			// 大小王
			if (flag1 > 0 || flag2 > 0) {
				if (flag1 > flag2) {
					return true;
				} else {
					return false;
				}
			}
			
			if (card1.getPaiType() == DNCardType.DN_SHUN_ZI
					|| card1.getPaiType() == DNCardType.DN_TONG_HUA_SHUN) {
				sortSamllCardShunZi(card1Types);
				sortSamllCardShunZi(card2Types);
			} else {
				sortSamllCard(card1Types);
				sortSamllCard(card2Types);
			}
			for (int i = DOUNIU_PAI_NUM - 1; i >= 0; i--) {
				if (card1Types[i] > card1Types[i]) {
					return true;
				} else if (card1Types[i] == card1Types[i]) {
					continue;
				} else {
					return false;
				}
			}
			return true;
			
		} else if (card1.getPaiType().getNumber() < card2.getPaiType()
				.getNumber()) {
			return true;
		} 
		return false;
		
	}
	
	/**
	 * 两张牌比大小
	 * @param id1
	 * @param id2
	 * @return 0：平牌，1：第一张大，2：第二张大
	 */
	@SuppressWarnings("unused")
	public static int towCardWhoBug(int id1,int id2){
		int result;
		if(id1 == id2){
			//平牌
			result = 0;
		}else if((id1 == 0 || id1 == 1) && (id2 == 0 || id2 == 1)){
			if(id1 > id2){
				result = 1;
			}else{
				result = 2;
			}
		}else if(id1 == 0 || id1 == 1 || id2 == 0 || id2 == 1){
			if(id1 == 0 || id1 == 1){
				result = 1;
			}else{
				result = 2;
			}
		}else{
			if(id1 > id2){
				result = 1;
			}else{
				result = 2;
			}
		}
		return result;
	}
	
	/**
	 * 将用户的牌排序 A<2<3<4<5<6<7<8<9<10<J<Q<K
	 * @param ids
	 */
	public static void sortSamllCardShunZi(int[] ids){
		int min;
		int index;
		for(int i = 0; i < ids.length; i++){
			if(ids[i] == 0){
				ids[i] = 13;
			}
		}
		for (int i = 0; i < ids.length; i++) {
			min = ids[i];
			index = i;
			for (int j = i+1; j < ids.length; j++) {
				if(min > ids[j]){
					min = ids[j];
					index = j;
				}
			}
			if(index != i){
				int temp = ids[index];
				ids[index] = ids[i];
				ids[i] = temp;
			}
		}
		for(int i = 0; i < ids.length; i++){
			if(ids[i] == 13){
				ids[i] = 0;
			}
		}
	}

	/**
	 * 将用户的牌排序 2<3<4<5<6<7<8<9<10<J<Q<K<A
	 * 
	 * @param ids
	 */
	public static void sortSamllCard(int[] ids) {
		int min;
		int index;
		for (int i = 0; i < ids.length; i++) {
			if (ids[i] == 1) { // 比单的时候，A是最大的，要将A排在前面
				ids[i] = 14;
			}
		}
		
		for (int i = 0; i < ids.length; i++) {
			min = ids[i];
			index = i;
			for (int j = i + 1; j < ids.length; j++) {
				if (min > ids[j]) {
					min = ids[j];
					index = j;
				}
			}
			if (index != i) {
				int temp = ids[index];
				ids[index] = ids[i];
				ids[i] = temp;
			}
		}
		
		for (int i = 0; i < ids.length; i++) {
			if (ids[i] == 14) { // 还原A
				ids[i] = 1;
			}
		}
	}
	
	/*public static void main(String[] args) {
		int[] card = { 12, 2, 5 ,4, 9};
		int[] color = {0,0,2,1,1};
		getNormalCardType(card, color);
	}*/
}
