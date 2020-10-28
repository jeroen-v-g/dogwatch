package com.example.dogwatch.services;

import java.io.IOException;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;
import org.slf4j.Logger;

import com.example.dogwatch.dao.PasswordFile;
import com.example.dogwatch.dao.entities.User;
import com.example.dogwatch.security.AuthenticationException;

public class BasicAuthenticator implements Authenticator {

	public static final String AUTH_TOKEN = "authToken";

	@Inject
	private Request request;

	@Inject
	Logger logger;
	
	private PasswordFile passwordFile;
	{
		try {
			passwordFile = new PasswordFile("admin",encryptPassword("admin"));
		} catch (AuthenticationException e) {
			logger.error("No default username/password could be set");
		}
	}

	@Override
	public void login(String username, String password) throws AuthenticationException {
		String validUsername = passwordFile.getUserName();
		String validPassword = passwordFile.getPassword();

		if (!username.equals(validUsername) || !encryptPassword(password).contentEquals(validPassword)) {
			throw new AuthenticationException("Ongeldige gegevens");
		}

		User user = new User();
		user.setPassword(encryptPassword(password));
		request.getSession(true).setAttribute(AUTH_TOKEN, user);
	}

	@Override
	public boolean isLoggedIn() {
		Session session = request.getSession(false);
		if (session != null) {
			return session.getAttribute(AUTH_TOKEN) != null;
		}
		return false;
	}

	@Override
	public void logout() {
		Session session = request.getSession(false);
		if (session != null) {
			session.setAttribute(AUTH_TOKEN, null);
			session.invalidate();
		}
	}

	@Override
	public User getLoggedUser() {
		User user = null;

		if (isLoggedIn()) {
			user = (User) request.getSession(true).getAttribute(AUTH_TOKEN);
		} else {
			throw new IllegalStateException("The user is not logged in.");
		}
		return user;
	}

	@Override
	public boolean verifyPassword(String candidatePassword) throws AuthenticationException {
		if (candidatePassword == null) {
			return false;
		}
		return encryptPassword(candidatePassword).equals(getLoggedUser().getPassword());
	}

	public boolean storeNewPassword(String existingPassword, String newPassword) throws AuthenticationException {

		if (encryptPassword(existingPassword).equals(getLoggedUser().getPassword())) {
			User user = new User();
			user.setPassword(encryptPassword(newPassword));
			request.getSession(true).setAttribute(AUTH_TOKEN, user);
			try {
				passwordFile.newPassword(user.getPassword());
			} catch (IOException e) {
				throw new AuthenticationException("Could not change password: "+e.getMessage());
			}
			return true;
		}

		return false;
	}

}
