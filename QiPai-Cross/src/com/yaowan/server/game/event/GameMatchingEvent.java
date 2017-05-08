package com.yaowan.server.game.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameType;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAddListenerAdapter;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoomFunction;
@Component
public class GameMatchingEvent extends EventHandlerAddListenerAdapter {
	@Autowired
	private RoomFunction roomFunction;
	@Override
	public int getHandle() {
		
		return HandleType.GAME_MATCHING;
	}

	public int process(Event event) {
		
		int realType = (Integer) event.getParam()[1];
		int gameType = roomFunction.getGameType(realType);
		//TODO 暂时处理
		if(GameType.DEZHOU == gameType){
			return 0;
		}
		
		
		
		return 0;
	}
	
}
