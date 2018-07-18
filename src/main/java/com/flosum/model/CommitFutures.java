package com.flosum.model;

import java.io.Serializable;


public class CommitFutures  implements Serializable {
	private static final long serialVersionUID = 1765389942191112199L;


	public CommitFutures() {
	}

	private Long repoId;
	private String branchName;
	private GitActor committer;
	private String message;
	private Long commitTime;
	private Integer counter;

	public CommitFutures(String branchName, GitActor committer, String msg, Long commitTime, Integer counter) {
		this.branchName = branchName;
		this.committer = committer;
		this.message = msg;
		this.commitTime = commitTime;
		this.counter = counter;
	}


	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public GitActor getCommitter() {
		return committer;
	}

	public void setCommitter(GitActor committer) {
		this.committer = committer;
	}

	public Long getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(Long commitTime) {
		this.commitTime = commitTime;
	}
	
	public Integer getCounter() {
		return counter;
	}

	public void setCounter(Integer counter) {
		this.counter = counter;
	}


	public Long getRepoId() {
		return repoId;
	}


	public void setRepoId(Long repoId) {
		this.repoId = repoId;
	}
	
	
}
