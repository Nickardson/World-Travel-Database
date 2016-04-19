package edu.seminolestate.gratzer.wtd.web.provider;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import edu.seminolestate.gratzer.wtd.Main;
import edu.seminolestate.gratzer.wtd.beans.IBean;
import edu.seminolestate.gratzer.wtd.beans.Travelogue;
import edu.seminolestate.gratzer.wtd.web.LoginSession;
import edu.seminolestate.gratzer.wtd.web.Server;
import edu.seminolestate.gratzer.wtd.web.PageProvider.Populator;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

public class IndexPopulator implements Populator {
	@Override
	public void populate(Map<String, Object> model, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		LoginSession login = Server.getLoginSession(session);
		if (login != null) {
			// list travelogues owned by the current user
			try {
				PreparedStatement query = Main.instance.dbConnection.prepareStatement("SELECT * FROM travelogues WHERE ownerid = ? ORDER BY visitdate DESC");
				query.setInt(1, login.getUserID());
				model.put("travelogues", IBean.executeQuery(Travelogue.class, query));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}