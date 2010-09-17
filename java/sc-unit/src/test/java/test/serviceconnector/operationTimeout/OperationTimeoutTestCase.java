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
package test.serviceconnector.operationTimeout;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.serviceconnector.call.SCMPCallFactory;
import org.serviceconnector.call.SCMPClnCreateSessionCall;
import org.serviceconnector.conf.RequesterConfigPool;
import org.serviceconnector.net.req.IConnectionPool;
import org.serviceconnector.net.req.Requester;
import org.serviceconnector.scmp.ISCMPSynchronousCallback;
import org.serviceconnector.scmp.SCMPFault;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.util.SynchronousCallback;

import test.serviceconnector.attach.SuperAttachTestCase;
import test.serviceconnector.pool.TestContext;
import test.serviceconnector.unit.SetupTestCases;


public class OperationTimeoutTestCase extends SuperAttachTestCase {

	public OperationTimeoutTestCase(String fileName) {
		super(fileName);
	}

	@Override
	@Before
	public void setup() throws Exception {
		SetupTestCases.setupSCSessionServer1Connections();
		try {
			this.config = new RequesterConfigPool();
			this.config.load(fileName);
			this.testContext = new TestContext(this.config.getRequesterConfig(), this.msgId);
			req = new Requester(this.testContext);
			IConnectionPool cp = this.testContext.getConnectionPool();
			cp.setCloseOnFree(true);
			cp.setMinConnections(5);
			cp.initMinConnections();
		} catch (Exception e) {
			e.printStackTrace();
		}
		clnAttachBefore();
	}

	@Test
	public void callbackOperationTimedOutOnClientSynchronCommunicationLateCCSFreeConnection() throws Exception {
		// sets up a create session call
		SCMPClnCreateSessionCall createSessionCall = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(req, "simulation");
		createSessionCall.setSessionInfo("sessionInfo");
		createSessionCall.setEchoIntervalSeconds(300);

		// time to wait in create session on server must be lower than SC operation timeout, 1 second
		createSessionCall.setRequestBody("wait:" + 2000);
		ISCMPSynchronousCallback callback = new SynchronousCallback() {
		};
		createSessionCall.invoke(callback, 2000);

		// timeout on callback must be the lowest value, 1/2 seconds
		SCMPFault fault = (SCMPFault) callback.getMessageSync(20000);
		Assert
				.assertEquals(
						"Request Timeout. The client did not produce a request within the time that the server was prepared to wait. [Getting message synchronous failed]",
						fault.getCause().getMessage());
		// wait for CCS to be received to late but initiates freeing the connection
		Thread.sleep(2000);
		// verify all connections freed properly
		IConnectionPool pool = this.testContext.getConnectionPool();
		int busyConnections = pool.getBusyConnections();
		Assert.assertEquals(0, busyConnections);
	}

	@Test
	public void callbackOperationTimedOutOnClientSynchronCommunicationIdleTimeoutFreeConnection() throws Exception {
		// sets up a create session call
		SCMPClnCreateSessionCall createSessionCall = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(req, "simulation");
		createSessionCall.setSessionInfo("sessionInfo");
		createSessionCall.setEchoIntervalSeconds(300);

		// time to wait in create session on server must be higher than SC operation timeout
		createSessionCall.setRequestBody("wait:" + 8000);
		ISCMPSynchronousCallback callback = new SynchronousCallback() {
		};
		createSessionCall.invoke(callback, 3);

		// time to wait on client must be lower than operation timeout on SC & waiting time on server
		SCMPFault responseMessage = (SCMPFault) callback.getMessageSync(500);
		Assert.assertTrue(responseMessage.isFault());
		Assert.assertEquals("time for receiving message run out. Getting message synchronous failed.", responseMessage
				.getCause().getMessage());
		// wait for idle connection to be received to initiates freeing the connection
		Thread.sleep(3000);
		// verify all connections freed properly
		IConnectionPool pool = this.testContext.getConnectionPool();
		int busyConnections = pool.getBusyConnections();
		Assert.assertEquals(0, busyConnections);
	}

	@Test
	public void idleOperationTimedOutOnClientSynchronCommunicationIdleTimeoutFreeConnection() throws Exception {
		// sets up a create session call
		SCMPClnCreateSessionCall createSessionCall = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(req, "simulation");
		createSessionCall.setSessionInfo("sessionInfo");
		createSessionCall.setEchoIntervalSeconds(300);

		// time to wait in create session on server must be higher than SC operation timeout
		createSessionCall.setRequestBody("wait:" + 3000);
		ISCMPSynchronousCallback callback = new SynchronousCallback() {
		};
		createSessionCall.invoke(callback, 3);
		// time to wait on client is default operation timeout & runs out first
		SCMPMessage msg = callback.getMessageSync();

		SCMPFault responseMessage = (SCMPFault) msg;
		Assert.assertTrue(responseMessage.isFault());

		if (responseMessage.getCause() == null) {
			// operation timeout occurred on SC - result contains SC error text
			Assert.assertEquals("Operation timeout - operation could not be completed.", responseMessage
					.getHeader(SCMPHeaderAttributeKey.SC_ERROR_TEXT));
		} else {
			// operation timeout occurred on client - result contains exception
			Assert.assertEquals("operation timeout. operation - could not be completed.", responseMessage.getCause()
					.getMessage());
		}
		// wait for freeing the connection not necessary - connection already freed - verify
		IConnectionPool pool = this.testContext.getConnectionPool();
		int busyConnections = pool.getBusyConnections();
		Assert.assertEquals(0, busyConnections);
	}

	@Test
	public void operationTimedOutOnClientAsynchronCommunication() throws Exception {
		// sets up a create session call
		SCMPClnCreateSessionCall createSessionCall = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(req, "simulation");
		createSessionCall.setSessionInfo("sessionInfo");
		createSessionCall.setEchoIntervalSeconds(300);

		// time to wait in create session on server must be lower than SC operation timeout, 1 second
		createSessionCall.setRequestBody("wait:" + 4000);
		ISCMPSynchronousCallback callback = new SynchronousCallback() {
		};
		createSessionCall.invoke(callback, 3);
		// wait for CCS to be received to late but initiates freeing the connection
		Thread.sleep(3000);
		// verify all connections freed properly
		IConnectionPool pool = this.testContext.getConnectionPool();
		int busyConnections = pool.getBusyConnections();
		Assert.assertEquals(0, busyConnections);
	}
}