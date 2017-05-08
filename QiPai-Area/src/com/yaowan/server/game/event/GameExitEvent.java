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
import com.yaowan.server.game.function.DouniuFunction;
import com.yaowan.server.game.function.RoleFunction;
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
public class GameExitEvent extends EventHandlerAdapter{

	
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
	private DouniuFunction douniuFunction;

	@Autowired
	private RoleFunction roleFunction;

	@Override
	public int execute(Event event) {
		Game game = (Game) event.getParam()[0];
		Long id = (Long) event.getParam()[1];
		if(game.getGameType()==GameType.MENJI){
			menjiFunction.exitTable(game,id);
		} else if (game.getGameType() == GameType.MAJIANG) {
			majiangFunction.exitTable(game, id);
		} else if (game.getGameType() == GameType.ZXMAJIANG) {
			zxmajiangFunction.exitTable(game, id);
		} else if (game.getGameType() == GameType.CDMAJIANG) {
			cdmajiangFunction.exitTable(game, id);
		} else if (game.getGameType() == GameType.DOUDIZHU) {
			doudizhuFunction.exitTable(game, id);
		} else if (game.getGameType() == GameType.DOUNIU) {
			douniuFunction.exitTable(game, id);
		}
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.GAME_EXIT;
	}


    

}
