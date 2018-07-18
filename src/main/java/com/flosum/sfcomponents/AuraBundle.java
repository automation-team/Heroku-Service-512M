package com.flosum.sfcomponents;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.constants.CompType;
import com.flosum.model.MetaItem;
import com.flosum.model.ZEntry;
import com.flosum.utils.IOLogger;
import com.flosum.utils.XmlPack;
import com.flosum.utils.ZipUtils;

public class AuraBundle {

	private static final Logger LOG = LoggerFactory.getLogger(AuraBundle.class);

	private static final String AURA_BUNDLE = "AuraDefinitionBundle";
	private static final char FILE_SEP = File.separatorChar;

	public AuraBundle() {
	}

	public static String createAuraPackage(final List<MetaItem> names, File repoDir)
			throws UnsupportedEncodingException, IOException {

		XmlPack xmlPack = new XmlPack();
		List<ZEntry> files = new ArrayList<ZEntry>();
		String path, baseDir;
		// firstly add component's directory path relatively repository dir
		baseDir = CompType.getDir(AURA_BUNDLE);
		File folder;
		// begin block in package.xml
		xmlPack.openTypesEntry();
		// open new log for  
		IOLogger log = new IOLogger();
		int fails = 0;
		if (names.size() > 0) {
			for (MetaItem mi : names) {
				files.clear();
				// create new [relative] path of bundle's directory to add. f.e.
				// aura/name
				path = baseDir + mi.getName();
				// create new [absolute] path of directory
				// must be like /aura/name
				folder = new File(repoDir, path);
				// get list of all files in this dir, add them as bundle
				File[] bundleFiles = folder.listFiles();
				for (File f : bundleFiles) {
					// form path like aura/name/comp.js
					String relPath = path + FILE_SEP + f.getName();
					files.add(new ZEntry(ZEntry.FileType.FILE, relPath, f, relPath));
					LOG.info("add file: absolute:{}, relative: {}", f, relPath);
				}
				try{
					ZipUtils.createZipPack(files,mi);
					LOG.info(" form zip for path={}",path);
					LOG.info("added a bundle from {} files", files.size());
				}catch(Exception e){
					fails++;
					log.error(e.getMessage());
				}
			}
		}
		log.download(AURA_BUNDLE, names.size() - fails);
		return log.getLog();
	}

}
