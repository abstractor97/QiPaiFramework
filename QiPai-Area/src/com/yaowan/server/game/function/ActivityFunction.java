package com.yaowan.server.game.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.MoneyEvent;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.framework.util.JSONObject;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.ItemGet;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GRole.GMsg_12002015;
import com.yaowan.protobuf.game.GRole.GRedPoint;
import com.yaowan.server.game.model.data.dao.ActivityDao;
import com.yaowan.server.game.model.data.dao.ActivityGiftBagDao;
import com.yaowan.server.game.model.data.dao.ActivityRoleDao;
import com.yaowan.server.game.model.data.dao.RoleDao;
import com.yaowan.server.game.model.data.entity.ActivityData;
import com.yaowan.server.game.model.data.entity.ActivityGiftBag;
import com.yaowan.server.game.model.data.entity.ActivityRole;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.struct.Activity;

/**
 * 
 * @author G_T_C
 */
/**
 * @author G_T_C
 */
@Component
public class ActivityFunction extends FunctionAdapter {

	@Autowired
	private ActivityDao activityDao;
	
	@Autowired
	private RoleDao roleDao;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private ActivityGiftBagDao activityGiftBagDao;

	@Autowired
	private ActivityRoleDao activityRoleDao;

	@Autowired
	private ItemFunction itemFunction;

//	private AtomicInteger atomicInteger = new AtomicInteger(1);

	/** 缓存玩家对应的活动列表 Map<rid, Map<aId,ActivityRole>> */
	private ConcurrentHashMap<Long, Map<Long, ActivityRole>> activityRoleCacheMap = new ConcurrentHashMap<>();

	/** 缓存活动列表 Map<aid, activity> */
	private ConcurrentHashMap<Long, Activity> activityCacheMap = new ConcurrentHashMap<>();

	/** 缓存活动对应的礼包列表 Map<aid, Map<giftId,ActivityGiftBag>> */
	// private ConcurrentHashMap<Long, Map<Long, ActivityGiftBag>>
	// activityGiftCacheMap = new ConcurrentHashMap<>();

	public ConcurrentHashMap<Long, Map<Long, ActivityRole>> getActivityRoleCacheMap() {
		return activityRoleCacheMap;
	}

	/*
	 * public ConcurrentHashMap<Long, Map<Long, ActivityGiftBag>>
	 * getActivityGiftCacheMap() { return activityGiftCacheMap; }
	 */

	public void setActivityRoleCacheMap(
			ConcurrentHashMap<Long, Map<Long, ActivityRole>> activityRoleCacheMap) {
		this.activityRoleCacheMap = activityRoleCacheMap;
	}

	public ConcurrentHashMap<Long, Activity> getActivityCacheMap() {
		return activityCacheMap;
	}

	public void setActivityCacheMap(
			ConcurrentHashMap<Long, Activity> activityCacheMap) {
		this.activityCacheMap = activityCacheMap;
	}

	/*
	 * public void setActivityGiftCacheMap( ConcurrentHashMap<Long, Map<Long,
	 * ActivityGiftBag>> activityGiftCacheMap) { this.activityGiftCacheMap =
	 * activityGiftCacheMap; }
	 */

	@Override
	public void handleOnServerStart() {
		// 把数据库的活动和礼包添加到缓存
		List<ActivityData> activities = activityDao.findAllStartTimeDesc();
		if (activities == null) {
			return;
		}
		for (ActivityData activitydata : activities) {
			long aid = activitydata.getId();
			Map<Long, ActivityGiftBag> giftBags = activityGiftBagDao
					.findByAid(aid);

			/*
			 * if (giftBags != null) { activityGiftCacheMap.put(aid,
			 * giftBags);// 如果为空，则该活动不存在奖励 }
			 */
			Activity activity = new Activity(activitydata, giftBags, 2);
			activityCacheMap.put(activitydata.getId(), activity);

		}
	}

	@Override
	public void handleOnServerShutdown() {
		activityCacheMap.clear();
		// activityGiftCacheMap.clear();
		activityRoleCacheMap.clear();
	}
	
	/**
	 * 统计点击量
	 * @author G_T_C
	 * @param role
	 * @param aid
	 */
	public void addClickNum(Role role, long aid){
		Map<Long, Integer> voteInfoMap = role.getVoteInfoMap();
		if(voteInfoMap != null && voteInfoMap.get(aid) != null && voteInfoMap.get(aid) == 0){//未点击
			voteInfoMap.put(aid, 1);//是否点击过 0为未点击，1为点击
			role.setVoteInfo(StringUtil.mapToString(voteInfoMap));
			role.markToUpdate("voteInfo");
			roleDao.updateProperty(role);
			activityDao.updateclickNum(aid);
		}
	}
	

	/**
	 * 角色登录加载活动
	 */
	/*	@Override
	public void handleOnRoleLogin(Role role) {
		if (role == null) {
			LogUtil.error("role is null!");
			return;
		}

		boolean hasJoin = false;
		// 检测有没有每日任务登录活动
		Activity loginActivity = findTargetActvity(ActivityType.DAYLOGIN);

		long rid = role.getRid();
		List<ActivityRole> list = activityRoleDao.findByRidAid(rid,
				activityCacheMap);
		Map<Long, ActivityRole> activityRoleMap = new HashMap<>();
		if (null != list && !list.isEmpty()) {
			for (ActivityRole ar : list) {
				activityRoleMap.put(ar.getAid(), ar);
				if (loginActivity != null
						&& loginActivity.getActivityData().getId() == ar
								.getAid()) {// 用户已经参加每天登录就送活动
					hasJoin = true;
				}
			}
			activityRoleCacheMap.put(rid, activityRoleMap);
		}
		// 每日登录就送活动
		if (loginActivity != null && !hasJoin) {
			Map<Long, ActivityGiftBag> activityGiftBagMap = loginActivity
					.getGiftBagMap();

			ActivityRole activityRole = new ActivityRole();
			activityRole.setAid(loginActivity.getActivityData().getId());
			activityRole.setProcess(1);
			activityRole.setRid(rid);
			activityRole.setStatus("");
			Map<Long, Integer> giftInfoMap = new HashMap<>();
			for (Long giftid : activityGiftBagMap.keySet()) {
				giftInfoMap.put(giftid, 1);

			}
			activityRole.setGiftInfoMap(giftInfoMap);
			activityRoleMap.put(activityRole.getAid(), activityRole);
			activityRoleCacheMap.put(rid, activityRoleMap);
			activityRoleDao.insert(activityRole);
		}
	}*/

	/**
	 * 根据活动类型查找活动
	 * @author G_T_C
	 * @param activityType
	 * @return
	 */
	private Activity findTargetActvity(int activityType) {
		Set<Long> keys = activityCacheMap.keySet();
		Activity targetActivity = null;
		if (null != keys) {
			for (Long id : keys) {
				Activity activity = activityCacheMap.get(id);
				ActivityData activityData = activity.getActivityData();
				if (null != activityData
						&& activityData.getType() == activityType
						&& activityData.getEndTime() > TimeUtil.time()) {
					targetActivity = activity;
					break;
				}
			}
		}

		return targetActivity;
	}

	/**
	 * 重置每日活动
	 */
/*	@Override
	public void handleOnNextDay() {
		boolean send = false;
		if (!activityRoleCacheMap.isEmpty()) {
			for (Long rid : activityRoleCacheMap.keySet()) {
				Map<Long, ActivityRole> map = activityRoleCacheMap.get(rid);
				if (null != map && !map.isEmpty()) {
					Activity loginActivity = findTargetActvity(ActivityType.DAYLOGIN);
					if (loginActivity != null
							&& loginActivity.getActivityData() != null
							&& loginActivity.getGiftBagMap() != null) {
						Long aid = loginActivity.getActivityData().getId();
						ActivityRole activityRole = map.get(aid);
						if (null != activityRole
								&& null != activityRole.getGiftInfoMap()) {
							Map<Long, Integer> giftInfoMap = activityRole
									.getGiftInfoMap();
							for (Long giftId : giftInfoMap.keySet()) {
								giftInfoMap.put(giftId, 1);
							}
							activityRole.setGiftInfoMap(giftInfoMap);
							send = true;
							// 更新数据库
							activityRoleDao.resetEveryDayActivity(aid,
									activityRole.getGiftInfo());
						}
					}

				}
				// 发送小红点。通知在线所有玩家
				if (send) {
					sendRedPoint();
				}
			}
		}
	}*/

/*	private void sendRedPoint() {
		GMsg_12002015.Builder builder = GMsg_12002015.newBuilder();
		GRedPoint.Builder red = GRedPoint.newBuilder();
		red.setType(1);
		red.setStatus(1);
		builder.setRedPoint(red);
		roleFunction.sendMessageToAll(Message.build(builder.build()));
	}*/

	/**
	 * 客户端请求领取奖励 检查玩家的活动是否完成，如果完成，则进行奖励发放 1、金币类型 2、钻石类型 3、背包道具物品类型
	 * 
	 */
	public boolean giveRewards(Player player, long id) {
		Role role = player.getRole();
		long rid = role.getRid();

		// 得到礼包
		Activity activity = activityCacheMap.get(id);
		if (null != activity && null != activity.getActivityData()
				&& null != activity.getGiftBagMap()) {
			if (activity.getActivityData().getRewardExpireTime() < TimeUtil
					.time()) {// 超过了领取截止时间
				return false;
			}

			Long aid = activity.getActivityData().getId();
			// 判断是否已经领取过了
			Map<Long, ActivityRole> activityRoleMap = activityRoleCacheMap
					.get(rid);
			if (null != activityRoleMap && null != activityRoleMap.get(aid)) {
				ActivityRole activityRole = activityRoleMap.get(aid);
				Map<Long, Integer> giftInfoMap = activityRole.getGiftInfoMap();

				List<ItemGet> list = new ArrayList<ItemGet>();
				Map<Long, ActivityGiftBag> giftBagMap = activity
						.getGiftBagMap();
				for (Long giftId : giftInfoMap.keySet()) {
					if (giftInfoMap.get(giftId) == 1
							&& null != giftBagMap.get(giftId)) {
						ItemGet item = new ItemGet();
						ActivityGiftBag giftBag = giftBagMap.get(giftId);
						Map<Integer, Integer> itemMap = StringUtil.stringToMap(
								giftBag.getReward(),
								StringUtil.DELIMITER_BETWEEN_ITEMS,
								StringUtil.DELIMITER_INNER_ITEM, Integer.class,
								Integer.class);
						if (null != itemMap && itemMap.size() > 0) {
							for (Integer itemId : itemMap.keySet()) {
								item.setId(itemId);
								item.setNum(itemMap.get(itemId));
								list.add(item);
							}
						}

					}
				}
				if (list.size() > 0) {
					// 1、调用公共接口发放奖励
					boolean b = itemFunction.getItems(role,
							JSONObject.encode(list),MoneyEvent.ACTIVITY);
					/* 更新缓存数据 更新数据库 */
					if (b) {

						for (Long giftId : giftInfoMap.keySet()) {
							if (giftInfoMap.get(giftId) == 1) {
								giftInfoMap.put(giftId, 2);
							}
						}
						activityRole.setProcess(1);
						activityRole.setGiftInfoMap(giftInfoMap);
						activityRole.markToUpdate("process");
						activityRole.markToUpdate("giftInfo");
						activityRoleDao.updateProcessAndGiftInfo(activityRole);
						GMsg_12002015.Builder builder = GMsg_12002015
								.newBuilder();
						GRedPoint.Builder red = GRedPoint.newBuilder();
						red.setType(1);
						red.setStatus(0);
						builder.setRedPoint(red);
						roleFunction.sendMessageToPlayer(rid, builder.build());
						return true;
					} else {
						LogUtil.warn("领取奖励 > 调用getItems接口异常,rid:" + rid
								+ " ， json:" + JSONObject.encode(list));
					}
				}

			}
		}
		return false;
	}

	/**
	 * 添加活动
	 * 
	 * @author G_T_C
	 * @param activity
	 */
	public ActivityData add(ActivityData activityData) {
		activityDao.insert(activityData);
		Activity activty = new Activity(activityData,
				new HashMap<Long, ActivityGiftBag>(), 2);
		activityCacheMap.put(activityData.getId(), activty);
		return activityData;
	}

	/**
	 * 修改活动
	 * 
	 * @author G_T_C
	 * @param activity
	 */
	public void edit(ActivityData activity) {
		activityDao.update(activity);
		// activityCacheMap.put(activity.getId(), activity);
	}

	/**
	 * 添加礼包
	 * 
	 * @author G_T_C
	 * @param giftBag
	 */
	public void addGiftBag(ActivityGiftBag giftBag) {
		activityGiftBagDao.insert(giftBag);
		Activity targetActivty = null;
		/*
		 * for(Integer id: activityCacheMap.keySet()){ Map<K, V> }
		 * 
		 * if (null == giftBagMap) { giftBagMap = new HashMap<>();
		 * giftBagMap.put(giftBag.getId(), giftBag);
		 * activityGiftCacheMap.put(giftBag.getAid(), giftBagMap); } else {
		 * giftBagMap.put(giftBag.getId(), giftBag); }
		 */
		for (Long id : activityCacheMap.keySet()) {
			Activity activity = activityCacheMap.get(id);
			if (null != activity && null != activity.getActivityData()) {
				ActivityData activityData = activity.getActivityData();
				if (activityData.getId() == giftBag.getAid()) {
					activity.getGiftBagMap().put(giftBag.getId(), giftBag);
					break;
				}
			}
		}
	}

}
