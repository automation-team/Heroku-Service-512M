package com.flosum.dao;

import java.io.File;
import java.io.IOException;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.eclipse.jgit.lib.Repository;

/**
 * This class used to perform disk operations like clear work dir, etc.
 *
 */
public class DiskService {

	final private Path workingDir;
	final private String bkUpDir;

	public DiskService(Repository repo) {
		workingDir = repo.getDirectory().toPath();
		bkUpDir = null;
	}

	public static void deleteWorktree(final Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return dir.equals(path) ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
			}

			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (!dir.equals(path))
					Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	public static void deleteFiles(final File rootDir, final List<String> list) {
		for (String path : list) {
			File f = new File(rootDir, path);
			if (f.isFile() && f.exists()) {
				f.delete();
			}
		}
	}

}
