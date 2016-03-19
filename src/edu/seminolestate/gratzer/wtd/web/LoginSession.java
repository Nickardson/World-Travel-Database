package edu.seminolestate.gratzer.wtd.web;

/**
 * A login session which can relate an anonymous session id to a userid.
 * @author Taylor
 */
public class LoginSession {
	private int sessionID;
	private int userID;
	
	/**
	 * Creates a LoginSession with the given IDs
	 * @param sessionID The session ID which the user keeps track of.
	 * @param userID The ID of the user.
	 */
	public LoginSession(int sessionID, int userID) {
		this.sessionID = sessionID;
		this.userID = userID;
	}
	
	/**
	 * @return The ID of this session
	 */
	public int getSessionID() {
		return sessionID;
	}
	
	/**
	 * @return The ID of the user this session represents
	 */
	public int getUserID() {
		return userID;
	}

	@Override
	public String toString() {
		return "LoginSession [sessionID=" + sessionID + ", userID=" + userID + "]";
	}
}
