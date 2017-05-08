package com.yaowan.server.game.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yaowan.model.struct.Player;
import com.yaowan.server.game.function.RedBagFunction;
import com.yaowan.server.game.model.data.entity.RedBag;

/**
 * 
 * @author G_T_C
 */
@Service
public class RedBagService {
	
	@Autowired
	private RedBagFunction redBagFunction;


	public void add(RedBag redBag) {
		redBagFunction.add(redBag);
	}

	public void update(RedBag redBag) {
		redBagFunction.update(redBag);
		
	}

	public void reward(Player player) {
		redBagFunction.reward(player);
	}
}
