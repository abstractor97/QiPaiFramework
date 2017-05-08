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
import com.yaowan.framework.util.LogUtil;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.CDMajiangFunction;
import com.yaowan.server.game.function.ZTMajiangFunction;
import com.yaowan.server.game.function.ZXMajiangFunction;
import com.yaowan.server.game.model.struct.ZTMaJongTable;

/**
 * 等待玩家操作
 *
 * @author zane
 */
@Component
public class MajiangWaitEvent extends EventHandlerAdapter{

	
	@Autowired
	private ZTMajiangFunction majiangFunction;

	@Autowired
	private ZXMajiangFunction zxmajiangFunction;

	@Autowired
	private CDMajiangFunction cdmajiangFunction;

	@Override
	public int execute(Event event) {
		ZTMaJongTable table = (ZTMaJongTable) event.getParam()[0];
		int seat = table.getLastPlaySeat();
		
		table.setLastPlaySeat(table.getNextPlaySeat());
		table.setNextSeat(table.getNextPlaySeat());
		LogUtil.info(table.getGame().getRoomId()+" seat "+seat +" MAJIANG_wait ");
		
		if (table.getGame().getGameType() == GameType.MAJIANG) {
			if (table.getPais().size() > 0) {	
				if (table.getWinners().size() >= table.getMembers().size() - 1) {
					majiangFunction.endTable(table);
				} else {
					if(table.getGame().isFriendRoom() && table.getCanOptions() != null && table.getCanOptions().size() > 0){
						majiangFunction.tableToWait(table, table.getLastPlaySeat(),
								table.getNextPlaySeat(), HandleType.MAJIANG_WAIT,
								System.currentTimeMillis() + 50);
					}else{
						majiangFunction.tableToWait(table, table.getLastPlaySeat(),
								table.getNextPlaySeat(), HandleType.MAJIANG_GET_PAI,
								System.currentTimeMillis() + 50);
					}
					
				}
			} else {
				majiangFunction.endTable(table);
			}	
		} else if (table.getGame().getGameType() == GameType.ZXMAJIANG) {
			if (table.getPais().size() > 0) {	
				if (table.getWinners().size() >= table.getMembers().size() - 1) {
					zxmajiangFunction.endTable(table);
				} else {
					if(table.getGame().isFriendRoom() && table.getCanOptions() != null && table.getCanOptions().size() > 0){
						zxmajiangFunction.tableToWait(table, table.getLastPlaySeat(),
								table.getNextPlaySeat(), HandleType.MAJIANG_WAIT,
								System.currentTimeMillis() + 50);
					}else{
						zxmajiangFunction.tableToWait(table, table.getLastPlaySeat(),
								table.getNextPlaySeat(), HandleType.MAJIANG_GET_PAI,
								System.currentTimeMillis() + 50);
					}
				}
			} else {
				zxmajiangFunction.endTable(table);
			}	
		} else if (table.getGame().getGameType() == GameType.CDMAJIANG) {
			if (table.getPais().size() > 0) {	
				if (table.getWinners().size() >= table.getMembers().size() - 1) {
					cdmajiangFunction.endTable(table);
				} else {
					cdmajiangFunction.tableToWait(table, table.getLastPlaySeat(),
							table.getNextPlaySeat(), HandleType.MAJIANG_GET_PAI,
							System.currentTimeMillis() + 50);
				}
			} else {
				cdmajiangFunction.endTable(table);
			}	
		}
		
		if(table.getGame().isFriendRoom() && table.getCanOptions() != null){
			
		}else{
			table.getCanOptions().clear();
		}
		
		/*boolean flag = true;
		flag = table.getCanOptions().size()>0?true:false;
        for(Map.Entry<Integer, List<GMahJong.OptionsType>> entry:table.getCanOptions().entrySet()){
        	ZTMajiangRole role =table.getMembers().get(entry.getKey()-1);
        	if(!role.getRole().isRobot()){
        		flag = false;
        		break;
        	}
		}
        if(flag){
        	for(Map.Entry<Integer, List<GMahJong.OptionsType>> entry:table.getCanOptions().entrySet()){
            	ZTMajiangRole role =table.getMembers().get(entry.getKey()-1);
            	if(entry.getValue().contains(OptionsType.PENG)){
            		majiangFunction.dealPeng(table, role);
            	}else if(entry.getValue().contains(OptionsType.FREE_PENG)){
            		majiangFunction.dealFreePeng(table, role);
            	}else if(entry.getValue().contains(OptionsType.EXPOSED_GANG)){
            		majiangFunction.dealGang(table, role);
            	}else if(entry.getValue().contains(OptionsType.FREE_EXPOSED_GANG)){
            		majiangFunction.dealFreeGang(table, role);
            	}else{
            		table.setLastPlaySeat(table.getNextPlaySeat());
            		table.setNextSeat(table.getNextPlaySeat());
            		LogUtil.info(table.getGame().getRoomId()+" seat "+seat +" MAJIANG_wait ");
            		
            		if (table.getPais().size() > 0) {	
            			if (table.getWinners().size() >= table.getMembers().size() - 1) {
            				majiangFunction.endTable(table);
            			} else {
            				majiangFunction.tableToWait(table, table.getLastPlaySeat(),
            						table.getNextPlaySeat(), HandleType.MAJIANG_GET_PAI,
            						System.currentTimeMillis() + 200);
            			}
            		} else {
            			majiangFunction.endTable(table);
            		}
            	}
            	
            	break;
            	
    		}
        	table.getCanOptions().clear();
        }else{
        	
        	table.setLastPlaySeat(table.getNextPlaySeat());
    		table.setNextSeat(table.getNextPlaySeat());
    		LogUtil.info(table.getGame().getRoomId()+" seat "+seat +" MAJIANG_wait ");
    		
    		if (table.getPais().size() > 0) {	
    			if (table.getWinners().size() >= table.getMembers().size() - 1) {
    				majiangFunction.endTable(table);
    			} else {
    				majiangFunction.tableToWait(table, table.getLastPlaySeat(),
    						table.getNextPlaySeat(), HandleType.MAJIANG_GET_PAI,
    						System.currentTimeMillis() + 200);
    			}
    		} else {
    			majiangFunction.endTable(table);
    		}
    		table.getCanOptions().clear();
        }*/
		
		
		
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.MAJIANG_WAIT; 
	}


    

}
