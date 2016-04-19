package edu.seminolestate.gratzer.wtd.web.provider;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import edu.seminolestate.gratzer.wtd.Main;
import edu.seminolestate.gratzer.wtd.beans.IBean;
import edu.seminolestate.gratzer.wtd.beans.UserInfo;
import edu.seminolestate.gratzer.wtd.web.PageProvider.Populator;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

public class UsersPopulator implements Populator {
	@Override
	public void populate(Map<String, Object> model, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		try {
			List<UserInfo> viewUsers = IBean.executeQuery(UserInfo.class, Main.instance.dbConnection.prepareStatement("SELECT * FROM userinfo ORDER BY logcount DESC"));
			model.put("users", viewUsers);
		} catch (SQLException e) {
		}					
	}
}