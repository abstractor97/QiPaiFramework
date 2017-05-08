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
public class MajiangAnGangEvent extends EventHandlerAdapter{

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
		// 机器人才能确定
		
		if (table.getGame().getGameType() == GameType.MAJIANG) {
			int pai = majiangFunction.canDarkGang(table, role);
			if (pai > 0) {
				majiangFunction.dealAnGang(table, role, pai);
			} else {
				pai = ZTMahJongRule.chosePai(role.getPai(), pai,
						table.getLaiZiNum());
				majiangFunction.dealDisCard(table, pai);
			}
		} else if (table.getGame().getGameType() == GameType.ZXMAJIANG) {
			int pai = zxmajiangFunction.canDarkGang(table, role);
			if (pai > 0) {
				zxmajiangFunction.dealAnGang(table, role, pai);
			} else {
				pai = ZXMahJongRule.chosePai(role.getPai(), pai, role.getQueType());
				zxmajiangFunction.dealDisCard(table, pai);
			}	
		} else if (table.getGame().getGameType() == GameType.CDMAJIANG) {
			int pai = cdmajiangFunction.canDarkGang(table, role);
			if (pai > 0) {
				cdmajiangFunction.dealAnGang(table, role, pai);
			} else {
				pai = CDMahJongRule.chosePai(role.getPai(), pai, role.getQueType());
				cdmajiangFunction.dealDisCard(table, pai);
			}	
		}

		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.MAJIANG_AN_GANG;
	}


    

}
