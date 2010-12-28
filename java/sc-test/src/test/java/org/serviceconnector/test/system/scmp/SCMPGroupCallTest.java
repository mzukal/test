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
package org.serviceconnector.test.system.scmp;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.serviceconnector.TestCallback;
import org.serviceconnector.TestConstants;
import org.serviceconnector.TestUtil;
import org.serviceconnector.call.ISCMPCall;
import org.serviceconnector.call.SCMPCallFactory;
import org.serviceconnector.call.SCMPClnCreateSessionCall;
import org.serviceconnector.call.SCMPClnDeleteSessionCall;
import org.serviceconnector.call.SCMPClnExecuteCall;
import org.serviceconnector.ctrl.util.ProcessCtx;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.net.req.RequesterContext;
import org.serviceconnector.net.req.SCRequester;
import org.serviceconnector.scmp.SCMPBodyType;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.test.system.SystemSuperTest;

public class SCMPGroupCallTest extends SystemSuperTest {

	private ProcessCtx sesSrvCtx;
	private SCRequester requester;
	private String sessionId;
	
		@Before
	public void beforeOneTest() throws Exception {
		super.beforeOneTest();
		sesSrvCtx = ctrl.startServer(TestConstants.COMMUNICATOR_TYPE_SESSION, TestConstants.log4jSrvProperties, TestConstants.sesServerName1,
				TestConstants.PORT_SES_SRV_TCP, TestConstants.PORT_SC_TCP, 1, 1, TestConstants.sesServiceName1);
		this.requester = new SCRequester(new RequesterContext(TestConstants.HOST, TestConstants.PORT_SC_HTTP, ConnectionType.NETTY_HTTP
				.getValue(), 0));
		this.createSession();
	}

	@After
	public void afterOneTest() throws Exception {
		this.deleteSession();
		this.sessionId = null;
		try {
			this.requester.destroy();
		} catch (Exception e) {
		}
		this.requester = null;
		try {
			ctrl.stopServer(sesSrvCtx);
		} catch (Exception e) {
		}
		sesSrvCtx = null;
		super.afterOneTest();
	}


	/**
	 * Description: execute group call - open group send each letter alone and close group<br>
	 * Expectation: passes
	 */
	@Test
	public void t01_GroupCall() throws Exception {
		SCMPClnExecuteCall executeCall = (SCMPClnExecuteCall) SCMPCallFactory.CLN_EXECUTE_CALL.newInstance(this.requester,
				TestConstants.sesServerName1, this.sessionId);
		executeCall.setMessagInfo(TestConstants.echoCmd);
		ISCMPCall groupCall = executeCall.openGroup();

		TestCallback cbk = new TestCallback();
		groupCall.invoke(cbk, 1000);
		TestUtil.checkReply(cbk.getMessageSync(100));

		for (int i = 0; i < TestConstants.pangram.length(); i++) {
			groupCall.setRequestBody(TestConstants.pangram.subSequence(i, i + 1));
			groupCall.invoke(cbk, 1000);
			TestUtil.checkReply(cbk.getMessageSync(100));
		}
		groupCall.closeGroup(cbk, 1000); // send REQ (no body content)
		SCMPMessage res = cbk.getMessageSync(100);

		Assert.assertEquals(TestConstants.pangram, res.getBody());
		Assert.assertNotNull(res.getMessageSequenceNr());
		Assert.assertEquals(SCMPBodyType.TEXT.getValue(), res.getHeader(SCMPHeaderAttributeKey.BODY_TYPE));
		Assert.assertEquals(TestConstants.pangram.length() + "", res.getBodyLength() + "");
		Assert.assertEquals(SCMPMsgType.CLN_EXECUTE.getValue(), res.getMessageType());
	}

	/**
	 * Description: execute group call - open group send a large message over group call and close group<br>
	 * Expectation: passes
	 */
	@Test
	public void t10_GroupCallLargeRequest() throws Exception {
		SCMPClnExecuteCall executeCall = (SCMPClnExecuteCall) SCMPCallFactory.CLN_EXECUTE_CALL.newInstance(this.requester,
				TestConstants.sesServerName1, this.sessionId);
		executeCall.setMessagInfo(TestConstants.echoCmd);
		ISCMPCall groupCall = executeCall.openGroup();

		String largeString = TestUtil.getLargeString();
		groupCall.setRequestBody(largeString);
		TestCallback cbk = new TestCallback(true);
		groupCall.invoke(cbk, 1000);
		SCMPMessage reply = cbk.getMessageSync(100);
		TestUtil.checkReply(reply);

		groupCall.closeGroup(cbk, 1000); // send REQ (no body content)
		reply = cbk.getMessageSync(3000);

		Assert.assertEquals(largeString.length() + "", reply.getBodyLength() + "");
		Assert.assertEquals(largeString, reply.getBody());
		Assert.assertEquals(SCMPMsgType.CLN_EXECUTE.getValue(), reply.getMessageType());
	}

	/**
	 * create session.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private void createSession() throws Exception {
		SCMPClnCreateSessionCall createSessionCall = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL.newInstance(
				this.requester, TestConstants.sesServerName1);
		createSessionCall.setSessionInfo("sessionInfo");
		createSessionCall.setEchoIntervalSeconds(3600);
		TestCallback cbk = new TestCallback();
		createSessionCall.invoke(cbk, 1000);
		SCMPMessage resp = cbk.getMessageSync(3000);
		this.sessionId = resp.getSessionId();
	}

	/**
	 * delete session.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private void deleteSession() throws Exception {
		SCMPClnDeleteSessionCall deleteSessionCall = (SCMPClnDeleteSessionCall) SCMPCallFactory.CLN_DELETE_SESSION_CALL.newInstance(
				this.requester, TestConstants.sesServerName1, this.sessionId);
		TestCallback cbk = new TestCallback();
		deleteSessionCall.invoke(cbk, 1000);
		cbk.getMessageSync(3000);
	}
}