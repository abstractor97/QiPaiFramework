package com.yaowan.server.game.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAddListenerAdapter;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoomFunction;
@Component
public class GameServerStartEvent extends EventHandlerAddListenerAdapter {
	@Autowired
	private RoomFunction roomFunction;
	@Override
	public int getHandle() {
		
		return HandleType.GAME_SERVER_START;
	}

	public int process(Event event) {
		return 0;
	}
	
}
