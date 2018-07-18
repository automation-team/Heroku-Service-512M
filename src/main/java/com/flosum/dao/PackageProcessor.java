package com.flosum.dao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.model.Payload;
import com.flosum.model.PayloadD;
import com.flosum.utils.ZipUtils;

/**
 * 
 * Used to perform operations with package
 * @author Alexey Kalutov
 *
 */

public class PackageProcessor {

	private final static  Logger LOG = LoggerFactory.getLogger(PackageProcessor.class);
	private static final  String TEMP_DIR = File.separatorChar + "tmp";

	private static final AtomicLong generateID = new AtomicLong();

	private File packageXml;
	private final Long opId;



	public PackageProcessor(Long opId){
		this.opId = opId;
	}
	
	

	/**
	 * Extracts all content of zip file to directory LOCAL_PATH
	 * Set the file path for package file
	 * returns list of unpacked files
	 *  
	 * @param p
	 * @param LOCAL_PATH
	 * @return
	 * @throws Exception
	 */
	public List<File> processComponents(PayloadD p, final File LOCAL_PATH) throws Exception{
		List<File> files = new ArrayList<File>();
		if (p == null || p.getBase64() == null) return files;
//		LOG.debug("LOCAL_PATH:{}",LOCAL_PATH.toString());

		LOG.debug("starting...");
		packageXml = ZipUtils.unpackZip(p.getBase64(), LOCAL_PATH, files);
//		LOG.debug("unpacked:{}",files);
//		LOG.debug("packageXml:{}",packageXml);
		return files;
	}
	
	
	
	public File getPackageFile(){
		return this.packageXml;
	}

	
	public static File setDirectory(){
		try {
			File localPath = File.createTempFile(TEMP_DIR, "");
			if (!localPath.delete()) {
				LOG.info("ERROR: Could not delete temporary file {}", localPath);
				return null;
			}
			return localPath;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	
}
