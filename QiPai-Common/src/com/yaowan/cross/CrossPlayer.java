package com.yaowan.cross;

import io.netty.channel.Channel;

import com.google.protobuf.GeneratedMessageLite;
import com.yaowan.framework.netty.Message;
import com.yaowan.model.struct.Player;

public class CrossPlayer extends Player {
	
	private volatile boolean locked = false;//状态锁
	//CrossCMD.Retransmission = 2
	private final short cmd = (short)2;
	
	private long tmpRid = 0;
	
	public CrossPlayer(long rid){
		super(null);
		tmpRid = rid;
	}
	public CrossPlayer(Channel channel) {
		super(channel);
	}
	
	@Override
	public long getId() {
		long rid = super.getId();
		if(rid == 0){
			return tmpRid;
		}
		return rid;
	}
	/**
	 * 锁定该连接，用来表明当前已有线程在处理该连接的网络包
	 * 注意：该方法非线程安全
	 * @return true加锁成功，false加锁失败
	 */
	public boolean tryLock() {
        return !locked && (locked = true);
	}
	
	/**
	 * 解锁连接
	 */
	public void unlock() {
		locked = false;
	}
	
	
	public void write(GeneratedMessageLite msg){
		write(cmd, msg);
	}
	public void write(short cmd,GeneratedMessageLite msg) {

		Message message = Message.build(msg);
		write(cmd,message);
	}
	public void write(Message message) {
		write(cmd,message);
	
	}
	public void write(short cmd,Message message){
		BasePacket basePacket = new BasePacket(cmd, message.getProtocol(),getId(),message.getMsgBody());
		write(basePacket);
	}
	
	public void write(BasePacket basePacket){
		getChannel().writeAndFlush(basePacket);
	}
	
	
	
}
