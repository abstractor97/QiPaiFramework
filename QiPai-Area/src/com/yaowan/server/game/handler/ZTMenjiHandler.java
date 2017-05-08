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
import com.yaowan.protobuf.game.GMenJi.GMsg_11013005;
import com.yaowan.server.game.service.ZTMenjiService;

/**
 *
 * @author zane
 */
@Component
public class ZTMenjiHandler extends GameHandler{

    @Autowired
    private ZTMenjiService menjiService;
    
    @Override
    public int moduleId() {
        return GameModule.ZHAOTONG_MENJI;
    }

    @Override
    public void register() {

        addExecutor(1, 1000, true, new ServerExecutor() {
            @Override
            public void doExecute(Player player, byte[] data) throws Exception {
                
            	menjiService.playerPrepare(player);
            }
        });
        
         addExecutor(2, 1000, false, new ServerExecutor() {
            @Override
            public void doExecute(Player player, byte[] data) throws Exception {
                
//                mahJongService.playerChangeRoom(member);
			}
		});

		addExecutor(3, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {

				// 发牌动画播放完成
				menjiService.clientFlashFinish(player);
			}
		});

		addExecutor(5, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {

				// 玩家操作
				GMsg_11013005 msg = GMsg_11013005.parseFrom(data);
				// 玩家操作
				menjiService.playHand(player, msg.getAction(), msg.getParam());

			}
		});
    }
    
}
