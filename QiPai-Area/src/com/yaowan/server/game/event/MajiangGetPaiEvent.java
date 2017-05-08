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
import com.yaowan.model.struct.GameRole;
import com.yaowan.protobuf.game.GCDMahJong.GMsg_12042004;
import com.yaowan.protobuf.game.GCDMahJong.GMsg_12042005;
import com.yaowan.protobuf.game.GGame.PlayerState;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011004;
import com.yaowan.protobuf.game.GMahJong.GMsg_12011005;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041004;
import com.yaowan.protobuf.game.GZXMahJong.GMsg_12041005;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.CDMajiangFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.ZTMajiangFunction;
import com.yaowan.server.game.function.ZXMajiangFunction;
import com.yaowan.server.game.model.struct.ZTMaJongTable;
import com.yaowan.server.game.model.struct.ZTMajiangRole;

/**
 * 超时不准备重新匹配
 *
 * @author zane
 */
@Component
public class MajiangGetPaiEvent extends EventHandlerAdapter{

	
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
		table.setWaitSeat(0);
		table.setQiangGangHu(false);
		table.getWaiter().clear();
		table.setManyOperate(false);
		table.getReceiveQueue().clear();
		for (Map.Entry<Long, GameRole> entry : table.getGame().getSpriteMap()
				.entrySet()) {
			if (entry.getValue().getStatus() == PlayerState.PS_FLASH_VALUE) {
				entry.getValue().setStatus(PlayerState.PS_PLAY_VALUE);
			}
		}
		
		LogUtil.info("玩家摸牌座位:"+seat);

		if (seat == 0) {
			LogUtil.error("牌局结速了？" + table.getPais().size() + ":"
					+ table.getGame().getStatus());
			return 0;
		}
		
		if (table.getGame().getGameType() == GameType.MAJIANG) {
			if (table.getPais().size() == 0) {
				majiangFunction.endTable(table);
				return 0;
			}
			
			//李培光概率摸牌修改
			ZTMajiangRole role = table.getMembers().get(seat - 1);
			LogUtil.info("玩家数:"+table.getMembers().size());
			role.setHuFan(-1);
			Integer pai=majiangFunction.moPai(table, role);
			//李培光摸牌修改
			if(table.getGmMoPai()!=0&&role.getRole().getRole().getRid()==table.getGmrid()){
				pai = table.getGmMoPai();
				table.setGmMoPai(0);
			}
			
			majiangFunction.refreshPai(table, role);//在摸牌协议发过去之前 向客户端发送玩家的牌信息同步手牌
			//Integer pai = table.getPais().remove(0);
			//pai = 18;
			table.setMoPai(seat);
			table.setLastMoPai(pai);
			
			table.setLastRealSeat(seat);
			
			GMsg_12011004.Builder builder = GMsg_12011004.newBuilder();
			builder.setSeat(seat);

			GMsg_12011005.Builder builder2 = GMsg_12011005.newBuilder();
			builder2.setPai(pai);
			//
			role.getPai().add(pai);
			long rid = role.getRole().getRole().getRid();
			roleFunction.sendMessageToPlayer(rid, builder2.build());

			// 其他玩家
			List<Long> idList = new ArrayList<Long>();
			idList.addAll(table.getGame().getRoles());
			if (idList.size() > 0) {
				idList.remove(seat - 1);
			}
			
			
			roleFunction.sendMessageToPlayers(idList, builder.build());
			
			majiangFunction.checkSelfOption(table, role);
			
			majiangFunction.checkTingPai(table, role);
			
			LogUtil.info(table.getGame().getRoomId()+" seat "+seat + "ZT_MAJIANG_GET_PAI "+pai);
		} else if (table.getGame().getGameType() == GameType.ZXMAJIANG) {
			if (table.getPais().size() == 0) {
				zxmajiangFunction.endTable(table);
				return 0;
			}
			
			ZTMajiangRole role = table.getMembers().get(seat - 1);
			// 先取消弃牌/弃胡
			role.setLastQiPai(0);
			role.setHuFan(-1);
			zxmajiangFunction.refreshPai(table, role);//在摸牌协议发过去之前 向客户端发送玩家的牌信息同步手牌
			Integer pai=zxmajiangFunction.moPai(table, role);
			if(table.getGmMoPai() != 0 && role.getRole().getRole().getRid() == table.getGmrid()){
				pai = table.getGmMoPai();
				table.setGmMoPai(0);
			}
			//Integer pai = table.getPais().remove(0);
			//pai = 18;
			table.setMoPai(seat);
			table.setLastMoPai(pai);
			
			table.setLastRealSeat(seat);
			
			GMsg_12041004.Builder builder = GMsg_12041004.newBuilder();
			builder.setSeat(seat);

			GMsg_12041005.Builder builder2 = GMsg_12041005.newBuilder();
			builder2.setPai(pai);
			//
			role.getPai().add(pai);
			long rid = role.getRole().getRole().getRid();
			roleFunction.sendMessageToPlayer(rid, builder2.build());

			// 其他玩家
			List<Long> idList = new ArrayList<Long>();
			idList.addAll(table.getGame().getRoles());
			if (idList.size() > 0) {
				idList.remove(seat - 1);
			}
			
			roleFunction.sendMessageToPlayers(idList, builder.build());
			
			zxmajiangFunction.checkSelfOption(table, role);
			
			zxmajiangFunction.checkTingPai(table, role);
			
			LogUtil.info(table.getGame().getRoomId()+" seat "+seat + "ZX_MAJIANG_GET_PAI "+pai);
		} else if (table.getGame().getGameType() == GameType.CDMAJIANG) {
			if (table.getPais().size() == 0) {
				cdmajiangFunction.endTable(table);
				return 0;
			}
			
			ZTMajiangRole role = table.getMembers().get(seat - 1);
			// 先取消弃牌/弃胡
			role.setLastQiPai(0);
			role.setHuFan(-1);
			Integer pai = cdmajiangFunction.moPai(table, role);
			//李培光摸牌修改
			if(table.getGmMoPai() != 0 && role.getRole().getRole().getRid() == table.getGmrid()){
				pai = table.getGmMoPai();
				table.setGmMoPai(0);
			}

			table.setMoPai(seat);
			table.setLastMoPai(pai);
			table.setLastRealSeat(seat);
			
			GMsg_12042004.Builder builder = GMsg_12042004.newBuilder();
			builder.setSeat(seat);

			GMsg_12042005.Builder builder2 = GMsg_12042005.newBuilder();
			builder2.setPai(pai);
			//
			role.getPai().add(pai);
			long rid = role.getRole().getRole().getRid();
			roleFunction.sendMessageToPlayer(rid, builder2.build());

			// 其他玩家
			List<Long> idList = new ArrayList<Long>();
			idList.addAll(table.getGame().getRoles());
			if (idList.size() > 0) {
				idList.remove(seat - 1);
			}
			
			roleFunction.sendMessageToPlayers(idList, builder.build());
			
			cdmajiangFunction.checkSelfOption(table, role);
			
			cdmajiangFunction.checkTingPai(table, role);
			
			LogUtil.info(table.getGame().getRoomId()+" seat "+seat + "CD_MAJIANG_GET_PAI "+pai);
		}
				
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.MAJIANG_GET_PAI;
	}


    

}
