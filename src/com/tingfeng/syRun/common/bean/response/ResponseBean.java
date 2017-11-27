package com.tingfeng.syrun.common.bean.response;

import com.tingfeng.syrun.common.ResponseStatus;

import java.io.Serializable;

public class ResponseBean implements Serializable{

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


	public static ResponseBean getResponse(ResponseStatus responseStatus,String msg){
		final ResponseBean response = new ResponseBean();
		response.setData(null);
		response.setStatus(responseStatus.getValue());
		response.setErrorMsg(msg);
		return response;
	}
}
