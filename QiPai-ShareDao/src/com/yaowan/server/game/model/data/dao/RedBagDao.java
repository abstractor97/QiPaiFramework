package com.yaowan.server.game.model.data.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.entity.RedBag;

/**
 * 红包dao
 * @author G_T_C
 */
@Repository
public class RedBagDao extends SingleKeyDataDao<RedBag, Long> {

	public List<RedBag> selectRedBag() {
		int time = TimeUtil.time();
		StringBuilder sql = new StringBuilder();
		sql.append("select * from red_bag where end_time >").append(time);
		return findList(sql.toString());
	}

}
