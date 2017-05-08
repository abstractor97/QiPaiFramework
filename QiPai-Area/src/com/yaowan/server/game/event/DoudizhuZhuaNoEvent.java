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
import com.yaowan.server.game.function.ZTDoudizhuFunction;
import com.yaowan.server.game.model.struct.ZTDoudizhuRole;
import com.yaowan.server.game.model.struct.ZTDoudizhuTable;

/**
 * 
 * 抓或者看牌
 * @author zane
 */
@Component
public class DoudizhuZhuaNoEvent extends EventHandlerAdapter{

	
	@Autowired
	private ZTDoudizhuFunction doudizhuFunction;

	@Autowired
	private RoleFunction roleFunction;

	@Override
	public int execute(Event event) {
		ZTDoudizhuTable table = (ZTDoudizhuTable) event.getParam()[0];
		
		int seat = table.getLastPlaySeat();
		ZTDoudizhuRole role = table.getMembers().get(seat-1);

	
		if (role.getLookedPai() == 0) {
			//重新倒计时
			doudizhuFunction.dealLook(table);
			
			
		}else{
			doudizhuFunction.dealZhua(table, 0);
		}
		
		
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.DOUDIZHU_ZHUA_NO;
	}


    

}
