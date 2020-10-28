package com.example.dogwatch.dao;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

public class ImageTransfer {

	private static final Logger log = Logger.getLogger(ImageTransfer.class);

	public static String getSearchImagePath() throws IOException {

		String searchImagePath = MountOperations.getDogWatchPath() + "/images";
		if (!Files.exists(Paths.get(searchImagePath))) {
			Files.createDirectories(Paths.get(searchImagePath));
		}
		return searchImagePath;

	}

	public static List<String> getSearchImages() throws IOException {
		Path imagedir = Paths.get(getSearchImagePath());
		List<String> filesList = Files.list(imagedir).map(path -> path.getFileName().toString())
				.collect(Collectors.toList());
		return filesList;
	}

	public static void writeFile(InputStream inputstream, String filename) throws IOException {
		byte[] buffer = new byte[inputstream.available()];
		inputstream.read(buffer);
		OutputStream outStream = new FileOutputStream(getSearchImagePath() + "/" + filename);
		outStream.write(buffer);
		outStream.close();
	}

	public static void removeImage(String fileName) throws IOException {
		Path imageFile = Paths.get(getSearchImagePath() + "/" + fileName);
		Files.delete(imageFile);
	}

	public static InputStream getThumbImage(String fileName) throws IOException {
		BufferedImage originalImage = ImageIO.read(new File(MountOperations.getDogWatchMountPath() + fileName));
		double height = originalImage.getHeight();
		double width = originalImage.getWidth();
		double ratio = height / width;
		int newHeight = (int) (400 * ratio);
		BufferedImage resizeImage = new BufferedImage(400, newHeight, originalImage.getType());
		Graphics graphics = resizeImage.createGraphics();
		graphics.drawImage(originalImage, 0, 0, 400, newHeight, null);
		graphics.dispose();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(resizeImage, "jpeg", os);
		return new ByteArrayInputStream(os.toByteArray());
	}
}
