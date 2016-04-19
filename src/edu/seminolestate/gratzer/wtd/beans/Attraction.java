package edu.seminolestate.gratzer.wtd.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import mjson.Json;
import edu.seminolestate.gratzer.wtd.IJsonable;
import edu.seminolestate.gratzer.wtd.database.DBUtil;

/**
 * A CRUD Bean for an attraction 
 * @author Taylor
 * @date 2016-04-17
 */
public class Attraction implements IBean<Attraction>, IJsonable {
	/**
	 * Represents a 'null' ID.
	 */
	public static int NO_ID = -1;
	
	int id;
	int logid;
	String name;
	int rating;
	String comment;
	String type;
	
	public Attraction() {
	}

	public Attraction(int id) {
		this.id = id;
	}
	
	

	public Attraction(int id, int logid, String name, int rating, String comment, String type) {
		this.id = id;
		this.logid = logid;
		this.name = name;
		this.rating = rating;
		this.comment = comment;
		this.type = type;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		if (rating < 1 || rating > 5)
			throw new IllegalArgumentException("Rating must be between 1 and 5");
		
		this.rating = rating;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public Attraction create(Connection connection) throws SQLException {
		PreparedStatement p = connection.prepareStatement("INSERT INTO attractions (id, logid, name, rating, comment, type) VALUES (?, ?, ?, ?, ?, ?)");
		if (getId() == NO_ID) {
			p.setNull(1, java.sql.Types.NULL);
		} else {
			p.setInt(1, getId());
		}
		p.setInt(2, getLogid());
		p.setString(3, getName());
		p.setInt(4, getRating());
		p.setString(5, getComment());
		p.setString(6, getType());
		
		p.executeUpdate();
		
		this.setId(DBUtil.getLastRowID(connection));
		
		return this;
	}
	
	@Override
	public Attraction read(Connection c) throws SQLException {
		PreparedStatement p = c.prepareStatement("SELECT * FROM attractions WHERE id = ?");
		p.setInt(1, id);
		
		ResultSet rs = p.executeQuery();
		while (rs.next()) {
			this.readResultSet(rs);
		}
		rs.close();
		
		return this;
	}
	
	
	
	@Override
	public Attraction update(Connection c) throws SQLException {
		PreparedStatement p = c.prepareStatement("UPDATE attractions SET logid = ?, name = ?, rating = ?, comment = ?, type = ? WHERE id = ?");
		p.setInt(1, getLogid());
		p.setString(2, getName());
		p.setInt(3, getRating());
		p.setString(4, getComment());
		p.setString(5, getType());
		
		p.setInt(6, getId());
		
		p.executeUpdate();
		
		return this;
	}
	
	@Override
	public Attraction delete(Connection c) throws SQLException {
		PreparedStatement p = c.prepareStatement("DELETE FROM attractions WHERE id = ?");
		p.setInt(1, getId());
		p.executeUpdate();
		
		return this;
	}
	
	@Override
	public Attraction readResultSet(ResultSet rs) throws SQLException {
		this.setId(rs.getInt("id"));
		this.setLogid(rs.getInt("logid"));
		this.setName(rs.getString("name"));
		this.setRating(rs.getInt("rating"));
		this.setComment(rs.getString("comment"));
		this.setType(rs.getString("type"));
		
		return this;
	}

	@Override
	public String toString() {
		return "Attraction [id=" + id + ", logid=" + logid + ", name=" + name
				+ ", rating=" + rating + ", comment=" + comment + ", type="
				+ type + "]";
	}

	@Override
	public Json toJSON() {
		return Json.object()
				.set("id", getId())
				.set("logid", getLogid())
				.set("name", getName())
				.set("rating", getRating())
				.set("comment", getComment())
				.set("type", getType());
	}
}
