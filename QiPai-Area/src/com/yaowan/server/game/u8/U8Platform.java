package com.yaowan.server.game.u8;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

//import com.google.gson.reflect.TypeToken;
import com.yaowan.core.base.GlobalConfig;
import com.yaowan.framework.util.Http;
//import com.yaowan.framework.util.JSONObject;
import com.yaowan.framework.util.JsonUtil;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.util.MD5Util;

public class U8Platform {
	// 登录验证地址
	private static String AuthURL = "/user/verifyAccount";
	// 获取订单号
	private static String GetOrderID = "/pay/getOrderID";
	
	public static final String AppKey = "f32fdc02123a82524eb4ea95e1383d0b";
	//渠道分配给游戏的AppSecret
	public static final String AppSecret = "7513a2c235647e3213538c6eb329eec9";
	private static U8Platform instance = new U8Platform();
	
	
	public static U8Platform getInstance(){
		return instance;
	}
	/**
	 * 登录回调
	 * 
	 * @param u8UserId
	 * @param token
	 * @param sign
	 */
	public boolean verifyAccount(int u8UserId, String token) {
		
		String sign = MD5Util.encodeByMD5("userID="+u8UserId+"token="+token+AppKey);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userID", "" + u8UserId);
		params.put("token", token);
		params.put("sign", sign);

		String data = Http.sendPost(GlobalConfig.U8ServerURL+AuthURL, params);
		/**
		 * 返回(JSON格式)： { state: 1(登录认证成功)；其他失败 data: 认证成功才有数据，否则为空 {
		 * userID:U8Server生成的唯一用户ID username:U8Server生成的统一格式的用户名
		 *
		 * } }
		 */
		Map response = (Map)JsonUtil.decodeJson(data, Map.class);
//		Map<String, Object> response = JSONObject.decode(data);
		if("1.0".equals(response.get("state").toString()) || "1".equals(response.get("state").toString())){
			return true;
		}
		return false;
	}

	/**
	 * 获取订单号
	 * @param role
	 * @param serverID
	 * @param money
	 * @param productID
	 * @param productName
	 * @param extension
	 * @return
	 * 
	 * 返回(json格式)：
     *	{ 
     * 		state: 1（下单成功）；其他失败
     * 		data: 认证成功才有数据，否则为空
     *       {
     *           orderID:U8Server生成的唯一订单号，需要传到渠道SDK提供的游戏订单号字段，
     *              或者自定义字段。支付成功回调到U8Server的时候，需要根据该字段来获取订单信息。
     *           extension:当前渠道SDK的扩展数据。比如渠道有下单操作之后，这个字段里面，就存放了渠道SDK下单返回的数据。
     *       }
     *	}
	 */
	public String getOrderId(Role role, int serverID, int money,
			String productID, String productName, String extension) {
		if(productName == null || "".equals(productName)){
			productName = "productName";
		}
		
		String productDesc = "productDesc";

		Map<String, Object> params = new HashMap<String, Object>();
		
		money *= 100;//将元转成分
		
		StringBuilder sb = new StringBuilder();
		sb.append("userID=").append(role.getU8id()).append("&")
				.append("productID=")
				.append(productID == null ? "" : productID).append("&")
				.append("productName=")
				.append(productName).append("&")
				.append("productDesc=").append(productDesc).append("&").append("money=")
				.append(money).append("&").append("roleID=")
				.append(role.getRid()).append("&").append("roleName=")
				.append("").append("&").append("serverID=").append(serverID)
				.append("&").append("serverName=").append("").append("&")
				.append("extension=")
				.append(extension == null ? "" : extension);
		
		sb.append(AppKey);
		
		String encoded = null;
		try {
			encoded = URLEncoder.encode(sb.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		}
		
		String sign = MD5Util.encodeByMD5(encoded);
		
		params.put("userID", role.getU8id());
		params.put("productID", productID);
		params.put("productName", productName);
		params.put("productDesc", productDesc);
		params.put("money", money);
		params.put("roleID", role.getRid());
		params.put("serverID", serverID);
		params.put("extension", extension);
		params.put("sign", sign);
		
		String data = Http.sendPost(GlobalConfig.U8ServerURL+GetOrderID, params);
		
		return data;
//		Map response = (Map)JsonUtil.decodeJson(data, Map.class);
//		
////		HashMap<String, Map<String, String>> response = JSONObject.decode(data,
////				new TypeToken<HashMap<String, Map<String, String>>>() {
////				}.getType());
//		
////		Map<String, Object> response = JSONObject.decode(data);
//		if("1.0".equals(response.get("state").toString()) || "1".equals(response.get("state").toString())){
//			return ((Map)response.get("data")).get("orderID").toString();
//		}
//		return "";
	}

}
