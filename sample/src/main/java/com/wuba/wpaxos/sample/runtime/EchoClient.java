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
package com.wuba.wpaxos.sample.runtime;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.wuba.wpaxos.comm.NodeInfo;
import com.wuba.wpaxos.sample.util.NodeUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EchoClient {
	private static final Logger logger = LogManager.getLogger(EchoClient.class);
	private ScheduledExecutorService scheduledService;
	private EchoServer myEchoServer;
	private AtomicInteger idxAtomicInteger;
	private final String nodeId;
	private boolean isStopToRun = false;

	public EchoClient(String nodeId, String rootPath, NodeInfo myNode, List<NodeInfo> nodeInfoList, int groupCount, int indexType) throws Exception{
		this.nodeId = nodeId;
//		this.scheduledService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat(nodeId).build());
		this.idxAtomicInteger = new AtomicInteger(0);
		this.myEchoServer = new EchoServer(myNode, nodeInfoList, groupCount, rootPath, indexType);
		this.myEchoServer.runPaxos();
		logger.info("echo server start, ip [" + myNode.getIp() + "] port [" + myNode.getPort() + "]");
	}

	public void proposeValue(long delayInSec, long durationInSec) {
		logger.info("start proposing value");
		RuntimeTimer timer = new RuntimeTimer(this.nodeId, true, delayInSec, durationInSec, new RuntimeTimer.TimerDelegate() {
			@Override
			public void tick() {
				logger.info("ticking...");
			}

			@Override
			public void timeUp() {
				LoggingUtil.splitterLog(logger, "TIMEUP: " + durationInSec + "s");
//				scheduledService.shutdownNow();
//				while (!scheduledService.isTerminated()) {
//
//				}

				logger.info("Shutdown the propose-value task");
			}
		});
		timer.start();

//		this.scheduledService.schedule(() -> {
//			try {
//				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//				int idxTemp;
//				while (true) {
////				System.out.println("please input : ");
////				String echoReqValue = br.readLine();
//					String echoReqValue = "1";
//					if ("quit".equals(echoReqValue)) break;
//					if ("1".equals(echoReqValue)) {
//						for (int i = 0; i < 50; i++) {
//							idxTemp = this.idxAtomicInteger.getAndIncrement();
//							String echoRespValue = this.myEchoServer.echo("wpaxos_test" + idxTemp, 0);
//							logger.info("echo response : " + echoRespValue);
//							logger.info("sleep: 1000");
//							Thread.sleep(1000);
//							logger.info("awakening");
//						}
//					}
//					else {
//						this.myEchoServer.addMember(NodeUtil.parseIpPort(echoReqValue));
//						logger.info(this.myEchoServer.getAllMembers());
//					}
//				}
//				br.close();
//			}
//			catch (Exception exception) {
//				exception.printStackTrace();
//			}
//		}, delayInSec, TimeUnit.SECONDS);
		this.proposeValueRunner();
	}

	private void proposeValueRunner() {
		try {
			logger.info("start propose value");
			int idxTemp;
			int maxProposedCounter = 50;

//				System.out.println("please input : ");
//				String echoReqValue = br.readLine();
				//String echoReqValue = "1";
				RuntimeMetric.startMeasurement(this.nodeId);
				String testMsgFormat = this.nodeId + ":" + RandomStringUtils.random(8, true, false) + "_test_";
				idxTemp = this.idxAtomicInteger.getAndIncrement();
				long startTime = 0;
				for (int i = 0; i < maxProposedCounter; ) {
					if (startTime == 0) {
						startTime = System.currentTimeMillis();
					}
					String echoReqValue = testMsgFormat + idxTemp;
					logger.info("echo request -> " + echoReqValue);
					String echoRespValue = this.myEchoServer.echo(echoReqValue, 0);
					if (echoRespValue != null) {
						RuntimeMetric.measureRuntime(idxTemp, echoRespValue, startTime, System.currentTimeMillis());
						// if it is consensus
						idxTemp = this.idxAtomicInteger.getAndIncrement();
						i++;
						// reset start time
						startTime = 0;
					}
					logger.info("echo response <- " + echoRespValue);
					logger.info("sleep: 100 ms");
					Thread.sleep(100);
					logger.info("awakening");
				}
				RuntimeMetric.endMeasurement();
				LoggingUtil.splitterLog(logger, "PROPOSED: " + (this.idxAtomicInteger.intValue() - 1));
//				else {
//					this.myEchoServer.addMember(NodeUtil.parseIpPort(echoReqValue));
//					logger.info(this.myEchoServer.getAllMembers());
//				}
			//Thread.sleep(1000);
			//System.exit(-1);
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
