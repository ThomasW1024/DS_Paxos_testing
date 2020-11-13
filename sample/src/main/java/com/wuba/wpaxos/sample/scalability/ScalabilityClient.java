package com.wuba.wpaxos.sample.scalability;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import com.wuba.wpaxos.comm.NodeInfo;
import com.wuba.wpaxos.sample.util.NodeUtil;

public class ScalabilityClient implements Runnable {

	private static Logger logger;
	private static final int LogPeriod = 10000;
	private static final int LifeTime = 300000;

	private static final int[] counter = { 0 };
	private int runningCycle;

	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			System.exit(1);
			// self address, address list, current Running Cycle(start with 0), boolean can this node propose
		}
		createLogger();

		// IP:port
		final NodeInfo myNode = NodeUtil.parseIpPort(args[0]);
		// Node List
		List<NodeInfo> nodeInfoList = NodeUtil.parseIpPortList(args[1]);

		// current cycle
		int currentCycle = Integer.parseInt(args[2]);
		// 50 byte
		final int sendSize = 50;

		final ScalabilityServer pxs = new ScalabilityServer(myNode, nodeInfoList);
		pxs.runPaxos(false);

		// if able to propose
		if (Boolean.parseBoolean(args[3])) {
//			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			Thread th = new Thread(() -> {
				while (true) {
//				CountDownLatch countDownLatch = new CountDownLatch(1);
					if (pxs.propose(generateData(sendSize)).getResult() == 0) {
						increaseCounter();
					}
				}
//			br.close();
			});
			th.setName("Input_thread_");
			th.setDaemon(true);
			th.start();
		}

		new ScalabilityClient(currentCycle).run();

	}

	// increment
	public synchronized static void increaseCounter() {
		counter[0] += 1;
	}

	// generate given byte size of data 
	private synchronized static byte[] generateData(int size) {
		byte[] b = new byte[size];
		new Random().nextBytes(b);
		return b;
	}

	private synchronized static void createLogger() throws FileNotFoundException, IOException {
		if (logger == null) {
			String log4jConfig = "." + File.separator + "conf" + File.separator + "log4j.xml";
			ConfigurationSource src = new ConfigurationSource(new FileInputStream(log4jConfig));
			Configurator.initialize(ScalabilityClient.class.getClassLoader(), src);
			logger = LogManager.getLogger(ScalabilityClient.class);
		}
		logger.info("logger init");
	}

	public ScalabilityClient(int current) {
		this.runningCycle = current;
	}

	// do the logging here
	@Override
	public synchronized void run() {
		int cycle = LifeTime / LogPeriod;
		for (int current = this.runningCycle; current < cycle; current += 1) {
			try {
				this.wait(LogPeriod);
			} catch (InterruptedException e) {
			}
			logger.info(current + "," + counter[0]);

		}
	}
}
