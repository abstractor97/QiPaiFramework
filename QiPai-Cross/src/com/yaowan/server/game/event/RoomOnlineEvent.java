package com.yaowan.server.game.event;

import org.springframework.stereotype.Component;

import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAddListenerAdapter;
import com.yaowan.server.game.event.type.HandleType;
/**
 * 游戏房在线
 * @author YW0941
 *
 */
@Component
public class RoomOnlineEvent extends EventHandlerAddListenerAdapter {

	@Override
	public int getHandle() {
		return HandleType.ROOM_ONLINE;
	}

	@Override
	public int process(Event event) {
		return 0;
	}

}
