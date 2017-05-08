package com.yaowan.server.game.service;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.csv.cache.ItemCache;
import com.yaowan.framework.util.JSONObject;
import com.yaowan.model.struct.ItemGet;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GMail.GMsg_12015002;
import com.yaowan.protobuf.game.GMail.GMsg_12015003;
import com.yaowan.protobuf.game.GMail.GMsg_12015004;
import com.yaowan.protobuf.game.GMail.GReward;
import com.yaowan.protobuf.game.GMail.MMail;
import com.yaowan.server.game.function.MailFunction;
import com.yaowan.server.game.model.data.dao.MailDao;
import com.yaowan.server.game.model.data.entity.Mail;

@Component
public class MailService {

	@Autowired
	private MailDao mailDao;

	@Autowired
	private MailFunction mailFunction;

	@Autowired
	private ItemCache itemCache;


	/**
	 * 用户读取邮件
	 * @param player
	 * @param id
	 */
	public void readMail(Player player, long id) {
		// TODO Auto-generated method stub
		GMsg_12015002.Builder builder = GMsg_12015002.newBuilder();
		mailFunction.updateMailByPlayerRead(player.getRole(),id);
		builder.setResult(1);
		player.write(builder.build());
	}
	
	/**
	 * 用户领取邮件奖励
	 * @param player
	 * @param id
	 */
	public void receiveMail(Player player,long id){
		GMsg_12015003.Builder builder = GMsg_12015003.newBuilder();
		int result =mailFunction.updateMailByPlayerReceive(player.getRole(),id);
		builder.setResult(result);
		player.write(builder.build());
	}

	/**
	 * 查看消息
	 * @param player
	 */
	public void checkMail(Player player) {
		// TODO Auto-generated method stub
		GMsg_12015004.Builder builder = GMsg_12015004.newBuilder();
		Map<Long, Mail> map = mailFunction.selectMailListByRid(player.getRole());
		for (Map.Entry<Long, Mail> entry : map.entrySet()) {
			Mail mail = entry.getValue();
			MMail.Builder mmailBuilder = MMail.newBuilder();
			
			mmailBuilder.setId(mail.getId());
			mmailBuilder.setContent(mail.getContent());
			mmailBuilder.setExpire(mail.getExpire());
			mmailBuilder.setReaded(mail.getReaded());
			mmailBuilder.setReceive(mail.getReceive());
			mmailBuilder.setReceiveId(mail.getReceiveId());
			mmailBuilder.setSendId(mail.getSendId());
			mmailBuilder.setSendTime(mail.getSendTime());
			mmailBuilder.setTitle(mail.getTitle());
			mmailBuilder.setRecommend(mail.getRecommend());
			List<ItemGet> mailItemList= JSONObject.decodeJsonArray(mail.getReward(), ItemGet[].class);
			if(mailItemList!=null){
				for (ItemGet itemGet : mailItemList) {
					if(itemGet.getId()!=0){
						GReward.Builder gRewardBuilder = GReward.newBuilder();
						gRewardBuilder.setItemId(itemGet.getId());
						gRewardBuilder.setNum(itemGet.getNum());
						mmailBuilder.addRewardList(gRewardBuilder);
					}
				}
			}
			
			builder.addMailList(mmailBuilder);
			
		}
		player.write(builder.build());
		
	}

	
	
}
