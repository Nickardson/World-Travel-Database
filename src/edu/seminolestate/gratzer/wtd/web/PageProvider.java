package edu.seminolestate.gratzer.wtd.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

/**
 * Provides data to Jade templates.
 * Can be added through PageProvider.register / PageProvider.registerPattern, and used through PageProvider.populate to populate models
 * @author Taylor
 * @date 2016-02-14
 */
public class PageProvider {
	/**
	 * Populates a Map for a jade model.
	 */
	public static interface Populator {
		/**
		 * Implementations should put relevant keys into the given model.
		 * @param model The model to populate with data.
		 * @param uriResource From the request.
		 * @param urlParams From the request.
		 * @param session From the request.
		 */
		public void populate(Map<String, Object> model, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session);
	}
	
	/**
	 * Populates a Map for a jade model, for matched patterns.
	 */
	public static interface RegexPopulator extends Populator {
		/**
		 * @return The regex pattern which determines if the path is matched 
		 */
		public Pattern getPattern();
	}
	
	private static Map<String, Populator> map = new HashMap<>();
	private static List<RegexPopulator> regexPopulators = new ArrayList<>();
	
	/**
	 * Registers a populator for the given path
	 * @param pop The populator
	 * @param path The path which the populator applies to
	 */
	public static void register(Populator pop, String path) {
		map.put(path, pop);
	}
	
	/**
	 * Applies the relevant populators to the given model.
	 * Finds the populators from the registered populators.
	 * @param model The model which will be populated.
	 * @param path The path which the model comes from
	 * @param uriResource From the request, may be null
	 * @param urlParams From the request, may be null
	 * @param session From the request, may be null
	 */
	public static void populate(Map<String, Object> model, String path, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		for (RegexPopulator pop : regexPopulators) {
			if (pop.getPattern().matcher(path).find()) {
				pop.populate(model, uriResource, urlParams, session);
			}
		}
		
		if (map.containsKey(path)) {
			map.get(path).populate(model, uriResource, urlParams, session);
		} else {
//			System.out.println("No PageProvider for " + path);
		}
	}
	
	/**
	 * Regex populators apply to any path which matches their matcher's regex.
	 * Every registered regexpopulator attempts to match every path, so they should be used sparingly
	 * @param pop
	 */
	public static void registerPattern(RegexPopulator pop) {
		regexPopulators.add(pop);
	}
	
}
