package com.wuba.wpaxos.sample.scalability;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.wuba.wpaxos.sample.util.NodeUtil;

public class ScalabilityTestMain {

	private static final int[] total = { 3 };

	public static final String host = "127.0.0.1:";
	public static final int portStart = 30000;
	private static String nodeList = "";
//	private static ScalabilityServer observerNode = null;

	private static final Map<Integer, Process> processes = new HashMap<>();

	public static void main(String[] args) {
//		observerNode = new ScalabilityServer(NodeUtil.parseIpPort(host + g2Port),
//				NodeUtil.parseIpPortList(host + g2Port));
//		Thread t = new Thread(()->{
//			try {
//				observerNode.runPaxos();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		});
//		t.setDaemon(true);
//		t.run();
		
		nodeList = createNodeList(total[0]);
		createProcesses(nodeList, 0, true);

		new ScalabilityTestMain().sleep(false);
		System.out.println("end");
		return;
	}

	public synchronized void sleep(boolean addMember) {
//		if (addMember) {
//			int cycle = (int) (ScalabilityClient.LifeTime / ScalabilityClient.LogPeriod);
//			for (int current = 0; current < cycle; current += 1) {
//				try {
//					Thread.sleep(ScalabilityClient.LogPeriod);
//					System.out.println("adding new node");
//					String nAddr = host + (portStart + total[0]);
//					nodeList = nodeList + "," + nAddr;
//					Thread.sleep(100);
//					addNode(nAddr, cycle, true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		} else {
			try {
				Thread.sleep(ScalabilityClient.LifeTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//		}
		
		// wait to end
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cleanUp();
	}

	public synchronized static String createNodeList(int number) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < number; i += 1) {
			if (sb.length() != 0) {
				sb.append(",");
			}
			String addr = host + (portStart + i);
			sb.append(addr);
		}
		return sb.toString();
	}

	public synchronized static void createProcesses(String list, int currentCycle, boolean isProposable) {
		boolean a = true;
		for (String s : list.split(",")) {
			Process p = createProcess(s, currentCycle, a);
			a = false;
			processes.put(processes.size() + 1, p);
		}
	}

//	public synchronized static void addNode(String addr, int currentCycle, boolean isProposable) throws Exception {
		// add member success
//		if (observerNode.addMember(NodeUtil.parseIpPort(addr)) == 0) {
//			Process p = createProcess(addr, currentCycle, isProposable);
//			if (p != null) {
//				System.out.println(addr + " is started ");
//				
//				processes.put(processes.size() + 1, p);
//				total[0] += 1;
//			} else {
//				System.out.println("failed to run");
//			}
//		}else {
//			System.out.println("failed to add");
//		}
//	}

	public synchronized static void cleanUp() {
		for (Entry<Integer, Process> set : processes.entrySet()) {
			if (set.getValue() != null) {
				set.getValue().destroyForcibly();
			}
		}
	}

	public synchronized static Process createProcess(String addr, int currentCycle, boolean isProposable) {
		try {
			String cmd = "java -cp target/wpaxos.sample-1.0.0.jar:target/dependency/* com.wuba.wpaxos.sample.scalability.ScalabilityClient "
					+ addr + " " + nodeList + " " + currentCycle + " " + isProposable;
			return Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
