package com.example.dogwatch.services;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import com.example.dogwatch.dao.entities.User;
import com.example.dogwatch.security.AuthenticationException;

/**
 * Basic security interface
 * 
 * @author karesti
 */
public interface Authenticator {

	static final String PASSWORD_CHARSET = "UTF-8";

	static final String DIGEST_ALGORITHM = "SHA-512";

	/**
	 * Gets the logged-in user
	 * 
	 * @return User, the logged in user
	 */
	User getLoggedUser();

	/**
	 * Checks if the current user is logged in
	 * 
	 * @return true if the user is logged in
	 */
	boolean isLoggedIn();

	/**
	 * Logs in the user.
	 * 
	 * @param username
	 * @param password
	 * @throws AuthenticationException throw if an error occurs
	 */
	void login(String username, String password) throws AuthenticationException;

	/**
	 * Logs out the user
	 */
	void logout();

	/**
	 * Encrypt the given password, using a one-way hash
	 * 
	 * @param password the original, plain-text password
	 * @return the encrypted password, with the digest algorithm and "::" prepended
	 */
	default String encryptPassword(String password) throws AuthenticationException {
		MessageDigest digest;
		byte[] result;
		try {
			digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new AuthenticationException("No such algorithm: " + DIGEST_ALGORITHM, e);
		}
		try {
			result = digest.digest(password.getBytes(PASSWORD_CHARSET));
		} catch (UnsupportedEncodingException e) {
			throw new AuthenticationException("Unsupported encoding: " + PASSWORD_CHARSET, e);
		}
		return DIGEST_ALGORITHM + "::" + bytesToHex(result);
	}

	/**
	 * Convert an array of bytes to a hex-encoded string
	 * 
	 * @param input the array of bytes
	 * @return the equivalent hex-encoded string
	 */
	default String bytesToHex(byte[] input) {
		try (Formatter form = new Formatter()) {
			for (int i = 0; i < input.length; i++)
				form.format("%02x", input[i]);
			return form.toString();
		}
	}

	/**
	 * Determine whether the given password matches the one stored for the current
	 * user
	 * 
	 * @param password the plain-text password to check
	 * @return true if matching, false otherwise
	 * @throws AuthenticationException if the encryption fails for some reason
	 */
	boolean verifyPassword(String password) throws AuthenticationException;

	boolean storeNewPassword(String existingPassword, String newPassword) throws AuthenticationException;
}
