package com.yaowan.server.center.action;

import java.util.HashMap;
import java.util.Map;

public class DispatchAction {
	
	private static Map<Integer, CenterAction> ActionMap = new HashMap<Integer, CenterAction>();
	
	public static void registe(CenterAction action){
		if(!ActionMap.containsKey(action.getCmd())){
			ActionMap.put(action.getCmd(), action);
		}
	}
	
	public static CenterAction getAction(int cmd){
		if(!ActionMap.containsKey(cmd)){
			throw new RuntimeException("CenterServer Action not exists : cmd:"+cmd);
		}
		return ActionMap.get(cmd);
	}
}
