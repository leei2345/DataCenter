package com.jinba.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtil {
	
	private static Logger clawerLogger = LoggerFactory.getLogger("clawerLogger");
	private static Logger httpLogger = LoggerFactory.getLogger("httpLogger");
	private static Logger proxyLogger = LoggerFactory.getLogger("proxyLogger");
	private static Logger taskLogger = LoggerFactory.getLogger("taskLogger");
	private static Logger imageLogger = LoggerFactory.getLogger("imageLogger");
	private static Logger cookieLogger = LoggerFactory.getLogger("cookieLogger");
	private static Logger damaLogger = LoggerFactory.getLogger("damaLogger");


	public static void ClawerInfoLog (String log) {
		clawerLogger.info(log);
	}
	
	public static void ImageInfoLog (String log) {
		imageLogger.info(log);
	}
	
	public static void TaskInfoLog (String log) {
		taskLogger.info(log);
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
	
	public static void CookieInfoLog (String log) {
		cookieLogger.info(log);
	}
	
	public static void DamaInfoLog (String log) {
		damaLogger.info(log);
	}
	
}
