package com.san.test1;

import org.apache.log4j.Logger;

public class NonDBLoggingTester1 {

	private static final Logger logger = Logger.getLogger(NonDBLoggingTester1.class);

	public void testLog() {
		logger.info("info log tested");
		logger.debug("debug log tested");
		logger.error("error log tested");
		logger.trace("trace log tested");
	}

}
