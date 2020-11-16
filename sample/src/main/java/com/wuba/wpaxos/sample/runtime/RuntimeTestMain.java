package com.wuba.wpaxos.sample.runtime;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.wuba.wpaxos.comm.NodeInfo;
import com.wuba.wpaxos.sample.util.NodeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.util.ExecutorServices;

public class RuntimeTestMain {
	private static final Logger logger = LogManager.getLogger(RuntimeTestMain.class);
	private static Executors executors;

	static void updateLogger(String rootDir, String nodeId){
		long currentTime = System.currentTimeMillis();
		System.setProperty("myLogFilename", rootDir + File.separator + nodeId +  "/"  + currentTime + ".log");
		System.setProperty("myMeasurementLog", rootDir + File.separator + nodeId + "/" + "measurement_" + currentTime + ".log");
		System.setProperty("totalMeasurementLog", rootDir + File.separator + "runtime_result.csv");
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		context.reconfigure();
//		context.updateLoggers();
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 7) {
			System.out.println("arguments num is wrong (required 7) , they are[rootPath{string},nodeName{String},myNode{ip{string}:port{int}},nodeList{ip{string}:port{int},ip{string}:port{int},...},groupCount{int},indexType{int},isProposer{boolean}]");
			System.out.println("e.g: ./ node1 127.0.0.1:30000 127.0.0.1:30000,127.0.0.1:30001,127.0.0.1:30002 1 1 false");
			System.out.println("Your Arg: " + Arrays.toString(args));
			System.exit(1);
		}
		String rootPath = args[0];
		String nodeName = args[1];
		updateLogger(rootPath, nodeName);
		NodeInfo myNode = NodeUtil.parseIpPort(args[2]);
		List<NodeInfo> nodeInfoList = NodeUtil.parseIpPortList(args[3]);
		int groupCount = Integer.parseInt(args[4]);
		int indexType = Integer.parseInt(args[5]);
		boolean isProposer = Boolean.parseBoolean(args[6]);
		String nodeId = nodeName + "_" + myNode.getIp() + ":" + myNode.getPort();
		LoggingUtil.splitterLog(logger, "Initialized Client - " + nodeId);
		EchoClient client = new EchoClient(nodeName, rootPath + "/db", myNode, nodeInfoList, groupCount, indexType);
		LoggingUtil.splitterLog(logger, "Waiting 3s for initialization");
		Thread.sleep(3000);
		if (isProposer) {
			logger.info("{} : is a proposer", nodeName);
			client.proposeValue(0, 10);
		} else {
			logger.info("{} : Not a proposer", nodeName);
		}
	}
}
