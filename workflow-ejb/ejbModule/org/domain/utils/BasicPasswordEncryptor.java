package org.domain.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BasicPasswordEncryptor {

	public String encryptPassword(String password) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		byte[] thedigest = md.digest(password.getBytes());
		return new String(thedigest);
	}

}
