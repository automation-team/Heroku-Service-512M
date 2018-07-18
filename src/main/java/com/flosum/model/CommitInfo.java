package com.flosum.model;

public class CommitInfo {
	
	private String commitMsg;
	private String branchCommitTo;
	private GitActor actor;
	// for async packs upload - after packsCounter = 0 
	// the commit&push command executed
	private Integer packsCounter;
	private Boolean isNewBranch = false;
	
	public CommitInfo(String commitMsg, String branchCommitTo, GitActor actor,Integer packsCounter) {
		this.commitMsg = commitMsg;
		this.branchCommitTo = branchCommitTo;
		this.actor = actor;
		this.packsCounter = packsCounter; 
	}
	
	public String getCommitMsg() {
		return commitMsg;
	}
	public void setCommitMsg(String commitMsg) {
		this.commitMsg = commitMsg;
	}
	public String getBranchCommitTo() {
		return branchCommitTo;
	}
	public void setBranchCommitTo(String branchCommitTo) {
		this.branchCommitTo = branchCommitTo;
	}
	public GitActor getActor() {
		return actor;
	}
	public void setActor(GitActor actor) {
		this.actor = actor;
	}
	public Boolean getIsNewBranch() {
		return isNewBranch;
	}
	public void setIsNewBranch(Boolean isNewBranch) {
		this.isNewBranch = isNewBranch;
	}

	public Integer getPacksCounter() {
		return packsCounter;
	}

	public void setPacksCounter(Integer packsCounter) {
		this.packsCounter = packsCounter;
	}
	
	public Boolean next(){
		packsCounter--;
		return (packsCounter > 0);
	}

}
