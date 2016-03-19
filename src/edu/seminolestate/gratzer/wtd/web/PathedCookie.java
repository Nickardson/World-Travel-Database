package edu.seminolestate.gratzer.wtd.web;

import fi.iki.elonen.NanoHTTPD.Cookie;

/**
 * An extension of a cookie which can optionally contain a cookie path
 * @author Taylor
 * @date 2016-02-14
 */
public class PathedCookie extends Cookie {
	private String path;
	
	/**
	 * Creates a PathedCookie
	 * @param name Name of the cookie
	 * @param value The value of the cookie
	 */
	public PathedCookie(String name, String value) {
		super(name, value);
	}
	
	/**
	 * Creates a PathedCookie
	 * @param name Name of the cookie
	 * @param value The value of the cookie
	 * @param numDays Number of days this cookie exists for
	 */
	public PathedCookie(String name, String value, int numDays) {
		super(name, value, numDays);
	}
	
	/**
	 * Creates a PathedCookie
	 * @param name Name of the cookie
	 * @param value The value of the cookie
	 * @param expires The time string when this cookie expires
	 */
	public PathedCookie(String name, String value, String expires) {
		super(name, value, expires);
	}
	
	/**
	 * Creates a pathed cookie with a given path
	 * @param name Name of the cookie
	 * @param value Value of the cookie
	 * @param numDays Number of days this cookie will exist
	 * @param path The path for which the cookie is valid.
	 */
	public PathedCookie(String name, String value, int numDays, String path) {
		super(name, value, numDays);
		this.path = path;
	}

	@Override
	public String getHTTPHeader() {
		if (path != null) {
			return super.getHTTPHeader() + "; Path=" + path;
		} else {
			return super.getHTTPHeader();
		}
	}
}
