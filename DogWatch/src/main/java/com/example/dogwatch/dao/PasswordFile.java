package com.example.dogwatch.dao;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PasswordFile {

	private final String defaultUsername;
	private final String defaultPassword;

	public static final Logger log = Logger.getLogger(PasswordFile.class);
	
	public PasswordFile(String defaultUsername, String defaultPassword) 
	{
		this.defaultUsername=defaultUsername;
		this.defaultPassword=defaultPassword;
	}
	
	public String getUserName()
	{
		return getUsernameOrPassword(true);
	}
	
	public String getPassword()
	{
		return getUsernameOrPassword(false);
	}
	
	private String getUsernameOrPassword(boolean returnUsername)
	{
		Properties properties = new Properties();
		try {
			String propertiesFileName = MountOperations.getDogWatchPath()+"/dogwatch.properties";
			InputStream inputStream = new FileInputStream(propertiesFileName);
			properties.loadFromXML(inputStream);
		} catch (FileNotFoundException e) {
			if (returnUsername)
				return defaultUsername;
			else
				return defaultPassword;
		}catch (IOException e) {
			log.error("Could not load properties file",e);
		}
		if (returnUsername)
		{
			return properties.getProperty("username")!=null?properties.getProperty("username"):defaultUsername;
		}
		else
		{
			return properties.getProperty("password")!=null?properties.getProperty("password"):defaultUsername;
		}
	}
	
	public void newPassword(String password) throws IOException
	{
		Properties properties = new Properties();
		InputStream inputStream;
		try {
			String propertiesFileName = MountOperations.getDogWatchPath()+"/dogwatch.properties";
			inputStream = new FileInputStream(propertiesFileName);
			properties.loadFromXML(inputStream);
		} catch (FileNotFoundException e) {
			log.info("Properties file not found, new file to be created");
		} catch (IOException e) {
			throw new IOException("Could not load properties file: "+e);
		}
		try {
			String propertiesFileName = MountOperations.getDogWatchPath()+"/dogwatch.properties";
			FileOutputStream outputStream = new FileOutputStream(propertiesFileName);
			properties.setProperty("password", password);
			properties.storeToXML(outputStream, "");
		} catch (IOException e) {
			throw new IOException("Could not save properties file: "+e);
		}
	}
	
}
