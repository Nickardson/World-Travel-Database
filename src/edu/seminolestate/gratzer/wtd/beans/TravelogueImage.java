package edu.seminolestate.gratzer.wtd.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.seminolestate.gratzer.wtd.database.DBUtil;

/**
 * A CRUD Bean for a TravelogueImage
 * @author Taylor
 * @date 2016-02-13
 */
public class TravelogueImage implements IBean<TravelogueImage> {
	/**
	 * Represents a 'null' ID.
	 */
	public static int NO_ID = -1;
	
	int id;
	int logid;
	String filename;
	
	public TravelogueImage() {
	}

	public TravelogueImage(int id) {
		this.id = id;
	}
	
	public TravelogueImage(int id, int logid, String filename) {
		this.id = id;
		this.logid = logid;
		this.filename = filename;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLogid() {
		return logid;
	}

	public void setLogid(int logid) {
		this.logid = logid;
	}

	public String getFilename() {
		return filename;
	}

	/**
	 * @date 2016-04-03
	 * Added safety check.
	 */
	public void setFilename(String filename) {
		if (filename.contains("/") || filename.contains("\\"))
			throw new IllegalArgumentException("Filename cannot traverse paths.");
		
		this.filename = filename;
	}

	@Override
	public TravelogueImage create(Connection connection) throws SQLException {
		PreparedStatement p = connection.prepareStatement("INSERT INTO travel_images (id, logid, filename) VALUES (?, ?, ?)");
		if (getId() == NO_ID) {
			p.setNull(1, java.sql.Types.NULL);
		} else {
			p.setInt(1, getId());
		}
		p.setInt(2, getLogid());
		p.setString(3, getFilename());
		
		p.executeUpdate();
		
		this.setId(DBUtil.getLastRowID(connection));
		
		return this;
	}
	
	@Override
	public TravelogueImage read(Connection c) throws SQLException {
		PreparedStatement p = c.prepareStatement("SELECT id, logid, filename FROM travel_images WHERE id = ?");
		p.setInt(1, id);
		
		ResultSet rs = p.executeQuery();
		while (rs.next()) {
			this.readResultSet(rs);
		}
		rs.close();
		
		return this;
	}
	
	
	
	@Override
	public TravelogueImage update(Connection c) throws SQLException {
		PreparedStatement p = c.prepareStatement("UPDATE travel_images SET logid = ?, filename = ? WHERE id = ?");
		p.setInt(1, getLogid());
		p.setString(2, getFilename());
		p.setInt(3, getId());
		p.executeUpdate();
		
		return this;
	}
	
	@Override
	public TravelogueImage delete(Connection c) throws SQLException {
		PreparedStatement p = c.prepareStatement("DELETE FROM travel_images WHERE id = ?");
		p.setInt(1, getId());
		p.executeUpdate();
		
		return this;
	}
	
	@Override
	public TravelogueImage readResultSet(ResultSet rs) throws SQLException {
		this.setId(rs.getInt("id"));
		this.setLogid(rs.getInt("logid"));
		this.setFilename(rs.getString("filename"));
		
		return this;
	}

	@Override
	public String toString() {
		return "TravelogueImage [id=" + id + ", logid=" + logid + ", filename=" + filename + "]";
	}
}
