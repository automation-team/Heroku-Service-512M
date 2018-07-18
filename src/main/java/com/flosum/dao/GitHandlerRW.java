package com.flosum.dao;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;

import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.constants.CompType;
import com.flosum.constants.ServiceConst;
import com.flosum.model.CommitFutures;
import com.flosum.model.Component;
import com.flosum.model.GitCredentials;
import com.flosum.model.MetaItem;
import com.flosum.model.Payload;
import com.flosum.model.RepoDescr;
import com.flosum.model.Operation;
import com.flosum.sfcomponents.Territory2;
import com.flosum.sfcomponents.Territory2Rule;
import com.flosum.utils.ArchiveWriter;
import com.flosum.utils.IOLogger;
import com.flosum.utils.StringUtils;
import com.flosum.utils.ZipUtils;

public class GitHandlerRW extends GitHandler{
	
	private final Logger LOG = LoggerFactory.getLogger(GitHandler.class);
	private static final ArchiveWriter gitLog = new ArchiveWriter(System.out);
	
	private static final Integer FOLDER = 40000;
	private static final Integer BLOB = 100644;
	private static final String AURA_BUNDLE = "AuraDefinitionBundle";
	private static final String EMAIL_TEMPLATE = "EmailTemplate";
	private static final String DOCUMENT = "Document";
	private static final String DOCUMENT_FOLDER = "DocumentFolder";

	private static final String NOTSF = "notSF";

	private Map<String, Component> components;
	private List<Long> uploadedPayloads;

	
	// needed for commit transaction
	private CommitFutures curCommit;// set before commit


	public GitHandlerRW(RepoDescr repo, Long gitId) throws GitAPIException, IOException {
		super(repo, gitId);
		type = ServiceConst.RW_TYPE;
		uploadedPayloads = new ArrayList<Long>();
	}
	

	/****************************************************************************/
	/* 					 changing local git structures methods					*/
	/****************************************************************************/

	
	/**
	 * Set (create) local branch
	 * if initial commit exists, check it out before
	 * (else do nothing, git set head to init commit)
	 */

	public void createNewBranch(String brName) throws IOException, GitAPIException {
		if (repo == null) return;
		if (!isEmpty){// set head to initial point
			checkoutLocalCommit(this.initCommit);
		}
		repo.getGit().branchCreate().setName(brName).call();
		repo.getRepo().updateRef(Constants.HEAD).link("refs/heads/" + brName);
		// add branch name to set
		repo.getLocalBranches().add(brName);
	}

	/**
	 * Set (create) local branch - the 2nd version
	 */

	public void createNewOrphanBranch(String brName,RevCommit rc) throws IOException, GitAPIException {
		if (repo == null)	return;
		repo.getGit().checkout().setOrphan(true).setCreateBranch(true).setStartPoint(rc).setName(brName).call();
		// add branch name to set
		repo.getLocalBranches().add(brName);
	}
	
	/****************************************************************************/
	/* 					write DATA to local Repository methods					*/
	/****************************************************************************/
	
	public void addId(Long id) {
		uploadedPayloads.add(id);
	}
	
	public List<Long> uploaded(){
		return uploadedPayloads;
	}
	

	public void setCommit(CommitFutures cf, Operation op) throws IOException, GitAPIException{
		String branchCommitTo = cf.getBranchName();
		
		LOG.info("setCommit");
		// always reset Xml processor when new upload cycle
		xmlPreprocessor.init(op.getId());
		// always reset id list when new cycle
		uploadedPayloads.clear();
		
		if (!isBranchExists(branchCommitTo)){//checkout the 1st commit from master branch, create and checkout the new branch 
			createNewBranch(branchCommitTo);
			op.completeWithStatus(true, ServiceConst.I_NEWBRANCH + branchCommitTo);
		}else{
			checkoutLocalBranch(branchCommitTo);
			op.completeWithStatus(true, ServiceConst.I_EXISTEDBRANCH + branchCommitTo);
		}
		packCounter = cf.getCounter();
		this.curCommit = cf;
	}
	
	/**
	 *  Add pack, dec counter
	 *  
	 * @param p
	 * @param op
	 * @throws IOException
	 * @throws GitAPIException
	 * @throws UnsupportedEncodingException
	 */
	
	public void uploadZipPack(Payload p, Operation op) throws IOException, GitAPIException, UnsupportedEncodingException{
		LOG.info("add pack of type:{} and listsize:{}",p.getCompType(),p.size());
/*
		LOG.info("gen files into :{} ",repo.getPath().toString());

		byte[] buf = new byte[8192];

		int len;

		String data = "";
		byte[] bArray = new byte[1024*1024];
		for (Integer i = 0; i < 1024*512; i++ ){
			data += "1";
		}
		for (Integer i = 0; i < 500; i++){
			String fileName = "/classes/f" + i;
			File f = new File(repo.getPath(), fileName);
			ZipUtils.write(data, f);
		}
		LOG.info("written 100x :{} bytes ", bArray.length);
*/		
		if (p.size() > 0){
			if (!CompType.isInner(p.getCompType())){
				if (!ServiceConst.DELETED.equals(p.getCompType())){
					for (MetaItem mi: p.getItems()){
						ZipUtils.unpackZip(mi.getDataStr(), repo.getPath(), p.getCompType(), mi.getName());
					}
					
				}else {
					List<String> paths = new ArrayList<String>();
					for (MetaItem mi: p.getItems()){
						paths.add(mi.getFilename());
					}					
					DiskService.deleteFiles(repo.getPath(),paths);
				}
			}else{
				for (MetaItem mi: p.getItems()){
					ZipUtils.unpackZip(mi.getDataStr(), repo.getPath(), p.getCompType(), mi.getName(),xmlPreprocessor);
				}
			}
		}
		 
		op.completeWithStatus(true, IOLogger.changeResultString(p.getCompType(), p.size()));
		this.packCounter--;
		LOG.info("complete with op={}",op.getId());
		LOG.info("packCounter:{}",this.packCounter);
	}

	
	/**
	 * Commits all changes to local repo under current branch
	 */
	
	public void commitAll(Operation op) throws IOException, GitAPIException {
		xmlPreprocessor.compileAll();
		xmlPreprocessor.cleanStageArea();
		xmlPreprocessor.reset();
		String msg = curCommit.getMessage();
		msg = (msg == null) ? "" : msg;
		String name = curCommit.getCommitter().getFullName();
		String email = curCommit.getCommitter().getMail();
		DirCache dc = repo.getGit().add().setUpdate(false).addFilepattern(".").call();
		LOG.info("added new files: {}",dc.getEntryCount());
		dc = repo.getGit().add().setUpdate(true).addFilepattern(".").call(); 
		LOG.info("added updated files: {}",dc.getEntryCount());
		// commit the changes
		RevCommit rc = repo.getGit().commit().setAuthor(name, email).setCommitter(name, email).setMessage(msg).call();
		if (this.isEmpty){// if repository was empty, set current commit as initial
			this.initCommit = rc.getId().getName();
			this.isEmpty = false;
		}
		op.completeWithStatus(true, rc.getName());
	}
	
	/**
	 * Rolls back all changes, if fail
	 */
	public void rollBack() {
		try {
			Set<String> removed = repo.getGit().clean().setCleanDirectories(true).call();
			resetCounter();
			LOG.info("Roolback: removed {} items", removed.size());
		} catch (GitAPIException e) {
			LOG.info("GITAPI error during rollback");
		}

	}


	/****************************************************************************/
	/* 					write DATA to Remote Repository methods					*/
	/****************************************************************************/

	/**
	 * Push all changes to remote (original) repo 
	 * (all branches under refs/heads/*)
	 */
	
	public void pushAuth(Operation op) throws IOException, GitAPIException,InvalidRemoteException {
		
		GitCredentials gc = repo.getGitCredentials();
		String username = gc.getUsername();
		String password = gc.getPassword();
		String log = "";
		PushCommand pc = repo.getGit().push();
		LOG.info("push: user:{}, pwd: {}", username, password);
		if (gc.getProtocol().equals(ServiceConst.P_HTTPS)){
			pc.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).setForce(true)
				.setProgressMonitor(new TextProgressMonitor(gitLog)).setPushAll();
		}else if (gc.getProtocol().equals(ServiceConst.P_SSH)){
			pc.setTransportConfigCallback(GitFactory.createTransportConfigCallback(gc)).setForce(true)
			.setProgressMonitor(new TextProgressMonitor(gitLog)).setPushAll();
		}else{// unsupported protocol
			op.completeWithStatus(false, ServiceConst.E_WRONG_PROTOCOL);
			return;
		}
		Iterator<PushResult> it = pc.call().iterator();
		if (it.hasNext()){
			String line = it.next().toString();
			LOG.info(line);
			log += line;
		}
		// update local data about branches
		op.completeWithStatus(true, log += gitLog.getNewData());
	}
	
	
	/**
	 * Returns list of components from referenced [root] tree, i.e. uses
	 * commit's root tree as starting point to walk. 
	 * If components have wrong name structure, do not process them (i.e. add as NotSF)
	 *
	 */

	public void getComponentsSha(RevCommit rc, Map<String,MetaItem> compMap) throws IOException, GitAPIException {
		List<String> compIds = new ArrayList<String>();// holds component's sha during traversing
		if (repo == null || rc == null)
			return;

		RevTree tree = rc.getTree();
		checkoutLocalCommit(rc.getId().name());// checkout needed to process  components, else I/O error

		try (TreeWalk treeWalk = new TreeWalk(repo.getRepo())) {
			treeWalk.addTree(tree);
			treeWalk.setRecursive(true);
			treeWalk.setPostOrderTraversal(true); // return trees
			while (treeWalk.next()) {
				int fileMode = Integer.parseInt(treeWalk.getFileMode(0).toString());
				ObjectId objectId = treeWalk.getObjectId(0);
				String path = treeWalk.getPathString();
				String name = treeWalk.getNameString();
				String sha = objectId.getName();

				if (fileMode == FOLDER) {
					// process special case - aura type
					// path = aura/TestComponent/TestComponent.cmp
					// name = TestComponent.cmp
//					if (!compIds.contains(sha)) {//add only unique components
						if (StringUtils.isAuraComp(path)) {// detect aura component by path
							putIfinList(sha, StringUtils.compositeName(AURA_BUNDLE, name),  compMap, false);
						} 
//						compIds.add(sha);
//					}
					continue;
				}

				if (fileMode == BLOB) {
					// omit aura inner elements
					if (StringUtils.isAuraComp(path)) continue;
					// omit meta files TODO: verify FOLDER type

					//process general/inner type
					
//					if (!compIds.contains(sha)) {//add only unique components
						String dir = StringUtils.getCompDir(path);
						String cType = CompType.getType(dir);
						if (cType == null) continue;
						Boolean isMeta = false;
						if (StringUtils.isMetaFile(name)){
							isMeta = true;
						}
						String folder = StringUtils.getFolderName(path);
//						LOG.info("isFolder: {}",folder);

						if (folder != null){ 
							putIfinList(sha, StringUtils.compositeName(StringUtils.getFolderType(dir), folder), compMap, false);
						}else if (cType.contains(DOCUMENT)){
							putIfinList(sha, StringUtils.compositeName(DOCUMENT, StringUtils.getDocComplexName(path)), compMap, isMeta);
						}else if (Territory2.isTerritory2(path)){
							cType = "Territory2";
							name = Territory2.getFullName(path, StringUtils.getCompName(name));
							putIfinList(sha, name, compMap, isMeta);
						}else if (Territory2Rule.isTerritory2Rule(path)){
							cType = "Territory2Rule";
							name = Territory2Rule.getFullName(path, StringUtils.getCompName(name));
							putIfinList(sha, name, compMap, isMeta);
						}else if (!FOLDERTYPES.contains(cType)){// all except email templates
							//first, verify is extension has the ending #<inner_set>
							String innerName;
							if ((innerName = StringUtils.isInnerComp(name)) != null){
								// add inner as component with name (without suffix ) and type defined by ext (extracted from s)
								cType = CompType.getTypebyExt(StringUtils.getExt(name));
								putIfinList(sha, StringUtils.compositeName(cType, innerName), compMap, isMeta);
							}else{// not inner, verify ext
								if (!isMeta){
									if (!CompType.getExt(cType).equals( StringUtils.getExt(name) )){
										cType = NOTSF;
									}
								}
								putIfinList(sha, StringUtils.compositeName(cType, StringUtils.getCompName(name)), compMap, isMeta);
							}
						}else{// for EMAIL_TEMPLATE
							putIfinList(sha,  StringUtils.compositeName(cType, StringUtils.getCompComplexName(path)), compMap, isMeta);
						}
//						compIds.add(sha);
//					}

				}
			}
			// end of walk
		}
	}

	public void putIfinList(String sha, String name, Map<String,MetaItem> compMap, Boolean isMeta){
		if (compMap.containsKey(name)){
			String oldSha = compMap.get(name).getSha();// initial value of sha must be empty
//			LOG.info("oldSha={}",oldSha);
			if (isMeta){
				oldSha += " " + sha;// wrote to tail
			}else{
				oldSha = sha + oldSha;// add 1st
			}
//			LOG.info("oldSha={}",oldSha);
			compMap.get(name).setSha(oldSha);
		}
	}
	


	
// end of class
}
