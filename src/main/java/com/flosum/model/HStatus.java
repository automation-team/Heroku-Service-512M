package com.flosum.model;

import java.io.Serializable;

public class HStatus implements Serializable {
	private static final long serialVersionUID = 9L;

	private String status;
	private String curJob;
	private String msg;

	public HStatus() {
	}

	public HStatus(String status, String curJob, String msg) {
		this.status = status;
		this.curJob = curJob;
		this.msg = msg;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCurJob() {
		return curJob;
	}

	public void setCurJob(String curJob) {
		this.curJob = curJob;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}