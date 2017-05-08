package com.yaowan.httpserver.handler;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.MathUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.httpserver.handler.entity.ResultInfo;
import com.yaowan.httpserver.handler.entity.StatusCode;
import com.yaowan.server.game.model.data.entity.Npc;
import com.yaowan.server.game.service.NpcService;

/**
 * Ai后端controller
 * @author G_T_C
 */
@Controller("HHNpc")
public class HHNpc {

	@Autowired
	private NpcService npcService;
	
	/**
	 * ai的添加
	 * @author G_T_C
	 * @param map
	 * @return
	 */
	public ResultInfo<String> add(Map<String, Object> map){
		ResultInfo<String> result = new ResultInfo<String>();
		try {
			LogUtil.info("paramter=" + StringUtil.mapToString(map));
			/*String  idString = (String) map.get("id");
			String[] ids = idString.split(","); */
			String  nickString = (String) map.get("nick");
			String[] nicks = nickString.split(","); 
			/*if(ids.length != nicks.length ){
				result.setData(StatusCode.CODE_ERROR);
				return result;
			}*/
	//		String goldString = (String) map.get("gold");
			String diamondString = (String) map.get("diamond");
			String lotteryString = (String) map.get("lottery");
			String expString = (String) map.get("exp");
		//	byte  head = Byte.parseByte(map.get("head")+"");
			String serverId = (String) map.get("server_id");
			int length = nicks.length;
			LogUtil.info("添加"+length+"个AI");
			for(int i = 0; i<length; i++){
			//	int gold = 0;
				int diamond = 0;
				int lottery = 0;
				int exp = 0;
				if(length == 1){//添加一个
				//	gold = Integer.parseInt(goldString);
					diamond = Integer.parseInt(diamondString);
					lottery = Integer.parseInt(lotteryString);
					exp = Integer.parseInt(expString);
				}else{//批量添加
				//	String [] golds = goldString.split(",");
				//	gold = MathUtil.randomNumber(Integer.parseInt(golds[0]),Integer.parseInt(golds[1]));
					String [] diamonds = diamondString.split(",");
					diamond = MathUtil.randomNumber(Integer.parseInt(diamonds[0]),Integer.parseInt(diamonds[1]));
					String [] lotterys = lotteryString.split(",");
					lottery = MathUtil.randomNumber(Integer.parseInt(lotterys[0]),Integer.parseInt(lotterys[1]));
					String [] exps = expString.split(",");
					exp = MathUtil.randomNumber(Integer.parseInt(exps[0]),Integer.parseInt(exps[1]));
				}
				npcService.saveNpc( nicks[i],diamond,lottery,exp,serverId);
			}
			result.setResult(StatusCode.CODE_OK);
		} catch (Exception e) {
			LogUtil.error(e);
		}
		return result;
	}
	
	/**
	 * ai编辑
	 * @author G_T_C
	 * @param map
	 * @return
	 */
	public ResultInfo<String> edit(Map<String, Object> map){
		ResultInfo<String> result = new ResultInfo<String>();
		try {
			LogUtil.info("paramter=" + StringUtil.mapToString(map));
			String  idString = (String) map.get("id");
			String[] ids = idString.split(","); 
			String nickString =  (String) map.get("nick");
			boolean updateNick = false;
			String[] nicks = null;
			if(nickString != null && !"".equals(nickString)){
				 nicks = nickString.split(","); 
				if(ids.length != nicks.length ){
					result.setData(StatusCode.CODE_ERROR);
					result.setErrmsg("昵称数量和id的数量不一致");
					return result;
				}
				updateNick = true;
			}
			
			String diamondString = (String) map.get("diamond");
			String lotteryString = (String) map.get("lottery");
			String expString = (String) map.get("exp");
			String serverId = (String) map.get("server_id");
			int length = ids.length;
			LogUtil.info("修改"+length+"个AI");
			for(int i = 0; i<length; i++){
			//	int gold = 0;
				int diamond = 0;
				int lottery = 0;
				int exp = 0;
				if(length == 1){//添加一个
					if(diamondString != null && !"".equals(diamondString)){						
						diamond = Integer.parseInt(diamondString);
					}
					if(lotteryString != null && !"".equals(diamondString)){						
						lottery = Integer.parseInt(lotteryString);
					}
					if(expString != null && !"".equals(diamondString)){						
						exp = Integer.parseInt(expString);
					}
				}else{//批量添加
					if(diamondString != null && !"".equals(diamondString)){						
						String [] diamonds = diamondString.split(",");
						diamond = MathUtil.randomNumber(Integer.parseInt(diamonds[0]),Integer.parseInt(diamonds[1]));
					}
					if(lotteryString != null && !"".equals(lotteryString)){						
						String [] lotterys = lotteryString.split(",");
						lottery = MathUtil.randomNumber(Integer.parseInt(lotterys[0]),Integer.parseInt(lotterys[1]));
					}
					if(expString != null && !"".equals(expString)){						
						String [] exps = expString.split(",");
						exp = MathUtil.randomNumber(Integer.parseInt(exps[0]),Integer.parseInt(exps[1]));
					}
				}
				Npc npc = new Npc();
				npc.setRid(Long.parseLong(ids[i]));
				if(updateNick){
					npc.setNick(nicks[i]);
					npc.markToUpdate("nick");
				}
				String  headString = (String) map.get("head");
				if(headString != null && !"".equals(headString)){
					npc.setHead(Byte.parseByte(headString+""));
					npc.markToUpdate("head");
				}
				if(diamondString != null && !"".equals(diamondString)){	
					npc.setDiamond(diamond);
					npc.markToUpdate("diamond");
				}
				if(lotteryString != null && !"".equals(lotteryString)){	
					npc.setCrystal(lottery);
					npc.markToUpdate("crystal");
				}
				if(expString != null && !"".equals(expString)){		
					npc.setExp(exp);
					npc.markToUpdate("exp");
				}
				if(serverId != null && !"".equals(serverId)){		
					npc.setServerId(Integer.parseInt(serverId));
					npc.markToUpdate("serverId");
				}
				npcService.modify(npc);
			}
			result.setResult(StatusCode.CODE_OK);
		} catch (Exception e) {
			LogUtil.error(e);
		}
		return result;
	}
	
	/**
	 * AI的调度
	 * @author G_T_C
	 * @param map
	 * @return
	 */
	public ResultInfo<String> dispatch(Map<String, Object> map){
		ResultInfo<String> result = new ResultInfo<String>();
		LogUtil.info("paramter=" + StringUtil.mapToString(map));
		try {

			String  idString = (String) map.get("id");
			String[] ids = idString.split(","); 
			if(ids.length<=0){
				result.setErrmsg(StatusCode.CODE_ERROR);
				result.setErrmsg("id为空");
				return result;
			}
			npcService.dispatch(ids, map);
			result.setResult(StatusCode.CODE_OK);
		} catch (Exception e) {
			LogUtil.error(e);
		}
		return result;
	}
	
	
	/**
	 * AI的开关
	 * @author G_T_C
	 * @param map
	 * @return
	 */
	public ResultInfo<String> change(Map<String, Object> map){
		ResultInfo<String> result = new ResultInfo<String>();
		LogUtil.info("paramter=" + StringUtil.mapToString(map));
		try {
			String  idString = (String) map.get("id");
			String[] ids = idString.split(","); 
			if(ids.length<=0){
				result.setErrmsg(StatusCode.CODE_ERROR);
				result.setErrmsg("id为空");
				return result;
			}
			int  isOpen = Integer.parseInt(map.get("isOpen")+"");
			String logId = (String) map.get("logId");
			npcService.onOrOff(ids, isOpen, logId);
			result.setResult(StatusCode.CODE_OK);
		} catch (Exception e) {
			LogUtil.error(e);
		}
		return result;
	}
	
	
}
