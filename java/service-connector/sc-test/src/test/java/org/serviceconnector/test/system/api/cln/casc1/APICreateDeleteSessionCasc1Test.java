/*-----------------------------------------------------------------------------*
 *                                                                             *
 *       Copyright © 2010 STABILIT Informatik AG, Switzerland                  *
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
package org.serviceconnector.test.system.api.cln.casc1;

import org.junit.Before;
import org.junit.Test;
import org.serviceconnector.TestConstants;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.SCServiceException;
import org.serviceconnector.api.cln.SCMgmtClient;
import org.serviceconnector.api.cln.SCSessionService;
import org.serviceconnector.cmd.SCMPValidatorException;
import org.serviceconnector.test.system.SystemSuperTest;
import org.serviceconnector.test.system.api.APISystemSuperSessionClientTest;

import junit.framework.Assert;

@SuppressWarnings("unused")
public class APICreateDeleteSessionCasc1Test extends APISystemSuperSessionClientTest {

	public APICreateDeleteSessionCasc1Test() {
		SystemSuperTest.setUp1CascadedServiceConnectorAndServer();
	}

	@Override
	@Before
	public void beforeOneTest() throws Exception {
		super.beforeOneTest();
		this.setUpClientToSC();
	}

	/**
	 * Description: Create session (regular)<br>
	 * Expectation: passes
	 */
	@Test
	public void t01_createSession() throws Exception {
		SCMessage request = new SCMessage();
		SCMessage response = null;
		sessionService1 = client.newSessionService(TestConstants.sesServiceName1);
		msgCallback1 = new MsgCallback(sessionService1);
		response = sessionService1.createSession(request, msgCallback1);
		Assert.assertNotNull("the session ID is null", sessionService1.getSessionId());
		sessionService1.deleteSession();
		Assert.assertNull("the session ID is NOT null after deleteSession()", sessionService1.getSessionId());
	}

	/**
	 * Description: Create session to publish service<br>
	 * Expectation: throws SCMPValidatorException
	 */
	@Test(expected = SCMPValidatorException.class)
	public void t06_createSession() throws Exception {
		SCMessage request = null;
		SCMessage response = null;
		sessionService1 = client.newSessionService(TestConstants.pubServiceName1);
		msgCallback1 = new MsgCallback(sessionService1);
		response = sessionService1.createSession(request, msgCallback1);
	}

	/**
	 * Description: Create session to file service<br>
	 * Expectation: throws SCServiceException (unfortunately this passes because file services uses sessions) file service accepts create session (sessionService)<br>
	 * TODO JOT/TRN how do we distinguish between session for file services??
	 */
	@Test(expected = SCMPValidatorException.class)
	public void t07_createSession() throws Exception {
		SCMessage request = null;
		SCMessage response = null;
		sessionService1 = client.newSessionService(TestConstants.filServiceName1);
		msgCallback1 = new MsgCallback(sessionService1);
		response = sessionService1.createSession(request, msgCallback1);
	}

	/**
	 * Description: Create session to service not served by a server<br>
	 * Expectation: throws SCMPValidatorException
	 */
	@Test(expected = SCMPValidatorException.class)
	public void t09_createSession() throws Exception {
		SCMessage request = null;
		SCMessage response = null;
		sessionService1 = client.newSessionService(TestConstants.sesServiceName2);
		msgCallback1 = new MsgCallback(sessionService1);
		response = sessionService1.createSession(request, msgCallback1);
	}

	/**
	 * Description: Create session with 60kB message<br>
	 * Expectation: passes
	 */
	@Test
	public void t50_createSession60kBmsg() throws Exception {
		SCMessage request = new SCMessage(new byte[TestConstants.dataLength60kB]);
		SCMessage response = null;
		sessionService1 = client.newSessionService(TestConstants.sesServiceName1);
		msgCallback1 = new MsgCallback(sessionService1);
		response = sessionService1.createSession(request, msgCallback1);
		Assert.assertNotNull("the session ID is null", sessionService1.getSessionId());
		sessionService1.deleteSession();
	}

	/**
	 * Description: Create session with large message<br>
	 * Expectation: throws SCServiceException
	 */
	@Test(expected = SCServiceException.class)
	public void t51_createSession1MBmsg() throws Exception {
		SCMessage request = new SCMessage(new byte[TestConstants.dataLength1MB]);
		SCMessage response = null;
		sessionService1 = client.newSessionService(TestConstants.sesServiceName1);
		msgCallback1 = new MsgCallback(sessionService1);
		response = sessionService1.createSession(request, msgCallback1);
	}

	/**
	 * Description: Create session twice<br>
	 * Expectation: throws SCMPValidatorException
	 */
	@Test(expected = SCMPValidatorException.class)
	public void t60_createSessionTwice() throws Exception {
		SCMessage request = null;
		SCMessage response = null;
		sessionService1 = client.newSessionService(TestConstants.sesServiceName1);
		msgCallback1 = new MsgCallback(sessionService1);
		response = sessionService1.createSession(request, msgCallback1);
		Assert.assertNotNull("the session ID is null", sessionService1.getSessionId());

		response = sessionService1.createSession(request, msgCallback1);
	}

	/**
	 * Description: Create two sessions to the same service<br>
	 * Expectation: passes
	 */
	@Test
	public void t70_createTwoSessions() throws Exception {
		SCMessage request = new SCMessage();
		SCMessage response = null;
		SCSessionService service1 = client.newSessionService(TestConstants.sesServiceName1);
		SCSessionService service2 = client.newSessionService(TestConstants.sesServiceName1);
		msgCallback1 = new MsgCallback(sessionService1);
		response = service1.createSession(request, msgCallback1);
		Assert.assertNotNull("the session ID is null", service1.getSessionId());
		MsgCallback cbk2 = new MsgCallback(service2);
		response = service2.createSession(request, cbk2);
		Assert.assertNotNull("the session ID is null", service2.getSessionId());

		service1.deleteSession();
		Assert.assertNull("the session ID is NOT null after deleteSession()", service1.getSessionId());
		service2.deleteSession();
		Assert.assertNull("the session ID is NOT null after deleteSession()", service2.getSessionId());
	}

	/**
	 * Description: screw up sessionId before create session<br>
	 * Expectation: passes because sessionId is set internally.
	 */
	@Test
	public void t80_sessionId() throws Exception {
		SCMessage request = new SCMessage(TestConstants.pangram);
		SCMessage response = null;
		sessionService1 = client.newSessionService(TestConstants.sesServiceName1);
		String sessionId = "aaaa0000-bb11-cc22-dd33-eeeeee444444";
		request.setSessionId(sessionId);
		msgCallback1 = new MsgCallback(sessionService1);
		response = sessionService1.createSession(request, msgCallback1);
		Assert.assertEquals("sessionId is the same", false, sessionId == response.getSessionId());
		sessionService1.deleteSession();
	}

	/**
	 * Description: create session and get sessionInfo from server<br>
	 * Expectation: passes
	 */
	@Test
	public void t81_sessionInfo() throws Exception {
		SCMessage request = new SCMessage(TestConstants.pangram);
		String sessionInfo = "sessionInfoFromServer";
		request.setSessionInfo(sessionInfo);
		SCMessage response = null;
		sessionService1 = client.newSessionService(TestConstants.sesServiceName1);
		msgCallback1 = new MsgCallback(sessionService1);
		response = sessionService1.createSession(request, msgCallback1);
		Assert.assertEquals("sessionInfo is not the same", sessionInfo, response.getSessionInfo());
		sessionService1.deleteSession();
	}

	/**
	 * Description: Create session with service which has been disabled<br>
	 * Expectation: throws SCMPValidatorException
	 */
	@Test(expected = SCMPValidatorException.class)
	public void t83_disabledService() throws Exception {
		// disable service
		SCMgmtClient clientMgmt = new SCMgmtClient(TestConstants.HOST, TestConstants.PORT_SC0_TCP);
		clientMgmt.attach();
		clientMgmt.disableService(TestConstants.sesServiceName1);
		clientMgmt.detach();

		SCMessage request = null;
		SCMessage response = null;
		sessionService1 = client.newSessionService(TestConstants.sesServiceName1);
		msgCallback1 = new MsgCallback(sessionService1);
		response = sessionService1.createSession(request, msgCallback1);
	}

	/**
	 * Description: Reject session by server, check error code<br>
	 * Expectation: passes, exception catched
	 */
	@Test
	public void t85_rejectSession() throws Exception {
		SCMessage request = new SCMessage();
		SCMessage response = new SCMessage();
		sessionService1 = client.newSessionService(TestConstants.sesServiceName1);
		request.setSessionInfo(TestConstants.rejectCmd);
		Boolean passed = false;
		try {
			msgCallback1 = new MsgCallback(sessionService1);
			response = sessionService1.createSession(request, msgCallback1);
		} catch (SCServiceException e) {
			passed = true;
			Assert.assertNull("the session ID is NOT null", sessionService1.getSessionId());
			Assert.assertEquals("is not appErrorCode", TestConstants.appErrorCode, e.getAppErrorCode());
			Assert.assertEquals("is not appErrorText", TestConstants.appErrorText, e.getAppErrorText());
		}
		Assert.assertTrue("did not throw exception", passed);
		sessionService1.deleteSession();
	}

	/**
	 * Description: Reject session by server<br>
	 * Expectation: throws SCServiceException
	 */
	@Test(expected = SCServiceException.class)
	public void t86_rejectSession() throws Exception {
		SCMessage request = new SCMessage(TestConstants.pangram);
		request.setCompressed(false);
		SCMessage response = null;
		sessionService1 = client.newSessionService(TestConstants.sesServiceName1);
		request.setSessionInfo(TestConstants.rejectCmd);
		msgCallback1 = new MsgCallback(sessionService1);
		response = sessionService1.createSession(request, msgCallback1);
	}

	/**
	 * Description: Delete session before create session<br>
	 * Expectation: passes
	 */
	@Test
	public void t90_deleteSession() throws Exception {
		sessionService1 = client.newSessionService(TestConstants.sesServiceName1);
		Assert.assertNull("the session ID is NOT null before deleteSession()", sessionService1.getSessionId());
		sessionService1.deleteSession();
		Assert.assertNull("the session ID is NOT null after deleteSession()", sessionService1.getSessionId());
	}

	/**
	 * Description: Delete session on service which has been disabled<br>
	 * Expectation: passes
	 */
	@Test
	public void t91_disabledService() throws Exception {
		SCMessage request = new SCMessage();
		SCMessage response = null;
		sessionService1 = client.newSessionService(TestConstants.sesServiceName1);
		msgCallback1 = new MsgCallback(sessionService1);
		response = sessionService1.createSession(request, msgCallback1);
		Assert.assertNotNull("the session ID is null", sessionService1.getSessionId());

		// disable service
		SCMgmtClient clientMgmt = new SCMgmtClient(TestConstants.HOST, TestConstants.PORT_SC0_TCP);
		clientMgmt.attach();
		clientMgmt.disableService(TestConstants.sesServiceName1);
		clientMgmt.detach();

		// delete session
		sessionService1.deleteSession();
		Assert.assertNull("the session ID is NOT null after deleteSession()", sessionService1.getSessionId());
	}

	/**
	 * Description: Delete session twice<br>
	 * Expectation: passes
	 */
	@Test
	public void t92_deleteSessionTwice() throws Exception {
		SCMessage request = new SCMessage();
		SCMessage response = null;
		sessionService1 = client.newSessionService(TestConstants.sesServiceName1);
		msgCallback1 = new MsgCallback(sessionService1);
		response = sessionService1.createSession(request, msgCallback1);
		Assert.assertNotNull("the session ID is null", sessionService1.getSessionId());
		sessionService1.deleteSession();
		Assert.assertNull("the session ID is NOT null after deleteSession()", sessionService1.getSessionId());
		sessionService1.deleteSession();
		Assert.assertNull("the session ID is NOT null after deleteSession()", sessionService1.getSessionId());
	}
}
