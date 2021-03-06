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
package org.serviceconnector.scmp;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class SCMPPart. Indicates this SCMP is a part of a bigger request/response. Request/Response is complete at the time all parts are sent and put together.
 *
 * @author JTraber
 */
public class SCMPPart extends SCMPMessage {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6783597447553148309L;

	/** The poll request. */
	private boolean pollRequest;

	/**
	 * Instantiates a new SCMPPart.
	 *
	 * @param scmpVersion the SCMP version
	 */
	public SCMPPart(SCMPVersion scmpVersion) {
		this(scmpVersion, false);
	}

	/**
	 * Instantiates a new SCMP part.
	 *
	 * @param scmpVersion the SCMP version
	 * @param pollRequest the poll request
	 */
	public SCMPPart(SCMPVersion scmpVersion, boolean pollRequest) {
		super(scmpVersion);
		this.pollRequest = pollRequest;
	}

	/**
	 * Instantiates a new SCMP part.
	 *
	 * @param scmpVersion the SCMP version
	 * @param pollRequest the poll request
	 * @param baseHeader the base header
	 */
	public SCMPPart(SCMPVersion scmpVersion, boolean pollRequest, Map<String, String> baseHeader) {
		this(scmpVersion, pollRequest);
		this.header = new HashMap<String, String>(baseHeader);
	}

	/**
	 * Instantiates a new SCMP part. Copy constructor. Make a copy of given object. Uses SCMP Message copy constructor.
	 *
	 * @param toCopyObject the object to be copied
	 */
	public SCMPPart(SCMPPart toCopyObject) {
		super(toCopyObject);
		this.pollRequest = toCopyObject.isPollRequest();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isPart() {
		return true;
	}

	/**
	 * Checks if the message is a poll request in large message sequence.
	 *
	 * @return true, if is poll
	 */
	@Override
	public boolean isPollRequest() {
		return this.pollRequest;
	}

	/**
	 * Sets the checks if is poll request.
	 *
	 * @param pollRequest the new checks if is poll request
	 */
	public void setIsPollRequest(boolean pollRequest) {
		this.pollRequest = pollRequest;
	}
}
