package com.flosum.sfcomponents;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.constants.CompType;
import com.flosum.model.Component;
import com.flosum.model.MetaItem;
import com.flosum.model.ZEntry;
import com.flosum.utils.IOLogger;
import com.flosum.utils.XmlPack;
import com.flosum.utils.ZipUtils;

public class CustomLabels {

	private static final Logger LOG = LoggerFactory.getLogger(CustomLabels.class);
	private static final String CUSTOM_LABELS = "CustomLabel";

	public CustomLabels() {
	}

	public static String createLabelsPackage(final List<MetaItem> names, File repoDir) 
			throws UnsupportedEncodingException, IOException {
		XmlPack xmlPack = new XmlPack();
		List<ZEntry> files = new ArrayList<ZEntry>();
		String path, baseDir, ext;
		File f;
		// firstly add component's directory path relatively repository dir
		baseDir = CompType.getDir(CUSTOM_LABELS);
		ext = CompType.getExt(CUSTOM_LABELS);
		// begin block in package.xml
		xmlPack.openTypesEntry();
		IOLogger log = new IOLogger();
		int fails = 0;

		if (names.size() > 0) {
			for (MetaItem mi : names) {
				// create list of files add to
				files.clear();
				String name = "CustomLabels";
				// create new [relative] path of component to add
				path = baseDir + name + ext;
				// create new [absolute] path to add
				f = new File(repoDir, path);
				files.add(new ZEntry(ZEntry.FileType.FILE, path, f,path));
				LOG.info("add file: absolute:{}, relative: {}", f, path);
				// add member's names from ids
				try{
					ZipUtils.createZipPack(files,mi);
				}catch(Exception e){
					fails++;
					log.error(e.getMessage());
				}
				LOG.info(" form zip for path={}",path);
			}
		}
		log.download(CUSTOM_LABELS, names.size() - fails);
		return log.getLog();
	}

}
