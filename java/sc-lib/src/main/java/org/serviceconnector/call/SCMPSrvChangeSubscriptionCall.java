/*
 *-----------------------------------------------------------------------------*
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
 *-----------------------------------------------------------------------------*
/*
/**
 * 
 */
package org.serviceconnector.call;

import org.apache.log4j.Logger;
import org.serviceconnector.net.req.IRequester;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;


/**
 * The Class SCMPSrvChangeSubscriptionCall. Call changes subscription for a client.
 * 
 * @author JTraber
 */
public class SCMPSrvChangeSubscriptionCall extends SCMPServerCallAdapter {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(SCMPSrvChangeSubscriptionCall.class);
	
	/**
	 * Instantiates a new SCMPSrvChangeSubscriptionCall.
	 */
	public SCMPSrvChangeSubscriptionCall() {
		this(null, null);
	}

	/**
	 * Instantiates a new SCMPSrvChangeSubscriptionCall.
	 * 
	 * @param requester
	 *            the requester
	 * @param receivedMessage
	 *            the received message
	 */
	public SCMPSrvChangeSubscriptionCall(IRequester requester, SCMPMessage receivedMessage) {
		super(requester, receivedMessage);
	}

	/** {@inheritDoc} */
	@Override
	public ISCMPCall newInstance(IRequester requester, SCMPMessage receivedMessage) {
		return new SCMPSrvChangeSubscriptionCall(requester, receivedMessage);
	}

	/** {@inheritDoc} */
	@Override
	public SCMPMsgType getMessageType() {
		return SCMPMsgType.SRV_CHANGE_SUBSCRIPTION;
	}

	/**
	 * Sets the compression.
	 * 
	 * @param compressed
	 *            the compression
	 */
	public void setCompressed(boolean compressed) {
		if (compressed) {
			this.requestMessage.setHeaderFlag(SCMPHeaderAttributeKey.COMPRESSION);
		}
	}
}