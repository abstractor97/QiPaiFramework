package com.yaowan.server.game.model.data.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.server.game.model.data.entity.Mail;

@Component
public class MailDao extends SingleKeyDataDao<Mail,Long> {
	

	/**
	 * 根据用户rid来获取相关的邮件信息
	 * @param rid
	 * @return
	 */
	public List<Mail> getMailListByRid(long rid)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("select * from mail where receive_id = " + rid + " and recycle = " + 0);
		return this.findList(sql.toString());
	}
	
	public boolean isExist(long id,long receiveId)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("select * from mail where id = " + id+" and receive_Id ="+receiveId);
		Mail mail = this.find(sql.toString());
		if(mail != null)
		{
			return true;
		}else
		{
			return false;
		}
	}
	
	
	public List<Mail> getMailListByDelId(long delId){
		StringBuilder sql = new StringBuilder();
		sql.append("select * from mail where del_id = " + delId);
		return this.findList(sql.toString());
	}
	
	
	
}
