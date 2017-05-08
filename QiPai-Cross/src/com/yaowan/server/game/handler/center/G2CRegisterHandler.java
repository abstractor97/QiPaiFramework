package com.yaowan.server.game.handler.center;

import org.springframework.stereotype.Component;

import com.google.protobuf.InvalidProtocolBufferException;
import com.yaowan.constant.CenterModule;
import com.yaowan.framework.core.handler.client.ClientExecutor;
import com.yaowan.framework.server.base.handler.CenterHandler;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.protobuf.center.CRegister.CMsg_22001001;


/**
 * @author zane
 *
 */
@Component
public class G2CRegisterHandler extends CenterHandler {

	@Override
	public int moduleId() {
		return CenterModule.REGISTER;
	}

	@Override
	public void register() {
		addExecutor(1, new ClientExecutor() {
			@Override
			public void doExecute(byte[] msgBody) {
				System.out.println("register to center success");
				CMsg_22001001 msg;
				try {
					msg = CMsg_22001001.parseFrom(msgBody);
	
					LogUtil.info("----------------------------------");
					LogUtil.info("本服>>>>"+msg.getServersList().get(0).getHost()+":"+msg.getServersList().get(0).getPort());
//					regService.saveServerInfo(msg);
				} catch (InvalidProtocolBufferException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
	}
}