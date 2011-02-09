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
package org.serviceconnector.test.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.serviceconnector.Constants;
import org.serviceconnector.SCVersion;
import org.serviceconnector.TestCallback;
import org.serviceconnector.TestConstants;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.net.connection.ConnectionPool;
import org.serviceconnector.net.connection.ConnectionPoolBusyException;
import org.serviceconnector.net.connection.IConnection;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.util.DateTimeUtility;

/**
 * @author JTraber
 */
@RunWith(Parameterized.class)
public class ConnectionPoolTest extends IntegrationSuperTest {

	private int port;
	private ConnectionType connectionType;
	private int keepAlivSeconds = 1;
	private ConnectionPool connectionPool;

	public ConnectionPoolTest(Integer port, ConnectionType connectionType) {
		this.port = port;
		this.connectionType = connectionType;
	}

	@Parameters
	public static Collection<Object[]> getParameters() {
		return Arrays.asList( //
				new Object[] { new Integer(TestConstants.PORT_SC_TCP), ConnectionType.NETTY_TCP }, //
				new Object[] { new Integer(TestConstants.PORT_SC_HTTP), ConnectionType.NETTY_HTTP });
	}

	@Before
	public void beforeOneTest() throws Exception {
		super.beforeOneTest();
		connectionPool = new ConnectionPool(TestConstants.HOST, this.port, this.connectionType.getValue(), this.keepAlivSeconds);
	}

	@After
	public void afterOneTest() throws Exception {
		try {
			connectionPool.destroy();
		} catch (Exception e) {
		}
		connectionPool = null;
		super.afterOneTest();
	}

	/**
	 * Description: Gets one connection and frees it<br>
	 * Expectation: passes
	 */
	@Test
	public void t01_GetAndFreeConnection() throws Exception {
		IConnection connection = connectionPool.getConnection();
		connectionPool.freeConnection(connection);
	}

	/**
	 * Description: Set max connection to 2, tries getting 3 connections<br>
	 * Expectation: throws ConnectionPoolBusyException
	 */
	@Test(expected = ConnectionPoolBusyException.class)
	public void t10_ConnectionPoolBusy() throws Exception {
		connectionPool.setMaxConnections(2);
		for (int i = 0; i < 3; i++) {
			connectionPool.getConnection();
		}
		Assert.assertFalse(connectionPool.hasFreeConnections());
		connectionPool.getConnection();
	}

	/**
	 * Description: Set max connection to 2, gets 2 connections<br>
	 * Expectation: passes
	 */
	@Test
	public void t11_ConnectionPoolHasNoFreeConnection() throws Exception {
		connectionPool.setMaxConnections(2);
		for (int i = 0; i < 2; i++) {
			connectionPool.getConnection();
		}
		Assert.assertFalse(connectionPool.hasFreeConnections());
	}

	/**
	 * Description: Send keep alive over connection<br>
	 * Expectation: passes
	 */
	@Test
	public void t20_KeepAliveActive() throws Exception {
		connectionPool.setMaxConnections(1);
		connectionPool.setMinConnections(1);
		connectionPool.initMinConnections();
		Thread.sleep((long) ((this.keepAlivSeconds + 0.2) * Constants.SEC_TO_MILLISEC_FACTOR));
		IConnection connection = connectionPool.getConnection();
		Assert.assertTrue(connection.getNrOfIdlesInSequence() > 0);
		connectionPool.freeConnection(connection);
	}

	/**
	 * Description: Get connection assert no keep alive will be sent<br>
	 * Expectation: passes
	 */
	@Test
	public void t21_NoKeepAliveForUsedConnection() throws Exception {
		connectionPool.setMaxConnections(1);
		IConnection connection = connectionPool.getConnection();
		Thread.sleep((long) ((this.keepAlivSeconds + 0.2) * Constants.SEC_TO_MILLISEC_FACTOR));
		Assert.assertTrue(connection.getNrOfIdlesInSequence() == 0);
		Assert.assertTrue(connection.isConnected());
	}

	/**
	 * Description: Close after free connection<br>
	 * Expectation: passes
	 */
	@Test
	public void t30_CloseAfterFreeConnection() throws Exception {
		connectionPool.setCloseOnFree(true);
		IConnection connection = connectionPool.getConnection();
		IConnection connection2 = connectionPool.getConnection();
		connectionPool.freeConnection(connection);
		Assert.assertFalse(connection.isConnected());
		connectionPool.freeConnection(connection2);
		Assert.assertTrue(connection2.isConnected());
	}

	/**
	 * Description: Test 100 connections in use<br>
	 * Expectation: passes
	 */
	@Test
	public void t40_HundredConnectionsInUse() throws Exception {
		List<IConnection> connections = new ArrayList<IConnection>();
		for (int i = 0; i < 100; i++) {
			connections.add(connectionPool.getConnection());
		}
		Assert.assertFalse(connectionPool.hasFreeConnections());
	}

	/**
	 * Description: Get connection send a message and free connection - 10000 times<br>
	 * Expectation: passes
	 */
	@Test
	public void t50_GetConnectionSendAttachFreeConnection10000Times() throws Exception {
		String ldt = DateTimeUtility.getCurrentTimeZoneMillis();
		SCMPMessage message = new SCMPMessage();
		message.setMessageType(SCMPMsgType.ATTACH);
		message.setHeader(SCMPHeaderAttributeKey.SC_VERSION, SCVersion.CURRENT.toString());
		message.setHeader(SCMPHeaderAttributeKey.LOCAL_DATE_TIME, ldt);

		for (int i = 0; i < 10000; i++) {
			IConnection connection = connectionPool.getConnection();
			TestCallback cbk = new TestCallback();
			connection.send(message, cbk);
			cbk.getMessageSync(1000);
			connectionPool.freeConnection(connection);
			if ((i + 1) % 5000 == 0) {
				testLogger.info("connection nr " + (i + 1) + "...");
			}
		}
	}

	/**
	 * Description: Create new ConnectionPool get connection send a message and free connection destroy pool - 10000 times<br>
	 * Expectation: passes
	 */
	@Test
	public void t51_NewConnectionPoolGetConnectionSendAttachFreeConnection10000Times() throws Exception {
		String ldt = DateTimeUtility.getCurrentTimeZoneMillis();
		SCMPMessage message = new SCMPMessage();
		message.setMessageType(SCMPMsgType.ATTACH);
		message.setHeader(SCMPHeaderAttributeKey.SC_VERSION, SCVersion.CURRENT.toString());
		message.setHeader(SCMPHeaderAttributeKey.LOCAL_DATE_TIME, ldt);

		for (int i = 0; i < 10000; i++) {
			ConnectionPool cp = new ConnectionPool(TestConstants.HOST, this.port, this.connectionType.getValue(),
					this.keepAlivSeconds);
			IConnection connection = cp.getConnection(); // TODO JOT ##testing l�uft bei mir, mit Anzahl spielen auf VM 10000Times
			TestCallback cbk = new TestCallback();
			connection.send(message, cbk);
			cbk.getMessageSync(1000);
			cp.freeConnection(connection);
			cp.destroy();
			if ((i + 1) % 5000 == 0) {
				testLogger.info("connection nr " + (i + 1) + "...");
			}
		}
	}
}
