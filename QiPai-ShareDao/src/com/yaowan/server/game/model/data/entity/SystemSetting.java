/**
 * 
 */
package com.yaowan.server.game.model.data.entity;

import java.util.Map;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.annotation.Transient;
import com.yaowan.framework.util.StringUtil;



/**
 * @author zane
 *
 */
@Table(name = "system_setting",comment = "系统设置")
public class SystemSetting {
	
	/**
	 * 代码类型
	 * 
	 * @author zane 2017年01月09日 下午5:13:35
	 */
	public enum CodeType {
		/**
		 * 不使用
		 */
		NULL,
		/**
		 * 签到字段清理
		 */
		SIGN
		
	}

	public SystemSetting() {
		this.aiOpenInfo = "";
		this.resetInfo = "";
	}

	@Column(comment = "ai开关")
	private String aiOpenInfo;

	/**
	 * ai开关
	 */
	@Transient
	private Map<Integer,Integer> aiOpenInfoMap;
	
	@Column(comment = "代码清理标志")
	private String resetInfo;
	
	/**
	 * 代码字段
	 */
	@Transient
	private Map<Integer,Integer> resetInfoMap;

	public String getAiOpenInfo() {
		return aiOpenInfo;
	}

	public void setAiOpenInfo(String aiOpenInfo) {
		this.aiOpenInfo = aiOpenInfo;
	}

	public Map<Integer, Integer> getAiOpenInfoMap() {
		if (aiOpenInfoMap == null) {
			aiOpenInfoMap = StringUtil.stringToMap(aiOpenInfo, "|", "_",
					Integer.class, Integer.class);
		}
		return aiOpenInfoMap;
	}

	public void setAiOpenInfoMap() {
		this.aiOpenInfo = StringUtil.mapToString(aiOpenInfoMap);
	}

	public String getResetInfo() {
		return resetInfo;
	}

	public void setResetInfo(String resetInfo) {
		this.resetInfo = resetInfo;
	}

	public Map<Integer, Integer> getResetInfoMap() {
		if (resetInfoMap == null) {
			resetInfoMap = StringUtil.stringToMap(resetInfo, "|", "_",
					Integer.class, Integer.class);
		}
		return resetInfoMap;
	}

	public void setResetInfoMap() {
		this.resetInfo = StringUtil.mapToString(resetInfoMap);
	}
	
	


	
}
