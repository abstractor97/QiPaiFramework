/**
 * 
 */
package com.yaowan.framework.core.events;


/**
 * 事件
 * 
 * @author Thomas Zheng
 * 
 */
public class Event {

	/**
	 * 事件类型 {@link EventType}
	 */
	private int eventType;

	/**
	 * 事件句柄
	 */
	private int handle;

	/**
	 * 事件参数
	 */
	private Object[] param;

	/**
	 * 处理结果
	 */
	private Object result;

	/**
	 * 公司ID
	 */
	private Long characterId;

	/**
	 * 处理标志
	 */
	private Integer flag;

	public Integer getFlag() {
		return flag;
	}

	public void setFlag(Integer flag) {
		this.flag = flag;
	}

	public Event() {
	}
	
	/**
	 * @param eventType
	 *            事件类型{@link Event}
	 * @param handle
	 *            事件句柄,对于使用道具就是道具ID
	 * @param param
	 *            事件参数
	 */
	public Event(int handle, Object... param) {
		this.eventType = handle/1000;
		this.handle = handle;
		this.param = param;
	}

	/**
	 * @param eventType
	 *            事件类型{@link Event}
	 * @param handle
	 *            事件句柄,对于使用道具就是道具ID
	 * @param param
	 *            事件参数
	 */
	public Event(int eventType, int handle, Object[] param) {
		this.eventType = eventType;
		this.handle = handle;
		this.param = param;
	}

	public int getEventType() {
		return eventType;
	}

	public void setEventType(int eventType) {
		this.eventType = eventType;
	}

	public int getHandle() {
		return handle;
	}

	public void setHandle(int handle) {
		this.handle = handle;
	}

	public Object[] getParam() {
		return param;
	}

	public void setParam(Object[] param) {
		this.param = param;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Long getCharacterId() {
		return characterId;
	}

	public void setCharacterId(Long characterId) {
		this.characterId = characterId;
	}

}
