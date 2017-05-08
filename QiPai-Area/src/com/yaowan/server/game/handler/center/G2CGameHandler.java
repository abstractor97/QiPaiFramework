package com.yaowan.server.game.handler.center;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.protobuf.InvalidProtocolBufferException;
import com.yaowan.constant.CenterModule;
import com.yaowan.framework.core.handler.client.ClientExecutor;
import com.yaowan.framework.server.base.handler.CenterHandler;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.protobuf.center.CGame;
import com.yaowan.protobuf.center.CGame.CMsg_22100003;
import com.yaowan.server.game.center.DispatchReceive;
import com.yaowan.server.game.service.CenterService;

/**
 * 游戏服对中心服开放接口
 * 
 * @author zane
 *
 */
@Component
public class G2CGameHandler extends CenterHandler {

	@Autowired
	private CenterService centerService;

	@Override
	public int moduleId() {
		return CenterModule.GAME;
	}

	@Override
	public void register() {

		addExecutor(2,new ClientExecutor(){
			@Override
			public void doExecute(byte[] data){
				//CGame_12100002 msg = CGame_12100002.parseFrom(data);
				//centerService.centerLogin(player, msg.getOpenId(), msg.getTime(), msg.getSign(), msg.getImei());
			}
		});

		addExecutor(3, new ClientExecutor() {

			@Override
			public void doExecute(byte[] msgBody) {
				
				try {
					CMsg_22100003 msg = CMsg_22100003.parseFrom(msgBody);
					LogUtil.info("----------------------------------");
					//centerService.putGameInServer(msg.getGame());
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}

			}
		});
		
		addExecutor(6, new ClientExecutor() {
			@Override
			public void doExecute(byte[] msgBody) {
				//LogUtil.info(("login to center success"));
				// CGame_12100002 msg = CGame_12100002.parseFrom(data);
				// centerService.centerLogin(player, msg.getOpenId(),
				// msg.getTime(), msg.getSign(), msg.getImei());
			}
		});
		//接收服务器列表
		addExecutor(7, new ClientExecutor() {
			@Override
			public void doExecute(byte[] msgBody) {
				CGame.CMsg_22100007 response;
				try {
					response = CGame.CMsg_22100007.parseFrom(msgBody);
					DispatchReceive.getReceive(response.getCmd()).execute(response.getData().toByteArray());
				} catch (InvalidProtocolBufferException e) {
					LogUtil.error(e);
//					e.printStackTrace();
				}
				
			}
		});
	}
}
