package com.flosum.dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.model.BranchDigest;
import com.flosum.model.CommitDigest;
import com.flosum.model.ComponentDigest;
import com.flosum.model.DataWrapper;

/**
 *	Used to auto split big lists on smaller parts 
 *
 */
public class DataManager {

	private final Logger LOG = LoggerFactory.getLogger(DataManager.class);
	private final CacheService cache;
	private final Integer chunkSize = 300;
	private final Integer branchChunkSize = 500;

	public DataManager(CacheService cache) {
		this.cache = cache;
	}
	
	public DataWrapper getCachedData(final Long opId, final Integer part) throws FileNotFoundException, IOException{
		Object o = cache.pop(CacheService.RAWDATA, opId, part);
		if (o != null){
			return (DataWrapper) o;
		}
		return null;
	}
	
	/**
	 * Splits long list, returns the 1st part, caches all the rest on disk
	 * @param branchLi
	 * @param opId
	 * @return DataWrapper with 1st part of data (which is the last, if flag isNext does not set)
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public DataWrapper split(List<BranchDigest> branchLi, final Long opId) throws FileNotFoundException, IOException{
		if (branchLi == null || branchLi.size() == 0){
			return new  DataWrapper(opId);
		}
		// do not cache the 1st part
		if (branchLi.size() < branchChunkSize + 1){
			return new DataWrapper(opId, 1, DataConvertor.listBranchToJson(branchLi), false);
		}
		LOG.info("branchLi size = {}", branchLi.size());
		Integer nChunks = (branchLi.size() - 1)/branchChunkSize + 1;
		Integer chunk = 1;
		while(chunk < nChunks){
			Integer start = chunk*branchChunkSize;
			Integer end = chunk == (nChunks - 1) ?  branchLi.size() : (chunk + 1)*branchChunkSize; 
			LOG.info("branchLi start = {}, end = {}", start, end);
			DataWrapper dw = new DataWrapper(opId, nChunks, DataConvertor.listBranchToJson(branchLi.subList(start, end)), true);
			if (chunk == (nChunks - 1) ){
				dw.setIsNext(false);
			}
			cache.push(dw, CacheService.RAWDATA, opId, chunk);
			chunk++;
		}
		// form wrapper to return
		return new DataWrapper(opId, nChunks, DataConvertor.listBranchToJson(branchLi.subList(0, chunkSize)), true);
	}

	/**
	 * Splits long list, returns the 1st part, caches all the rest on disk
	 * @param branchLi
	 * @param opId
	 * @return DataWrapper with 1st part of data (which is the last, if flag isNext does not set)
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public DataWrapper split(List<CommitDigest> commitLi, final Long opId, Boolean b) throws FileNotFoundException, IOException{
		if (commitLi == null || commitLi.size() == 0){
			return new  DataWrapper(opId);
		}
		// do not cache the 1st part
		if (commitLi.size() < chunkSize + 1){
			return new DataWrapper(opId, 1, DataConvertor.listCommitToJson(commitLi), false);
		}
		LOG.info("commitLi size = {}", commitLi.size());
		Integer nChunks = (commitLi.size() - 1)/chunkSize + 1;
		Integer chunk = 1;
		while(chunk < nChunks){
			Integer start = chunk*chunkSize;
			Integer end = chunk == (nChunks - 1) ?  commitLi.size() : (chunk + 1)*chunkSize; 
			DataWrapper dw = new DataWrapper(opId, nChunks, DataConvertor.listCommitToJson(commitLi.subList(start, end)), true);
			if (chunk == (nChunks - 1) ){
				dw.setIsNext(false);
			}
			cache.push(dw, CacheService.RAWDATA, opId, chunk);
			chunk++;
		}
		// form wrapper to return the 1st part
		return new DataWrapper(opId, nChunks, DataConvertor.listCommitToJson(commitLi.subList(0, chunkSize)), true);
	}

	/**
	 * Splits long list, returns the 1st part, caches all the rest on disk
	 * @param branchLi
	 * @param opId
	 * @return DataWrapper with 1st part of data (which is the last, if flag isNext does not set)
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public DataWrapper split(List<ComponentDigest> compLi, final Long opId, final Integer limit) throws FileNotFoundException, IOException{
		if (compLi == null || compLi.size() == 0){
			return new  DataWrapper(opId);
		}
		// do not cache the 1st part
		if (compLi.size() < chunkSize + 1){
			return new DataWrapper(opId, 1, DataConvertor.listCompToJson(compLi), false);
		}
		Integer nChunks = (compLi.size() - 1)/chunkSize + 1;
		Integer chunk = 1;
		while(chunk < nChunks){
			Integer start = chunk*chunkSize;
			Integer end = chunk == (nChunks - 1) ?  compLi.size() : (chunk + 1)*chunkSize; 
			DataWrapper dw = new DataWrapper(opId, nChunks, DataConvertor.listCompToJson(compLi.subList(start, end)), true);
			if (chunk == (nChunks - 1) ){
				dw.setIsNext(false);
			}
			cache.push(dw, CacheService.RAWDATA, opId, chunk);
			chunk++;
		}
		// form wrapper to return
		return new DataWrapper(opId, nChunks, DataConvertor.listCompToJson(compLi.subList(0, chunkSize)), true);
	}

}
