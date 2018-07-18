package com.flosum.model;

import java.io.Serializable;

/**
 * This class used to transfer component's context between SF and Heroku
 */
public class PayloadA implements Serializable {

	private static final long serialVersionUID = 4L;

	private Long opId;//need to return id of operation during upload data
	private String data;
	private Long repositoryId;

	public PayloadA(Long opId) {
		this.opId = opId;
	}
	public PayloadA(Long opId, String data) {
		this.opId = opId;
		this.data = data;
	}

	// need for deserialization
	public PayloadA() {
	}
	
	/**
	 * 		Methods
	 */
	

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}


	public Long getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(Long repositoryId) {
		this.repositoryId = repositoryId;
	}

	public Long getOpId() {
		return opId;
	}

	public void setOpId(Long opId) {
		this.opId = opId;
	}



}