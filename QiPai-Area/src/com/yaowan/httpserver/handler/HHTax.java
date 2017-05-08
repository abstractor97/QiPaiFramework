package com.yaowan.httpserver.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yaowan.framework.util.StringUtil;
import com.yaowan.server.game.function.TaxFunction;



/**
 * 
 * http格式:http://ip:port?action=HHClassName.methodName&param1=value1&param2=
 * value2&...
 * 
 */
@Service("HHTax")
public class HHTax {
	
	@Autowired
	private TaxFunction taxFunction;

	/**
	 * 后台更新抽水（目前只适用于焖鸡）
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> addTax(Map<String,String> params)
			throws Exception {
		Map<String, Object> result = new HashMap<>();
		Byte gameType = StringUtil.getByte(params.get("gameType"));
		int roomType = StringUtil.getInt(params.get("roomType"));
		int taxCount = StringUtil.getInt(params.get("taxCount"));
		try {
			taxFunction.addTax(gameType, roomType, taxCount);
			result.put("result", 1);
			return result;
		} catch (Exception e) {
			// TODO: handle exception
			result.put("result", 0);
			return result;
		}
	}
	
	/**
	 * 
	 */
	public Map<String,Object> updateTax(Map<String,String> params)
			throws Exception {
		Map<String, Object> result = new HashMap<>();
		long id = StringUtil.getLong(params.get("id"));
		int taxCount = StringUtil.getInt(params.get("taxCount"));
		try {
			boolean flag = taxFunction.updateTax(id, taxCount);
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
	
	/**
	 * 查询各个游戏的抽水情况（目前只有焖鸡）
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> findAllTax(Map<String,String> params)
			throws Exception {
		Map<String, Object> result = new HashMap<>();
		
		result.put("data", taxFunction.getAllTax());
		return result;
	}

	
}
