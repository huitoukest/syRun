package com.tingfeng.syRun.common.bean.response;

public class ResponseBean{
	/**
	 * 对应请求消息中的唯一id
	 */
	public String id;
	/**
	 * 回应的数据内容
	 */
	public String data;

	public int status;
	/**
	 * 错误状态会有相应的提示信息,正确是无内容
	 */
	public String errorMsg;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
}