package com.yaowan.httpserver.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.yaowan.framework.util.LogUtil;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.service.ShopService;
import com.yaowan.util.MD5Util;

/**
 * 格式
 * http格式:http://ip:port?action=HHClassName.methodName&param1=value1&param2=
 * value2&...
 * 该路径
 * http格式:http://ip:port?action=Recharge.methodName&message=value 
 */
@Component
@Service("HHCharge")
public class HHCharge {
	
	public HHCharge(){
		LogUtil.info("Load HHCharge ......");
	}
	@Autowired	
	RoleFunction roleFunction;
	
	@Autowired
	ShopService shopService;

	public Map<String,Object> callback(Map<String, String> params) {
		//调用商场的函数增加物品
		Role role=roleFunction.getRoleByOpenId(params.get("open_id"));
		if(role==null){
			Map<String, Object> result = new HashMap<>();
			result.put("errno", 0);
			result.put("errmsg", "open_id:error:not exist"+params.get("open_id"));
			return result;
		}
		String order_id=params.get("order_id");
		String platform=params.get("platform");
		String channel=params.get("channel");
		String open_id=params.get("open_id");
		String money=params.get("money");
		String server_id=params.get("server_id");
		String role_id=params.get("role_id");
		String goods_id=params.get("goods_id");
		String ts=params.get("ts");
		String sign=params.get("sign");
		String md5Sign = MD5Util.getOrderSign(params, "c3t4bpfuup6efag7m");
		LogUtil.info(md5Sign+"--"+params);
		shopService.reChargeCallBack(role.getRid(), order_id);
		Map<String, Object> result = new HashMap<>();
		result.put("errno", 1);
		result.put("errmsg", order_id+":success:md5Sign"+md5Sign);
		return result;
	}
}
