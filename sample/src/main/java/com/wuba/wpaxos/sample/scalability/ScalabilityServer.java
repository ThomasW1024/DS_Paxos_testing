package com.wuba.wpaxos.sample.scalability;

import java.io.File;
import java.util.List;

import com.wuba.wpaxos.ProposeResult;
import com.wuba.wpaxos.comm.GroupSMInfo;
import com.wuba.wpaxos.comm.NodeInfo;
import com.wuba.wpaxos.comm.Options;
import com.wuba.wpaxos.comm.enums.IndexType;
import com.wuba.wpaxos.node.Node;

import com.wuba.wpaxos.store.config.StoreConfig;
import com.wuba.wpaxos.storemachine.SMCtx;

public class ScalabilityServer {

	private NodeInfo myNode;
	private List<NodeInfo> nodeList;
	private Node paxosNode;
	private boolean useMaster = false;
	private String rootPath;
	private IndexType indexType = IndexType.LEVEL_DB;

	public ScalabilityServer(NodeInfo myNode, List<NodeInfo> nodeList) {
		this.myNode = myNode;
		this.rootPath = "./" + File.separator + "db" + File.separator + myNode.getNodeID();
		this.nodeList = nodeList;
	}

	// tracker use to add and kill process with added node
	// different group will not communication with each other according to the
	// documentation
	public void runPaxos(boolean isTracker) throws Exception {
		int gCount = 1;
		Options options = new Options();
		String logStoragePath = this.makeLogStoragePath();
		options.setGroupCount(gCount);
		options.setLogStoragePath(logStoragePath);
		options.setMyNode(this.myNode);
		options.setNodeInfoList(this.nodeList);
		options.setUseMembership(true);
		options.setUseBatchPropose(false);
		options.setIndexType(indexType);
		options.setStoreConfig(new StoreConfig(rootPath, null));

		for (int gid = 0; gid < gCount; gid++) {
			GroupSMInfo smInfo = new GroupSMInfo();
			smInfo.setUseMaster(this.useMaster);
			smInfo.setGroupIdx(gid);
			ScalabilitySM fileSM = new ScalabilitySM(gid);
			smInfo.getSmList().add(fileSM);
			options.getGroupSMInfoList().add(smInfo);
		}
		this.paxosNode = Node.runNode(options);
		this.paxosNode.setHoldPaxosLogCount(500000);

//		RocksDBHolder.init(gCount, rootPath);
	}

	public int addMember(NodeInfo node) throws Exception {
		return this.paxosNode.addMember(0, node);
	}
	
	public ProposeResult propose(byte[] writeReqValue) {
		SMCtx ctx = new SMCtx();
		ctx.setSmId(ScalabilitySM.SMID);
		this.paxosNode.setTimeoutMs(3000);
		ProposeResult proposeResult = this.paxosNode.propose(0, writeReqValue, ctx);
		return proposeResult;
	}

	private String makeLogStoragePath() {
		if (rootPath == null) {
			rootPath = System.getProperty("user.dir");
		}
		String logStoragePath = rootPath + File.separator + "db" + File.separator + myNode.getNodeID() + File.separator
				+ "db";
		File file = new File(logStoragePath);
		file.mkdirs();
		return logStoragePath;
	}
}
