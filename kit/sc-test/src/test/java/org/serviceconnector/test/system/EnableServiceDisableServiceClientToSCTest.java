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
package org.serviceconnector.test.system;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.serviceconnector.TestConstants;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.cln.SCMgmtClient;
import org.serviceconnector.api.cln.SCSessionService;
import org.serviceconnector.ctrl.util.ProcessesController;
import org.serviceconnector.log.Loggers;
import org.serviceconnector.service.SCServiceException;

public class EnableServiceDisableServiceClientToSCTest {
	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(EnableServiceDisableServiceClientToSCTest.class);

	private static final Logger testLogger = Logger.getLogger(Loggers.TEST.getValue());

	private static Process scProcess;
	private Process srvProcess;

	private SCMgmtClient client;
	private Exception ex;

	private static ProcessesController ctrl;

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		ctrl = new ProcessesController();
		try {
			scProcess = ctrl.startSC(TestConstants.log4jSCProperties, TestConstants.SCProperties);
		} catch (Exception e) {
			logger.error("oneTimeSetUp", e);
		}
	}

	@Before
	public void setUp() throws Exception {
		client = new SCMgmtClient();
		client.attach(TestConstants.HOST, TestConstants.PORT_HTTP);
		srvProcess = ctrl.startServer(TestConstants.sessionSrv, TestConstants.log4jSrvProperties,
				TestConstants.PORT_LISTENER, TestConstants.PORT_TCP, 100, new String[] { TestConstants.sessionServiceName,
						TestConstants.publishServiceName, TestConstants.sessionServiceName });
	}

	@After
	public void tearDown() throws Exception {
		try {
			client.detach();
		} catch (Exception e) {
		}
		client = null;
		ctrl.stopProcess(srvProcess, TestConstants.log4jSrvProperties);
		srvProcess = null;
		ex = null;
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		ctrl.stopProcess(scProcess, TestConstants.log4jSCProperties);
		ctrl = null;
		scProcess = null;
	}

	/**
	 * Description:	Create session on enabled service <br>
	 * Expectation:	Session is successfully created, returns sessionId
	 */
	@Test
	public void createSession_1() throws Exception {
		assertEquals(true, client.isServiceEnabled(TestConstants.sessionServiceName));

		// create session 
		SCSessionService sessionService = client.newSessionService(TestConstants.sessionServiceName);

		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession(300, 60, scMessage);

		assertEquals(true, client.isServiceEnabled(TestConstants.sessionServiceName));
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	/**
	 * Description:	Create session on initially disabled service enabled by the client<br> 
	 * Expectation:	Session is successfully created, returns sessionId
	 */
	// TODO doubt this test case is useful ?
	@Test
	public void createSession_2() throws Exception {
		assertEquals(false, client.isServiceEnabled(TestConstants.sessionServiceName));
		client.enableService(TestConstants.sessionServiceName);
		assertEquals(true, client.isServiceEnabled(TestConstants.sessionServiceName));

		SCSessionService sessionService = client.newSessionService(TestConstants.sessionServiceName);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession(300, 60, scMessage);

		assertEquals(true, client.isServiceEnabled(TestConstants.sessionServiceName));
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();

		client.disableService(TestConstants.sessionServiceName);
	}

	/**
	 * Description:	Create session on initially enabled service disabled by the client<br> 
	 * Expectation:	Throws SCServiceException exception
	 */
	@Test
	public void createSession_3() throws Exception {
		assertEquals(true, client.isServiceEnabled(TestConstants.sessionServiceName));
		client.disableService(TestConstants.sessionServiceName);
		SCSessionService sessionService = client.newSessionService(TestConstants.sessionServiceName);

		try {
			SCMessage scMessage = new SCMessage();
			scMessage.setSessionInfo("sessionInfo");
			sessionService.createSession(300, 60, scMessage);
		} catch (Exception e) {
			ex = e;
		}
		assertEquals(true, ex instanceof SCServiceException);
		assertEquals(false, client.isServiceEnabled(TestConstants.sessionServiceName));
		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		client.enableService(TestConstants.sessionServiceName);
	}

	/**
	 * Description:<br> 
	 * 1. Disabled and enabled again the service.<br>
	 * 2. Create the session on enabled service.<br>
	 * <br>	
	 * Expectation:	Session is successfully created, returns sessionId
	 */
	// TODO doubt this test case is useful ? disabled & enabled first the service
	@Test
	public void createSession_4() throws Exception {
		// 1.
		assertEquals(true, client.isServiceEnabled(TestConstants.sessionServiceName));
		client.disableService(TestConstants.sessionServiceName);
		client.enableService(TestConstants.sessionServiceName);
		assertEquals(true, client.isServiceEnabled(TestConstants.sessionServiceName));

		// 2.
		SCSessionService sessionService = client.newSessionService(TestConstants.sessionServiceName);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession(300, 60, scMessage);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	/**
	 * Description:<br> 
	 * 1. Disabled and enabled the service 1000 times.<br>
	 * 2. Create the session on enabled service.<br>
	 * <br>	
	 * Expectation:	Session is successfully created, returns sessionId
	 */
	// TODO doubt this test case is useful ? disabled & enabled first the service
	@Test
	public void createSession_5() throws Exception {
		// 1.
		assertEquals(true, client.isServiceEnabled(TestConstants.sessionServiceName));

		for (int i = 0; i < 1000; i++) {
			if ((i % 100) == 0)
				testLogger.info("EnabledDisableService_1000Times cycle:\t" + i + " ...");
			client.disableService(TestConstants.sessionServiceName);
			client.enableService(TestConstants.sessionServiceName);
		}
		assertEquals(true, client.isServiceEnabled(TestConstants.sessionServiceName));
		
		// 2.
		SCSessionService sessionService = client.newSessionService(TestConstants.sessionServiceName);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession(300, 60, scMessage);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	/**
	 * Description: Do this steps 1000 times.<br>
	 * 	1. Disabled and enabled the service.<br>
	 * 	2. Create the session on enabled service.<br>
	 * <br>
	 * Expectation:	:	Session is successfully created, returns sessionId
	 */
	// TODO doubt this test case is useful ? disabled & enabled first the service
	@Test
	public void createSession_6() throws Exception {
		assertEquals(true, client.isServiceEnabled(TestConstants.sessionServiceName));

		for (int i = 0; i < 1000; i++) {
			if ((i % 100) == 0)
				testLogger.info("EnabledDisableService_1000Times cycle:\t" + i + " ...");
			// 1.
			client.disableService(TestConstants.sessionServiceName);
			client.enableService(TestConstants.sessionServiceName);
			
			// 2.
			SCSessionService sessionService = client.newSessionService(TestConstants.sessionServiceName);
			SCMessage scMessage = new SCMessage();
			scMessage.setSessionInfo("sessionInfo");
			sessionService.createSession(300, 60, scMessage);
			assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
			sessionService.deleteSession();
		}
	}
}
