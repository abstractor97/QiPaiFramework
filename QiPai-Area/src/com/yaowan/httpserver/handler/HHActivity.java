package com.yaowan.httpserver.handler;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yaowan.framework.util.BeanMapChangeUtil;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.httpserver.handler.entity.ResultInfo;
import com.yaowan.httpserver.handler.entity.StatusCode;
import com.yaowan.server.game.function.ActivityFunction;
import com.yaowan.server.game.model.data.entity.ActivityData;

/**
 * 活动
 * 
 * @author G_T_C
 */
@Service("HHActivity")
public class HHActivity {

	@Autowired
	private ActivityFunction activityFunction;

	/**
	 * 处理添加新活动
	 * 
	 * @author G_T_C
	 * @param map
	 * @return
	 */
	public ResultInfo<Integer> add(Map<Object, Object> map) {
		LogUtil.info("paramter=" + StringUtil.mapToString(map));
		ResultInfo<Integer> result = new ResultInfo<>();
		try {
			if (!checkParam(result, map)) {
				result.setErrno(StatusCode.CODE_ERROR);
				return result;
			}
			ActivityData activity = BeanMapChangeUtil.toBean(map, ActivityData.class);
			if (activity == null) {
				result.setErrno(StatusCode.CODE_ERROR);
				return result;
			}
			LogUtil.debug(activity.toString());
			activityFunction.add(activity);
			result.setResult(StatusCode.CODE_OK);
			result.equals(activity.getId());
		} catch (Exception e) {
			LogUtil.error(e);
		}
		return result;
	}

	/**
	 * 修改活动
	 * 
	 * @author G_T_C
	 * @param map
	 * @return
	 */
	public ResultInfo<String> edit(Map<Object, Object> map) {
		LogUtil.info("paramter=" + StringUtil.mapToString(map));
		ResultInfo<String> result = new ResultInfo<>();
		try {
			if (!checkParam(result, map)) {
				result.setErrno(StatusCode.CODE_ERROR);
				return result;
			}
			ActivityData activity = BeanMapChangeUtil.toBean(map, ActivityData.class);
			if (activity == null) {
				result.setErrno(StatusCode.CODE_ERROR);
				return result;
			}
			for(Object key : map.keySet()){
				if(!(key+"").equals("id")){
					activity.markToUpdate(key+"");
				}
			}
			LogUtil.debug(activity.toString());
			activityFunction.edit(activity);
			result.setResult(StatusCode.CODE_OK);
		} catch (Exception e) {
			LogUtil.error(e);
		}
		return result;
	}

	/**
	 * 添加礼包
	 * 
	 * @author G_T_C
	 * @param map
	 * @return
	 *//*
	public ResultInfo<String> addGift(Map<Object, Object> map) {
		LogUtil.info("paramter=" + StringUtil.mapToString(map));
		ResultInfo<String> result = new ResultInfo<>();
		try {
			if (!checkParam(result, map)) {
				result.setErrno(StatusCode.CODE_ERROR);
				return result;
			}
			ActivityGiftBag giftBag = BeanMapChangeUtil.toBean(map, ActivityGiftBag.class);
			activityFunction.addGiftBag(giftBag);
			result.setResult(StatusCode.CODE_OK);
		} catch (Exception e) {
			LogUtil.error(e);
		}
		return result;
	}
*/
	private <T>boolean checkParam(ResultInfo<T> result,
			Map<Object, Object> map) {
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
}
