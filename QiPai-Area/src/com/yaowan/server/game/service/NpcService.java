package com.yaowan.server.game.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yaowan.server.game.function.NPCFunction;
import com.yaowan.server.game.model.data.entity.Npc;

@Service
public class NpcService {
	
	@Autowired
	private NPCFunction npcFunction;

	public void saveNpc( String nick, int diamond, int lottery,
			int exp,String serverId) throws Exception {
		npcFunction.insert(nick,diamond,lottery,exp,serverId);
	}

	public void modify(Npc npc) throws Exception {
		npcFunction.updateNpc(npc);
	}
	
	public void dispatch(String []ids, Map<String, Object> map) throws Exception{
		npcFunction.dispatch(ids, map);
	}

	public void onOrOff(String[] ids, int isOpen, String logId) throws Exception {
		npcFunction.onOrOff(isOpen, ids, logId);
	}

}
