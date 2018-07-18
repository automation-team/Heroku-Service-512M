package com.flosum.dao;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.constants.CompType;
import com.flosum.constants.ServiceConst;
import com.flosum.model.Branch;
import com.flosum.model.Commit;
import com.flosum.model.CommitDigest;
import com.flosum.model.Component;
import com.flosum.model.GitCredentials;
import com.flosum.model.MetaItem;
import com.flosum.model.Payload;
import com.flosum.model.RepoDescr;
import com.flosum.sfcomponents.Territory2;
import com.flosum.sfcomponents.Territory2Rule;
import com.flosum.utils.StringUtils;

public class GitHandlerRO extends GitHandler  {
	
	private final static Logger LOG = LoggerFactory.getLogger(GitHandlerRO.class);

	
	private static final Integer FOLDER = 40000;
	private static final Integer BLOB = 100644;
	private static final String AURA_BUNDLE = "AuraDefinitionBundle";
	private static final String DOCUMENT = "Document";
	private static final String NOTSF = "notSF";
	


	private Map<String, Component> components;


	public GitHandlerRO(RepoDescr repo, Long gitId) throws GitAPIException, IOException {
		super(repo, gitId);
		type = ServiceConst.RO_TYPE;
		components = new HashMap<String, Component>();
	}
	
	

	/**
	 * Returns list of components from referenced [root] tree, i.e. uses
	 * commit's root tree as starting point to walk. 
	 * If components have wrong name structure, do not process them (i.e. add as NotSF)
	 *
	 */

	public List<Component> getComponents(RevCommit rc) throws IOException, GitAPIException {
		List<Component> comps = new ArrayList<Component>();
		if (repo == null || rc == null)
			return comps;
//		components.clear();
		RevTree tree = rc.getTree();
		checkoutLocalCommit(rc.getId().name());// checkout needed to process  components, else I/O error
		// Long start = Instant.now().toEpochMilli();

		// now try to find a specific file
		try (TreeWalk treeWalk = new TreeWalk(repo.getRepo())) {
			treeWalk.addTree(tree);
			treeWalk.setRecursive(true);
			treeWalk.setPostOrderTraversal(true); // return trees
			while (treeWalk.next()) {
				Integer fileMode = Integer.parseInt(treeWalk.getFileMode(0).toString());
				ObjectId objectId = treeWalk.getObjectId(0);
				String path = treeWalk.getPathString();
				String name = treeWalk.getNameString();
				String sha = objectId.getName();
				File f = new File(repo.getPath(), path);
				Long fSize = 0L;
				if (f.exists()){
					if (f.isFile()) fSize =	f.length();
				}else{
					continue;
				}
//				LOG.info("found: fileMode {}, path {}, name {}, size {}, sha{}",fileMode, path, name,fSize, sha);
				if (fileMode.equals(FOLDER)) {// process special cases - types with [inner] folders 
//					LOG.info("folder type: secondName {}",StringUtils.secondName(name));
					
					// path = aura/TestComponent/TestComponent.cmp
					// name = TestComponent.cmp
//					if (!components.containsKey(sha)) {//add only unique components
						if (StringUtils.isAuraComp(path)) {// detect aura component by path
							putComponent(sha, AURA_BUNDLE, name, fSize, new File(path), path, comps);
						}
//					}
					continue;
				}

				if (fileMode.equals(BLOB)) {
					// omit aura inner elements
					if (StringUtils.isAuraComp(path)) continue;
					// omit meta files TODO: verify FOLDER type
					
					//process general/inner type
					
//					if (!components.containsKey(sha)) {//add only unique components
						String dir = StringUtils.getCompDir(path);
						String cType = CompType.getType(dir);
						if (cType == null) continue;
						// correct type and name for territory types
						if (StringUtils.isMetaFile(name)){
							cType += "(meta)";	
						}
//						LOG.info("cType: {}",cType);
						String folder = StringUtils.getFolderName(path);
//						LOG.info("isFolder: {}",folder);
						
						if (folder != null){ 
							putComponent(sha, StringUtils.getFolderType(dir), folder, fSize, new File(path), path, comps);
						}else if (cType.contains(DOCUMENT)){
							putComponent(sha, cType, StringUtils.getDocComplexName(path), fSize, new File(path), path, comps);
						}else if (Territory2.isTerritory2(path)){
							cType = "Territory2";
							name = Territory2.getFullName(path, StringUtils.getCompName(name));
							putComponent(sha, cType, name, fSize, new File(path), path, comps);
						}else if (Territory2Rule.isTerritory2Rule(path)){
							cType = "Territory2Rule";
							name = Territory2Rule.getFullName(path, StringUtils.getCompName(name));
							putComponent(sha, cType, name, fSize, new File(path), path, comps);
						}else if (!FOLDERTYPES.contains(cType)){// all except email templates, Dashboard, Reports
							//first, verify is extension has the ending #<inner_set>
							String innerName;
//							LOG.info("isInner?: {}",name);
							if ((innerName = StringUtils.isInnerComp(name)) != null){
								// add inner as component with name (without suffix ) and type defined by ext (extracted from s)
//								LOG.info("innerName: {}",innerName);
								cType = CompType.getTypebyExt(StringUtils.getExt(name));
								putComponent(sha, cType, innerName, fSize, new File(path), path, comps);
							}else{// not inner, verify ext
//								LOG.info("No ? {} = {}",CompType.getExt(cType), StringUtils.getExt(name));
								if (!StringUtils.isMetaFile(name)){
									if (!CompType.getExt(cType).equals( StringUtils.getExt(name) )){
										cType = NOTSF;
									}
								}
								putComponent(sha, cType, StringUtils.getCompName(name), fSize, new File(path), path, comps);
							}
						}else{// complex names
							putComponent(sha, cType, StringUtils.getCompComplexName(path), fSize, new File(path), path, comps);
						}
						
//					}

				}
			}
			// end of walk
		}
		// Long end = Instant.now().toEpochMilli();
		// LOG.info("Benchmark:getComponents: {} ms",end-start);
		LOG.info("listOfSize: {}",comps.size());
		return comps;
	}
	
	private void putComponent(String sha, String compType, String compName, Long blobSize, File path, String relPath, List<Component> comps){
		Component c = new Component(sha, compType, compName, blobSize, path, relPath);
//		LOG.info("put: {}",compName);
		comps.add(c);
//		components.put(sha,	c);
	}
	
	

	
	/**
	 * Returns a payload with selected component's context
	 * 
	 * NB: before executing this operation checkout appropriate commit 
	 * 
	 */
	public String downloadZipPack(Payload p) throws IOException, GitAPIException, UnsupportedEncodingException{
		LOG.info("returns pack of components with  type:{}",p.getCompType());
		checkoutLocalCommit(p.getCommitId());
		return ComponentOperations.createPackage(p.getItems(), p.getCompType(), repo.getPath());
	}
	
	/**
	 *  getter methods
	 * @return descriptor for git object from cache
	 * NB: in order these methods work, call getBranches(), getCommits first
	 * @throws GitAPIException 
	 * @throws IOException 
	 */

	public Branch getBranchbyId(String sha) throws IOException, GitAPIException{
		if (commits.isEmpty() && branches.containsKey(sha)){
			getCommits(branches.get(sha).getCommits());
			return branches.get(sha);
		}
		return null;
	}
	

	public Commit getCommitbyId(String sha){
		LOG.info("commits size = {}",commits.size());
		LOG.info("commits ({})? {}",sha,commits.containsKey(sha));
		return commits.get(sha);
	}
	
	public List<Commit> getCommitFromBranch(String branchSha) throws IOException, GitAPIException{
		Branch b = getBranchbyId(branchSha);
		return getCommits(b == null ? null : b.getCommits());
	}

	public List<Commit> getCommitFromBranch(String branchSha, String commitSha) throws IOException, GitAPIException{
		Branch b = getBranchbyId(branchSha);
		return getSingleCommit(b == null ? null : b.getCommits(),commitSha);
	}

	public List<Component> getComponentsFromCommit(String commitSha) throws IOException, GitAPIException{
//		Commit c = getCommitbyId(commitSha);
		return getComponents(getCommit(commitSha));
	}
	

	/**
	 * TODO: add creds support
	 * Introduced for Integration wit SF
	 * Must be called only when repository exists on the disk
	 * @return map of branches status commit descriptors
	 * @throws GitAPIException 
	 * @throws IOException 
	 */
	public Map<String, CommitDigest> ShowBranchTrackingStatus() throws GitAPIException, IOException{
		Map<String, CommitDigest> resultMap = new HashMap<String, CommitDigest>();
			GitCredentials gc = repo.getGitCredentials();
			String username = gc.getUsername();
			String password = gc.getPassword();
			FetchCommand fetch = repo.getGit().fetch().setCheckFetchedObjects(true).setProgressMonitor(new TextProgressMonitor(gitLog));
			
			if (gc.getProtocol().equals(ServiceConst.P_HTTPS)){
				fetch.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
			}else if (gc.getProtocol().equals(ServiceConst.P_SSH)){
				fetch.setTransportConfigCallback(GitFactory.createTransportConfigCallback(gc));
			}else{// unsupported protocol
				return resultMap;
			}

    		FetchResult result = fetch.call();
            LOG.info("Messages: " + result.getMessages());
            for (TrackingRefUpdate upd : result.getTrackingRefUpdates()){
            	LOG.info("upd {} | newId {}",upd.getLocalName(),upd.getNewObjectId().name());
            	String branchName = upd.getLocalName();
            	String sha = upd.getNewObjectId().name();
            	CommitDigest details = getCommitDetails(sha);
            	if (details != null){
            		resultMap.putIfAbsent(branchName, details);
            	}
            }
        return resultMap;   
	}
	
    private static List<Integer> getCounts(org.eclipse.jgit.lib.Repository repository, String branchName) throws IOException {
        BranchTrackingStatus trackingStatus = BranchTrackingStatus.of(repository, branchName);
        List<Integer> counts = new ArrayList<>();
        if (trackingStatus != null) {
            counts.add(trackingStatus.getAheadCount());
            counts.add(trackingStatus.getBehindCount());
            LOG.info("{}: ahead {}, behind: {}",trackingStatus.getRemoteTrackingBranch(),trackingStatus.getAheadCount(), trackingStatus.getBehindCount());
        } else {
            LOG.info("Returned null, likely no remote tracking of branch " + branchName);
            counts.add(0);
            counts.add(0);
        }
        return counts;
    }



}
