package com.yaowan.server.game.model.struct;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.yaowan.framework.util.TimeUtil;

/**
 * 比赛排行榜
 */
public class MatchRanking {
	
	private ArrayList<MatchRankingInfo> rankingByIndex = new ArrayList<>();
	private ConcurrentHashMap<Long, MatchRankingInfo> rankingByRid = new ConcurrentHashMap<>();
	
	/**
	 * 交换
	 */
	private void swap(int index1, int index2) {
		MatchRankingInfo left = rankingByIndex.get(index1);
		MatchRankingInfo right = rankingByIndex.get(index2);
		
		left.setRanking(index2 + 1);
		right.setRanking(index1 + 1);
		rankingByIndex.set(index1, right);
		rankingByIndex.set(index2, left);
	}
	
	/**
	 * 比较操作
	 */
	private boolean more(MatchRankingInfo left, MatchRankingInfo right) {
		if (left.getScore() > right.getScore())
			return true;
		
		// 先上榜的排前
		if (left.getScore() == right.getScore() &&
			left.getTime() < right.getTime())
			return true;
		
		return false;
	}
	
	/**
	 * 快速排序实现
	 */
	private void subSort(int start, int end) {
		if ((end - start + 1) < 2)
			return;
		
		int part = partition(start, end);
		
		if (part == start) {
			subSort(part + 1, end);
		} else if (part == end) {
			subSort(start, part - 1);
		} else {
			subSort(start, part - 1);
			subSort(part + 1, end);
		}
	}
	
	/**
	 * 快速排序实现
	 */
	private int partition(int start, int end) {
		MatchRankingInfo info = rankingByIndex.get(end);
		int index = start - 1;
		
		for (int i = start; i < end; ++i) {
			if (more(rankingByIndex.get(i), info)) {
				index++;
				if (index != i) {
					swap(index, i);
				}
			}
		}
		
		if ((index + 1) != end) {
			swap(index + 1, end);
		}
		
		return index + 1;
	}
	
	/**
	 * 进行排序
	 */
	public void sort() {
		// 元素不多，快速排序即可
		subSort(0, rankingByIndex.size() - 1);
	}
	
	public MatchRankingInfo getRankingByRoleId(long rid) {
		return rankingByRid.get(rid);
	}
	
	public MatchRankingInfo getRankingByRanking(int ranking) {
		int index = ranking - 1;
		if (index < 0 || index > rankingByIndex.size())
			return null;
		
		return rankingByIndex.get(index);
	}
	
	/**
	 * 积分改变是调用，之后要手工调用sort()进行排序
	 * 不自动调用排序是因为改变积分通常是多人同时改变，可以减少排序次数
	 */
	public void changeScore(long rid, int newScore) {
		MatchRankingInfo info = getRankingByRoleId(rid);
		if (info != null) {
			info.setScore(newScore);
			info.setTime(TimeUtil.time());
		} else {
			info = new MatchRankingInfo(rid, newScore);
			info.setRanking(rankingByIndex.size() + 1);
			rankingByIndex.add(info);
			rankingByRid.put(rid, info);
		}
	}
	
	/**
	 * 从排行榜移除排名项
	 */
	public void remove(long rid) {
		MatchRankingInfo info = getRankingByRoleId(rid);
		if (info == null)
			return;
		
		rankingByIndex.remove(info.getRanking() - 1);
		rankingByRid.remove(info.getRid());
		
		// 重新处理排名
		for (int i = info.getRanking() - 1; i < rankingByIndex.size(); ++i) {
			rankingByIndex.get(i).setRanking(i + 1);
		}
	}
}
