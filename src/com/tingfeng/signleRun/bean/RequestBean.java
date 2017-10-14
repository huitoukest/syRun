package com.tingfeng.signleRun.bean;

public class RequestBean<T extends BaseRequestParam> {
	/**
	 * 请求的类型
	 */
	public int type;
	/**
	 * 消息的唯一识别码
	 */
	public String id;
	/**
	 * 请求参数
	 */
	public T params;
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public T getParams() {
		return params;
	}

	public void setParams(T params) {
		this.params = params;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
