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

public class Territory2Model {

	private static final Logger LOG = LoggerFactory.getLogger(Territory2.class);
	private static final String TERRITORY2MODEL = "Territory2Model";
	private static final char FILE_SEP = File.separatorChar;
	
	public Territory2Model() {
	}
	
	public static String createTerritory2ModelPackage(final List<MetaItem> names, File repoDir) 
			throws UnsupportedEncodingException, IOException {
		XmlPack xmlPack = new XmlPack();
		List<ZEntry> files = new ArrayList<ZEntry>();
		String path;
		File f;
		// firstly add component's directory path relatively repository dir
		final String baseDir = CompType.getDir(TERRITORY2MODEL);
		final String ext = CompType.getExt(TERRITORY2MODEL);
		Set<String> dirSet = new HashSet<String>();
		// begin block in package.xml
		xmlPack.openTypesEntry();
		IOLogger log = new IOLogger();
		int fails = 0;

		if (names.size() > 0) {
			for (MetaItem mi : names) {
				files.clear();
				String name = mi.getName();
				String dirName = StringUtils.getCompDir(name);
				if (!dirSet.contains(dirName)){// add directory to package -  once per component
					dirSet.add(dirName);
					xmlPack.createMemberEntry(dirName);
				}
				// create new [relative] path of component to add like
				path = baseDir + name + FILE_SEP + name + ext;
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

				LOG.info(" form zip for territory2model ={}",name);
				mi.setCrc32(files.get(0).getCrc32());
			}
		}
		log.download(TERRITORY2MODEL, names.size() - fails);
		return log.getLog();

	}


}
