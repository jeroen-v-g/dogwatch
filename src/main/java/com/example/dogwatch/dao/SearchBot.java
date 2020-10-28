package com.example.dogwatch.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

public class SearchBot implements Runnable {

	public final List<String> found = new ArrayList<>();
	private static final Logger log = Logger.getLogger(SearchBot.class);
	private boolean stopSearching;
	private List<Path> searchPaths;
	private Process process;

	public void stopSearching() {
		stopSearching = true;
		if (process!=null)
			process.destroyForcibly();
	}

	public void setSearchResults(List<Path> searchPaths)
	{
		this.searchPaths=new CopyOnWriteArrayList<Path>(searchPaths);
	}
	
	@Override
	public void run() {
		stopSearching = false;
		found.clear();
		for (Path searchPath : searchPaths) {
			try {
				Stream<Path> traversal = Files.walk(searchPath);
				List<Path> pathList = traversal.filter(path -> Files.isDirectory(path)).collect(Collectors.toList());
				traversal.close();

				for (Path path : pathList) {
					String[] arguments = {  "/bin/sh", "-c", "face_recognition "+ImageTransfer.getSearchImagePath() +" '"+path.toString()+"' "};
					ProcessBuilder processBuilder = new ProcessBuilder(arguments);
					processBuilder.redirectErrorStream(true);
					process = processBuilder.start();
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String result = null;
					while ((result = bufferedReader.readLine()) != null) {
						System.out.println(result);
						if (stopSearching)
							return;

						if (result.contains("FOUND-")) {
							/// /tmp/dogwatch/photo/media/foto_jeroen/31-12- 08 and 01-01- 09 in
							/// London/1092266860193.jpg,FOUND-search_jeroen
							String imageName = result.split(",")[0].substring(MountOperations.getDogWatchMountPath().length());
							if (!found.contains(imageName))
								found.add(imageName);
							
						}
					}
				}

			} catch (IOException e) {
				log.error("Error while searching",e);
			}
		}
		System.out.println("Done searching");

	}
}
