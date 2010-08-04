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
package com.stabilit.scm.common.call;

import com.stabilit.scm.cln.call.ISCMPCall;
import com.stabilit.scm.cln.call.SCMPCallAdapter;
import com.stabilit.scm.common.net.req.IRequester;
import com.stabilit.scm.common.scmp.ISCMPCallback;
import com.stabilit.scm.common.scmp.SCMPHeaderAttributeKey;
import com.stabilit.scm.common.scmp.SCMPMessage;
import com.stabilit.scm.common.scmp.SCMPMsgType;
import com.stabilit.scm.common.util.DateTimeUtility;

/**
 * The Class SCMPRegisterServiceCall. Call registers a server.
 * 
 * @author JTraber
 */
public class SCMPRegisterServiceCall extends SCMPCallAdapter {

	/**
	 * Instantiates a new SCMPRegisterServiceCall.
	 */
	public SCMPRegisterServiceCall() {
		this(null, null);
	}

	/**
	 * Instantiates a new SCMPRegisterServiceCall.
	 * 
	 * @param req
	 *            the requesters to use when invoking call
	 */
	public SCMPRegisterServiceCall(IRequester req, String serviceName) {
		super(req, serviceName);
	}

	/** {@inheritDoc} */
	@Override
	public ISCMPCall newInstance(IRequester req, String serviceName) {
		return new SCMPRegisterServiceCall(req, serviceName);
	}

	/** {@inheritDoc} */
	@Override
	public void invoke(ISCMPCallback scmpCallback) throws Exception {
		this.setVersion(SCMPMessage.SC_VERSION.toString());
		this.setLocalDateTime(DateTimeUtility.getCurrentTimeZoneMillis());
		super.invoke(scmpCallback);
	}

	/** {@inheritDoc} */
	@Override
	public SCMPMsgType getMessageType() {
		return SCMPMsgType.REGISTER_SERVICE;
	}

	/**
	 * Sets the version.
	 * 
	 * @param version
	 *            the new version
	 */
	private void setVersion(String version) {
		this.requestMessage.setHeader(SCMPHeaderAttributeKey.SC_VERSION, version);
	}

	/**
	 * Sets the local date time.
	 * 
	 * @param localDateTime
	 *            the new local date time
	 */
	private void setLocalDateTime(String localDateTime) {
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
