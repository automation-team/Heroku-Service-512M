package com.flosum.dao;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.flosum.model.Operation;

/**
 * Used to dynamically update operation table's wait section
 * Thread-safe
 * 
 * @author Alexey Kalutov
 * @since v.3.0
 *
 */
public final class OperationMap {
	
	private static final int MAX_SIZE = 1000;

	private ConcurrentMap<Long,Long> waitingRO;// wait status can have only 1 operation/repository
	private ConcurrentMap<Long,Long> waitingRW;
	

	public OperationMap() {
		waitingRO = new ConcurrentHashMap<Long,Long>();
		waitingRW = new ConcurrentHashMap<Long,Long>();
		
	}
	
	/**
	 * Try to move opId to waiting status
	 *  
	 * @param repoId
	 * @param opId
	 * @return true, if move successful
	 */
	public Boolean toWaitingRO(Long repoId, Long opId){
		if (this.waitingRO.containsKey(repoId)){
			this.waitingRO.replace(repoId, opId);
		}else{
			this.waitingRO.putIfAbsent(repoId, opId);
		}
		// verify, that replacement achieved
		if (this.waitingRO.get(repoId) == opId){//condition to cut  2nd thread, then  remove old key
			// now opId really in waiting map
			return true;
		}
		return false;//another thread already put opId, must nothing changed
	}
	
	/**
	 * Try to move opId to waiting status
	 *  
	 * @param repoId
	 * @param opId
	 * @return true, if move successful
	 */
	public Boolean toWaitingRW(Long repoId, Long opId){
		if (this.waitingRW.containsKey(repoId)){
			this.waitingRW.replace(repoId, opId);
		}else{
			this.waitingRW.putIfAbsent(repoId, opId);
		}
		// verify, that replacement achieved
		if (this.waitingRW.get(repoId) == opId){//condition to cut  2nd thread, then  remove old key
			return true;
		}
		return false;
	}
	
	/**
	 * Try to move opId from waiting to inProgress status
	 * waiting map always 1=>1, so this operation is thread-safe
	 *  
	 * @param repoId
	 * @return none
	 */
	public void toInProgressRO(Long repoId){
		if (this.waitingRO.containsKey(repoId)){
			this.waitingRO.remove(repoId);
		}
	}

	/**
	 * Try to move opId from waiting to inProgress status
	 * waiting map always 1=>1, so this operation is thread-safe
	 *  
	 * @param repoId
	 * @return none
	 */
	public void toInProgressRW(Long repoId){
		if (this.waitingRW.containsKey(repoId)){
			this.waitingRW.remove(repoId);
		}
	}

}
