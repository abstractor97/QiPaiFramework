package com.yaowan.httpserver.handler.entity;

public class ResultInfo<T> {
	
	/**
	 * 状态码
	 */
	private String result;
	
	/**
	 * 成功的状态描述
	 */
	private String resultMsg;
	
	/**
	 * 失败的描述
	 */
	private String errno;
	
	/**
	 * 失败的信息
	 */
	private String errmsg;
	
	/**
	 * 返回数据
	 */
	private T data;	

	public ResultInfo() {		
	}
	
	public ResultInfo(String errno, String errmsg, T data) {
		super();
		this.errno = errmsg;
		this.errmsg = errmsg;
		this.data = data;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String resultCode) {
		this.result = resultCode;
	}

	public String getResultMsg() {
		return resultMsg;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public String getErrno() {
		return errno;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrno(String errno) {
		this.errno = errno;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	@Override
	public String toString() {
		return "ResultInfo [result=" + result + ", resultMsg=" + resultMsg
				+ ", errno=" + errno + ", errmsg=" + errmsg + ", data=" + data
				+ "]";
	}

	
}
