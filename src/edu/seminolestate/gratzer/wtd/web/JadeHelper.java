package edu.seminolestate.gratzer.wtd.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility methods for jade templates
 * @author Taylor
 * @date 2016-02-13
 */
public class JadeHelper {
	/**
	 * Format for displaying the date of travelogues
	 */
	private static DateFormat DATE_FORMATTER = new SimpleDateFormat("MMMM dd, yyyy @ h:mm a");
	
	/**
	 * Gets a formatted date string for the given 
	 * @param time
	 * @return
	 */
	public String getFormattedDate(long time) {
		return DATE_FORMATTER.format(new Date(time));
	}
	
	// make certain methods visible to jade template as needed
	
	public double max(double a, double b) {
		return Math.max(a, b);
	}
	
	public double min(double a, double b) {
		return Math.min(a, b);
	}
	
	public boolean endsWith(String str, String sub) {
		return str.endsWith(sub);
	}
}
