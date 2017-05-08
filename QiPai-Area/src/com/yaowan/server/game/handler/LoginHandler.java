/**
 * 
 */
package com.yaowan.server.game.handler;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GLogin.GMsg_11001001;
import com.yaowan.protobuf.game.GLogin.GMsg_11001005;
import com.yaowan.server.game.service.LoginService;



/**
 * @author zane
 *
 */
@Component
public class LoginHandler extends GameHandler {

	@Autowired
	private LoginService loginService;
	
	@Override
	public int moduleId() {
		return GameModule.LOGIN;
	}

	@Override
	public void register() {
		addExecutor(1, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11001001 msg = GMsg_11001001.parseFrom(data);
				loginService.login(player, msg.getOpenId(), msg.getTime(), msg.getSign(),msg.getImei(),msg.getPlatform(),msg.getToken(), (byte)msg.getDeviceType(), (byte)msg.getLoginType(),msg.getDeviceToken(),msg.getU8Id());
			}
		});
		
		// 心跳检测
		addExecutor(2, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] msgBody)
					throws Exception {
				loginService.heartbeat(player);
			}
		});

		addExecutor(3, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				loginService.logout(player);
			}
		});
		
		addExecutor(5, 1000, false,new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11001005 msg = GMsg_11001005.parseFrom(data);
				loginService.reConnect(player,msg.getRid(),msg.getOpenId());
			}
		});
	}
}
