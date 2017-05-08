package com.yaowan.httpserver.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import sun.util.logging.resources.logging;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.httpserver.handler.entity.ResultInfo;
import com.yaowan.httpserver.handler.entity.StatusCode;
import com.yaowan.model.struct.Game;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.model.data.dao.RoleDao;

@Controller("HHRoom")
public class HHRoom {

	@Autowired
	private RoomFunction roomFunction;
	
	@Autowired
	private RoleDao roleDao;
	
	/**
	 * http调用查询比赛id
	 */
	
	public ResultInfo<Map<String, String>> getRoomId(Map<String, String> parms) {
		LogUtil.info("paramter=" + StringUtil.mapToString(parms));
		ResultInfo<Map<String, String>> result = new ResultInfo<Map<String,String>>();
		JsonObject jsonObject = new JsonObject();
		Map<String, String> object = new HashMap<String, String>();
		try{
			long rid = Long.parseLong(parms.get("rid"));
			String nick = parms.get("nick");
			if(rid == 0) {
				rid = roleDao.getRidByNick(nick);
			}else {
				nick = roleDao.getNickdById(rid);
			}
			object.put("rid", String.valueOf(rid));
			object.put("nick", nick);
			Game game = roomFunction.getGameByRole(rid);
			if(game == null) {
				result.setResult(StatusCode.CODE_ERROR);
				result.setResultMsg("查找不到该rid的比赛信息");
			}else {
				object.put("roomId", String.valueOf(game.getRoomId()));
				result.setResult(StatusCode.CODE_OK);
				result.setResultMsg("查找成功");
				result.setData(object);
			}
		}catch(Exception e) {
			LogUtil.error(e);
		}
		
		return result;
	}
	
	/**
	 * http强制解散房间
	 */
	public ResultInfo<String> dismissRoomById(Map<String, String> parms) {
		LogUtil.info("paramter=" + StringUtil.mapToString(parms));
		ResultInfo<String> result = new ResultInfo<String>();
		try{
			long roomId = Long.parseLong(parms.get("roomid"));
			if(roomId != 0) {
				Game game = roomFunction.getGame(roomId);
				if(game != null) {
					roomFunction.endGame(game);
					result.setResult(StatusCode.CODE_OK);
					result.setResultMsg("解散成功");
				}else {
					result.setResult(StatusCode.CODE_ERROR);
					result.setResultMsg("没有查找到比赛房间");
				}
			}
		}catch (Exception e) {
			LogUtil.error(e);
		}
		
		return result;
	}
}
