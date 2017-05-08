package com.yaowan.server.game.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GRankingList.GMsg_12018001;
import com.yaowan.protobuf.game.GRankingList.GRankingInfo;
import com.yaowan.server.game.function.RankingListFunction;

/**
 * 排行榜service
 *
 * @author G_T_C
 */
@Service
public class RankingListService {

	@Autowired
	private RankingListFunction rankingListFunction;

	/**
	 * 发送排行榜信息到客户端
	 * 
	 * @author G_T_C
	 */
	@SuppressWarnings("unchecked")
	public void senderMessages(Player player) {
		GMsg_12018001.Builder buider = GMsg_12018001.newBuilder();
		for (int i = 1; i <= 3; i++) {
			Map<Integer, Object> map = rankingListFunction
					.getRankingListByType(i, player.getRole().getRid());
			List<GRankingInfo> infoList = (List<GRankingInfo>) map.get(1);
			GRankingInfo myRankingInfo = (GRankingInfo) map.get(2);
			if(infoList == null){
				infoList = new ArrayList<>();
				infoList.add(myRankingInfo);
			}
			switch (i) {
			case 1:
				buider.addAllMakeMoneyRank(infoList);
				buider.setSelfMakeMoneyRank(myRankingInfo);
				break;
			case 2:
				buider.addAllActivityRank(infoList);
				buider.setSelfActivityRank(myRankingInfo);
				break;
			case 3:
				buider.addAllRichRank(infoList);
				buider.setSelfRichRank(myRankingInfo);
				break;
			}

		}

		player.write(buider.build());
	}
}
