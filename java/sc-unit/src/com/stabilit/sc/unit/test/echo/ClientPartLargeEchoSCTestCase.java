/*
 *-----------------------------------------------------------------------------*
 *                            Copyright � 2010 by                              *
 *                    STABILIT Informatik AG, Switzerland                      *
 *                            ALL RIGHTS RESERVED                              *
 *                                                                             *
 * Valid license from STABILIT is required for possession, use or copying.     *
 * This software or any other copies thereof may not be provided or otherwise  *
 * made available to any other person. No title to and ownership of the        *
 * software is hereby transferred. The information in this software is subject *
 * to change without notice and should not be construed as a commitment by     *
 * STABILIT Informatik AG.                                                     *
 *                                                                             *
 * All referenced products are trademarks of their respective owners.          *
 *-----------------------------------------------------------------------------*
 */
package com.stabilit.sc.unit.test.echo;

import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.stabilit.sc.cln.client.ClientFactory;
import com.stabilit.sc.cln.config.ClientConfig;
import com.stabilit.sc.cln.service.SCMPCallFactory;
import com.stabilit.sc.cln.service.SCMPEchoSCCall;
import com.stabilit.sc.common.io.SCMP;
import com.stabilit.sc.common.io.SCMPHeaderType;
import com.stabilit.sc.common.io.SCMPMsgType;
import com.stabilit.sc.unit.test.SCTest;
import com.stabilit.sc.unit.test.SetupTestCases;
import com.stabilit.sc.unit.test.SuperTestCase;

public class ClientPartLargeEchoSCTestCase extends SuperTestCase {
	
	@Before
	@Override
	public void setup() throws Exception {
		SetupTestCases.setupSC();
		try {
			config = new ClientConfig();
			config.load("sc-unit.properties");
			ClientFactory clientFactory = new ClientFactory();
			client = clientFactory.newInstance(config.getClientConfig());
			client.connect(); // physical connect
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void invokePartEchoSrvTest() throws Exception {
		SCMPEchoSCCall echoCall = (SCMPEchoSCCall) SCMPCallFactory.ECHO_SC_CALL.newInstance(client);
		echoCall.setPartMessage(true);

		for (int i = 0; i < 100; i++) {
			String s = "Hello part " + i;
			echoCall.setBody(s);
			SCMP result = echoCall.invoke();

			Map<String, String> header = result.getHeader();
			Assert.assertEquals("string", header.get(SCMPHeaderType.SCMP_BODY_TYPE.getName()));
			Assert.assertEquals(echoCall.getCall().getHeader(SCMPHeaderType.SCMP_MESSAGE_ID.getName()),
					header.get(SCMPHeaderType.SCMP_MESSAGE_ID.getName()));

			if (i < 10) {
				Assert.assertEquals("12", header.get(SCMPHeaderType.BODY_LENGTH.getName()));
				Assert.assertEquals("12", header.get(SCMPHeaderType.SCMP_CALL_LENGTH.getName()));
			} else {
				Assert.assertEquals("13", header.get(SCMPHeaderType.BODY_LENGTH.getName()));
				Assert.assertEquals("13", header.get(SCMPHeaderType.SCMP_CALL_LENGTH.getName()));
			}
			Assert.assertEquals(SCTest.getExpectedOffset(i, 12), header.get(SCMPHeaderType.SCMP_OFFSET
					.getName()));
			Assert.assertEquals(SCMPMsgType.ECHO_SC.getResponseName(), result.getMessageType());
		}
		String s = "This is the end";
		echoCall.setBody(s);
		echoCall.setPartMessage(false);
		echoCall.invoke();
	}
}
