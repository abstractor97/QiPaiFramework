/**
 * 
 */
package com.yaowan.server.center.service;

import java.nio.ByteBuffer;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.GameServer;
import com.yaowan.protobuf.center.CGame.CMsg_21100006;
import com.yaowan.protobuf.center.CGame.CMsg_22100006;
import com.yaowan.protobuf.cmd.CMD;
import com.yaowan.server.center.action.CenterAction;
import com.yaowan.server.center.function.CGameFunction;
import com.yaowan.server.center.function.CRegisterFunction;
import com.yaowan.server.center.function.CRoleFunction;
import com.yaowan.server.center.model.data.CenterRole;
import com.yaowan.server.center.model.data.Server;

/**
 * @author zane
 * 
 * 用户注册
 */
@Component
public class GameService {
	
	@Autowired
	private CRoleFunction roleFunction;
	
	@Autowired
	private CGameFunction gameFunction;
	@Autowired
	private CRegisterFunction cRegisterFunction;
	
	public void loginRole(GameServer gameServer, CMsg_21100006 msg) {
		
		int lastServerId = 0; //最后次登录的服务器ID
		CenterRole centerRole = roleFunction.getCenterRole(msg.getRole()
				.getRid());
		if (centerRole == null) {
			centerRole = new CenterRole();
			centerRole.setChannel(1);
			centerRole.setCreateTime(TimeUtil.time());
			centerRole.setNickname(msg.getRole().getNick());
			centerRole.setOpenId(msg.getRole().getOpenId());
			centerRole.setRid(msg.getRole().getRid());
			centerRole.setServerId(gameServer.getServerId());
			centerRole.setLoginTime(TimeUtil.time());
			centerRole.setPlatform(msg.getRole().getPlatform());
			centerRole.setServerId(msg.getRole().getServerId());
			centerRole.setIp(gameServer.getIp());
			roleFunction.addCenterRole(centerRole);
		} else {
			lastServerId = centerRole.getServerId();
			
			centerRole.setLoginTime(TimeUtil.time());
//			if (!centerRole.getNickname().equals(msg.getRole().getNick())) {
//				centerRole.addProperty("nickname", msg.getRole().getNick());
//			}
//			if (centerRole.getServerId() != gameServer.getServerId()) {
//				centerRole.addProperty("serverId", gameServer.getServerId());
//			}
			
			centerRole.setNickname(msg.getRole().getNick());
			centerRole.setPlatform(msg.getRole().getPlatform());
			centerRole.setServerId(msg.getRole().getServerId());
			centerRole.setIp(gameServer.getIp());
			roleFunction.updateCenterRole(centerRole);
		}
		CMsg_22100006.Builder builder = CMsg_22100006.newBuilder();
		gameServer.write(builder.build());
		LogUtil.info("centerRole" + centerRole.getNickname());
		
		if(lastServerId >0 && lastServerId != gameServer.getServerId()){
			//检验该用户是否还在此服上，如果在需要踢他下线
			GameServer lastGameServer = cRegisterFunction.getGameServer(lastServerId);
			ByteBuffer buffer = ByteBuffer.allocate(8);
			buffer.putLong(centerRole.getRid());
			CenterAction.sendResponse(lastGameServer, (short)CMD.CenterCMD.RepetitionLoginCheck_VALUE, buffer.array());
		}
	}
	//服务器列表
	public Collection<Server> getServers(){
		return gameFunction.getServerMap().values();
	}
	
}
