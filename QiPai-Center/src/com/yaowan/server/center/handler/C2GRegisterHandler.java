/**
 * 
 */
package com.yaowan.server.center.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.CenterModule;
import com.yaowan.framework.server.base.handler.CenterHandler;
import com.yaowan.model.struct.GameServer;
import com.yaowan.protobuf.center.CRegister.CMsg_21001001;
import com.yaowan.server.center.base.ServerCGExecutor;
import com.yaowan.server.center.service.RegisterService;

/**
 * @author huangyuyuan
 *
 */
@Component
public class C2GRegisterHandler extends CenterHandler {

	@Autowired
	private RegisterService registerService;
	
	@Override
	public int moduleId() {
		return CenterModule.REGISTER;
	}

	@Override
	public void register() {
		/**
		 * 当地区服和跨区服启动后，会通过此接口向中心服注册
		 */
		addExecutor(1, 0, false, new ServerCGExecutor() {
			@Override
			public void doExecute(GameServer gameServer, byte[] data) throws Exception {
				CMsg_21001001 msg = CMsg_21001001.parseFrom(data);
				registerService.gameServerRegister(gameServer, msg.getServerId(),msg.getOnline(),msg.getGameTypesList());
			}
		});
		
		addExecutor(2, new ServerCGExecutor() {
			@Override
			public void doExecute(GameServer gameServer, byte[] data) throws Exception {
				System.out.println("gameServer " + gameServer.getServerId() + " disconnect");
			}
		});
	}
}
