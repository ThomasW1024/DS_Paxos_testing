package com.wuba.wpaxos.sample.runtime;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RuntimeMetric {
	private static final Logger logger = LogManager.getLogger(RuntimeMetric.class);
	private static final Level RESULT = Level.forName("RESULT", 99);
	private static long startTime;
	private static long endTime;
	private static String nodeId;
	private static AtomicInteger atomicCounter;

	public static void startMeasurement(String aNodeId) {
		startTime = System.currentTimeMillis();
		nodeId = aNodeId;
		atomicCounter = new AtomicInteger(0);
	}

	public static void endMeasurement() {
		endTime = System.currentTimeMillis();
		double averageConsensusPerSecond = (float)atomicCounter.intValue()/((endTime - startTime)/1000.0);
		logger.info("Average Proposed Value Per Second: {}", averageConsensusPerSecond);
		logger.log(RESULT,"{},{}", nodeId, averageConsensusPerSecond);
	}

	public static void measureRuntime(int instanceId, String respValue, long start, long end) {
		logger.info("{},{},{},{},{}", instanceId, respValue, start, end, end - start);
		atomicCounter.incrementAndGet();
	}
}
