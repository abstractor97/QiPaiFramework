package com.yaowan.server.game.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.RecommendStatType;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GRecommend.GMsg_11023001;
import com.yaowan.protobuf.game.GRecommend.GMsg_11023002;
import com.yaowan.protobuf.game.GRecommend.GMsg_11023003;
import com.yaowan.protobuf.game.GRecommend.GMsg_12023001;
import com.yaowan.protobuf.game.GRecommend.GMsg_12023002;
import com.yaowan.protobuf.game.GRecommend.GMsg_12023003;
import com.yaowan.protobuf.game.GRole.GMsg_12002015;
import com.yaowan.protobuf.game.GRole.GRedPoint;
import com.yaowan.server.game.function.RecommendFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.model.data.entity.RoleRecommend;
import com.yaowan.server.game.model.data.entity.Role;

@Component
public class RecommendService {

	@Autowired
	private RecommendFunction recommendFunction;
	
	@Autowired
	private RoleFunction roleFunction;
	
	/**
	 * 获得代理信息
	 */
	public void recommendList(Player player) {
		Role role = player.getRole();
		RoleRecommend recommend = recommendFunction.getRecommendMsg(role.getRid());
		GMsg_12023001.Builder builder = GMsg_12023001.newBuilder();
		builder.setHadGetRewards(recommend.getHasGetReward());
		builder.setHadrecommendFriend(recommend.getRecommendFriendNum());
		builder.setRecommendNumber(recommend.getCode());
		builder.setReceiveRewards(recommend.getCanGetReward());
		player.write(builder.build());
	}
	
	/**
	 * 返回绑定操作结果
	 */
	public void doResult(Player player, String recommendNum) {
		Role role = player.getRole();
		int result = recommendFunction.checkRecommendNum(recommendNum, role.getRid());
		GMsg_12023002.Builder builder = GMsg_12023002.newBuilder();
		builder.setStatus(result);
		player.write(builder.build());
		
		if(result == RecommendStatType.SUCCESS) {
			//更新推广状态
			role.setIsRecommend((byte)1);
			recommendFunction.updateIsBeRecommend(role);
			
			
			//被推荐人得到奖励
			recommendFunction.bindReward(role);
			
			//推荐人得到奖励
			long rid = recommendFunction.findRidByRecommend(recommendNum);
			RoleRecommend recommend = recommendFunction.findRecommend(recommendNum, rid);
			recommend = recommendFunction.recommendReward(recommend);
			Player player2 = roleFunction.getPlayer(rid);
			if(player2 != null) {
				GMsg_12023001.Builder builder2 = GMsg_12023001.newBuilder();
				builder2.setHadGetRewards(recommend.getHasGetReward());
				builder2.setHadrecommendFriend(recommend.getRecommendFriendNum());
				builder2.setRecommendNumber(recommend.getCode());
				builder2.setReceiveRewards(recommend.getCanGetReward());
				player2.write(builder2.build());
				
				GMsg_12002015.Builder builder3 = GMsg_12002015.newBuilder();
				GRedPoint.Builder redPoint = GRedPoint.newBuilder();
				redPoint.setStatus(1);
				redPoint.setType(4);
				builder3.setRedPoint(redPoint);
				player2.write(builder3.build());
			}
			//插入推广日志
			recommendFunction.insertRecommendLog(rid, role.getRid());
		}
	}
	
	/**
	 * 返回领取操作结果
	 * @param player
	 */
	public void doGetMoney(Player player) {
		GMsg_12023003.Builder builder = GMsg_12023003.newBuilder();
		RoleRecommend recommend = recommendFunction.findRecommendByRid(player.getRole());
		builder.setReceiveRewards(recommend.getCanGetReward());
		int result = recommendFunction.getRecommendReward(recommend, player.getRole());
		builder.setStatus(result);
		player.write(builder.build());
		
		GMsg_12023001.Builder builder2 = GMsg_12023001.newBuilder();
		builder2.setHadGetRewards(recommend.getHasGetReward());
		builder2.setHadrecommendFriend(recommend.getRecommendFriendNum());
		builder2.setRecommendNumber(recommend.getCode());
		builder2.setReceiveRewards(recommend.getCanGetReward());
		player.write(builder2.build());
		
		GMsg_12002015.Builder builder3 = GMsg_12002015.newBuilder();
		GRedPoint.Builder redPoint = GRedPoint.newBuilder();
		redPoint.setStatus(0);
		redPoint.setType(4);
		builder3.setRedPoint(redPoint);
		player.write(builder3.build());
	}
}
