/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.rule;

import java.util.ArrayList;
import java.util.List;

import com.yaowan.framework.util.LogUtil;
import com.yaowan.protobuf.game.GMenJi;
import com.yaowan.protobuf.game.GMenJi.MJCardType;
import com.yaowan.server.game.model.struct.CardBigType;



/**
 * 焖鸡算法
 *
 * @author zane
 */
public class ZTMenji {
	
	
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
		}
		return bigType;
	}
	
	/**
	 * 是否是顺子
	 * @param card
	 * @return
	 */
	public static boolean getSmallTypeIsShunzi(int[] card){
		
		if((card[0] != 0 && card[1] != 0 && card[2] != 0) && (card[0] != 1 && card[1] != 1 && card[2] != 1)){
			if(Math.abs(card[1]-card[0]) == 1 && Math.abs(card[2]-card[1]) == 1){
				return true;
			}
		} else if((card[0] == 11 && card[1] == 12 && card[2] == 0) || 
				(card[0] == 12 && card[1] == 0 && card[2] == 1) || (card[0] == 2 && card[1] == 3 && card[2] == 1)){
			return true;
		}
		
		return false;
	}
	
	/**
	 * 是否是豹子
	 * @param card
	 * @return
	 */
	public static boolean getSmallTypeIsBaozi(int[] card){
		if(card[0] == card[1] && card[1] == card[2]){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 是否是对子
	 * @param card
	 * @return
	 */
	public static boolean getSmallTypeIsDuizi(int[] card){
		if(card[0] == card[1] || card[1] == card[2]){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 是否是特殊
	 * @param card
	 * @return
	 */
	public static boolean getSmallTypeIsTeshu(int[] card){
		if(card[0] == 2 && card[1] == 3 && card[2] == 5){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 是否是同花
	 * @param cardBigTypes
	 * @return
	 */
	public static boolean getBigTypeIsTonghua(CardBigType[] cardBigTypes){
		if(cardBigTypes[0].equals(cardBigTypes[1]) && cardBigTypes[1].equals(cardBigTypes[2])){
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * 获得牌的类型
	 * @param ids
	 * @return
	 */
	@SuppressWarnings("unused")
	public	static GMenJi.MJCardType getCardType(List<Integer> ids){
		if (ids.size() != 3) {
			LogUtil.error("ids.size()"+ids.size());
			return GMenJi.MJCardType.SAN_PAI;
			//throw new RuntimeException("牌的数量不合法"+ids.size());
		}
		LogUtil.info("三个参数分别为" + ids.get(0) + "," + ids.get(1) + "," + ids.get(2));
		int[] cardSmallTypes = new int[3];
		CardBigType[] cardBigTypes = new CardBigType[3];
		for (int i = 0 ; i < ids.size() ; i ++) {
			cardSmallTypes[i] = ids.get(i) % 13;
		}
		for(int i = 0 ; i < ids.size() ; i ++) {
			cardBigTypes[i] = getBigType(ids.get(i));
		}
		sortSamllCard(cardSmallTypes);
		boolean isShunzi = getSmallTypeIsShunzi(cardSmallTypes);
		if(getBigTypeIsTonghua(cardBigTypes)){
			if(isShunzi){
				return GMenJi.MJCardType.TONG_HUA_SHUN;
			}else{
				return GMenJi.MJCardType.JIN_HUA;
			}
		}else{
			if(getSmallTypeIsBaozi(cardSmallTypes)){
				return GMenJi.MJCardType.BAO_ZI;
			}else if(isShunzi){
				return GMenJi.MJCardType.SHUN_ZI;
			}else if(getSmallTypeIsDuizi(cardSmallTypes)){
				return GMenJi.MJCardType.DUI_ZI;
			}else if(getSmallTypeIsTeshu(cardSmallTypes)){
				return GMenJi.MJCardType.TE_SHU;
			}else{
				return GMenJi.MJCardType.SAN_PAI;
			}
		}
	}
	
	
	/**
	 * 两个用户比牌
	 * @param cardBigTypeList
	 * @param ids
	 */
	@SuppressWarnings("unused")
	public static boolean getWhoBig(List<MJCardType> menJiCardTypeList,List<Integer> ids1,List<Integer> ids2){
		int[] array1 = new int[3];
		int[] array2 = new int[3];
		for (int i = 0; i < array1.length; i++) {
			array1[i] = ids1.get(i)%13;
			array2[i] = ids2.get(i)%13;
		}
		//Collections.sort(ids1);
		sortSamllCard(array1);
		sortSamllCard(array2);
		int[] cardType = new int[7];
		MJCardType winerCardType;
		int index;
		for (int i = 0; i < menJiCardTypeList.size(); i++) {
			switch (menJiCardTypeList.get(i).toString()) {
			case "BAO_ZI":
				cardType[0] = cardType[0] + 1;
				break;
			case "TONG_HUA_SHUN":
				cardType[1] = cardType[1] + 1;
				break;
			case "JIN_HUA":
				cardType[2] = cardType[2] + 1;
				break;
			case "SHUN_ZI":
				cardType[3] = cardType[3] + 1;
				break;
			case "DUI_ZI":
				cardType[4] = cardType[4] + 1;
				break;
			case "SAN_PAI":
				cardType[5] = cardType[5] + 1;
				break;
			case "TE_SHU":
				cardType[6] = cardType[6] + 1;
				break;
			}
		}
		if(cardType[0] >= 1){
			if(cardType[6] >= 1){
				index = 6;
				winerCardType = MJCardType.TE_SHU;
			}else{
				index = 0;
				winerCardType = MJCardType.BAO_ZI;
			}
		} else if(cardType[1] >= 1){
			index = 1;
			winerCardType = MJCardType.TONG_HUA_SHUN;
		} else if(cardType[2] >= 1){
			index = 2;
			winerCardType = MJCardType.JIN_HUA;
		} else if(cardType[3] >= 1){
			index = 3;
			winerCardType = MJCardType.SHUN_ZI;
		} else if(cardType[4] >= 1){
			index = 4;
			winerCardType = MJCardType.DUI_ZI;
		} else if(cardType[5] >= 1){
			index = 5;
			winerCardType = MJCardType.SAN_PAI;
		}else{
			index = 6;
			winerCardType = MJCardType.TE_SHU;
		}
		
		if(cardType[index] == 2){//两个用户的牌型一致
			int result = 0;
			switch (index) {
			case 0:
				result = towCardWhoBug(array1[0], array2[0]);
				break;
			case 1:
			case 2:
			case 3:
			case 5:
				for (int i = 2; i >= 0; i--) {
					result = towCardWhoBug(array1[i], array2[i]);
					if(result != 0){
						break;
					}
				}
				break;
			case 4:
				result = towCardWhoBug(array1[1], array2[1]);
				if(result == 0){
					int id1;
					int id2;
					if(array1[0] != array1[1]){
						id1 = array1[0];
					}else{
						id1 = array1[2];
					}
					if(array2[0] != array2[1]){
						id2 = array2[0];
					}else{
						id2 = array2[2];
					}
					result = towCardWhoBug(id1, id2);
				}
				break;

			default:
				result = 0;
				break;
			}
			if(result == 1){
				return true;
			}else{
				return false;
			}
		}else{
			if(menJiCardTypeList.get(0).equals(winerCardType)){
				return true;
			}else{
				return false;
			}
		}
		
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
	 * 将用户的牌排序 2<3<4<5<6<7<8<9<10<J<Q<K<A
	 * @param ids
	 */
	public static void sortSamllCard(int[] ids){
		int min;
		int index;
		for(int i = 0; i < ids.length; i++){
			if(ids[i] == 0){
				ids[i] = 13;
			}else if(ids[i] == 1){
				ids[i] =14;
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
			}else if(ids[i] == 14){
				ids[i] =1;
			}
		}
	}

//	public static void sortBigAndSmallCard(int[] color,int[] ids){
//		if(ids[0] == ids[1] || ids[0] == ids[2] || ids[1] == ids[2]){//有两张以上牌相同
//			if(ids[0] == ids[1]){
//				
//			}else if(ids[0] == ids[2]){
//				int temp = ids[2];
//				ids[2] = ids[1];
//				ids[1] = temp;
//			}else if(ids[1] == ids[2]){
//				int temp = ids[0];
//				ids[0] = ids[2];
//				ids[2] = temp;
//			}
//		}else{
//			for (int i = 0; i < ids.length; i++) {
//				if(ids[i] == 1){
//					ids[i] = 14;
//				}
//			}
//			
//			
//			for (int i = 0; i < ids.length; i++) {
//				int max = ids[i];
//				int index = i;
//				for (int j = i+1; j < ids.length; j++) {
//					if(max > ids[j]){
//						max = ids[j];
//						index = j;
//					}
//				}
//				int temp = ids[index];
//				ids[index] = ids[i];
//				ids[i] = temp;
//				temp = color[index];
//				color[index] = color[i];
//				color[i] = temp;
//			}
//			for (int i = 0; i < ids.length; i++) {
//				if(ids[i] == 14){
//					ids[i] = 1;
//				}
//			}
//		}
//		
//	}
	

	/**
	 * 计算出第几部牌是最大的
	 * @param cardlists
	 */
	public static int sortCard(List<List<Integer>> cardlists){
		List<GMenJi.MJCardType> mjCardTypelist = new ArrayList<GMenJi.MJCardType>();
		boolean isTeShu = false;
		boolean isTeShuAndBaozi = false;
		int index = 0;
		int teshuIndex = 0;
		for (List<Integer> cards : cardlists) {
			MJCardType mjCardType = getCardType(cards);
			mjCardTypelist.add(mjCardType);
			if(mjCardType == MJCardType.TE_SHU){
				isTeShu = true;
				teshuIndex = cardlists.indexOf(cards);
			}
		}
		if(isTeShu){
			for (MJCardType mjCardType : mjCardTypelist) {
				if(mjCardType == MJCardType.BAO_ZI){
					isTeShuAndBaozi = true;
				}
			}
		}
		
		
		if(isTeShuAndBaozi){
			index = teshuIndex;
		}else{
			for (int i = 1; i < mjCardTypelist.size(); i++) {
				List<GMenJi.MJCardType> mjCardTypes = new ArrayList<GMenJi.MJCardType>();
				mjCardTypes.add(mjCardTypelist.get(index));
				mjCardTypes.add(mjCardTypelist.get(i));
				if(!getWhoBig(mjCardTypes, cardlists.get(index), cardlists.get(i))){
					index = i;
				}
			}
		}
		return index;
	}
}
