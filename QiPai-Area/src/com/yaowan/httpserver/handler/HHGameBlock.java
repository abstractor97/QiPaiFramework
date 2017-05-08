package com.yaowan.httpserver.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.yaowan.framework.util.TimeUtil;
import com.yaowan.httpserver.handler.entity.ResultInfo;
import com.yaowan.httpserver.handler.entity.StatusCode;
import com.yaowan.model.struct.Game;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.model.data.dao.RoleDao;
/**
 * 被阻塞的游戏
 * @author YW0941
 *
 */
@Controller("HHGameBlock")
public class HHGameBlock {

	@Autowired
	private RoomFunction roomFunction;
	
	@Autowired
	private RoleDao roleDao;
	//已运行的时间
	private static long  RunningTime= 30*60*1000;
	
	public ResultInfo<String> waitJoinNumber(Map<String, String> parms){
		
		StringBuilder builder = new StringBuilder();
		Map<Integer, Integer> gameType2PlayerNumberMap = roomFunction.getWaitJoinPlayerNumber();
		for (Map.Entry<Integer, Integer> entry : gameType2PlayerNumberMap.entrySet()) {
			builder.append("gameType: ").append(entry.getKey()).append(",playerNumber: ").append(entry.getValue()).append(";");
		}
		
		ResultInfo<String> result = new ResultInfo<String>();
		result.setResult(StatusCode.CODE_OK);
		result.setResultMsg("");
		result.setData(builder.toString());
		
		return result;
		
	}
	/**
	 * 返回被卡住的游戏列表
	 * @return
	 */
	public ResultInfo<String> showList(Map<String, String> parms){
		StringBuilder builder = new StringBuilder();
		int count = 0;
		for (Game game : roomFunction.getRunningGames().values()) {
			if(game.getStartTime()+RunningTime < System.currentTimeMillis()){//过了30分钟，认为已经被卡住了
				builder.append("gameType:").append(game.getGameType()).append(",").append("roomId:").append(game.getRoomId()).append(",")
				.append("startTime:").append(TimeUtil.date(game.getStartTime())).append(";");
				count++;
			}
		}
		String xx = "!!!";
		ResultInfo<String> result = new ResultInfo<String>();
		result.setResult(StatusCode.CODE_OK);
		result.setResultMsg("total:"+count+xx);
		result.setData(builder.toString());
		
		return result;
	}
	/**
	 * 终止被卡住的游戏
	 * max 
	 * @return
	 */
	public ResultInfo<String> stop(Map<String, String> parms){
		int max = Integer.valueOf(parms.get("max"));
		StringBuilder builder = new StringBuilder();
		int count = 0;
		List<Game> games = new ArrayList<>();
		for (Game game : roomFunction.getRunningGames().values()) {
			if(game.getStartTime()+RunningTime < System.currentTimeMillis()){//过了30分钟，认为已经被卡住了
				games.add(game);
				count++;
				if(count == max){
					break;
				}
			}
		}
		
		ResultInfo<String> result = new ResultInfo<String>();
		count = 0;
		for (Game game : games) {
			try {
				if(game != null) {
					roomFunction.endGame(game);
					builder.append("gameType=").append(game.getGameType()).append(",").append("roomId=").append(game.getRoomId()).append(",")
					.append("startTime:").append(TimeUtil.date(game.getStartTime())).append(";");
					count ++;
				}
			} catch (Exception e) {
				ExceptionUtils.getStackTrace(e);
			}
		}
		result.setResult(StatusCode.CODE_OK);
		result.setResultMsg("total:"+count);
		result.setData(builder.toString());
		return result;
	}
}
