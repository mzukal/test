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
package com.stabilit.sc.unit.test.srvData;

import junit.framework.Assert;

import org.junit.Test;

import com.stabilit.sc.cln.service.SCMPCallFactory;
import com.stabilit.sc.cln.service.SCMPClnDataCall;
import com.stabilit.sc.common.io.SCMP;
import com.stabilit.sc.common.io.SCMPBodyType;
import com.stabilit.sc.common.io.SCMPHeaderAttributeKey;
import com.stabilit.sc.common.io.SCMPMsgType;
import com.stabilit.sc.unit.test.session.SuperSessionTestCase;

/**
 * @author JTraber
 * 
 */
public class SrvDataTestCase extends SuperSessionTestCase {

	@Test
	public void multipleSrvDataTest() throws Exception {

		for (int i = 0; i < 100; i++) {
			SCMPClnDataCall clnDataCall = (SCMPClnDataCall) SCMPCallFactory.CLN_DATA_CALL.newInstance(client,
					scmpSession);
			clnDataCall.setMessagInfo("message info");
			SCMP scmpReply = clnDataCall.invoke();

			Assert.assertEquals("Message number " + i, scmpReply.getBody());
			Assert.assertEquals(SCMPBodyType.text.getName(), scmpReply
					.getHeader(SCMPHeaderAttributeKey.SCMP_BODY_TYPE));
			int bodyLength = (i + "").length() + 15;
			Assert.assertEquals(bodyLength + "", scmpReply.getHeader(SCMPHeaderAttributeKey.BODY_LENGTH));
			Assert.assertNotNull(scmpReply.getHeader(SCMPHeaderAttributeKey.SESSION_INFO));
			Assert.assertEquals(SCMPMsgType.CLN_DATA.getResponseName(), scmpReply.getMessageType());
			String serviceName = clnDataCall.getCall().getHeader(SCMPHeaderAttributeKey.SERVICE_NAME);
			String sessionId = clnDataCall.getCall().getSessionId();
			Assert.assertEquals(serviceName, scmpReply.getHeader(SCMPHeaderAttributeKey.SERVICE_NAME));
			Assert.assertEquals(sessionId, scmpReply.getSessionId());
		}
	}
}