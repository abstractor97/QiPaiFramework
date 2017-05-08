package com.yaowan.httpserver.handler;

import java.lang.instrument.UnmodifiableClassException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.cndw.dj.inst.AgentServer;
import com.cndw.dj.inst.ClazzDefinition;
import com.cndw.dj.inst.ClazzReloader;
import com.yaowan.core.base.GlobalConfig;
import com.yaowan.framework.util.LogUtil;

/**
 * 用于在线修复bug，来替换class文件
 * 使用前先上传Class文件到csv/clazzes目录下
 * http://ip:port?action=ClazzLoader.load&clazzes=clazzName1,clazzName2,clazzName3 
 * @author YW0941
 *
 */
@Service("HHClazzLoader")
public class HHClazzLoader {
	
	private static final String Key = "clazzes";
	/**
	 *  Key =  clazzes
	 * @param params
	 */
	public Object load(Map<String, String> params){
		HashMap<String, Integer> hm=new HashMap<String, Integer>();
		//设置Class文件所在目录
		if(ClazzDefinition.BinDir == null){
			ClazzDefinition.BinDir = GlobalConfig.csvConfigDir.endsWith("/") ? GlobalConfig.csvConfigDir+"clazzes" : GlobalConfig.csvConfigDir+"/clazzes";
		}
		
		//代理没启动，则启动代理
		String agentJar = GlobalConfig.csvConfigDir.endsWith("/") ? GlobalConfig.csvConfigDir+"agentjar/AppAgent.jar" : GlobalConfig.csvConfigDir+"/agentjar/AppAgent.jar";
		AgentServer.startAgent(agentJar);
		
		if(params!=null && params.containsKey(Key)){
			String[] splits = params.get(Key).split(",");
			
			ClazzDefinition[] clazzDefinitions = new ClazzDefinition[splits.length];
			int i = 0;
			for (String clazzName : splits) {
				clazzDefinitions[i++] = new ClazzDefinition(clazzName);
			}
			try {
				ClazzReloader.getInstance().reloadClass(clazzDefinitions);
			} catch (ClassNotFoundException e) {
				LogUtil.error(e);
				hm.put("result", 0);
			} catch (UnmodifiableClassException e) {
				LogUtil.error(e);
				hm.put("result", 0);
			}
		}
		hm.put("result", 1);
		return hm;
	}
	
	
}
