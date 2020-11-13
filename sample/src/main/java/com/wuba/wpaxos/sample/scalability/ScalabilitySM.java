package com.wuba.wpaxos.sample.scalability;

import java.util.List;

import com.wuba.wpaxos.storemachine.SMCtx;
import com.wuba.wpaxos.storemachine.StateMachine;
import com.wuba.wpaxos.utils.JavaOriTypeWrapper;

public class ScalabilitySM implements StateMachine {
	public static final int SMID = 3;
	private int groupId;
	private long checkPointInstanceId;

	public ScalabilitySM(int group) {
		this.groupId = group;
	}

	@Override
	public int getSMID() {
		return SMID;
	}

	@Override
	public boolean execute(int groupIdx, long instanceID, byte[] paxosValue, SMCtx smCtx) {
		executeForCheckpoint(groupIdx, instanceID, paxosValue);
		return true;
	}

	@Override
	public boolean executeForCheckpoint(int groupIdx, long instanceID, byte[] paxosValue) {
		this.checkPointInstanceId = instanceID;
		return false;
	}

	@Override
	public long getCheckpointInstanceID(int groupIdx) {
		return checkPointInstanceId;
	}

	@Override
	public int lockCheckpointState() {
		return 0;
	}

	@Override
	public int getCheckpointState(int groupIdx, JavaOriTypeWrapper<String> dirPath, List<String> fileList) {
		return 0;
	}

	@Override
	public void unLockCheckpointState() {
	}

	@Override
	public int loadCheckpointState(int groupIdx, String checkpointTmpFileDirPath, List<String> fileList,
			long checkpointInstanceID) {
		return 0;
	}

	@Override
	public byte[] beforePropose(int groupIdx, byte[] sValue) {
		return null;
	}

	@Override
	public boolean needCallBeforePropose() {
		return false;
	}

	@Override
	public void fixCheckpointByMinChosenInstanceId(long minChosenInstanceID) {
		if (minChosenInstanceID > checkPointInstanceId) {
			checkPointInstanceId = minChosenInstanceID;
		}
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
}
