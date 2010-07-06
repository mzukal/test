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
package com.stabilit.scm.common.service;

import com.stabilit.scm.cln.call.SCMPCallFactory;
import com.stabilit.scm.cln.call.SCMPClnCreateSessionCall;
import com.stabilit.scm.cln.call.SCMPClnDataCall;
import com.stabilit.scm.cln.call.SCMPClnDeleteSessionCall;
import com.stabilit.scm.cln.service.ISCMessageCallback;
import com.stabilit.scm.cln.service.IServiceContext;
import com.stabilit.scm.cln.service.ISessionService;
import com.stabilit.scm.cln.service.SCMessage;
import com.stabilit.scm.common.ctx.IContext;
import com.stabilit.scm.common.net.req.IRequester;
import com.stabilit.scm.common.net.req.Requester;
import com.stabilit.scm.common.scmp.ISCMPCallback;
import com.stabilit.scm.common.scmp.SCMPHeaderAttributeKey;
import com.stabilit.scm.common.scmp.SCMPMessage;

/**
 * @author JTraber
 */
public class SessionService implements ISessionService {

	private String serviceName;
	private String sessionId;
	private IServiceContext sessionContext;
	private IRequester requester;

	public SessionService(String serviceName, IContext context) {
		this.serviceName = serviceName;
		this.sessionId = null;
		this.requester = new Requester(context);
		this.sessionContext = new ServiceContext(
				(IServiceConnectorContext) context, this);
	}

	@Override
	public void createSession(String sessionInfo, int echoTimeout, int echoInterval) throws Exception {
		SCMPClnCreateSessionCall createSessionCall = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(this.requester, this.serviceName);
		createSessionCall.setSessionInfo(sessionInfo);
		createSessionCall.setEchoTimeout(echoTimeout); 
		createSessionCall.setEchoInterval(echoInterval);
		SCMPMessage reply = createSessionCall.invoke();
		this.sessionId = reply.getSessionId();
	}

	@Override
	public void deleteSession() throws Exception {
		SCMPClnDeleteSessionCall deleteSessionCall = (SCMPClnDeleteSessionCall) SCMPCallFactory.CLN_DELETE_SESSION_CALL
				.newInstance(this.requester, this.serviceName, this.sessionId);
		deleteSessionCall.invoke();
	}

	@Override
	public SCMessage execute(SCMessage requestMsg) throws Exception {
		SCMPClnDataCall clnDataCall = (SCMPClnDataCall) SCMPCallFactory.CLN_DATA_CALL
				.newInstance(this.requester, this.serviceName, this.sessionId);
		clnDataCall.setMessagInfo(requestMsg.getMessageInfo());
		clnDataCall.setRequestBody(requestMsg.getData());
		SCMPMessage reply = clnDataCall.invoke();
		SCMessage replyToClient = new SCMessage();
		replyToClient.setData(reply.getBody());
		replyToClient.setCompressed(reply
				.getHeaderBoolean(SCMPHeaderAttributeKey.COMPRESSION));
		return replyToClient;
	}

	@Override
	public void execute(SCMessage requestMsg, ISCMessageCallback messageCallback)
			throws Exception {
		SCMPClnDataCall clnDataCall = (SCMPClnDataCall) SCMPCallFactory.CLN_DATA_CALL
				.newInstance(this.requester, this.serviceName, this.sessionId);
		clnDataCall.setMessagInfo(requestMsg.getMessageInfo());
		clnDataCall.setRequestBody(requestMsg.getData());
		ISCMPCallback scmpCallback = new SessionServiceSCMPCallback(
				messageCallback);
		if (messageCallback instanceof IActiveState) {
			((IActiveState) messageCallback).setActive(true);
		}
		clnDataCall.invoke(scmpCallback);
		return;
	}

	// member class
	private class SessionServiceSCMPCallback implements ISCMPCallback {

		private IContext context;
		private ISCMessageCallback messageCallback;

		public SessionServiceSCMPCallback(ISCMessageCallback messageCallback) {
			this.messageCallback = messageCallback;
			this.context = null;
		}

		@Override
		public IContext getContext() {
			return this.context;
		}
		
		@Override
		public void callback(SCMPMessage scmpReply) throws Exception {
			SCMessage messageReply = new SCMessage();
			messageReply.setData(scmpReply.getBody());
			messageReply.setCompressed(scmpReply
					.getHeaderBoolean(SCMPHeaderAttributeKey.COMPRESSION));
			this.messageCallback.callback(messageReply);
			if (messageCallback instanceof IActiveState) {
				((IActiveState) this.messageCallback).setActive(false);
			}
		}

		@Override
		public void callback(Throwable th) {
			this.messageCallback.callback(th);
			if (messageCallback instanceof IActiveState) {
				((IActiveState) this.messageCallback).setActive(false);
			}
		}

		@Override
		public void setContext(IContext context) {
			this.context = context;
		}
	}

	@Override
	public IServiceContext getContext() {
		return this.sessionContext;
	}
}
