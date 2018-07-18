package com.flosum.model;

import java.io.Serializable;
import java.util.List;

public class UpdateDescriptor implements Serializable{

	private static final long serialVersionUID = -7944823388268912518L;
	private List<BranchDescriptor> branchNameList;
	private Long repoId;
	private String repoUri;// url from repodescriptor
	
	public UpdateDescriptor(){
	}

	public UpdateDescriptor(String repoUri){
		this.repoUri = repoUri;
	}

	public UpdateDescriptor(List<BranchDescriptor> branchNameList, Long repoId, String repoUri){
		this.branchNameList = branchNameList;
		this.repoId = repoId;
		this.repoUri = repoUri;
	}

	public List<BranchDescriptor> getbranchNameList() {
		return branchNameList;
	}
	public void setbranchNameList(List<BranchDescriptor> branchNameList) {
		this.branchNameList = branchNameList;
	}
	public Long getRepoId() {
		return repoId;
	}
	public void setRepoId(Long repoId) {
		this.repoId = repoId;
	}

	public String getRepoUri() {
		return repoUri;
	}

	public void setRepoUri(String repoUri) {
		this.repoUri = repoUri;
	}
	
}
