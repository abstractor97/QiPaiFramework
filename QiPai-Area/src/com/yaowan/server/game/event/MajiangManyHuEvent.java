/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameType;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAdapter;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.protobuf.game.GBaseMahJong.OptionsType;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.CDMajiangFunction;
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
public class MajiangManyHuEvent extends EventHandlerAdapter{

	
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
	    List<Integer> huSeat = new ArrayList<Integer>();
	
	    //先从recevice里取出胡的座位号
	    if (table.getReceiveQueue().size() > 0) {
			for(Entry<Integer, OptionsType> entry1 : table.getReceiveQueue().entrySet()){
				if ( entry1.getValue() == OptionsType.ANNOUNCE_WIN) {
					huSeat.add(entry1.getKey());
				}
			}
		}
	    //再从canoption里取出胡的座位号
	    if (table.getCanOptions().size() > 0) {
	    	 for(Entry<Integer, List<OptionsType>> entry : table.getCanOptions().entrySet()){
	    		 if (entry.getValue().contains(OptionsType.ANNOUNCE_WIN)) {
	    			   if (table.getReceiveQueue().size() > 0) {
	    					for(Entry<Integer, OptionsType> entry1 : table.getReceiveQueue().entrySet()){
	    						if (entry1.getKey() != entry.getKey()
	    							&& entry.getValue().contains(OptionsType.ANNOUNCE_WIN)) {
	    							huSeat.add(entry.getKey());
	    						}
	    					}
	    				}else {
							huSeat.add(entry.getKey());
						}
				}
	    	 }
		}
	    LogUtil.info("自动触发多人胡:座位分别是:"+huSeat);
		ZTMajiangRole[] huRoles = new ZTMajiangRole[3];
		for(int i = 0 ; i < huSeat.size() ; i++){
			huRoles[i] = table.getMembers().get(huSeat.get(i) - 1);
		}

		if (table.getGame().getGameType() == GameType.MAJIANG) {
			majiangFunction.dealManyHu(table, huRoles);	
		} else if (table.getGame().getGameType() == GameType.ZXMAJIANG) {
		zxmajiangFunction.dealManyHu(table, huRoles);
		} else if (table.getGame().getGameType() == GameType.CDMAJIANG) {
			cdmajiangFunction.dealManyHu(table, huRoles);
		}
		table.getCanOptions().clear();
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.MAJIANG_MANY_HU;
	}


    

}
