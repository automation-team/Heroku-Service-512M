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

public class EmailTemplate {
	
	private static final Logger LOG = LoggerFactory.getLogger(EmailTemplate.class);
	private static final String EMAIL_TEMPLATE = "EmailTemplate";


	public EmailTemplate() {
	}

	public static String createEmailPackage(final List<MetaItem> names, File repoDir) 
			throws UnsupportedEncodingException, IOException {
		XmlPack xmlPack = new XmlPack();
		List<ZEntry> files = new ArrayList<ZEntry>();
		String path;
		File f;
		// firstly add component's directory path relatively repository dir
		final String baseDir = CompType.getDir(EMAIL_TEMPLATE);
		final String ext = CompType.getExt(EMAIL_TEMPLATE);
		final String metaExt = CompType.getMetaExt(EMAIL_TEMPLATE);
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
				// email/unfiled$public/CommunityWelcomeEmailTemplate.email
				path = baseDir + name + ext;
				// create new [absolute] path to add
				f = new File(repoDir, path);
				files.add(new ZEntry(ZEntry.FileType.FILE, path, f, path));
				LOG.info("add file: absolute:{}, relative: {}", f, path);
				// check, is component comes with meta file
				path = baseDir + name + metaExt;
				f = new File(repoDir, path);
				files.add(new ZEntry(ZEntry.FileType.FILE, path, f, path));
				LOG.info("add file: absolute:{}, relative: {}", f, path);
				try{
					ZipUtils.createZipPack(files,mi);
				}catch(Exception e){
					fails++;
					log.error(e.getMessage());
				}

				LOG.info(" form zip for email template={}",name);
				if (files.size() == 2){
					mi.setCrc32(files.get(0).getCrc32() + " " + files.get(1).getCrc32());
				}
			}
		}
		log.download(EMAIL_TEMPLATE, names.size() - fails);
		return log.getLog();

	}

}
