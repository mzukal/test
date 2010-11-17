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
package org.serviceconnector.net;

import org.apache.log4j.Logger;
import org.serviceconnector.Constants;


/**
 * The Class HttpFrameDecoder. Decodes a Http frame.
 * 
 * @author JTraber
 */
public class HttpFrameDecoder extends DefaultFrameDecoder {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(HttpFrameDecoder.class);
	
	/**
	 * Instantiates a new http frame decoder.
	 */
	protected HttpFrameDecoder() {
	}

	/** {@inheritDoc} */
	@Override
	public int parseFrameSize(byte[] buffer) throws FrameDecoderException {

		if (buffer == null || buffer.length <= 0) {
			throw new FrameDecoderException("invalid scmp header line");
		}

		int sizeStart = 0;
		int sizeEnd = 0;
		int headerEnd = 0;
		int bytesRead = buffer.length;

		// watch out for Content-Length attribute in http header to evaluate frame size
		// (bytesRead - 3) avoids IndexOutOfBoundException because inner while looks ahead
		label: for (int i = 0; i < (bytesRead - 3); i++) {
			if (buffer[i] == Constants.SCMP_CR && buffer[i + 1] == Constants.SCMP_LF) {
				i += 2;
				if (buffer[i] == Constants.SCMP_CR && buffer[i + 1] == Constants.SCMP_LF) {
					headerEnd = i + 2;
					break label;
				}
				if (buffer[i] == 'C' && buffer[i + 7] == '-' && buffer[i + 8] == 'L' && buffer[i + 14] == ':') {
					sizeStart = i + 16;
					sizeEnd = sizeStart + 1;
					while (sizeEnd < bytesRead) {
						if (buffer[sizeEnd + 1] == Constants.SCMP_CR && buffer[sizeEnd + 2] == Constants.SCMP_LF) {
							break;
						}
						sizeEnd++;
					}
				}
			}
		}
		int contentLength = readInt(buffer, sizeStart, sizeEnd);
		return contentLength + headerEnd;
	}
}