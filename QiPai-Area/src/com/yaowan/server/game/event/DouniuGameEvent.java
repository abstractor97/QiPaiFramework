/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.csv.cache.NiuniuRoomCache;
import com.yaowan.csv.entity.NiuniuRoomCsv;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAdapter;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.DouniuFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.model.struct.DouniuTable;

/**
 * 
 *
 * @author zane
 */
@Component
public class DouniuGameEvent extends EventHandlerAdapter{

	@Autowired
	private NiuniuRoomCache niuniuRoomCache;
	
	@Autowired
	private DouniuFunction douniuFunction;

	@Autowired
	private RoleFunction roleFunction;

	@Override
	public int execute(Event event) {
		DouniuTable table = (DouniuTable) event.getParam()[0];

		NiuniuRoomCsv niuniuRoomCsv = niuniuRoomCache.getConfig(table.getGame()
				.getRoomType());
		
		douniuFunction.endTable(table);
		LogUtil.info(table.getGame().getRoomId()+" douniu_GAME ");
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.DOUNIU_RESULT;
	}
    

}
