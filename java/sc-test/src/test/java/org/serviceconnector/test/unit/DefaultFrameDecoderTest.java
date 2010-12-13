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

import org.junit.Assert;
import org.junit.Test;
import org.serviceconnector.Constants;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.net.FrameDecoderException;
import org.serviceconnector.net.IFrameDecoder;

/**
 * The Class DefaultFrameDecoderTest.
 * 
 * @author JTraber
 */
public class DefaultFrameDecoderTest {

	/** The decoder. */
	private IFrameDecoder decoder = AppContext.getFrameDecoderFactory().getFrameDecoder(
			Constants.TCP);

	/**
	 * Parses the frame size fail test.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void parseFrameSizeFailTest() throws Exception {
		try {
			int frameSize = decoder.parseFrameSize(null);
			Assert.assertEquals("0", frameSize + "");
			frameSize = decoder.parseFrameSize(new byte[0]);
			Assert.assertEquals("0", frameSize + "");
		} catch (FrameDecoderException e) {
			Assert.fail("Should not throw Exception!");
		}
	}

	/**
	 * Parses the frame size test.
	 */
	@Test
	public void parseFrameSizeTest() {
		byte[] b = null;
		int frameSize = 0;
		String headline = "REQ 0000078 00043 1.0\n";
		try {
			b = headline.getBytes();
			frameSize = decoder.parseFrameSize(b);
			Assert.assertEquals("100", frameSize + "");
		} catch (Exception e) {
			Assert.fail("Should not throw Exception!");
		}

		headline = "REQ 0011178 00043 1.0\n";
		try {
			b = headline.getBytes();
			frameSize = decoder.parseFrameSize(b);
			Assert.assertEquals("11200", frameSize + "");
		} catch (Exception e) {
			Assert.fail("Should not throw Exception!");
		}
	}

	/**
	 * Read int fail test.
	 */
	@Test
	public void readIntFailTest() {
		byte[] b = null;
		String headline = "REQ  008700 00000 1.0\n";
		try {
			b = headline.getBytes();
			decoder.parseFrameSize(b);
			Assert.fail("Should throw Exception!");
		} catch (Exception e) {
			Assert.assertEquals("invalid scmp message length", e.getMessage());
		}
	}
}
