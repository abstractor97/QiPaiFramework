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
import com.yaowan.model.struct.GameRole;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.CDMajiangFunction;
import com.yaowan.server.game.function.DouniuFunction;
import com.yaowan.server.game.function.NPCFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.function.RoomLogFunction;
import com.yaowan.server.game.function.ZTDoudizhuFunction;
import com.yaowan.server.game.function.ZTMajiangFunction;
import com.yaowan.server.game.function.ZTMenjiFunction;
import com.yaowan.server.game.function.ZXMajiangFunction;
import com.yaowan.server.game.model.data.entity.Npc;
import com.yaowan.server.game.model.data.entity.Role;

/**
 * 
 *
 * @author zane
 */
@Component
public class GameEnterEvent extends EventHandlerAdapter{

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
	private RoomFunction roomFunction;
	@Autowired
	private RoomLogFunction roomLogFunction;
	
	@Autowired
	private NPCFunction NPCFunction;
	
	@Autowired
	private DouniuFunction douniuFunction;

	@Override
	public int execute(Event event) {
		Game game = (Game) event.getParam()[0];
		if (game.getStatus() == GameStatus.RUNNING) {
			if (game.getGameType() == GameType.MENJI) {
				Role role = (Role) event.getParam()[1];
				menjiFunction.enterTable(game,role);	
			} else if (game.getGameType() == GameType.DOUDIZHU) {
				doudizhuFunction.enterTable(game,getGameRole(game,event.getParam()[1]));
			} else if (game.getGameType() == GameType.MAJIANG) {
				majiangFunction.enterTable(game,getGameRole(game,event.getParam()[1]));
			} else if (game.getGameType() == GameType.ZXMAJIANG) {
				zxmajiangFunction.enterTable(game,getGameRole(game,event.getParam()[1]));
			} else if (game.getGameType() == GameType.CDMAJIANG) {
				cdmajiangFunction.enterTable(game,getGameRole(game,event.getParam()[1]));
			}else if (game.getGameType() == GameType.DOUNIU) {
				Role role = (Role) event.getParam()[1];
				douniuFunction.enterTable(game,role);
			}
		}else if (game.getStatus() == GameStatus.WAIT_READY){
			game.setStartTime(System.currentTimeMillis());
			Role role = (Role) event.getParam()[1];
			if (game.getGameType() == GameType.MENJI) {
				menjiFunction.enterTable(game,role);
			}else if (game.getGameType() == GameType.DOUNIU) {
				douniuFunction.enterTable(game,role);
			}else{
				Npc npc = NPCFunction.getNpcById(game.getGameType(), game.getRoomType(), role.getRid());
				GameRole gameRole = game.findEnterSeat(role);
				if(npc != null){
					gameRole.setWinTotal(npc.getWinTotal());
					gameRole.setWinWeek(npc.getWinWeek());
					gameRole.setCountTotal(npc.getCountTotal());
					gameRole.setCountWeek(npc.getCountWeek());
				}
				if (game.getGameType() == GameType.DOUDIZHU) {
					doudizhuFunction.enterTable(game,gameRole);
				}else if(game.getGameType() == GameType.MAJIANG){
					majiangFunction.enterTable(game,gameRole);
				} else if(game.getGameType() == GameType.ZXMAJIANG){
					zxmajiangFunction.enterTable(game,gameRole);
				} else if(game.getGameType() == GameType.CDMAJIANG){
					cdmajiangFunction.enterTable(game,gameRole);
				}	
			}
		}

		return 0;
	}
	
	private GameRole getGameRole(Game game,Object param){
		GameRole gameRole = null;
		if(param instanceof Role){
			Role role = (Role)param;
			gameRole = game.getSpriteMap().get(role.getRid());
		}else {
			gameRole = (GameRole)param;
		}
		return gameRole;
	}

	@Override
	public int getHandle() {
		return HandleType.GAME_ENTER;
	}


    

}
