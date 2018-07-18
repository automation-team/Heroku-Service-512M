package com.flosum.controller;

import com.flosum.constants.ServiceConst;
import com.flosum.dao.ScheduledTasks.UpdateDescriptor;
import com.flosum.model.*;
import com.flosum.service.RepoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class RepoController {

	private final Logger LOG = LoggerFactory.getLogger(RepoController.class);
	@Autowired
	private RepoService repoService;
	
	/*****************************		 A. Info operations (All non-blocking)      *******************************/

	@RequestMapping(value = "/operation", method = RequestMethod.GET)
	public ResponseEntity<Operation> getStatus(@RequestParam(value = "opid", defaultValue = "-1") Long opid) {
		LOG.info("Get status");
		if (opid == -1L) opid = null;
		return operationDetails(opid);
	}
	
	/**
	 * Returns subset of the records from  opTable
	 * @param size	- length of list from  the tail
	 * @param filter - filter value, all if  = -1
	 * @param pack -  unpacked, if  = 0 
	 * @param log - with log if  = 1
	 * @return
	 */
 	@RequestMapping(value = "/history", method = RequestMethod.GET)
	public ResponseEntity<List<Operation>> getOperationalHistory(@RequestParam(value = "size", defaultValue = "1") Integer size,
																 @RequestParam(value = "filter", defaultValue = "-1") Long filter,
																 @RequestParam(value = "pack", defaultValue = "0") Integer pack,
																 @RequestParam(value = "log", defaultValue = "1") Integer log) {
 		if (filter == -1L){
 			return new ResponseEntity<List<Operation>>(repoService.getOpList(size), HttpStatus.OK);
 		}else if (filter != null){
 			return new ResponseEntity<List<Operation>>(repoService.getActiveOp(filter), HttpStatus.OK);
 		}
 		return new ResponseEntity<List<Operation>>(HttpStatus.NO_CONTENT);
	}
 	
	/**
	 * 
	 * Returns descriptor for  repository, if a such exists
	 * 
	 * @param repoId == unique Id for repository if not specified, set to -1 which means current (usually last set) repository
	 *
	 */
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ResponseEntity<List<UpdateDescriptor>> getInfo() {
		
		List<UpdateDescriptor> response  = repoService.getGitInfo();

		if (response == null) {
			return new ResponseEntity<List<UpdateDescriptor>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<UpdateDescriptor>>(response, HttpStatus.OK);
	}

 	
	/**
	 * 
	 * Returns descriptor for  repository, if a such exists
	 * 
	 * @param repoId == unique Id for repository if not specified, set to -1 which means current (usually last set) repository
	 *
	 */
	@RequestMapping(value = "/repo", method = RequestMethod.GET)
	public ResponseEntity<RepoDescrDigest> getInfo(@RequestParam(value = "rid", defaultValue = "-1") Long repoId) {
		
		RepoDescrDigest repoDescr  = repoService.getDescription(repoId);

		if (repoDescr == null) {
			return new ResponseEntity<RepoDescrDigest>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<RepoDescrDigest>(repoDescr, HttpStatus.OK);

	}
	
	/*****************************		  B. Establish operations (All non-blocking)        ******************************/

	
	@RequestMapping(value = "/repo/sync", method = RequestMethod.POST)
	public ResponseEntity<Operation> clone(@RequestParam(value = "mode", defaultValue = "0") Integer mode,
											@RequestBody GitCredentials gc) {

		if (gc == null) {
			return new ResponseEntity<Operation>(HttpStatus.NO_CONTENT);
		}

		if (mode == ServiceConst.CLONE_CHECK_CREDS_1 || mode == ServiceConst.CLONE_CHECK_CREDS_2) {
			LOG.info("verify/check git repo creds");
			if (repoService.isRemoteValid(gc, mode)) {
				return new ResponseEntity<Operation>(new Operation(), HttpStatus.OK);
			}
			return new ResponseEntity<Operation>(HttpStatus.NOT_FOUND);
		}


		if (mode == ServiceConst.CLONE_ACTUAL) {
			LOG.info("clone a git repo with auth");

			return operationDetails(repoService.cloneWithAuth(gc));
		}

		return new ResponseEntity<Operation>(new Operation(), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/repo", method = RequestMethod.POST)
	public ResponseEntity<RepoDescrDigest> getInfo(@RequestParam(value = "rid", defaultValue = "-1") Long repoId,
												   @RequestParam(value = "action", defaultValue = ServiceConst.ACTION_NON) String action) {
		
		RepoDescrDigest repoDescr = null;
		
		if (repoId == -1L) return new ResponseEntity<RepoDescrDigest>(HttpStatus.BAD_REQUEST);
		if (action.equals(ServiceConst.ACTION_ROLLBACK)){
			repoService.resetChanges(repoId);
			repoDescr = repoService.getDescription(repoId);
		} else if (action.equals(ServiceConst.ACTION_NON)){
			repoDescr  = repoService.getDescription(repoId);
		}

		if (repoDescr == null) {
			return new ResponseEntity<RepoDescrDigest>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<RepoDescrDigest>(repoDescr, HttpStatus.OK);

	}


	@RequestMapping(value = "/operation", method = RequestMethod.POST)
	public ResponseEntity<Operation> setStatus(@RequestParam(value = "opid", defaultValue = "1") Long opid,
												@RequestParam(value = "action", defaultValue = ServiceConst.ACTION_NON) String action) {
		LOG.info("Get status");
		if (action.equals(ServiceConst.ACTION_NON)){
			return new ResponseEntity<Operation>(repoService.getOpStatus(opid), HttpStatus.OK);
		}else if (action.equals(ServiceConst.ACTION_CANCEL)){
			return new ResponseEntity<Operation>(repoService.resetOpStatus(opid), HttpStatus.OK);
		}
		return new ResponseEntity<Operation>(HttpStatus.BAD_REQUEST);
	}



	/*********************************	            	 C. Get data                ********************************/
	
	@RequestMapping(value = "/data", method = RequestMethod.GET)
	public ResponseEntity<Operation> data(@RequestParam(value = "rid", defaultValue = "-1L") Long rid) {
		LOG.info("query data access");
		if (rid == -1L) {
			return new ResponseEntity<Operation>(HttpStatus.NO_CONTENT);
		}
		return operationDetails(repoService.getData(rid));	
	}

	
	@RequestMapping(value = "/branches", method = RequestMethod.POST)
	public ResponseEntity<DataWrapper> getBranches(@RequestParam(value = "opid", defaultValue = "-1") Long opid,
												   @RequestParam(value = "part", defaultValue = "0") Integer part,
												   @RequestBody GitPath path) {
		if (opid == -1L) return new ResponseEntity<DataWrapper>(HttpStatus.BAD_REQUEST);

		DataWrapper dw = (part == 0) ? repoService.getBranches(path, opid) : repoService.getContinueData(opid, part);

		if (dw == null) {
			return new ResponseEntity<DataWrapper>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<DataWrapper>(dw, HttpStatus.OK);

	}


	/**
	 * Returns list of commits for specified branch
	 */
	@RequestMapping(value = "/commits", method = RequestMethod.POST)
	public ResponseEntity<DataWrapper> getRefs(@RequestParam(value = "opid", defaultValue = "-1") Long opid,
											   @RequestParam(value = "single", defaultValue = "false") Boolean single,
			 								   @RequestParam(value = "part", defaultValue = "0") Integer part,
										       @RequestBody GitPath path) {
		if (opid == -1L) return new ResponseEntity<DataWrapper>(HttpStatus.BAD_REQUEST);
		DataWrapper p = null;
		if (single){
			p = repoService.getCommits(path, opid, true);
		}else{
			p = (part == 0) ? repoService.getCommits(path, opid, false) : repoService.getContinueData(opid, part);
		}

		if (p == null) {
			return new ResponseEntity<DataWrapper>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<DataWrapper>(p, HttpStatus.OK);

	}

	/**
	 * Returns list of components from requested commit
	 */
	@RequestMapping(value = "/components", method = RequestMethod.POST)
	public ResponseEntity<DataWrapper> getComponents(@RequestParam(value = "opid", defaultValue = "-1") Long opid,
													 @RequestParam(value = "part", defaultValue = "0") Integer part,
													 @RequestBody GitPath path) {
		LOG.info("Getting the metainfo about components from  repository");
		if (opid == -1L) return new ResponseEntity<DataWrapper>(HttpStatus.BAD_REQUEST);
	
		DataWrapper p  = (part == 0) ? repoService.getComponents(path, opid) : repoService.getContinueData(opid, part);

		if (p == null) {
			return new ResponseEntity<DataWrapper>(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<DataWrapper>(p, HttpStatus.OK);

	}

	/**
	 *  Returns payload with filled: dataStr fields, or sha fields
	 *  meta = 0: 	dataStr fields
	 *  meta = 1: 	sha fields
	 *  meta = 2: 	sha fields with pre-cleaning
	 */
	
	@RequestMapping(value = "/components/package", method = RequestMethod.POST)
	public ResponseEntity<Payload> getComponents(@RequestParam(value = "opid", defaultValue = "-1") Long opid,
												 @RequestParam(value = "meta", defaultValue = "0") Integer meta,
												 @RequestBody Payload p) {
		LOG.info("Getting the zip pack with components from  repository");
		Payload pReturned = null;
		if (opid == -1L) return new ResponseEntity<Payload>(HttpStatus.BAD_REQUEST);
		if (meta == 0){
			pReturned = repoService.getCompPack(p, opid);
		}else if (meta == 1){
			pReturned = repoService.getCompPackSha(p, opid);
		}else if (meta == 2){
			if (p.size() > 0){
				for (MetaItem mi : p.getItems()){
					mi.setSha("");
				}
			}
			pReturned = repoService.getCompPackSha(p, opid);
		}

		if (pReturned == null) {
			return new ResponseEntity<Payload>(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<Payload>(pReturned, HttpStatus.OK);
	}

	
	/***************************	          	D. Transactional operations         ***************************/
	
	@RequestMapping(value = "/repo/init", method = RequestMethod.POST)
	public ResponseEntity<Operation> setcommit(@RequestBody CommitFutures cf,
												@RequestParam(value = "rid", defaultValue = "-1") Long rid) {
		LOG.info("init commit operation all in git repo");
		
		if (cf != null){
			return operationDetails(repoService.setCommit(cf));
		}
		return new ResponseEntity<Operation>(HttpStatus.BAD_REQUEST);
	}
	
	@RequestMapping(value = "/repo/add", method = RequestMethod.POST)
	public ResponseEntity<Operation> addComponents(@RequestParam(value = "ticket", defaultValue = "-1") Long ticket,
													@RequestBody Payload p) {
		LOG.info("add the zip pack with components to repository");
		return operationDetails(repoService.addCompPack(p,ticket));

	}


	@RequestMapping(value = "/repo/commitall", method = RequestMethod.POST)
	public ResponseEntity<Operation> commitall(@RequestParam(value = "ticket", defaultValue = "-1") Long ticket) {
		LOG.info("commit all in git repo");
		return operationDetails(repoService.commitAll(ticket));
	}

	@RequestMapping(value = "/repo/push", method = RequestMethod.POST)
	public ResponseEntity<Operation> push(@RequestParam(value = "ticket", defaultValue = "-1") Long ticket) {
		LOG.info("push all to remote repo");
		return operationDetails(repoService.push(ticket));
	}

	/*****************************		  E. Operations for deployment functionality        ******************************/

	
	@RequestMapping(value = "/package/init", method = RequestMethod.POST)
	public ResponseEntity<Long> initPackageUpload() {

		Long opId = repoService.initDataUpload();
		if (opId < 0) {
			return new ResponseEntity<Long>(HttpStatus.TOO_MANY_REQUESTS);
		}


		return new ResponseEntity<Long>(opId, HttpStatus.OK);
	}
	


	/**
	 */
	
	@RequestMapping(value = "/package/upload", method = RequestMethod.POST)
	public ResponseEntity<Long> upload(@RequestParam(value = "opid", defaultValue = "-1") Long opid,
									   @RequestBody PayloadD p) {
		LOG.info("Getting the zip pack with components from  repository");

		if (opid == -1L) return new ResponseEntity<Long>(HttpStatus.BAD_REQUEST);
		Long result = repoService.uploadData(p, opid);

		LOG.info("result {} ",result);
		if (result == null || result < 0) {
			return new ResponseEntity<Long>(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<Long>(opid, HttpStatus.OK);
	}


	/**
	 */
	
	@RequestMapping(value = "/package/deploy", method = RequestMethod.POST)
	public ResponseEntity<Long> download(@RequestParam(value = "opid", defaultValue = "-1") Long opid,
										 @RequestBody DeploymentWrapper dw) {
		LOG.info("Getting the zip pack with components from  repository :{}",dw.toString());

		if (opid == -1L || dw == null) return new ResponseEntity<Long>(HttpStatus.BAD_REQUEST);
		
		repoService.deploymentResult(opid, dw.getAuth(),  dw.getOpts());

	
		return new ResponseEntity<Long>(opid, HttpStatus.OK);
	}
	
	/*****************************		  F. Operations for review functionality         ******************************/

	
	@RequestMapping(value = "/data/init", method = RequestMethod.POST)
	public ResponseEntity<Long> initUpload() {

		Long opId = repoService.initUpload();
		if (opId < 0) {
			return new ResponseEntity<Long>(HttpStatus.TOO_MANY_REQUESTS);
		}


		return new ResponseEntity<Long>(opId, HttpStatus.OK);
	}
	


	/**
	 */
	
	@RequestMapping(value = "/data/upload", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE}, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<Long> upload(@RequestParam(value = "opid", defaultValue = "-1") Long opid,
									   @RequestBody Payload p) {
		LOG.info("Getting the zip pack with components from  repository");

		if (opid == -1L) return new ResponseEntity<Long>(HttpStatus.BAD_REQUEST);
		Long result = repoService.uploadData(p, opid);

		LOG.info("result {} ",result);

		return new ResponseEntity<Long>(opid, HttpStatus.OK);
	}



	/**
	 */
	
	@RequestMapping(value = "/data/download", method = RequestMethod.GET)
	public ResponseEntity<PayloadA> download(@RequestParam(value = "opid", defaultValue = "-1") Long opid) {
		LOG.info("Getting the zip pack with components from  repository");

		if (opid == -1L) return new ResponseEntity<PayloadA>(HttpStatus.BAD_REQUEST);
		PayloadA result = repoService.getResults(opid);

		if (result == null) {
			return new ResponseEntity<PayloadA>(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<PayloadA>(result, HttpStatus.OK);
	}





	/**
	 * The handler for uncatched errors
	 * Uncomment this in production version
	 * @return ResponseEntity<Message>
	 */
/*	@ExceptionHandler(Exception.class)
	public ResponseEntity<Message> handleError(){
		return new ResponseEntity<Message>(new Message("Data processing error"), HttpStatus.INTERNAL_SERVER_ERROR);
	}
*/	
	/**
	 * Utility method for inner use
	 * @param opId
	 * @return ResponseEntity
	 */
	private ResponseEntity<Operation> operationDetails(Long opId){
		if (opId == null) {
			return new ResponseEntity<Operation>(HttpStatus.NO_CONTENT);
		}
		Operation op = repoService.getOperationDetails(opId);
		if (op == null) {
			return new ResponseEntity<Operation>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<Operation>(op, HttpStatus.OK);
	}

}
