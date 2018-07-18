package com.flosum.sfcomponents;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.constants.CompType;
import com.flosum.model.MetaItem;
import com.flosum.model.ZEntry;
import com.flosum.utils.IOLogger;
import com.flosum.utils.StringUtils;
import com.flosum.utils.XmlPack;
import com.flosum.utils.ZipUtils;

public class Territory2 {
	
	private static final Logger LOG = LoggerFactory.getLogger(Territory2.class);
	private static final String TERRITORY2 = "Territory2";
	private static final String TERRITORY2_ROOT_DIR = "territory2Models/";
	private static final String TERRITORY2_DIR = "/territories/";
	
	public Territory2() {
	}
	
	public static String createTerritory2Package(final List<MetaItem> names, File repoDir) 
			throws UnsupportedEncodingException, IOException {
		XmlPack xmlPack = new XmlPack();
		List<ZEntry> files = new ArrayList<ZEntry>();
		String path;
		File f;
		// firstly add component's directory path relatively repository dir
		final String baseDir = CompType.getDir(TERRITORY2);
		final String ext = CompType.getExt(TERRITORY2);
		Set<String> dirSet = new HashSet<String>();
		// begin block in package.xml
		xmlPack.openTypesEntry();
		IOLogger log = new IOLogger();
		int fails = 0;

		if (names.size() > 0) {
			for (MetaItem mi : names) {
				files.clear();
				String name = mi.getName();
				String mainName = StringUtils.getParentName(name);
				if (mainName == null) mainName = name;
				String territoryName = StringUtils.getInnerName(name);
				if (territoryName == null) territoryName = name;
				String dirName = StringUtils.getCompDir(name);
				if (!dirSet.contains(dirName)){// add directory to package -  once per component
					dirSet.add(dirName);
					xmlPack.createMemberEntry(dirName);
				}
				// create new [relative] path of component to add like
				path = baseDir + mainName + TERRITORY2_DIR + territoryName + ext;
				// create new [absolute] path to add
				f = new File(repoDir, path);
				files.add(new ZEntry(ZEntry.FileType.FILE, path, f, path));
				LOG.info("add file: absolute:{}, relative: {}", f, path);
				try{
					ZipUtils.createZipPack(files,mi);
				}catch(Exception e){
					fails++;
					log.error(e.getMessage());
				}

				LOG.info(" form zip for territory2 ={}",name);
				mi.setCrc32(files.get(0).getCrc32());
			}
		}
		log.download(TERRITORY2, names.size() - fails);
		return log.getLog();

	}

	// extracts territory2Models/Test/territories/name.territory2 -> Test.name
	
	public static Boolean isTerritory2(String path){
		if (path == null || path == "") return false;
		if (!path.startsWith(TERRITORY2_ROOT_DIR) || !path.contains(TERRITORY2_DIR)) return false;
		return true;
	}

	public static String getFullName(String path, String name){
		if (path == null || path == "") return null;
		if (!path.startsWith(TERRITORY2_ROOT_DIR) || !path.contains(TERRITORY2_DIR)) return null;
		return path.substring(TERRITORY2_ROOT_DIR.length(), path.indexOf(TERRITORY2_DIR)) + "." + name;
	}

	
}
