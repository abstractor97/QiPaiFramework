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
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.ZTMajiangFunction;
import com.yaowan.server.game.function.ZXMajiangFunction;
import com.yaowan.server.game.model.struct.ZTMaJongTable;
import com.yaowan.server.game.model.struct.ZTMajiangRole;

/**
 * 
 *
 * @author zane
 */
@Component
public class MajiangFreeGangEvent extends EventHandlerAdapter{

	
	@Autowired
	private ZTMajiangFunction majiangFunction;
	
	@Autowired
	private ZXMajiangFunction zxmajiangFunction;
	
	@Autowired
	private RoleFunction roleFunction;

	@Override
	public int execute(Event event) {
		ZTMaJongTable table = (ZTMaJongTable) event.getParam()[0];
		int seat = table.getWaitSeat();
		ZTMajiangRole role = table.getMembers().get(seat - 1);
		
		if (table.getGame().getGameType() == GameType.MAJIANG) {
			majiangFunction.dealFreeGang(table, role);	
		} 

		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.MAJIANG_FREE_GANG;
	}


    

}
