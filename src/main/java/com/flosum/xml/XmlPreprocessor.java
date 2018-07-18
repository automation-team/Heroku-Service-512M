package com.flosum.xml;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.utils.StringUtils;
import com.flosum.utils.ZipUtils;
import com.flosum.constants.CompType;

public class XmlPreprocessor {
	
	private final static  Logger LOG = LoggerFactory.getLogger(XmlPreprocessor.class);

	private static final String INNER_TOKEN = "#";
	private static final String CustomLabels = "CustomLabels";
	private Long curOperation;
	private Map<String,List<File>> filesMap;
	private Map<String,String> typesMap;
	private List<File> toDelete;
	
	public XmlPreprocessor(){
		filesMap = new HashMap<String,List<File>>();
		typesMap = new HashMap<String,String>();
		toDelete = new ArrayList<File>();
	}
	
	public Boolean add(String name, String type, File f){
		String parentName = StringUtils.getParentName(name);
		String masterType = CompType.getParentType(type);
		if (CustomLabels.equals(masterType)) {
			parentName = CustomLabels;
		}
		String key = masterType + "#" + parentName;
//		LOG.info("add: name {}, parent name {} key {} f {}",name,parentName,key,f);
		if (CompType.isInner(type)){// do not process master types - no need
			if (!filesMap.containsKey(key)){
				filesMap.put(key, new ArrayList<File>());// map Knowledge=>[Knowledge#1.rule]
			}
			filesMap.get(key).add(f);
			toDelete.add(f);
			if (!typesMap.containsKey(key)){// only master types
				typesMap.put(key, masterType);
			}
		}
		return true;
	}
	
	// Stitches together
	public void compileAll() throws UnsupportedEncodingException, IOException{

		LOG.info("compileAll: filesMap {}",filesMap.size());
		if (filesMap.isEmpty()) return;// nothing to do
		
		for (String name : filesMap.keySet()){
			List<File> toCompile = filesMap.get(name);
			if (toCompile.isEmpty()) continue;
			File masterFile = getMaster(toCompile.get(0));
//			LOG.info("masterFile {}",masterFile);
//			LOG.info("masterFile exists? {}",masterFile.exists());
			if (typesMap.containsKey(name)){
				XmlParser parser = new XmlParser(typesMap.get(name));
				if (masterFile.exists()){
					parser.setOriginalXml(ZipUtils.createPlainPack(masterFile, null));
					parser.extractTags();
				}
				for (File f : toCompile){
//					LOG.info("f {}",f);
//					LOG.info("f exists? {}",f.exists());
					if (f.exists()){
						parser.setOriginalXml(ZipUtils.createPlainPack(f, null));
						parser.extractTags();
					}
				}
				ZipUtils.write(parser.compileXml(), masterFile);
			}
		}
	}
	
	public File getMaster(File f){
		String path = f.toString();
		Integer end = path.indexOf(INNER_TOKEN);
		if (end < 0){
			return f;
		}
		return new File(path.substring(0, end));
	}
	
	// this method cleans up all inner components saved earlier
	public void cleanStageArea(){
		LOG.info("cleanStageArea {}",toDelete.size());
		if (toDelete.isEmpty()) return;
		for (File f : toDelete){
			f.delete();
		}
	}
	
	public void init(Long opId){
		curOperation = opId;
		reset();
	}
	
	public void reset(){
		filesMap.clear();
		typesMap.clear();
		toDelete.clear();
	}


}
