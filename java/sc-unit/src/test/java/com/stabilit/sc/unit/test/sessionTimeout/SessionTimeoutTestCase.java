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
package com.stabilit.sc.unit.test.sessionTimeout;

import org.junit.Test;

import com.stabilit.sc.common.call.SCMPCallFactory;
import com.stabilit.sc.common.call.SCMPClnCreateSessionCall;
import com.stabilit.sc.common.call.SCMPClnDeleteSessionCall;
import com.stabilit.sc.common.call.SCMPClnEchoCall;
import com.stabilit.sc.common.call.SCMPClnExecuteCall;
import com.stabilit.sc.common.scmp.SCMPError;
import com.stabilit.sc.common.scmp.SCMPMessage;
import com.stabilit.sc.common.scmp.SCMPMsgType;
import com.stabilit.sc.unit.test.SCTest;
import com.stabilit.sc.unit.test.attach.SuperAttachTestCase;

public class SessionTimeoutTestCase extends SuperAttachTestCase {

	/**
	 * @param fileName
	 */
	public SessionTimeoutTestCase(String fileName) {
		super(fileName);
	}

	@Test
	public void sessionTimeout() throws Exception {
		// sets up a create session call
		SCMPClnCreateSessionCall createSessionCall = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(req, "simulation");
		createSessionCall.setSessionInfo("sessionInfo");
		createSessionCall.setEchoIntervalSeconds(1);
		createSessionCall.invoke(this.attachCallback, 1000);
		SCMPMessage responseMessage = this.attachCallback.getMessageSync();
		SCTest.checkReply(responseMessage);
		String sessionId = responseMessage.getSessionId();
		Thread.sleep(1200);
		// session should not exist
		SCMPClnExecuteCall clnExecuteCall = (SCMPClnExecuteCall) SCMPCallFactory.CLN_EXECUTE_CALL.newInstance(req, "simulation",
				sessionId);
		clnExecuteCall.setMessagInfo("message info");
		clnExecuteCall.setRequestBody("get Data (query)");
		clnExecuteCall.invoke(this.attachCallback, 1000);
		SCMPMessage msg = this.attachCallback.getMessageSync();
		SCTest.verifyError(msg, SCMPError.NOT_FOUND, " [no session found for " + sessionId + "]", SCMPMsgType.CLN_EXECUTE);

		SCMPClnDeleteSessionCall deleteSessionCall = (SCMPClnDeleteSessionCall) SCMPCallFactory.CLN_DELETE_SESSION_CALL
				.newInstance(req, "simulation", sessionId);
		deleteSessionCall.invoke(this.attachCallback, 1000);
	}

	@Test
	public void noSessionTimeout() throws Exception {
		// sets up a create session call
		SCMPClnCreateSessionCall createSessionCall = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(req, "simulation");
		createSessionCall.setSessionInfo("sessionInfo");
		createSessionCall.setEchoIntervalSeconds(1);
		createSessionCall.invoke(this.attachCallback, 1000);
		SCMPMessage responseMessage = this.attachCallback.getMessageSync();
		SCTest.checkReply(responseMessage);
		String sessionId = responseMessage.getSessionId();

		for (int i = 0; i < 5; i++) {
			SCMPClnEchoCall clnEchoCall = (SCMPClnEchoCall) SCMPCallFactory.CLN_ECHO_CALL.newInstance(req,
					"simulation", sessionId);
			clnEchoCall.invoke(this.attachCallback, 1000);
			SCTest.checkReply(this.attachCallback.getMessageSync());
			Thread.sleep(400);
		}
		// session should still exist
		SCMPClnExecuteCall clnExecuteCall = (SCMPClnExecuteCall) SCMPCallFactory.CLN_EXECUTE_CALL.newInstance(req, "simulation",
				sessionId);
		clnExecuteCall.setMessagInfo("message info");
		clnExecuteCall.setRequestBody("get Data (query)");
		clnExecuteCall.invoke(this.attachCallback, 1000);
		SCTest.checkReply(this.attachCallback.getMessageSync());

		SCMPClnDeleteSessionCall deleteSessionCall = (SCMPClnDeleteSessionCall) SCMPCallFactory.CLN_DELETE_SESSION_CALL
				.newInstance(req, "simulation", sessionId);
		deleteSessionCall.invoke(this.attachCallback, 1000);
	}
}