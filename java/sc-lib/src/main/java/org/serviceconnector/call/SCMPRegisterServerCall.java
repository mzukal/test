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
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMsgType;

/**
 * The Class SCMPRegisterServerCall. Registers a server to a service.
 * 
 * @author JTraber
 */
public class SCMPRegisterServerCall extends SCMPCallAdapter {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(SCMPRegisterServerCall.class);

	/**
	 * Instantiates a new SCMPRegisterServerCall.
	 * 
	 * @param req
	 *            the requesters to use when invoking call
	 */
	public SCMPRegisterServerCall(IRequester req, String serviceName) {
		super(req, serviceName);
	}

	/** {@inheritDoc} */
	@Override
	public SCMPMsgType getMessageType() {
		return SCMPMsgType.REGISTER_SERVER;
	}

	/**
	 * Sets the version.
	 * 
	 * @param version
	 *            the new version
	 */
	public void setVersion(String version) {
		this.requestMessage.setHeader(SCMPHeaderAttributeKey.SC_VERSION, version);
	}

	/**
	 * Sets the local date time.
	 * 
	 * @param localDateTime
	 *            the new local date time
	 */
	public void setLocalDateTime(String localDateTime) {
		if (localDateTime == null) {
			return;
		}
		this.requestMessage.setHeader(SCMPHeaderAttributeKey.LOCAL_DATE_TIME, localDateTime);
	}

	/**
	 * Sets the max sessions.
	 * 
	 * @param maxSessions
	 *            the new max sessions
	 */
	public void setMaxSessions(int maxSessions) {
		this.requestMessage.setHeader(SCMPHeaderAttributeKey.MAX_SESSIONS, maxSessions);
	}

	/**
	 * Sets the max connections.
	 * 
	 * @param maxConnections
	 *            the new max connections
	 */
	public void setMaxConnections(int maxConnections) {
		this.requestMessage.setHeader(SCMPHeaderAttributeKey.MAX_CONNECTIONS, maxConnections);
	}

	/**
	 * Sets the keep alive interval.
	 * 
	 * @param keepAliveInterval
	 *            the new keep alive interval
	 */
	public void setKeepAliveInterval(int keepAliveInterval) {
		this.requestMessage.setHeader(SCMPHeaderAttributeKey.KEEP_ALIVE_INTERVAL, keepAliveInterval);
	}

	/**
	 * Sets the port number.
	 * 
	 * @param portNumber
	 *            the new port number
	 */
	public void setPortNumber(int portNumber) {
		this.requestMessage.setHeader(SCMPHeaderAttributeKey.PORT_NR, portNumber);
	}

	/**
	 * Sets the immediate connect.
	 * 
	 * @param immediateConnect
	 *            the new immediate connect
	 */
	public void setImmediateConnect(boolean immediateConnect) {
		if (immediateConnect) {
			this.requestMessage.setHeaderFlag(SCMPHeaderAttributeKey.IMMEDIATE_CONNECT);
		}
	}
}
