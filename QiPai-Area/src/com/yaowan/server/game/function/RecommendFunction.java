package com.yaowan.server.game.function;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.ItemEvent;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.constant.RecommendStatType;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.PromotionAwardCache;
import com.yaowan.csv.entity.PromotionAwardCsv;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.dao.RoleRecommendDao;
import com.yaowan.server.game.model.data.dao.RoleDao;
import com.yaowan.server.game.model.data.entity.RoleRecommend;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.dao.RoleRecommendLogDao;
import com.yaowan.server.game.model.log.entity.RoleRecommendLog;

@Component
public class RecommendFunction extends FunctionAdapter{

	
	@Autowired
	private RoleRecommendDao recommendDao;
	
	@Autowired
	private RoleRecommendLogDao roleRecommendLogDao;
	
	@Autowired
	private RoleDao roleDao;
	
	@Autowired
	private RoleFunction roleFunction;
	
	@Autowired
	private PromotionAwardCache promotionAwardCache;

	@Autowired
	private ItemFunction itemFunction;
	
	/**
	 * 查询推荐信息
	 */
	public RoleRecommend getRecommendMsg(long rid) {
		RoleRecommend recommend = recommendDao.findByKey(rid);
		if(recommend == null) {
			recommend = new RoleRecommend();
			Role role = roleDao.findByKey(rid);
			recommend.setCode(role.getCode());
			recommend.setRid(rid);
			recommend.setCanGetReward(0);
			recommend.setHasGetReward(0);
			recommend.setRecommendFriendNum(0);
			recommend.setLevel(1);
			recommend.setIsOpen(1);
			recommend.setTimes(0);
		}
		
		return recommend;
 	}
	
	/**
	 * 判断推荐码
	 */
	public int checkRecommendNum(String recommendNum, long rid) {
		
		if(!isExitRecommend(recommendNum)) {
			return RecommendStatType.NOTEXIT;
		}
//		if(!isUserOpen(recommendNum)) {
//			return RecommendStatType.NOTEXIT;
//		}
		if(checkIsVisitor(rid)) {
			return RecommendStatType.ISVISITER;
		}
		if(isHasBeRecommend(rid)) {
			return RecommendStatType.ISBERECOMMEND;
		}else {
			if(isOverTime(recommendNum)) {
				return RecommendStatType.ISOVERTIME;
			}else {
				return RecommendStatType.SUCCESS;
			}
		}
	}
	
	/**
	 * 能不能领取奖励
	 * @param rid
	 * @return
	 */
	public boolean findIsCanGetMoney(long rid) {
		RoleRecommend recommend = recommendDao.findByKey(rid);
		if(recommend != null && recommend.getCanGetReward() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 判断是否为游客账号
	 */
	public boolean checkIsVisitor(long rid) {
		int type = roleDao.getLoginTypeByRid(rid);
		if(type == 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 判断绑定次数是否达到上限
	 */
	 public boolean isOverTime(String recommendNum) {
		 int num = recommendDao.getTodayTimes(recommendNum);
		 PromotionAwardCsv promotionAwardCsv = promotionAwardCache.getConfigList().
				 			get(promotionAwardCache.getConfigList().size() - 1);
		 if(num >= promotionAwardCsv.getDailyBindingLimit()) {
			 return true;
		 }
		 return false;
	 }
	 
	 /**
	  * 判断用户是否已经被推荐
	  * @param rid
	  * @return
	  */
	 public boolean isHasBeRecommend(long rid) {
		 int is_Recommend = roleDao.checkIsBeRecommend(rid);
		 if(is_Recommend == 1) {
			 return true;
		 }
		 return false;
	 }
	 
	 /**
	  * 判断推荐码是否存在
	  * @param recommend
	  * @return
	  */
	 public boolean isExitRecommend(String recommend) {
		 boolean isExit = roleDao.isExistRecommend(recommend);
		 return isExit;
	 }
	 
	 /**
	  * 判断用户是否被开启
	  * @param recommend
	  * @return
	  */
	 public boolean isUserOpen(String recommend) {
		 Integer isOpen = recommendDao.getUserIsOpen(recommend);
		 if(isOpen == null) {
			 return true;
		 }else {
			 if(isOpen == 1) {
				 return true;
			 }else {
				 return false;
			 }
		 }
	 }
	
	/**
	 * 被推荐人奖励
	 */
	public void bindReward(Role role) {
		PromotionAwardCsv promotionAwardCsv = promotionAwardCache.getConfigList().
							get(promotionAwardCache.getConfigList().size() - 1);
		String[] recipientReward = StringUtil.split(promotionAwardCsv.getRecipientReward(),"|");
		int itemId = Integer.parseInt(recipientReward[0]);
		int reward = Integer.parseInt(recipientReward[1]);
		if(itemId == 2010001) { //金币
			roleFunction.goldAdd(role, reward, MoneyEvent.RECOMMEND, true);
		}else {
			itemFunction.addPackItem(role, itemId, reward,
					ItemEvent.ExchangeItem, true);// 获得物品
		}
	}
	
	/**
	 * 得到推荐记录
	 * @param rid
	 * @return
	 */
	public RoleRecommend findRecommend(String code, long rid) {
		List<RoleRecommend> list = recommendDao.findByRecommend(code);
		if(list == null || list.size() == 0) {
			RoleRecommend recommend = new RoleRecommend();
			recommend.setRid(rid);
			recommend.setCode(code);
			recommend.setCanGetReward(0);
			recommend.setHasGetReward(0);
			recommend.setRecommendFriendNum(0);
			recommend.setTimes(0);
			recommend.setIsOpen(1);
			recommend.setLevel(1);
			recommendDao.insert(recommend);
			return recommend;
		}else {
			return list.get(0);
		}
	}
	
	/**
	 * 推荐人奖励
	 */
	public RoleRecommend recommendReward(RoleRecommend recommend) {
		PromotionAwardCsv promotionAwardCsv = promotionAwardCache.getConfigList().
							get(promotionAwardCache.getConfigList().size() - 1);
		String[] sponsorAward = StringUtil.split(promotionAwardCsv.getSponsorAward(), "|");
		int reward = Integer.parseInt(sponsorAward[1]);
//		Recommend recommend = recommendDao.findByRecommend(recommendNum).get(0);
		
		recommend.setCanGetReward(recommend.getCanGetReward() + reward);
		recommend.setRecommendFriendNum(recommend.getRecommendFriendNum() + 1);
		recommend.setTimes(recommend.getTimes() + 1);
		recommendDao.update(recommend);
		return recommend;
	}
	
	/**
	 * 通过推荐码查rid
	 * @param recommend
	 * @return
	 */
	public long findRidByRecommend(String recommend) {
		long rid = roleDao.findRidByCode(recommend);
		return rid;
	}
	
	
	/**
	 * 领取推荐奖励
	 * @return
	 */
	public int getRecommendReward(RoleRecommend recommend, Role role) {
		if(role == null) {
			return RecommendStatType.ISGETMONEYFAIL;
		}
		int canGetReward = recommend.getCanGetReward();
		if(canGetReward > 0) {
			roleFunction.goldAdd(role, canGetReward, MoneyEvent.RECOMMEND, true);
			recommend.setHasGetReward(recommend.getHasGetReward() + recommend.getCanGetReward());
			recommend.setCanGetReward(0);
			recommendDao.update(recommend);
		}
		return RecommendStatType.ISGETMONEYSYCCESS;
	}
	
	/**
	 * 插入推广日志
	 * @param recommendRid
	 * @param beRecommendRid
	 */
	public void insertRecommendLog(long recommendRid, long beRecommendRid) {
		LogUtil.debug("插入推广日志");
		RoleRecommendLog roleRecommendLog = new RoleRecommendLog();
		roleRecommendLog.setRecommendRid(recommendRid);
		roleRecommendLog.setBeRecommendRid(beRecommendRid);
		roleRecommendLog.setRecommendTime(TimeUtil.time());
		roleRecommendLogDao.insert(roleRecommendLog);
	}
	
	/**
	 * 更新用户被推荐状态
	 */
	public void updateIsBeRecommend(Role role) {
		role.markToUpdate("is_recommend");
		roleDao.updateProperty(role);
	}
	
//	public boolean updateIsOpen(Long rid, int status) {
//		if(recommendDao.findByKey(rid) == null) {
//			return false;
//		}
//		recommendDao.updateIsOpen(rid, status);
//		return true;
//	}
	
	
	public RoleRecommend findRecommendByRid(Role role) {
		RoleRecommend recommend = recommendDao.findByKey(role.getRid());
		return recommend;
	}
	
	@Override
	public void handleOnNextDay() {
		// TODO Auto-generated method stub
		recommendDao.updateTimes();
	}

}
