package com.wuba.wpaxos.sample.runtime;

import java.util.Optional;

import org.apache.logging.log4j.Logger;

public class LoggingUtil {
	private static final String SPLITTER_FORMAT = "-----------------------------{}------------------------------";

	public static void splitterLog(Logger logger, String title) {
		logger.info(SPLITTER_FORMAT, title);
	}
}
