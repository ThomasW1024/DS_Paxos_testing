package com.wuba.wpaxos.sample.scalability;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
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
	public static final long LogPeriod = 10000;
	public static final long LifeTime = 50000;

	private final int[] counter = { 0 };
	private int runningCycle;
	private int myPort;
	private ScalabilityServer svr;
	private List<Long> times ;

	private static final int nodeForAppend = 40000;

	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("not enough param");
			System.exit(1);
			// self address, address list, current Running Cycle(start with 0), boolean can this node propose
		}

		try {
			createLogger();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// IP:port
		final NodeInfo myNode = NodeUtil.parseIpPort(args[0]);
		// Node List
		List<NodeInfo> nodeInfoList = NodeUtil.parseIpPortList(args[1]);

		// current cycle
		int currentCycle = Integer.parseInt(args[2]);
		// 50 byte
		final int sendSize = 50;
		final ScalabilityClient self = new ScalabilityClient(currentCycle, Integer.parseInt(args[0].split(":")[1]));
		final ScalabilityServer pxs = new ScalabilityServer(myNode, nodeInfoList);
		self.svr = pxs;

		try {
			pxs.runPaxos();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// if able to propose
		if (Boolean.parseBoolean(args[3])) {
//			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			Thread th = new Thread(() -> {
				while (true) {
//				CountDownLatch countDownLatch = new CountDownLatch(1);
					self.times.add(System.currentTimeMillis());
					if (pxs.propose(generateData(sendSize)).getResult() == 0) {
						self.increaseCounter();
					}
					self.times.add(System.currentTimeMillis());
					// slight delay
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
//			br.close();
			});
			th.setName("Input_thread_");
			th.setDaemon(true);
			th.start();
		}

		self.run();

	}

	// increment
	public synchronized void increaseCounter() {
		this.counter[0] += 1;
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
			System.out.print(log4jConfig);
			ConfigurationSource src = new ConfigurationSource(new FileInputStream(log4jConfig));
			Configurator.initialize(ScalabilityClient.class.getClassLoader(), src);
			logger = LogManager.getLogger(ScalabilityClient.class);
		}
	}

	public ScalabilityClient(int current, int port) {
		this.runningCycle = current;
		this.myPort = port;
		this.times = Collections.synchronizedList(new ArrayList<Long>());
	}

	// do the logging here
	@Override
	public synchronized void run() {
		long cycle = LifeTime / LogPeriod;
		for (long current = this.runningCycle; current < cycle; current += 1) {

			try {
				this.wait(LogPeriod);
			} catch (InterruptedException e) {
			}
			logger.info(this.myPort + ":" + current + "," + counter[0]);
		}
		logger.info(this.myPort + ":" + this.times);
		try {
			this.wait(5000);
		} catch (InterruptedException e) {
		}

	}
}
