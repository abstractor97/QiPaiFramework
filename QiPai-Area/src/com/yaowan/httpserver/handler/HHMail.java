package com.yaowan.httpserver.handler;




import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yaowan.framework.util.JSONObject;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.server.game.function.MailFunction;



/**
 * 
 * http格式:http://ip:port?action=HHClassName.methodName&param1=value1&param2=
 * value2&...
 * 
 */
@Service("HHMail")
public class HHMail {

	@Autowired
	private MailFunction mailFunction;


	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> systemSendMessageToAll(Map<String,String> params)
			throws Exception {
		Map<String, Object> result = new HashMap<>();
		String content = params.get("content");
		String title = params.get("title");
		String cash = params.get("cash");
		String item = params.get("items");
		String ridStr = params.get("rids");
		int expire = StringUtil.getInt(params.get("expire"));
		int is_recommend = StringUtil.getInt(params.get("is_recommend"));
		int is_all = StringUtil.getInt(params.get("send_all"));
		int id = StringUtil.getInt(params.get("id"));
		//String type =  params.get("type");
		if(StringUtil.isStringEmpty(ridStr) && is_all == 1){//没有输入用户
			ridStr ="0";
		}
		String[] rids = ridStr.split(",");
		JSONObject cashJsonObject = JSONObject.decode(cash);
		JSONObject itemJsonObject = JSONObject.decode(item);
		String[] cashjsonString = cashJsonObject.keySet().toArray(new String[cashJsonObject.keySet().size()]);
		String[] itemjsonString = itemJsonObject.keySet().toArray(new String[itemJsonObject.keySet().size()]);
		List<StringBuilder> jsonList = new ArrayList<StringBuilder>();
		StringBuilder jsonString = new StringBuilder();
		jsonString = jsonString.append("[");
		int h = 0;
		int cashid;
		for (int i = 0; i < cashjsonString.length; i++) {
			String key = cashjsonString[i];
			Integer value = cashJsonObject.getInt(key);
			if(value > 0){
				if (h != 0) {
					jsonString = jsonString.append(",");
				}
				if (key.equals("gold")) {
					cashid = 2010001;
				} else if (key.equals("diamond")) {
					cashid = 2020002;
				} else if (key.equals("crystal")) {
					cashid = 2030003;
				} else {
					continue;
				}
				jsonString = jsonString.append("{\"id\":" + cashid + ",\"num\":" + value + "}");
				h = h + 1;
				if(h == 3){
					h = 0;
					jsonString = jsonString.append("]");
					jsonList.add(jsonString);
					jsonString = new StringBuilder();
					jsonString = jsonString.append("[");
				}
			}	
		}
		for (int i = 0; i < itemjsonString.length; i++) {
			if(h != 0){
				jsonString = jsonString.append(",");
			}
			jsonString = jsonString.append("{\"id\":" + itemjsonString[i] + ",\"num\":" + itemJsonObject.getInt(itemjsonString[i]) + "}");
			h = h + 1;
			if(h == 3){
				h = 0;
				jsonString = jsonString.append("]");
				jsonList.add(jsonString);
				jsonString = new StringBuilder();
				jsonString = jsonString.append("[");
			}
		}
		if(jsonList == null || jsonList.isEmpty()){
			jsonString = jsonString.append("]");
			jsonList.add(jsonString);
		}else if( h == 1 || h == 2){
			jsonString = jsonString.append("]");
			jsonList.add(jsonString);
		}
		mailFunction.insertMailBySystem(rids,expire, content, jsonList, title,is_recommend,id);
		
		result.put("result", 1);
		LogUtil.info("录入邮件信息成功");
		LogUtil.info("result信息："+result.get("result"));
		return result;
	}

	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> systemUpdateMail(Map<String,String> params)
			throws Exception {
		Map<String, Object> result = new HashMap<>();
		long id = StringUtil.getInt(params.get("id"));
		long receiveId = StringUtil.getLong(params.get("receiveId"));
		String content = params.get("content");
		String reward = params.get("reward");
		String title = params.get("title");
		mailFunction.updateMailBySystem(id,receiveId,content,reward,title);
		result.put("result", 1);
		return result;
	}
	
	public Map<String,Object> systemDelMessageToAll(Map<String,String> params)
			throws Exception {
		Map<String, Object> result = new HashMap<>();
		int delId = StringUtil.getInt(params.get("id"));
		int is_del = StringUtil.getByte(params.get("is_del"));
		mailFunction.deleteMailBySystem(delId,is_del);
		result.put("result", 1);
		return result;
	}
	
}
