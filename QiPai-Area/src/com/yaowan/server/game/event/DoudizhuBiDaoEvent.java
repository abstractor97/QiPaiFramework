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
import com.yaowan.framework.util.LogUtil;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_12012011;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.ZTDoudizhuFunction;
import com.yaowan.server.game.model.struct.ZTDoudizhuRole;
import com.yaowan.server.game.model.struct.ZTDoudizhuTable;

/**
 * 
 *
 * @author zane
 */
@Component
public class DoudizhuBiDaoEvent extends EventHandlerAdapter{

	
	@Autowired
	private ZTDoudizhuFunction doudizhuFunction;

	@Autowired
	private RoleFunction roleFunction;

	@Override
	public int execute(Event event) {
		ZTDoudizhuTable table = (ZTDoudizhuTable) event.getParam()[0];
		ZTDoudizhuRole role = table.getMembers().get(
				table.getLastPlaySeat() - 1);

		// 必倒通知
		GMsg_12012011.Builder builder2 = GMsg_12012011.newBuilder();
		builder2.setAction(2);
		roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(),
				builder2.build());
		LogUtil.info("Action(2)" + role.getRole().getRole().getNick());
		doudizhuFunction.dealDao(table, 1);

		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.DOUDIZHU_BI_DAO;
	}


    

}
