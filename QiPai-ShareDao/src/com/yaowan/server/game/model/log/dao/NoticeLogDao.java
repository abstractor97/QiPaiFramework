package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Repository;

import com.yaowan.framework.database.LogDao;
import com.yaowan.server.game.model.log.entity.NoticeLog;

@Repository
public class NoticeLogDao extends LogDao<NoticeLog>{
	
	public void updateclickNum(int aid) {
		NoticeLog log = find("select * from notice_log where notice_id ="+aid);
		if(log == null){
			log = new NoticeLog();
			log.setClickNum(1);
			log.setNoticeId(aid);
			insert(log);
		}else{
			String sql = "update notice_log set click_num = click_num+1 where notice_id ="+aid;
			executeSql(sql);
		}
		
	}
}
