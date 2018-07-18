package com.flosum.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.flosum.constants.ServiceConst;

/**
 *	Describes an atomic operation 
 *
 */
public class Operation implements Serializable {
	
	private static final long serialVersionUID = -15L;
	
	private Long ticket; // id for transaction, === opId for operation with TRANSACTION type 
	private Long id; // opId
	private List<Long> idList; // list of opId for transaction
	private Integer parts; // number of parts if operatiion deal with big data
	private Long repoID; // inner id of repository the current operation deals with
	private Long sign; // signature in the form Category#gitID, generated automatically; used as generalization of repoID 
	private String opStatus;
	private String opType;
	private Long started; // set by system
	private Long ended;// set by system
	private String asyncId;
	private String log;
	
	private Boolean isSuccess;
	
	// constructor without arg creates an empty operation
	// used for testing
	public Operation() {
		this.opStatus = ServiceConst.STATE_NONE;
		this.opType = ServiceConst.STATE_NONE;
		this.isSuccess = false;
		this.ticket = -1L;
		this.idList = new ArrayList<Long>();
		this.repoID = -1L;
	}

	public Operation(String opType, Long ticket, Long repoID) {
		this.opStatus = ServiceConst.STATE_INPROGRESS;
		this.opType = opType;
		this.started = Instant.now().toEpochMilli();
		this.isSuccess = false;
		this.ticket = ticket;
		this.repoID = repoID;
		genSignature();
	}

	public Operation(String opType, String opStatus, Long ticket, Long repoID) {
		this.opStatus = opStatus;
		this.opType = opType;
		this.started = Instant.now().toEpochMilli();
		this.isSuccess = false;
		this.ticket = ticket;
		this.repoID = repoID;
		genSignature();
	}
	
	public Operation(String opType, String opStatus) {
		this.opStatus = opStatus;
		this.opType = opType;
		this.started = Instant.now().toEpochMilli();
		this.isSuccess = false;
	}


	
	public String getOpStatus() {
		return opStatus;
	}
	public void setOpStatus(String opStatus) {
		if (opStatus.equals(ServiceConst.STATE_WAITING)){// if waiting status set, change also the start time
			this.started = Instant.now().toEpochMilli();
		}
		this.opStatus = opStatus;
	}
	public String getOpType() {
		return opType;
	}
	public void setOpType(String opType) {
		this.opType = opType;
	}
	public Long getStarted() {
		return started;
	}
	public Long getEnded() {
		return ended;
	}
	public void setEnded(Long ended) {
		this.ended = ended;
	}
	public Boolean getIsSuccess() {
		return isSuccess;
	}
	public void setIsSuccess(Boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	public String getLog() {
		return log;
	}
	public void setLog(String log) {
		this.log = log;
	}

	public Long getTicket() {
		return this.ticket;
	}
	
	public void setTicket(Long ticket) {
		this.ticket = ticket;
	}

	public Long getRepoID() {
		return this.repoID;
	}

	public void setRepoID(Long repoID) {
		this.repoID = repoID;
	}

	public void completeWithStatus(Boolean status){
		if (!ServiceConst.STATE_CANCELED.equals(opStatus)){
			opStatus = ServiceConst.STATE_DONE;
			isSuccess = status;
			this.ended = Instant.now().toEpochMilli();
		}
	}
	public void completeWithStatus(Boolean status, Long id){
		if (!ServiceConst.STATE_CANCELED.equals(opStatus)){
			opStatus = ServiceConst.STATE_DONE;
			isSuccess = status;
			repoID = id;
			this.ended = Instant.now().toEpochMilli();
		}
	}
	public void completeWithStatus(Boolean status, String log){
		if (!ServiceConst.STATE_CANCELED.equals(opStatus)){
			opStatus = ServiceConst.STATE_DONE;
			isSuccess = status;
			this.log = log;
			this.ended = Instant.now().toEpochMilli();
		}
	}
	public void abort(){
		opStatus = ServiceConst.STATE_CANCELED;
		isSuccess = false;
		this.ended = Instant.now().toEpochMilli();
	}
	public void abort(String log){
		opStatus = ServiceConst.STATE_CANCELED;
		isSuccess = false;
		this.log = log;
		this.ended = Instant.now().toEpochMilli();
	}
	/*
	 *	Used to auto generate signature, based on data in operation record 
	 */
	public void genSignature(){
		if (this.repoID != null){
			this.sign = ServiceConst.getSignature(this.opType) + this.repoID;
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public Long getSign() {
		return sign;
	}

	public void setSign(Long sign) {
		this.sign = sign;
	}

	public List<Long> getIdList() {
		return idList;
	}

	public void setIdList(List<Long> idList) {
		this.idList = idList;
	}

	public void updateTransactionList(Long opId){
		if (this.idList == null){
			this.idList = new ArrayList<Long>();
		}
		this.idList.add(opId);
	}
	
	/**
	 * @return true, if operation had executed/to be executed
	 */
	public Boolean isAlive(){
		return !(opStatus.equals(ServiceConst.STATE_CANCELED) || opStatus.equals(ServiceConst.STATE_DONE) || opStatus.equals(ServiceConst.STATE_NONE)); 
	}

	/**
	 * @return true, if operation in queue and NOT TRANSACTION
	 * if filter != null, add conditions: AND ticket == filter
	 */
	public Boolean isQueueableFiltered(Long filter){
		if (filter == null){
			return isQueueable();
		}else{
			return isQueueable() && (ticket == filter);
		}
	}
	public Boolean isQueueable(){
			return opStatus.equals(ServiceConst.STATE_QUEUED);
	}
	
	public String toJson(){
		return "{" + "ticket:" + ticket + " id:" + id + " opStatus:" + opStatus + " opType:" + opType + " sign:" + sign + "}";
	}

	public Integer getParts() {
		return parts;
	}

	public void setParts(Integer parts) {
		this.parts = parts;
	}

	public String getAsyncId() {
		return asyncId;
	}

	public void setAsyncId(String asyncId) {
		this.asyncId = asyncId;
	}

}
