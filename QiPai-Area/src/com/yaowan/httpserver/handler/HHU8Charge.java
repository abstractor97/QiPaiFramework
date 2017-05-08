package com.yaowan.httpserver.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.google.gson.reflect.TypeToken;
import com.yaowan.framework.util.JSONObject;
import com.yaowan.framework.util.JsonUtil;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.service.ShopService;
import com.yaowan.server.game.u8.U8Platform;
import com.yaowan.util.MD5Util;

/**
 * U8平台回调
 * 格式
 * http格式:http://ip:port?action=HHClassName.methodName&param1=value1&param2=
 * value2&...
 * 该路径
 * http格式:http://ip:port?action=U8Charge.callback& 
 */
@Component
@Service("HHU8Charge")
public class HHU8Charge {
	@Autowired	
	RoleFunction roleFunction;
	
	@Autowired
	ShopService shopService;

	public Map<String,Object> callback(Map<String, String> params) {
		
//		productID：商品ID 
//		orderID: 订单号 
//		userID: 用户ID 
//		channelID: 渠道ID 
//		gameID: 游戏ID 
//		serverID：游戏服务器ID 
//		money:充值金额，单位分 
//		currency：货币类型，默认RMB 
//		extension：获取订单号服务器传过来的自定义参数，原样返回 
//		signType：签名类型，用 md5。 该字段不参与签名 
//		sign：签名值。 该字段不参与签名
		
//		Map extension = (Map)JsonUtil.decodeJson(params.get("extension"), Map.class);
		
		HashMap<String, String> extension = JSONObject.decode(params.get("extension"),
				new TypeToken<HashMap<String, String>>() {
				}.getType());
		
		int vAdd = Integer.valueOf(extension.get("vAdd").toString());		
		long rid = Long.valueOf(extension.get("rid").toString());
		
		
		
		
		//调用商场的函数增加物品
		Role role=roleFunction.getRoleByRid(rid);
		if(role==null){
			Map<String, Object> result = new HashMap<>();
			result.put("errno", 0);
			result.put("errmsg", "rid:error:not exist, rid="+rid);
			return result;
		}
		String order_id=params.get("order_id");
	
		String sign=params.get("sign");
		String md5Sign = MD5Util.getOrderSign(params, U8Platform.AppSecret);
		if(!sign.equals(md5Sign)){
			Map<String, Object> result = new HashMap<>();
			result.put("errno", 0);
			result.put("errmsg", "签名错误, sign="+sign);
			return result;
		}
		
		LogUtil.info(md5Sign+"--"+params);
		shopService.reChargeCallBack(role.getRid(), order_id);
		Map<String, Object> result = new HashMap<>();
		result.put("errno", 1);
		result.put("errmsg", order_id+":success:md5Sign"+md5Sign);
		return result;
	}
}
