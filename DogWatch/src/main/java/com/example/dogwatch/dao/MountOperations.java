package com.example.dogwatch.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;

public class MountOperations {

	public static final String dogWatchPathName = "/tmp/dogwatch";
	public static final String dogWatchMountPathName = dogWatchPathName + "/mount";
	private static final ArrayList<String> activeMounts = new ArrayList<>();
	public static final Logger log = Logger.getLogger(MountOperations.class);

	public static String getDogWatchPath() throws IOException {

		if (!Files.exists(Paths.get(dogWatchPathName)))
			Files.createDirectories(Paths.get(dogWatchPathName));
		return dogWatchPathName;
	}

	public static String getDogWatchMountPath() throws IOException {
		if (!Files.exists(Paths.get(dogWatchMountPathName)))
			Files.createDirectories(Paths.get(dogWatchMountPathName));
		return dogWatchMountPathName;
	}

	static {

		Process process;
		try {
			String[] arguments = { "/bin/sh", "-c", "mount | grep " + getDogWatchMountPath() };
			ProcessBuilder processBuilder = new ProcessBuilder(arguments);
			processBuilder.redirectErrorStream(true);
			process = processBuilder.start();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String result = null;
			while ((result = bufferedReader.readLine()) != null) {
				String[] splitResult = result.split(" ");
				if (splitResult.length < 4) {
					log.error("Unexpected output mount command" + result);
					continue;
				}
				if (!splitResult[1].equals("on")) {
					log.error("Unexpected output mount command" + result);
					continue;
				}
				activeMounts.add(splitResult[2]);
			}

			Stream<Path> mountPath = Files.list(Paths.get(getDogWatchMountPath()));
			mountPath.forEach(path -> {
				if (!activeMounts.contains(path.toString())) {
					try {
						Files.delete(path);
					} catch (IOException e) {
						log.error("Error deleting file", e);
					}
				}
			});
			mountPath.close();

		} catch (IOException e) {
			log.error("Error operating mount-dir", e);
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<String> getActiveMounts() {
		return new ImmutableList.Builder().add(activeMounts.toArray()).build();
	}

	public static boolean mount(String mount, String username, String password) throws IOException {
		if (mount.split("/").length != 4)
			return false;
		String localMount = getDogWatchMountPath() + "/" + mount.split("/")[3];
		String credentialsLocation = "";
		if (!activeMounts.contains(localMount)) {
			try {
				credentialsLocation = getDogWatchPath() + "/credentials.secret";
				Files.createDirectory(Paths.get(localMount));
				String credentialsFileContent = "username=" + username + "\n" + "password=" + password;
				FileOutputStream fileOutputStream = new FileOutputStream(new File(credentialsLocation), false);
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
				outputStreamWriter.append(credentialsFileContent);
				outputStreamWriter.close();
				String[] arguments = { "sudo", "mount.cifs", mount, localMount, "-o",
						"credentials=" + credentialsLocation + ",vers=2.0" };
				ProcessBuilder processBuilder = new ProcessBuilder(arguments);
				processBuilder.redirectErrorStream(true);
				Process process = processBuilder.start();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String result = null;
				while ((result = bufferedReader.readLine()) != null) {
					throw new IOException("Mount failed:" + result);
				}
				activeMounts.add(localMount);
				return true;
			} catch (IOException e) {
				Files.deleteIfExists(Paths.get(localMount));
				throw e;
			} finally {
				try {
					Files.deleteIfExists(Paths.get(credentialsLocation));
				} catch (IOException e) {
					log.error("Error while removing credentials file", e);
				}
			}

		}
		return false;
	}

	public static boolean unMount(String localMount) throws IOException {
		if (activeMounts.contains(localMount)) {
			String[] arguments = { "sudo", "umount", "-a", "-t", "cifs", "-l", localMount };
			// sudo umount -a -t cifs -l /tmp/dogwatch/photo
			ProcessBuilder processBuilder = new ProcessBuilder(arguments);
			processBuilder.redirectErrorStream(true);

			Process process = processBuilder.start();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String result = null;
			while ((result = bufferedReader.readLine()) != null) {
				throw new IOException("Unmount failed:" + result);
			}
			Files.deleteIfExists(Paths.get(localMount));
			activeMounts.remove(localMount);
			return true;

		}
		return false;
	}

}
