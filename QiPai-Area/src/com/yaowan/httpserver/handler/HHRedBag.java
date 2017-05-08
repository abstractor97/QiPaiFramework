package com.yaowan.httpserver.handler;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.httpserver.handler.entity.ResultInfo;
import com.yaowan.httpserver.handler.entity.StatusCode;
import com.yaowan.server.game.model.data.entity.RedBag;
import com.yaowan.server.game.model.struct.RedBagInfo;
import com.yaowan.server.game.service.RedBagService;

@Controller("HHRedBag")
public class HHRedBag {
	
	@Autowired
	private RedBagService redBagService;
	
	public ResultInfo<String> add(Map<String, String> parms){
		LogUtil.info("paramter=" + StringUtil.mapToString(parms));
		ResultInfo<String> result = new ResultInfo<>();
		try {
			int startTime = Integer.parseInt(parms.get("startTime"));
			int endTime = Integer.parseInt(parms.get("endTime"));
			int dayLimit = Integer.parseInt(parms.get("dayLimit"));
			String info = parms.get("content");
			RedBag redBag = new RedBag();
			redBag.setDayLimit(dayLimit);
			redBag.setEndTime(endTime);
			redBag.setInfo(info);
			redBag.setStartTime(startTime);
			boolean b = checkTime(redBag);
			if(!b){
				result.setResult(StatusCode.CODE_ERROR);
				return result;
			} 
			redBagService.add(redBag);
			result.setResult(StatusCode.CODE_OK);
		} catch (Exception e) {
			LogUtil.error(e);;
		}
		return result;
	}
	
	public ResultInfo<String> edit(Map<String, String> parms){
		LogUtil.info("paramter=" + StringUtil.mapToString(parms));
		ResultInfo<String> result = new ResultInfo<>();
		try {
			long id = Long.parseLong(parms.get("id"));
			int startTime = Integer.parseInt(parms.get("startTime"));
			int endTime = Integer.parseInt(parms.get("endTime"));
			int dayLimit = Integer.parseInt(parms.get("dayLimit"));
			String info = parms.get("content");
			RedBag redBag = new RedBag();
			redBag.setDayLimit(dayLimit);
			redBag.setEndTime(endTime);
			redBag.setInfo(info);
			redBag.setStartTime(startTime);
			redBag.setId(id);
			boolean b = checkTime(redBag);
			if(!b){
				result.setResult(StatusCode.CODE_ERROR);
				return result;
			}
			redBagService.update(redBag);
			result.setResult(StatusCode.CODE_OK);
		} catch (Exception e) {
			LogUtil.error(e);
		}
		return result;
	}
	
	private boolean checkTime(RedBag redBag){
		int startTime = redBag.getStartTime();
		int endTime = redBag.getEndTime();
		if(startTime >= endTime){
			return false;
		}
		List<RedBagInfo> redBagInfos = redBag.getInfoList();
/*		if(redBagInfos != null){
			for(RedBagInfo redBagInfo : redBagInfos){
				int stime = redBagInfo.getStime();
				int etime = redBagInfo.getEtime();
				if(stime >startTime || etime > endTime){
					return false;
				}
			}*/
			Collections.sort(redBagInfos, new Comparator<RedBagInfo>() {

				@Override
				public int compare(RedBagInfo o1, RedBagInfo o2) {
					if(o1.getEtime()> o2.getEtime()){
						return 1;
					}else if(o1.getEtime()< o2.getEtime()){
						return -1;
					}else{
						return 0;
					}
				}
			});
			/*for(int i = 0; i < redBagInfos.size(); i++){
				System.out.println(redBagInfos.get(i).getEtime());
			}
			for(int i = 0; i < redBagInfos.size(); i++){
				for(int j = 1; j < redBagInfos.size(); j++){
						RedBagInfo bRedBagInfo = redBagInfos.get(i);
						int betime = bRedBagInfo.getEtime();
						RedBagInfo aRedBagInfo = redBagInfos.get(j);
						int astime = aRedBagInfo.getStime();
						int aetime = aRedBagInfo.getEtime();
						if(betime < aetime){
							if(astime< betime){
								System.out.println(astime+",,,"+betime);
								return false;
							}
						}
					}*/
		//}
		return true;
	}
}
