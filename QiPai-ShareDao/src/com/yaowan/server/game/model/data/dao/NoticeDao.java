package com.yaowan.server.game.model.data.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.entity.Notice;

@Component
public class NoticeDao extends SingleKeyDataDao<Notice,Integer> {
	
	/**
	 * 获取开启的,结束时间在当前时间后面，暂时定为10条
	 * @return
	 */
	public List<Notice> getNoticeList()
	{
		StringBuilder sql = new StringBuilder();
		sql.append("select * from notice where status=1 and type=1 and etime>").append(TimeUtil.time()).append(" limit 10");
		return this.findList(sql.toString());
	}
	
	/**
	 * 按活动开始时间的降序查询领取还没有过期的活动
	 * 
	 * @author G_T_C 
	 * @return
	 */
	public List<Notice> findAllStartTimeDesc() {
		int time = TimeUtil.time();
		String sql = "select * from notice where etime >"+time+" and stime <="+time+" and status=1 and type=2 order by order_by";
		return findList(sql);
	}
	
	/**
	 * 按登录公告开始时间的降序查询领取还没有过期的活动
	 * 
	 * @author G_T_C 
	 * @return
	 */
	public List<Notice> findLoginNotice() {
		int time = TimeUtil.time();
		String sql = "select * from notice where etime >"+time+" and stime <="+time+" and status=1 and type=3 order by order_by";
		return findList(sql);
	}
}
