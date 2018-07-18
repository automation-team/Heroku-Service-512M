package com.flosum.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *	Describes the cluster of operations associated with specific repository  
 *	Operations can  be of the following types:
 *  OP_TRANSACTION, OP_ADD, OP_COMMIT, OP_PUSH, OP_SETCREDS, OP_GETDATA
 *
 */
public class Slot {
	private Long repoId;
	// current transaction with state: inProgress; coincides with opId of Operation which initializes transaction
	// always points to operation to be executed
	private Long curTransactionId; 

	private Map <Long,Transaction> transMap;
	private List <Long> transactions;

	public Slot (Long repoId){
		this.repoId = repoId;
		this.transMap = new HashMap<Long,Transaction>();
		this.transactions = new ArrayList<Long>();
	}
	
	public Boolean addTransaction(Long opId){
		// do not add the same op twice if already in list
		if (transMap.containsKey(opId)) return false;
		if (transMap.isEmpty()){
			transMap.put(opId, new Transaction(opId, true));
		}else{
			transMap.put(opId, new Transaction(opId));
		}
		transactions.add(opId);
		return true;
	}

	public Boolean removeTransaction(Long opId){
		if (transMap.containsKey(opId)){// remove only if exists in map
			transactions.remove(opId);
			transMap.remove(opId);
			return true;
		}
		return false;
	}

	/**
	 * Fills transaction with ids of operations which are included in it
	 * @param opId - id of operation to add
	 * @param trId - id of transaction which cover this operation
	 */
	public Boolean addToTransaction(Long opId, Long trId){
		if (transMap.containsKey(trId)){
			transMap.get(trId).add(opId);
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @return - Transaction from map defined by its id; Or null if such transaction do not exist
	 */
	public Transaction getTransaction(Long trId) {
		if (transMap.containsKey(trId)){
			return transMap.get(trId);
		}
		return null;
	}
	/**
	 * Specialized version of previous method
	 * @return
	 */
	public Transaction getCurrentTransaction() {
		if (transactions.size() != 0){
			return transMap.get(transactions.get(0));
		}
		return null;
	}

	public Boolean isEmpty(){
		return  transactions.isEmpty();
	}

	public List<Long> getTransactions() {
		return transactions;
	}

	

}
