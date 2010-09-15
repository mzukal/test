/*-----------------------------------------------------------------------------*
 *                                                                             *
 *       Copyright � 2010 STABILIT Informatik AG, Switzerland                  *
 *                                                                             *
 *  Licensed under the Apache License, Version 2.0 (the "License");            *
 *  you may not use this file except in compliance with the License.           *
 *  You may obtain a copy of the License at                                    *
 *                                                                             *
 *  http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                             *
 *  Unless required by applicable law or agreed to in writing, software        *
 *  distributed under the License is distributed on an "AS IS" BASIS,          *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 *  See the License for the specific language governing permissions and        *
 *  limitations under the License.                                             *
 *-----------------------------------------------------------------------------*/
package com.stabilit.sc.unit.techConnect;

import org.junit.Before;
import org.junit.Test;

import com.stabilit.sc.common.SCVersion;
import com.stabilit.sc.common.net.req.ConnectionPool;
import com.stabilit.sc.common.net.req.IConnection;
import com.stabilit.sc.common.net.req.IConnectionPool;
import com.stabilit.sc.common.scmp.SCMPHeaderAttributeKey;
import com.stabilit.sc.common.scmp.SCMPMessage;
import com.stabilit.sc.common.scmp.SCMPMsgType;
import com.stabilit.sc.common.util.DateTimeUtility;
import com.stabilit.sc.common.util.SynchronousCallback;
import com.stabilit.sc.unit.test.SetupTestCases;

public class PoolConnectTestCase {

	@Before
	public void setup() throws Exception {
		SetupTestCases.setupSCOverFile("scPerf.properties");
	}

	public void connect() throws Exception {
		IConnectionPool cp = new ConnectionPool("localhost", 8080, "netty.http", 0);
		String ldt = DateTimeUtility.getCurrentTimeZoneMillis();

		for (int i = 0; i < 500000; i++) {
			IConnection connection = cp.getConnection();
			SCMPMessage message = new SCMPMessage();
			message.setMessageType(SCMPMsgType.ATTACH);
			message.setHeader(SCMPHeaderAttributeKey.SC_VERSION, SCVersion.CURRENT.toString());
			message.setHeader(SCMPHeaderAttributeKey.LOCAL_DATE_TIME, ldt);
			ConnectCallback callback = new ConnectCallback();
			connection.send(message, callback);
			callback.getMessageSync();
			cp.freeConnection(connection);
			if (i % 1000 == 0) {
				System.out.println("connection nr " + i + " is done!");
			}
		}
	}

	@Test
	public void connectNewPool() throws Exception {

		String ldt = DateTimeUtility.getCurrentTimeZoneMillis();

		for (int i = 0; i < 500000; i++) {
			IConnectionPool cp = new ConnectionPool("localhost", 8080, "netty.http", 0);
			IConnection connection = cp.getConnection();
			SCMPMessage message = new SCMPMessage();
			message.setMessageType(SCMPMsgType.ATTACH);
			message.setHeader(SCMPHeaderAttributeKey.SC_VERSION, SCVersion.CURRENT.toString());
			message.setHeader(SCMPHeaderAttributeKey.LOCAL_DATE_TIME, ldt);
			ConnectCallback callback = new ConnectCallback();
			connection.send(message, callback);
			callback.getMessageSync();
			cp.freeConnection(connection);
			cp.destroy();
			if (i % 1000 == 0) {
				System.out.println("connection nr " + i + " is done!");
			}
		}
	}

	private class ConnectCallback extends SynchronousCallback {
		// nothing to implement in this case - everything is done by super-class
	}
}