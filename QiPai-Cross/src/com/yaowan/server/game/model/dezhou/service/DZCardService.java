package com.yaowan.server.game.model.dezhou.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.csv.cache.DZConfigCache;
import com.yaowan.csv.entity.DZConfigCsv;
import com.yaowan.server.game.model.dezhou.DZRoom;
import com.yaowan.server.game.model.dezhou.function.DZCardFunction;

/**
 * 德州扑克
 * @author YW0941
 *
 */
@Component
public class DZCardService{
	@Autowired
	private DZCardFunction dzCardFunction;
	
	@Autowired
	private DZConfigCache dzConfigCache;
	
	/**
	 * 获取人数
	 * @param type
	 * @return
	 */
	public int getPlayerCountByCid(int cid){
		int count = 0;
		for (DZRoom dzRoom : dzCardFunction.getDzRooms()) {
			if(dzRoom.getCid() == cid){
				count += dzRoom.getPlayerCount();
			}
		}
		return count;
	}
	/**
	 * 获取制定类型的房间在线人数
	 * @param roomType
	 * @return
	 */
	public int getPlayerCountByRoomType(int roomType){
		int count = 0;
		for (DZRoom dzRoom : dzCardFunction.getDzRooms()) {
			if(dzConfigCache.getRoomType(dzRoom.getCid()) == roomType){
				count += dzRoom.getPlayerCount();
			}
		}
		return count;
	}
	public List<Integer> getRoomTypes(){
		return dzConfigCache.getRoomTypes();
	}
	
	/**
	 * 获取指定类型的房间配置列表
	 * @param roomType
	 * @return
	 */
	public List<DZConfigCsv> getDzConfigCsvs(int roomType){
		return dzConfigCache.getDzConfigCsvs(roomType);
	}
	/**
	 * 获取指定类型已开放的房间
	 * @param roomType
	 * @return
	 */
	public List<DZRoom> getDzRooms(int roomType){
		List<DZRoom> dzRooms = new ArrayList<DZRoom>();
		for (DZRoom dzRoom : dzCardFunction.getDzRooms()) {
			if(dzConfigCache.getRoomType(dzRoom.getCid()) == roomType){
				dzRooms.add(dzRoom);
			}
		}
		return dzRooms;
	}
	
//	/**
//	 * 进入房间
//	 * @param player
//	 * @param cid
//	 */
//	public DZPlayer enterRoom(Player player,int cid){
//		DZConfigCsv dzConfigCsv = dzConfigCache.getDzConfigCsv(cid);
//		
//		DZPlayer dzPlayer = new DZPlayer(player.getRole().getRid(), dzConfigCsv.getInitialJetton());
//		boolean seated = false;
//		for (DZRoom dzRoom : rooms.values()) {
//			if(dzRoom.getCid() != cid){
//				continue;
//			}
//			if(!dzRoom.canSeat()){
//				continue;
//			}
//			//安排座位
//			if(seated = dzRoom.seat(dzPlayer)){
//				break;
//			}
//		}
//		
//		if(!seated){//所有的座位坐满，新开房间
//			DZRoom room = new DZRoom(dzConfigCsv.getID(), dzConfigCsv.getPeople(), dzConfigCsv.getBigBlind());
//			//安排座位
//			room.seat(dzPlayer);
//			rooms.put(room.getGid(), room);
//		}
//		return dzPlayer;
//	}
	public DZRoom getDzRoom(long gid) {
		return dzCardFunction.getDzRoom(gid);
	}
	

	
}
