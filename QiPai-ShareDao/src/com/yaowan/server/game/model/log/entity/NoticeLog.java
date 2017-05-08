package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Table;

@Table(name = "notice_log")
public class NoticeLog {
	@Column(comment = "公告id")
	private int noticeId;

	@Column(comment = "活动点击量")
	private int clickNum;

	public int getNoticeId() {
		return noticeId;
	}

	public int getClickNum() {
		return clickNum;
	}

	public void setNoticeId(int noticeId) {
		this.noticeId = noticeId;
	}

	public void setClickNum(int clickNum) {
		this.clickNum = clickNum;
	}
	
}
