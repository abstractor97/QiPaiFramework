/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.event;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameType;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAdapter;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.protobuf.game.GCDMahJong.GMsg_12042011;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011011;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041011;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.CDMajiangFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.ZTMajiangFunction;
import com.yaowan.server.game.function.ZXMajiangFunction;
import com.yaowan.server.game.model.struct.ZTMaJongTable;
import com.yaowan.server.game.model.struct.ZTMajiangRole;
import com.yaowan.server.game.rule.CDMahJongRule;
import com.yaowan.server.game.rule.ZTMahJongRule;
import com.yaowan.server.game.rule.ZXMahJongRule;
/**
 * 
 *
 * @author zane
 */
@Component
public class MajiangOutPaiEvent extends EventHandlerAdapter{

	
	@Autowired
	private ZTMajiangFunction majiangFunction;
	
	@Autowired
	private ZXMajiangFunction zxmajiangFunction;
	
	@Autowired
	private CDMajiangFunction cdmajiangFunction;
	
	@Autowired
	private RoleFunction roleFunction;

	@Override
	public int execute(Event event) {
		ZTMaJongTable table = (ZTMaJongTable) event.getParam()[0];
		int seat = table.getLastPlaySeat();
		ZTMajiangRole role = table.getMembers().get(seat - 1);
		Integer pai = 0;
		try {
			
			pai = role.getPai().get(role.getPai().size() - 1);
		} catch (Exception e) {
			LogUtil.error(ExceptionUtils.getStackTrace(e));
		}
		
		if (table.getGame().getGameType() == GameType.MAJIANG) {
			if (role.getRole().isAuto() || pai == table.getLaiZiNum()) {
				pai = ZTMahJongRule.chosePai(role.getPai(), pai,
						table.getLaiZiNum());
			} else {
				role.setTimeOutNum(role.getTimeOutNum() + 1);
				if (role.getTimeOutNum() >= 2) {
					role.getRole().setAuto(true);
					GMsg_12011011.Builder builder = GMsg_12011011.newBuilder();
					builder.setIsAuto(1);
					roleFunction.sendMessageToPlayer(role.getRole().getRole()
							.getRid(), builder.build());
				}
			}
			majiangFunction.dealDisCard(table, pai);	
		} else if (table.getGame().getGameType() == GameType.ZXMAJIANG) {
			if (role.getRole().isAuto()) {
				pai = ZXMahJongRule.chosePai(role.getPai(), pai, role.getQueType());
			} else {
				role.setTimeOutNum(role.getTimeOutNum() + 1);
				if (role.getTimeOutNum() >= 2) {
					role.getRole().setAuto(true);
					GMsg_12041011.Builder builder = GMsg_12041011.newBuilder();
					builder.setIsAuto(1);
					roleFunction.sendMessageToPlayer(role.getRole().getRole()
							.getRid(), builder.build());
				}
			}
			zxmajiangFunction.dealDisCard(table, pai);
		} else if (table.getGame().getGameType() == GameType.CDMAJIANG) {
			if (role.getRole().isAuto()) {
				pai = CDMahJongRule.chosePai(role.getPai(), pai, role.getQueType());
			} else {
				role.setTimeOutNum(role.getTimeOutNum() + 1);
				if (role.getTimeOutNum() >= 2) {
					role.getRole().setAuto(true);
					GMsg_12042011.Builder builder = GMsg_12042011.newBuilder();
					builder.setIsAuto(1);
					roleFunction.sendMessageToPlayer(role.getRole().getRole()
							.getRid(), builder.build());
				}
			}
			cdmajiangFunction.dealDisCard(table, pai);
		}

		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.MAJIANG_OUT_PAI;
	}


    

}
