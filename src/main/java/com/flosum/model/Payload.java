package com.flosum.model;

import java.io.Serializable;
import java.util.List;

/**
 * This class used to transfer component's context between SF and Heroku
 */
public class Payload implements Serializable {

	private static final long serialVersionUID = 4L;

	private List<MetaItem> items;
	private Long opId;//need to return id of operation during upload data
	private String compType;
	private String compName;
	private String data;
	private String commitId;
	private String branchId;
	private Long repositoryId;

	public Payload(Long opId) {
		this.opId = opId;
	}

	public Payload(String compType, String data, String branchId, String commitId) {
		this.compType = compType;
		this.data = data;
		this.commitId = commitId;
		this.branchId = branchId;
	}

	public Payload(String compType, String data, String branchId, String commitId, Long repositoryId) {
		this.compType = compType;
		this.data = data;
		this.commitId = commitId;
		this.branchId = branchId;
		this.repositoryId = repositoryId;
	}

	// need for deserialization
	public Payload() {
	}
	
	/**
	 * 		Methods
	 */
	
	public Integer size(){
		if (items != null){
			return items.size();
		}
		return 0;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public String getCompType() {
		return compType;
	}

	public void setCompType(String compType) {
		this.compType = compType;
	}

	public String getBranchId() {
		return branchId;
	}

	public void setBranchId(String branchId) {
		this.branchId = branchId;
	}

	public Long getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(Long repositoryId) {
		this.repositoryId = repositoryId;
	}


	public String getCompName() {
		return compName;
	}

	public void setCompName(String compName) {
		this.compName = compName;
	}

	public List<MetaItem> getItems() {
		return items;
	}

	public void setItems(List<MetaItem> items) {
		this.items = items;
	}

	public Long getOpId() {
		return opId;
	}

	public void setOpId(Long opId) {
		this.opId = opId;
	}



}