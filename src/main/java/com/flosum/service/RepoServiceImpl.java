package com.flosum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flosum.constants.ServiceConst;
import com.flosum.dao.OperationalFabric;
import com.flosum.dao.RepoDAO;
import com.flosum.dao.RepoDAOImpl;
import com.flosum.dao.ScheduledTasks.UpdateDescriptor;
import com.flosum.model.*;

import java.util.List;

@Service
public class RepoServiceImpl implements RepoService {
	
	private final static OperationalFabric opFabric = RepoDAOImpl.getOperationFabric();

	@Autowired
	RepoDAO repoDAO;
	

	@Override
	public Operation getOpStatus(Long opId) {
		return repoDAO.getOpStatus(opId);
	}

	@Override
	public List<Operation> getOpList(Integer size) {
		return repoDAO.getOpList(size);
	}

	@Override
	public Operation resetOpStatus(Long opId) {
		return repoDAO.resetOpStatus(opId);
	}
	
	@Override
	public Long getData(Long repoId) {
		return repoDAO.getData(repoId);
	}

	@Override
	public Long setCredentials(GitCredentials gc, Long rid) {
		return repoDAO.setCredentials(gc, rid);
	}

	@Override
	public DataWrapper getBranches(GitPath path, Long opId) {
		return repoDAO.getBranches(path,opId);
	}

	@Override
	public DataWrapper getCommits(GitPath path, Long opId, Boolean single) {
		return repoDAO.getCommits(path, opId, single);
	}

	@Override
	public DataWrapper getComponents(GitPath path, Long opId) {
		return repoDAO.getComponents(path,opId);
	}

	@Override
	public Long cloneWithAuth(GitCredentials gc) {
		Long opId = opFabric.addOperation(new Operation(ServiceConst.OP_CLONE,ServiceConst.STATE_QUEUED,null,null));
		repoDAO.cloneWithAuth(gc,opId);
		return opId;
	}

	/**
	 *  4 commands from C-group
	 */
	@Override
	public Long push(Long ticket) {
		Operation trans = opFabric.getOperation(ticket);
		if (trans != null){// push operation always must have a non-null ticket field
			Long opId = opFabric.addOperation(new Operation(ServiceConst.OP_PUSH, ServiceConst.STATE_QUEUED, ticket, trans.getRepoID()) );
			repoDAO.push(opId);
			return opId;
		}else{
			return null;
		}
	}


	@Override
	public Long setCommit(CommitFutures cf) {
		return repoDAO.initUpload(cf);
	}

	@Override
	public Long commitAll(Long ticket) {
		Operation trans = opFabric.getOperation(ticket);
		if (trans != null){
			Long opId = opFabric.addOperation(new Operation(ServiceConst.OP_COMMIT, ServiceConst.STATE_QUEUED, ticket, trans.getRepoID()));
			repoDAO.commitAll(opId);
			return opId;
		}else{
			return null;
		}
	}

	@Override
	public Long addCompPack(Payload p, Long ticket) {
		Operation trans = opFabric.getOperation(ticket);
		if (trans != null){
			Long opId = opFabric.addOperation(new Operation(ServiceConst.OP_ADD, ServiceConst.STATE_QUEUED, ticket, trans.getRepoID()));
			repoDAO.addCompPack(p, opId, trans.getRepoID());
			return opId;
		}else{
			return null;
		}
	}

	@Override
	public Boolean isRemoteValid(GitCredentials gc, Integer phase) {
		return repoDAO.isRemoteValid(gc, phase);
	}

	@Override
	public Payload getCompPack(Payload p, Long opId) {
		return repoDAO.getCompPack(p, opId);
	}

	@Override
	public Boolean resetChanges(Long gid) {
		return repoDAO.rollBackRepoChanges(gid);
	}

	/**
	 * 
	 */
	@Override
	public RepoDescrDigest getDescription(Long gitId) {
		return repoDAO.getDescription(gitId);
	}

	/**
	 * Additional service - returns record of SOperation type
	 * This is an Idempotent operation, i.e. not changes the state 
	 * Note the difference from method - getOpStatus
	 */
	@Override
	public Operation getOperationDetails(Long opId) {
		return opFabric.getOperation(opId);
	}

	@Override
	public List<Operation> getActiveOp(Long signature) {
		return null;
	}

	@Override
	public DataWrapper getContinueData(Long opId, Integer part) {
		return repoDAO.getContinueData(opId, part);
	}

	@Override
	public SystemInfo getServerInfo() {
		return repoDAO.getServerInfo();
	}

	@Override
	public Payload getCompPackSha(Payload p, Long opId) {
		return repoDAO.getCompPackSha(p, opId);
	}

	@Override
	public List<UpdateDescriptor> getGitInfo() {
		return repoDAO.getGitInfo();
	}

	@Override
	public Long initDataUpload() {
		return repoDAO.initPackageUpload();
	}

	@Override
	public Long uploadData(PayloadD p, Long opId) {
		repoDAO.uploadPackage(p, opId);
		return opId;
	}

	@Override
	public void deploymentResult(Long opId, AuthDetails auth, DeploymentOptions opt) {
		repoDAO.deploy(opId, auth, opt);
	}

	@Override
	public Long initUpload() {
		return repoDAO.initUpload();
	}

	@Override
	public Long uploadData(Payload p, Long opId) {
		repoDAO.uploadData(p, opId);
		return opId;
	}

	@Override
	public PayloadA getResults(Long opId) {
		return repoDAO.getResults(opId);
	}


}
