package com.wuba.wpaxos.sample.runtime;

import java.util.LongSummaryStatistics;
import java.util.concurrent.CopyOnWriteArrayList;
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
	private static CopyOnWriteArrayList<Long> runtimeList = new CopyOnWriteArrayList<>();

	public static void startMeasurement(String aNodeId) {
		startTime = System.currentTimeMillis();
		nodeId = aNodeId;
		atomicCounter = new AtomicInteger(0);
	}

	private static double calculateSD(CopyOnWriteArrayList<Long> numArray, double mean)
	{
		double standardDeviation = 0.0;
		for(long num: numArray) {
			standardDeviation += Math.pow((double)num - mean, 2);
		}

		return Math.sqrt(standardDeviation/numArray.size());
	}

	public static void endMeasurement() {
		endTime = System.currentTimeMillis();
		double averageConsensusPerSecond = (float)atomicCounter.intValue()/((endTime - startTime)/1000.0);
		logger.info("Average Proposed Value Per Second: {}", averageConsensusPerSecond);
		LongSummaryStatistics statistics = runtimeList.stream().mapToLong((x) -> x).summaryStatistics();
		double deviation = calculateSD(runtimeList, statistics.getAverage());
		logger.log(RESULT,"{},{},{},{},{},{},{},{},{}", nodeId, startTime, endTime, averageConsensusPerSecond, statistics.getSum(), statistics.getMax(), statistics.getMin(), statistics.getAverage(), deviation);
	}

	public static void measureRuntime(int instanceId, String respValue, long start, long end) {
		// time, a value has been accepted after requested
		long gap = end - start;
		logger.info("{},{},{},{},{}", instanceId, respValue, start, end, gap);
		runtimeList.add(gap);
		atomicCounter.incrementAndGet();
	}
}
