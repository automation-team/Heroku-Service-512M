package com.flosum.dao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.constants.ServiceConst;
import com.flosum.model.GitCredentials;
import com.flosum.model.RepoDescr;
import com.flosum.model.Operation;
import com.flosum.utils.ArchiveWriter;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Manages all cloned repositories on the server
 * 
 * 
 * @author Alexey Kalutov
 * @since  0.3.0
 */
public class GitFactory {

	private static final Boolean saveLogs = false;
	private static final String TEMP_DIR = File.separatorChar + "tmp";
	private static final String VSTS_HOST = "visualstudio.com";
	private static final Integer MAX_STACK_SIZE = 100;
	private static final Logger LOG = LoggerFactory.getLogger(GitFactory.class);
	// used to generate UID for repository
	private static final AtomicLong generateID = new AtomicLong();
	private static final ArchiveWriter gitLog = new ArchiveWriter(System.out);
	private final OperationalFabric operationFabric;
	
	private final ScheduledTasks scheduledTasks;



	/**  Describes stack of cloned repositories as map git key => GitClient instance 	 */
	/**	 If repository created for RO operations, gitKey === url  						 */
	private  Map<String, GitHandler> stackMap;
	/**  Describes stack of cloned repositories as map UID => url  */
	private  Map<Long, String> idMap;
	/**  Describes stack of cloned repositories as map url => UID   */
	private  Map<String, Long> urlMap;
	
	private Long gitId;// active, i.e. last cloned repository ; deprecated
	

	public GitFactory(OperationalFabric operationFabric, ScheduledTasks scheduledTasks) {
		this.stackMap = new HashMap<String, GitHandler>();
		this.idMap = new HashMap<Long, String>();
		this.urlMap = new HashMap<String,Long>();
		this.scheduledTasks = scheduledTasks;
		this.operationFabric = operationFabric;
	}
	
	public GitHandler getGitHandler(){
		return getGitHandler(this.gitId);
	}
	
	public GitHandler getGitHandler(Long UID){
		if (UID == null) return null;
		if (UID == -1L){
			UID = gitId;
		}
		String gitKey = idMap.get(UID);
		if (stackMap.containsKey(gitKey)){
			return stackMap.get(gitKey);
		}else{
			return null;
		}
	}
	
	public GitHandlerRO getGitROHandler(Long UID){
		if (UID == null) return null;
		String gitKey = idMap.get(UID);
		if (stackMap.containsKey(gitKey)){
			return (GitHandlerRO) stackMap.get(gitKey);
		}else{
			return null;
		}
	}
	
	public GitHandlerRW getGitRWHandler(Long UID){
		if (UID == null) return null;
		String gitKey = idMap.get(UID);
		if (stackMap.containsKey(gitKey)){
			return (GitHandlerRW) stackMap.get(gitKey);
		}else{
			return null;
		}
	}
	
	public Integer getNumberofROHandlers(){
		Integer n = 0;
		for (String key : stackMap.keySet()){
			GitHandler gh = stackMap.get(key);
			if (gh.isExist() && gh.type.equals(ServiceConst.RO_TYPE)){
				n ++;
			}
		}
		return n;
	}
	
	public Integer getNumberofRWHandlers(){
		Integer n = 0;
		for (String key : stackMap.keySet()){
			GitHandler gh = stackMap.get(key);
			if (gh.isExist() && gh.type.equals(ServiceConst.RW_TYPE)){
				n ++;
			}
		}
		return n;
	}
	
	public Long getCurrentId(){
		return gitId;
	}
	
	public static TransportConfigCallback createTransportConfigCallback(GitCredentials creds) {
	    final String username = creds.getUsername();
	    final String password = creds.getPassword();
	    final String publicKey = creds.getPair().getPublicKey();
	    final String privateKey = creds.getPair().getPrivateKey();
	    final String knownHosts = creds.getPair().getKnownHosts();
	    final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {

	        @Override
	        protected void configure(Host host, Session session) {
	        //	  session.setConfig("StrictHostKeyChecking", "no");
	        }

	        protected JSch createDefaultJSch(FS fs) throws JSchException {
	            JSch defaultJSch = super.createDefaultJSch(fs);
	            if (privateKey != null) {
	                byte[] passphrase = null;
	                if (password != null && !password.trim().isEmpty())
	                    passphrase = password.getBytes(StandardCharsets.UTF_8);
	                byte[] publicKeyDecoded = Base64.getDecoder().decode(publicKey);
	                byte[] privateKeyDecoded = Base64.getDecoder().decode(privateKey);
	                LOG.info("addIdentity");
	                defaultJSch.addIdentity(username, privateKeyDecoded, publicKeyDecoded, passphrase);
	                LOG.info("setKnownHosts");
	                defaultJSch.setKnownHosts(new ByteArrayInputStream(Base64.getDecoder().decode(knownHosts)));
	            }
	            return defaultJSch;
	        }
	    };
	    return new TransportConfigCallback() {

	        @Override
	        public void configure(Transport transport) {
	            SshTransport sshTransport = (SshTransport) transport;
	            sshTransport.setSshSessionFactory(sshSessionFactory);
	        }
	    };
	}

	/*
	 *  Perform clone operation, create client to do git operations 
	 *  with repository, update stackMap, update idMap, update operation record
	 *  After cloning, updates for main git structures must take place
	 */
	public void cloneWithAuthHttps(GitCredentials creds, Operation op) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		String user = creds.getUsername();
		String pwd = creds.getPassword();

		final String REMOTE_URL = getFullUrl(ServiceConst.P_HTTPS, creds.getHost(), creds.getPath(),true);
		final String REMOTE_URL2 = getFullUrl(ServiceConst.P_HTTPS, creds.getHost(), creds.getPath(),false);
		final String gitKey = key(REMOTE_URL, creds.getUserId());
		if (stackMap.containsKey(gitKey)){// verify is repo was cloned
			if (stackMap.get(gitKey).isExist()){//verify is repo still exists on hdd
				LOG.info("Repository "+REMOTE_URL+" has been cloned already");
				// pull only if changes took place
				if (scheduledTasks.isRepositoryChanged(urlMap.get(gitKey))){
					stackMap.get(gitKey).pullAuth(op, urlMap.get(gitKey));
					operationFabric.closeOperation(op);
				}else{
					stackMap.get(gitKey).pullAuth(op, urlMap.get(gitKey));// for test only
					operationFabric.closeOperation(op);
				}
				return;
			}else{// remove from track list
				LOG.info("Repository {} does not exist on hdd ",gitKey);
				scheduledTasks.removeTrackingRepository(urlMap.get(gitKey));
			}
		}
		

		File localPath = File.createTempFile(TEMP_DIR, "");

		if (!localPath.delete()) {
			LOG.info("ERROR: Could not delete temporary file {}", localPath);
			operationFabric.completeWithStatus(op.getId(), false, ServiceConst.E_IO);
			return;
		}
		
		String validUrl = "";
		if (isPathValid(creds,ServiceConst.CLONE_CHECK_CREDS_1,true)) {
			validUrl = REMOTE_URL;
		}else if (isPathValid(creds,ServiceConst.CLONE_CHECK_CREDS_1,false)) {
			validUrl = REMOTE_URL2;
		}else {
			operationFabric.cancelOperation(op, "Invalid Url");
			return;
		}
		// then clone
		try (Git git = Git.cloneRepository().setURI(validUrl).setDirectory(localPath)
				.setProgressMonitor(new TextProgressMonitor(gitLog))
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, pwd))
				.setCloneAllBranches(true).call()) {
			Repository localRepo = git.getRepository();

			gitId = generateID.incrementAndGet();
			RepoDescr curGit = new RepoDescr(gitId, REMOTE_URL, localPath, git, localRepo, creds);
			StoredConfig config = git.getRepository().getConfig();
			config.setString("remote", "origin", "fetch", "+refs/*:refs/*");
			config.save();
			// remove excessive descriptors
			removeExcess();
			// create client, put it into map (git vars such as branches, checkout created in constructor)
			if (creds.getUserId() == null){
				LOG.info("creating ro handler", localPath);
				stackMap.put(gitKey, new GitHandlerRO(curGit,gitId));
				// update tracking list for scheduler
				scheduledTasks.addTrackingRepository(gitId,REMOTE_URL);
			}else{
				LOG.info("creating rw handler for {}", creds.getUserId());
				stackMap.put(gitKey, new GitHandlerRW(curGit,gitId));
			}
			idMap.put(gitId, gitKey);
			urlMap.put(gitKey, gitId);
			// update operation record in table
			operationFabric.completeWithStatus(op.getId(),true, gitId);
			if (saveLogs){
				op.setLog(gitLog.getNewData());
			}
			op.genSignature();
		}
	}

	
	public void cloneWithAuthSsh(GitCredentials creds, Operation op) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		String user = creds.getUsername();
		String pwd = creds.getPassword();
		// host must have the form git@github.com
		final String REMOTE_URL = getFullUrl(ServiceConst.P_SSH, creds.getHost(),creds.getPath(),true);
		final String REMOTE_URL2 = getFullUrl(ServiceConst.P_SSH, creds.getHost(),creds.getPath(),false);
		LOG.info("URL:"+REMOTE_URL);
		final String gitKey = key(REMOTE_URL, creds.getUserId());
		if (stackMap.containsKey(gitKey)){// verify is repo was cloned
			if (stackMap.get(gitKey).isExist()){//verify is repo still exists on hdd
				LOG.info("Repository "+REMOTE_URL+" has been cloned already");
				stackMap.get(gitKey).pullAuth(op, urlMap.get(gitKey));
				operationFabric.closeOperation(op);
				return;
			}else{// remove from track list
				LOG.info("Repository {} does not exist on hdd ",gitKey);
				scheduledTasks.removeTrackingRepository(urlMap.get(gitKey));
			}
		}
		

		File localPath = File.createTempFile(TEMP_DIR, "");

		if (!localPath.delete()) {
			LOG.info("ERROR: Could not delete temporary file {}", localPath);
			operationFabric.completeWithStatus(op.getId(),false, ServiceConst.E_IO);
			return;
		}

		String validUrl = "";
		if (isPathValid(creds,ServiceConst.CLONE_CHECK_CREDS_1,true)) {
			validUrl = REMOTE_URL;
		}else if (isPathValid(creds,ServiceConst.CLONE_CHECK_CREDS_1,false)) {
			validUrl = REMOTE_URL2;
		}else {
			operationFabric.cancelOperation(op, "Invalid Url");
			return;
		}
		// then clone
		try (Git git = Git.cloneRepository().setURI(REMOTE_URL).setDirectory(localPath)
				.setProgressMonitor(new TextProgressMonitor(gitLog))
				.setTransportConfigCallback(createTransportConfigCallback(creds))
				.setCloneAllBranches(true).call()) {
			Repository localRepo = git.getRepository();
			gitId = generateID.incrementAndGet();
			RepoDescr curGit = new RepoDescr(gitId, REMOTE_URL, localPath, git, localRepo, creds);
			// remove excessive descriptors
			removeExcess();
			// create client, put it into map (git vars such as branches, checkout created in constructor)
			if (creds.getUserId() == null){
				LOG.info("creating ro handler", localPath);
				stackMap.put(gitKey, new GitHandlerRO(curGit,gitId));
				// update tracking list for scheduler
				scheduledTasks.addTrackingRepository(gitId,REMOTE_URL);
			}else{
				LOG.info("creating rw handler for {}", creds.getUserId());
				stackMap.put(gitKey, new GitHandlerRW(curGit,gitId));
			}
			idMap.put(gitId, gitKey);
			urlMap.put(gitKey, gitId);
			// update operation record in table
			operationFabric.completeWithStatus(op.getId(), true, gitId);
			if (saveLogs){
				op.setLog(gitLog.getNewData());
			}
			op.genSignature();
			// set tracking configuration
			StoredConfig config = git.getRepository().getConfig();
			config.setString("remote", "origin", "fetch", "+refs/*:refs/*");
			config.save();
		}
	}
	
	public Boolean isPathValid(GitCredentials gc, Integer phase, Boolean withSuffix) {
		try {
			return isValid(gc,phase,withSuffix);
		}catch(Exception e) {
			LOG.info("Validation:{}"+e.getMessage());
			return false;
		}
	}
	
	public Boolean isValid(GitCredentials gc, Integer phase, Boolean withSuffix) throws InvalidRemoteException, TransportException, GitAPIException{
		// the 1st phase - used for private repos
		if (phase == ServiceConst.CLONE_CHECK_CREDS_1 ){
		
			Collection<Ref> refs = null;
			if (gc.getProtocol().equals(ServiceConst.P_HTTPS)){
				final String REMOTE_URL = getFullUrl(ServiceConst.P_HTTPS, gc.getHost(), gc.getPath(), withSuffix);
				refs = Git.lsRemoteRepository()
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(gc.getUsername(), gc.getPassword()))
						.setHeads(true).setRemote(REMOTE_URL).setTimeout(ServiceConst.T_TIMEOUT).call();
			}else if (gc.getProtocol().equals(ServiceConst.P_SSH)){
				final String REMOTE_URL = getFullUrl(ServiceConst.P_SSH, gc.getHost(),gc.getPath(), withSuffix);
				refs = Git.lsRemoteRepository()
						.setTransportConfigCallback(createTransportConfigCallback(gc))
						.setHeads(true).setRemote(REMOTE_URL).setTimeout(ServiceConst.T_TIMEOUT).call();
			}
			if (refs != null){
				if (refs.size() != 0){
					return true;
				}
			}
		}
		return false;

	}

	private static String getFullUrl(String protocol, String host, String path, Boolean withSuffix){
		String suffix = "";
		if (host != null && !host.contains(VSTS_HOST)){// not VSTS
			suffix = ".git";
		}
		if (protocol == null ) return "";
		String result = "";
		if (protocol.equals(ServiceConst.P_SSH)){
			result =  host + ":" + path + suffix;
		}else if (protocol.equals(ServiceConst.P_HTTPS)){
			if (host != null && !host.trim().startsWith("http")) {
				result = "https://";
			}
			result += (host + "/" + path + suffix);
		}
		result = result.trim();
		if (withSuffix) return result;
		if (result.endsWith(".git")) {
			return result.substring(0, result.length() - 4);
		}
		return result;
	}
	
	private static String key(String url, String userId){
		if (userId != null){
			return url + "#" + userId;
		}else{
			return url;
		}
	}
	
	private void removeExcess(){
		if (stackMap.size() > MAX_STACK_SIZE){
			stackMap.remove(0);
			idMap.remove(0);
		}
	}

	public ScheduledTasks getScheduledTasks(){
		return this.scheduledTasks;
	}

}
