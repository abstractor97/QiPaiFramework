/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameType;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAdapter;
import com.yaowan.model.struct.Game;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.CDMajiangFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.function.ZTDoudizhuFunction;
import com.yaowan.server.game.function.ZTMajiangFunction;
import com.yaowan.server.game.function.ZTMenjiFunction;
import com.yaowan.server.game.function.ZXMajiangFunction;

/**
 * 
 *
 * @author zane
 */
@Component
public class ResetRoomEvent extends EventHandlerAdapter{

	@Autowired
	private ZTMenjiFunction menjiFunction;
	
	@Autowired
	private ZTMajiangFunction majiangFunction;
	
	@Autowired
	private ZXMajiangFunction zxmajiangFunction;
	
	@Autowired
	private CDMajiangFunction cdmajiangFunction;
	
	@Autowired 
	private ZTDoudizhuFunction doudizhuFunction;
	
	@Autowired
	private RoleFunction roleFunction;
	
	@Autowired
	private RoomFunction roomFunction;
	@Override
	public int execute(Event event) {
		Game game = (Game)event.getParam()[0];
		if (game.getGameType() == GameType.MENJI) {
			if(menjiFunction.getTable(game.getRoomId()) != null){
				menjiFunction.resetOverTable(game);
			}
		} else if (game.getGameType() == GameType.DOUDIZHU) {
			if(doudizhuFunction.getTable(game.getRoomId()) != null){
				doudizhuFunction.resetOverTable(game);
			}
		} else if (game.getGameType() == GameType.MAJIANG) {
			if(majiangFunction.getTable(game.getRoomId()) != null){
				majiangFunction.resetOverTable(game);
			}
		} else if (game.getGameType() == GameType.ZXMAJIANG) {
			if(zxmajiangFunction.getTable(game.getRoomId()) != null){
				zxmajiangFunction.resetOverTable(game);
			}
		} else if (game.getGameType() == GameType.CDMAJIANG) {
			if(cdmajiangFunction.getTable(game.getRoomId()) != null){
				cdmajiangFunction.resetOverTable(game);
			}
		}
		//roomFunction.
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.GAME_RESET;
	}


    

}
