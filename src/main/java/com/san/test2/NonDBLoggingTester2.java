package com.san.test2;

import org.apache.log4j.Logger;

public class NonDBLoggingTester2 {
	private static final Logger logger = Logger.getLogger(NonDBLoggingTester2.class);

	public void testLog() {
		logger.info("info log tested (#test1$) it shlould pass filter");
		logger.debug("debug log tested (# test1$) it shlould not pass filter");
		logger.error("error log tested (#test1$) it shlould pass filter");
		logger.trace("trace log tested (# test1$) it shlould not pass filter");
	}
}
