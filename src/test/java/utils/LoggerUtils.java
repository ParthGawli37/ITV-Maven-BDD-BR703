package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerUtils {
	private static final Logger log = LogManager.getLogger(LoggerUtils.class);

	public static void info(String msg) {
		log.info(msg);
	}
	
	public static void pass(String msg) {
		log.info(msg);
	}
	
	public static void fail(String msg) {
		log.error(msg);
	}
	
	public static void warn(String msg) {
		log.warn(msg);
	}
	
	
}