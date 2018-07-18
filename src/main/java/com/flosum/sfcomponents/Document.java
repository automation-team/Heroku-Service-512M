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

public class Document {
	
	private static final Logger LOG = LoggerFactory.getLogger(Document.class);
	private static final String DOCUMENT = "Document";


	public Document() {
	}

	public static String createDocumentPackage(final List<MetaItem> names, File repoDir) 
			throws UnsupportedEncodingException, IOException {
		XmlPack xmlPack = new XmlPack();
		List<ZEntry> files = new ArrayList<ZEntry>();
		String path, baseDir, cname, cnameext, compType;
		File f, folder;
		// firstly add component's directory path relatively repository dir
		baseDir = CompType.getDir(DOCUMENT);
		compType = CompType.getName(DOCUMENT);
		// begin block in package.xml
		xmlPack.openTypesEntry();
		IOLogger log = new IOLogger();
		int fails = 0;

		if (names.size() > 0) {
			for (MetaItem mi : names) {
				files.clear();
				// create new [relative] path of component to add
				// NOTE: ext must already included in name!!!
				// path = documents/
				path = baseDir + mi.getName();
				// create new [absolute] path to add
				File fileinfolder = new File(repoDir, path);
//				String nameplusext = findFile(folder.listFiles(), name);
//				if (nameplusext == null)
//					continue;
//				path = baseDir + nameplusext;
				// now path like documents/AccountingFolder/Logo.gif
//				f = new File(repoDir, path);
				files.add(new ZEntry(ZEntry.FileType.FILE, path, fileinfolder, path));
				LOG.info("add file: absolute:{}, relative: {}", fileinfolder, path);
				// this component comes with meta file always
				path += "-meta.xml";
				f = new File(repoDir, path);
				files.add(new ZEntry(ZEntry.FileType.FILE, path, f, path));
				LOG.info("add file: absolute:{}, relative: {}", f, path);
				try{
					ZipUtils.createZipPack(files,mi);
				}catch(Exception e){
					fails++;
					log.error(e.getMessage());
				}

			}
		}
		log.download(DOCUMENT, names.size() - fails);
		return log.getLog();

	}
	
	//returns full name for fname
	private static String findFile(File[] list, String fname) {
		for (File f : list) {
			String name = f.getName();
			if (name.indexOf(fname) != -1) // full name must incl. truncated
				return name;
		}
		return null;
	}


}
