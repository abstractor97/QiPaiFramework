package com.yaowan.httpserver.handler;



import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yaowan.framework.util.JSONObject;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GLogin.GMsg_12001004;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.model.data.entity.Role;



/**
 * 
 * http格式:http://ip:port?action=HHClassName.methodName&param1=value1&param2=
 * value2&...
 * 
 */
@Service("HHRole")
public class HHRole {

	@Autowired
	private RoleFunction roleFunction;


	

	/**
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> roleInfoUpdate(Map<String, String> params)
			throws Exception {
		Map<String, Object> result = new HashMap<>();

		long rid = StringUtil.getLong(params.get("rid"));
		String openId = params.get("openId");
		Role role = roleFunction.getRoleByRid(rid);
		if (role == null) {
			if (StringUtils.isNotBlank(openId)) {
				role  = roleFunction.getRoleByOpenId(openId);
				if (role !=null) {
					rid =role.getRid();
				}
			}
			// user not exsist
			if (role == null) {
				result.put("result", -1);
				return result;
			}
		}

		if (params.containsKey("gold")) {
			role.setGold(StringUtil.getInt(params.get("gold")));
			role.markToUpdate("gold");
		}
		if (params.containsKey("vip_level")) {
			role.setVipLevel(StringUtil.getShort(params.get("vip_level")));
			role.markToUpdate("vip_level");
		}

		roleFunction.updatePropertys(role);
		// success
		result.put("result", 1);
		LogUtil.debug("update role:" + JSONObject.encode(role));
		return result;
	}

	/**
	 * 角色禁言
	 * 
	 * @param params
	 * @throws Exception
	 */
	public Map<String, Object> chatForbid(Map<String, String> params) {

		String ridStr = params.get("rid");
		String[] rids = ridStr.split(",");

		int shutUp = Math.abs(StringUtil.getInt(params.get("shutUp")));
		int serverTime = TimeUtil.time();
		int shutUpTime = 0;
		// 为0接触禁言
		if (shutUp != 0) {
			if (serverTime + shutUp <= 0) {
				shutUpTime = Integer.MAX_VALUE;
			} else {
				shutUpTime = serverTime + shutUp;
			}
		}
		for (String rid : rids) {
			Role role = roleFunction.getRoleByRid(StringUtil.getLong(rid));
			if (role == null) {
				continue;
			}
			role.setShutUp(shutUpTime);
			role.markToUpdate("shutUp");
			roleFunction.updatePropertys(role);
		}
		Map<String, Object> result = new HashMap<>();
		result.put("result", 1);
		return result;
	}

	/**
	 * 角色封号
	 * 
	 * @param params
	 * @throws Exception
	 */
	public Map<String, Object> loginForbid(Map<String, String> params)
			throws Exception {
		LogUtil.info("paramter=" + StringUtil.mapToString(params));
		String ridStr = params.get("rid");
		String[] rids = ridStr.split(",");

		int forbid = Math.abs(StringUtil.getInt(params.get("forbid")));
		int serverTime = TimeUtil.time();
		int forbidTime = 0;
		// 大于0封号，等于0解封
		if (forbid != 0) {
			if (serverTime + forbid <= 0) {
				forbidTime = Integer.MAX_VALUE;
			} else {
				forbidTime = serverTime + forbid;
			}
		}
		// 封号发送重登错误
		GMsg_12001004.Builder res = GMsg_12001004.newBuilder();
		res.setForbidTime(forbidTime);
		

		for (String rid : rids) {
			Role role = roleFunction.getRoleByRid(StringUtil.getLong(rid));
			if (role == null) {
				continue;
			}
			role.setForbid(forbidTime);
			role.markToUpdate("forbid");
			roleFunction.updatePropertys(role);
			Player player = roleFunction.getPlayer(StringUtil.getLong(rid));
			if(player!=null){
				player.write(res.build());
			}	
		}
		Map<String, Object> result = new HashMap<>();
		result.put("result", 1);
		return result;
	}

	
	/**
	 * 角色禁言
	 * 
	 * @param params
	 * @throws Exception
	 */
	public Map<String, Object> dropAll(Map<String, String> params) {
		roleFunction.allOffline();
		Map<String, Object> result = new HashMap<>();
		result.put("result", 1);
		return result;
	}
}
