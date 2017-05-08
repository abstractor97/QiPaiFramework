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
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.function.ZTDoudizhuFunction;
import com.yaowan.server.game.function.ZTMajiangFunction;
import com.yaowan.server.game.function.ZTMenjiFunction;

/**
 * 
 *
 * @author zane
 */
@Component
public class GameRunEvent extends EventHandlerAdapter{

	@Autowired
	private ZTMenjiFunction menjiFunction;
	
	@Autowired
	private ZTMajiangFunction majiangFunction;
	
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
			long time = game.getStartTime();
			if (game.getEndTime() > game.getStartTime()) {
				time = game.getEndTime();
			}
			//智能机器人进出场
			int dif = (int) (System.currentTimeMillis() - time) / 1000;
			// 5秒不准备
			if (dif >= 5
					&& System.currentTimeMillis() - game.getLastRobotCreate() > 5000) {
				//menjiFunction.robotOutIn(game);]
				game.setLastRobotCreate(System.currentTimeMillis());
			}
			
		} 
		//roomFunction.
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.GAME_RUNNING;
	}


    

}
