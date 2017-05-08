/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.csv.cache.MenjiRoomCache;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAdapter;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.ZTMenjiFunction;
import com.yaowan.server.game.model.struct.ZTMenjiRole;
import com.yaowan.server.game.model.struct.ZTMenjiTable;

/**
 * 
 *
 * @author zane
 */
@Component
public class MenjiQiPaiEvent extends EventHandlerAdapter {
	
	@Autowired
	private MenjiRoomCache menjiRoomCache;

	@Autowired
	private ZTMenjiFunction menjiFunction;

	@Autowired
	private RoleFunction roleFunction;
	

	@Override
	public int execute(Event event) {
		ZTMenjiTable table = (ZTMenjiTable) event.getParam()[0];
		
		int seat = table.getLastPlaySeat();
		ZTMenjiRole role = table.getMembers().get(seat - 1);
		menjiFunction.dealFold(table, role);
		return 0;
	}
	
	@Override
	public int getHandle() {
		return HandleType.MENJI_QI_PAI;
	}

	
}
