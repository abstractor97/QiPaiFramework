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
import com.yaowan.server.game.function.ZXMajiangFunction;
import com.yaowan.server.game.model.struct.ZTMaJongTable;

/**
 * 等待玩家操作
 *
 * @author zane
 */
@Component
public class MajiangWaitShowDingQueEvent extends EventHandlerAdapter{

	@Autowired
	private ZXMajiangFunction zxmajiangFunction;

	@Override
	public int execute(Event event) {
		ZTMaJongTable table = (ZTMaJongTable) event.getParam()[0];	
		zxmajiangFunction.dealShowDingQue(table);
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.MAJIANG_WAIT_SHOW_DING_QUE;
	}
}
