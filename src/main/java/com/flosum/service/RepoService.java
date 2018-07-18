package com.flosum.service;

import java.util.List;
import java.util.Set;

import com.flosum.dao.ScheduledTasks.UpdateDescriptor;
import com.flosum.model.*;

public interface RepoService {

	public Operation getOpStatus(Long opId);

	public List<Operation> getOpList(Integer size);

	public Operation resetOpStatus(Long opId);

	public Long setCredentials(GitCredentials gc, Long rid);

	public Long getData(Long repoId);
	public DataWrapper getBranches(GitPath path,Long opId);
	public DataWrapper getCommits(GitPath path, Long opId, Boolean  single);
	public DataWrapper getComponents(GitPath path, Long opId);
	public DataWrapper getContinueData(Long opId, Integer part);

	public Payload getCompPack(Payload p, Long opId);
	public Payload getCompPackSha(Payload p, Long opId);

	public Long push(Long ticket);

	public Long cloneWithAuth(GitCredentials gc);
	
	public Long setCommit(CommitFutures cf);

	public Long commitAll(Long ticket);

	public Long addCompPack(Payload p, Long ticket);

	public Boolean isRemoteValid(GitCredentials gc, Integer phase);


	
	public Boolean resetChanges(Long gid);
	
	public RepoDescrDigest getDescription(Long gitId);
	
	// additional service
	public Operation getOperationDetails(Long opId);

	public List<Operation> getActiveOp(Long signature);
	
	public SystemInfo getServerInfo();
	
	public List<UpdateDescriptor> getGitInfo();
	
	// deployment service
	public Long initUpload();

	public Long uploadData(PayloadD p, Long opId);

	public void deploymentResult(Long opId, AuthDetails auth, DeploymentOptions opt);
	
	//review service
	public Long initDataUpload();

	public Long uploadData(Payload p, Long opId);

	public PayloadA getResults(Long opId);




}
