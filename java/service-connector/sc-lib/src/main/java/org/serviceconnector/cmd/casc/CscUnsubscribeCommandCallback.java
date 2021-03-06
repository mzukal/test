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
package org.serviceconnector.cmd.casc;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.serviceconnector.net.req.IRequest;
import org.serviceconnector.net.req.netty.IdleTimeoutException;
import org.serviceconnector.net.res.IResponderCallback;
import org.serviceconnector.net.res.IResponse;
import org.serviceconnector.scmp.ISCMPMessageCallback;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMessageFault;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.scmp.SCMPVersion;
import org.serviceconnector.server.IStatefulServer;
import org.serviceconnector.service.InvalidMaskLengthException;
import org.serviceconnector.service.Subscription;

/**
 * The Class CscUnsubscribeCommandCallback.
 */
public class CscUnsubscribeCommandCallback implements ISCMPMessageCallback {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CscUnsubscribeCommandCallback.class);
	/** The callback. */
	private IResponderCallback responderCallback;
	/** The request. */
	private IRequest request;
	/** The response. */
	private IResponse response;
	/** The subscription. */
	private Subscription cascSubscription;

	/**
	 * Instantiates a new csc unsubscribe command callback.
	 *
	 * @param request the request
	 * @param response the response
	 * @param responderCallback the responder callback
	 * @param cascSubscription the casc subscription
	 */
	public CscUnsubscribeCommandCallback(IRequest request, IResponse response, IResponderCallback responderCallback, Subscription cascSubscription) {
		this.responderCallback = responderCallback;
		this.request = request;
		this.response = response;
		this.cascSubscription = cascSubscription;
	}

	/**
	 * Receive.
	 *
	 * @param reply the reply {@inheritDoc}
	 */
	@Override
	public void receive(SCMPMessage reply) {
		SCMPMessage reqMessage = request.getMessage();
		String serviceName = reqMessage.getServiceName();
		IStatefulServer server = this.cascSubscription.getServer();
		if (reqMessage.getHeader(SCMPHeaderAttributeKey.CASCADED_MASK) == null) {
			// free server from subscription if cascaded SC unsubscribes himself
			server.removeSession(this.cascSubscription);
		}
		// forward server reply to client
		reply.setIsReply(true);
		reply.setServiceName(serviceName);
		reply.setMessageType(SCMPMsgType.CSC_UNSUBSCRIBE);
		this.response.setSCMP(reply);
		this.responderCallback.responseCallback(request, response);
		if (reply.isFault()) {
			// delete subscription failed abort!
			server.abortSession(this.cascSubscription, "unsubscribe failed, fault reply received in callback");
		}
	}

	/**
	 * Receive.
	 *
	 * @param ex the ex {@inheritDoc}
	 */
	@Override
	public void receive(Exception ex) {
		SCMPMessage reqMessage = this.request.getMessage();
		String sid = reqMessage.getSessionId();
		LOGGER.warn("receive exception sid=" + sid + " " + ex.toString());
		String serviceName = reqMessage.getServiceName();
		IStatefulServer server = this.cascSubscription.getServer();
		SCMPVersion scmpVersion = reqMessage.getSCMPVersion();

		if (reqMessage.getHeader(SCMPHeaderAttributeKey.CASCADED_MASK) == null) {
			// free server from subscription if cascaded SC unsubscribes himself
			server.removeSession(this.cascSubscription);
		}
		SCMPMessage fault = null;
		if (ex instanceof IdleTimeoutException) {
			// operation timeout handling - SCMP Version request
			fault = new SCMPMessageFault(scmpVersion, SCMPError.OPERATION_TIMEOUT, "Operation timeout expired on SC csc unsubscribe sid=" + sid);
		} else if (ex instanceof IOException) {
			fault = new SCMPMessageFault(scmpVersion, SCMPError.CONNECTION_EXCEPTION, "broken connection on SC csc unsubscribe sid=" + sid);
		} else if (ex instanceof InvalidMaskLengthException) {
			fault = new SCMPMessageFault(scmpVersion, SCMPError.HV_WRONG_MASK, ex.getMessage() + " sid=" + sid);
		} else {
			fault = new SCMPMessageFault(scmpVersion, SCMPError.SC_ERROR, "executing csc unsubscribe failed sid=" + sid);
		}
		// forward server reply to client
		fault.setIsReply(true);
		fault.setServiceName(serviceName);
		fault.setMessageType(SCMPMsgType.CSC_UNSUBSCRIBE);
		this.response.setSCMP(fault);
		this.responderCallback.responseCallback(request, response);
		// delete subscription failed abort!
		server.abortSession(this.cascSubscription, "unsubscribe failed, exception received in callback");
	}
}
