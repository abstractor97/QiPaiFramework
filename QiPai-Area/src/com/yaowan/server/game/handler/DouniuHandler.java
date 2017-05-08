package com.yaowan.server.game.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GDouniu.GMsg_11025003;
import com.yaowan.protobuf.game.GDouniu.GMsg_11025007;
import com.yaowan.protobuf.game.GDouniu.GMsg_11025009;
import com.yaowan.protobuf.game.GDouniu.GMsg_11025011;
import com.yaowan.server.game.service.DouniuService;

/**
 *
 * @author zane
 */
@Component
public class DouniuHandler extends GameHandler{

    @Autowired
    private DouniuService douniuService;
    
    @Override
    public int moduleId() {
        return GameModule.DOUNIU;
    }

    @Override
    public void register() {

        addExecutor(1, 1000, true, new ServerExecutor() {
            @Override
            public void doExecute(Player player, byte[] data) throws Exception {
                
            	douniuService.playerPrepare(player);
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
				// 玩家操作
				GMsg_11025003 msg = GMsg_11025003.parseFrom(data);
				// 发牌动画播放完成
				douniuService.putChip(player, msg.getIndex(), msg.getChipIndex());
			}
		});
		

		
		addExecutor(7, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {

				// 玩家操作
				GMsg_11025007 msg = GMsg_11025007.parseFrom(data);
				// 玩家操作
				douniuService.enterTable(player, msg.getRoomId());

			}
		});
		
		addExecutor(9, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {

				// 玩家操作
				GMsg_11025009 msg = GMsg_11025009.parseFrom(data);
				// 玩家操作
				douniuService.applyOwner(player);

			}
		});
		
		addExecutor(11, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {

				// 玩家操作
				GMsg_11025011 msg = GMsg_11025011.parseFrom(data);

				douniuService.cancelOwner(player);

			}
		});
    }
    
}
