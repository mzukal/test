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
package org.serviceconnector.test.system.scmp;

import org.junit.Before;
import org.junit.Test;
import org.serviceconnector.TestCallback;
import org.serviceconnector.TestConstants;
import org.serviceconnector.TestUtil;
import org.serviceconnector.call.SCMPClnChangeSubscriptionCall;
import org.serviceconnector.call.SCMPClnSubscribeCall;
import org.serviceconnector.call.SCMPClnUnsubscribeCall;
import org.serviceconnector.conf.RemoteNodeConfiguration;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.net.req.SCRequester;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.test.system.scmp.casc1.SCMPClnChangeSubscriptionCasc1Test;

import junit.framework.Assert;

public class SCMPClnChangeSubscriptionTest extends SCMPClnChangeSubscriptionCasc1Test {

	public SCMPClnChangeSubscriptionTest() {
		SCMPClnChangeSubscriptionCasc1Test.setUpServiceConnectorAndServer();
	}

	@Override
	@Before
	public void beforeOneTest() throws Exception {
		super.beforeOneTest();
		if (this.requester != null) {
			this.requester.destroy();
		}
		this.requester = new SCRequester(
				new RemoteNodeConfiguration(TestConstants.RemoteNodeName, TestConstants.HOST, TestConstants.PORT_SC0_HTTP, ConnectionType.NETTY_HTTP.getValue(), 0, 0, 10), 0);
		AppContext.init();
	}

	/**
	 * Description: change subscription twice, second one fails because there is no free connection<br>
	 * Expectation: passes
	 */
	@Test
	public void t20_ChangeTwiceFailsNoFreeConnection() throws Exception {
		SCMPClnSubscribeCall subscribeCall = new SCMPClnSubscribeCall(this.requester, TestConstants.pubServerName1);

		subscribeCall.setSessionInfo(TestConstants.publishCompressedMsgCmd);
		subscribeCall.setNoDataIntervalSeconds(10);
		subscribeCall.setMask(TestConstants.mask);
		subscribeCall.setRequestBody("100");
		TestCallback cbk = new TestCallback();
		subscribeCall.invoke(cbk, 1300);
		SCMPMessage reply = cbk.getMessageSync(1300);
		TestUtil.checkReply(reply);
		String sessionId = reply.getSessionId();

		SCMPClnChangeSubscriptionCall changeSubscriptionCall = new SCMPClnChangeSubscriptionCall(this.requester, TestConstants.pubServerName1, sessionId);
		// mask matches now
		changeSubscriptionCall.setMask(TestConstants.mask);
		changeSubscriptionCall.setSessionInfo(TestConstants.sleepCmd);
		changeSubscriptionCall.setRequestBody("2000");
		cbk = new TestCallback();
		changeSubscriptionCall.invoke(cbk, 4000);

		changeSubscriptionCall = new SCMPClnChangeSubscriptionCall(this.requester, TestConstants.pubServerName1, sessionId);
		changeSubscriptionCall.setMask(TestConstants.mask);
		TestCallback cbk1 = new TestCallback();
		changeSubscriptionCall.invoke(cbk1, 1300);

		TestUtil.checkReply(cbk.getMessageSync(4000));
		reply = cbk1.getMessageSync(1300);
		Assert.assertTrue(reply.isFault());
		TestUtil.verifyError(reply, SCMPError.NO_FREE_CONNECTION, SCMPMsgType.CLN_CHANGE_SUBSCRIPTION);

		SCMPClnUnsubscribeCall unSubscribeCall = new SCMPClnUnsubscribeCall(this.requester, TestConstants.pubServerName1, sessionId);
		cbk = new TestCallback();
		unSubscribeCall.invoke(cbk, 1300);
		reply = cbk.getMessageSync(1300);
		TestUtil.checkReply(reply);
	}
}
