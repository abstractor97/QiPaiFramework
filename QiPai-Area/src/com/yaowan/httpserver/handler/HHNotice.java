package com.yaowan.httpserver.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.yaowan.constant.NoticeType;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.httpserver.handler.entity.ResultInfo;
import com.yaowan.httpserver.handler.entity.StatusCode;
import com.yaowan.server.game.function.NoticeFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.model.data.entity.Notice;
import com.yaowan.server.game.service.GameUpdateNoticeService;

/**
 * 格式
 * http格式:http://ip:port?action=HHClassName.methodName&param1=value1&param2=
 * value2&...
 * 该路径
 * http格式:http://ip:port?action=Broadcast.broadcast&message=value 
 */
@Component
@Service("HHNotice")
public class HHNotice {
	@Autowired
	RoleFunction roleFunction;
	
	@Autowired
	NoticeFunction noticeFunction;
	
	@Autowired
	private GameUpdateNoticeService gameUpdateNoticeService;
	
	/**
	 * http调用全局广播
	 * @param params
	 */
	@SuppressWarnings("finally")
	public Object systemSendNOticeToAll(Map<String, String> params)
	{		
		
		/*String id=params.get("id");
		String stime=params.get("stime");
		String etime=params.get("etime");
		String content=params.get("content");
		String diff_time=params.get("diff_time");
		String status=params.get("status");
		String mark=params.get("mark");*/
		
		HashMap<String, Integer> hm=new HashMap<String, Integer>();
		try {
			LogUtil.info("paramter=" + StringUtil.mapToString(params));
			Notice notice=new Notice();
			int id=Integer.parseInt(params.get("id"));
			notice.setId(id);
			notice.setStime(Integer.parseInt(params.get("stime")));
			notice.setEtime(Integer.parseInt(params.get("etime")));
			notice.setContent(params.get("content"));
			notice.setTime(TimeUtil.time());
			notice.setDiff_time(Integer.parseInt(params.get("diff_time")));
			notice.setStatus(Byte.parseByte(params.get("status")));
			//notice.setMark(params.get("mark"));		
			notice.setImg(params.get("img"));
			notice.setContentUrl(params.get("content_url"));
			notice.setType(Byte.parseByte(params.get("type")));
			notice.setTitle(params.get("title"));
			//公告类型判断，跑马灯则设置优先级字段值，其他类型的公告的优先级设为0
			if(Integer.parseInt(params.get("type")) == NoticeType.MARQUEE_NOTICE_TYPE){
			notice.setLevel(Integer.parseInt(params.get("level")));
			}else{
			notice.setLevel(0);
			}
			String orderBy = params.get("no");
			if(orderBy != null && !orderBy.equals("")){
				notice.setOrderBy(Integer.parseInt(orderBy));				
			}
			String loginNoticeType = params.get("pop_rate");
			if(orderBy != null && !orderBy.equals("")){
				notice.setLoginNoticeType(Integer.parseInt(loginNoticeType));			
			}
			
			//插入
			if(noticeFunction.getNotice(id)==null)
			{
				LogUtil.info("add "+params.get("content"));
				noticeFunction.addNotice(notice);
			}else//修改
			{	LogUtil.info("update "+params.get("content"));
				noticeFunction.updateNotice(notice);
			}			
			hm.put("result", 1);			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			hm.put("result", 0);
			e.printStackTrace();
		}finally{
			return hm;
		}			
	}
	
	/**
	 * 游戏更新公告
	 * @author G_T_C
	 */
	public ResultInfo<String> gameUpdateNotice(Map<String, String> params){
		LogUtil.info("paramter=" + StringUtil.mapToString(params));
		ResultInfo<String> result = new  ResultInfo<>();
		try {
			int gameType = Integer.parseInt(params.get("gameType"));
			String content = params.get("content");
			gameUpdateNoticeService.addOrUpdate(gameType, content);
			result.setResult(StatusCode.CODE_OK);
		} catch (Exception e) {
			LogUtil.error(e);
		}
		return result;
	}
}
