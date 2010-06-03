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
package com.stabilit.scm.cln.call;

import java.util.Map;

import com.stabilit.scm.cln.req.IClientSession;
import com.stabilit.scm.cln.req.IRequester;
import com.stabilit.scm.scmp.SCMPHeaderAttributeKey;
import com.stabilit.scm.scmp.SCMPMsgType;

/**
 * The Class SCMPSrvCreateSessionCall. Call tries getting a session on a backend server.
 * 
 * @author JTraber
 */
public class SCMPSrvCreateSessionCall extends SCMPCallAdapter {

	/**
	 * Instantiates a new SCMPSrvCreateSessionCall.
	 */
	public SCMPSrvCreateSessionCall() {
		this(null, null);
	}

	/**
	 * Instantiates a new SCMPSrvCreateSessionCall.
	 * 
	 * @param client
	 *            the client
	 * @param scmpSession
	 *            the scmp session
	 */
	public SCMPSrvCreateSessionCall(IRequester client, IClientSession scmpSession) {
		super(client, scmpSession);
	}

	/** {@inheritDoc} */
	@Override
	public ISCMPCall newInstance(IRequester client) {
		return new SCMPSrvCreateSessionCall(client, null);
	}

	/**
	 * Sets the session id.
	 * 
	 * @param sessionId
	 *            the new session id
	 */
	public void setSessionId(String sessionId) {
		requestMessage.setHeader(SCMPHeaderAttributeKey.SESSION_ID, sessionId);
	}

	/**
	 * Sets the header.
	 * 
	 * @param header
	 *            the header
	 */
	public void setHeader(Map<String, String> header) {
		this.requestMessage.setHeader(header);
	}

	/**
	 * Gets the message type.
	 * 
	 * @return the message type
	 */
	@Override
	public SCMPMsgType getMessageType() {
		return SCMPMsgType.SRV_CREATE_SESSION;
	}
}
