package com.flosum.model;

import java.io.Serializable;

public class GitPath implements Serializable {

	private static final long serialVersionUID = -76L;
	
	private String branchId;
	private String commitId;
	private Long repositoryId;
//	private Long opId;
	

	public GitPath() {
	}

	public GitPath(String branchId, String commitId, Long repositoryId) {
		this.branchId = branchId;
		this.commitId = commitId;
		this.repositoryId = repositoryId;
//		this.opId = opId;
	}

	public String getBranchId() {
		return branchId;
	}

	public void setBranchId(String branchId) {
		this.branchId = branchId;
	}

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public Long getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(Long repositoryId) {
		this.repositoryId = repositoryId;
	}

/*	public Long getOpId() {
		return opId;
	}

	public void setOpId(Long opId) {
		this.opId = opId;
	}
*/
}
