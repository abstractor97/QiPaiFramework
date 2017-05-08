package com.yaowan.httpserver.handler;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.yaowan.framework.util.BeanMapChangeUtil;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.httpserver.handler.entity.ResultInfo;
import com.yaowan.httpserver.handler.entity.StatusCode;
import com.yaowan.server.game.model.data.entity.Exchange;
import com.yaowan.server.game.service.ExchangeService;

@Controller("HHExchange")
public class HHExchange {

	@Autowired
	private ExchangeService exchangeService;

	public ResultInfo<String> add(Map<Object, Object> map) {
		LogUtil.info("paramter=" + StringUtil.mapToString(map));
		ResultInfo<String> result = new ResultInfo<>();
		try {
			if (!checkParam(result, map)) {
				result.setErrno(StatusCode.CODE_ERROR);
				return result;
			}
			Exchange exchange = BeanMapChangeUtil.toBean(map, Exchange.class);
			boolean flag = exchangeService.addExchangeItem(exchange);
			if(!flag){
				result.setErrno(StatusCode.CODE_ERROR);
				result.setErrmsg("查找不到该itemId的item信息");
				LogUtil.error("查找不到该itemId的item信息! itemId="+exchange.getItemId());
				return result;
			}
			result.setResult(StatusCode.CODE_OK);
		} catch (Exception e) {
			LogUtil.error(e);
		}
		return result;
	}

	public ResultInfo<String> edit(Map<Object, Object> map) {
		LogUtil.info("paramter=" + StringUtil.mapToString(map));
		ResultInfo<String> result = new ResultInfo<>();
		try {
			if(null == map.get("id")){
				result.setResult(StatusCode.PARAM_MSG);
				return result;
			}
			if (!checkParam(result, map)) {
				result.setErrno(StatusCode.CODE_ERROR);
			
				return result;
			}
			Exchange exchange = BeanMapChangeUtil.toBean(map, Exchange.class);
			for(Object key : map.keySet()){
				if(!(key+"").equals("id")){
					exchange.markToUpdate(key+"");
				}
			}
		 exchangeService.updateExchangeItem(exchange);
			result.setResult(StatusCode.CODE_OK);
		} catch (Exception e) {
			LogUtil.error(e);
		}
		return result;
	}

/*	public ResultInfo<List<Exchange>> list(Map<Object, Object> map) {
		
		LogUtil.info("paramter=" + StringUtil.mapToString(map));
		ResultInfo<List<Exchange>> result = new ResultInfo<>();
		try {
			List<Exchange> list = exchangeService.findExchanges();
			result.setResult(StatusCode.CODE_OK);
			result.setData(list);
		} catch (Exception e) {
			LogUtil.error(e);
		}
		return result;
	}*/

	private <T> boolean checkParam(ResultInfo<T> result, Map<Object, Object> map) {
		if (map == null) {
			result.setErrno(StatusCode.CODE_ERROR);
			result.setErrmsg(StatusCode.PARAM_MSG);
			return false;
		}
		if (map.size() <= 0) {
			result.setErrno(StatusCode.CODE_ERROR);
			result.setErrmsg(StatusCode.PARAM_MSG);
			return false;
		}
		return true;
	}
	
	public ResultInfo<String> delete(Map<Object, Object> map) {
		LogUtil.info("paramter=" + StringUtil.mapToString(map));
		ResultInfo<String> result = new ResultInfo<>();
		long id = Long.parseLong(map.get("id")+"");
		 exchangeService.del(id);
		 result.setResult(StatusCode.CODE_OK);
		return result;
	}
}
