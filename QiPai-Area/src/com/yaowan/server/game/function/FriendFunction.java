/**
 * 
 */
package com.yaowan.server.game.function;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.ExpCache;
import com.yaowan.server.game.model.data.dao.FriendDao;
import com.yaowan.server.game.model.data.entity.Friend;
import com.yaowan.server.game.model.data.entity.Role;

/**
 * @author zane
 *
 */
@Component
public class FriendFunction extends FunctionAdapter {
	
	@Autowired
	private FriendDao friendDao;
	
	@Autowired
	private ExpCache expCache;
	
	@Autowired
	private RoleFunction roleFunction;
	
	/**
	 * 好友缓存
	 */
	private final ConcurrentHashMap<Long, Friend> friendCacheMap = new ConcurrentHashMap<>();
	
	/**
	 * 查询好友信息
	 *
	 * @param rid
	 * @return
	 */
	public List<Role> listFriends(long rid) {
		List<Role> list = new ArrayList<Role>();
		for (Long id : friendDao.getFriends(rid)) {
			list.add(roleFunction.getRoleByRid(id));
		}
		return list;
	}
	
	/**
	 * 查询自己申请别人的信息
	 * 
	 * @param rid
	 * @return
	 */
	public List<Role> listAsks(long rid){
		List<Role> list = new ArrayList<Role>();
		for (Long id : friendDao.getAsks(rid)) {
			list.add(roleFunction.getRoleByRid(id));
		}
		return list;
	}
	
	/**
	 * 查询别人申请自己的信息
	 * 
	 * @param rid
	 * @return
	 */
	public List<Role> listApplys(long rid){
		List<Role> list = new ArrayList<Role>();
		for (Long id : friendDao.getApplys(rid)) {
			list.add(roleFunction.getRoleByRid(id));
		}
		return list;
	}
	
	
	/**
	 * 把添加到缓存中
	 * 
	 * @param role
	 */
	private void addCache(Friend friend) {
		friendCacheMap.put(friend.getId(), friend);
	}
	/**
	 * 
	 *
	 * @param rid
	 * @return
	 */
	public Friend getFriend(long id) {
		Friend friend = friendCacheMap.get(id);
		if (friend != null) {
			return friend;
		}
		friend = friendDao.findByKey(id);
		if(friend != null) {
			addCache(friend);
		}
		return friend;
	}
	
	public Friend getFriend(long rid1, long rid2) {
		Friend friend = friendDao.getFriend(rid1, rid2);
		if (friend != null) {
			addCache(friend);
		}
		return friend;
	}
	
    /**
     * 申请好友（注意：你申请了别人好友后，别人也可申请你好友）
     * 保存了各自的关系
     * @param rid1
     * @param rid2
     */
    public void applyFriend(long rid1,long rid2) {    	
    	Friend friend = friendDao.getFriend(rid1, rid2);
    	if(friend==null){
    		friend = new Friend(rid1,rid2);
    		friend.setStatus("0");
    		friendDao.insert(friend);
    	}else{
    		List<Integer> statusList=friend.getStatusList();
    		statusList.add(0);
    		friend.setStatusList();
    		friendDao.update(friend);
    	}
    	Friend targetFriend = friendDao.getFriend(rid2,rid1);
    	if(targetFriend==null){
    		targetFriend = new Friend(rid2,rid1);
    		targetFriend.setStatus("1");
    		friendDao.insert(targetFriend);
    	}else{
    		List<Integer> statusList=targetFriend.getStatusList();
    		statusList.add(1);
    		targetFriend.setStatusList();
    		friendDao.update(targetFriend);
    	}
    	
    	/*Friend friend = new Friend(rid1,rid2);
    	friend.getStatusList();
    	friendDao.insert(friend);
    	
    	Friend targetFriend = new Friend(rid2,rid1);
    	targetFriend.setStatus(1);
    	friendDao.insert(targetFriend);*/ 
    	
	}
    
    public void updateFriendApplys(long rid1,long rid2){
    	Friend friend = friendDao.getFriend(rid1, rid2);
    	if(friend!=null){
    		List<Integer> statusList=friend.getStatusList();
    		Integer integer=1;
    		statusList.remove(integer);
    		if(statusList.size()==0){
    			deleteFriend(rid1,rid2);
    		}else{
    			friend.setStatusList();
    			friendDao.update(friend);
    		}
    	}	
    	Friend friend2 = friendDao.getFriend(rid2, rid1);
    	if(friend2!=null){
    		List<Integer> statusList=friend2.getStatusList();
    		Integer integer=0;
    		statusList.remove(integer);
    		if(statusList.size()==0){
    			deleteFriend(rid2,rid1);
    		}else{
    			friend2.setStatusList();
    			friendDao.update(friend2);
    		}
    	}    	
    }
    
    public void agreeFriend(long rid1,long rid2) {
    	friendDao.agreeFriend(rid1, rid2);
	}
    
    public void deleteFriend(long rid1,long rid2) {
    	friendDao.deleteFriend(rid1, rid2);
	}
    
    @Override
	public void handleOnRoleLogout(Role role) {
    	friendCacheMap.remove(role.getRid());
	}
	
}
