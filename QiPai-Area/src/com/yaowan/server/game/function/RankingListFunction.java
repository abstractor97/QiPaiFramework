package com.yaowan.server.game.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.RankListCache;
import com.yaowan.csv.entity.RankListCsv;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.RankListBean;
import com.yaowan.protobuf.game.GRankingList.GRankingInfo;
import com.yaowan.server.game.model.data.dao.RoleDao;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.dao.RankingLogDao;
import com.yaowan.server.game.model.log.entity.RankingLog;

/**
 * 排行榜
 * 
 * @author G_T_C
 */
@Component
public class RankingListFunction extends FunctionAdapter {

	@Autowired
	private RankListCache topRoleCache;

	@Autowired
	private RoleDao roleDao;

	@Autowired
	private RoleFunction roleFunction;
	
	@Autowired
	private RankingLogDao rankingLogDao;

	//private AtomicInteger lastUpdateTime = new AtomicInteger(TimeUtil.time());// 上次更新时间

	/**
	 * 排行榜缓存
	 */
	private final ConcurrentHashMap<Integer, List<RankListBean>> cacheMap = new ConcurrentHashMap<>();

	@Override
	public void handleOnServerStart() {
		statistics();
	}

	@Override
	public void handleOnServerShutdown() {
		cacheMap.clear();
	}

	/**
	 * 根据类型获取排行榜信息
	 * 
	 * @author G_T_C
	 * @param type
	 * @return
	 */
	public Map<Integer, Object> getRankingListByType(int type, long rid) {
		Map<Integer, Object> result = new HashMap<Integer, Object>();
		boolean myIsRanking = false;// 玩家自己是否上榜
		if (type == 0) {// 如果没有传，就默认为查1.赚钱榜
			type = 1;
		}
		List<RankListBean> rankingList = null;
		// int currentTime = TimeUtil.time();
		RankListCsv topRoleCsv = topRoleCache.getConfig(type);
		// int rankLimit = topRoleCsv.getRankingLimit();
		int showLimit = 30;
		if (topRoleCsv != null) {
			showLimit = topRoleCsv.getShowLimit();
		}
		rankingList = cacheMap.get(type);
		if (null == rankingList) {
			statistics();// 如果缓存为空，需要重置
			rankingList = cacheMap.get(type);
		}
		List<GRankingInfo> infoList = new ArrayList<>();
		if (null != rankingList && !rankingList.isEmpty()) {
			for (int i = 0; i < rankingList.size(); i++) {
				RankListBean rank = rankingList.get(i);
				GRankingInfo.Builder builder = GRankingInfo.newBuilder();
				int rankNuber = getRank(i, showLimit);
				builderRankInfo(type, showLimit, rankNuber, rank, builder);
				// 判断查出的所有的排行榜记录有没有当前玩家
				if (rid == rank.getRid()) {
					myIsRanking = true;
					result.put(2, builder.build());
				}
				if (i <= showLimit - 1) {
					infoList.add(builder.build());
				}
			}
			result.put(1, infoList);
		}
		if (!myIsRanking) {// 如果记录中没有记录，需要查询该玩家的记录
			Role role = roleFunction.getRoleByRid(rid);
			GRankingInfo.Builder builder = GRankingInfo.newBuilder();
			builder.setBureauCount(role.getBureauCount());
			int totalMoney = role.getGold() + role.getGoldPot();
			builder.setGold(totalMoney);
			builder.setHead(role.getHead());
			builder.setIsVip(role.getVipTime() > TimeUtil.time() ? true : false);
			builder.setMakeMoney(totalMoney - role.getLastWeekMoney());
			builder.setName(role.getNick());
			builder.setType(type);
			builder.setRanking(0);
			builder.setRid(role.getRid());
			result.put(2, builder.build());
		}
		return result;
	}

	/**
	 * 构造协议对象
	 * 
	 * @author G_T_C
	 * @param type
	 * @param showLimit
	 * @param rankNuber
	 * @param log
	 * @param builder
	 */
	private void builderRankInfo(int type, int showLimit, int rankNuber,
			RankListBean rank, GRankingInfo.Builder builder) {
		builder.setBureauCount(rank.getBureauCount());
		builder.setGold(rank.getTotalMoney());
		builder.setHead(rank.getHead());
		builder.setIsVip(rank.getIsVip() == 1 ? true : false);
		builder.setMakeMoney(rank.getChangeMoney());
		builder.setName(rank.getName());
		builder.setType(type);
		builder.setRanking(rankNuber);
		builder.setRid(rank.getRid());
	}

	/**
	 * 判断是否上榜
	 * 
	 * @author G_T_C
	 * @param i
	 * @param showLimit
	 * @return
	 */
	private int getRank(int i, int showLimit) {
		if (i < showLimit) {
			return i + 1;// 名次
		} else {
			return 0;// 未上榜
		}
	}

	@Override
	public void handleOnNextDay() {
		// 获取今天是星期几
		int week = TimeUtil.getDayOfWeek();
		if (week != 1) {
			return;
		}
		//记录日志
		LogUtil.info("周一，排行榜数据重置前记录日志"); // 如果是星期一,更新赚钱，活跃排行榜
		doLog();
		LogUtil.info("周一，排行榜数据开始重置"); // 如果是星期一,更新赚钱，活跃排行榜
		roleDao.resetRanking();
		// 把最后一次时间重置为现在的时间
	//	lastUpdateTime.set(TimeUtil.time());
		statistics();
	}

	private void doLog() {
		int rankNum = 25;//前25名
		int time = TimeUtil.time();
		for(int i = 1; i<4;i++){
			List<RankListBean> list = roleDao.getThisRanking(i, rankNum);
			RankingLog log = new RankingLog();
			log.setType(i);
			log.setTime(time);
			String info = "";
			if(null != list && list.size()> 0){
				List<Object[]> infoList = new ArrayList<>();
				for(int j= 0; j<list.size();j++){
					Object [] objects = new Object[4];
					RankListBean bean = list.get(j);
					objects[0] = bean.getRid();
					objects[1] = bean.getName();
					objects[2] = j+1;
					switch (i) {
					case 1: {// 赚钱榜
						objects[3] = bean.getChangeMoney();
						break;
					}
					case 2: {// 活跃榜
						objects[3] = bean.getBureauCount();
						break;
					}
					case 3: {// 富豪榜
						objects[3] = bean.getTotalMoney();
						break;
					}
					}
					infoList.add(objects);
				}
				info = StringUtil.listArrayToString(infoList,
						StringUtil.DELIMITER_BETWEEN_ITEMS,StringUtil.DELIMITER_INNER_ITEM);
			}
			if(info == null){
				info = "";
			}
			log.setInfo(info);
			rankingLogDao.insert(log);
		}
		
	}

	/**
	 * 重新统计
	 * 
	 * @author G_T_C
	 */
	public void statistics() {
		// 清除缓存
		// cacheMap.clear();
		// LogUtil.info("排行榜缓存清除，开始重新排序");
	//	int lastTime = TimeUtil.time();
		// topRoleLogDao.addTop(lastUpdateTime.get(), lastTime);
		int first = 10;
		int second = 10;
		int third = 10;
		if (topRoleCache.getConfig(1) != null) {
			first = topRoleCache.getConfig(1).getRankingLimit();
		}
		if (topRoleCache.getConfig(2) != null) {
			second = topRoleCache.getConfig(2).getRankingLimit();
		}
		if (topRoleCache.getConfig(3) != null) {
			third = topRoleCache.getConfig(3).getRankingLimit();
		}
		
		cacheMap.put(1, roleDao.getThisRanking(1, first));
		cacheMap.put(2, roleDao.getThisRanking(2, second));
		cacheMap.put(3, roleDao.getThisRanking(3, third));
		
		//lastUpdateTime.set(lastTime);
	}
}
