/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_11012005;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_11012010;
import com.yaowan.server.game.service.ZTDoudizhuService;

/**
 *
 * @author zane
 */
@Component
public class ZTDoudizhuHandler extends GameHandler {

	@Autowired
	private ZTDoudizhuService doudizhuService;

	@Override
	public int moduleId() {
		return GameModule.ZHAOTONG_DOUDIZHU;
	}

	@Override
	public void register() {

		addExecutor(1, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {

				doudizhuService.playerPrepare(player);
			}
		});

		addExecutor(3, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {

				// 发牌动画播放完成
				doudizhuService.clientFlashFinish(player);
			}
		});

		addExecutor(5, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11012005 msg = GMsg_11012005.parseFrom(data);

				// 玩家操作
				doudizhuService.playHand(player, msg.getAction(),msg.getOutPai().getPaiList());

			}
		});
		
	
		
		addExecutor(9, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				
				// 玩家操作
				//doudizhuService.isAuto(player);

			}
		});
		
		addExecutor(10, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11012010 msg = GMsg_11012010.parseFrom(data);

				// 玩家操作
				doudizhuService.isAuto(player, msg.getIsAuto());

			}
		});
	}

}
