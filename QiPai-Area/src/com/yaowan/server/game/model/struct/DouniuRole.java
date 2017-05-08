package com.yaowan.server.game.model.struct;

import java.util.HashMap;
import java.util.Map;

import com.yaowan.model.struct.GameRole;
import com.yaowan.protobuf.game.GGame.PlayerState;


/**
 * @author zane
 *
 */
public  class DouniuRole implements IRoleBase{

	public DouniuRole() {

	}

	public DouniuRole(GameRole role) {
		this.role = role;
		this.reset();
	}

	public void reset() {
		if (role != null) {
			winGold = 0;
			winPower = 0;
			chips.clear();
			role.setStatus(PlayerState.PS_SEAT_VALUE);
		}
	}

	/**
	 * 初始角色 焖鸡离开座位可以为空
	 */
	private GameRole role = null;
	
    
    private int playerType;//0-等待者 1-庄家 2-押注者
    
    private Map<Integer,Integer> chips = new HashMap<Integer, Integer>();//押注
    
    private int winGold;//输赢
    
    private int winPower;//输赢倍数
    
    private int noGameCount;//连续不下注次数
	
	public int getPlayerType() {
		return playerType;
	}

	public void setPlayerType(int playerType) {
		this.playerType = playerType;
	}

	public GameRole getRole() {
		return role;
	}

	public void setRole(GameRole role) {
		this.role = role;
	}

	public Map<Integer, Integer> getChips() {
		return chips;
	}

	public void setChips(Map<Integer, Integer> chips) {
		this.chips = chips;
	}

	public int getWinGold() {
		return winGold;
	}

	public void setWinGold(int winGold) {
		this.winGold = winGold;
	}

	public int getWinPower() {
		return winPower;
	}

	public void setWinPower(int winPower) {
		this.winPower = winPower;
	}

	@Override
	public long getRid() {
		return role.getRole().getRid();
	}



	public int getNoGameCount() {
		return noGameCount;
	}

	public void setNoGameCount(int noGameCount) {
		this.noGameCount = noGameCount;
	}

	
}
