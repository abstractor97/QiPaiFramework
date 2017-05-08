package com.yaowan.server.game.model.dezhou.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.constant.GameType;
import com.yaowan.csv.entity.DZConfigCsv;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.DZCardProto;
import com.yaowan.protobuf.game.GGame.PlayerState;
import com.yaowan.server.game.model.dezhou.DZPlayer;
import com.yaowan.server.game.model.dezhou.DZRoom;
import com.yaowan.server.game.model.dezhou.service.DZCardService;
import com.yaowan.util.RandomPlayerCount;
/**
 * 德州
 * @author YW0941
 *
 */
@Component
public class DZCardHandler extends GameHandler {
	@Autowired
	private DZCardService dzCardService;
	
	@Override
	public int moduleId() {
		
		return GameModule.DEZHOU;
	}

	@Override
	public void register() {
		addExecutor(1, 1000, false, new ServerExecutor() {
			/***
			 * 获取列表德州房类型对应的在线人数
			 */
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				List<Integer> roomTypes = dzCardService.getRoomTypes();
				DZCardProto.GMsg_12051001.Builder builder = DZCardProto.GMsg_12051001.newBuilder();
				
				DZCardProto.Online.Builder builder2 = DZCardProto.Online.newBuilder(); 
				for (Integer roomType : roomTypes) {
					builder2.setId(roomType);
					builder2.setOnline(dzCardService.getPlayerCountByRoomType(roomType));
					builder.addOnlines(builder2.build());
					builder2.clear();
				}
				player.write(builder.build());
			}
		});
		
		addExecutor(2, 1000, false, new ServerExecutor() {
			/***
			 * 获取制定房间类型，所有房间的在线人数
			 */
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				DZCardProto.GMsg_11051002  request = DZCardProto.GMsg_11051002.parseFrom(data);
				int roomType = request.getRoomType();
				
				DZCardProto.GMsg_12051002.Builder builder = DZCardProto.GMsg_12051002.newBuilder();  
				DZCardProto.Online.Builder builder2 = DZCardProto.Online.newBuilder(); 
				
				List<DZConfigCsv> dzConfigCsvs = dzCardService.getDzConfigCsvs(roomType);
				
				for (DZConfigCsv dzConfigCsv : dzConfigCsvs) {
					builder2.setId(dzConfigCsv.getID());
					builder2.setOnline(dzCardService.getPlayerCountByCid(dzConfigCsv.getID())+RandomPlayerCount.roomCount(GameType.DEZHOU));
					builder.addOnlines(builder2.build());
					builder2.clear();
				}
				player.write(builder.build());
			}
		});
		

		addExecutor(3, 1000, false, new ServerExecutor() {
			/***
			 * 点击房间进入
			 * GMsg_11051003
			 */
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				
			
//				DZCardProto.ObjBytes.Builder builder = DZCardProto.ObjBytes.newBuilder();
//				builder.setValue(ByteString.copyFrom(data));
				
//				DZCardProto.ObjBytes objBytes = builder.build();
//				byte[] value = objBytes.getValue().toByteArray();
//				DZCardProto.GMsg_11051003 request = DZCardProto.GMsg_11051003.parseFrom(data);
//				int roomId = request.getRoomId();
//				System.out.println(roomId);
//				CGame.CMsg_21100007.Builder builder = CGame.CMsg_21100007.newBuilder();
//				builder.setCmd(CenterCMD.ServerListAction_VALUE);
//				builder.setData(ByteString.copyFrom(new byte[0]));
//				NettyClient.write(builder.build());
				
			
				
//				player.write(builder.build());
				
				
////				GMsg_11051003
//				DZCardProto.GMsg_11051003 request = DZCardProto.GMsg_11051003.parseFrom(data);
//				int roomId = request.getRoomId();
//				DZPlayer dzPlayer = dzCardService.enterRoom(player, roomId);
//				DZCardProto.GMsg_12051003.Builder builder = DZCardProto.GMsg_12051003.newBuilder();
//				builder.setHead(player.getRole().getHead());
//				builder.setRid(player.getRole().getRid());
//				builder.setPosition(dzPlayer.getPosition());
//				builder.setJeton(dzPlayer.getJeton());
//				builder.setRoomId(dzPlayer.getRoom().getGid());
//				player.write(builder.build());
			}
		});
		
		
		addExecutor(4, 1000, false, new ServerExecutor() {
			/***
			 * 下注处理(下注，跟注，全下，弃牌，让牌等)
			 * GMsg_11051004
			 */
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				DZCardProto.GMsg_11051004 request = DZCardProto.GMsg_11051004.parseFrom(data);
				long roomId = request.getRoomId();
				int op = request.getOp();
				int jeton = request.getJeton();
//				long rid = request.getRid();
				long rid = player.getRole().getRid();
				DZRoom room = dzCardService.getDzRoom(roomId);
				
				DZPlayer dzPlayer = room.getDzPlayer(rid);
				
				//TODO 先进行判断操作是否合理
				
				//推送数据
				DZCardProto.GMsg_12051004.Builder builder = DZCardProto.GMsg_12051004.newBuilder();
				builder.setJeton(jeton);
				builder.setOp(op);
				builder.setRoomId(roomId);
				
				//1下注，2跟注，3全下，4弃牌，5让牌
				switch (op) {
				case 1:
					dzPlayer.chipIn(jeton);
					break;
				case 2:
					dzPlayer.follow(jeton);
					break;
				case 3:
					dzPlayer.allIn(jeton);
					break;
				case 4:
					dzPlayer.giveupPai();
					break;
				case 5:
					dzPlayer.yieldPai();
					break;
				default:
					break;
				}
			}
		});
		
		addExecutor(5, 1000, false, new ServerExecutor() {
			/**
			 *  
			 *  准备中
			 *  request roomId 
			 *  
			 */
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				
				long roomId = DZCardProto.GMsg_11051005.parseFrom(data).getRoomId();
				DZRoom dzRoom = dzCardService.getDzRoom(roomId);
				dzRoom.getGame().getSpriteMap().get(player.getRole().getRid()).setStatus(PlayerState.PS_PREPARE_VALUE);
				
				DZCardProto.GMsg_12051005.Builder builder = DZCardProto.GMsg_12051005.newBuilder();
				builder.setStatus(1);
				dzRoom.sendMessageToAll(builder.build(), player.getRole().getRid());
			}
		});
		
		addExecutor(6, 1000, false, new ServerExecutor() {
			/***
			 *  开始游戏
			 */
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				
			}
		});
			
	}

}
