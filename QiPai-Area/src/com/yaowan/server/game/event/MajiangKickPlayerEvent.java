/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameType;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAdapter;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.protobuf.game.GGame.GMsg_12006014;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.CDMajiangFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.ZTMajiangFunction;
import com.yaowan.server.game.function.ZXMajiangFunction;
import com.yaowan.server.game.model.struct.ZTMaJongTable;
import com.yaowan.server.game.model.struct.ZTMajiangRole;

/**
 * 
 * 玩家在游戏中 金币为零然后不领取救济金和不充值的踢人事件
 * @author zane
 */
@Component
public class MajiangKickPlayerEvent extends EventHandlerAdapter{

	
	@Autowired
	private ZTMajiangFunction zTMajiangFunction;
	
	@Autowired
	private ZXMajiangFunction zxmajiangFunction;
	
	@Autowired
	private CDMajiangFunction cdmajiangFunction;
	
	@Autowired
	private RoleFunction roleFunction;

	@Override
	public int execute(Event event) {
		ZTMaJongTable table = (ZTMaJongTable) event.getParam()[0];
		if (table==null){
			return 0;
		}
		zTMajiangFunction.kickPlayer(table ,table.getPoChangRoles());
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.MAJIANG_KICK_PLAYER;
	}


    

}
