package com.flosum.dao;

import com.flosum.dao.ScheduledTasks.UpdateDescriptor;
import com.flosum.model.*;

import java.util.List;

public interface RepoDAO {

	public Operation getOpStatus(Long opId);

	public List<Operation> getOpList(Integer size);

	public Operation resetOpStatus(Long opId);

	public Boolean rollBackRepoChanges(Long gitId);

	public RepoDescrDigest getDescription(Long gitId);

	public Long setCredentials(GitCredentials gc, Long repoId);

	public Long getData(Long rid);
	public DataWrapper getBranches(GitPath path, Long opId);
	public DataWrapper getCommits(GitPath path, Long opId, Boolean single);
	public DataWrapper getComponents(GitPath path, Long opId);
	public DataWrapper getContinueData(Long opId, Integer part);

	public Payload getCompPack(Payload p, Long opId);
	public Payload getCompPackSha(Payload p, Long opId);

	// Async
	public void push(Long opId);
	// Async
	public void cloneWithAuth(GitCredentials gc, Long opId);

	public Long initUpload(CommitFutures cf);

	// Async
	public void commitAll(Long opId);

	public void addCompPack(Payload p, Long opId, Long gitId);

	public Boolean isRemoteValid(GitCredentials gc, Integer phase);

	public SystemInfo getServerInfo();

	public List<UpdateDescriptor> getGitInfo();
	
	// for review extension
	public PayloadA getResults(Long opId);
	// Async
	public void uploadData(Payload p, Long opId);
	public Long initUpload();
	
	// for deployment extension
	// Async
	public void deploy(Long opId, AuthDetails auth, DeploymentOptions opt);
	public Long initPackageUpload();
	// Async
	public void uploadPackage(PayloadD p, Long opId);

}
