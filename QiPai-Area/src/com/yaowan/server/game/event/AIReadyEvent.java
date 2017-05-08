/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameStatus;
import com.yaowan.constant.GameType;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAdapter;
import com.yaowan.model.struct.Game;
import com.yaowan.server.game.event.type.HandleType;
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
public class AIReadyEvent extends EventHandlerAdapter{

	
	@Autowired
	private ZTMenjiFunction menjiFunction;
	
	@Autowired
	private ZTMajiangFunction majiangFunction;
	@Autowired
	private ZXMajiangFunction zxmajiangFunction;
	
	@Autowired
	private ZTDoudizhuFunction doudizhuFunction;

	@Autowired
	private RoleFunction roleFunction;
	
	@Autowired
	private RoomFunction roomFunction;

	@Override
	public int execute(Event event) {
		Game game = (Game) event.getParam()[0];
		Long id = (Long) event.getParam()[1];
		if(game.getGameType() == GameType.MAJIANG) {
			if(majiangFunction.getTable(game.getRoomId()) != null){
				game = majiangFunction.getTable(game.getRoomId()).getGame();
				if(game != null && game.getStatus() != GameStatus.RUNNING){
					roomFunction.AIready(game, id);
				}
			}
		} else if (game.getGameType() == GameType.ZXMAJIANG) {
			if(majiangFunction.getTable(game.getRoomId()) != null){
				game = zxmajiangFunction.getTable(game.getRoomId()).getGame();
				if(game != null && game.getStatus() != GameStatus.RUNNING){
					roomFunction.AIready(game, id);
				}
			}
		} else if (game.getGameType() == GameType.DOUDIZHU) {
			if(majiangFunction.getTable(game.getRoomId()) != null){
				game = doudizhuFunction.getTable(game.getRoomId()).getGame();
				if(game != null && game.getStatus() != GameStatus.RUNNING){
					roomFunction.AIready(game, id);
				}
			}
		}
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.AI_END_READY;
	}


    

}
