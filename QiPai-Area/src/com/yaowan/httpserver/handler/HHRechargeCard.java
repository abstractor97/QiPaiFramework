package com.yaowan.httpserver.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.server.game.service.ExchangeService;

/**
 * 充值卡回调
 * 格式
 * http格式:http://ip:port?action=HHClassName.methodName&param1=value1&param2=
 * value2&...
 * 该路径
 * http格式:http://ip:port?action=Recharge.methodName&message=value 
 */
@Controller("HHRechargeCard")
public class HHRechargeCard {
	
	@Autowired
	private ExchangeService exchangeService;
	
	public Map<String, Object> callback(Map<String, String> params) {
		Map<String, Object> result = new HashMap<>();
		if(params == null){
			result.put("errmsg", "参数为空");
			return result;
		}
		LogUtil.info("paramter=" + StringUtil.mapToString(params));
		String msg=params.get("msg");
		String retcode=params.get("retcode");
		String orderId=params.get("orderId");
		result.put("result", 1);
		exchangeService.updateStatusInfo(Integer.parseInt(retcode), msg, orderId);
		return result;
	}
}
