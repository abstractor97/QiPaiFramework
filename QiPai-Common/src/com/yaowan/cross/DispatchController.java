package com.yaowan.cross;

import java.util.HashMap;
import java.util.Map;

public class DispatchController {
	private static Map<Short, Controller> controllerMap = new HashMap<Short, Controller>();
	
	public static void registe(Controller controller){
		controllerMap.put(controller.getCmd(), controller);
	}

	public static Controller get(short cmd) {
		if(!controllerMap.containsKey(cmd)){
			throw new RuntimeException("Cross  DispatchController  controller not exists!! cmd="+cmd);
		}
		return controllerMap.get(cmd);
	}
}
