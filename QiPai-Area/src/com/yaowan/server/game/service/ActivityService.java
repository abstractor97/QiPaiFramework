package com.yaowan.server.game.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GActivity.ActivityInfo;
import com.yaowan.protobuf.game.GActivity.GMsg_12019001;
import com.yaowan.protobuf.game.GActivity.GMsg_12019002;
import com.yaowan.protobuf.game.GActivity.GiftBagInfo;
import com.yaowan.protobuf.game.GActivity.RoleJoiningInfo;
import com.yaowan.server.game.function.ActivityFunction;
import com.yaowan.server.game.model.data.entity.ActivityData;
import com.yaowan.server.game.model.data.entity.ActivityGiftBag;
import com.yaowan.server.game.model.data.entity.ActivityRole;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.struct.Activity;

/**
 * 
 * @author G_T_C
 */
@Service
public class ActivityService {

	@Autowired
	private ActivityFunction activityFunction;

	public void sendMessage(Player player) {
		if (player.getRole() == null) {
			return;
		}
		Role role = player.getRole(); 
		Map<Long, Activity> activityMap = activityFunction
				.getActivityCacheMap();
		GMsg_12019001.Builder builder = GMsg_12019001.newBuilder();
		List<ActivityInfo> activityInfos = new ArrayList<ActivityInfo>();
		if (null != activityMap && !activityMap.isEmpty()) {
			for (Long id : activityMap.keySet()) {
				ActivityInfo info = createActivityInfo(activityMap.get(id),
						role);
				activityInfos.add(info);
			}
		}
		builder.addAllActivityInfo(activityInfos);
		player.write(builder.build());
	}

	private ActivityInfo createActivityInfo(Activity activity, Role role) {
		ActivityData activityData = activity.getActivityData();
		if (null != activityData) {
			ActivityInfo.Builder builder = ActivityInfo.newBuilder();
			builder.setEndTime(activityData.getEndTime());
			builder.setExplain(activityData.getExplain());
			builder.setAid(activityData.getId());
			builder.setName(activityData.getName());
			builder.setIconUrl(activityData.getPictureUrl());
			builder.setActivityUrl(activityData.getActivtityUrl());
			builder.setStartTime(activityData.getStartTime());
			builder.setRewardExpireTime(activityData.getRewardExpireTime());
			builder.setType(activityData.getType());
			builder.setRewardExpireTime(activityData.getRewardExpireTime());
			List<GiftBagInfo> giftBagInfos = createGiftBagInfo(
					activity.getGiftBagMap(), role);
			builder.addAllGiftBagInfos(giftBagInfos);
			Map<Long, Integer> infoMap = role.getVoteInfoMap();
			int click = 0;
			if(infoMap != null && infoMap.get(activityData.getId()) != null){
				click = infoMap.get(activityData.getId());
			}
			builder.setIsClick(click);
			return builder.build();
		} else {
			return null;
		}

	}

	private List<GiftBagInfo> createGiftBagInfo(
			Map<Long, ActivityGiftBag> giftBagMap, Role role) {
		List<GiftBagInfo> infos = new ArrayList<>();

		if (giftBagMap == null) {
			return infos;
		}
		Map<Long, Map<Long, ActivityRole>> activityRoleCacheMap = activityFunction
				.getActivityRoleCacheMap();
		for (Long giftId : giftBagMap.keySet()) {
			ActivityGiftBag giftBag = giftBagMap.get(giftId);
			GiftBagInfo.Builder builder = GiftBagInfo.newBuilder();
			builder.setCondition(giftBag.getCondition());
			builder.setGiftId(giftBag.getId());
			builder.setName(giftBag.getName());
			builder.setReward(giftBag.getReward());
			if (null != activityRoleCacheMap) {
				Map<Long, ActivityRole> activityRoleMap = activityRoleCacheMap
						.get(role.getRid());
				RoleJoiningInfo.Builder roleInfoBuilder = RoleJoiningInfo
						.newBuilder();
				// roleInfoBuilder.setRid(role.getRid());
				if (null != activityRoleMap && null != activityRoleMap
						&& null != activityRoleMap.get(giftBag.getAid())) {
					ActivityRole activityRole = activityRoleMap.get(giftBag.getAid());
					roleInfoBuilder.setIsReward(activityRole.getGiftInfoMap().get(giftId));
					roleInfoBuilder.setProcess(activityRole.getProcess());
					roleInfoBuilder.setStatus(activityRole.getStatus());
					builder.setRoleInfo(roleInfoBuilder.build());
				}
				infos.add(builder.build());
			}

		}
		return infos;
	}

	/**
	 * 用户上传aid,giftid,领取任务奖励
	 * 
	 * @param player
	 */
	public void requestRewards(Player player, long aid) {
		LogUtil.info("ACTIVITY REWARDS :::: ACTIVITYID->>>" + aid);

		boolean b = activityFunction.giveRewards(player, aid); // 发放奖励
		int status = 0;
		if (b)
			status = 1; // 成功

		// 下发奖励领取结果
		GMsg_12019002.Builder builder = GMsg_12019002.newBuilder();
		builder.setStatus(status);
		builder.setAid(aid);
		player.write(builder.build());
	}

	public void addClickNum(Player player, long aid) {
		activityFunction.addClickNum(player.getRole(), aid);
	}

}
