package com.yaowan.httpserver.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.yaowan.constant.GameType;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.httpserver.handler.entity.ResultInfo;
import com.yaowan.httpserver.handler.entity.StatusCode;
import com.yaowan.model.struct.Game;
import com.yaowan.server.game.function.CDMajiangFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.function.ZTDoudizhuFunction;
import com.yaowan.server.game.function.ZTMajiangFunction;
import com.yaowan.server.game.function.ZTMenjiFunction;
import com.yaowan.server.game.function.ZXMajiangFunction;
import com.yaowan.server.game.model.struct.ZTDoudizhuRole;
import com.yaowan.server.game.model.struct.ZTDoudizhuTable;
import com.yaowan.server.game.model.struct.ZTMaJongTable;
import com.yaowan.server.game.model.struct.ZTMajiangRole;

/**
 * 格式
 * http格式:http://ip:port?action=HHClassName.methodName&param1=value1&param2=
 * value2&...
 * 该路径
 * http格式:http://ip:port?action=Broadcast.broadcast&message=value 
 */
@Component
@Service("HHGame")
public class HHGame {
	@Autowired
	RoleFunction roleFunction;
	
	@Autowired
	RoomFunction roomFunction;
	
	@Autowired
	ZTMajiangFunction majiangFunction;
	
	@Autowired
	ZXMajiangFunction zxmajiangFunction;
	
	@Autowired
	CDMajiangFunction cdmajiangFunction;
	
	@Autowired
	ZTMenjiFunction menjiFunction;
	
	@Autowired
	ZTDoudizhuFunction doudizhuFunction;
	
	/**
	 * http调用更改牌局
	 * @param params
	 */
	@SuppressWarnings("finally")
	public Object resetPai(Map<String, String> params) {

		HashMap<String, Integer> hm = new HashMap<String, Integer>();
		try {
			LogUtil.info("HHNotice content" + params.get("content"));
			long rid = Long.parseLong(params.get("rid"));
			Game game = roomFunction.getGameByRole(rid);
			if (game == null) {
				hm.put("result", 0);
			} else {
				if (game.getGameType() == GameType.MENJI) {
					menjiFunction.endTable(menjiFunction.getTable(game
							.getRoomId()));
				} else if (game.getGameType() == GameType.DOUDIZHU) {
					ZTDoudizhuTable table = doudizhuFunction.getTable(game
							.getRoomId());
					ZTDoudizhuRole role = doudizhuFunction.getRole(rid);
					doudizhuFunction.dealResult(
							doudizhuFunction.getTable(game.getRoomId()), role);
				} else if (game.getGameType() == GameType.MAJIANG) {
					ZTMaJongTable table = majiangFunction.getTable(game
							.getRoomId());
					ZTMajiangRole role = majiangFunction.getRole(rid);
					majiangFunction.endTable(table);
				} else if (game.getGameType() == GameType.ZXMAJIANG) {
					ZTMaJongTable table = zxmajiangFunction.getTable(game
							.getRoomId());
					zxmajiangFunction.endTable(table);
				} else if (game.getGameType() == GameType.CDMAJIANG) {
					ZTMaJongTable table = cdmajiangFunction.getTable(game
							.getRoomId());
					cdmajiangFunction.endTable(table);
				}
			}

			hm.put("result", 1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			hm.put("result", 0);
			e.printStackTrace();
		} finally {
			return hm;
		}
	}
	/**
	 * 运行的游戏数量
	 * @param parms
	 * @return
	 */
	public ResultInfo<String> running(Map<String, String> parms){
		ResultInfo<String> result = new ResultInfo<String>();
		result.setResult(StatusCode.CODE_OK);
		result.setResultMsg("total:"+roomFunction.getRunningGames().size());
		
		return result;
	}
}
