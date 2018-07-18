package com.flosum.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.flosum.constants.ServiceConst;

/**
 *	Describes 1 transaction. Any transaction can contain 1 or more operations 
 *
 */
public class Transaction {
	public static final Integer OPENED = 0;
	public static final Integer QUEUED = 1;
	public static final Integer CLOSED = 2;
	
	
	private Integer defaultState;// initial state which the [0] operation has
	private final Long initOperation; // = opId of previous transaction; =null, if the current transaction is the first one
	private List <Long> operations;// list of transactional operations - the [0]  is always init operation
	
	/**
	 * The constructor 
	 * @param initOperation -  id of initial operation
	 */

	public Transaction(Long initOperation){
		this.defaultState = QUEUED;
		this.initOperation = initOperation;
		this.operations = new ArrayList<Long>();
		this.operations.add(initOperation);
	}

	public Transaction(Long initOperation, Boolean isOpened){
		this(initOperation);
		if (isOpened){
			this.defaultState = OPENED;
		}
	}

	/**
	 * 
	 * @param opId	 - id of operation to add to transaction
	 *  always added to tail of list
	 * @return
	 */
	public Boolean add(Long opId){
		this.operations.add(opId);
		return true;
	}
	
	
	/**
	 * Closes current operation
	 * @return  opId,  if success
	 */
	public Long close(Long opId){
		if (this.operations.contains(opId)){
			this.operations.remove(opId);
		}
		return opId;
	}

	/**
	 * Closes all operations in transaction ( usually used in case of canceling)
	 */
	public List<Long> closeAllOpen(){
		List<Long> closedOpList = new ArrayList<Long>(this.operations);
		this.operations.clear();
		return closedOpList;
	}
	
	public Boolean isEmpty(){
		return this.operations.isEmpty();
	}
	
	public Long getCurrent(){
		if (!this.operations.isEmpty()){
			return this.operations.get(0);
		}
		return null;
	}

	public Integer getDefaultState() {
		return defaultState;
	}

	public void setDefaultState(Integer defaultState) {
		this.defaultState = defaultState;
	}

	public List<Long> getOperations() {
		return operations;
	}

	
}
