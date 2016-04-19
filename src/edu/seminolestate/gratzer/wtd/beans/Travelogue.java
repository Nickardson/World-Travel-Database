package edu.seminolestate.gratzer.wtd.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.seminolestate.gratzer.wtd.Main;
import edu.seminolestate.gratzer.wtd.database.DBUtil;

/**
 * A CRUD Bean for a Travelogue
 * @author Taylor
 * @date 2016-02-13
 */
public class Travelogue implements IBean<Travelogue> {
	/**
	 * Represents a 'null' ID.
	 */
	public static int NO_ID = -1;
	
	/**
	 * Date format as it is stored in the database
	 */
	public static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	int id;
	int ownerid;
	String content;
	Date visitdate;
	boolean shared;
	int locationid;
	int views;
	
	public Travelogue() {
	}

	public Travelogue(int id) {
		this.id = id;
	}
	
	public Travelogue(int id, int ownerid, String content, Date visitdate) {
		this(id, ownerid, content, visitdate, false, NO_ID);
	}
	
	// Database Version 2
	public Travelogue(int id, int ownerid, String content, Date visitdate, boolean shared, int locationid) {
		this(id, ownerid, content, visitdate, shared, locationid, 0);
	}
	
	/**
	 * @date 2016-04-03
	 * Added version 3
	 */
	public Travelogue(int id, int ownerid, String content, Date visitdate, boolean shared, int locationid, int views) {
		this.id = id;
		this.ownerid = ownerid;
		this.content = content;
		this.visitdate = visitdate;
		this.shared = shared;
		this.locationid = locationid;
		this.views = views;
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

	// ownerid is validated in the database
	public void setOwnerid(int ownerid) {
		this.ownerid = ownerid;
	}

	public String getContent() {
		return content;
	}

	/**
	 * @date 2016-04-03
	 * Added constraint checks.
	 */
	public void setContent(String content) {
		if (content.length() > 10_000)
			throw new IllegalArgumentException("Content cannot exceed 10,000 characters.");
		
		this.content = content;
	}

	public Date getVisitDate() {
		return visitdate;
	}

	public void setVisitDate(Date visitDate) {
		this.visitdate = visitDate;
	}
	
	// Database Version 2
	public boolean isShared() {
		return shared;
	}
	
	public void setShared(boolean shared) {
		this.shared = shared;
	}
	
	public int getLocationid() {
		return locationid;
	}
	
	// locationid is validated in the database
	public void setLocationid(int locationid) {
		this.locationid = locationid;
	}
	
	public int getViews() {
		return views;
	}

	// views is validated in the database
	public void setViews(int views) {
		this.views = views;
	}
	
	// @date 2016-04-03 added view counter
	public boolean addView() {
		try {
			this.setViews(this.getViews() + 1);
			// runs a simpler update query, rather than full-blown update call
			PreparedStatement up = Main.instance.dbConnection.prepareStatement("UPDATE travelogues SET views = ? WHERE id = ?");
			up.setInt(1, this.getViews());
			up.setInt(2, getId());
			up.executeUpdate();
			
			return true;
		} catch (Exception ignored) {
			// views are not really important
			return false;
		}
	}

	/**
	 * @return SELECTed list of TravelogueImages that belong to the Travelogue with this ID
	 * @throws SQLException
	 */
	public List<TravelogueImage> getImages() throws SQLException {
		this.addView();
		
		PreparedStatement query = Main.instance.dbConnection.prepareStatement("SELECT * FROM travel_images WHERE logid = ?");
		query.setInt(1, this.getId());
		return IBean.executeQuery(TravelogueImage.class, query);
	}

	/**
	 * @return SELECTed list of Attractions that belong to the Travelogue with this ID
	 * @throws SQLException
	 */
	public List<Attraction> getAttractions() throws SQLException {
		PreparedStatement query = Main.instance.dbConnection.prepareStatement("SELECT * FROM attractions WHERE logid = ?");
		query.setInt(1, this.getId());
		return IBean.executeQuery(Attraction.class, query);
	}
	
	/**
	 * Returns the Location bean, if any, for this Travelogue.
	 * @return The Location, or null if no location exists.
	 * @throws SQLException
	 * @date 2016-03-06
	 */
	public Location getLocation() throws SQLException {
		if (getLocationid() != Location.NO_ID) {
			PreparedStatement query = Main.instance.dbConnection.prepareStatement("SELECT * FROM locations WHERE id = ?");
			query.setInt(1, this.getLocationid());
			return IBean.executeQuery(Location.class, query).get(0);			
		} else {
			return null;
		}
	}
	
	@Override
	public Travelogue create(Connection connection) throws SQLException {
		PreparedStatement p;
		if (DBUtil.getUserVersion(Main.instance.dbConnection) >= 3) {
			p = connection.prepareStatement("INSERT INTO travelogues (id, ownerid, content, visitdate, shared, locationid, views) VALUES (?, ?, ?, ?, ?, ?, ?)");
		} else if (DBUtil.getUserVersion(Main.instance.dbConnection) >= 2) {
			p = connection.prepareStatement("INSERT INTO travelogues (id, ownerid, content, visitdate, shared, locationid) VALUES (?, ?, ?, ?, ?, ?)");
		} else {
			p = connection.prepareStatement("INSERT INTO travelogues (id, ownerid, content, visitdate) VALUES (?, ?, ?, ?)");
		}
		
		if (getId() == NO_ID) {
			p.setNull(1, Types.NULL);
		} else {
			p.setInt(1, getId());
		}
		p.setInt(2, getOwnerid());
		p.setString(3, getContent());
		
		if (getVisitDate() == null) {
			p.setNull(4, Types.NULL);
		} else {
			p.setString(4, DATE_FORMAT.format(getVisitDate()));
		}
		
		// Database Version 2
		if (DBUtil.getUserVersion(Main.instance.dbConnection) >= 2) {
			p.setBoolean(5, isShared());
			
			if (getLocationid() == NO_ID) {
				p.setNull(6, Types.NULL);
			} else {
				p.setInt(6, getLocationid());
			}
		}
		
		if (DBUtil.getUserVersion(Main.instance.dbConnection) >= 3) {
			p.setInt(7, getViews());
		}
		
		p.executeUpdate();
		
		this.setId(DBUtil.getLastRowID(connection));
		
		return this;
	}
	
	@Override
	public Travelogue read(Connection c) throws SQLException {
		PreparedStatement p = c.prepareStatement("SELECT * FROM travelogues WHERE id = ?");
		p.setInt(1, id);
		
		ResultSet rs = p.executeQuery();
		while (rs.next()) {
			this.readResultSet(rs);
		}
		rs.close();
		
		return this;
	}
	
	@Override
	public Travelogue update(Connection c) throws SQLException {
		PreparedStatement p;
		if (DBUtil.getUserVersion(Main.instance.dbConnection) >= 3) {
			p = c.prepareStatement("UPDATE travelogues SET ownerid = ?, content = ?, visitdate = ?, shared = ?, locationid = ?, views = ? WHERE id = ?");
		} else if (DBUtil.getUserVersion(Main.instance.dbConnection) >= 2) {
			p = c.prepareStatement("UPDATE travelogues SET ownerid = ?, content = ?, visitdate = ?, shared = ?, locationid = ? WHERE id = ?");
		} else {
			p = c.prepareStatement("UPDATE travelogues SET ownerid = ?, content = ?, visitdate = ? WHERE id = ?");
		}
		
		int INDEX = 1;
		
		p.setInt(INDEX++, getOwnerid());
		p.setString(INDEX++, getContent());
		if (getVisitDate() == null) {
			p.setNull(INDEX++, Types.NULL);
		} else {
			p.setString(INDEX++, DATE_FORMAT.format(getVisitDate()));
		}
		
		// Database Version 2
		if (DBUtil.getUserVersion(Main.instance.dbConnection) >= 2) {
			p.setBoolean(INDEX++, isShared());
			
			if (getLocationid() == NO_ID) {
				p.setNull(INDEX++, Types.NULL);
			} else {
				p.setInt(INDEX++, getLocationid());
			}			
		}
		
		if (DBUtil.getUserVersion(Main.instance.dbConnection) >= 3) {
			System.out.println("version 3 w views " + this);
			p.setInt(INDEX++, getViews());
		}
		
		p.setInt(INDEX++, getId());
		p.executeUpdate();
		
		return this;
	}
	
	@Override
	public Travelogue delete(Connection c) throws SQLException {
		// delete the log itself
		PreparedStatement p = c.prepareStatement("DELETE FROM travelogues WHERE id = ?");
		p.setInt(1, getId());
		p.executeUpdate();
		
		return this;
	}
	
	@Override
	public Travelogue readResultSet(ResultSet rs) throws SQLException {
		this.setId(rs.getInt("id"));
		this.setOwnerid(rs.getInt("ownerid"));
		this.setContent(rs.getString("content"));
		try {
			if (rs.getString("visitdate") != null) {
				this.setVisitDate(DATE_FORMAT.parse(rs.getString("visitdate")));
			} else {
				this.setVisitDate(null);
			}
		} catch (ParseException e) {
			
		} catch (NumberFormatException e) {
			
		}
		
		
		// Database Version 2
		if (DBUtil.getUserVersion(Main.instance.dbConnection) >= 2) {
			this.setShared(rs.getBoolean("shared"));
			
			int locationid = rs.getInt("locationid");
			if (locationid == 0) {
				this.setLocationid(NO_ID);
			} else {
				this.setLocationid(locationid);			
			}			
		}
		
		if (DBUtil.getUserVersion(Main.instance.dbConnection) >= 3) {
			this.setViews(rs.getInt("views"));
		}
		
		return this;
	}

	@Override
	public String toString() {
		String shortContent;
		
		if (content.length() > 20) {
			shortContent = content.substring(0, 20) + " (...)";
		} else {
			shortContent = content;
		}
		
		return "Travelogue [id=" + id + ", ownerid=" + ownerid + ", content="
				+ shortContent + ", visitdate=" + visitdate + ", shared=" + shared
				+ ", locationid=" + locationid + ", views=" + views + "]";
	}
}
