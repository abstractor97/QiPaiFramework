/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.yaowan.framework.util.MathUtil;
import com.yaowan.framework.util.Probability;

/**
 * 成都麻将算法
 *
 * @author yangbin
 */
public class CDMahJongRule {

	/**
	 * 牌的类型 一万-九万 11 - 19 一筒-九筒 21 - 29 一索-九索 31-39 将牌转换为二维数组
	 *
	 * 首位是牌型总数
	 * 
	 * @param list
	 * @return
	 */
	public static int[][] conversionType(List<Integer> list) {

		int[][] allPai = new int[4][10];
		for (int i = 0; i < list.size(); i++) {
			Integer pai = list.get(i);
			switch (pai / 10) {
			case 1:
				allPai[0][0] = allPai[0][0] + 1;
				allPai[0][pai % 10] = allPai[0][pai % 10] + 1;
				break;
			case 2:
				allPai[1][0] = allPai[1][0] + 1;
				allPai[1][pai % 10] = allPai[1][pai % 10] + 1;
				break;
			case 3:
				allPai[2][0] = allPai[2][0] + 1;
				allPai[2][pai % 10] = allPai[2][pai % 10] + 1;
				break;
			case 4:
				allPai[3][0] = allPai[3][0] + 1;
				allPai[3][pai % 10] = allPai[3][pai % 10] + 1;
				break;
			default:
				break;
			}
		}
		return allPai;
	}

	/**
	 * 听牌
	 *
	 * @param allPai
	 * @return
	 */
	public static List<Integer> tingPai(int[][] allPai, Map<Integer, List<Integer>> show) {

		int pai = 0;
		List<Integer> tingTable = new ArrayList<>();
		int[][] readyAllPai = allPai;

		for (int i = 1; i <= 3; i++) {
			for (int j = 1; j <= 9; j++) {

				pai = 10 * i + j;
				boolean bool = false;
				if (readyAllPai[i - 1][j] < 4) {// 已经是一杠牌了不处理
					readyAllPai[i - 1][0] = readyAllPai[i - 1][0] + 1;
					readyAllPai[i - 1][j] = readyAllPai[i - 1][j] + 1;
					bool = true;
				}// 遍历添加一张 看是否符合胡牌条件
				if (fitHu(readyAllPai,show)) {
					tingTable.add(pai);
				}
				if (bool) {
					readyAllPai[i - 1][0] = readyAllPai[i - 1][0] - 1;
					readyAllPai[i - 1][j] = readyAllPai[i - 1][j] - 1;
				}
			}
		}
		return tingTable;
	}

	/**
	 * 李培光：判断听牌2,通过size
	 * 
	 * @param allPai
	 * @return
	 */
	public static List<Integer> tingPai2(List<Integer> paiList, int laiZi, Map<Integer, List<Integer>> show) {
		// 复制
		List<Integer> tingTable = new ArrayList<>();
		List<Integer> shouPai = new ArrayList<Integer>();
		shouPai.addAll(paiList);
		// list转hashmap（hm是用来判断一条杠）
		HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
		for (Integer integer : shouPai) {
			if (hm.get(integer) == null) {
				hm.put(integer, 1);
			} else {
				hm.put(hm.get(integer), hm.get(integer) + 1);
			}
		}
		int pai = 0;// 添加的牌
		for (int i = 1; i <= 3; i++) {
			for (int j = 1; j <= 9; j++) {
				boolean bool = false;
				pai = Integer.parseInt(i + "" + j);
				if (hm.get(pai) == null || hm.get(pai) < 4) {
					shouPai.add(pai);
					bool = true;
				}
				if (huLaizi(shouPai, laiZi,new HashMap<Integer, List<Integer>>()).size() > 0) {
					tingTable.add(pai);
				}
				if (bool) {
					shouPai.remove(shouPai.size() - 1);
				}

			}
		}
		return tingTable;		
		/*if (tingTable.size() > 0) {
			return true;
		} else {
			return false;
		}*/
	}

	/**
	 * 花猪,癞子不能当做一种花色
	 * 不能有之前确定的缺一门类型
	 * @param pai
	 * @return
	 */
	public static boolean huaZhu(List<Integer> pai, int que_type) {
		int[][] allPai = conversionType(pai);
		
		if (que_type > 0) {
			if (allPai[que_type - 1][0] > 0) {
				// 花猪不能胡牌
				return false;
			}
		} else{ // 除非逻辑错误，不然que_type一定是>0的
			if (allPai[0][0] > 0 && allPai[1][0] > 0 && allPai[2][0] > 0) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * 赖子能不能胡
	 *
	 * @param allPai
	 * @return
	 */
	public static List<List<Integer>> huLaizi(List<Integer> list, int laizi,Map<Integer,List<Integer>> show) {
		int[][] allPai = conversionType(list);
		int num = allPai[laizi / 10 - 1][laizi % 10];
		allPai[laizi / 10 - 1][laizi % 10] = 0;
		allPai[laizi / 10 - 1][0] = allPai[laizi / 10 - 1][0] - num;
		List<Integer> tingTable = quePai(allPai);
		tingTable.add(laizi);
		tingTable.addAll(CDMahJongRule.quePai2(allPai, laizi, num));
		List<List<Integer>> huData = new ArrayList<List<Integer>>();
		if (num == 1) {
			for (Integer pai : tingTable) {
				int yu = pai / 10 - 1;
				int mod = pai % 10;
				allPai[yu][mod] = allPai[yu][mod] + 1;
				allPai[yu][0] = allPai[yu][0] + 1;
				if (fitHu(allPai,show)) {
					List<Integer> data = new ArrayList<Integer>();
					data.add(pai);
					huData.add(data);
				}
				allPai[yu][mod] = allPai[yu][mod] - 1;
				allPai[yu][0] = allPai[yu][0] - 1;
			}
		} else if (num == 2) {
			for (Integer pai1 : tingTable) {
				for (Integer pai2 : tingTable) {
					int yu1 = pai1 / 10 - 1;
					int mod1 = pai1 % 10;
					int yu2 = pai2 / 10 - 1;
					int mod2 = pai2 % 10;
					allPai[yu1][mod1] = allPai[yu1][mod1] + 1;
					allPai[yu2][mod2] = allPai[yu2][mod2] + 1;
					allPai[yu1][0] = allPai[yu1][0] + 1;
					allPai[yu2][0] = allPai[yu2][0] + 1;
					// ystem.out.println("pai1"+pai1+"pai2:"+pai2);
					if (pai1 == 16 && pai2 == 37) {
						int i = 0;
					}
					if (fitHu(allPai,show)) {
						List<Integer> data = new ArrayList<Integer>();
						data.add(pai1);
						data.add(pai2);
						huData.add(data);
					}
					allPai[yu1][mod1] = allPai[yu1][mod1] - 1;
					allPai[yu2][mod2] = allPai[yu2][mod2] - 1;
					allPai[yu1][0] = allPai[yu1][0] - 1;
					allPai[yu2][0] = allPai[yu2][0] - 1;
				}
			}
		} else if (num == 3) {
			for (Integer pai1 : tingTable) {
				for (Integer pai2 : tingTable) {
					for (Integer pai3 : tingTable) {
						int yu1 = pai1 / 10 - 1;
						int mod1 = pai1 % 10;
						int yu2 = pai2 / 10 - 1;
						int mod2 = pai2 % 10;
						int yu3 = pai3 / 10 - 1;
						int mod3 = pai3 % 10;
						allPai[yu1][mod1] = allPai[yu1][mod1] + 1;
						allPai[yu2][mod2] = allPai[yu2][mod2] + 1;
						allPai[yu3][mod3] = allPai[yu3][mod3] + 1;

						allPai[yu1][0] = allPai[yu1][0] + 1;
						allPai[yu2][0] = allPai[yu2][0] + 1;
						allPai[yu3][0] = allPai[yu3][0] + 1;
						if (fitHu(allPai,show)) {
							List<Integer> data = new ArrayList<Integer>();
							data.add(pai1);
							data.add(pai2);
							data.add(pai3);
							huData.add(data);
						}
						allPai[yu1][mod1] = allPai[yu1][mod1] - 1;
						allPai[yu2][mod2] = allPai[yu2][mod2] - 1;
						allPai[yu3][mod3] = allPai[yu3][mod3] - 1;

						allPai[yu1][0] = allPai[yu1][0] - 1;
						allPai[yu2][0] = allPai[yu2][0] - 1;
						allPai[yu3][0] = allPai[yu3][0] - 1;
					}
				}
			}
		} else if (num == 4) {
			for (Integer pai1 : tingTable) {
				for (Integer pai2 : tingTable) {
					for (Integer pai3 : tingTable) {
						for (Integer pai4 : tingTable) {
							int yu1 = pai1 / 10 - 1;
							int mod1 = pai1 % 10;
							int yu2 = pai2 / 10 - 1;
							int mod2 = pai2 % 10;
							int yu3 = pai3 / 10 - 1;
							int mod3 = pai3 % 10;
							int yu4 = pai4 / 10 - 1;
							int mod4 = pai4 % 10;
							allPai[yu1][mod1] = allPai[yu1][mod1] + 1;
							allPai[yu2][mod2] = allPai[yu2][mod2] + 1;
							allPai[yu3][mod3] = allPai[yu3][mod3] + 1;
							allPai[yu4][mod4] = allPai[yu4][mod4] + 1;

							allPai[yu1][0] = allPai[yu1][0] + 1;
							allPai[yu2][0] = allPai[yu2][0] + 1;
							allPai[yu3][0] = allPai[yu3][0] + 1;
							allPai[yu4][0] = allPai[yu4][0] + 1;
							if (fitHu(allPai,show)) {
								List<Integer> data = new ArrayList<Integer>();
								data.add(pai1);
								data.add(pai2);
								data.add(pai3);
								data.add(pai4);
								huData.add(data);
							}
							allPai[yu1][mod1] = allPai[yu1][mod1] - 1;
							allPai[yu2][mod2] = allPai[yu2][mod2] - 1;
							allPai[yu3][mod3] = allPai[yu3][mod3] - 1;
							allPai[yu4][mod4] = allPai[yu4][mod4] - 1;

							allPai[yu1][0] = allPai[yu1][0] - 1;
							allPai[yu2][0] = allPai[yu2][0] - 1;
							allPai[yu3][0] = allPai[yu3][0] - 1;
							allPai[yu4][0] = allPai[yu4][0] - 1;
						}
					}
				}
			}
		}

		return huData;
	}

	/**
	 * 是否带顺序的牌及其听牌
	 * 
	 * @param allPai
	 * @param pai
	 * @param tingTable
	 * @return
	 */
	private static boolean shunFilter(int[][] allPai, int pai,
			List<Integer> tingTable) {
		int yu = pai / 10 - 1;
		int mod = pai % 10;
		// 是否有对子
		int size = allPai[yu][mod];
		boolean hasShun = false;
		// 边张判断
		if (mod == 9) {
			if (allPai[yu][mod - 1] >= 1 && allPai[yu][mod - 2] >= 1) {
				hasShun = true;
				if (size >= 2 && allPai[yu][mod - 1] >= 2) {
					if (!tingTable.contains(pai - 2)) {
						tingTable.add(pai - 2);
					}
				}
				if (size >= 2 && allPai[yu][mod - 2] >= 2) {
					if (!tingTable.contains(pai - 1)) {
						tingTable.add(pai - 1);
					}
				}

			} else if (allPai[yu][mod - 1] >= 1) {
				if (!tingTable.contains(pai - 2)) {
					tingTable.add(pai - 2);
				}
				hasShun = true;
			} else if (allPai[yu][mod - 2] >= 1) {
				if (!tingTable.contains(pai - 1)) {
					tingTable.add(pai - 1);
				}

				if (size == 1) {
					if (!tingTable.contains(pai)) {
						tingTable.add(pai);
					}
				}

				hasShun = true;

			} else {
				if (size == 1) {
					if (!tingTable.contains(pai)) {
						tingTable.add(pai);
					}
				}
			}
		} else if (mod == 1) {
			if (allPai[yu][mod + 1] >= 1 && allPai[yu][mod + 2] >= 1) {
				hasShun = true;
				if (size >= 2 && allPai[yu][mod + 1] >= 2) {
					if (!tingTable.contains(pai + 2)) {
						tingTable.add(pai + 2);
					}
				}
				if (size >= 2 && allPai[yu][mod + 2] >= 2) {
					if (!tingTable.contains(pai + 1)) {
						tingTable.add(pai + 1);
					}
				}
			} else if (allPai[yu][mod + 1] >= 1) {
				if (!tingTable.contains(pai + 2)) {
					tingTable.add(pai + 2);
				}
				hasShun = true;
			} else if (allPai[yu][mod + 2] >= 1) {
				if (!tingTable.contains(pai + 1)) {
					tingTable.add(pai + 1);
				}
				if (size == 1) {
					if (!tingTable.contains(pai)) {
						tingTable.add(pai);
					}
				}
				hasShun = true;
			} else {
				if (size == 1) {
					if (!tingTable.contains(pai)) {
						tingTable.add(pai);
					}
				}
			}
		} else if (mod == 8) {
			if (allPai[yu][mod - 1] >= 1 && allPai[yu][mod - 2] >= 1) {
				hasShun = true;
				if (size >= 2 && allPai[yu][mod - 1] >= 2) {
					if (!tingTable.contains(pai - 2)) {
						tingTable.add(pai - 2);
					}
				}
				if (size >= 2 && allPai[yu][mod - 2] >= 2) {
					if (!tingTable.contains(pai - 1)) {
						tingTable.add(pai - 1);
					}
				}
			} else if (allPai[yu][mod - 1] >= 1 && allPai[yu][mod + 1] >= 1) {
				hasShun = true;
				if (size >= 2 && allPai[yu][mod - 1] >= 2) {
					if (!tingTable.contains(pai + 1)) {
						tingTable.add(pai + 1);
					}
				}
				if (size >= 2 && allPai[yu][mod + 1] >= 2) {
					if (!tingTable.contains(pai - 1)) {
						tingTable.add(pai - 1);
					}
				}
			} else if (allPai[yu][mod - 1] >= 1) {
				hasShun = true;
				if (!tingTable.contains(pai - 2)) {
					tingTable.add(pai - 2);
				}
				if (!tingTable.contains(pai + 1)) {
					tingTable.add(pai + 1);
				}

			} else if (allPai[yu][mod - 2] >= 1 || allPai[yu][mod + 1] >= 1) {
				hasShun = true;
				if (!tingTable.contains(pai - 1)) {
					tingTable.add(pai - 1);
				}
				if (size == 1) {
					if (!tingTable.contains(pai)) {
						tingTable.add(pai);
					}
				}
			} else {
				if (size == 1) {
					if (!tingTable.contains(pai)) {
						tingTable.add(pai);
					}
				}
			}
		} else if (mod == 2) {
			if (allPai[yu][mod + 1] >= 1 && allPai[yu][mod + 2] >= 1) {
				hasShun = true;
				if (size >= 2 && allPai[yu][mod + 1] >= 2) {
					if (!tingTable.contains(pai + 2)) {
						tingTable.add(pai + 2);
					}
				}
				if (size >= 2 && allPai[yu][mod + 2] >= 2) {
					if (!tingTable.contains(pai + 1)) {
						tingTable.add(pai + 1);
					}
				}
			} else if (allPai[yu][mod - 1] >= 1 && allPai[yu][mod + 1] >= 1) {
				hasShun = true;
				if (size >= 2 && allPai[yu][mod + 1] >= 2) {
					if (!tingTable.contains(pai - 1)) {
						tingTable.add(pai - 1);
					}
				}
				if (size >= 2 && allPai[yu][mod - 1] >= 2) {
					if (!tingTable.contains(pai + 1)) {
						tingTable.add(pai + 1);
					}
				}
			} else if (allPai[yu][mod + 1] >= 1) {
				hasShun = true;
				if (!tingTable.contains(pai + 2)) {
					tingTable.add(pai + 2);
				}
				if (!tingTable.contains(pai + 1)) {
					tingTable.add(pai + 1);
				}
			} else if (allPai[yu][mod + 2] >= 1 || allPai[yu][mod - 1] >= 1) {
				hasShun = true;
				if (!tingTable.contains(pai + 1)) {
					tingTable.add(pai + 1);
				}
				if (size == 1) {
					if (!tingTable.contains(pai)) {
						tingTable.add(pai);
					}
				}
			} else {
				if (size == 1) {
					if (!tingTable.contains(pai)) {
						tingTable.add(pai);
					}
				}
			}
		} else {
			if (allPai[yu][mod - 1] >= 1 && allPai[yu][mod + 1] >= 1) {
				hasShun = true;
				if (size >= 2 && allPai[yu][mod - 1] >= 2) {
					if (!tingTable.contains(pai + 1)) {
						tingTable.add(pai + 1);
					}
				}
				if (size >= 2 && allPai[yu][mod + 1] >= 2) {
					if (!tingTable.contains(pai - 1)) {
						tingTable.add(pai - 1);
					}
				}

			} else if (allPai[yu][mod - 1] >= 1 && allPai[yu][mod - 2] >= 1) {
				hasShun = true;
				if (size >= 2 && allPai[yu][mod - 1] >= 2) {
					if (!tingTable.contains(pai - 2)) {
						tingTable.add(pai - 2);
					}
				}
				if (size >= 2 && allPai[yu][mod - 2] >= 2) {
					if (!tingTable.contains(pai - 1)) {
						tingTable.add(pai - 1);
					}
				}
			} else if (allPai[yu][mod + 1] >= 1 && allPai[yu][mod + 2] >= 1) {
				hasShun = true;
				if (size >= 2 && allPai[yu][mod + 1] >= 2) {
					if (!tingTable.contains(pai + 2)) {
						tingTable.add(pai + 2);
					}
				}
				if (size >= 2 && allPai[yu][mod + 2] >= 2) {
					if (!tingTable.contains(pai + 1)) {
						tingTable.add(pai + 1);
					}
				}
			} else if (allPai[yu][mod - 1] >= 1) {
				hasShun = true;
				if (!tingTable.contains(pai + 1)) {
					tingTable.add(pai + 1);
				}
				if (!tingTable.contains(pai - 2)) {
					tingTable.add(pai - 2);
				}
			} else if (allPai[yu][mod + 1] >= 1) {
				hasShun = true;
				if (!tingTable.contains(pai + 2)) {
					tingTable.add(pai + 2);
				}
				if (!tingTable.contains(pai - 1)) {
					tingTable.add(pai - 1);
				}
			} else if (allPai[yu][mod - 2] >= 1) {
				if (!tingTable.contains(pai - 1)) {
					tingTable.add(pai - 1);
				}
				if (size == 1) {
					if (!tingTable.contains(pai)) {
						tingTable.add(pai);
					}
				}
			} else if (allPai[yu][mod + 2] >= 1) {
				if (!tingTable.contains(pai + 1)) {
					tingTable.add(pai + 1);
				}
				if (size == 1) {
					if (!tingTable.contains(pai)) {
						tingTable.add(pai);
					}
				}
			} else {
				if (size == 1) {
					if (!tingTable.contains(pai)) {
						tingTable.add(pai);
					}
				}
			}
		}
		return hasShun;
	}

	/**
	 * 麻将缺什么牌 型完整
	 * 
	 * @param allPai
	 * @return
	 */
	public static List<Integer> quePai(int[][] allPai) {

		int pai = 0;
		List<Integer> tingTable = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			List<Integer> duizi = new ArrayList<Integer>();
			for (int j = 1; j <= 9; j++) {

				pai = 10 * i + j;
				int yu = pai / 10 - 1;
				int mod = pai % 10;
				// 是否有对子
				int size = allPai[yu][mod];
				if (size == 0) {
					continue;
				}

				boolean hasShun = shunFilter(allPai, pai, tingTable);

				if (size == 2 && !hasShun) {
					duizi.add(pai);
				}
				if (size == 2 && hasShun) {
					if (!tingTable.contains(pai)) {
						tingTable.add(pai);
					}
				}
				
				if (size == 2) {
					if (!tingTable.contains(pai)) {
						tingTable.add(pai);
					}
				}
				if (size == 3 && hasShun) {
					duizi.add(pai);
				}
			}
			if (duizi.size() > 1) {
				for (Integer data : duizi) {
					if (!tingTable.contains(data)) {
						tingTable.add(data);
					}
				}
			}
		}
		// 连续牌缺牌的整体判断
		for (int i = 1; i <= 3; i++) {
			Map<Integer, Integer> shun = new HashMap<Integer, Integer>();
			int max = 0;
			int start = 0;
			int yu = i - 1;
			for (int j = 1; j <= 9; j++) {
				int next = 1;
				start = j;
				for (int k = j; k <= 8; k++) {
					if (allPai[yu][k] > 0 && allPai[yu][k + 1] > 0) {
						next++;
					} else {
						break;
					}
				}
				if (next > 3) {
					shun.put(start, next);
				}
				if (next > max) {
					max = next;
					
				}
			}
			for (Map.Entry<Integer, Integer> entry : shun.entrySet()) {

				for (int k = 0; k < entry.getValue(); k++) {
					pai = 10 * i + entry.getKey() + k;
					if (!tingTable.contains(pai)) {
						tingTable.add(pai);
					}
				}
				if (entry.getKey() > 1) {
					pai = 10 * i + entry.getKey() - 1;
					if (!tingTable.contains(pai)) {
						tingTable.add(pai);
					}
				}
				int end = entry.getKey() + entry.getValue();
				if (end < 10) {
					pai = 10 * i + end;
					if (!tingTable.contains(pai)) {
						tingTable.add(pai);
					}
				}

			}
			/*System.out
					.println(shun.size() + "start:" + start + " -- max" + max);*/
			shun.clear();

		}
		//System.out.println("缺牌" + tingTable);
		return tingTable;
	}
	/*
	 * 带癞子的七小对的缺牌
	 */
	
	public static List<Integer> quePai2(int[][] allPai , int laizi ,int laizi_num) {

		int pai = 0;
		int duizi_num = 0;
		int three_size = 0;
		//int laizi_num = allPai[laizi / 10 - 1][laizi % 10];//癞子个数
	//	allPai[laizi / 10 - 1][laizi % 10] = 0;
		//allPai[laizi / 10 - 1][0] = allPai[laizi / 10 - 1][0] - laizi_num;
		List<Integer> tingTable = new ArrayList<Integer>();
		List<Integer> singlePai = new ArrayList<Integer>();//单张牌
		for (int i = 1; i <= 3; i++) {
			for (int j = 1; j <= 9; j++) {
				pai = 10 * i + j;
				int yu = pai / 10 - 1;
				int mod = pai % 10;
				// 是否有对子
				int size = allPai[yu][mod];
				if (size == 0) {
					continue;
				}
				if (size == 1) {
					singlePai.add(pai);
				}
				if (size == 2 ) {
					duizi_num ++;
				}
			}
		}
		if (laizi_num == 1 && duizi_num == 6) {
			tingTable.addAll(singlePai);
		}else if (laizi_num == 2 && duizi_num == 5) {
			tingTable.addAll(singlePai);
		}else if (laizi_num == 3 && duizi_num == 4) {
			tingTable.addAll(singlePai);
		}else if (laizi_num == 4 && duizi_num == 3 ) {
			tingTable.addAll(singlePai);
		}
		
		return tingTable;
	}

	public static void main(String[] args) {
		List<Integer> allPai = new LinkedList<>();
		for (int i = 11; i < 40; i++) {
			if (i % 10 == 0) {
				continue;
			}
			for (int j = 0; j < 4; j++) {
				allPai.add(i);
			}
		}
		// 洗牌 打乱顺序
		Collections.shuffle(allPai);
		List<Integer> pais = new ArrayList<Integer>();
		for (int i = 0; i < 13; i++) {
			Integer remove = allPai.remove(0);
			pais.add(remove);
		}
		System.out.println((19 / 10 * 10 + 1) + "所有牌" + pais);

		for (Integer data : allPai) {
			System.out.println("缺牌" + quePai(conversionType(pais)));
			Integer result = chosePai(pais, data, 0);
			System.out.println("加牌" + data);
			System.out.println("打牌" + result);
			pais.add(data);
			pais.remove(result);
			System.out.println("所有牌" + pais);
			huLaizi(pais, 11,new HashMap<Integer, List<Integer>>());
		}
		List<Integer> aaa = new ArrayList<Integer>();

		int[] array ={23,13,14,24,24,24,25,26,33,33};
		for(int data:array){
			aaa.add(data);
		}
		System.out.println("能胡吗" + aaa);
		System.out.println("能胡吗" + huLaizi(aaa, 23,new HashMap<Integer, List<Integer>>()));

//		List<Integer> ddd = tingPai(conversionType(aaa));
//		int ccc=tingPai(conversionType(aaa)).size();
		//System.out.println(ccc);
	}

	/**
	 * 能否飞杠
	 *
	 * @param maJongTable
	 * @param member
	 */
	public static boolean canFreeGang(int laizinum, int lastPai,
			List<Integer> memberShouPai) {
		int free = laizinum;
		if (free == lastPai) {
			return false;
		}
		int[][] memberPai = CDMahJongRule.conversionType(memberShouPai);
		// 已有牌的数量
		int yetNum = memberPai[lastPai / 10 - 1][lastPai % 10];
		int freeNum = memberPai[free / 10 - 1][free % 10];
		if (freeNum > 0 && yetNum >= 2) {
			return true;
		}
		return false;
	}

	/**
	 * 直接检测 定缺 类型
	 * 
	 * @param allPai
	 * @return
	 */
	public static boolean isQueYiMen(int[][] allPai, int que_type) {
		if (que_type == 0){
			// 除非逻辑出错，不然一定设置了缺一门类型
			return false;
		}
		return allPai[que_type - 1][0] == 0 ? true : false;
	}

	/**
	 * 缺的类型下标
	 * 
	 * @param allPai
	 * @return
	 */
	public static int choseQueType(int[][] allPai) {
		if (allPai[0][0] == 0) {
			return 0;
		}
		if (allPai[1][0] == 0) {
			return 1;
		}
		if (allPai[2][0] == 0) {
			return 2;
		}

		int index = 0;
		for (int count = 0; count < 2; count++) {
			int last = allPai[index][0];
			int next = allPai[count + 1][0];
			if (next < last) {
				index = count + 1;
			}
		}
		return index;
	}

	/**
	 * 选择牌
	 *
	 * @param allPai
	 * @return
	 */
	public static int chosePai(List<Integer> list, int start, int que_pai_type) {
		int[][] allPai = conversionType(list);
		// 先打定缺的
		if (que_pai_type > 0) {
			for (int j = 1; j <= 9; j++) {
				if (allPai[que_pai_type - 1][j] > 0) {
					return (int)(10 * que_pai_type + j);
				}
			}
		}
		
		int result = start;
		int priority = -1;
		for (int i = 1; i <= 3; i++) {
			for (int j = 1; j <= 9; j++) {
				int pai = 10 * i + j;
				int yu = pai / 10 - 1;
				int mod = pai % 10;
				int yuShu = allPai[yu][0] % 3;
				// 是否有对子
				int size = allPai[yu][mod];
				if (size == 0) {
					continue;
				}
				int curPriority = 1;
				// 是否有顺序
				boolean hasShun = false;
				if (mod == 9) {
					if (allPai[yu][mod - 1] >= 1 && allPai[yu][mod - 2] >= 1) {
						curPriority = 4;
						hasShun = true;
					} else if (allPai[yu][mod - 1] >= 1
							|| allPai[yu][mod - 2] >= 1) {
						curPriority = 1;
					} else {
						curPriority = 0;
					}
				} else if (mod == 1) {
					if (allPai[yu][mod + 1] >= 1 && allPai[yu][mod + 2] >= 1) {
						curPriority = 4;
						hasShun = true;
					} else if (allPai[yu][mod + 1] >= 1
							|| allPai[yu][mod + 2] >= 1) {
						curPriority = 1;
					} else {
						curPriority = 0;
					}
				} else if (mod == 8) {
					if (allPai[yu][mod - 1] >= 1 && allPai[yu][mod - 2] >= 1) {
						curPriority = 4;
						hasShun = true;
					} else if (allPai[yu][mod - 1] >= 1
							&& allPai[yu][mod + 1] >= 1) {
						curPriority = 4;
						hasShun = true;
					} else if (allPai[yu][mod - 1] >= 1) {
						curPriority = 3;
					} else if (allPai[yu][mod - 2] >= 1
							|| allPai[yu][mod + 1] >= 1) {
						curPriority = 1;
					} else {
						curPriority = 0;
					}
				} else if (mod == 2) {
					if (allPai[yu][mod + 1] >= 1 && allPai[yu][mod + 2] >= 1) {
						curPriority = 4;
						hasShun = true;
					} else if (allPai[yu][mod - 1] >= 1
							&& allPai[yu][mod + 1] >= 1) {
						curPriority = 4;
						hasShun = true;
					} else if (allPai[yu][mod + 1] >= 1) {
						curPriority = 3;
					} else if (allPai[yu][mod + 2] >= 1
							|| allPai[yu][mod - 1] >= 1) {
						curPriority = 1;
					} else {
						curPriority = 0;
					}
				} else {
					if (allPai[yu][mod - 1] >= 1 && allPai[yu][mod + 1] >= 1) {
						curPriority = 4;
						hasShun = true;
					} else if (allPai[yu][mod - 1] >= 1
							&& allPai[yu][mod - 2] >= 1) {
						curPriority = 4;
						hasShun = true;
					} else if (allPai[yu][mod + 1] >= 1
							&& allPai[yu][mod + 2] >= 1) {
						curPriority = 4;
						hasShun = true;
					} else if (allPai[yu][mod - 1] >= 1
							|| allPai[yu][mod + 1] >= 1) {
						curPriority = 3;
					} else if (allPai[yu][mod - 2] >= 1
							|| allPai[yu][mod + 2] >= 1) {
						curPriority = 1;
					} else {
						curPriority = 0;
					}
				}
				if (size > 2 && hasShun) {
					curPriority = 3;
				}
				if (size > 2) {
					curPriority = 4;
				} else if (size > 1 && hasShun) {
					curPriority = 1;
				} else if (size > 1) {
					curPriority = curPriority > 1 ? curPriority : 2;
				} else if (size > 0 && hasShun) {

				} else {
					// 啥都不是
					// curPriority = curPriority>1?curPriority:0;
				}

				if (priority == -1) {
					priority = curPriority;
					result = pai;
					continue;
				}

				if (curPriority >= priority) { // 这个牌有价值
					/*
					 * if (result == pai && priority == 1) { priority =
					 * curPriority; }
					 */
					continue;
				} else {
					priority = curPriority;
					result = pai;
				}
			}
		}
		return result;

	}

	/**
	 * 胡牌
	 * 龙七对可以看做巧七对的一种
	 * @param allPai
	 * @return
	 */
	public static boolean duiduiHu(int[][] allPai) {
		// 对对胡
		boolean isDuizi = true;
		for (int i = 0; i < 3; i++) {
			for (int j = 1; j <= 9; j++) {
				if (allPai[i][j] > 0 && (allPai[i][j] != 2&&allPai[i][j] != 4)) {//4为龙7对的判定
					isDuizi = false;
					break;
				}
			}
			if (!isDuizi) {
				break;
			}
		}
		if (isDuizi) {
			return true;
		}
		return false;
	}

	/**
	 * 胡手牌
	 *
	 * @param allPai
	 * @return
	 */
	public static boolean huPai(int[][] allPai) {
		int jiangPos = 0;// 哪一组牌中存在将
		int yuShu = 0;
		boolean jiangExisted = false;
		for (int i = 0; i < 3; i++) {
			yuShu = allPai[i][0] % 3;
			if (yuShu == 1) {
				return false;
			}
			if (yuShu == 2) {
				if (jiangExisted) {
					return false;
				}
				jiangPos = i;
				jiangExisted = true;
			}
		}

		for (int i = 0; i < 3; i++) {
			if (i != jiangPos) {
				int[] temp = new int[10];
				for (int j = 0; j <= 9; j++) {
					temp[j] = allPai[i][j];
				}
				/*
				 * if (0 != analyze(temp).length) { return false; }
				 */
				if (!analyze(temp, i == 3)) {
					return false;
				}
			}
		}

		boolean success = false;
		for (int j = 1; j <= 9; j++) {
			if (allPai[jiangPos][j] >= 2) {
				allPai[jiangPos][j] = allPai[jiangPos][j] - 2;
				allPai[jiangPos][0] = allPai[jiangPos][0] - 2;

				int[] temp = new int[10];
				for (int k = 0; k <= 9; k++) {
					temp[k] = allPai[jiangPos][k];
				}
				if (analyze(temp, jiangPos == 3)) {
					success = true;
				}
				allPai[jiangPos][j] = allPai[jiangPos][j] + 2;
				allPai[jiangPos][0] = allPai[jiangPos][0] + 2;
				if (success) {
					break;
				}
			}
		}
		return success;
	}
	
	/**
	 * 胡加七小对
	 * @param table
	 * @param role
	 * @return
	 */
	public static boolean fitHu(int[][] memberPai, Map<Integer,List<Integer>> show){
		boolean huPai=false;
		if (CDMahJongRule.huPai(memberPai)) {
			huPai = true;
		} else {
			if (show == null || show.size() == 0) {
				if (CDMahJongRule.duiduiHu(memberPai)){
					huPai = true;
					
				}
			}
			
		}
		return huPai;
	}

	/**
	 * 分析每一种牌是否符合
	 *
	 * @param aKindPai
	 * @return
	 */
	public static boolean analyze(int[] aKindPai, boolean ziPai) {

		if (aKindPai[0] == 0) {
			return true;
		}
		int index = 0;
		for (int i = 1; i <= 9; i++) {
			if (aKindPai[i] != 0) {
				index = i;
				break;
			}
		}
		boolean result = false;
		if (aKindPai[index] >= 3) {
			aKindPai[index] = aKindPai[index] - 3;
			aKindPai[0] = aKindPai[0] - 3;
			int[] temp = new int[10];
			// System.arraycopy(aKindPai, 0, temp, 0, aKindPai.length);
			result = analyze(aKindPai, ziPai);
			aKindPai[index] = aKindPai[index] + 3;
			aKindPai[0] = aKindPai[0] + 3;
			return result;
		}

		if (!ziPai && index < 8 && aKindPai[index + 1] > 0
				&& aKindPai[index + 2] > 0) {
			aKindPai[index] = aKindPai[index] - 1;
			aKindPai[index + 1] = aKindPai[index + 1] - 1;
			aKindPai[index + 2] = aKindPai[index + 2] - 1;
			aKindPai[0] = aKindPai[0] - 3;
			int[] temp = new int[10];
			// System.arraycopy(aKindPai, 0, temp, 0, aKindPai.length);
			result = analyze(aKindPai, ziPai);
			aKindPai[index] = aKindPai[index] + 1;
			aKindPai[index + 1] = aKindPai[index + 1] + 1;
			aKindPai[index + 2] = aKindPai[index + 2] + 1;
			aKindPai[0] = aKindPai[0] + 3;
			return result;
		}
		return false;
	}

	/**
	 * 杠
	 *
	 * @param allPai
	 * @param pai
	 * @param type
	 * @return
	 */
	public static boolean gangPai(int[][] allPai, int pai, int type) {

		int idx = pai / 10;
		int pos = pai % 10;

		switch (type) {
		case 1: {// 暗杠
			int yetPaiNum = allPai[idx - 1][pos];
			return yetPaiNum == 4;
		}
		case 2: {// 明杠
			int yetPaiNum = allPai[idx - 1][pos];
			return yetPaiNum == 3;
		}
		}
		return false;
	}

	public boolean huPai(int[][] allPai, int laizi) {

		int wang = allPai[0][0];
		int tong = allPai[1][0];
		int tiao = allPai[2][0];
		if (wang > 0 && tong > 0 && tiao > 0) {
			// 花猪不能胡牌
			return false;
		}
		int allPaiNum = wang + tong + tiao + laizi;
		if (allPaiNum % 3 != 2) {
			// 不能满足3n+2的数量要求
			return false;
		}

		int jiangPos = 0;// 哪一组牌中存在将
		int yuShu = 0;
		boolean jiangExisted = false;
		for (int i = 0; i < 3; i++) {
			yuShu = allPai[i][0] % 3;
			if (yuShu == 1) {
				return false;
			}
			if (yuShu == 2) {
				if (jiangExisted) {
					return false;
				}
				jiangPos = i;
				jiangExisted = true;
			}
		}

		if (wang > 0) {
			int[] temp = new int[10];
			for (int j = 0; j <= 9; j++) {
				// temp[j] = allPai[i][j];
			}
			if (!analyze(temp, false)) {
				return false;
			}
		}

		boolean success = false;
		for (int j = 1; j <= 9; j++) {
			if (allPai[jiangPos][j] >= 2) {
				allPai[jiangPos][j] = allPai[jiangPos][j] - 2;
				allPai[jiangPos][0] = allPai[jiangPos][0] - 2;

				int[] temp = new int[10];
				for (int k = 0; k <= 9; k++) {
					temp[k] = allPai[jiangPos][k];
				}
				if (analyze(temp, false)) {
					success = true;
				}
				allPai[jiangPos][j] = allPai[jiangPos][j] + 2;
				allPai[jiangPos][0] = allPai[jiangPos][0] + 2;
				if (success) {
					break;
				}
			}
		}
		return success;
	}

	/**
	 * 注：清一色(整副牌,包括吃、碰、杠) 原理：判断第一张牌和剩余牌的首个数字(1为万,2为筒,3为索)
	 * 
	 * @param list传所有牌
	 * @return
	 */
	public static boolean judgeFlush(List<Integer> list) {
		String fistChar = String.valueOf(list.get(0)).substring(0, 1);
		
		for (Integer integer : list) {
			if (!String.valueOf(integer).substring(0, 1).equals(fistChar)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 大对子 11、111、111、111、111的牌型(有过碰,杠的牌都算,不需要考虑碰，杠)
	 * 判断原理：1张false,2张只能出现一次（避免七对胡）,4张false,剩余为3张
	 * 
	 * @param list传剩余手牌
	 * @return
	 */
	public static boolean judgeFourTriple(List<Integer> list) {
		HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
		for (Integer integer : list) {
			if (hm.get(integer) == null) {
				hm.put(integer, 1);
			} else {
				hm.put(integer, hm.get(integer) + 1);
			}
		}
		int twoNum = 0;
		for (Map.Entry<Integer, Integer> entry : hm.entrySet()) {
			// 只能出现2张或3张
			if (entry.getValue() == 2 || entry.getValue() == 3) {
				if (entry.getValue() == 2) {
					twoNum++;
					if (twoNum >= 2) {
						// 2张只能出现一次
						return false;
					}
				}
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * 巧7对11、22、33、44、55、66、77 原理：手上的牌需要14张 7对子必然有7张不同的牌
	 * 
	 * @param list传手牌
	 * @return
	 */
	public static boolean judegeSevenPairs(List<Integer> list) {
		if (list.size() == 14) {
			HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
			for (Integer integer : list) {
				if (hm.get(integer) == null) {
					hm.put(integer, 1);
				} else {
					hm.put(integer, hm.get(integer) + 1);
				}
			}
			// 遍历map
			for (Map.Entry<Integer, Integer> entry : hm.entrySet()) {
				if (entry.getValue() != 2) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 龙7对：11、22、33、44、55、6666(14张牌全是对子，其中还要有四张一样的)
	 * 
	 * @param list传手牌
	 * @return
	 */
	public static boolean judegeLongSevenPairs(List<Integer> list) {
		if (list.size() == 14) {
			HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
			for (Integer integer : list) {
				if (hm.get(integer) == null) {
					hm.put(integer, 1);
				} else {
					hm.put(integer, hm.get(integer) + 1);
				}
			}
			for (Map.Entry<Integer, Integer> entry : hm.entrySet()) {
				if (entry.getValue() % 2 != 0) {
					return false;
				}
			}
			for (Map.Entry<Integer, Integer> entry : hm.entrySet()) {
				if (entry.getValue() == 4) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

	/**
	 * 判断勾胡（判断show牌）
	 * 
	 * @return 杠的数目
	 */
	public static int judegeWithGang(Map<Integer, List<Integer>> map) {

		int gang_num = 0;
		for (Entry<Integer, List<Integer>> entry : map.entrySet()) {
			if (entry.getValue().size() >= 4) {
				gang_num++;
			}
		}
		return gang_num;
	}
	
	public static Integer moPai(List<Integer> shouPaiList,List<Integer> pais){
		Integer moPai=0;
		List<Integer> pai = shouPaiList;
		List<Integer> tablePais = pais;
		List<Integer> randomList=new ArrayList<Integer>();
		randomList=moPaiOne(shouPaiList);
		moPai=fitMoPai(pai,randomList,tablePais);
		if(moPai!=0){
			return moPai;
		}
		randomList.clear();
		randomList=moPaiTwo(shouPaiList);
		moPai=fitMoPai(pai,randomList,tablePais);
		if(moPai!=0){
			return moPai;
		}
		randomList.clear();
		randomList=moPaiThree(shouPaiList);
		moPai=fitMoPai(pai,randomList,tablePais);
		if(moPai!=0){
			return moPai;
		}
		randomList.clear();
		randomList=moPaiFour(shouPaiList);
		moPai=fitMoPai(pai,randomList,tablePais);
		if(moPai!=0){
			return moPai;
		}
		randomList.clear();
		randomList=moPaiFive(shouPaiList);
		moPai=fitMoPai(pai,randomList,tablePais);
		if(moPai!=0){
			return moPai;
		}
		return moPai;
	}
	
	//给玩家随机发一个手上牌数最多的那个牌型的牌，如果牌堆里面没有这种牌，则给玩家随机发一个手上牌数第二多的那个牌型的牌
	public static Integer randomMoPai(List<Integer> shouPaiList, List<Integer> tablePais){
		Integer moPai = 0;
		//得到玩家各种牌型的总数 排序  选第一 第二
		int[][] shouPaiArr = CDMahJongRule.conversionType(shouPaiList);
		// 手牌的数量
		int wangShouNum = shouPaiArr[0][0];
		int tongShouNum = shouPaiArr[1][0];
		int tiaoShouNum = shouPaiArr[2][0];
		
		int wang = 1;
		int tong = 2;
		int tiao = 3;

		HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
		hm.put(wang, wangShouNum);
		hm.put(tong, tongShouNum);
		hm.put(tiao, tiaoShouNum);
		// 对hm进行排序返回
		Map<Integer, Integer> hm2 = CDMahJongRule.sortMapByValue(hm);
		// list存储table牌 万，筒，条剩余的数量
		List<Integer> wanglist = new ArrayList<Integer>();
		List<Integer> tonglist = new ArrayList<Integer>();
		List<Integer> tiaolist = new ArrayList<Integer>();
		
		for (Integer integer : tablePais) {
				if (integer >= 11 && integer < 20) {
					wanglist.add(integer);
				} else if (integer >= 21 && integer < 30) {
					tonglist.add(integer);
				} else if (integer >= 31 && integer < 40) {
					tiaolist.add(integer);
				}
		}
		int times = 0;//2次
		for (Entry<Integer, Integer> entry : hm2.entrySet() ) {
			if (times == 2) {
				break;
			}
			Integer key = entry.getKey();// 键值
			switch (key) {
			case 1:
				if (wanglist.size() > 0) {
					moPai = Probability.getRand(wanglist);
				}
				break;
			case 2:
				if (tonglist.size() > 0) {
					moPai = Probability.getRand(tonglist);
				}
				break;
			case 3:
				if (tiaolist.size() > 0) {
					moPai = Probability.getRand(tiaolist);
				}
				break;
			default:
				break;
			}
			if (moPai != 0) {
				return moPai;
			}
			times++;
		}
		//
		return moPai;
	}
	public static Integer fitMoPai(List<Integer> shouPaiList,List<Integer> randomList,List<Integer> tablePais){		
		Integer moPai=0;	
		int[][] shouPaiArr = CDMahJongRule.conversionType(shouPaiList);
		// 手牌最大的数量
		int wangShouNum = shouPaiArr[0][0];
		int tongShouNum = shouPaiArr[1][0];
		int tiaoShouNum = shouPaiArr[2][0];
		
		int wang = 1;
		int tong = 2;
		int tiao = 3;

		HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
		hm.put(wang, wangShouNum);
		hm.put(tong, tongShouNum);
		hm.put(tiao, tiaoShouNum);
		// 对hm进行排序返回
		Map<Integer, Integer> hm2 = CDMahJongRule.sortMapByValue(hm);
		// list存储万，筒，条的数量
		List<Integer> wanglist = new ArrayList<Integer>();
		List<Integer> tonglist = new ArrayList<Integer>();
		List<Integer> tiaolist = new ArrayList<Integer>();
		
		for (Integer integer : tablePais) {
			if(randomList.contains(integer)){
				if (integer >= 11 && integer < 20) {
					wanglist.add(integer);
				} else if (integer >= 21 && integer < 30) {
					tonglist.add(integer);
				} else if (integer >= 31 && integer < 40) {
					tiaolist.add(integer);
				}
			}
		}
		
		System.out.println("万" + wanglist);
		System.out.println("筒" + tonglist);
		System.out.println("条" + tiaolist);
		
		for (Map.Entry<Integer, Integer> entry : hm2.entrySet()) {
			Integer key = entry.getKey();// 键值
			switch (key) {
			case 1:
				if (wanglist.size() > 0) {
					moPai = Probability.getRand(wanglist);
				}
				break;
			case 2:
				if (tonglist.size() > 0) {
					moPai = Probability.getRand(tonglist);
				}
				break;
			case 3:
				if (tiaolist.size() > 0) {
					moPai = Probability.getRand(tiaolist);
				}
				break;
			default:
				break;
			}
		}
		return moPai;
	}

	// 如 4 6筒中的缺五筒 1 3万中缺二万 （卡牌） 没有听牌的套路发牌1
	public static List<Integer> moPaiOne(List<Integer> shouPaiList) {
		List<Integer> randomList = new ArrayList<Integer>();
		List<Integer> pai = shouPaiList;
		int[][] paiArr = CDMahJongRule.conversionType(pai);
		// 缺牌list
		List<Integer> quePai = CDMahJongRule.quePai((paiArr));
		for (Integer integer : quePai) {
			int front = integer / 10 - 1;
			int next = integer % 10;
			int num = paiArr[front][next];
			//System.out.println("front=" + front + "next=" + next + "num" + num);
			// 分别是1条，1万，1筒，9条，9万，9筒不符合
			if (next != 1 && next != 9) {
				if (paiArr[front][next - 1] >= 1
						&& paiArr[front][next + 1] >= 1) {
					//System.out.println("符合" + integer);
					randomList.add(integer);
				}
			}
		}
		return randomList;
	}
	
	
	// 如89筒缺7筒，12筒缺3筒，条万同理（偏章）没有听牌的套路发牌2
	public static List<Integer> moPaiTwo(List<Integer> shouPaiList) {
		List<Integer> randomList = new ArrayList<Integer>();
		List<Integer> pai = shouPaiList;
		int[][] paiArr = CDMahJongRule.conversionType(pai);
		// 缺牌list
		List<Integer> quePai = CDMahJongRule.quePai((paiArr));
		for (Integer integer : quePai) {
			int front = integer / 10 - 1;
			int next = integer % 10;
			int num = paiArr[front][next];
			//System.out.println("front=" + front + "next=" + next + "num" + num);
			// 只有3筒，7筒，3万，7万， 3条，7条符合要求
			if (next == 3) {
				if (paiArr[front][next - 1] >= 1
						&& paiArr[front][next - 2] >= 1) {
					System.out.println("符合" + integer);
					randomList.add(integer);
				}
			} else if (next == 7) {
				if (paiArr[front][next + 1] >= 1
						&& paiArr[front][next + 2] >= 1) {
					System.out.println("符合" + integer);
					randomList.add(integer);
				}
			}
		}
		return randomList;
	}
	
	// 偏章，5689筒缺4，7筒，1245筒缺3，6筒，同时去掉偏章89筒和12筒的影响
	public static List<Integer> moPaiThree(List<Integer> shouPaiList) {
		List<Integer> randomList = new ArrayList<Integer>();
		List<Integer> pai = shouPaiList;
		int[][] paiArr = CDMahJongRule.conversionType(pai);
		// 所缺的牌list
		List<Integer> quePai = CDMahJongRule.quePai((paiArr));
		for (Integer integer : quePai) {
			int front = integer / 10 - 1;
			int next = integer % 10;
			int num = paiArr[front][next];
			//System.out.println("front=" + front + "next=" + next + "num" + num);
			if (next == 1 || next == 2 || next == 3) {
				if (paiArr[front][next + 1] >= 1 && paiArr[front][next + 2] > 0) {
					//System.out.println("符合" + integer);
					randomList.add(integer);
				}
			} else if (next == 7 || next == 8 || next == 9) {
				if (paiArr[front][next - 1] > 0 && paiArr[front][next - 2] > 0) {
					//System.out.println("符合" + integer);
					randomList.add(integer);
				}
			} else {
				if ((paiArr[front][next - 1] > 0 && paiArr[front][next - 2] > 0)
						|| paiArr[front][next + 1] >= 1
						&& paiArr[front][next + 2] > 0) {
					//System.out.println("符合" + integer);
					randomList.add(integer);
				}
			}
		}
		return randomList;
	}
	
	// 寻找2张一样的
	public static List<Integer> moPaiFour(List<Integer> shouPaiList) {
		List<Integer> randomList = new ArrayList<Integer>();
		List<Integer> pai = shouPaiList;
		int[][] paiArr = CDMahJongRule.conversionType(pai);
		for (int i = 0; i < 3; i++) {
			for (int j = 1; j <= 9; j++) {
				if (paiArr[i][j] == 2) {
					Integer Integer = 10 * (1 + i) + j;
					System.out.println("测试five" + Integer);
					randomList.add(Integer);
				}
			}
		}
		return randomList;
	}

	// 寻找3张一样的
	public static List<Integer> moPaiFive(List<Integer> shouPaiList) {
		List<Integer> pai = shouPaiList;
		List<Integer> randomList = new ArrayList<Integer>();
		int[][] paiArr = CDMahJongRule.conversionType(pai);
		for (int i = 0; i < 3; i++) {
			for (int j = 1; j <= 9; j++) {
				if (paiArr[i][j] == 3) {
					Integer Integer = 10 * (1 + i) + j;
					System.out.println("测试five" + Integer);
					randomList.add(Integer);
				}
			}
		}
		return randomList;
	}
	
	/**
	 * 李培光 针对麻将规则的排序
	 * 使用 Map按value进行排序 	未封装成泛型 
	 * @param oriMap
	 * @return
	 */
	public static Map<Integer, Integer> sortMapByValue(Map<Integer, Integer> oriMap) {
		if (oriMap == null || oriMap.isEmpty()) {
			return null;
		}
		Map<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
		List<Map.Entry<Integer, Integer>> entryList = new ArrayList<Map.Entry<Integer, Integer>>(
				oriMap.entrySet());
		Collections.sort(entryList, new Comparator<Map.Entry<Integer, Integer>>() {			
			public int compare(Entry<Integer, Integer> me1, Entry<Integer, Integer> me2) {
				//两个值相等则进行随机排序
				if(me2.getValue()==me1.getValue()){
					int num=MathUtil.randomNumber(0, 1);
					if(num==0){
						return me1.getValue().compareTo(me2.getValue());
					}else{
						return me2.getValue().compareTo(me1.getValue());
					}
				}else{
					return me2.getValue().compareTo(me1.getValue());
				}		
			}
		});
		Iterator<Map.Entry<Integer, Integer>> iter = entryList.iterator();
		Map.Entry<Integer, Integer> tmpEntry = null;
		while (iter.hasNext()) {
			tmpEntry = iter.next();
			sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
		}
		return sortedMap;
	}
	
	 /**
     * 获取正常出牌情况下 下一的出牌座位
     * 剔除胡牌
     *
     * @return
     */
	public static int getNextPlaySeat(int size,List<Integer> winners,int lastPlaySeat) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 1; i <= size; i++) {
			if (!winners.contains(i)) {
				list.add(i);
			}
		}
		Integer seat = lastPlaySeat;
		int index = list.indexOf(seat);
		if (index == -1) {
			index = 0;
		}
		if (index == list.size() - 1) {

			return list.get(0);
		} else {
			if (list.size() == 1) {
				return list.get(0);
			} else if (list.size() == 0) {
				return 0;
			} else {
				return list.get(index + 1);
			}
		}

	}
	
}
