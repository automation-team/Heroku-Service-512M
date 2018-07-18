package com.flosum.sfcomponents;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.constants.CompType;
import com.flosum.model.MetaItem;
import com.flosum.model.ZEntry;
import com.flosum.utils.IOLogger;
import com.flosum.utils.XmlPack;
import com.flosum.utils.ZipUtils;

public class GeneralComponent {
	private static final Logger LOG = LoggerFactory.getLogger(GeneralComponent.class);

	private static final Set<String> FOLDERTYPES = Collections.unmodifiableSet(new HashSet<String>() {
		private static final long serialVersionUID = 1482574588167253024L;
		{
			add("DocumentFolder");
			add("EmailFolder");
			add("DashboardFolder");
			add("ReportFolder");
		}
	});

	public GeneralComponent() {
	}
	
	public static String createStandartPackage(final List<MetaItem> names, final String compType, final File repoDir)
			throws IOException, UnsupportedEncodingException {
		XmlPack xmlPack = new XmlPack();
		List<ZEntry> files = new ArrayList<ZEntry>();
		String path = null, baseDir, ext, metaExt;
		File f;
		// firstly add component's directory path relatively repository dir in the form objects/
		baseDir = CompType.getDir(compType);
		// f.e. .object
		ext = CompType.getExt(compType);
		// used if needed, f.e. .page-meta.xml
		metaExt = CompType.getMetaExt(compType);
		// begin a block in package.xml
		xmlPack.openTypesEntry();
		IOLogger log = new IOLogger();
		int fails = 0;

		if (names.size() > 0) {
			for (MetaItem mi : names) {
				files.clear();
				String name = mi.getName();
				if (!FOLDERTYPES.contains(compType)){
					// create new [relative] path of component to add
					path = baseDir + name + ext;
					// create new [absolute] path to add
					f = new File(repoDir, path);
					// for standart object set zpath the same as real
					files.add(new ZEntry(ZEntry.FileType.FILE, path, f, path));
					LOG.info("add file: absolute:{}, relative: {}", f, path);
				}
				// check, is component comes with meta file
				if (CompType.isPair(compType)) {
					path = baseDir + name + metaExt;
					f = new File(repoDir, path);
					files.add(new ZEntry(ZEntry.FileType.FILE, path, f, path));
					LOG.info("add file: absolute:{}, relative: {}", f, path);
				}
				try{
					ZipUtils.createZipPack(files,mi);// if I/O errors, execution will be stopped here
				}catch(Exception e){
					fails++;
					log.error(e.getMessage());
				}

				LOG.info(" form zip for path={}, component = {}",path, name);
				if (files.size() == 1){
					mi.setCrc32(""+files.get(0).getCrc32());
				}else if (files.size() == 2){
					mi.setCrc32(files.get(0).getCrc32() + " " + files.get(1).getCrc32());
				}
			}
		}
		log.download(compType, names.size() - fails);
		return log.getLog();

	}

	public static void unpackGeneralTypes(String zip, String compType, File repoDir) 
			throws UnsupportedEncodingException, IOException {
			ZipUtils.unpackZip(zip, repoDir,compType, null);
	}


}
