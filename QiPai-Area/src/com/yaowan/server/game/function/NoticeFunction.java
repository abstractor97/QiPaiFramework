package com.yaowan.server.game.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.protobuf.game.GChat.GMsg_12008002;
import com.yaowan.protobuf.game.GChat.GMsg_12008006;
import com.yaowan.protobuf.game.GChat.LoginNoticeInfo;
import com.yaowan.server.game.model.data.dao.NoticeDao;
import com.yaowan.server.game.model.data.dao.RoleDao;
import com.yaowan.server.game.model.data.entity.Notice;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.dao.NoticeLogDao;

@Component
public class NoticeFunction extends FunctionAdapter {

	@Autowired
	NoticeDao noticeDao;
	@Autowired
	RoleFunction roleFunction;

	@Autowired
	private RoleDao roleDao;

	@Autowired
	private NoticeLogDao noticeLogDao;

	private final ConcurrentHashMap<Integer, Notice> noticeMap = new ConcurrentHashMap<>();

	/**
	 * 存储公告下一次的时间信息
	 */
	private final ConcurrentHashMap<Integer, Integer> noticeTimeMap = new ConcurrentHashMap<>();

	/** 缓存活动列表 Map<aid, activity> */
	private ConcurrentHashMap<Integer, Notice> activityCacheMap = new ConcurrentHashMap<>();

	/** 缓存登录公告列表 Map<aid, activity> */
	private Map<Integer, Notice> loginNoticeCacheMap = new ConcurrentHashMap<>();

	@Override
	public void handleOnServerStart() {
		// TODO Auto-generated method stub
		List<Notice> noticeList = noticeDao.getNoticeList();
		for (Notice notice : noticeList) {
			noticeMap.put(notice.getId(), notice);
			noticeTimeMap.put(notice.getId(), TimeUtil.time());
		}
		LogUtil.info("Load noticeMap size " + noticeMap.size());

		// 把数据库的活动添加到缓存
		List<Notice> notices = noticeDao.findAllStartTimeDesc();
		if (notices == null) {
			return;
		}
		for (Notice notice : notices) {
			activityCacheMap.put(notice.getId(), notice);
		}
		
		List<Notice> loginNotices = noticeDao.findLoginNotice();
		if (loginNotices == null) {
			return;
		}
		for (Notice notice : loginNotices) {
			loginNoticeCacheMap.put(notice.getId(), notice);
		}
	}

	@Override
	public void handleOnServerShutdown() {
		activityCacheMap.clear();
	}

	@Override
	public void handleOnRoleLogin(Role role) {
		// 推送登录公告
		if (loginNoticeCacheMap.isEmpty()) {
			return;
		}
		int time = TimeUtil.time();
		List<LoginNoticeInfo> list = new ArrayList<>();
		
		
		//修复登录公告不排序问题
		List<Notice> notices = new ArrayList<>(loginNoticeCacheMap.values());
		Collections.sort(notices, new Comparator<Notice>() {

			@Override
			public int compare(Notice o1, Notice o2) {
				return o1.getOrderBy() - o2.getOrderBy();
			}
		});
		
		for (Notice notice : notices) {
			if (notice.getStime() <= time && notice.getEtime() > time && notice.getStatus() == 1) {
				switch (notice.getLoginNoticeType()) {
				case 1:
					if (role.getDailyLogin() == 1) {
						loginNoticeInfoBuilder(list, notice);
					}
					break;
				case 2:
					loginNoticeInfoBuilder(list, notice);
					break;
				}
			} 
		}
		
		GMsg_12008006.Builder builder = GMsg_12008006.newBuilder();
		builder.addAllNoticeInfo(list);
		roleFunction.sendMessageToPlayer(role.getRid(), builder.build());
	}

	private void loginNoticeInfoBuilder(List<LoginNoticeInfo> list,
			Notice notice) {
		LoginNoticeInfo.Builder infoBuillder = LoginNoticeInfo.newBuilder();
		infoBuillder.setId(notice.getId());
		infoBuillder.setContent(notice.getContent());
		infoBuillder.setOrder(notice.getOrderBy());
		infoBuillder.setTitle(notice.getTitle());
		infoBuillder.setType(notice.getType());
		list.add(infoBuillder.build());
	}

	/**
	 * 增加公告
	 * 
	 * @param notice
	 */
	public void addNotice(Notice notice) {
		// 要限制条目
		noticeDao.insert(notice);
		switch (notice.getType()) {
		case 1: {
			if (notice.getStatus() == 1) {
				noticeMap.put(notice.getId(), notice);
				noticeTimeMap.put(notice.getId(), TimeUtil.time());
				// 添加的时候执行一次
				systemSendNOticeToAll();
			}
			break;
		}
		case 2: {
			if (notice.getStatus() == 1) {
				activityCacheMap.put(notice.getId(), notice);
			}
			break;
		}
		case 3: {
			if (notice.getStatus() == 1) {
				loginNoticeCacheMap.put(notice.getId(), notice);
			}
			break;
		}
		}
	}

	public Notice getNotice(int id) {
		Notice notice = noticeDao.findByKey(id);
		return notice;
	}

	/**
	 * 修改公告
	 * 
	 * @param notice
	 */
	public void updateNotice(Notice notice) {
		switch (notice.getType()) {
		case 1: {
			if (notice.getStatus() == 1) {
				noticeMap.put(notice.getId(), notice);
				noticeTimeMap.put(notice.getId(), TimeUtil.time());
			} else if (notice.getStatus() == 2) {
				noticeMap.remove(notice.getId());
				noticeTimeMap.remove(notice.getId());
			}
			break;
		}
		case 2: {
			if (notice.getStatus() == 1) {
				activityCacheMap.put(notice.getId(), notice);
			} else if (notice.getStatus() == 2) {
				activityCacheMap.remove(notice.getId());
			}
			break;
		}
		case 3: {
			if (notice.getStatus() == 1) {
				loginNoticeCacheMap.put(notice.getId(), notice);
			} else if (notice.getStatus() == 2) {
				loginNoticeCacheMap.remove(notice.getId());
			}
			break;
		}
		}

		noticeDao.update(notice);
	}

	/**
	 * 发送公告
	 * 
	 * @return
	 */
	public void systemSendNOticeToAll() {
		GMsg_12008002.Builder builder = GMsg_12008002.newBuilder();
		for (Map.Entry<Integer, Notice> entry : noticeMap.entrySet()) {
			Notice notice = entry.getValue();
			if (noticeSend(notice)) {
				builder.setRid(0);
				builder.setType(0);
				builder.setNick("系统");
				builder.setMessage(notice.getContent());
				builder.setLevel(notice.getLevel());
				roleFunction.sendMessageToAll(builder.build());
			}
		}
	}

	/**
	 * 判断是否可以发送公告
	 * 
	 * @param notice
	 * @return
	 */
	public boolean noticeSend(Notice notice) {
		int startTime = notice.getStime();// 开始时间
		int endTime = notice.getEtime();// 结束时间
		int time = TimeUtil.time();
		if (notice.getStatus() == 1 && startTime < time && endTime > time) {
			// LogUtil.info(noticeTimeMap.get(notice.getId())+" "+time);
			if (time >= noticeTimeMap.get(notice.getId())) {
				// LogUtil.info(notice.getContent() +
				// "Lpg"+TimeUtil.ONE_SECOND*notice.getDiff_time());
				noticeTimeMap.put(notice.getId(), time + TimeUtil.ONE_SECOND
						* notice.getDiff_time());
				return true;
			}
		}
		return false;
	}

	/**
	 * 统计点击量
	 * 
	 * @author G_T_C
	 * @param role
	 * @param aid
	 */
	public void addClickNum(Role role, int aid) {
		Map<Long, Integer> voteInfoMap = role.getVoteInfoMap();
		if (voteInfoMap != null && voteInfoMap.get(aid) != null
				&& voteInfoMap.get(aid) == 1) {
			return;
		} else {// 未点击
			long laid = aid;
			voteInfoMap.put(laid, 1);// 是否点击过 0为未点击，1为点击
			role.setVoteInfo(StringUtil.mapToString(voteInfoMap));
			role.markToUpdate("voteInfo");
			roleDao.updateProperty(role);
			noticeLogDao.updateclickNum(aid);
		}
	}

	public Map<Integer, Notice> getActivityCacheMap() {
		return activityCacheMap;
	}

}
