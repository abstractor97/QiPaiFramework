package com.yaowan.server.center.action;

import java.nio.ByteBuffer;

import org.springframework.stereotype.Component;

import com.yaowan.model.struct.GameServer;
import com.yaowan.protobuf.cmd.CMD.CenterCMD;
/**
 * 修改在线人数
 * @author YW0941
 *
 */
@Component
public class OnlineAction extends CenterAction {

	public OnlineAction() {
		super((short)CenterCMD.OnlineAction_VALUE);
	}

	@Override
	public void execute(GameServer gameServer, byte[] data) {
		
		if(data == null || data.length == 0){
			gameServer.getOnline().incrementAndGet();
		}else {
			ByteBuffer buffer = ByteBuffer.allocate(4);
			buffer.put(data);
			gameServer.getOnline().set(buffer.getInt());
		}
	}

}
