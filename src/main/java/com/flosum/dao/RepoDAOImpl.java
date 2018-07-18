package com.flosum.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import org.eclipse.jgit.api.errors.*;
import com.flosum.model.*;
import com.flosum.utils.StringUtils;
import com.flosum.utils.ZipUtils;
import com.flosum.xml.XmlPreprocessor;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.DeployOptions;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.TestLevel;
import com.sforce.ws.ConnectionException;
import com.flosum.constants.*;
import com.flosum.dao.ScheduledTasks.UpdateDescriptor;

/**
 *	The main DAO class to work with remote git repository 
 * @author Alexey Kalutov
 * @since  0.1.0
 */
@Service
public class RepoDAOImpl implements RepoDAO {


	private final static  Logger LOG = LoggerFactory.getLogger(RepoDAOImpl.class);
	
	private final static  CacheService cache = new CacheService();
	private final static  ScheduledTasks scheduledTasks = new ScheduledTasks();
	
	private final static  DataManager  dataManager = new DataManager(cache);
	private final static  PackageManager packages = new PackageManager();
	private final static  OperationalFabric operationFabric = new OperationalFabric(cache);
	

	private final static  GitFactory GFactory = new GitFactory(operationFabric, scheduledTasks);
	private final static  TExecutor executor = new TExecutor(operationFabric, GFactory, cache);
	
	private final static Integer MAX_LOAD = 5;
	private volatile Integer load;

	public RepoDAOImpl(){
		load = 0;
	}

	// NB: non-blocking operation - returns last op status
	// if operation has green light to exec, then execute it
	
	@Override
	public Operation getOpStatus(Long opId) {
		Operation op = operationFabric.getOperation(opId);
		if (op != null){
		LOG.info("getOpId={}",op.getId());
		LOG.info("getOpStatus for opId={}",op.getOpStatus());
		LOG.info("getOpType for opId={}",op.getOpType());

/*
			if (inProgress(op)){
				try {
					executor.execute(opId);
				} catch (IOException | GitAPIException e) {
					operationFabric.cancelWithMsg(op, e.getMessage());
					return op;
				}
			}
*/
		}
		return op;
	}
	
	

	// NB: non-blocking operation - returns list  of performed operations of size =  size
	@Override
	public List<Operation> getOpList(Integer size) {
		return  operationFabric.getListOperations(size);
	}

	// NB: non-blocking operation - set last op status

	@Override
	public Operation resetOpStatus(Long opId) {
		Operation op = operationFabric.getOperation(opId);
		operationFabric.cancelWithMsg(op, ServiceConst.E_USER_CANCEL);
		return op;
	}
	
	/**
	 * Non-blocking, because executed only on error case
	 * 
	 */
	@Override
	public Boolean rollBackRepoChanges(Long gitId){
		GitHandlerRW git = GFactory.getGitRWHandler(gitId);
		if (git != null){
			git.rollBack();
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 	Verify the real existence of repository on the hdd, 
	 * 	Returns descriptor
	 * 	If does not exist, return null 
	 */
	@Override
	public RepoDescrDigest getDescription(Long gitId){
		GitHandler git = GFactory.getGitHandler(gitId);
		if (git == null) return null;
		if (git.isExist()){
			return DataConvertor.repoDescr2Digest(git.getRepo());
		}
		return null;
	}	

	/**
	 * Sync
	 * Set creds for repository
	 * return null if remote git creds invalid or id of operation
	 * @throws GitAPIException 
	 * @throws IOException 
	 */
	@Override
	public Long setCredentials(GitCredentials gc, Long repoId)  {
		Long opId = null;
		try{
			if (isRemoteValid(gc,ServiceConst.CLONE_CHECK_CREDS_1) ){
				opId = operationFabric.addOperation(new Operation(ServiceConst.OP_SETCREDS, ServiceConst.STATE_QUEUED, null, repoId));
				cache.push(gc, CacheService.GITCREDS, opId);
				Operation op = operationFabric.getOperation(opId);
				if (op != null && inProgress(op)){
					executor.execute(opId);
				}
			}
			return opId;
		}catch (Exception e) {
			LOG.info(ServiceConst.E_GITAPI);
			operationFabric.cancelWithMsg(opId, ServiceConst.E_GITAPI + e.getMessage());
			return opId;
		}
	}
	
	/**
	 *  Query the permission for access the repository in order to get data
	 *  NOTE: Cancel | Close command executed when data actually got
	 */
	@Override
	public Long getData(Long rid) {
		Long opId = operationFabric.addOperation(new Operation(ServiceConst.OP_GETDATA, ServiceConst.STATE_QUEUED, null, rid));
		Operation op = operationFabric.getOperation(opId);
		if (op != null && inProgress(op)){
			try {
				executor.execute(opId);
			} catch (Exception e) {
				operationFabric.cancelWithMsg(opId, ServiceConst.E_IO + e.getMessage());
				return opId;
			}
		}
		return opId;
	}

	@Override
	public DataWrapper getContinueData(Long opId, Integer part) {
		Operation op = operationFabric.getOperation(opId);
		if (op != null && !op.getOpStatus().equals(ServiceConst.ACTION_CANCEL)){
			try {
				return dataManager.getCachedData(opId, part);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 	Blocking operation 
	 * 	Serialize list of branches from branch defined in GitPath, put it to cache under name 
	 *  Returns DataWrapper with 1st block of data from cache
	 *  Executed only for operations with state = inProgress  
	 */

	@Override
	public DataWrapper getBranches(GitPath path, Long opId) {
		Operation op = null;
		List<BranchDigest> branchesList = null;
		try {
			// get pointer to operation written in descriptor
				GitHandlerRO gitClientRO = GFactory.getGitROHandler(path.getRepositoryId());
				if (gitClientRO  != null){
					branchesList = DataConvertor.Branch2Digest(gitClientRO.getBranches());
				}else{
				}
				return dataManager.split(branchesList, opId);
		} catch (GitAPIException e) {
			LOG.info("GITAPI error in getBranches "+ e.getMessage());
			operationFabric.cancelWithMsg(op,e.getMessage());
			return null;
		} catch (Exception e) {
			LOG.info("I/O error getBranches "+e.getMessage());
			operationFabric.cancelWithMsg(op,e.getMessage());
			return null;
		}
	}
	
	/**
	 * 	Returns list of commits from branch sha
	 *  if sha wrong, returns null 
	 *  Executed only for operations inProgress  
	 */
	@Override
	public DataWrapper getCommits(GitPath path, Long opId, Boolean single) {
		Operation op = null;
		List<CommitDigest> lst = null;
		try {
			op = operationFabric.getOperation(opId);
			if (inProgress(op)){
				GitHandlerRO gitClientRO = GFactory.getGitROHandler(path.getRepositoryId());
				if (gitClientRO  != null){
					lst =  single ?
							DataConvertor.Commit2Digest( gitClientRO.getCommitFromBranch(path.getBranchId(), path.getCommitId()) )
							:DataConvertor.Commit2Digest( gitClientRO.getCommitFromBranch(path.getBranchId()) );
					operationFabric.completeWithStatus(opId, true);
				}else{
					operationFabric.completeWithStatus(opId, false, ServiceConst.E_WRONG_RID);
				}
				return dataManager.split(lst, opId, false);
			}else{// operation still with queue status OR canceled, in both cases
				return null;
			}
		} catch (GitAPIException e) {
			LOG.info("GITAPI error in getCommits "+ e.getMessage());
			operationFabric.cancelWithMsg(op,e.getMessage());
			return null;
		} catch (Exception e) {
			LOG.info("I/O error getCommits "+ e.getMessage());
			operationFabric.cancelWithMsg(op,e.getMessage());
			return null;
		}
	}

	/**
	 * 	Returns list of components from branch sha 
	 *  Executed only for operations inProgress  
	 */
	@Override
	public DataWrapper getComponents(GitPath path, Long opId) {
		Operation op = null;
		List<ComponentDigest> lst = null;
		try {
			op = operationFabric.getOperation(opId);
			if (inProgress(op)){
				GitHandlerRO gitClientRO = GFactory.getGitROHandler(path.getRepositoryId());
				if (gitClientRO  != null){
					// update commit cache before call
					Branch br = gitClientRO.getBranchbyId(path.getBranchId());
					LOG.info("commit: {}"+path.getCommitId());
					lst = DataConvertor.Component2Digest( gitClientRO.getComponentsFromCommit(path.getCommitId()) );
				}else{
					operationFabric.completeWithStatus(opId, false, ServiceConst.E_WRONG_RID);
				}
				operationFabric.completeWithStatus(opId, true);
				return dataManager.split(lst, opId, 0);
			}else{
				return null;
			}
		} catch (GitAPIException e) {
			LOG.info("GITAPI error in getComponents "+ e.getMessage());
			operationFabric.cancelWithMsg(op,e.getMessage());
			return null;
		} catch (Exception e) {
			LOG.info("I/O error getComponents "+ e.getMessage());
			operationFabric.cancelWithMsg(op,e.getMessage());
			return null;
		}
	}
	
	/**
	 * Sync
	 * Returns from repository the payload with list of MetaItem components by names defined in package (set in Payload
	 * object - list MetaItem) 
	 * @return The same Payload with list populated with base64 data
	 */
	@Override
	public Payload  getCompPack(Payload p, Long opId) {
		Operation op = null;
		try {
			op = operationFabric.getOperation(opId);
			if (inProgress(op)){
				GitHandlerRO gitClientRO = GFactory.getGitROHandler(p.getRepositoryId());
				if (gitClientRO  != null){
					String result = gitClientRO.downloadZipPack(p);
					operationFabric.completeWithStatus(opId, true, result);
				}else{
					operationFabric.completeWithStatus(opId, false, ServiceConst.E_WRONG_RID);
				}
				return p;
			}else{
				return null;
			}
		} catch (UnsupportedEncodingException e) {
			LOG.info(ServiceConst.E_ENC + e.getMessage());
			operationFabric.cancelWithMsg(opId,e.getMessage());
			return null;
		} catch (GitAPIException e) {
			LOG.info(ServiceConst.E_GITAPI_GENERAL + e.getMessage());
			operationFabric.cancelWithMsg(opId,e.getMessage());
			return null;
		} catch (Exception e) {
			LOG.info(ServiceConst.E_IO_GENERAL + e.getMessage());
			operationFabric.cancelWithMsg(opId,e.getMessage());
			return null;
		}
	}
	
	/**
	 * Sync
	 * Returns from repository the payload with list of MetaItem components by names defined in package (set in Payload
	 * object - list MetaItem), initial sha must be null 
	 * @return The same Payload with list populated with new sha values
	 */
	@Override
	public Payload  getCompPackSha(Payload p, Long opId) {
		Operation op = null;
		try {
			op = operationFabric.getOperation(opId);
			if (inProgress(op)){
				GitHandlerRW gitClientRW = GFactory.getGitRWHandler(p.getRepositoryId());
				if (gitClientRW  != null){
					Map<String,MetaItem> compMap = new HashMap<String,MetaItem>();
					for (MetaItem mi : p.getItems()){
						compMap.put(StringUtils.compositeName(p.getCompType(), mi.getName()), mi);
					}
					gitClientRW.getComponentsSha(gitClientRW.getCommit(p.getCommitId()), compMap);;
					operationFabric.completeWithStatus(opId, true);
				}else{
					operationFabric.completeWithStatus(opId, false, ServiceConst.E_WRONG_RID);
				}
				return p;
			}else{
				return null;
			}
		} catch (UnsupportedEncodingException e) {
			LOG.info(ServiceConst.E_ENC + e.getMessage());
			operationFabric.cancelWithMsg(opId,e.getMessage());
			return null;
		} catch (GitAPIException e) {
			LOG.info(ServiceConst.E_GITAPI_GENERAL + e.getMessage());
			operationFabric.cancelWithMsg(opId,e.getMessage());
			return null;
		} catch (Exception e) {
			LOG.info(ServiceConst.E_IO_GENERAL + e.getMessage());
			operationFabric.cancelWithMsg(opId,e.getMessage());
			return null;
		}
	}

	
	/**
	 *    Async operations
	 * @throws GitAPIException 
	 * @throws IOException 
	 */
	
	@Async
	@Override
	public void push(Long opId) {
		Operation op = operationFabric.getOperation(opId);
		if (op != null && inProgress(op)){
			try {
				executor.execute(opId);
			} catch (GitAPIException e) {
				LOG.info(ServiceConst.E_GITAPI); 
				operationFabric.cancelWithMsg(opId, ServiceConst.E_GITAPI+ e.getMessage());
			} catch (Exception e) {
				LOG.info("I/O error in push command");
				operationFabric.cancelWithMsg(opId, ServiceConst.E_IO+ e.getMessage());
			}
		}
	}
	
	
	/**
	 * Clone (with authentication), async operation
	 */
	@Async
	@Override
	public void cloneWithAuth(GitCredentials gc, Long opId) {
		LOG.info("clone: {}" + gc.toString());
		try {
			cache.push(gc, CacheService.GITCREDS, opId);
			executor.execute(opId);
			LOG.info("main thread");
		} catch (GitAPIException e) {
			LOG.info(ServiceConst.E_GITAPI);
			e.printStackTrace();
			operationFabric.cancelWithMsg(opId, ServiceConst.E_GITAPI + e.getMessage());
		} catch (IOException e) {
			LOG.info(ServiceConst.E_IO);
			operationFabric.cancelWithMsg(opId, ServiceConst.E_IO + e.getMessage());
		} catch (Exception e) {
			operationFabric.cancelWithMsg(opId, ServiceConst.E_IO_GENERAL + e.getMessage());
		}
	}

	/**
	 * Sync
	 * Initialize uploading data  by setting futures for commit,
	 * which is including the number of zip archives and commiter's name
	 * @return operation id
	 */
	@Override
	public Long initUpload(CommitFutures cf) {
		Long opId = null;
		try {
			opId = operationFabric.addOperation(new Operation(ServiceConst.OP_TRANSACTION, ServiceConst.STATE_QUEUED, null, cf.getRepoId()));
			GitHandler gh = GFactory.getGitHandler(cf.getRepoId());
			if (gh != null){
				if (gh.getType().equals(ServiceConst.RW_TYPE)){
					cache.push(cf,	CacheService.COMMITFUTURES,	opId);
					Operation op = operationFabric.getOperation(opId);
					if (op != null && inProgress(op)){
						executor.execute(opId);
					}
				}
			}else{
				operationFabric.cancelWithMsg(opId, ServiceConst.E_WRONG_RID);
			}
			return opId;
		} catch (GitAPIException e) {
			LOG.info("GITAPI error in setCommit command");
			operationFabric.cancelWithMsg(opId, ServiceConst.E_GITAPI_GENERAL+ e.getMessage());
			return opId;
		} catch (Exception e) {
			LOG.info("I/O error in setCommit command");
			operationFabric.cancelWithMsg(opId, ServiceConst.E_IO_GENERAL+ e.getMessage());
			return opId;
		}
	}

	
	/**
	 * Sync
	 * @return operation id
	 */
	@Async
	@Override
	public void commitAll(Long opId) {
		Operation op = operationFabric.getOperation(opId);
		if (op != null && inProgress(op)){
			try {
				executor.execute(opId);
			} catch (GitAPIException e) {
				LOG.info("GITAPI error in commitAll command");
				operationFabric.cancelWithMsg(opId, ServiceConst.E_GITAPI_GENERAL+ e.getMessage());
			} catch (Exception e) {
				LOG.info("I/O error in commitAll command");
				operationFabric.cancelWithMsg(opId, ServiceConst.E_IO_GENERAL+ e.getMessage());
			}
		}
	}


	/**
	 * Sync
	 * Adds to repository the components from list of zip packages (set in Payload: items) 
	 * Data must be encoded in Base64 format
	 * if somewhat errors - cancel operation + roll back changes
	 * @return operation id
	 * if fail to perform operation (not started || not in progress) - returns null 
	 */
	@Async
	@Override
	public void addCompPack(Payload p, Long opId, Long repoId) {
		try {
			cache.push(p,	CacheService.PAYLOAD,	opId);
			Operation op = operationFabric.getOperation(opId);
			if (op != null && inProgress(op)){
				executor.execute(opId);
				GFactory.getGitRWHandler(repoId).addId(opId);
			}
			//return opId;
		} catch (UnsupportedEncodingException e) {
			LOG.info(ServiceConst.E_ENC);
			operationFabric.cancelWithMsg(opId,  ServiceConst.E_ENC+ e.getMessage());
			GFactory.getGitRWHandler(repoId).rollBack();
			//return opId;
		} catch (GitAPIException e) {
			LOG.info(ServiceConst.E_GITAPI_GENERAL);
			operationFabric.cancelWithMsg(opId,  ServiceConst.E_GITAPI_GENERAL+ e.getMessage());
			GFactory.getGitRWHandler(repoId).rollBack();
			//return opId;
		} catch (Exception e) {
			LOG.info(ServiceConst.E_IO_GENERAL);
			operationFabric.cancelWithMsg(opId, ServiceConst.E_IO_GENERAL+ e.getMessage());
			GFactory.getGitRWHandler(repoId).rollBack();
			//return opId;
		}
		
	}
	
	/**
	 *  Query the permission for review
	 *  If success, return a number > 0
	 */
	@Override
	public Long initUpload() {
		LOG.info("initUpload, load = " + load);
//		if (load < MAX_LOAD){
			incLoad();
			Long opId = operationFabric.addOperation(new Operation(ServiceConst.OP_GETANALYZEDDATA, ServiceConst.STATE_QUEUED));
			Operation op = operationFabric.getOperation(opId);
			LOG.info("initUpload"+opId);
			return opId;
//		}
//		return -1L;
	}

	/**
	 *    Async operations
	 * @throws GitAPIException 
	 * @throws IOException 
	 */
	
	@Async
	@Override
	public void uploadData(Payload p,Long opId) {
		LOG.info("uploadData opId = " + opId);
		Operation op = operationFabric.getOperation(opId);
		if (op != null && op.isAlive() && p != null){
			try {
				LOG.info("init processor for type = " + p.getCompType());
				DataProcessor dataProcessor = new DataProcessor(p.getCompType(),opId);
				op.setOpStatus(ServiceConst.STATE_INPROGRESS);
				Integer issues = dataProcessor.processComponents(p);
				LOG.info("result = {}",issues);
				if (issues < 0){
					op.completeWithStatus(false, opId);
				}else{
					op.completeWithStatus(true, opId);
					cache.push(dataProcessor.getTempDirectory(), opId);
				}
				LOG.info("main thread finished");
			} catch (Exception e) {
				op.completeWithStatus(false, e.getMessage());
			} finally{
				decLoad();// always decrease load counter
			}
		}
	}
	
	
	/**
	 * Sync operation
	 */
	@Override
	public PayloadA getResults( Long opId) {
		LOG.info("getResults for opId = "+opId);
		Operation op = operationFabric.getOperation(opId);
		if (op != null && !op.isAlive()){// get result must work only for completed operations
			try {
				LOG.info("call cache for opId = "+opId);
				String output_file = "data" + (CacheService.RESULTS +  opId) + ".cache";
				File f = new File (output_file);
				String base64 = ZipUtils.createPlainPack(f,"results");
				if (base64 != null){
					String regex = cache.pop(opId);
					if (regex != null){
						base64 = base64.replace(regex, "");
					}
//					LOG.info("create the payload with {}",base64);
					PayloadA p = new PayloadA (opId, base64);
					Long issues = 0L + countLines(base64);
					p.setRepositoryId(issues);
					return p;
				}
			} catch (Exception e) {
				e.printStackTrace();// remove this line in production
				return null;
			}
		}
		return null;
	}
	
	/**
	 *  Query the permission for review
	 *  If success, return a number > 0
	 */
	@Override
	public Long initPackageUpload() {
		LOG.info("initUpload, load = " + load);
//		if (load < MAX_LOAD){
			incLoad();
			Long opId = operationFabric.addOperation(new Operation(ServiceConst.OP_GETPACKAGE, ServiceConst.STATE_QUEUED));
			Operation op = operationFabric.getOperation(opId);
			LOG.info("initUpload"+opId);
			File tmp_dir = PackageProcessor.setDirectory();
			packages.addPackage(opId, tmp_dir);
			return opId;
//		}
//		return -1L;
	}

	/**
	 *    Async operations
	 * @throws GitAPIException 
	 * @throws IOException 
	 */
	
	@Async
	@Override
	public void uploadPackage(PayloadD p,Long opId) {
		LOG.info("uploadData opId = " + opId);
		Operation op = operationFabric.getOperation(opId);
		if (op != null && op.isAlive() && p != null && packages.isExist(opId)){
			try {
				PackageProcessor dataProcessor = new PackageProcessor(opId);
				PackageDescriptor packageDescr = packages.getPackageDescriptor(opId);
				op.setOpStatus(ServiceConst.STATE_INPROGRESS);
				List<File> success = dataProcessor.processComponents(p,packageDescr.getLocalDir());
				File packFile = dataProcessor.getPackageFile();
				packageDescr.addFiles(success);
				if (packFile != null){
					packages.parsePackage(opId, packFile);
					String test = packages.composePackageManifest(opId);
					LOG.info("pack:test={}", test);
				}
				LOG.info("main thread finished");
			} catch (Exception e) {
				LOG.info(e.getMessage());;
				op.completeWithStatus(false, e.getMessage());
			} finally{
				decLoad();// always decrease load counter
			}
		}
		return;
	}
	
	
	/**
	 * ASync operation
	 */
	@Async
	@Override
	public void deploy( Long opId, AuthDetails auth, DeploymentOptions opt) {
		LOG.info("getResults for opId = "+opId);
		Operation op = operationFabric.getOperation(opId);
		if (op != null && op.isAlive()){// get result must work only for completed operations
			LOG.info("init the deployment process for opid={}", opId);
			// update field in Operation with result, close operation
			try {
				MetadataConnection conn = MetadataLoginUtil.login(auth);
//				LOG.info("config:url:{}",conn.getConfig().getServiceEndpoint());
				byte zipBytes[] = packages.composePackage(opId);
				DeployOptions deployOptions = new DeployOptions();
				if (opt == null){
					opt = new DeploymentOptions(TestLevel.NoTestRun.toString());
				}
				opt.setDeploymentOptions(deployOptions);
				if (zipBytes != null){
					LOG.info("size of zip : {} bytes", zipBytes.length);
				}
//				LOG.info("base64 : {} bytes", Base64.getEncoder().encodeToString(zipBytes));
//				LOG.info("init the deployment ops={}", deployOptions);
				AsyncResult asyncResult = conn.deploy(zipBytes, deployOptions);
				if (asyncResult != null){
					LOG.info("asyncResult={}", asyncResult.getId());
					LOG.info("msg={}", asyncResult.getMessage());
					op.setAsyncId(asyncResult.getId());
					op.completeWithStatus(true);
				}else{
					op.completeWithStatus(false);
				}
			} catch (ConnectionException e) {
				e.printStackTrace();
				op.completeWithStatus(false, e.getMessage());
			} catch ( Exception e){
				e.printStackTrace();
				op.completeWithStatus(false, e.getMessage());
			} finally {
				// remove the package from memory
				packages.removePackage(opId);
			}
		}
	}





	/**
	 * Non-blocking operation:
	 * Verify (with authentication), the existence of remote repository
	 */

	@Override
	public Boolean isRemoteValid(GitCredentials gc, Integer phase) {
		return (GFactory.isPathValid(gc, phase, true) || GFactory.isPathValid(gc, phase, false)); 
	}

	@Override
	public SystemInfo getServerInfo() {
		return new SystemInfo(getNumberActiveThreads(), GFactory.getNumberofROHandlers(), GFactory.getNumberofRWHandlers());
	}
	
	@Override
	public List<UpdateDescriptor> getGitInfo() {
		return scheduledTasks.getCache();
	}


	/**
	 * 		Utility methods
	 * 
	 */
	public static OperationalFabric getOperationFabric(){
		return RepoDAOImpl.operationFabric;
	}

	public static GitFactory getGFactory(){
		return RepoDAOImpl.GFactory;
	}
	
	public Boolean inProgress(Operation op){
		if (op != null){
			return (op.getOpStatus().equals(ServiceConst.STATE_INPROGRESS));
		}
		return false;
	}

	public Boolean doneWithSuccess(Operation op){
		if (op != null){
			return (op.getOpStatus().equals(ServiceConst.STATE_DONE) && op.getIsSuccess());
		}
		return false;
	}

	public Integer getNumberActiveThreads(){
		return Thread.activeCount();
	}

	
	public synchronized void incLoad(){
		load ++;
	}

	public synchronized void decLoad(){
		load --;
		if (load < 0) load = 0;
	}
	
	public static int countLines(String input) throws IOException {
		if (input == null || input == "") return 0;
	    LineNumberReader lineNumberReader = new LineNumberReader(new StringReader(input));
	    lineNumberReader.skip(Long.MAX_VALUE);
	    return lineNumberReader.getLineNumber();
	}



	// end of class
}
