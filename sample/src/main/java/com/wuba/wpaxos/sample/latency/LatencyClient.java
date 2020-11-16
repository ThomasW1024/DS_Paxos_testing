/*
 * Copyright (C) 2005-present, 58.com.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wuba.wpaxos.sample.latency;

import com.wuba.wpaxos.comm.NodeInfo;
import com.wuba.wpaxos.sample.util.NodeUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class LatencyClient implements Runnable {

	private static Logger logger;
	public static long settleTime = 3000;

	private boolean droppable = false;
	private List<Long> latency;
	private String address;
	private String testplan;

	/**
	 * 
	 * @param args[0] String address of node
	 * @param args[1] String list of address
	 * @param args[2] Boolean can this node send proposal
	 * @param args[3] Boolean will this node drop itself after timeout
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 5) {
			System.out.println(
					"arguments num is wrong , they are [String-myNode] , [String-nodeList], [boolean-isProposable], [boolean-droppable], [Testplan]");
			System.exit(1);
		}

		String rootPath = "./";
		int groupCount = 1;
		int indexType = 0;
		final String addr = args[0];
		NodeInfo myNode = NodeUtil.parseIpPort(addr);
		createLogger("node_" + myNode.getNodeID());
		List<NodeInfo> nodeInfoList = NodeUtil.parseIpPortList(args[1]);
		boolean isProposable = Boolean.parseBoolean(args[2]);
		boolean droppable = Boolean.parseBoolean(args[3]);
		String testPlan = args[4];

//		String log4jConfig = rootPath + File.separator + "conf" + File.separator + "log4j.xml";
//		ConfigurationSource src = new ConfigurationSource(new FileInputStream(log4jConfig));
//		Configurator.initialize(LatencyClient.class.getClassLoader(), src);

		LatencyServer latencyServer = new LatencyServer(myNode, nodeInfoList, groupCount, rootPath, indexType);

		latencyServer.runPaxos();
		logger.info("latency server start, ip [" + myNode.getIp() + "] port [" + myNode.getPort() + "]");
		final LatencyClient clt = new LatencyClient(droppable, addr, testPlan);

		final Random random = new Random();

		if (isProposable) {
			Thread th = new Thread(() -> {
				int idx = 0;
				// wait for nodes to settle
				try {
					Thread.sleep(settleTime);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				AtomicLong startStamp = new AtomicLong(System.currentTimeMillis());
				while (true) {
					try {
						String val = generateData(64).toString();
						String latencyRespValue = latencyServer.propose(val, 0);
						long endStamp = System.currentTimeMillis();
						if (latencyRespValue != null) {
							clt.latency.add((endStamp - startStamp.get()));
						} else {
							continue;
						}
						Thread.sleep(random.nextInt(5) * 100);
						startStamp.set(System.currentTimeMillis());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			th.setName("Node_" + addr);
			th.setDaemon(true);
			th.start();

			clt.run();
		}

	}

	public LatencyClient(boolean droppable, String address, String testPlan) {
		this.droppable = droppable;
		this.latency = new ArrayList<>();
		this.address = address;
		this.testplan = testPlan;
	}

	// generate given byte size of data
	private synchronized static byte[] generateData(int size) {
		byte[] b = new byte[size];
		new Random().nextBytes(b);
		return b;
	}

	@Override
	public synchronized void run() {
		while (true) {
			try {
				if (this.droppable) {
					this.wait(new Random().nextInt(20) * 100 + 5000);
					break;
				} else {
					this.wait(20000);
					write(this.address, this.latency.toString(), this.testplan);
				}
			} catch (InterruptedException e) {
				break;
			}
		}
		write(this.address, this.latency.toString(), this.testplan);
		try {
			this.wait(2000);
		} catch (InterruptedException e) {
		}
		return;
	}

	public synchronized static void write(String fileName, String data, String testplan) {
		try {
			FileWriter myWriter = new FileWriter("./logs/latency/" + testplan + "/" + fileName + ".txt", true);
			myWriter.write(data);
			myWriter.close();
			System.out.println("Successfully wrote to the file.");
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	private synchronized static void createLogger(String nodeName)  {
		if (logger == null) {

			String currentDir = System.getProperty("user.dir");
			System.out.println(currentDir);
			String log4jConfig = currentDir + "/conf" + File.separator + "log4j.xml";
			File logCofigFle = new File(log4jConfig);
			if (log4jConfig.isEmpty()) {
				throw new RuntimeException("log4J.xml has not found at root " + currentDir);
			}
			System.out.println(log4jConfig);
			//ConfigurationSource src = new ConfigurationSource(new FileInputStream(log4jConfig));
			Configurator.initialize(null, log4jConfig);
			System.setProperty("nodeName", nodeName);
			LoggerContext context = (LoggerContext) LogManager.getContext(false);
			context.updateLoggers();
			logger = LogManager.getLogger(LatencyClient.class);
			logger.info("---Initialed Logger---");
		}
	}

}
