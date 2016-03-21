package edu.seminolestate.gratzer.wtd.beans;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A bean for a member of the userinfo view. As the underlying view is not intended to be read or updated individually, the bean does not allow certain methods.
 * @author Taylor
 * @date 2016-03-20 
 */
public class UserInfo implements IBean<UserInfo> {

	String username;
	int logcount;
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getLogcount() {
		return logcount;
	}

	public void setLogcount(int logcount) {
		this.logcount = logcount;
	}

	@Override
	public UserInfo create(Connection c) throws SQLException {
		throw new IllegalStateException("Cannot be created");
	}

	@Override
	public UserInfo read(Connection c) throws SQLException {
		throw new IllegalStateException("Cannot be individually queried");
	}

	@Override
	public UserInfo update(Connection c) throws SQLException {
		throw new IllegalStateException("Cannot be updated");
	}

	@Override
	public UserInfo delete(Connection c) throws SQLException {
		throw new IllegalStateException("Cannot be deleted");
	}

	@Override
	public UserInfo readResultSet(ResultSet rs) throws SQLException {
		this.setUsername(rs.getString("username"));
		this.setLogcount(rs.getInt("logcount"));
		
		return this;
	}

}
