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
package org.serviceconnector.call;

import org.apache.log4j.Logger;
import org.serviceconnector.net.req.IRequester;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;


/**
 * The Class SCMPSrvDeleteSessionCall. Call deletes session on backend server.
 * 
 * @author JTraber
 */
public class SCMPSrvDeleteSessionCall extends SCMPCallAdapter {

	/** The Constant LOGGER. */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(SCMPSrvDeleteSessionCall.class);

	/**
	 * Instantiates a new SCMPSrvDeleteSessionCall.
	 * 
	 * @param req
	 *            the requester
	 * @param receivedMessage
	 *            the received message
	 */
	public SCMPSrvDeleteSessionCall(IRequester req, SCMPMessage receivedMessage) {
		super(req, receivedMessage);
	}

	/** {@inheritDoc} */
	@Override
	public SCMPMsgType getMessageType() {
		return SCMPMsgType.SRV_DELETE_SESSION;
	}
}
