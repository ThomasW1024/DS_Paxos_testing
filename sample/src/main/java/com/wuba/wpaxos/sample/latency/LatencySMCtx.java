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

public class LatencySMCtx {
	public int executeRet;
	private byte[] latencyRespValue;

	public LatencySMCtx() {
		executeRet = -1;
	}

	public int getExecuteRet() {
		return executeRet;
	}

	public void setExecuteRet(int executeRet) {
		this.executeRet = executeRet;
	}

	public byte[] getLatencyRespValue() {
		return latencyRespValue;
	}

	public void setLatencyRespValue(byte[] latencyRespValue) {
		this.latencyRespValue = latencyRespValue;
	}
}
