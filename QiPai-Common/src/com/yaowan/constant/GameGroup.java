package com.yaowan.constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yaowan.framework.util.Slf4jLogUtil.SimpleLogUtil;

/**
 *
 *
 * @author JeffieChan
 * @version 2017年2月28日 下午5:01:35
 */
public class GameGroup {

	/** 分组 - 麻将 */
	public static long GROUP_MAHJONG = 1;

	private static final Map<Long, Long> GROUP_MAP = new HashMap<>();

	private static final Map<Long, List<Long>> GROUP_LIST = new HashMap<>();

	private static final long SYMBOL = 10000;

	public static void put(long group, long gameType) {
		long num = group * SYMBOL + gameType;
		GROUP_MAP.put(gameType, num);
		List<Long> list = GROUP_LIST.get(group);
		if (list == null) {
			list = new ArrayList<>();
			GROUP_LIST.put(group, list);
		}
		list.add(gameType);
		SimpleLogUtil.info(GameGroup.class, "GROUP_MAP", GROUP_LIST);
	}

	public static long getGroup(long gameType) {
		Long value = GROUP_MAP.get(gameType);
		if (value == null) {
			return -1;
		}
		long result = (value - gameType) / SYMBOL;
		return result;
	}

	public static List<Long> getGameTypeList(long group) {
		return GROUP_LIST.get(group);
	}

	/**
	 * 
	 * @param groupId
	 * @param gameType
	 * @return
	 */
	public static boolean isExist(long group, long gameType) {
		long result = getGroup(gameType);
		return result == group;
	}

	/**
	 * 
	 * @param groupId
	 * @param gameType
	 * @return
	 */
	public static boolean isExist(long gameType) {
		long result = getGroup(gameType);
		return result >= 0;
	}

	public static void main(String[] args) {
		long gameType = 0;
		for (long i = 1; i < 10; i++) {
			gameType = i;
			long group = i % 3;
			put(group, gameType);
		}
		for (long i = 1; i < 10; i++) {
			gameType = i;
			long group = i % 3;
			System.err.println(group + ",GROUP_MAP.get(gameType):" + GROUP_MAP.get(gameType));
			System.err.println(gameType + ":" + isExist(gameType));
		}
		// SimpleLogUtil.info(GameGroup.class, "GROUP_MAP", "GROUP_MAP",
		// GROUP_MAP);
		SimpleLogUtil.info(GameGroup.class, "GROUP_MAP", "GROUP_MAP", GROUP_LIST);
	}

}
