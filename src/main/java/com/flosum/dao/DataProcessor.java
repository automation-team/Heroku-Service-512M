package com.flosum.dao;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.constants.CompType;
import com.flosum.constants.PMDConstants;
import com.flosum.constants.ServiceConst;
import com.flosum.model.MetaItem;
import com.flosum.model.Payload;
import com.flosum.utils.StringUtils;
import com.flosum.utils.ZipUtils;

import net.sourceforge.pmd.PMD;

public class DataProcessor {

	private final static  Logger LOG = LoggerFactory.getLogger(DataProcessor.class);
	private static final  String TEMP_DIR = File.separatorChar + "tmp";
	private final  String TEMP_DIR2;
	private static final AtomicLong generateID = new AtomicLong();

	private ReportGenerator report;
	private final String COMPONENT_TYPE;
	private final Long opId;
	private final File LOCAL_PATH;

	private final String APEXCLASS = "ApexClass";
	private final String VFPAGE = "ApexPage";
	private final String APEXTRIGGER = "ApexTrigger";

	public DataProcessor(String type,Long opId){
		COMPONENT_TYPE = type;
		this.opId = opId;
		report = new ReportGenerator(type);
		LOCAL_PATH = setDirectory();
		if (LOCAL_PATH != null){
			TEMP_DIR2 =  LOCAL_PATH.toString() + File.separatorChar;
		}else{
			TEMP_DIR2 = null;
		}
	}

	public Integer processComponents(Payload p) throws Exception{
		Integer result = -1;
		if (p == null || p.getItems() == null) return result;
//		LOG.debug("LOCAL_PATH:{}",LOCAL_PATH.toString());
//		LOG.debug("items:{}",p.getItems().size());
		
		if (!CompType.isSupported(COMPONENT_TYPE)){
			throw new Exception("Unsupported type");
		}
		if (LOCAL_PATH == null){
			throw new Exception("Allocating memory error");
		}
//		LOG.debug("init vars:");
		String filesToProcess = "";
//		LOG.debug("filesToProcess:{}",filesToProcess);
		String inputfileName = TEMP_DIR2 + "in" + generateID.get();
//		LOG.debug("inputfileName:{}",inputfileName);
		String output_format = "text";
//		LOG.debug("output_format:{}",output_format);
		String rulesets = getRulesets();
//		String output_file = CacheService.getPath(CacheService.RESULTS + p.getOpId());
//		LOG.debug("rulesets:{}",rulesets);
		String output_file = "data" + (CacheService.RESULTS +  opId) + ".cache";
//		LOG.debug("output_file:{}",output_file);
//		LOG.debug("starting...");
		File f = new File(inputfileName);
		Boolean isEmptyList = true;
		for (MetaItem mi : p.getItems()){
			if (mi.getDataStr() == null) continue;// omit null values
			Map<String,String> compMap = ZipUtils.unpackZip(mi.getDataStr(),LOCAL_PATH); 
			if (compMap != null && compMap.size() > 0){
				for (String filename : compMap.keySet()){
					LOG.debug("processing:{}",filename);
					if (!StringUtils.isMetaFile(filename)){
						if (filesToProcess == ""){
							filesToProcess = TEMP_DIR2 + filename;
						}else{
							filesToProcess += ("," + TEMP_DIR2 + filename);
						}
						isEmptyList = false;
					}
				}
			}
		}
//		LOG.debug("filesToProcess:{}",filesToProcess);
		if (filesToProcess != ""){
			ZipUtils.write(filesToProcess, f);
		}
//		LOG.debug("inputfileName:{}",inputfileName);
		if (!isEmptyList){
//			LOG.debug("output_file:{}",output_file);

			//testing PMD
			LOG.debug("start testing PMD");
			String[] arguments = { "-filelist", inputfileName, "-f", output_format, "-R", rulesets, "-r", output_file }; 
			result = PMD.run(arguments);
			LOG.debug("finish testing PMD:{}",result);
		}
		return result;

	}
	
	private static String getRulesets(){
		String ruleSets = "";
		for (String rule : PMDConstants.getApexRulesets()){
			if (ruleSets == ""){
				ruleSets = rule;
			}else{
				ruleSets += ("," + rule);
			}
		}
		for (String rule : PMDConstants.getVfRulesets()){
			if (ruleSets == ""){
				ruleSets = rule;
			}else{
				ruleSets += ("," + rule);
			}
		}
		return ruleSets;
	}
	
	private static String getPath(Long id){
		return TEMP_DIR + id + ".cache";
	}

	
	public String genReport(){
		return report.genReport();
	}

	public File setDirectory(){
		try {
			File localPath = File.createTempFile(TEMP_DIR, "");
			if (!localPath.delete()) {
				LOG.info("ERROR: Could not delete temporary file {}", LOCAL_PATH);
				return null;
			}
			return localPath;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	public String getTempDirectory() {
		return TEMP_DIR2;
	}
	
	
}
