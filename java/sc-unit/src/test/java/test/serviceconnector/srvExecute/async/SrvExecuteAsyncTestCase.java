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
package test.serviceconnector.srvExecute.async;

import junit.framework.Assert;

import org.junit.Test;
import org.serviceconnector.call.SCMPCallFactory;
import org.serviceconnector.call.SCMPClnExecuteCall;
import org.serviceconnector.scmp.ISCMPCallback;
import org.serviceconnector.scmp.SCMPBodyType;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;

import test.serviceconnector.session.SuperSessionTestCase;


/**
 * @author JTraber
 */
public class SrvExecuteAsyncTestCase extends SuperSessionTestCase {

	private static boolean pendingRequest = false;

	/**
	 * The Constructor.
	 * 
	 * @param fileName
	 *            the file name
	 */
	public SrvExecuteAsyncTestCase(String fileName) {
		super(fileName);
	}

	@Test
	public void multipleSrvExecuteTest() throws Exception {

		for (int i = 0; i < 100; i++) {
			SCMPClnExecuteCall clnExecuteCall = (SCMPClnExecuteCall) SCMPCallFactory.CLN_EXECUTE_CALL.newInstance(req,
					"simulation", this.sessionId);
			clnExecuteCall.setMessagInfo("message info");
			clnExecuteCall.setRequestBody("get Data (query)");
			SrvExecuteTestCaseCallback callback = new SrvExecuteTestCaseCallback(clnExecuteCall);
			SrvExecuteAsyncTestCase.pendingRequest = true;
			clnExecuteCall.invoke(callback, 1000);
			while (SrvExecuteAsyncTestCase.pendingRequest == true);
		}
	}

	private class SrvExecuteTestCaseCallback implements ISCMPCallback {

		private SCMPClnExecuteCall clnExecuteCall;

		public SrvExecuteTestCaseCallback(SCMPClnExecuteCall clnExecuteCall) {
			this.clnExecuteCall = clnExecuteCall;
		}

		@Override
		public void callback(SCMPMessage scmpReply) throws Exception {
			SrvExecuteAsyncTestCase.pendingRequest = false;
			Assert.assertEquals("message data test case", scmpReply.getBody());
			Assert.assertEquals(SCMPBodyType.TEXT.getValue(), scmpReply
					.getHeader(SCMPHeaderAttributeKey.BODY_TYPE));
			int bodyLength = "message data test case".length();
			Assert.assertEquals(bodyLength + "", scmpReply.getBodyLength() + "");
			Assert.assertEquals(SCMPMsgType.CLN_EXECUTE.getValue(), scmpReply.getMessageType());
			String serviceName = clnExecuteCall.getRequest().getServiceName();
			String sessionId = clnExecuteCall.getRequest().getSessionId();
			Assert.assertEquals(serviceName, scmpReply.getServiceName());
			Assert.assertEquals(sessionId, scmpReply.getSessionId());
		}

		@Override
		public void callback(Exception ex) {
			SrvExecuteAsyncTestCase.pendingRequest = false;
		}
	}
}