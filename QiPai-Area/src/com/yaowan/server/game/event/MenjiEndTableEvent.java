/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAdapter;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.ZTMenjiFunction;
import com.yaowan.server.game.model.struct.ZTMenjiTable;

/**
 * 
 *
 * @author zane
 */
@Component
public class MenjiEndTableEvent extends EventHandlerAdapter{

	
	@Autowired
	private ZTMenjiFunction menjiFunction;

	@Autowired
	private RoleFunction roleFunction;

	@Override
	public int execute(Event event) {
		ZTMenjiTable table = (ZTMenjiTable) event.getParam()[0];
		menjiFunction.endTable(table);
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.MENJI_END;
	}


    

}
