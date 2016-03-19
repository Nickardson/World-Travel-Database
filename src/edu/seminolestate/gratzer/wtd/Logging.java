package edu.seminolestate.gratzer.wtd;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Logging {
	/**
	 * Gets a logger for the given class.
	 * No caching is handled by this class.
	 * @param clazz
	 * @return
	 */
	public static Logger get(Class<?> clazz) {
		Logger LOG = Logger.getLogger(Main.class.getName());
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT [%4$s] %2$s: %5$s%6$s%n");
		FileHandler fh=null;
		try {
			fh = new FileHandler("worldtravel.log", true);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		fh.setFormatter(new SimpleFormatter());
		LOG.addHandler(fh);
		
		return LOG;
	}
}
