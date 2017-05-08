package com.yaowan.server.game.function;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.MatchHappenType;
import com.yaowan.constant.MatchJoinType;
import com.yaowan.constant.MatchStat;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.constant.MoneyType;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.MatchCache;
import com.yaowan.csv.cache.MatchRewardCache;
import com.yaowan.csv.entity.MatchCsv;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GMatch;
import com.yaowan.protobuf.game.GMatch.ApplyResult;
import com.yaowan.protobuf.game.GMatch.GMatchInfo;
import com.yaowan.protobuf.game.GMatch.GMsg_12030001;
import com.yaowan.protobuf.game.GMatch.GMsg_12030002;
import com.yaowan.protobuf.game.GMatch.GMsg_12030003;
import com.yaowan.protobuf.game.GMatch.GMsg_12030009;
import com.yaowan.protobuf.game.GMatch.ReqApplyType;
import com.yaowan.server.game.model.data.dao.MatchDao;
import com.yaowan.server.game.model.data.dao.MatchRecordDao;
import com.yaowan.server.game.model.data.dao.MatchRoleDao;
import com.yaowan.server.game.model.data.entity.MatchData;
import com.yaowan.server.game.model.data.entity.MatchRecord;
import com.yaowan.server.game.model.data.entity.MatchRoleData;
import com.yaowan.server.game.model.struct.Match;
import com.yaowan.server.game.model.struct.MatchRanking;
import com.yaowan.server.game.model.struct.MatchRole;

/**
 * 比赛逻辑
 */
@Component
public class MatchFunction extends FunctionAdapter {
	
	/**
	 *
	 */
	@Autowired
	private RoleFunction roleFunction;
	
	/**
	 * 比赛信息
	 */
	@Autowired
	private MatchDao matchDao;
	
	/**
	 * 比赛参与者
	 */
	@Autowired
	private MatchRoleDao matchRoleDao;
	
	/**
	 * 比赛记录
	 */
	@Autowired
	private MatchRecordDao matchRecordDao;
	
	/**
	 * 比赛配置信息
	 */
	@Autowired
	private MatchCache matchCache;
	
	/**
	 * 比赛奖励信息
	 */
	@Autowired
	private MatchRewardCache matchRewardCache;
	
	/**
	 * 比赛信息
	 */
	private ConcurrentHashMap<Integer, Match> matchMap = new ConcurrentHashMap<>();
	
	/**
	 * 比赛参与者信息
	 * ConcurrentHashMap<matchId, Map<roleId, MatchRole>>
	 */
	private ConcurrentHashMap<Integer, Map<Long, MatchRole>> matchRoleMap = new ConcurrentHashMap<>();
	
	/**
	 * 比赛排名信息
	 * key: matchId
	 */
	private ConcurrentHashMap<Integer, MatchRanking> matchRankingMap = new ConcurrentHashMap<>();
	
	//-------------------------------------------------------------
	// 主要函数
	
	@Override
	public void handleOnServerStart() {
		
		List<MatchData> matchDataList = matchDao.findAll();
		if (matchDataList == null) {
			return;
		}
		for (MatchData matchData : matchDataList) {
			// 以后不会再举行的比赛不放进列表中
			if (matchData.getStat() == MatchStat.END)
				continue;
			
			// 构建比赛对象
			Match match = new Match(matchData);
			matchMap.put(matchData.getMatchId(), match);
			
			// 构建参与者列表
			ConcurrentHashMap<Long, MatchRole> roleMap = new ConcurrentHashMap<>();
			matchRoleMap.put(matchData.getMatchId(), roleMap);
			
			// 构建排名列表
			MatchRanking ranking = new MatchRanking();
			matchRankingMap.put(matchData.getMatchId(), ranking);
			
			//
			List<MatchRoleData> matchRoleDataList = matchRoleDao.getAllRoleDataByMatchId(matchData.getMatchId());
			if (matchDataList != null) {
				for (MatchRoleData matchRoleData : matchRoleDataList) {
					// 生成参与者列表
					MatchRole matchRole = new MatchRole(matchRoleData);
					roleMap.put(matchRoleData.getRid(), matchRole);
					
					// 添加排名信息
					ranking.changeScore(matchRoleData.getRid(), matchRoleData.getScore());
				}
			}
			
			// 进行排序
			ranking.sort();
			
			// 对比赛对象进行初始化，生成运行时信息
			if (!initMatch(match)) {
				// 比赛已经无效，移除掉
				removeMatch(match);
			}
		}
	}
	
	@Override
	public void handleOnServerShutdown() {
		matchMap.clear();
		matchRoleMap.clear();
		matchRankingMap.clear();
	}
	
	private boolean initMatch(Match match) {
		
		int matchType = match.getMatchData().getMatchType();
		MatchCsv matchCsv = matchCache.getConfig(matchType);
		if (matchCsv == null) {
			throw new NullPointerException("can not find match type in csv, match type:" + matchType);
		}
		
		int dayBreak = TimeUtil.dayBreak();
		MatchData matchData = match.getMatchData();
		// 先计算预赛时间
		int happenTime;
		happenTime = calcTime(matchData.getHappenType(), dayBreak, matchData.getMatch01Date(),
			matchData.getMatch01DayOfWeek(), matchData.getMatch01TimeOfDay());
		
		// 生成时间和状态
		long now = TimeUtil.time();
		if (now > happenTime) {
			if (matchData.getStat() == MatchStat.UNINIT
				|| matchData.getStat() == MatchStat.PREPARE
				|| matchData.getStat() == MatchStat.APPLY
				|| matchData.getStat() == MatchStat.END) {
				
				// 情况1： 尚未开始的比赛，可以直接进入比赛前的各种状态
				// 不需要做任何事
				
			} else if (matchData.getStat() == MatchStat.MATCH01
				|| matchData.getStat() == MatchStat.MATCH02
				|| matchData.getStat() == MatchStat.MATCH03) {
				// 情况2： 尚未开始的比赛，但比赛的状态是比赛状态，
				// 需要返还报名费用
				// TODO: 退还报名费用，并清空报名的玩家
				
			} else {
				// 这种情况不应该发生
				throw new IllegalArgumentException("unknown stat 1, stat: " + matchData.getStat());
			}
			
			// 更新状态和时间
			int applyTime = calcTime(matchData.getHappenType(), dayBreak, matchData.getApplyDate(),
				matchData.getApplyDayOfWeek(), matchData.getApplyTimeOfDay());
			match.setApplyTime(applyTime);
			match.setMatchTime(happenTime);
			if (now >= applyTime) {
				match.setNextUpdateTime(happenTime);
				match.getMatchData().setStat(MatchStat.APPLY);
			} else {
				match.setNextUpdateTime(applyTime);
				match.getMatchData().setStat(MatchStat.PREPARE);
			}
			
			// 保存
			matchDao.update(match.getMatchData());
			
		} else if (now <= happenTime) {
			if (matchData.getStat() == MatchStat.MATCH01
				|| matchData.getStat() == MatchStat.MATCH02
				|| matchData.getStat() == MatchStat.MATCH03) {
				// 情况3： 已经开始了的比赛，不能继续，只能返还报名费用
				// TODO: 退还报名费用，并清空报名的玩家
				
			} else {
				// 情况4：这次的比赛已经结束，更新比赛时间
			}
			
			happenTime = calcNextTime(matchData.getHappenType(), dayBreak, matchData.getMatch01Date(),
				matchData.getMatch01DayOfWeek(), matchData.getMatch01TimeOfDay());
			if (happenTime == 0) {
				// 不存在下次比赛
				match.getMatchData().setStat(MatchStat.END);
				return false;
			} else {
				int applyTime = calcNextTime(matchData.getHappenType(), dayBreak, matchData.getApplyDate(),
					matchData.getApplyDayOfWeek(), matchData.getApplyTimeOfDay());
				match.setApplyTime(applyTime);
				match.setMatchTime(happenTime);
				match.setNextUpdateTime(applyTime);
				match.getMatchData().setStat(MatchStat.PREPARE);
			}
			
			// 保存
			matchDao.update(match.getMatchData());
			
		} else {
			// 这种情况不应该发生
			throw new IllegalArgumentException("unknown stat 2, stat: " + matchData.getStat());
		}
		
		match.setInited(true);
		return true;
	}
	
	/**
	 * 更新比赛对象
	 */
	public void update() {
		
		int now = TimeUtil.time();
		Match match;
		Iterator<Map.Entry<Integer, Match>> iter = matchMap.entrySet().iterator();
		while (iter.hasNext()) {
			match = iter.next().getValue();
			
			if (!match.isInited()) {
				LogUtil.error("game match not inited. matchId: " + match.getMatchData().getMatchId());
				continue;
			}
			
			// 还没到需要更新的时间
			if (match.getNextUpdateTime() > now)
				continue;
			
			MatchCsv matchCsv = matchCache.getConfig(match.getMatchData().getMatchType());
			
			switch (match.getMatchData().getStat()) {
				case MatchStat.PREPARE: {
					// 转换为报名状态
					match.getMatchData().setStat(MatchStat.APPLY);
					match.setNextUpdateTime(match.getMatchTime());
					// 保存
					matchDao.update(match.getMatchData());
					break;
				}
				case MatchStat.APPLY: {
					match.getMatchData().setStat(MatchStat.MATCH01);
					notifyMatchStart(match);
					// 保存
					matchDao.update(match.getMatchData());
					break;
				}
				case MatchStat.MATCH01: {
					//
					break;
				}
				case MatchStat.MATCH02: {
					//
					break;
				}
				case MatchStat.MATCH03: {
					break;
				}
				case MatchStat.END: {
					// 移除
					removeMatch(match, iter);
					break;
				}
				default: {
					LogUtil.error("unknown stat in update. stat: " + match.getMatchData().getStat());
					break;
				}
			}
		}
	}
	
	//-------------------------------------------------------------
	// 辅助函数
	
	private void removeMatch(Match match) {
		// 移除排名
		matchRankingMap.remove(match.getMatchData().getMatchId());
		
		// 移除参与者
		matchRoleDao.deleteAllRole(match.getMatchData().getMatchId());
		matchRoleMap.remove(match.getMatchData().getMatchId());
		
		// 移除掉
		matchDao.delete(match.getMatchData());
		matchMap.remove(match.getMatchData().getMatchId());
	}
	
	private void removeMatch(Match match, Iterator<Map.Entry<Integer, Match>> matchIter) {
		// 移除排名
		matchRankingMap.remove(match.getMatchData().getMatchId());
		
		// 移除参与者
		matchRoleDao.deleteAllRole(match.getMatchData().getMatchId());
		matchRoleMap.remove(match.getMatchData().getMatchId());
		
		matchDao.delete(match.getMatchData());
		matchIter.remove();
	}
	
	/**
	 * 计算时间
	 *
	 * @param dayBreak 举行活动那一天的凌晨的时间戳
	 */
	private int calcTime(int happenType, int dayBreak, String date, int dayOfWeek, String timeOfDay) {
		
		int ret = 0;
		
		switch (happenType) {
			case MatchHappenType.HAPPEN_ONCE: {
				String dt = date + " " + timeOfDay;
				ret = TimeUtil.str2time(dt);
				break;
			}
			case MatchHappenType.HAPPEN_DAILY: {
				String strDate = TimeUtil.date("yyyy-MM-dd", (int) dayBreak);
				strDate = strDate + " " + timeOfDay;
				ret = TimeUtil.str2time(strDate);
				break;
			}
			case MatchHappenType.HAPPEN_WEEKLY: {
				// TODO: 尚未实现
				break;
			}
			default: {
				// 这种情况不应该发生
				throw new IllegalArgumentException("unknown match happen type. type: " + happenType);
			}
		}
		
		return ret;
	}
	
	/**
	 * 计算相对于当前时间的下次比赛时间
	 */
	private int calcNextTime(int happenType, int dayBreak, String date, int dayOfWeek, String timeOfDay) {
		
		int ret = 0;
		
		switch (happenType) {
			case MatchHappenType.HAPPEN_ONCE: {
				// 不存在下次比赛时间
				ret = 0;
				break;
			}
			case MatchHappenType.HAPPEN_DAILY: {
				dayBreak = dayBreak + TimeUtil.ONE_DAY;
				
				String strDate = TimeUtil.date("yyyy-MM-dd", (int) dayBreak);
				strDate = strDate + " " + timeOfDay;
				ret = TimeUtil.str2time(strDate);
				break;
			}
			case MatchHappenType.HAPPEN_WEEKLY: {
				// TODO: 尚未实现
				break;
			}
			default: {
				// 这种情况不应该发生
				throw new IllegalArgumentException("unknown match happen type. type: " + happenType);
			}
		}
		
		return ret;
	}
	
	/**
	 * 通知玩家比赛已经开始
	 */
	private void notifyMatchStart(Match match) {
		GMsg_12030003.Builder builder = GMsg_12030003.newBuilder();
		builder.setMatchType(match.getMatchData().getMatchType());
		builder.setMatchId(match.getMatchData().getMatchId());
		GMsg_12030003 msg = builder.build();
		
		Map<Long, MatchRole> roleMap = matchRoleMap.get(match.getMatchData().getMatchId());
		for (Map.Entry<Long, MatchRole> entry : roleMap.entrySet()) {
			// 获取在线玩家
			Player player = roleFunction.getPlayer(entry.getKey());
			if (player == null) {
				continue;
			}
			
			player.write(msg);
		}
	}
	
	//-------------------------------------------------------------
	// 消息处理
	
	/**
	 * 获取比赛列表
	 */
	public void getMatchList(Player player) {
		
		GMsg_12030001.Builder builder = GMsg_12030001.newBuilder();
		
		MatchData matchData;
		Match match;
		for (Map.Entry<Integer, Match> matchEntry : matchMap.entrySet()) {
			match = matchEntry.getValue();
			matchData = match.getMatchData();
			
			if (matchData.getStat() == MatchStat.APPLY
				|| matchData.getStat() == MatchStat.MATCH01
				|| matchData.getStat() == MatchStat.MATCH02
				|| matchData.getStat() == MatchStat.MATCH03) {
				
				GMatchInfo.Builder info = GMatchInfo.newBuilder();
				info.setMatchId(matchData.getMatchId());
				info.setMatchType(matchData.getMatchType());
				info.setApplyTime(match.getApplyTime());
				info.setStartTime(match.getMatchTime());
				info.setStat(matchData.getStat());
				
				// 不需要报名的比赛也需要显示, 所以这类比赛的apply状态只是用于控制是否要下发比赛信息
				if (matchData.getStat() == MatchStat.APPLY) {
					MatchCsv matchCsv = matchCache.getConfig(match.getMatchData().getMatchType());
					if (matchCsv.getJoinType() == MatchJoinType.IMMEDIATELY) {
						// 强行修改为准备状态，防止客户端出错
						info.setStat(MatchStat.PREPARE);
					}
				}
				
				Map<Long, MatchRole> roleMap = matchRoleMap.get(matchData.getMatchId());
				MatchRole matchRole = roleMap.get(player.getId());
				if (matchRole == null) {
					info.setIsApply(0);
				} else {
					info.setIsApply(1);
				}
				info.setApplyCount(roleMap.size());
				builder.addMatchList(info);
			}
		}
		player.write(builder.build());
	}
	
	/**
	 * 报名比赛
	 */
	public void applyMatch(Player player, ReqApplyType reqType, int matchId) {
		
		Match match = matchMap.get(matchId);
		if (match == null) {
			// 比赛不存在
			LogUtil.debug("applyMatch(), match not found. matchId: " + matchId);
			return;
		}
		
		MatchCsv matchCsv = matchCache.getConfig(match.getMatchData().getMatchType());
		// 判断比赛状态
		if (matchCsv.getJoinType() == MatchJoinType.IMMEDIATELY) {
			// 不需要报名
			LogUtil.debug("applyMatch(), match not need apply. matchId: " + matchId);
			return;
		}
		
		GMsg_12030002.Builder builder = GMsg_12030002.newBuilder();
		builder.setReqType(reqType);
		builder.setMatchId(matchId);
		
		if (match.getMatchData().getStat() != MatchStat.APPLY) {
			// 不是报名时间
			builder.setResult(ApplyResult.APPLY_EXPIRED);
			player.write(builder.build());
			return;
		}
		
		//
		if (reqType == ReqApplyType.REQUEST_APPLY) { // 报名
			
			Map<Long, MatchRole> roleMap = matchRoleMap.get(matchId);
			MatchRole matchRole = roleMap.get(player.getId());
			if (matchRole != null) {
				// 重复参加
				LogUtil.debug("applyMatch(), match apply repeat. matchId: " + matchId);
				return;
			}
			// 判断金钱是否足够
			if (matchCsv.getMoneyType() == MoneyType.Gold.ordinal()) {
				if (player.getRole().getGold() < matchCsv.getMoneyValue())
					return;
			} else if (matchCsv.getMoneyType() == MoneyType.Diamond.ordinal()) {
				if (player.getRole().getDiamond() < matchCsv.getMoneyValue())
					return;
			} else if (matchCsv.getMoneyType() == MoneyType.Cristal.ordinal()) {
				if (player.getRole().getCrystal() < matchCsv.getMoneyValue())
					return;
			} else {
				throw new IllegalArgumentException("unknown money type.");
			}
			
			// 扣除费用
			if (matchCsv.getMoneyType() == MoneyType.Gold.ordinal()) {
				roleFunction.goldSub(player.getRole(), matchCsv.getMoneyValue(), MoneyEvent.MATCH_APPLY, true);
			} else if (matchCsv.getMoneyType() == MoneyType.Diamond.ordinal()) {
				roleFunction.diamondSub(player.getRole(), matchCsv.getMoneyValue(), MoneyEvent.MATCH_APPLY, true);
			} else if (matchCsv.getMoneyType() == MoneyType.Cristal.ordinal()) {
				roleFunction.crystalSub(player.getRole(), matchCsv.getMoneyValue(), MoneyEvent.MATCH_APPLY);
			} else {
				throw new IllegalArgumentException("unknown money type.");
			}
			
			// 加入到参加者列表
			MatchRoleData matchRoleData = new MatchRoleData();
			matchRoleData.setRid(player.getId());
			matchRoleData.setMatchId(matchId);
			matchRoleData.setJoinTime(TimeUtil.time());
			matchRoleData.setScore(matchCsv.getInitScore());
			matchRoleDao.insert(matchRoleData);
			matchRole = new MatchRole(matchRoleData);
			roleMap.put(matchRoleData.getMatchId(), matchRole);
			// 加入到排名列表
			MatchRanking ranking = matchRankingMap.get(matchId);
			ranking.changeScore(player.getId(), matchRoleData.getScore());
			ranking.sort();
			
			// 通知结果
			builder.setResult(ApplyResult.APPLY_SUCESS);
			player.write(builder.build());
			
		} else { // 取消报名
			
			Map<Long, MatchRole> roleMap = matchRoleMap.get(matchId);
			MatchRole matchRole = roleMap.get(player.getId());
			if (matchRole == null) {
				// 并没有参加
				return;
			}
			
			// 返回报名费用
			if (matchCsv.getMoneyType() == MoneyType.Gold.ordinal()) {
				roleFunction.goldAdd(player.getRole(), matchCsv.getMoneyValue(), MoneyEvent.MATCH_APPLY, true);
			} else if (matchCsv.getMoneyType() == MoneyType.Diamond.ordinal()) {
				roleFunction.diamondAdd(player.getRole(), matchCsv.getMoneyValue(), MoneyEvent.MATCH_APPLY);
			} else if (matchCsv.getMoneyType() == MoneyType.Cristal.ordinal()) {
				roleFunction.crystalAdd(player.getRole(), matchCsv.getMoneyValue(), MoneyEvent.MATCH_APPLY);
			} else {
				throw new IllegalArgumentException("unknown money type.");
			}
			
			// 从参加者列表移除
			roleMap.remove(player.getId());
			matchRoleDao.delete(matchRole.getMatchRoleData());
			
			// 移除排名
			MatchRanking ranking = matchRankingMap.get(matchId);
			ranking.remove(player.getId());
			
			// 通知结果
			builder.setResult(ApplyResult.APPLY_SUCESS);
			player.write(builder.build());
		}
	}
	
	/**
	 * 获取比赛记录
	 */
	public void getMatchRecord(Player player) {
		List<MatchRecord> recordList = matchRecordDao.getAllRecored(player.getId());
		if (recordList == null)
			return;
		
		GMsg_12030009.Builder builder = GMsg_12030009.newBuilder();
		for (MatchRecord matchRecord : recordList) {
			GMatch.MatchRecord.Builder value = GMatch.MatchRecord.newBuilder();
			value.setTime(matchRecord.getTime());
			value.setMatchType(matchRecord.getMatchType());
			value.setRanking(matchRecord.getRanking());
			builder.addMatchRecord(value);
		}
		player.write(builder.build());
	}
}
