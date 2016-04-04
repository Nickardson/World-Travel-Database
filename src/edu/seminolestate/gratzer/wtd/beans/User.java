package edu.seminolestate.gratzer.wtd.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.seminolestate.gratzer.wtd.database.DBUtil;

/**
 * A CRUD Bean for a User
 * @author Taylor
 * @date 2016-02-13
 */
public class User implements IBean<User> {
	/**
	 * Represents a 'null' ID.
	 */
	public static final int NO_ID = -1;
	
	private int id;
	private String username;
	private String password;
	private boolean admin;
	
	public User() {
	}
	
	public User(int id) {
		this.id = id;
	}
	
	public User(int id, String username, String password, boolean admin) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.admin = admin;
	}

	public int getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public boolean isAdmin() {
		return admin;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @date 2016-04-03
	 * Added constraint checks.
	 */
	public void setPassword(String password) {
		if (password == null || password.length() == 0)
			throw new IllegalArgumentException("Password must be provided.");
		
		this.password = password;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	
	@Override
	public User create(Connection connection) throws SQLException {
		PreparedStatement p = connection.prepareStatement("INSERT INTO users (id, username, password, admin) VALUES (?, ?, ?, ?)");
		if (getId() == NO_ID) {
			p.setNull(1, java.sql.Types.NULL);
		} else {
			p.setInt(1, getId());
		}
		p.setString(2, getUsername());
		p.setString(3, getPassword());
		p.setBoolean(4, isAdmin());
		
		p.executeUpdate();
		
		this.setId(DBUtil.getLastRowID(connection));
		
		return this;
	}

	@Override
	public User read(Connection connection) throws SQLException {
		PreparedStatement p = connection.prepareStatement("SELECT * FROM users WHERE id = ?");
		p.setInt(1, id);
		
		ResultSet rs = p.executeQuery();
		
		while (rs.next()) {
			this.readResultSet(rs);
		}
		rs.close();
		
		return this;
	}
	
	public User read(Connection connection, String username) throws SQLException {
		PreparedStatement p = connection.prepareStatement("SELECT id, username, password, admin FROM users WHERE username = ?");
		p.setString(1, username);
		
		ResultSet rs = p.executeQuery();
		while (rs.next()) {
			this.readResultSet(rs);
		}
		rs.close();
		
		return this;
	}

	@Override
	public User update(Connection connection) throws SQLException {
		PreparedStatement p = connection.prepareStatement("UPDATE users SET username = ?, password = ?, admin = ? WHERE id = ?");
		p.setString(1, getUsername());
		p.setString(2, getPassword());
		p.setBoolean(3, isAdmin());
		p.setInt(4, getId());
		p.executeUpdate();
		
		return this;
	}

	@Override
	public User delete(Connection connection) throws SQLException {
		PreparedStatement p = connection.prepareStatement("DELETE FROM users WHERE id = ?");
		p.setInt(1, getId());
		p.executeUpdate();
		
		return this;
	}

	@Override
	public User readResultSet(ResultSet rs) throws SQLException {
		this.setId(rs.getInt("id"));
		this.setUsername(rs.getString("username"));
		this.setPassword(rs.getString("password"));
		this.setAdmin(rs.getBoolean("admin"));
		
		return this;
	}
	
	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", password=" + password + ", admin=" + admin + "]";
	}
}
