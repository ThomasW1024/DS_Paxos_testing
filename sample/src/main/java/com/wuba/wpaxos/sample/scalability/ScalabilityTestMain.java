package com.wuba.wpaxos.sample.scalability;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.wuba.wpaxos.sample.util.NodeUtil;

public class ScalabilityTestMain {
	
	private static final int[] total = {3};
	
	private static final String host = "127.0.0.1:";
	private static final int g2Port = 29999;
	private static final int portStart = 30000;
	private static ScalabilityServer observerNode;
	
	private static final Map<Integer, Process> processes = new HashMap<>();
	public static void main(String[] args) throws Exception {
		observerNode = new ScalabilityServer(NodeUtil.parseIpPort(host+g2Port), NodeUtil.parseIpPortList(host+g2Port));
		observerNode.runPaxos(true);
		String nodeList = createNodeList(3);
		
	}
	
	public static String createNodeList(int number) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < number; i+=1) {
			if (sb.length() != 0) {
				sb.append(",");
			}
			String addr = host + (portStart + i);
			sb.append(addr);
		}
		return sb.toString();
	}
	
	public static void createProcess(String list) {
		for( String s: list.split(",")) {
			Process p = null;
			processes.put(processes.size()+1, p);
		}
	}
	
	public static void addNode(String addr) throws Exception {
		
		// add member success
		if (observerNode.addMember(NodeUtil.parseIpPort(addr)) == 0) {
			Process p = null;
			processes.put(processes.size()+1, p);
			total[0] += 1;
		};
	}

	public static void cleanUp() {
		for( Entry<Integer,Process> set : processes.entrySet()) {
			set.getValue().destroyForcibly();
		}
	}
}
