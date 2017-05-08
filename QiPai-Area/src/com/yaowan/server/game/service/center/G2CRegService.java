package com.yaowan.server.game.service.center;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.ObjectUtil;
import com.yaowan.protobuf.center.CRegister.CMsg_22001001;
import com.yaowan.protobuf.center.CRegister.CServerInfo;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.model.struct.ServerInfo;

/**
 * 游戏服对中心服业务层
 * 
 * @author zane
 *
 */
@Component
public class G2CRegService {

	@Autowired
	private RoomFunction roomFunction;
	@Autowired
	private RoleFunction roleFunction;

	private Map<Integer,ServerInfo> serverMap = new ConcurrentHashMap<Integer, ServerInfo>();

	public void saveServerInfo(CMsg_22001001 msg) {
		for (CServerInfo server : msg.getServersList()) {
			ServerInfo serverInfo = new ServerInfo();
			ObjectUtil.copyProperties(server, serverInfo);
			serverMap.put(serverInfo.getId(), serverInfo);
			LogUtil.info("Id()" + serverInfo.getId() + " Host"
					+ serverInfo.getHost() + " port" + server.getPort());
		}

	}
	
}
