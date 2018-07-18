package com.flosum.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is a simple utility class, which holds only constant values
 * 
 */

public class ServiceConst {
	
	// transport const
	public static final Integer T_TIMEOUT = 120000;
	
	// git access protocol
	public static final String P_HTTPS = "HTTPS";
	public static final String P_SSH = "SSH";
	
	// git ops statuses
	public static final String STATE_NONE = "NONE";
	public static final String STATE_QUEUED = "QUEUED";
	public static final String STATE_WAITING = "WAITING";
	public static final String STATE_INPROGRESS = "INPROGRESS";
	public static final String STATE_DONE = "DONE";
	public static final String STATE_CANCELED = "CANCELED";
	// git ops types
	public static final String OP_CLONE = "CLONE";
	public static final String OP_PULL = "PULL";
	public static final String OP_TRANSACTION = "TRANSACTION";
	public static final String OP_ADD = "ADD";
	public static final String OP_COMMIT = "COMMIT";
	public static final String OP_PUSH = "PUSH";
	public static final String OP_SETCREDS = "SETCREDENTIALS";
	public static final String OP_GETDATA = "GETDATA";
	public static final String OP_GETMETA = "GETMETA";
	public static final String OP_GETPACKAGE = "GETPACKAGE";
	public static final String OP_GETANALYZEDDATA = "GETANALYZEDDATA";
	
	public static final Long RW_BLOCKING     =  1000000L;
	public static final Long RW_NOT_BLOCKING =  2000000L;
	public static final Long RO_BLOCKING     = 10000000L;
	public static final Long RO_NOT_BLOCKING = 20000000L;
	public static final Long NOT_DEFINED = 0L;
	
	public static final Integer RO_TYPE = 0;
	public static final Integer RW_TYPE = 1;
	
	public static final String DELETED = "DELETED";

	private static final Map<String, Long> OPERATION2CATEGORY = Collections.unmodifiableMap(new HashMap<String, Long>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1378572561989929422L;

		{
			put(OP_CLONE, RW_NOT_BLOCKING);
			put(OP_PULL, RW_NOT_BLOCKING);
			put(OP_TRANSACTION, RW_BLOCKING);
			put(OP_ADD, RW_BLOCKING);
			put(OP_COMMIT, RW_BLOCKING);
			put(OP_PUSH, RW_BLOCKING);
			put(OP_SETCREDS, RW_BLOCKING);
			put(OP_GETDATA, RO_BLOCKING);
			put(OP_GETMETA, RO_BLOCKING);
		}
	});
	private static final Set<String> TRANSACTIONAL = Collections.unmodifiableSet(new HashSet<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6463535784824857735L;

		{
			add(OP_TRANSACTION);
			add(OP_ADD);
			add(OP_COMMIT);
			add(OP_PUSH);
		}
	});
	
	private static final Set<String> RW = Collections.unmodifiableSet(new HashSet<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7680910383512267927L;

		{
			add(OP_TRANSACTION);
			add(OP_ADD);
			add(OP_COMMIT);
			add(OP_PUSH);
		}
	});

	private static final Set<String> RO = Collections.unmodifiableSet(new HashSet<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8462497742682990990L;

		{
			add(OP_GETDATA);
			add(OP_GETMETA);
		}
	});
	
	private static final Set<String> NEXT = Collections.unmodifiableSet(new HashSet<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -538532668855725468L;

		{
			add(STATE_QUEUED);
			add(STATE_WAITING);
		}
	});
	
	private static final Set<String> ACTIVE = Collections.unmodifiableSet(new HashSet<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5088363474870897210L;

		{
			add(STATE_QUEUED);
			add(STATE_WAITING);
			add(STATE_INPROGRESS);
		}
	});

	private static final Set<String> NONACTIVE = Collections.unmodifiableSet(new HashSet<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1959378850065075504L;

		{
			add(STATE_NONE);
			add(STATE_DONE);
			add(STATE_CANCELED);
		}
	});

	// actions for operations
	public static final String ACTION_NON = "noaction";
	public static final String ACTION_CANCEL = "cancel";
	public static final String ACTION_ROLLBACK = "rollback";
	
	// resources type for caching service
	public static final String TYPE_DATA = "DATA";
	public static final String TYPE_CREDS = "CREDENTIALS";
	public static final String TYPE_FUTURES = "FUTURES";
	
	// git clone mode
	public static final Integer CLONE_ACTUAL = 0;
	public static final Integer CLONE_SET_CREDS = 1;
	public static final Integer CLONE_CHECK_CREDS_1 = 2;
	public static final Integer CLONE_CHECK_CREDS_2 = 4;
	
	// info messages
	public static final String I_NEWBRANCH = "Created a new branch ";
	public static final String I_EXISTEDBRANCH = "Checkout an existed branch ";
	public static final String I_SUCCESSCOMMIT = "Wrote the commit ";
	public static final String I_ADDCOMPONENTS = "Number of added components: ";
	public static final String I_DELCOMPONENTS = "Number of deleted components: ";
	public static final String I_CREDSCHANGED = "Changed the credentials to git repository";
	
	// git and other errors
	public static final String E_GITAPI = "GITAPI error during creation of  repository "; 
	public static final String E_IO = "I/O error during creation of  temporary file ";
	public static final String E_GITAPI_GENERAL = "GITAPI error ";
	public static final String E_ENC = "Encoding Error ";
	public static final String E_IO_GENERAL = "I/O error ";
	public static final String E_CONCURRENT = "THREAD EXECUTION ERROR ";
	public static final String E_WRONG_RID = "Wrong repository Id";
	public static final String E_WRONG_PROTOCOL = "Wrong repository Id";
	// genneral erros
	public static final String E_CANCEL_TRANSACTION = "Transaction have been canceled";
	public static final String E_USER_CANCEL = "Canceled by user";
	public static final String E_NULL_VALUE = "Null value: ";
	public static final String E_NO_CACHE = "Cache error";
	public static final String E_OVERLOAD = "Too many active threads";
	
	public static Boolean isRO(String opType){
		return RO.contains(opType);
	}

	public static Boolean isRW(String opType){
		return RW.contains(opType);
	}
	
	public static Boolean isEqualTypes(String opType2, String opType1){
		if (isRO(opType1)){
			 return isRO(opType2);
		}else if (isRW(opType1)){
			 return isRW(opType2);
		}
		return false;
	}
	
	public static Boolean isNextToExec(String opStatus){
		return NEXT.contains(opStatus);
	}

	public static Boolean isTransactional(String opType){
		return TRANSACTIONAL.contains(opType);
	}
	
	public static Long getSignature(String opType){
		if (OPERATION2CATEGORY.containsKey(opType)){
			return OPERATION2CATEGORY.get(opType);
		}
		return NOT_DEFINED;
	}
	
	public static Boolean isActive(String opStatus){
		return ACTIVE.contains(opStatus);
	}


}
