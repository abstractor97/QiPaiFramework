package com.yaowan.server.game.cross;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.protobuf.GeneratedMessageLite;
import com.yaowan.constant.GameType;
import com.yaowan.cross.BasePacket;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.listener.EventListener;
import com.yaowan.framework.core.handler.ProtobufCenter;
import com.yaowan.framework.util.ObjectAndByteArrayUtil;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.cmd.CMD.CenterCMD;
import com.yaowan.protobuf.cmd.CMD.CrossCMD;
import com.yaowan.protobuf.game.GCenter.IntVar;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.main.NettyClient;
import com.yaowan.server.game.model.data.entity.Role;
/**
 * 跨服处理
 * @author YW0941
 *
 */
/**
 * 跨服处理
 * @author YW0941
 *
 */
@Component
public class CrossFunction implements EventListener{
	//保存已经连接的所有的跨服服务器列表
	//crossServerId =>CrossGameClient
	private ConcurrentMap<Integer, CrossGameClient> crossGameClientMap = new ConcurrentHashMap<Integer, CrossGameClient>();
	//玩家发送过来的游戏请求
	private ConcurrentMap<Integer, RequestQueue> requestQueueMap = new ConcurrentHashMap<Integer, RequestQueue>();
	
	//rid=>gameType  玩家正在跨区服上玩的游戏
	private ConcurrentMap<Long, Integer> rid2GameTypeMap = new ConcurrentHashMap<Long, Integer>();
	
	//TODO 当增加新的跨区游戏时，需要把游戏类型添加进来
	private int[] crossGame = new int[] {GameType.DEZHOU,};
	@Autowired
	private RoleFunction roleFunction;
	class RequestQueue {
		private int gameType;
		
		private ConcurrentLinkedQueue<BasePacket> packetQueue =  new ConcurrentLinkedQueue<BasePacket>();
		public RequestQueue(int gameType){
			this.gameType = gameType;
		}
		public int getGameType() {
			return gameType;
		}

		public ConcurrentLinkedQueue<BasePacket> getPacketQueue() {
			return packetQueue;
		}	
	}
	/**
	 * 加入跨区游戏
	 * @param rid
	 */
	public void joinCrossGame(long rid,int gameType){
		rid2GameTypeMap.putIfAbsent(rid, gameType);
	}
	/**
	 * 是否在跨区游戏
	 * @param rid
	 * @return
	 */
	public boolean inCrossGame(long rid){
		return rid2GameTypeMap.containsKey(rid);
	}
	public int getJoinedGameType(long rid){
		if(rid2GameTypeMap.containsKey(rid)){
			return rid2GameTypeMap.get(rid);
		}
		return 0;
	}
	/**
	 * 退出跨区游戏
	 * @param rid
	 */
	public void quitCrossGame(long rid){
		rid2GameTypeMap.remove(rid);
	}
	
	/**
	 * 对CrossPlayer对象做初始化，在进入跨服游戏前必须调用
	 * @param gameType
	 * @param role
	 */
	public void initCrossPlayer(int gameType,Role role){
		byte[] roleBytes = ObjectAndByteArrayUtil.toByteArray(role);
		BasePacket basePacket = new BasePacket((short)CrossCMD.InitCrossPlayer_VALUE, 0, role.getRid(), roleBytes);
		forwardCrossServer(gameType, role.getRid(), basePacket);
	}
	/**
	 * 请求跳转封装
	 * @param gameType
	 * @param rid
	 * @param gmsgID
	 * @param gmsgData
	 */
	public void crossForward(int gameType,long rid,int gmsgID,byte[] gmsgData){
		BasePacket basePacket = new BasePacket((short)CrossCMD.Retransmission_VALUE, gmsgID, rid,gmsgData);
		forwardCrossServer(gameType, rid, basePacket);
	}
	
	/**
	 * GMsg_11006001跳转
	 * @param player
	 * @param gameType
	 * @param gmsgData
	 */
	public void crossGMsg_11006001(Player player,int gameType,byte[] gmsgData){
		//先初始化账号
		initCrossPlayer(gameType, player.getRole());
		crossForward(gameType,player.getId(),11006001,gmsgData);
	}
	
	/**
	 * GMsg_11006003跳转
	 * @param gameType
	 * @param rid
	 */
	public void crossGMsg_11006003(int gameType,long rid,byte[] gmsgData){
		BasePacket basePacket = new BasePacket((short)CrossCMD.Retransmission_VALUE, 11006003, rid,gmsgData);
		forwardCrossServer(gameType, rid, basePacket);
	}
	/**
	 * GMsg_11006004跳转
	 * @param gameType
	 * @param rid
	 */
	public void crossGMsg_11006004(int gameType,long rid,byte[] gmsgData){
		BasePacket basePacket = new BasePacket((short)CrossCMD.Retransmission_VALUE, 11006004, rid,gmsgData);
		forwardCrossServer(gameType, rid, basePacket);
	}
	/**
	 * GMsg_11006006跳转
	 * @param player
	 * @param gameType
	 * @param gmsgData
	 */
	public void crossGMsg_11006006(Player player, int gameType,byte[]gmsgData) {
		BasePacket basePacket = new BasePacket((short)CrossCMD.Retransmission_VALUE, 11006006, player.getId(),gmsgData);
		forwardCrossServer(gameType, player.getId(), basePacket);
	}
	
	/**
	 * 将请求转发给跨服服务器
	 */
	public void forwardCrossServer(int gameType,long rid,GeneratedMessageLite gMsgMesage){
		BasePacket basePacket = new BasePacket((short)CrossCMD.Retransmission_VALUE, ProtobufCenter.getProtocol(gMsgMesage.getClass()), rid, gMsgMesage.toByteArray());
		forwardCrossServer(gameType,rid,basePacket);
	}
	
	
	/**
	 * 将请求转发给跨服服务器
	 * @param gameType
	 * @param rid
	 * @param basePacket
	 */
	public void forwardCrossServer(int gameType,long rid,BasePacket basePacket){
		Player player = roleFunction.getPlayer(rid);
		if(player.getCrossServerId()>0){//已经与具体的跨服游戏建立了联系
			crossGameClientMap.get(player.getCrossServerId()).write(basePacket);
		}else {//询问中心服， 跟哪个跨服服务器建立连接
			NettyClient.request(CenterCMD.CrossServerGetAction_VALUE, IntVar.newBuilder().setVal(gameType).build().toByteArray());
			
//			Thread.sleep(100);
			
			if(!requestQueueMap.containsKey(gameType)){
				synchronized (requestQueueMap) {
					if(!requestQueueMap.containsKey(gameType)){
						requestQueueMap.put(gameType,new RequestQueue(gameType));
					}
				}
			}
			requestQueueMap.get(gameType).getPacketQueue().add(basePacket);
		}	
	}

	public void putCrossGameClient(int crossServerId,CrossGameClient crossGameClient){
		crossGameClientMap.put(crossServerId, crossGameClient);
	}
	
	public CrossGameClient getCrossGameClient(int crossServerId){
		return crossGameClientMap.get(crossServerId);
	}
	/**
	 * 执行转发
	 * @param crossServerId
	 */
	public void executeRetransmission(int crossServerId,CrossGameClient crossGameClient){
		
		Iterator<Integer> gameTypeIterator = crossGameClient.getGameTypes().iterator();
		while (gameTypeIterator.hasNext()) {
			Integer gameType = gameTypeIterator.next();
			final RequestQueue requestQueue = requestQueueMap.get(gameType);			
			Iterator<BasePacket> iterator = requestQueue.getPacketQueue().iterator();
			while (iterator.hasNext()) {
				final BasePacket basePacket = (BasePacket) iterator.next();
				Player player = roleFunction.getPlayer(basePacket.getRid());
				if(player.getCrossServerId() >0){
					crossGameClientMap.get(player.getCrossServerId()).write(basePacket);
					iterator.remove();
				}else {
					
					if(crossGameClient.getChannel()!=null && crossGameClient.getChannel().isActive()&& crossGameClient.getChannel().isOpen() && crossGameClient.getChannel().isWritable()){
						player.setCrossServerId(crossServerId);
						crossGameClient.write(basePacket);
						iterator.remove();
					}else {
						try {
							Thread.sleep(5);//等待与跨服服务器的连接成功
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						executeRetransmission(crossServerId,crossGameClient);
					}		
				}
			}
		}
	}
	
	
	@Override
	public void listenIn(Event event, int eventHandle) {
		//TODO  跨区监听处理
		
	}
	@Override
	public void addToEventHandlerAddListenerAdapter() {
		//TODO  跨区监听处理
	}
	
	/**
	 * 是否为跨服游戏
	 * @param gameType
	 * @return
	 */
	public boolean isCrossGame(int gameType) {
		if(Arrays.binarySearch(crossGame, gameType) != -1){
			return true;
		}
		return false;
	}
}
