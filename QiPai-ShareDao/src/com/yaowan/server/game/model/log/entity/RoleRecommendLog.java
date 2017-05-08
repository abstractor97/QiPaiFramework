package com.yaowan.server.game.model.log.entity;

import org.springframework.beans.factory.annotation.Autowired;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;

@Table(name = "role_recommend_log")
@Index(names = {"role_recommend_id"}, indexs = {"role_recommend_id"})
public class RoleRecommendLog {

	@Id(strategy = Strategy.AUTO)
	@Column(comment = "推荐id")
	private int roleRecommendId;
	
	@Column(comment = "推荐人id")
	private long recommendRid;
	
	@Column(comment = "被推荐人id")
	private long beRecommendRid;
	
	@Column(comment = "推荐时间")
	private int recommendTime;

	public int getRoleRecommendId() {
		return roleRecommendId;
	}

	public void setRoleRecommendId(int roleRecommendId) {
		this.roleRecommendId = roleRecommendId;
	}

	

	public long getRecommendRid() {
		return recommendRid;
	}

	public void setRecommendRid(long recommendRid) {
		this.recommendRid = recommendRid;
	}

	public long getBeRecommendRid() {
		return beRecommendRid;
	}

	public void setBeRecommendRid(long beRecommendRid) {
		this.beRecommendRid = beRecommendRid;
	}

	public int getRecommendTime() {
		return recommendTime;
	}

	public void setRecommendTime(int recommendTime) {
		this.recommendTime = recommendTime;
	}
	
}
