package com.yaowan.httpserver.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.yaowan.model.struct.Player;
import com.yaowan.server.game.function.RoleFunction;

//金币增加，测试用
@Controller("HHGoldAdd")
public class HHGoldAdd {

	@Autowired
	private RoleFunction roleFunction;
	
	public Map<String, Object> add(Map<String, String> params){
		
		Map<String, Object> hm = new HashMap<String, Object>();
		
		Collection<Player> players = roleFunction.getPlayerMap().values();
		for (Player player : players) {
			if(player.getRole()!=null && player.getRole().getGold()<900000){
				player.getRole().markToUpdate("gold");
				player.getRole().setGold(Integer.MAX_VALUE);
				roleFunction.updatePropertys(player.getRole());
			}
		}
		
		hm.put("result:", "SUCCESS");
		return hm;
	}
}
