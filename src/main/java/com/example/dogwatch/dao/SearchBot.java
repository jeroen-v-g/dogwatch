package com.example.dogwatch.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
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
	private double percentageCompleted;
	private PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**.{jpg,jpeg,png,JPEG,JPG,PNG}");


	public void stopSearching() {
		stopSearching = true;
		if (process != null)
			process.destroyForcibly();
	}

	public void setSearchResults(List<Path> searchPaths) {
		this.searchPaths = new CopyOnWriteArrayList<Path>(searchPaths);
	}
	
	public double getPercentageCompleted()
	{
		return percentageCompleted;
	}

	@Override
	public void run() {
		stopSearching = false;
		found.clear();
		int countFiles = 0;
		try {
			for (Path searchPath : searchPaths) {
				countFiles += Files.walk(searchPath)
						.filter(path -> pathMatcher.matches(path)
								&& !Files.isDirectory(path))
						.count();
			}
		} catch (IOException e) {
			log.error("Error while counting files", e);
		}
		double devider = (double)countFiles / (double)100;
		int processedFiles = 0;
		for (Path searchPath : searchPaths) {
			try {
				Stream<Path> traversal = Files.walk(searchPath);
				List<Path> pathList = traversal.filter(path -> Files.isDirectory(path)).collect(Collectors.toList());
				traversal.close();
				for (Path path : pathList) {
					String[] arguments = { "/usr/local/bin/face_recognition", "--cpus", "1",
							ImageTransfer.getSearchImagePath(), path.toString() };
					ProcessBuilder processBuilder = new ProcessBuilder(arguments);
					processBuilder.redirectErrorStream(true);
					process = processBuilder.start();
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String result = null;
					while ((result = bufferedReader.readLine()) != null) {
						processedFiles++;
						percentageCompleted = processedFiles / devider;
						if (!result.endsWith("no_persons_found") && !result.endsWith("unknown_person")
								&& !result.contains("FOUND-"))
							System.out.println(result);
						if (stopSearching)
							return;

						if (result.contains("FOUND-")) {
							String imageName = result.split(",")[0]
									.substring(MountOperations.getDogWatchMountPath().length());
							if (!found.contains(imageName))
								found.add(imageName);

						}
					}
				}

			} catch (IOException e) {
				log.error("Error while searching", e);
			}
		}
		percentageCompleted=0;
		System.out.println("Done searching");

	}
}
