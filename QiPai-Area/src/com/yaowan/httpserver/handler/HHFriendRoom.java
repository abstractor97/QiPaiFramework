package com.yaowan.httpserver.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yaowan.framework.util.StringUtil;
import com.yaowan.server.game.function.FriendRoomFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.model.data.entity.FriendRoom;
import com.yaowan.server.game.model.data.entity.Role;



/**
 * 
 * http格式:http://ip:port?action=HHClassName.methodName&param1=value1&param2=
 * value2&...
 * 
 */
@Service("HHFriendRoom")
public class HHFriendRoom {
	
	@Autowired
	private FriendRoomFunction friendRoomFunction;
	
	@Autowired
	private RoleFunction roleFunction;

	/**
	 * 获取房间列表
	 */
	public Map<String,Object> findRoom(Map<String,String> params)
			throws Exception {
		Map<String, Object> result = new HashMap<>();
		ConcurrentHashMap<Long, FriendRoom> friendRoomMap;
		List<Map<String,String>> listMap = new ArrayList<Map<String,String>>();
		try {
			friendRoomMap = friendRoomFunction.getFriendRoomMap();
			if(friendRoomMap != null){
				for(FriendRoom friendRoom : friendRoomMap.values()){
					Map<String,String> map = new HashMap<String,String>();
					Role role = roleFunction.getRoleByRid(friendRoom.getOwner());
					map.put("rid", String.valueOf(role.getRid()));
					map.put("openId", String.valueOf(role.getOpenId()));
					map.put("roomId", String.valueOf(friendRoom.getId()));
					map.put("roomId", String.valueOf(friendRoom.getId()));
					map.put("createTime", String.valueOf(friendRoom.getCreateTime()));
					listMap.add(map);
				}
			}
			result.put("success", 1);
			result.put("data", listMap);
			return result;
		} catch (Exception e) {
			// TODO: handle exception
			result.put("success", 0);
			result.put("data", listMap);
			return result;
		}
	}
	
	
	/**
	 * 强制解散房间
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> forceClearRoom(Map<String,String> params)
			throws Exception {
		Map<String, Object> result = new HashMap<>();
		long roomId = StringUtil.getLong(params.get("id"));
		try {
			boolean flag = friendRoomFunction.forceClear(roomId);
			if(flag){
				result.put("result", 1);
			}else{
				result.put("result", 0);
			}
			return result;
		} catch (Exception e) {
			// TODO: handle exception
			result.put("result", 0);
			return result;
		}
	}
	
	
}
