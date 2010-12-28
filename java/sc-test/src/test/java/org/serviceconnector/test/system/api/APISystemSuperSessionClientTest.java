/*
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
 */
package org.serviceconnector.test.system.api;

import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.serviceconnector.TestConstants;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.api.cln.SCMessageCallback;
import org.serviceconnector.api.cln.SCSessionService;
import org.serviceconnector.ctrl.util.ProcessCtx;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.service.SCServiceException;
import org.serviceconnector.test.system.SystemSuperTest;

public class APISystemSuperSessionClientTest extends SystemSuperTest {

	protected SCClient client;
	protected SCSessionService sessionService = null;
	protected ProcessCtx sesSrvCtx;
	protected MsgCallback msgCallback = null;
	protected static boolean messageReceived = false;
	
	@Before
	public void beforeOneTest() throws Exception {
		super.beforeOneTest();
		sesSrvCtx = ctrl.startServer(TestConstants.COMMUNICATOR_TYPE_SESSION, TestConstants.log4jSrvProperties,
				TestConstants.sesServerName1, TestConstants.PORT_SES_SRV_TCP, TestConstants.PORT_SC_TCP, 100, 10,
				TestConstants.sesServiceName1);
		client = new SCClient(TestConstants.HOST, TestConstants.PORT_SC_TCP, ConnectionType.NETTY_TCP);
		client.attach();
		messageReceived = false;
	}

	@After
	public void afterOneTest() throws Exception {
		try {
			sessionService.deleteSession();
		} catch (Exception e1) {
		}
		sessionService = null;
		try {
			client.detach();
		} catch (Exception e) {
		}
		client = null;
		try {
			ctrl.stopServer(sesSrvCtx);
		} catch (Exception e) {
		}
		sesSrvCtx = null;
		super.afterOneTest();
	}

	protected class MsgCallback extends SCMessageCallback {
		private SCMessage response = null;

		public MsgCallback(SCSessionService service) {
			super(service);
		}

		public SCMessage getResponse() {
			return response;
		}

		public void waitForMessage(int nrSeconds) throws Exception {
			for (int i = 0; i < (nrSeconds * 10); i++) {
				if (APISystemSuperSessionClientTest.messageReceived) {
					return;
				}
				Thread.sleep(100);
			}
			throw new TimeoutException("No message received within " + nrSeconds + " seconds timeout.");
		}
		@Override
		public void receive(SCMessage msg) {
			response = msg;
			APISystemSuperSessionClientTest.messageReceived = true;
		}

		@Override
		public void receive(Exception e) {
			testLogger.info("Error received");
			if (e instanceof SCServiceException) {
				logger.info("SC error received code:" + ((SCServiceException) e).getSCErrorCode() + " text:"
						+ ((SCServiceException) e).getSCErrorText());
			} else {
				logger.error("receive error: " + e.getMessage());
			}
			response = null;
			APISystemSuperSessionClientTest.messageReceived = true;
		}
	}
}