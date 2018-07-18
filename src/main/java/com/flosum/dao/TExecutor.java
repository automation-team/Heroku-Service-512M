package com.flosum.dao;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.constants.ServiceConst;
import com.flosum.model.CommitFutures;
import com.flosum.model.GitCredentials;
import com.flosum.model.Operation;
import com.flosum.model.Payload;

public class TExecutor {
	private OperationalFabric operationFabric;
	private GitFactory gitFactory;
	private CacheService cacheService;
	private final Logger LOG = LoggerFactory.getLogger(TExecutor.class);
	
	public TExecutor(OperationalFabric operationFabric, GitFactory gitFactory, CacheService cacheService) {
		this.operationFabric = operationFabric;
		this.gitFactory = gitFactory;
		this.cacheService = cacheService;
	}
	
	/**
	 *  Executes 1 queued operation
	 * @param curOperationId - id of operation to be executed
	 * @throws IOException
	 * @throws GitAPIException
	 */
	
	public void execute(Long curOperationId) throws IOException, GitAPIException {
		Operation curOperation = operationFabric.getOperation(curOperationId);// more details about performed operation
		if (curOperation == null) return;
		final Long RID = curOperation.getRepoID();
		final Long SIGN = curOperation.getSign();
		final String OPTYPE = curOperation.getOpType();
		LOG.info("exec opid={}",curOperationId);
		
		// executed only queued OR inprogress operations
		if (curOperation.getOpStatus().equals(ServiceConst.STATE_QUEUED) || curOperation.getOpStatus().equals(ServiceConst.STATE_INPROGRESS)) {
			// NB: for clone operation, client = null
			GitHandler client = gitFactory.getGitHandler(curOperation.getRepoID());
			if (client == null && !curOperation.getOpType().equals(ServiceConst.OP_CLONE)){
				operationFabric.cancelWithMsg(curOperation, ServiceConst.E_NULL_VALUE + "client, method execute");
				return;
			}
			// update operation status
			curOperation.setOpStatus(ServiceConst.STATE_INPROGRESS);
			// separately execute the clone operation
			if (OPTYPE.equals(ServiceConst.OP_CLONE)) {
				Object o =  cacheService.pop(CacheService.GITCREDS, curOperationId);
				if ( o != null){
					GitCredentials gc = (GitCredentials) o;
					if (gc.getProtocol().equals(ServiceConst.P_HTTPS)){
						gitFactory.cloneWithAuthHttps(gc, curOperation);
					}else if (gc.getProtocol().equals(ServiceConst.P_SSH)){
						gitFactory.cloneWithAuthSsh(gc, curOperation);
					}else{
						operationFabric.completeWithStatus(curOperationId,false, ServiceConst.E_WRONG_PROTOCOL);
					}
				}else{
					operationFabric.completeWithStatus(curOperationId,false, ServiceConst.E_NO_CACHE);
				}
			}else if (OPTYPE.equals(ServiceConst.OP_TRANSACTION)) {
				GitHandlerRW clientRW= gitFactory.getGitRWHandler(RID);
				Object o =  cacheService.pop(CacheService.COMMITFUTURES, curOperationId);
				if ( o != null){
					clientRW.setCommit((CommitFutures) o, curOperation);
					operationFabric.closeOperation(curOperation);
				}else{
					operationFabric.completeWithStatus(curOperationId, false, ServiceConst.E_NO_CACHE);
				}
			} else if (OPTYPE.equals(ServiceConst.OP_PUSH)) {
				GitHandlerRW clientRW= gitFactory.getGitRWHandler(RID);
				clientRW.pushAuth(curOperation);
				operationFabric.closeOperation(curOperation);
			} else if (OPTYPE.equals(ServiceConst.OP_COMMIT)) {
				GitHandlerRW clientRW= gitFactory.getGitRWHandler(RID);
				LOG.info("####        Committing         #####");
				for (Long id : clientRW.uploaded()) {
					LOG.info("opId: {}, status: {}",id,operationFabric.getOperation(id).getOpStatus());
				}
				LOG.info("####      ##############       #####");

				clientRW.commitAll(curOperation);
				operationFabric.closeOperation(curOperation);
			} else if (OPTYPE.equals(ServiceConst.OP_ADD)) {
				GitHandlerRW clientRW= gitFactory.getGitRWHandler(RID);
				Object o =  cacheService.pop(CacheService.PAYLOAD, curOperationId);
				if ( o != null){
					clientRW.uploadZipPack((Payload) o, curOperation);
					operationFabric.closeOperation(curOperation);
				}else{
					operationFabric.completeWithStatus(curOperationId,false, ServiceConst.E_NO_CACHE);
				}
			} else if (OPTYPE.equals(ServiceConst.OP_SETCREDS)) {
				GitHandlerRW clientRW= gitFactory.getGitRWHandler(RID);
				Object o =  cacheService.pop(CacheService.GITCREDS, curOperationId);
				if ( o != null){
					clientRW.setCredentials( (GitCredentials) o, curOperation);
					operationFabric.closeOperation(curOperation);
				}else{
					operationFabric.completeWithStatus(curOperationId,false, ServiceConst.E_NO_CACHE);
				}
			}else if (OPTYPE.equals(ServiceConst.OP_GETDATA)) {
				if (gitFactory.getGitHandler(RID) == null){// wrong rid
					operationFabric.cancelWithMsg(curOperation, ServiceConst.E_WRONG_RID);
				}else{
					// do nothing, status for operation left: inprogress
				}
			}
		}else{
			LOG.info("attempt to exec op with state = {}",curOperation.getOpStatus());
		}
	}
	

}
