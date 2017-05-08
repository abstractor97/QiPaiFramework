/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameStatus;
import com.yaowan.csv.cache.MenjiRoomCache;
import com.yaowan.csv.entity.MenjiRoomCsv;
import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.core.thread.SingleThreadManager;
import com.yaowan.framework.core.thread.SingleThreadTask;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GGame.PlayerState;
import com.yaowan.protobuf.game.GMenJi;
import com.yaowan.protobuf.game.GMenJi.GMsg_12013001;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.function.ZTMenjiFunction;
import com.yaowan.server.game.model.struct.ZTMenjiRole;
import com.yaowan.server.game.model.struct.ZTMenjiTable;

/**
 * 昭通麻将
 *
 * @author zane
 */
@Component
public class ZTMenjiService {
	
	@Autowired
	private MenjiRoomCache menjiRoomCache;

	@Autowired
	private SingleThreadManager manager;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private RoomFunction roomFunction;
	
	@Autowired
	private ZTMenjiFunction menjiFunction;

	
	/**
	 * 玩家准备
	 *
	 * @param player
	 */
	public void playerPrepare(Player player) {
		final ZTMenjiRole role = menjiFunction.getRole(player.getRole().getRid());
		if (role == null) {
			GMsg_12013001.Builder builder = GMsg_12013001.newBuilder();
			builder.setSeat(0);
			player.write(builder.build());
			return;
		}
		ZTMenjiTable table = menjiFunction.getTableByRole(player.getRole().getRid());
		if (table == null) {
			GMsg_12013001.Builder builder = GMsg_12013001.newBuilder();
			builder.setSeat(0);
			player.write(builder.build());
			return;
		}
		
		manager.executeTask(new SingleThreadTask(table) {

			@Override
			public void doTask(ISingleData singleData) {
				ZTMenjiTable table = (ZTMenjiTable)singleData;
				menjiFunction.playerPrepare(table.getGame(), role.getRole());

			}
		});
	}
	

	/**
	 * 玩家操作
	 *
	 * @param player
	 * @param member
	 *            谁出牌
	 * @param optionsType
	 * @param pai
	 * @param laizi
	 */
	public void playHand(Player player, final GMenJi.MenJiAction action,
			final int param) {
		final ZTMenjiRole member = menjiFunction.getRole(player.getRole()
				.getRid());
		if (member == null) {
			LogUtil.error("出牌操作 找不到对应牌局 " + player.getRole().getNick());
			return;
		}
		
		long tableId = member.getRole().getRoomId();
		final ZTMenjiTable table = menjiFunction.getTable(tableId);

		if (table == null) {
			LogUtil.error("出牌操作 找不到对应牌局 table id " + tableId);
			return;
		}

		manager.executeTask(new SingleThreadTask(table) {
			
			@Override
			public void doTask(ISingleData singleData) {
				if (table.getGame().getStatus()!=GameStatus.RUNNING) {
					LogUtil.error("size"+table.getGame().getSpriteMap().size());
					return;
				}

				switch (action) {
				case MJ_LOOK: {// 看牌
					menjiFunction.dealLook(table, member);
					break;
				}
				case MJ_FOLD: {// 弃牌
					menjiFunction.dealFold(table, member);
					break;
				}
				case MJ_COMPETE: {// 比牌
					menjiFunction.dealCompete(table, member, param);
					break;
				}
				case MJ_FOLLOW: {// 跟
					//是否看牌，若看牌倍数为2
					int multiple = 1;
					if(member.getLook() == 1){
						multiple = 2;
					}
					int current = member.getRole().getRole().getGold()-member.getChip();
					if(current <= table.getBetsNum() * multiple){
						LogUtil.info("用户筹码不足");
						return;
					}
					menjiFunction.dealFollow(table, member);
					break;
				}
				case MJ_ADD: {// 加注
					MenjiRoomCsv menjiRoomCsv = menjiRoomCache.getConfig(table.getGame().getRoomType());
					List<Integer> list = StringUtil.stringToList(menjiRoomCsv.getRaiseOdds(),StringUtil.DELIMITER_BETWEEN_ITEMS,Integer.class);
					int data = param;
					if(data>list.size()){
						data = 1;
					}
					//是否看牌，若看牌倍数为2
					int multiple = 1;
					if(member.getLook() == 1){
						multiple = 2;
					}
					int current = member.getRole().getRole().getGold()-member.getChip();
					if(current<= list.get(data - 1) * multiple){
						LogUtil.info("用户筹码不足");
						return;
					}
					LogUtil.info("-----"+data);
					menjiFunction.dealAdd(table, member, data);
					break;
				}

				}
			}
		});
	}

	
	




	/**
	 * 客户端动画播放完成
	 *
	 * @param member
	 */
	public void clientFlashFinish(Player player) {
		ZTMenjiRole role = menjiFunction.getRole(player.getRole().getRid());
		if (role == null) {
			return;
		}
		ZTMenjiTable table = menjiFunction.getTableByRole(player.getRole().getRid());
		if (table == null) {
			return;
		}

		role.getRole().setStatus(PlayerState.PS_FLASH_VALUE);

		

		boolean isAllPrepare = true;
		List<ZTMenjiRole> members = table.getMembers();
		for (ZTMenjiRole otherRole : members) {
			if(otherRole == null){
				continue;
			}
			if (otherRole.getRole() != null
					&& otherRole.getRole().getStatus() != PlayerState.PS_FLASH_VALUE) {
				isAllPrepare = false;
			}
		}

		if (isAllPrepare) {
			// 全部已经准备 开始麻将
			/*manager.executeTask(new SingleThreadTask(table) {
				@Override
				public void doTask(ISingleData singleData) {
				
					ZTMenjiTable table = (ZTMenjiTable) singleData;
					menjiFunction.startTable(table.getGame());
				}
			});*/
		}
		
		// 暂时不需要完成
	}

	

}
