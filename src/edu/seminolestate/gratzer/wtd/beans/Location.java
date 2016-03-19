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
public class Location implements IBean<Location> {
	/**
	 * Represents a 'null' ID.
	 */
	public static int NO_ID = -1;
	
	int id;
	int ownerid;
	String name;
	double latitude;
	double longitude;
	boolean shared;
	
	public Location() {
	}

	public Location(int id) {
		this.id = id;
	}
	
	public Location(int id, int ownerid, String name, double latitude, double longitude, boolean shared) {
		this.id = id;
		this.ownerid = ownerid;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.shared = shared;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOwnerid() {
		return ownerid;
	}

	public void setOwnerid(int ownerid) {
		this.ownerid = ownerid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	@Override
	public Location create(Connection connection) throws SQLException {
		PreparedStatement p = connection.prepareStatement("INSERT INTO locations (id, ownerid, name, latitude, longitude, shared) VALUES (?, ?, ?, ?, ?, ?)");
		if (getId() == NO_ID) {
			p.setNull(1, java.sql.Types.NULL);
		} else {
			p.setInt(1, getId());
		}
		p.setInt(2, getOwnerid());
		p.setString(3, getName());
		p.setDouble(4, getLatitude());
		p.setDouble(5, getLongitude());
		p.setBoolean(6, isShared());
		
		p.executeUpdate();
		
		this.setId(DBUtil.getLastRowID(connection));
		
		return this;
	}
	
	@Override
	public Location read(Connection c) throws SQLException {
		PreparedStatement p = c.prepareStatement("SELECT * FROM locations WHERE id = ?");
		p.setInt(1, id);
		
		ResultSet rs = p.executeQuery();
		while (rs.next()) {
			this.readResultSet(rs);
		}
		rs.close();
		
		return this;
	}
	
	
	
	@Override
	public Location update(Connection c) throws SQLException {
		PreparedStatement p = c.prepareStatement("UPDATE locations SET ownerid = ?, name = ? latitude = ?, longitude = ?, shared = ? WHERE id = ?");
		p.setInt(1, getOwnerid());
		p.setString(2, getName());
		p.setDouble(3, getLatitude());
		p.setDouble(4, getLongitude());
		p.setBoolean(5, isShared());
		
		p.setInt(6, getId());
		return this;
	}
	
	@Override
	public Location delete(Connection c) throws SQLException {
		PreparedStatement p = c.prepareStatement("DELETE FROM locations WHERE id = ?");
		p.setInt(1, getId());
		p.executeUpdate();
		
		return this;
	}
	
	@Override
	public Location readResultSet(ResultSet rs) throws SQLException {
		this.setId(rs.getInt("id"));
		this.setOwnerid(rs.getInt("ownerid"));
		this.setName(rs.getString("name"));
		this.setLatitude(rs.getDouble("latitude"));
		this.setLongitude(rs.getDouble("longitude"));
		this.setShared(rs.getBoolean("shared"));
		
		return this;
	}

	@Override
	public String toString() {
		return "Location [id=" + id + ", ownerid=" + ownerid + ", name=" + name
				+ ", latitude=" + latitude + ", longitude=" + longitude
				+ ", shared=" + shared + "]";
	}
}