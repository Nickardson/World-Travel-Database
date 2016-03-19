package edu.seminolestate.gratzer.wtd.database;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Can hash various data, such as strings or files.
 * @author Taylor
 * @date 2016-02-13
 */
public class PasswordHasher {
	private PasswordHasher() {
	}
	
	/**
	 * Hashes a password with no salt
	 * @param plaintext The plaintext
	 * @return The hashed text
	 */
	public static String hashPassword(String plaintext) {
		return sha256(plaintext);
	}
	
	/**
	 * Gets the SHA-256 hash of the given text
	 * @param plaintext The plaintext
	 * @return The hashed text
	 */
	public static String sha256(String plaintext) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.update(plaintext.getBytes("UTF-8"));
	  		byte[] digest = md.digest();
	  		
			return String.format("%064x", new java.math.BigInteger(1, digest));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return plaintext;
	}
	
	/**
	 * Attempts to get the MD5 hash of the given file.
	 * @param f The file to check the hash of
	 * @return The MD5 has of the file's contents, or null if an error occured.
	 * Hex string format (ie d3adb33f)
	 */
	public static String md5File(File f) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			try (InputStream is = Files.newInputStream(f.toPath());
			     DigestInputStream dis = new DigestInputStream(is, md)) 
			{
				/* Read decorated stream (dis) to EOF as normal... */
				byte[] buffer = new byte[1024];
				while (dis.read(buffer) != -1)
					;
				dis.close();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			byte[] digest = md.digest();
			
			return String.format("%032x", new java.math.BigInteger(1, digest)); 
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
}
