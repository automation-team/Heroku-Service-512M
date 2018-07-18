package com.flosum.model;

import java.io.Serializable;
import java.util.List;

public class PayloadArray implements Serializable  {
	
	private static final long serialVersionUID = -14L;

	private List<MetaItem> items;
	private String compType;
	private String commitId;
	private String branchId;
	private Long repositoryId;


	// need for deserialization
	public PayloadArray() {
	}
	
	public Integer size(){
		if (items != null){
			return items.size();
		}
		return 0;
	}


	public List<MetaItem> getItems() {
		return items;
	}


	public void setItems(List<MetaItem> items) {
		this.items = items;
	}


	public String getCompType() {
		return compType;
	}


	public void setCompType(String compType) {
		this.compType = compType;
	}


	public String getCommitId() {
		return commitId;
	}


	public void setCommitId(String commitId) {
		this.commitId = commitId;
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
	

}
