package com.yaowan.server.game.model.struct;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameType;
import com.yaowan.csv.cache.LotteryTaskCache;
import com.yaowan.csv.entity.LotteryTaskCsv;
import com.yaowan.framework.util.MathUtil;

@Component
public class LotterTaskCreator {
	@Autowired
	private LotteryTaskCache lotteryTaskCache;
	
	/**
	 * 缓存配置表的概率区间最大值
	 */
	private Map<Integer, Integer> totalCache = new ConcurrentHashMap<Integer, Integer>();

	public int randomTaskId(int gameType) {
		int lotteryId = 0;
		int randomNo = MathUtil.randomNumber(1, getCount(gameType));
		switch (gameType) {
		case GameType.DOUDIZHU:
			lotteryId = getTaskId(gameType, randomNo);
			break;
		case GameType.MAJIANG:
			lotteryId = getTaskId(gameType, randomNo);
			break;
		case GameType.ZXMAJIANG:
			lotteryId = getTaskId(gameType, randomNo);
			break;
		case GameType.CDMAJIANG:
			lotteryId = getTaskId(gameType, randomNo);
			break;
		}
		return lotteryId;
	}

	private  int getTaskId(int gameType, int randomNo){
		int minxVale = 0;//区间的前一个值
		if(lotteryTaskCache.getConfigList()!= null){
			List<LotteryTaskCsv> list = lotteryTaskCache.getConfigList();
			for(LotteryTaskCsv csv: list){
				if(csv.getAscriptionGameId() != gameType){
					continue;
				}
				int maxValue = minxVale+csv.getProbability();
				if(randomNo>= minxVale && randomNo< maxValue){
					return csv.getLotteryTaskID();
				}
				minxVale = maxValue;
			}
		}
		return 0;
	}
	
	private  int getCount(int gameType){
		Integer maxValue = totalCache.get(gameType);
		if(maxValue == null){
			maxValue = 0;
			if(lotteryTaskCache.getConfigList()!= null){
				List<LotteryTaskCsv> list = lotteryTaskCache.getConfigList();
				for(LotteryTaskCsv csv: list){
					if(csv.getAscriptionGameId() != gameType){
						continue;
					}
					 maxValue = maxValue+csv.getProbability();
					
				}
			}
			totalCache.put(gameType, maxValue);
		}
		
		return maxValue;
	}
}
