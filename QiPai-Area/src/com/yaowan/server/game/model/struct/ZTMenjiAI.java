package com.yaowan.server.game.model.struct;

public class ZTMenjiAI {
	
	private int id;//AI位移编号
	
	private int lookProbability;//AI看牌率
	
	private int competeProbability;//AI比牌率
	
	private int foldProbability;//AI弃牌率
	
	private int followProbability;//AI跟注率
	
	private int addProbability;//AI加注率
	
	private int lookValue;//AI看牌值
	
	private int darkFollowRate;//AI蒙牌概率 （以100为单位）
	

	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLookProbability() {
		return lookProbability;
	}

	public void setLookProbability(int lookProbability) {
		this.lookProbability = lookProbability;
	}

	public int getCompeteProbability() {
		return competeProbability;
	}

	public void setCompeteProbability(int competeProbability) {
		this.competeProbability = competeProbability;
	}

	public int getFoldProbability() {
		return foldProbability;
	}

	public void setFoldProbability(int foldProbability) {
		this.foldProbability = foldProbability;
	}

	public int getFollowProbability() {
		return followProbability;
	}

	public void setFollowProbability(int followProbability) {
		this.followProbability = followProbability;
	}

	public int getAddProbability() {
		return addProbability;
	}

	public void setAddProbability(int addProbability) {
		this.addProbability = addProbability;
	}

	public int getLookValue() {
		return lookValue;
	}

	public void setLookValue(int lookValue) {
		this.lookValue = lookValue;
	}

	public int getDarkFollowRate() {
		return darkFollowRate;
	}

	public void setDarkFollowRate(int darkFollowRate) {
		this.darkFollowRate = darkFollowRate;
	}
	
	

}
