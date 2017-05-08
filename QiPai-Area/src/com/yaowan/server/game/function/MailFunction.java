package com.yaowan.server.game.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.MoneyEvent;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.framework.util.JSONObject;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.ItemGet;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GMail.GMsg_12015004;
import com.yaowan.protobuf.game.GMail.GMsg_12015006;
import com.yaowan.protobuf.game.GMail.GReward;
import com.yaowan.protobuf.game.GMail.MMail;
import com.yaowan.server.game.model.data.dao.MailDao;
import com.yaowan.server.game.model.data.dao.RoleDao;
import com.yaowan.server.game.model.data.entity.Mail;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.service.ItemService;

@Component
public class MailFunction extends FunctionAdapter {

	@Autowired
	private MailDao mailDao;
	
	@Autowired
	private RoleDao roleDao;
	
	@Autowired
	private ItemService itemService;

	@Autowired
	private RoleFunction roleFunction;
	
	@Autowired
	private ItemFunction itemFunction;
	/**
	 * 缓存 Long 用户id Map<Long,Mail> 邮件id 邮件id对应的Mail
	 */
	private final ConcurrentHashMap<Long, Map<Long, Mail>> mapCache = new ConcurrentHashMap<>();

	// 用户登录内存加载邮件
	@Override
	public void handleOnRoleLogin(Role role) {
		// TODO Auto-generated method stub
		List<Mail> mailList = mailDao.getMailListByRid(role
				.getRid());

		Map<Long, Mail> map = new HashMap<Long, Mail>();
		for (Mail mail : mailList) {
			map.put(mail.getId(), mail);
		}
		mapCache.put(role.getRid(), map);
		LogUtil.info("Load Mail size " + mapCache.get(role.getRid()).size());
	}
	
	/**
	 * 用户获取邮件 1.内存 2.数据库
	 * 
	 * @param rid
	 * @return
	 */
	public Map<Long, Mail> getMailListByRid(long rid) {
		Map<Long, Mail> map = mapCache.get(rid);
		if (map != null)// 有缓存
		{
			return map;
		} else// 无缓存
		{
			map = new HashMap<Long, Mail>();
			List<Mail> MailList = mailDao.getMailListByRid(rid);
			for (Mail mail : MailList) {
				// 添加进缓存
				map.put(mail.getId(), mail);
			}
			mapCache.put(rid, map);
			return map;
		}
	}
	
	/**
	 * 获取系统群发信息
	 * @return
	 */
	public Map<Long, Mail> getMailListBySystem(){
		Map<Long, Mail> map = mapCache.get(0);
		if (map != null)// 有缓存
		{
			return map;
		} else// 无缓存
		{
			map = new HashMap<Long, Mail>();
			List<Mail> MailList = mailDao.getMailListByRid(0);
			for (Mail mail : MailList) {
				// 添加进缓存
				map.put(mail.getId(), mail);
			}
			mapCache.put((long)0, map);
			return map;
		}
	}
	
	public Mail getMailByIds(long rid,long id){
		Map<Long, Mail> map = getMailListByRid(rid);
		return map.get(id);
	}


	/**
	 * 系统发送消息
	 * @param receiveId
	 * @param sendTime
	 * @param expire
	 * @param content
	 * @param reward
	 */
	public void insertMailBySystem(String[] receiveIds, int expire,
			String content, List<StringBuilder> rewardlist,String title,int is_recommend,int id) {
		// TODO Auto-generated method stub
		
		List<Mail> mailList = new ArrayList<Mail>();
		for (int i = 0; i < rewardlist.size(); i++) {
			for (int j = 0; j < receiveIds.length; j++) {
				Mail mail = new Mail();
				mail.setSendId(0);
				mail.setReceiveId(Long.parseLong(receiveIds[j]));
				mail.setExpire(expire);
				mail.setContent(content);
				mail.setReward(rewardlist.get(i).toString());
				mail.setReaded(0);
				mail.setTitle(title);
				mail.setDelId(id);
				mail.setRecommend(is_recommend);
				mail.setSendTime(TimeUtil.time());
				if(!StringUtil.isStringEmpty(rewardlist.get(i).toString()) && !rewardlist.get(i).toString().equals("[]")){
					mail.setReceive(1);
				}else{
					mail.setReceive(0);
				}
				mailList.add(mail);
			}
		}
		mailDao.insertAll(mailList);
		for (int i = 0; i < mailList.size(); i++) {
				Map<Long, Mail> map = getMailListByRid(mailList.get(i).getReceiveId()); 
				map.put(mailList.get(i).getId(), mailList.get(i));
				GMsg_12015004.Builder builder = GMsg_12015004.newBuilder();
				MMail.Builder builder1 = MMail.newBuilder();
				Mail mail = mailList.get(i);
				builder1.setContent(mail.getContent());
				builder1.setExpire(mail.getExpire());
				builder1.setId(mail.getId());
				builder1.setReaded(mail.getReaded());
				builder1.setReceive(mail.getReceive());
				builder1.setReceiveId(mail.getReceiveId());
				builder1.setSendId(mail.getSendId());
				builder1.setSendTime(mail.getSendTime());
				builder1.setTitle(mail.getTitle());
				builder1.setRecommend(mail.getRecommend());
				List<ItemGet> mailItemList=JSONObject.decodeJsonArray(mailList.get(i).getReward(), ItemGet[].class);	
				for (ItemGet itemGet : mailItemList) {
					GReward.Builder gRewardBuilder = GReward.newBuilder();
					gRewardBuilder.setItemId(itemGet.getId());
					gRewardBuilder.setNum(itemGet.getNum());
					builder1.addRewardList(gRewardBuilder);
				}
				builder.addMailList(builder1);
				if(mailList.get(i).getReceiveId() != 0){
					Player player = roleFunction.getPlayer(mailList.get(i).getReceiveId());
					if(player != null){
						player.write(builder.build());
					}
					
				}else{
					roleFunction.sendMessageToAll(builder.build());
				}
			
			
		}
	}

	/**
	 * 读取邮件
	 * @param rid
	 * @param id
	 */
	public void updateMailByPlayerRead(Role role, long id) {
		// TODO Auto-generated method stub
		Map<Long, Mail> map = getMailListByRid(role.getRid());
		Mail mail = map.get(id);
		if(mail == null){
			map = getMailListBySystem();
			mail = map.get(id);
		}
		if(mail.getReceiveId() != 0){//不是群发邮件
			if(mail.getReaded() == 0){
				mail.setReaded(1);
				mailDao.update(mail);
			}
			
		}else{//群发邮件
			//修改用户表
			if(!StringUtil.isStringEmpty(role.getMailRead())){
				Map<Long, Long> stringMap = StringUtil.stringToMap(role.getMailRead(), Long.class, Long.class);
				if(stringMap.get(id) == null){//已读
					stringMap.put(id, id);
					String str = StringUtil.mapToString(stringMap);
					role.setMailRead(str);
					roleDao.update(role);
				}
			}else{
				Map<Long, Long> stringMap = new HashMap<Long, Long>();
				stringMap.put(id, id);
				String str = StringUtil.mapToString(stringMap);
				role.setMailRead(str);
				roleDao.update(role);
			}
			
		}
	}
	
	/**
	 * 领取邮件奖励
	 * @param rid
	 * @param id
	 * return 1为领取，0领取不成功，-1异常
	 */
	public int updateMailByPlayerReceive(Role role,long id) {
		// TODO Auto-generated method stub
		Mail mail = mailDao.findByKey(id);
		boolean isToreceive = false;//是否可以领取
		boolean fromAll = false;//邮件是否来自群发
		boolean isRead = false;//邮件是否已读
		Map<Long, Long> receiveStringMap = new HashMap<Long, Long>();
		Map<Long, Long> readStringMap = new HashMap<Long, Long>();
		
		if(mail != null && mail.getReceiveId() == role.getRid()){//不是群发邮件
			if(mail.getReceive() == 1){//邮件是可领取的
				isToreceive = true;
			}
		}else if(mail != null){//群发邮件
			Map<Long, Mail> map = getMailListBySystem();
			mail = map.get(id);
			readStringMap = StringUtil.stringToMap(role.getMailRead(), Long.class, Long.class);
			String str2 = StringUtil.mapToString(readStringMap);
			if(readStringMap.get(id) == null){
				readStringMap.put(id, id);
				role.setMailRead(str2);
				isRead = false;
			}
			if(mail.getReceive() == 1){//邮件是可领取的
				if(!StringUtil.isStringEmpty(role.getMailReceive())){
					receiveStringMap = StringUtil.stringToMap(role.getMailReceive(), Long.class, Long.class);
					if(receiveStringMap.get(id) == null){//未领取
						isToreceive = true;
						fromAll = true;
					}
				}else{
					isToreceive = true;
					fromAll = true;
				}
				
			}
		}else{
			return -1;
		}
		if(isToreceive){
			if(itemFunction.getItems(role, mail.getReward(),MoneyEvent.GM_MAIL)){
				if(fromAll){
					receiveStringMap.put(id, id);
					String str = StringUtil.mapToString(receiveStringMap);
					role.setMailReceive(str);
					roleDao.update(role);
				}else{
					Map<Long, Mail> map = getMailListByRid(role.getRid());
					mail.setReceive(0);
					mail.setReaded(1);
					map.put(role.getRid(), mail);
					mailDao.update(mail);
				}
			}else{
				return 2;
			}
		}else{
			if(!isRead && fromAll){
				roleDao.update(role);
			}
		}
		return 1;
	}

	/**
	 * 系统更新邮件信息
	 * @param id
	 * @param receiveId
	 * @param content
	 * @param reward
	 * @param title
	 */
	public void updateMailBySystem(long id, long receiveId, String content,
			String reward,String title) {
		// TODO Auto-generated method stub
		Map<Long, Mail> map = getMailListByRid(receiveId);
		Mail mail = map.get(id);
		mail.setContent(content);
		mail.setReward(reward);
		mail.setTitle(title);
		mailDao.update(mail);
	}

	/**
	 * 系统删除邮件信息
	 * @param id
	 * @param receiveId
	 */
	public void deleteMailBySystem(int delId, int isDel) {
		// TODO Auto-generated method stub
		List<Mail> maillist = mailDao.getMailListByDelId(delId);
		if(maillist.size() != 0){
			if(isDel == 1){
				//放入回收站
				for(Mail mail : maillist){
					mail.setRecycle(1);
					Map<Long, Mail> map = getMailListByRid(mail.getReceiveId());
					map.remove(mail.getId());
					GMsg_12015006.Builder builder = GMsg_12015006.newBuilder();
					builder.setId(mail.getId());
					mailDao.update(mail);
					if(mail.getReceiveId() == 0){
						roleFunction.sendMessageToAll(builder.build());
					}else{
						roleFunction.sendMessageToPlayer(mail.getReceiveId(),builder.build());
					}
					
				}
			}else if (isDel == 2){
				//彻底删除
				for(Mail mail : maillist){
					mailDao.delete(mail);
					Map<Long, Mail> map = getMailListByRid(mail.getReceiveId());
					map.remove(mail.getId());
				}
			}
		}
		
	}

	/**
	 * 查看全部邮件信息
	 * @param role
	 * @return
	 */
	public Map<Long, Mail> selectMailListByRid(Role role) {
		// TODO Auto-generated method stub
		Map<Long, Mail> map = getMailListByRid(role.getRid());
		Map<Long, Mail> systemMap = getMailListBySystem();
		Map<Long, Mail> returnMap = new HashMap<Long, Mail>();
		Map<Long, Long> receiveStringMap = new HashMap<Long, Long>();
		Map<Long, Long> readStringMap = new HashMap<Long, Long>();
		for(Entry<Long, Mail> entry : map.entrySet()){
			Mail mail = entry.getValue();

			if(mail.getSendTime() <= TimeUtil.time()){//邮件的发送时间是不是大于当前时间
				if(mail.getExpire() < TimeUtil.time() && mail.getExpire() != -1){//过期
				//删除邮件
					LogUtil.info("删除个人信息");
					mail.setRecycle(1);
					mailDao.update(mail);
					map.entrySet().remove(entry.getKey());
				}else{
					returnMap.put(entry.getKey(), mail);
				}
			}
		}
		if(!StringUtil.isStringEmpty(role.getMailReceive())){
			receiveStringMap = StringUtil.stringToMap(role.getMailReceive(), Long.class, Long.class);
		}
		if(!StringUtil.isStringEmpty(role.getMailRead())){
			readStringMap = StringUtil.stringToMap(role.getMailRead(), Long.class, Long.class);
		}
		for(Entry<Long, Mail> entry : systemMap.entrySet()){
			Mail mail = entry.getValue();
			if(mail.getSendTime() <= TimeUtil.time()){//邮件的发送时间是不是大于当前时间
				if(mail.getExpire() < TimeUtil.time() && mail.getExpire() != -1){//过期
				//删除邮件
					LogUtil.info("删除系统信息");
					mail.setRecycle(1);
					mailDao.update(mail);
					map.entrySet().remove(entry.getKey());
				}else{
					if(mail.getReceive() == 1){//可领取
						if(receiveStringMap.get(mail.getId()) != null){//已经领取过
							mail.setReceive(0);
						}
					}
					if(readStringMap.get(mail.getId()) == null){
						mail.setReaded(0);
					}else{
						mail.setReaded(1);
					}
					returnMap.put(entry.getKey(), mail);
				}
			}
		}
		return returnMap;
	}
 
	/**
	 * 内部增加邮件信息
	 * @param receiveId 接收者rid
	 * @param expire 过期时间 = 当前时间 + 邮件保存的时间
	 * @param content 邮件内容
	 * @param reward 奖励信息，json格式 {"物品id"：数量}，例如 {"123":12}，没有则为""
	 * @param title 标题
	 * @param recommend 是否推荐 （0否1是）
	 * @return
	 */
	public boolean insertMailByInside(long receiveId,int expire,String content,String reward,String title,int recommend){
		JSONObject itemJsonObject = JSONObject.decode(reward);
		String[] itemjsonString = itemJsonObject.keySet().toArray(new String[itemJsonObject.keySet().size()]);
		List<String> list = new ArrayList<String>();
		LogUtil.info(reward);
		if(!StringUtil.isStringEmpty(reward)){	
			int h = 0;
			StringBuilder reward2 = new StringBuilder("[");
			for (int i = 0; i < itemjsonString.length; i++) {
				if(h != 0){
					reward2 = reward2.append(",");
				}
				reward2 = reward2.append("{\"id\":" + itemjsonString[i] + ",\"num\":" + itemJsonObject.getInt(itemjsonString[i]) + "}");
				h = h + 1;
				if(h == 3){
					h = 0;
					reward2 = reward2.append("]");
					list.add(reward2.toString());
					reward2 = new StringBuilder();
					reward2 = reward2.append("[");
				}
			}
			if(list == null || list.isEmpty()){
				reward2 = reward2.append("]");
				list.add(reward2.toString());
			}else if( h == 1 || h == 2){
				reward2 = reward2.append("]");
				list.add(reward2.toString());
			}
			
		}
		List<Mail> mailList = new ArrayList<Mail>();
		if(list.size() > 0){
			Map<Long, Mail> map = getMailListByRid(receiveId);
			for (int i = 0; i < list.size(); i++) {
				Mail mail = new Mail();
				mail.setReceiveId(receiveId);
				mail.setExpire(expire);
				mail.setContent(content);
				mail.setTitle(title);
				mail.setSendId(0);
				mail.setReaded(0);
				mail.setSendTime(TimeUtil.time());
				mail.setDelId(-1);
				mail.setRecycle(0);
				mail.setRecommend(recommend);
				mail.setReceive(1);
				mail.setReward(list.get(i));
				mailDao.insert(mail);
				map.put(mail.getId(), mail);
				mailList.add(mail);
			}
		}else{
			Map<Long, Mail> map = getMailListByRid(receiveId);
			Mail mail = new Mail();
			mail.setReceiveId(receiveId);
			mail.setExpire(expire);
			mail.setContent(content);
			mail.setTitle(title);
			mail.setSendId(0);
			mail.setReaded(0);
			mail.setSendTime(TimeUtil.time());
			mail.setDelId(-1);
			mail.setRecycle(0);
			mail.setRecommend(recommend);
			mail.setReward("[]");
			mail.setReceive(0);
			mailDao.insert(mail);
			map.put(mail.getId(), mail);
			mailList.add(mail);
		}
		for (int i = 0; i < mailList.size(); i++) {
			Map<Long, Mail> map = getMailListByRid(mailList.get(i).getReceiveId()); 
			map.put(mailList.get(i).getId(), mailList.get(i));
			GMsg_12015004.Builder builder = GMsg_12015004.newBuilder();
			MMail.Builder builder1 = MMail.newBuilder();
			Mail mail = mailList.get(i);
			builder1.setContent(mail.getContent());
			builder1.setExpire(mail.getExpire());
			builder1.setId(mail.getId());
			builder1.setReaded(mail.getReaded());
			builder1.setReceive(mail.getReceive());
			builder1.setReceiveId(mail.getReceiveId());
			builder1.setSendId(mail.getSendId());
			builder1.setSendTime(mail.getSendTime());
			builder1.setTitle(mail.getTitle());
			builder1.setRecommend(mail.getRecommend());
			List<ItemGet> mailItemList=JSONObject.decodeJsonArray(mailList.get(i).getReward(), ItemGet[].class);	
			for (ItemGet itemGet : mailItemList) {
				GReward.Builder gRewardBuilder = GReward.newBuilder();
				gRewardBuilder.setItemId(itemGet.getId());
				gRewardBuilder.setNum(itemGet.getNum());
				builder1.addRewardList(gRewardBuilder);
			}
			builder.addMailList(builder1);
			if(mailList.get(i).getReceiveId() != 0){
				Player player = roleFunction.getPlayer(mailList.get(i).getReceiveId());
				if(player != null){
					player.write(builder.build());
				}
				
			}else{
				roleFunction.sendMessageToAll(builder.build());
			}
		
		
	}
		
		return true;
		
	}
	
	@Override
	public void handleOnRoleLogout(Role role) {
		mapCache.remove(role.getRid());
	}
}
