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
import com.yaowan.protobuf.game.GMahJong.GMsg_11011008;
import com.yaowan.protobuf.game.GMahJong.GMsg_11011011;
import com.yaowan.server.game.service.ZTMajiangService;

/**
 *
 * @author zane
 */
@Component
public class ZTMajiangHandler extends GameHandler{

    @Autowired
    private ZTMajiangService majiangService;
    
    @Override
    public int moduleId() {
        return GameModule.ZHAOTONG_MAJIANG;
    }

    @Override
    public void register() {

        addExecutor(1, 1000, false, new ServerExecutor() {
            @Override
            public void doExecute(Player player, byte[] data) throws Exception {
                
            	majiangService.playerPrepare(player);
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
                
                //发牌动画播放完成
            	majiangService.clientFlashFinish(player);
            }
        });
          
        addExecutor(8, 1000, false, new ServerExecutor() {
            @Override
            public void doExecute(Player player, byte[] data) throws Exception {
            	GMsg_11011008 msg = GMsg_11011008.parseFrom(data);
                  //玩家操作
            	majiangService.playHand(player, msg.getOption(),msg.getOperatePai().getPaiList());

            }
        });
        
        addExecutor(9, 1000, false, new ServerExecutor() {
            @Override
            public void doExecute(Player player, byte[] data) throws Exception {
            	
                  //玩家操作
            	//majiangService.endTable(player);

            }
        });

		addExecutor(11, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11011011 msg = GMsg_11011011.parseFrom(data);
				// 发牌动画播放完成
				majiangService.isAuto(player, msg.getIsAuto());
			}
		});
		
		addExecutor(16, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
//				GMsg_12011016 msg = GMsg_11011011.parseFrom(data);
				//玩家刷新手牌
				majiangService.refreshPai(player);
			}
		});
    }
    
}
