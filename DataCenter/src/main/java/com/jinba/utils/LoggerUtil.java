package com.jinba.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtil {
	
	private static Logger systemLogger = LoggerFactory.getLogger("SystemLogger");
	private static Logger httpLogger = LoggerFactory.getLogger("HttpLogger");
	private static Logger proxyLogger = LoggerFactory.getLogger("proxyLogger");


	public static void InfoLog (String log) {
		systemLogger.info(log);
	}
	
	public static void ProxyLog (String log) {
		proxyLogger.info(log);
	}
	
	public static void HttpInfoLog (String log) {
		httpLogger.info(log);
	}
	
	public static void HttpDebugLog (String log) {
		httpLogger.debug(log);
	}
	
}
