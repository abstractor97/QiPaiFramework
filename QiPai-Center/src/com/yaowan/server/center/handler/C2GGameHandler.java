package com.yaowan.server.center.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.protobuf.ByteString;
import com.yaowan.constant.CenterModule;
import com.yaowan.framework.server.base.handler.CenterHandler;
import com.yaowan.model.struct.GameServer;
import com.yaowan.protobuf.center.CGame.CMsg_21100006;
import com.yaowan.protobuf.center.CGame.CMsg_21100007;
import com.yaowan.server.center.action.DispatchAction;
import com.yaowan.server.center.base.ServerCGExecutor;
import com.yaowan.server.center.service.GameService;

/**
 * 跨服对外服务层
 * 
 * @author zane
 *
 */
@Component
public class C2GGameHandler extends CenterHandler {
	@Autowired
	private GameService gameService;
	@Override
	public int moduleId() {
		return CenterModule.GAME;
	}

	@Override
	public void register() {
	
		addExecutor(6, 0, false, new ServerCGExecutor() {
			@Override
			public void doExecute(GameServer gameServer, byte[] data) throws Exception {
				CMsg_21100006 msg = CMsg_21100006.parseFrom(data);
				gameService.loginRole(gameServer,msg);
			}
		});
		
		addExecutor(7, 0, false, new ServerCGExecutor() {
			@Override
			public void doExecute(GameServer gameServer, byte[] data) throws Exception {
				CMsg_21100007 request = CMsg_21100007.parseFrom(data);
				int cmd = request.getCmd();
				if(request.getData() == null){
					DispatchAction.getAction(cmd).execute(gameServer, null);
				}else {
					ByteString bytesParam = request.getData();				
					DispatchAction.getAction(cmd).execute(gameServer, bytesParam.toByteArray());	
				}
			}
		});

	}
}
