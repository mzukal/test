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
package org.serviceconnector.test.unit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.net.FlyweightEncoderDecoderFactory;
import org.serviceconnector.net.IEncoderDecoder;
import org.serviceconnector.scmp.SCMPBodyType;
import org.serviceconnector.scmp.SCMPMessageFault;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPHeadlineKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;

/**
 * The Class DefaultEncoderDecoderTest.
 */
public class DefaultEncoderDecoderTest {

	/** The coder factory. */
	private FlyweightEncoderDecoderFactory coderFactory = AppContext.getEncoderDecoderFactory();
	/** The head key. */
	private SCMPHeadlineKey headKey;
	/** The msg type. */
	private SCMPMsgType msgType;
	/** The body type. */
	private SCMPBodyType bodyType;
	/** The msg id. */
	private String msgSequenceNr;
	/** The body length. */
	private String bodyLength;
	/** The body. */
	private String body;
	/** The encode scmp. */
	private SCMPMessage encodeScmp;

	/**
	 * Sets the up.
	 */
	@Before
	public void beforeOneTest() {
		this.headKey = SCMPHeadlineKey.REQ;
		this.msgType = SCMPMsgType.ATTACH;
		this.bodyType = SCMPBodyType.BINARY;
		this.msgSequenceNr = "1";
		this.bodyLength = "12";
		this.body = "hello world!";

		encodeScmp = new SCMPMessage();
		encodeScmp.setHeader(SCMPHeaderAttributeKey.MSG_TYPE, msgType.getValue());
		encodeScmp.setHeader(SCMPHeaderAttributeKey.BODY_TYPE, bodyType.getValue());
		encodeScmp.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, msgSequenceNr);
		encodeScmp.setBody(body.getBytes());
	}

	/**
	 * Decode REQ test.
	 */
	@Test
	public void decodeREQTest() {
		String header = "bty=" + bodyType.getValue() + "\n" + "mid=" + msgSequenceNr + "\n" + "mty=" + msgType.getValue()
				+ "\n";

		String requestString = AllUnitTests.getSCMPString(headKey, header, body);
		byte[] buffer = requestString.getBytes();
		InputStream is = new ByteArrayInputStream(buffer);
		IEncoderDecoder coder = coderFactory.createEncoderDecoder(buffer);

		SCMPMessage message = null;
		try {
			message = (SCMPMessage) coder.decode(is);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		verifySCMP(message);
	}

	/**
	 * Decode RES test.
	 */
	@Test
	public void decodeRESTest() {
		headKey = SCMPHeadlineKey.RES;

		String header = "bty=" + bodyType.getValue() + "\n" + "mid=" + msgSequenceNr + "\n" + "mty=" + msgType.getValue()
				+ "\n";

		String requestString = AllUnitTests.getSCMPString(headKey, header, body);

		byte[] buffer = requestString.getBytes();
		InputStream is = new ByteArrayInputStream(buffer);
		IEncoderDecoder coder = coderFactory.createEncoderDecoder(buffer);

		SCMPMessage message = null;
		try {
			message = (SCMPMessage) coder.decode(is);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		verifySCMP(message);
	}

	/**
	 * Decode exc test.
	 */
	@Test
	public void decodeEXCTest() {
		headKey = SCMPHeadlineKey.EXC;

		String header = "bty=" + bodyType.getValue() + "\n" + "mid=" + msgSequenceNr + "\n" + "mty=" + msgType.getValue()
				+ "\n";

		String requestString = AllUnitTests.getSCMPString(headKey, header, body);

		byte[] buffer = requestString.getBytes();
		InputStream is = new ByteArrayInputStream(buffer);
		IEncoderDecoder coder = coderFactory.createEncoderDecoder(buffer);

		SCMPMessage message = null;
		try {
			message = (SCMPMessage) coder.decode(is);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		if (message.isFault() == false) {
			Assert.fail("scmp should be of type fault");
		}
		verifySCMP(message);
	}

	/**
	 * Decode undef test.
	 */
	@Test
	public void decodeUNDEFTest() {
		String requestString = "garbage /s=69&awd 1.0\n" + "bty=" + bodyType.getValue() + "\n" + "mid=" + msgSequenceNr + "\n"
				+ "mty=" + msgType.getValue() + "\n\n" + body + "\n";

		byte[] buffer = requestString.getBytes();
		InputStream is = new ByteArrayInputStream(buffer);
		IEncoderDecoder coder = coderFactory.createEncoderDecoder(buffer);

		try {
			coder.decode(is);
			Assert.fail("Should throw exception");
		} catch (Exception e) {
			Assert.assertEquals("wrong protocol in message not possible to decode", e.getMessage());
		}
	}

	/**
	 * Decode body types test.
	 */
	@Test
	public void decodeBodyTypesTest() {
		String header = "bty=" + bodyType.getValue() + "\n" + "mid=" + msgSequenceNr + "\n" + "mty=" + msgType.getValue()
				+ "\n";

		String requestString = AllUnitTests.getSCMPString(headKey, header, body);

		byte[] buffer = requestString.getBytes();
		InputStream is = new ByteArrayInputStream(buffer);
		IEncoderDecoder coder = coderFactory.createEncoderDecoder(buffer);

		SCMPMessage message = null;
		try {
			message = (SCMPMessage) coder.decode(is);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		verifySCMP(message);

		bodyType = SCMPBodyType.TEXT;
		header = "bty=" + bodyType.getValue() + "\n" + "mid=" + msgSequenceNr + "\n" + "mty=" + msgType.getValue() + "\n";

		requestString = AllUnitTests.getSCMPString(headKey, header, body);

		buffer = requestString.getBytes();
		is = new ByteArrayInputStream(buffer);
		coder = coderFactory.createEncoderDecoder(buffer);

		message = null;
		try {
			message = (SCMPMessage) coder.decode(is);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		verifySCMPStringBody(message);
	}

	/**
	 * Encode REQ test.
	 */
	@Test
	public void encodeREQTest() {
		IEncoderDecoder coder = coderFactory.createEncoderDecoder(encodeScmp);

		String header = "msn=" + msgSequenceNr + "\n" + "bty=" + bodyType.getValue() + "\n" + "mty=" + msgType.getValue()
				+ "\n";

		String expectedString = AllUnitTests.getSCMPString(headKey, header, body);

		OutputStream os = new ByteArrayOutputStream();
		try {
			coder.encode(os, encodeScmp);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		Assert.assertEquals(expectedString, os.toString());
	}

	/**
	 * Encode RES test.
	 */
	@Test
	public void encodeRESTest() {
		IEncoderDecoder coder = coderFactory.createEncoderDecoder(encodeScmp);

		this.headKey = SCMPHeadlineKey.RES;
		String header = "msn=" + msgSequenceNr + "\n" + "bty=" + bodyType.getValue() + "\n" + "mty=" + msgType.getValue()
				+ "\n";

		String expectedString = AllUnitTests.getSCMPString(headKey, header, body);

		SCMPMessage encodeRes = new SCMPMessage();
		encodeRes.setIsReply(true);
		encodeRes.setHeader(encodeScmp);
		encodeRes.setBody(body.getBytes());

		OutputStream os = new ByteArrayOutputStream();
		try {
			coder.encode(os, encodeRes);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		Assert.assertEquals(expectedString, os.toString());
	}

	/**
	 * Encode EXC test.
	 */
	@Test
	public void encodeEXCTest() {
		IEncoderDecoder coder = coderFactory.createEncoderDecoder(encodeScmp);

		this.headKey = SCMPHeadlineKey.EXC;
		String header = "msn=" + msgSequenceNr + "\n" + "bty=" + bodyType.getValue() + "\n" + "mty=" + msgType.getValue()
				+ "\n";

		String expectedString = AllUnitTests.getSCMPString(headKey, header, body);

		SCMPMessage encodeExc = new SCMPMessageFault();
		encodeExc.setHeader(encodeScmp);
		encodeExc.setBody(body.getBytes());

		OutputStream os = new ByteArrayOutputStream();
		try {
			coder.encode(os, encodeExc);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		Assert.assertEquals(expectedString, os.toString());
	}

	/**
	 * Encode body types test.
	 */
	@Test
	public void encodeBodyTypesTest() {
		IEncoderDecoder coder = coderFactory.createEncoderDecoder(encodeScmp);

		String header = "msn=" + msgSequenceNr + "\n" + "bty=" + bodyType.getValue() + "\n" + "mty=" + msgType.getValue()
				+ "\n";

		String expectedString = AllUnitTests.getSCMPString(headKey, header, body);

		OutputStream os = new ByteArrayOutputStream();
		try {
			coder.encode(os, encodeScmp);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		Assert.assertEquals(expectedString, os.toString());

		coder = coderFactory.createEncoderDecoder(encodeScmp);
		bodyType = SCMPBodyType.TEXT;
		encodeScmp.setHeader(SCMPHeaderAttributeKey.BODY_TYPE, bodyType.getValue());

		header = "msn=" + msgSequenceNr + "\n" + "bty=" + bodyType.getValue() + "\n" + "mty=" + msgType.getValue() + "\n";

		expectedString = AllUnitTests.getSCMPString(headKey, header, body);

		os = new ByteArrayOutputStream();
		try {
			coder.encode(os, encodeScmp);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		Assert.assertEquals(expectedString, os.toString());
	}

	//TODO JOT
	public void encodeLargeBody() {

		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		for (int i = 0; i < 1024; i++) {
			byteBuffer.put((byte) i);
		}
		byte[] buf = byteBuffer.array();
		this.bodyLength = new Integer(buf.length).toString();
		this.body = "hello world!";

		SCMPMessage scmp = new SCMPMessage();
		scmp.setHeader(SCMPHeaderAttributeKey.MSG_TYPE, msgType.getValue());
		scmp.setHeader(SCMPHeaderAttributeKey.BODY_TYPE, bodyType.getValue());
		scmp.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, msgSequenceNr);
		scmp.setBody(body.getBytes());

		IEncoderDecoder coder = coderFactory.createEncoderDecoder(scmp);

		String header = "msn=" + msgSequenceNr + "\n" + "bty=" + bodyType.getValue() + "\n" + "mty=" + msgType.getValue()
				+ "\n";

		String expectedString = AllUnitTests.getSCMPString(headKey, header, body);

		OutputStream os = new ByteArrayOutputStream();
		try {
			coder.encode(os, scmp);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		Assert.assertEquals(expectedString, os.toString());
	}

	/**
	 * Verify scmp string body.
	 * 
	 * @param scmp
	 *            the scmp
	 */
	private void verifySCMPStringBody(SCMPMessage scmp) {
		Assert.assertEquals(bodyType.getValue(), scmp.getHeader(SCMPHeaderAttributeKey.BODY_TYPE));
		// Assert.assertEquals(msgID, scmp.getHeader(SCMPHeaderAttributeKey.MESSAGE_ID));
		Assert.assertEquals(bodyLength, scmp.getBodyLength() + "");
		Assert.assertEquals(msgType.getValue(), scmp.getHeader(SCMPHeaderAttributeKey.MSG_TYPE));
		Assert.assertEquals(body, scmp.getBody());
	}

	/**
	 * Verify scmp.
	 * 
	 * @param scmp
	 *            the scmp
	 */
	private void verifySCMP(SCMPMessage scmp) {
		Assert.assertEquals(bodyType.getValue(), scmp.getHeader(SCMPHeaderAttributeKey.BODY_TYPE));
		// Assert.assertEquals(msgID, scmp.getHeader(SCMPHeaderAttributeKey.MESSAGE_ID));
		Assert.assertEquals(bodyLength, scmp.getBodyLength() + "");
		Assert.assertEquals(msgType.getValue(), scmp.getHeader(SCMPHeaderAttributeKey.MSG_TYPE));
		Assert.assertEquals(body, new String((byte[]) scmp.getBody()));
	}
}
