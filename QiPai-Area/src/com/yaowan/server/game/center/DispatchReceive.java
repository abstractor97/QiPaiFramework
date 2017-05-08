package com.yaowan.server.game.center;

import java.util.HashMap;
import java.util.Map;

public class DispatchReceive {
	
	private static Map<Integer, Receive> receiveMap = new HashMap<Integer, Receive>();
	
	public static void registe(Receive receive){
		if(!receiveMap.containsKey(receive.getCmd())){
			receiveMap.put(receive.getCmd(), receive);
		}
	}
	
	public static Receive getReceive(int cmd){
		if(!receiveMap.containsKey(cmd)){
			throw new RuntimeException("CenterServer Action not exists : cmd:"+cmd);
		}
		return receiveMap.get(cmd);
	}
}
