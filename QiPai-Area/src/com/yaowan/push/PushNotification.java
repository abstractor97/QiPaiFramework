package com.yaowan.push;




import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.framework.push.model.DPushNotification;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.model.data.entity.Role;



/**
 * 推送定时器
 */
@Component
public class PushNotification {
	
	@Autowired
	public RoleFunction roleFunction;
	
	/**
	 * 推送数据比较器
	 */
	private Comparator<String> pushNotificationComparator = new Comparator<String>() {
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	};
	/**
	 * 推送map
	 */
	private Map<Long, Map<String, DPushNotification>> pushNotificationMap = new ConcurrentHashMap<Long, Map<String, DPushNotification>>();
	
	/**
	 * 加载数据库中的推送信息
	 */
	public void loadPushDatas() {
		
	}

	public void execute(){
		int now = TimeUtil.time();
		try {
			long t = System.nanoTime();
			int num = 0;
			// 把缓存中的数据加入到map中
			Iterator<Entry<Long, Map<String, DPushNotification>>> iterator = pushNotificationMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Long, Map<String, DPushNotification>> roleDPush = iterator.next();
				Map<String, DPushNotification> roleMap = roleDPush.getValue();
				if (roleMap.isEmpty()) {
					iterator.remove();
					continue;
				}
				Iterator<Entry<String, DPushNotification>> dpIterator = roleMap.entrySet().iterator();
				while (dpIterator.hasNext()) {
					Entry<String, DPushNotification> e = dpIterator.next();
					DPushNotification pushNotification = e.getValue();
					if (pushNotification.getIsRemove()) {
						roleMap.remove(e.getKey());
						continue;
					}
					if (pushNotification.getPushTime() < now) {
						try {
							// 推送时间到了，如果玩家不在线，发送推送；如果玩家在线，不发送
							// 删除这个推送任务
							if (!roleFunction.isOnline(pushNotification.getRid())) {
								Role role = roleFunction.getRoleByRid(pushNotification.getRid());
								if (role.getPushSwitch() == 1) {		//是否开启离线推送
									PushHelper.unicastPush(role, pushNotification.getMsg(), pushNotification.getTicker(), pushNotification.getTitle());
									num += 1;
								}
							}
						} catch (Exception e1) {
							String errorMsg = e1.getMessage();
							LogUtil.error(errorMsg);
						} finally {
							dpIterator.remove();
						}
					} else {
						continue;
					}
				}
			}
		} catch (Exception e) {
			String errorMsg = e.getMessage();
			LogUtil.error(errorMsg);
		}
	}

	/**
	 * 添加一条推送
	 * @author zhaozhiheng 2016年6月22日
	 * @param pushNotification
	 */
	private void addPushToMap(DPushNotification pushNotification) {
		Map<String, DPushNotification> tmap = pushNotificationMap.get(pushNotification.getRid());
		if (null == tmap) {
			tmap = new ConcurrentSkipListMap<String, DPushNotification>(pushNotificationComparator);
			tmap.put(pushNotification.toString(), pushNotification);
			pushNotificationMap.put(pushNotification.getRid(), tmap);
			return;
		}
		// 只合并资源已满推送
		boolean isAdd = true;
		for (Integer eventId : PushConst.getPushEvent()) {
			if (pushNotification.getEventId() == eventId) {
				for (Entry<String, DPushNotification> entry : tmap.entrySet()) {
					DPushNotification dPush = entry.getValue();
					// 只合并资源已满推送
					if (dPush.getIsRemove() || dPush.getEventId() != eventId) {
						continue;
					}
					// 非同一事物取最新的推送
					if (pushNotification.getExtendsionId() != dPush.getExtendsionId()) {
						if (pushNotification.getPushTime() < dPush.getPushTime()) {
							dPush.setIsRemove(true); // 删除掉之前的那个
						} else {
							isAdd = false;
						}
					} else {
						dPush.setIsRemove(true); // 删除掉之前的那个
					}
				}
			}
		}
		if (isAdd) {
			tmap.put(pushNotification.toString(), pushNotification);
		}
	}

	/**
	 * 添加一条推送
	 * @author ruan
	 * @param pushNotification 推送对象
	 */
	public void addPushNotification(DPushNotification pushNotification) {
		addPushToMap(pushNotification);
	}

	/**
	 * 添加一条推送(同步redis)
	 * @author ruan
	 * @param data
	 */
	public void addPushNotification(String data) {
//		if (!Config.serverType.equals(SceneServerType.Master)) {
//			return;
//		}
//		String[] arr = data.split(ChatServer.specialChatDelimiter);
//		if (arr.length <= 0) {
//			return;
//		}
//		addPushNotification(parseArray(arr));
	}
	
	/**
	 * 构造添加推送实体
	 * @author zhaozhiheng 2016年6月23日
	 * @param rid
	 * @param extendsionId
	 * @param eventId
	 * @param msg
	 * @param pushTime
	 * @return
	 */
	public DPushNotification addPushNotification(long rid, long extendsionId, int eventId, String msg, 
			int pushTime) {
		DPushNotification pushNotification = new DPushNotification();
		pushNotification.setRid(rid);
		pushNotification.setExtendsionId(extendsionId);
		pushNotification.setEventId(eventId);
		pushNotification.setMsg(msg);
		pushNotification.setPushTime(pushTime);
		pushNotification.setIsRemove(false);
		return pushNotification;
	}
	
	/**
	 * 构造删除推送实体
	 * @author zhaozhiheng 2016年6月23日
	 * @param rid
	 * @param extendsionId
	 * @param eventId
	 * @return
	 */
	public DPushNotification removePushNotification(long rid, long extendsionId, int eventId) {
		return addPushNotification(rid, extendsionId, eventId, "", 0);
	}

	/**
	 * 把字符串解析成DPushNotification对象
	 * @author ruan
	 * @param arr
	 * @return
	 */
	private DPushNotification parseArray(String[] arr) {
		DPushNotification pushNotification = new DPushNotification();
		pushNotification.setRid(Long.parseLong(arr[0]));
		pushNotification.setExtendsionId(Long.parseLong(arr[1]));
		pushNotification.setEventId(Integer.parseInt(arr[2]));
		pushNotification.setMsg(arr[3]);
		pushNotification.setPushTime(Integer.parseInt(arr[4]));
		pushNotification.setIsRemove(Boolean.parseBoolean(arr[5]));
		return pushNotification;
	}

	/**
	 * 删除一条推送
	 * @author ruan
	 * @param pushNotification 推送对象
	 */
	public void removePushNotification(DPushNotification pushNotification) {
		Map<String, DPushNotification> tmap = pushNotificationMap.get(pushNotification.getRid());
		if (null != tmap) {
			DPushNotification dpush = tmap.get(pushNotification.toString());
			if (dpush != null) {
				dpush.setIsRemove(true);
			}
		}
	}

	/**
	 * 删除一条推送(同步redis)
	 * @author ruan
	 * @param data
	 */
	public void removePushNotification(String data) {
//		if (!Config.serverType.equals(SceneServerType.Master)) {
//			return;
//		}
//		String[] arr = data.split(ChatServer.specialChatDelimiter);
//		if (arr.length <= 0) {
//			return;
//		}
//
//		removePushNotification(parseArray(arr));
	}
}