package com.wuba.wpaxos.sample.testcase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.RandomStringUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

public class RuntimeTestCaseMain {

	private static int counter = 0;
	private static ExecutorService executorService;

	private static void stopApp() {
		try {
			System.out.println("Stop The Application");
			executorService.shutdown();
			Thread.sleep(1000);
			System.out.println("All Process has been finished");
			System.exit(-1);
		}
		catch (InterruptedException exception) {
			exception.printStackTrace();
		}
	}

	public static void main(String[] args) throws InterruptedException {
		if (args.length != 6) {
			System.out.println("Argument invalid");
			System.out.println("number of node, percentage of proposer, percentage of failure, propose value wait time, message bytes length, timeout in sec");
			System.out.println("Your input: " + Arrays.toString(args));
			throw new RuntimeException("Argument is invalid");
		}
		int numberOfNode = Integer.parseInt(args[0]);
		int proposerNum = (int)Math.ceil(Integer.parseInt(args[1])/100.0 * numberOfNode);
		int failurePercentage = Integer.parseInt(args[2]);

		int failureNodes = 0;
		if (failurePercentage > 0) {
			failureNodes = (int)Math.floor(failurePercentage/100.0 * numberOfNode);
			if (failureNodes > ((numberOfNode/2) + 1)) {
				failureNodes = failureNodes - 1;
			}
			if (failureNodes == 0) {
				failureNodes = 1;
			}
		}
		int proposedWaitTime = Integer.parseInt(args[3]);
		int msgBytesLength = Integer.parseInt(args[4]);
		long processTimeOut = Long.parseLong(args[5]);
		String rootDir = System.getProperty("user.dir");
		String sampleDir = rootDir + "/sample";
		System.out.println(rootDir);
		String testcaseName = String
				.format("testcase_%dn_%dp_%df_%dw_%dl", numberOfNode, proposerNum, failureNodes, proposedWaitTime, msgBytesLength);
		System.out.println("Testcase name: " + testcaseName);
		final File loadExecuteJar = new File(sampleDir + "/target/paxos_runtime_testing-jar-with-dependencies.jar");
		if (loadExecuteJar.exists()) {
			System.out.println("sample dir: " + sampleDir);
			System.out.println("Jar file existed at: " + loadExecuteJar.getAbsolutePath());
			String testCaseRootPath = sampleDir + File.separator + testcaseName;
			int initialPort = 10001;
			List<String> nodeAddressList = new ArrayList<>();
			List<ProcessExecutor> processExecutors = new ArrayList<>();
			List<Boolean> proposerList = new ArrayList<>(numberOfNode);
			for (int i = 1; i <= numberOfNode; i++) {
				String nodeAddress = "127.0.0.1:" + initialPort;
				nodeAddressList.add(nodeAddress);
				initialPort ++;
				if (i <= proposerNum) {
					proposerList.add(true);
				} else {
					proposerList.add(false);
				}
			}
			// shuffle the proposer list
			Collections.shuffle(proposerList);

			String nodesAddresses = String.join(",", nodeAddressList);
			int failureNodeCounter = 0;
			for (int i = 0; i < nodeAddressList.size(); i++) {
				String nodeAddress = nodeAddressList.get(i);
				boolean isProposer = proposerList.get(i);
				if (!isProposer) {
					if (failureNodeCounter < failureNodes) {
						processExecutors.add(new ProcessExecutor().command("java", "-jar", loadExecuteJar.getPath(), testCaseRootPath, "node" + (i + 1), nodeAddress, nodesAddresses, "1","0", "false").redirectOutput(Slf4jStream.of(RuntimeTestCaseMain.class).asInfo()).destroyOnExit().timeout(5, TimeUnit.SECONDS));
						failureNodeCounter++;
						continue;
					}
				}
				processExecutors.add(new ProcessExecutor().command("java", "-jar", loadExecuteJar.getPath(), testCaseRootPath, "node" + (i + 1), nodeAddress, nodesAddresses, "1","0", isProposer ? "true" : "false").redirectOutput(Slf4jStream.of(RuntimeTestCaseMain.class).asInfo()).destroyOnExit().timeout(processTimeOut, TimeUnit.SECONDS));
			}
			executorService = Executors.newFixedThreadPool(processExecutors.size());
			for (ProcessExecutor process : processExecutors) {
				executorService.submit(() -> {
					try {
						process.execute();
					}
					catch (InterruptedException | TimeoutException | IOException exception) {
						exception.printStackTrace();
						counter = counter + 1;
						System.out.println("Stop current thread: " + Thread.currentThread().getName());
						Thread.currentThread().interrupt();
						if (counter == processExecutors.size()) {
							stopApp();
						}
					}
				});
			}
		} else {
			new RuntimeException("Jar file is not existed");
		}
	}

}
