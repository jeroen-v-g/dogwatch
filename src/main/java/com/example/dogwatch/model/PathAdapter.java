package com.example.dogwatch.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.tapestry5.tree.TreeModelAdapter;
import org.apache.log4j.Logger;

public class PathAdapter implements TreeModelAdapter<Path> {

	private static final Logger logger = Logger.getLogger(PathAdapter.class);

	@Override
	public boolean isLeaf(Path value) {
		
		boolean hasDirectory = false;
		try {
			hasDirectory = Files.list(value).anyMatch(path -> Files.isDirectory(path));
		} catch (IOException e) {
			logger.error("Error isLeaf",e);
			hasDirectory = false;
		}
		return !hasDirectory;
	}

	@Override
	public boolean hasChildren(Path value) {
		boolean hasDirectory = false;
		try {
			hasDirectory = Files.list(value).anyMatch(path -> Files.isDirectory(path));
		} catch (IOException e) {
			logger.error("Error hasChildren",e);
			hasDirectory = false;
		}
		return hasDirectory;
	}

	@Override
	public List<Path> getChildren(Path value) {

		List<Path> children = null;
		try {
			children = Files.list(value).filter(path -> Files.isDirectory(path)).collect(Collectors.toList());
		} catch (IOException e) {
			logger.error("Error getChildren",e);
		}

		return children;
	}

	@Override
	public String getLabel(Path value) {
		return value.getFileName().toString();
	}

}
