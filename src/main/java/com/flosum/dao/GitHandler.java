package com.flosum.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.constants.ServiceConst;
import com.flosum.model.*;
import com.flosum.utils.ArchiveWriter;
import com.flosum.xml.XmlPreprocessor;

/**
 * This class used to perform the low-level operations with remote git repository
 * Designed as operational interface
 *  
 * @author Alexey Kalutov
 * @since  0.2.0
 */
public class GitHandler {
	
	private final static Logger LOG = LoggerFactory.getLogger(GitHandler.class);

	protected static final String MASTER = "master";
	protected static final String MASTER_HEAD = "refs/heads/master";
	protected static final ArchiveWriter gitLog = new ArchiveWriter(System.out);
	


	protected static final Set<String> FOLDERTYPES = Collections.unmodifiableSet(new HashSet<String>() {
		private static final long serialVersionUID = -4257416339152389197L;

		{
			add("EmailTemplate");
			add("EmailTemplate(meta)");
			add("Report");
			add("Report(meta)");
			add("Dashboard");
			add("Dashboard(meta)");
		}
	});


	protected Boolean isEmpty = true;
	
		
	// a simple cache to improve performance
	protected Map<String, Branch> branches;
	protected Map<String, Commit> commits;
	protected final XmlPreprocessor xmlPreprocessor = new XmlPreprocessor();
	
	
	protected String initCommit;// needed to create a new branches
	protected String masterId; // sha of master branch
	
	protected Integer packCounter = -1;
	protected Integer type;// describes type of repository for RO/RW operations
	
	/* Describes  repository; always != null */
	
	public  RepoDescr repo;

	public GitHandler(RepoDescr repo, Long gitId) throws GitAPIException, IOException {
		this.repo = repo;
		branches = new HashMap<String, Branch>();
		commits = new HashMap<String, Commit>();
		// create track branches, update list of local branches
		createTrackBranches();
		// checkout master branch by default
		checkoutLocalBranch(MASTER);
		//get sha of initial commit
		// first, get list of branches 
		getBranches();
		// get list of commits from master branch (master must always exist)
		List<Commit> lc = getCommits(branches.get(masterId).getCommits());
		if (lc != null && lc.size() > 0){// set init commit only if list of commits non-empty
			LOG.info("total commits:{}",lc.size());
			isEmpty = false;
			initCommit = lc.get(lc.size() - 1).getId().getName();// set last commit in list as initial
			LOG.info("initCommit={}",initCommit);
		}
	}
	
	
	
	
	/****************************************************************************/
	/* 							 get (not changing) 	methods					*/
	/****************************************************************************/
	
	// check existence on the hdd
	public Boolean isExist(){
		return repo.getPath().exists();
	}
	
	// returns value of uploaded packs
	public Integer getCounter(){
		return this.packCounter;
	}
	

	/**
	 * Returns a list of branches from active repository. Updates branches (for cache)
	 * 
	 */

	public List<Branch> getBranches() throws IOException, GitAPIException {
		List<Branch> lb = new ArrayList<Branch>();

		if (repo == null){
			return lb;
		}
		branches.clear();
		List<Ref> call = repo.getGit().branchList().setListMode(ListMode.ALL).call();
		for (Ref ref : call) {
			Branch b = new Branch(ref.getObjectId(), ref.getName(), ref);
			LOG.info("add branch:{}",ref.getName());
			if (ref.getName().equals(MASTER_HEAD))
				masterId = ref.getObjectId().getName();
			lb.add(b);
			branches.put(ref.getObjectId().getName(), b);
		}
		return lb;
	}
	
	/**
	 * Returns list of commits from branch referenced by {@code head} Updates commits (for cache)
	 * Returns empty list if wrong branch sha
	 */

	public List<Commit> getCommits(Ref head) throws IOException, GitAPIException {

		List<Commit> lc = new ArrayList<Commit>();
		
		if (head == null) return lc;
		
		commits.clear();

		LOG.info("get commits for branch: {}", head.getName());

		Iterable<RevCommit> revCommits = repo.getGit().log().add(repo.getRepo().resolve(head.getName())).call();
		for (RevCommit rev : revCommits) {

			Commit c = new Commit(rev.getId(), rev.getAuthorIdent().getName(), rev.getShortMessage(),
					rev.getCommitTime(), rev);
			lc.add(c);
			commits.put(c.getId().getName(), c);
		}
		return lc;
	}

	public List<Commit> getSingleCommit(Ref head, String commitSha) throws IOException, GitAPIException {

		List<Commit> lc = new ArrayList<Commit>();
		
		if (head == null || commitSha == null || commitSha == "") return lc;
		
		LOG.info("get single commit {} from branch: {}", commitSha, head.getName());

		Iterable<RevCommit> revCommits = repo.getGit().log().add(repo.getRepo().resolve(head.getName())).call();
		for (RevCommit rev : revCommits) {
			if (commitSha.equals(rev.getId().getName())){
				lc.add(new Commit(rev.getId(), rev.getAuthorIdent().getName(), rev.getShortMessage(), rev.getCommitTime(), rev));
				return lc;
			}
		}
		return null;
	}

	/**
	 * Set local branch
	 * NB: branch must exist in git!
	 */

	public void checkoutLocalBranch(String brName) throws IOException, GitAPIException {
		if (repo == null)
			return;
		// if branch brName exists:
		// clean stage area before checkout
		Set<String> result = repo.getGit().clean().setForce(true).setCleanDirectories(true).call();
		LOG.info("clean result: " + result);
		LOG.info("checkout existing branch: " + brName);
		repo.getGit().checkout().setCreateBranch(false).setName(brName).call();
		repo.getRepo().updateRef(Constants.HEAD).link("refs/heads/" + brName);
	}

	/**
	 * Checkout commit
	 *  NB: commit must exist in git!
	 */

	public void checkoutLocalCommit(String cId) throws IOException, GitAPIException {
		// if branch brName exists:
		// clean stage area before checkout
		Set<String> result = repo.getGit().clean().setForce(true).setCleanDirectories(true).call();
		LOG.info("clean result: " + result);
		LOG.info("checkout commit: " + cId);
		repo.getGit().checkout().setName(cId).setForce(true).call();
	}
	
	/**
	 * Set creds
	 * NB: validate creds before set 
	 * @param gc
	 * @return
	 */
	
	public Boolean setCredentials(GitCredentials gc,  Operation op) {
		if (repo == null) return false;
		repo.setGitCredentials(gc);
		op.completeWithStatus(true, ServiceConst.I_NEWBRANCH);
		return true;
	}
	
	public void resetCounter(){
		 this.packCounter = -1;
	}

	/**
	 * Pull all changes from remote (original) repo 
	 * (all branches under refs/heads/*)
	 */
	
	public void pullAuth(Operation op, Long gitId) throws IOException, GitAPIException,InvalidRemoteException {
		final String REMOTE_URL;
		
		GitCredentials gc = repo.getGitCredentials();
		String username = gc.getUsername();
		String password = gc.getPassword();
		String log = "";
		// checkout [master] branch in order avoid Detach head error
		Ref l = repo.getGit().reset().setMode(ResetType.HARD).setRef(MASTER).call();

		Set<String> cleanResult = repo.getGit().clean().setForce(true).setCleanDirectories(true).call();
		checkoutLocalBranch(MASTER);
		
		LOG.info("cleanResult : {}",cleanResult);

		FetchCommand fetch = repo.getGit().fetch();
		if (gc.getProtocol().equals(ServiceConst.P_HTTPS)){
			fetch.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
						.setProgressMonitor(new TextProgressMonitor(gitLog));
		}else if (gc.getProtocol().equals(ServiceConst.P_SSH)){
			fetch.setTransportConfigCallback(GitFactory.createTransportConfigCallback(gc))
						.setProgressMonitor(new TextProgressMonitor(gitLog));
		}else{// unsupported protocol
			op.completeWithStatus(false, ServiceConst.E_WRONG_PROTOCOL);
			return;
		}
		FetchResult fetchResult = fetch.call();
		Boolean result = true;
		String msg  = fetchResult.getMessages();
		  


//		MergeResult res = pc.call().getMergeResult();
		
		PullCommand pc = repo.getGit().pull().setRebase(true);
		if (gc.getProtocol().equals(ServiceConst.P_HTTPS)){
			pc.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
						.setProgressMonitor(new TextProgressMonitor(gitLog));
		}else if (gc.getProtocol().equals(ServiceConst.P_SSH)){
			pc.setTransportConfigCallback(GitFactory.createTransportConfigCallback(gc))
						.setProgressMonitor(new TextProgressMonitor(gitLog));
		}else{// unsupported protocol
			op.completeWithStatus(false, ServiceConst.E_WRONG_PROTOCOL);
			return;
		}
//		Boolean result = true;
		PullResult res = pc.call();
//		String msg  = res.getMergeResult() == null? "":res.getMergeResult().getMergeStatus().toString();

		log += msg;
		op.completeWithStatus(result, "Pull complete");
		op.setRepoID(gitId);
		// update Scheduler flags
		RepoDAOImpl.getGFactory().getScheduledTasks().setFlagTrackingRepository(gitId, true);
		updateTrackBranches();
	}

	
	
	
	/****************************************************************************/
	/* 					 		Utility methods									*/
	/****************************************************************************/


	public RevCommit getCommit(String sha) throws MissingObjectException, IncorrectObjectTypeException, IOException{
		if (sha != null && sha != ""){
			ObjectId commitId = ObjectId.fromString( sha);
			RevWalk revWalk = new RevWalk( repo.getRepo() );
			RevCommit commit = revWalk.parseCommit( commitId );
			revWalk.close();
			return commit;
		}
		return null;
	}
	
	public CommitDigest getCommitDetails(String sha){
		try {
			RevCommit rc = getCommit(sha);
			return new CommitDigest(rc);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates/Updates a bundle of tracking branches for remote branches
	 * 
	 * @throws GitAPIException
	 */
	public void createTrackBranches() throws GitAPIException {
		if (repo.getLocalBranches() != null) return;
		
		Set<String> localBranches = new HashSet<String>();
		localBranches.add(MASTER);

		List<Ref> call = repo.getGit().branchList().setListMode(ListMode.ALL).call();
		for (Ref ref : call) {
			String refName = ref.getName();
			if (refName.indexOf("refs/remotes/origin/") != -1) {
				String brName = refName.replaceAll("refs/remotes/origin/", "");
				if (!brName.equals("master")) {
					LOG.info("Creating tracking branch : {}", brName);
					repo.getGit().branchCreate().setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
							.setStartPoint("origin/" + brName).setForce(true).setName(brName).call();
					localBranches.add(brName);
				}
			}
		}
		repo.setLocalBranches(localBranches);
	}

	
	public void updateTrackBranches() throws GitAPIException {
		
		List<Ref> call = repo.getGit().branchList().setListMode(ListMode.ALL).call();
		for (Ref ref : call) {
			String refName = ref.getName();
			if (refName.indexOf("refs/remotes/origin/") != -1) {
				String brName = refName.replaceAll("refs/remotes/origin/", "");
				if (!brName.equals("master")) {
					if (!repo.getLocalBranches().contains(brName)){// create a tracking branch only if it does note exist
						LOG.info("Creating tracking branch : {}", brName);
						repo.getGit().branchCreate().setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
							.setStartPoint("origin/" + brName).setForce(true).setName(brName).call();
						repo.getLocalBranches().add(brName);
					}
				}
			}
		}
	}

	// returns true, if branch exists in current repo
	public Boolean isBranchExists(String brName) {
		if (repo == null)
			return false;
		LOG.info("verify branch:" + brName);

		return repo.getLocalBranches().contains(brName);
	}


	/**
	 * Returns operational log for git commands
	 * 
	 */
	
	public String getLog(){
		if (gitLog.isNewData()){
			return gitLog.getNewData();
		}
		return "";
	}
	
	
	public RepoDescr getRepo(){
		return this.repo;
	}


	public Integer getType() {
		return type;
	}


}
