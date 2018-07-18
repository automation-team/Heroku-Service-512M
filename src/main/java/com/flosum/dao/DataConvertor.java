package com.flosum.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.flosum.constants.CompType;
import com.flosum.model.Branch;
import com.flosum.model.BranchDigest;
import com.flosum.model.Commit;
import com.flosum.model.CommitDigest;
import com.flosum.model.Component;
import com.flosum.model.ComponentDigest;
import com.flosum.model.RepoDescr;
import com.flosum.model.RepoDescrDigest;
import com.flosum.utils.StringUtils;
import com.flosum.utils.ZipUtils;


public class DataConvertor {
	private static final Logger LOG = LoggerFactory.getLogger(DataConvertor.class);
	private static final char INNER_TOKEN = '#';
	private static final String EMPTYJSON = "{}";
	
	public static RepoDescrDigest repoDescr2Digest(RepoDescr rd){
		return new RepoDescrDigest(rd.getId(),rd.getUrl(), null, rd.getPath().getPath());
	}
	
	public static List<BranchDigest> Branch2Digest(List<Branch> lc){
		List<BranchDigest> lcd = new ArrayList<BranchDigest>();
		if (lc == null) return lcd;
		for(Branch c: lc){
			lcd.add(new BranchDigest(c));
		}
		return lcd;
	}

	public static List<CommitDigest> Commit2Digest(List<Commit> lc){
		List<CommitDigest> lcd = new ArrayList<CommitDigest>();
		if (lc == null) return lcd;
		for(Commit c: lc){
			lcd.add(new CommitDigest(c));
		}
		return lcd;
	}
	
	public static List<ComponentDigest> Component2Digest(List<Component> lc){
		List<ComponentDigest> lcd = new ArrayList<ComponentDigest>();
		if (lc == null) return lcd;
		for(Component c: lc){
			lcd.add(new ComponentDigest(c));
		}
		return lcd;
	}
	
	//converts (Account.field1, CustomField) -> [objects/][Account][.object][$][field1][.field] 
	public static String getInnerPath(String compName, String compType){
		return CompType.getDir(compType)+ StringUtils.getParentName(compName) + CompType.getParentExt(compType) + INNER_TOKEN +
				StringUtils.getInnerName(compName)  + CompType.getExt(compType);
	}
	
	// Convert Java object to JSON
	public static String listStringToJson(List<String> lst){
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(lst);
		} catch (JsonProcessingException e) {
			LOG.info("Json error,{}",e.getMessage());
			return EMPTYJSON;
		} 
	}

	// Convert Java object to JSON
	public static String listBranchToJson(List<BranchDigest> lst){
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(lst);
		} catch (JsonProcessingException e) {
			LOG.info("Json error,{}",e.getMessage());
			return EMPTYJSON;
		} 
	}

	
	// Convert Java object to JSON
	public static String listCommitToJson(List<CommitDigest> lst){
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(lst);
		} catch (JsonProcessingException e) {
			LOG.info("Json error,{}",e.getMessage());
			return EMPTYJSON;
		} 
	}
	// Convert Java object to JSON
	public static String listCompToJson(List<ComponentDigest> lst){
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(lst);
		} catch (JsonProcessingException e) {
			LOG.info("Json error,{}",e.getMessage());
			return EMPTYJSON;
		} 
	}
	
	// these 3 methods used to convert Java objects <-> json
	public static List<String> jsonToStringList(final String str){
		ObjectMapper mapper = new ObjectMapper(); 
		try {
			return mapper.readValue(str, new TypeReference<List<String>>(){});
		} catch (IOException e) {
			LOG.info("Json error,{}",e.getMessage());
			return new ArrayList<String>();
		}
	}
	
	public static List<String> base64encJsonToStringList(final String base64str){
		byte[] base64decodedBytes = Base64.getDecoder().decode(base64str);
		List<String> lst  = new ArrayList<String>(); 
		if (base64decodedBytes != null){
			lst = jsonToStringList(base64decodedBytes.toString());
		}
		return lst; 
	}
	
	public static List<ComponentDigest> jsonToComponentDigestList(final String str){
		ObjectMapper mapper = new ObjectMapper(); 
		try {
			return mapper.readValue(str, new TypeReference<List<ComponentDigest>>(){});
		} catch (IOException e) {
			LOG.info("Json error,{}",e.getMessage());
			return new ArrayList<ComponentDigest>();
		}
	}

	

}
