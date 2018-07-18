package com.flosum.dao;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.constants.ServiceConst;
import com.flosum.model.Operation;
import com.flosum.model.Slot;
import com.flosum.model.Transaction;

public class OperationalFabric {
	
	// contains list of operations performed on the server
	private final Integer MAX_SIZE = 10000;
	private final Logger LOG = LoggerFactory.getLogger(OperationalFabric.class);
	private final CacheService cache;

	//	private ConcurrentMap<Long,Boolean> activeOperations;
	private QueuedMap<Long,Operation> opQueue;
	
	// contains the momentary snapshot of currently executed operations
	// can be updated only through [synchronized] accessSlot method
	private Map <Long, Slot> slots;
	
	private static final AtomicLong generateID = new AtomicLong();
	private static final long WAIT_TIME = 120000L; // = time given for actual execution of  operation from C-group
	private static final long WORK_TIME = 1200000L;// = max time for completion
	
	public static final Integer ADD = 0; 
	public static final Integer CLOSE = 1; 
	public static final Integer CANCEL = 2; 
	private volatile Long lastTransactionId;

	public OperationalFabric(CacheService cache) {
		opQueue = new QueuedMap<Long,Operation>(MAX_SIZE);
		slots = new  HashMap < Long, Slot >();
		this.cache = cache;
	}
	
	/**
	 * 	Alternative Sync method to access slot
	 *  NB: if transOpId does not specified use opId for access Transaction
	 */
	private synchronized Long accessSlot(Operation op, Integer opType, String msg){

		if (opType == ADD){
			op.setOpStatus(ServiceConst.STATE_INPROGRESS);
		}else if (opType == CLOSE){// remove transactions description from slot
			if (msg != null){
				op.setLog(msg);
			}
			op.setOpStatus(ServiceConst.STATE_DONE);
			closeSingleOperation(op);
			if (op.getTicket() != null && op.getOpType().equals(ServiceConst.OP_PUSH)){
				Operation trans = opQueue.getItem(op.getTicket());
				if (trans != null){
					trans.setOpStatus(ServiceConst.STATE_DONE);
					closeSingleOperation(trans);
				}
			}
		}else if (opType == CANCEL){// if canceled close also transactional
			cancelOperation(op,msg);
			if (op.getTicket() != null){
				Operation trans = opQueue.getItem(op.getTicket());
				if (trans != null){
					cancelOperation(trans,msg);
				}
			}
		}
		return op.getId();
	}
	
	
	/**
	 * 	Sync method to access slot
	 *  NB: if transOpId does not specified use opId for access Transaction
	 */

	private synchronized Long accessSlot(Long signature, Long opId, Long transOpId, Integer opType, String msg){
		Slot slot = null;
		
		LOG.info("updateSlot, type:  {}",opType);

		//access slot associated with specific repository
		if (slots.containsKey(signature)){// get existing slot to work with
			slot = slots.get(signature);
		}else{// Create a new empty slot
			slot = new Slot(signature);
			slots.putIfAbsent(signature,slot);
		}
		
		if (opType == ADD){
			if (transOpId != null && opId != transOpId){// update transactional, but omit init case
				slot.addToTransaction(opId, transOpId);
			}else{
				slot.addTransaction(opId);
			}
			updateOpStatus(slot);
			return opId;
		}else if (opType == CLOSE){// remove transactions description from slot
			LOG.info("CLOSE: to remove:{}",opId);
			LOG.info("before:{}",slot.getTransactions());
			if (transOpId != null && opId == null){// close all from transaction transOpId
				Transaction t = slot.getTransaction(transOpId);
				if (t != null){
					t.closeAllOpen();
				}
				slot.removeTransaction(transOpId);
			}else if (transOpId != null && opId != null){// closing isolated operation _inside_ transOpId, do not close transaction
				Transaction t = slot.getTransaction(transOpId);
				if (t != null){
					t.close(opId);
				}
				LOG.info("after closing isolated operation:{}",t.getOperations());
			}else if (opId != null){
				Transaction t = slot.getCurrentTransaction();
				if (t != null){
					t.close(opId);
				}
				slot.removeTransaction(opId);
			}
			LOG.info("after:{}",slot.getTransactions());
			updateOpStatus(slot);
			return opId;
		}else if (opType == CANCEL){//
			if (transOpId != null && opId == null){// cancel all from transaction transOpId, including initial
				Transaction t = slot.getTransaction(transOpId);
				if (t != null){
					cancel(t.closeAllOpen(),msg);
				}
				slot.removeTransaction(transOpId);
			}else if (transOpId != null && opId != null){// closing isolated operation,, do not close transaction
				Transaction t = slot.getTransaction(transOpId);
				if (t != null){
					cancel(t.close(opId),msg);
				}
			}else if (opId != null){
				Transaction t = slot.getCurrentTransaction();
				if (t != null){
					cancel(t.close(opId),msg);
				}
				slot.removeTransaction(opId);
			}
			updateOpStatus(slot);
			return opId;
		}
		return null;
	}

	
	/**
	 * Updates state of current active operation in specified slot
	 * NB: This method must be called after any changes in slot
	 * @param slot
	 */
	public Boolean updateOpStatus(Slot slot){
		if (!slot.isEmpty()){
			LOG.info("updateOpStatus slot, list of transactions:{}",slot.getTransactions());
			Transaction t = slot.getCurrentTransaction();
			if (t != null){
				Long opId = t.getCurrent();
				LOG.info("current op list in transaction:{}",t.getOperations());
				if (opId != null){
					Operation op = getOperation(opId);
					if (op != null){
						LOG.info("update: {}", op.toJson());
						op.setOpStatus(ServiceConst.STATE_INPROGRESS);
						return true;
					}
				}
			}
		}
		return false;
	}
	

	/**
	 * Used by PMD service
	 */
	public Long addSingleOperation(Operation op){
		
		Long id = generateID.getAndIncrement();
		op.setId(id);
		opQueue.addItem(id, op);
		LOG.info("addOperation:{}",  op.toJson());
		return id;
	}
	
	/**
	 * Used by PMD service
	 */
	public Long closeSingleOperation(Operation op){
		
		if (op == null) return null;
		LOG.info("close op:{}",  op.toJson());
		// op not null, safe to work with
		releaseResources(op);
		return op.getId();

	}

	/**
	 * Used by PMD service
	 */
	public Long closeSingle(Long opId){
		LOG.info("close opId={}",  opId);
		return closeSingleOperation(opQueue.getItem(opId));
	}
	
	public Long cancelOperation(Operation op, String msg){
		
		if (op == null) return null;
		LOG.info("cancel op:{}",  op.toJson());
		// op not null, safe to work with
		op.abort(msg);
		releaseResources(op);
		return op.getId();
	}

	private void releaseSingleResources(Operation op){
		cache.drop(CacheService.PAYLOAD, op.getId());
	}

	
	/**
	 *  Returns operation by its id
	 *  NB: can return  null, if operation does not exist in queue
	 *  Additional:
	 *  1) validate by time limits and forcefully cancel operation if time limit exceeds
	 *  2) verify is requested operation  - the next to be executed, and set status Inprogress if true
	 */
	public Operation getSingleOperation(Operation operation){
		if (operation != null){
			if (!isValid(operation)){
				operation.abort("Time out");// only 1 from transaction
				releaseResources(operation);
			}
		}
		return operation;
	}


	/**
	 * Add operation to Operational table
	 * Called when a new operation queued
	 * Initial status  for all added operations: Queued
	 *  
	 * @param op
	 * @return opId
	 */
	public Long addOperation(Operation op){
		
		Long id = generateID.getAndIncrement();
		op.setId(id);
		if (op.getOpType().equals(ServiceConst.OP_TRANSACTION)){
			op.setTicket(id);
			cancelPreviousTransaction(id);
		}
		opQueue.addItem(id, op);
		accessSlot(op,ADD,null);
//		accessSlot(op.getSign(), op.getId(), op.getTicket(), ADD, null);
		
		LOG.info("addOperation:{}",  op.toJson());
		return id;
	}
	
	/**
	 * Closes operation in Operational table
	 * Called when task executor complete its executing
	 * Updates appropriate slot variables:
	 * if closed some op from transactional (init, add, commit), change only their state inside transaction
	 * else call close method
	 * updates ExecStack
	 *  
	 * @param op
	 * @return opId - id of closed operation
	 */
	public Long closeOperation(Operation op){
		
		if (op == null) return null;
		LOG.info("close op:{}",  op.toJson());
		
		if (op.getOpType().equals(ServiceConst.OP_PUSH)){
			accessSlot(op,CLOSE,null);

//			accessSlot(op.getSign(), null, 	  	 op.getTicket(), CLOSE, null);// close all transaction
		}else{// any other operation
			accessSlot(op,CLOSE,null);

//			accessSlot(op.getSign(), op.getId(), op.getTicket(), CLOSE, null);// only 1 from transaction
		}
		return op.getId();

	}
	
	/**
	 * Overloaded method
	 * @param opId
	 * @return
	 */
	public Long close(Long opId){
		LOG.info("close opId={}",  opId);
		return closeOperation(opQueue.getItem(opId));
	}
	
	private void releaseResources(Operation op){
		if (op.getOpType().equals(ServiceConst.OP_TRANSACTION)){
			cache.drop(CacheService.COMMITFUTURES, op.getId());
		}else if (op.getOpType().equals(ServiceConst.OP_SETCREDS)){
			cache.drop(CacheService.GITCREDS, op.getId());
		}else if (op.getOpType().equals(ServiceConst.OP_ADD)){
			cache.drop(CacheService.PAYLOAD, op.getId());
		}else if (op.getOpType().equals(ServiceConst.OP_CLONE)){
			cache.drop(CacheService.GITCREDS, op.getId());
		}
	}

	
	/**
	 *  Returns operation by its id
	 *  NB: can return  null, if operation does not exist in queue
	 *  Additional:
	 *  1) validate by time limits and forcefully cancel operation if time limit exceeds
	 *  2) verify is requested operation  - the next to be executed, and set status Inprogress if true
	 */
	public Operation getOperation(Long id){
		LOG.info("getOperation({})",id);
		Operation operation = opQueue.getItem(id);
		if (operation != null){
			if (ServiceConst.OP_GETPACKAGE.equals(operation.getOpType()) || ServiceConst.OP_GETPACKAGE.equals(operation.getOpType())){
				return getSingleOperation(operation);
			}
			if (!isValid(operation)){
				accessSlot(operation,CANCEL,"Time out");

//				accessSlot(operation.getSign(), operation.getId(), operation.getTicket(), CANCEL, "Time out");// only 1 from transaction
			}
		}
		return operation;
	}

	/**
	 * Returns true, if 
	 * 1) operation from C-group started no more than WAIT_TIME ms ago, OR
	 * 2) any other operation not older than WORK_TIME ms
	 * @param operation
	 * @return
	 */
	public Boolean isValid(Operation operation){
		if (ServiceConst.isActive(operation.getOpStatus())){// testing only alive operation
			if (operation.getOpType().equals(ServiceConst.OP_GETDATA)){
				return Instant.now().toEpochMilli() - operation.getStarted() < WAIT_TIME;
			}else {
				return Instant.now().toEpochMilli() - operation.getStarted() < WORK_TIME;
			}
		}
		return true;
	}

	
	/**
	 * Block of methods to change status of operations - called after slot updating
	 * @param listOpIds
	 * @param msg
	 */
	private  void cancel(List<Long> listOpIds, String msg){
		if (listOpIds == null || listOpIds.size() == 0) return;
		for (Long opId : listOpIds ){
			Operation operation = opQueue.getItem(opId);
			if (operation != null && operation.isAlive()){
				operation.abort(msg);
			}
		}
	}

	private  void cancel(Long opId, String msg){
		if (opId == null) return;
		Operation operation = opQueue.getItem(opId);
		if (operation != null && operation.isAlive()){
			operation.abort(msg);
		}
	}
	
	/**
	 * Cancels operation 
	 * updates ExecStack
	 * @param op
	 * @param msg
	 * @return
	 */
	public Long cancelWithMsg(Operation op, String msg){
		if (op.getOpType().equals(ServiceConst.OP_TRANSACTION)){
			accessSlot(op,CANCEL,msg);

//			accessSlot(op.getSign(), null, op.getTicket(), CANCEL, msg);
		}else{
			accessSlot(op,CANCEL,msg);
//			accessSlot(op.getSign(), op.getId(), op.getTicket(), CANCEL, msg);
		}
		cancel(op.getId(),msg);
		return op.getId();
	}

	/**
	 * Cancels operation - overloaded method
	 * updates ExecStack
	 * @param op
	 * @param msg
	 * @return
	 */
	public Long cancelWithMsg(Long opId, String msg){
		Operation op = opQueue.getItem(opId);
		if (op != null){
			return cancelWithMsg(op, msg);
		}
		return opId;
	}
	
	public void cancelPreviousTransaction(Long opId){
		if (lastTransactionId != null){
			cancelWithMsg(lastTransactionId,"Overridden");
		}
		lastTransactionId = opId;

		/*		if (opQueue.size() > 0){
			for (Operation op: opQueue.getItemList()){
				if (op.getId() != opId && op.getOpType().equals(ServiceConst.OP_TRANSACTION)){
					cancelWithMsg(op.getId(),"Overridden");
				}
			}
		}
*/
	}


	public  void completeWithStatus(Long opId,Boolean status){
		LOG.info("completeWithStatus({})",status);
		Operation op = opQueue.getItem(opId);
		if (op != null){
			LOG.info("call close for: {}",opId);
			closeOperation(op);
			op.completeWithStatus(status);
		}
	}

	public  void completeWithStatus(Long opId,Boolean status, Long gitId){
		LOG.info("completeWithStatus({})",status);
		Operation op = opQueue.getItem(opId);
		if (op != null){
			LOG.info("call close for: {}",opId);
			closeOperation(op);
			op.completeWithStatus(status, gitId);
		}
	}

	public  void completeWithStatus(Long opId, Boolean status, String log){
		LOG.info("completeWithStatus({})",status);
		Operation op = opQueue.getItem(opId);
		if (op != null){
			LOG.info("call close for: {}",opId);
			closeOperation(op);
			op.completeWithStatus(status, log);
		}
	}

	/****************************
	 * 		Utility methods		*
	 ****************************/
	/**
	 *  Returns list of operations from tail
	 *  Verify validity and updates slot if not valid 
	 */
	public List<Operation> getListOperations(Integer size){
		if (size > MAX_SIZE){
			size = MAX_SIZE;
		}
		if (size < 0){
			size = 0;
		}
		List<Operation> lst =  opQueue.getItemList();
		Integer length = lst.size();
		if (size > length){
			size = length;
		}
		List<Operation> subList = lst.subList(length-size, length);
		for (Operation operation : subList){
			if (!isValid(operation)){
				accessSlot(operation,CANCEL,"Time out");
//				accessSlot(operation.getSign(), operation.getId(), operation.getTicket(), CANCEL, "Time out");
			}
		}
		return subList;
	}
	
	public void clearQueue(){
		opQueue.clear();
	}



}
