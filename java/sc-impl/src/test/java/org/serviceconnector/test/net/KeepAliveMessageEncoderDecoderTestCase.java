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
package org.serviceconnector.test.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.serviceconnector.net.EncoderDecoderFactory;
import org.serviceconnector.net.IEncoderDecoder;
import org.serviceconnector.scmp.SCMPHeadlineKey;
import org.serviceconnector.scmp.SCMPKeepAlive;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.test.SCImplTest;



/**
 * The Class LargeMessageEncoderDecoderTest.
 */
public class KeepAliveMessageEncoderDecoderTestCase {

	/** The coder factory. */
	private EncoderDecoderFactory coderFactory = EncoderDecoderFactory.getCurrentEncoderDecoderFactory();
	/** The head key. */
	private SCMPHeadlineKey headKey;
	/** The encode scmp. */
	private SCMPMessage encodeScmp;

	@Test
	public void decodeKRQTest() {
		this.headKey = SCMPHeadlineKey.KRQ;

		String requestString = SCImplTest.getSCMPString(headKey, null, null);
		
		byte[] buffer = requestString.getBytes();
		InputStream is = new ByteArrayInputStream(buffer);
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPKeepAlive());

		SCMPMessage message = null;
		try {
			message = (SCMPMessage) coder.decode(is);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		verifySCMP(message);
	}

	@Test
	public void decodeKRSTest() {
		this.headKey = SCMPHeadlineKey.KRS;
		String requestString = headKey.name() + " 0000000 00000 1.0\n";

		byte[] buffer = requestString.getBytes();
		InputStream is = new ByteArrayInputStream(buffer);
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPKeepAlive());

		SCMPMessage message = null;
		try {
			message = (SCMPMessage) coder.decode(is);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		verifySCMP(message);
	}

	@Test
	public void encodeKRQTest() {
		this.headKey = SCMPHeadlineKey.KRQ;
		this.encodeScmp = new SCMPKeepAlive();
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPKeepAlive());

		String expectedString = this.headKey.name() + " 0000000 00000 1.0\n";

		OutputStream os = new ByteArrayOutputStream();
		try {
			coder.encode(os, encodeScmp);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		Assert.assertEquals(expectedString, os.toString());
	}	
	
	@Test
	public void encodeKRSTest() {
		this.headKey = SCMPHeadlineKey.KRS;
		this.encodeScmp = new SCMPKeepAlive();
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPKeepAlive());

		String expectedString = this.headKey.name() + " 0000000 00000 1.0\n";

		OutputStream os = new ByteArrayOutputStream();
		try {
			encodeScmp.setIsReply(true);
			coder.encode(os, encodeScmp);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		Assert.assertEquals(expectedString, os.toString());
	}	

	private void verifySCMP(SCMPMessage scmp) {
		Assert.assertNull(scmp.getBody());
		Assert.assertEquals(SCMPKeepAlive.class, scmp.getClass());
	}
}