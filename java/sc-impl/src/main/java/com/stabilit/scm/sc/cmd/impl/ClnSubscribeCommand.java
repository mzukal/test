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
package com.stabilit.scm.sc.cmd.impl;

import org.apache.log4j.Logger;

import com.stabilit.scm.common.cmd.ICommandValidator;
import com.stabilit.scm.common.cmd.IPassThroughPartMsg;
import com.stabilit.scm.common.cmd.SCMPValidatorException;
import com.stabilit.scm.common.log.ISubscriptionLogger;
import com.stabilit.scm.common.log.impl.SubscriptionLogger;
import com.stabilit.scm.common.scmp.HasFaultResponseException;
import com.stabilit.scm.common.scmp.IRequest;
import com.stabilit.scm.common.scmp.IResponse;
import com.stabilit.scm.common.scmp.ISCMPSynchronousCallback;
import com.stabilit.scm.common.scmp.SCMPError;
import com.stabilit.scm.common.scmp.SCMPFault;
import com.stabilit.scm.common.scmp.SCMPHeaderAttributeKey;
import com.stabilit.scm.common.scmp.SCMPMessage;
import com.stabilit.scm.common.scmp.SCMPMsgType;
import com.stabilit.scm.common.scmp.internal.SCMPPart;
import com.stabilit.scm.common.service.IFilterMask;
import com.stabilit.scm.common.util.ValidatorUtility;
import com.stabilit.scm.sc.registry.SubscriptionQueue;
import com.stabilit.scm.sc.registry.SubscriptionSessionRegistry;
import com.stabilit.scm.sc.service.IPublishTimerRun;
import com.stabilit.scm.sc.service.PublishService;
import com.stabilit.scm.sc.service.SCMPMessageFilterMask;
import com.stabilit.scm.sc.service.Server;
import com.stabilit.scm.sc.service.Session;

/**
 * The Class ClnSubscribeCommand. Responsible for validation and execution of subscribe command. Allows subscribing to a
 * publish service.
 * 
 * @author JTraber
 */
public class ClnSubscribeCommand extends CommandAdapter implements IPassThroughPartMsg {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(ClnSubscribeCommand.class);

	/** The Constant subscriptionLogger. */
	private final static ISubscriptionLogger subscriptionLogger = SubscriptionLogger.getInstance();
	
	/**
	 * Instantiates a ClnSubscribeCommand.
	 */
	public ClnSubscribeCommand() {
		this.commandValidator = new ClnSubscribeCommandValidator();
	}

	/** {@inheritDoc} */
	@Override
	public SCMPMsgType getKey() {
		return SCMPMsgType.CLN_SUBSCRIBE;
	}

	/** {@inheritDoc} */
	@Override
	public void run(IRequest request, IResponse response) throws Exception {
		SCMPMessage reqMessage = request.getMessage();
		String serviceName = reqMessage.getServiceName();
		String mask = (String) request.getAttribute(SCMPHeaderAttributeKey.MASK);
		// check service is present
		PublishService service = this.validatePublishService(serviceName);

		// create session
		Session session = new Session();
		reqMessage.setSessionId(session.getId());

		int noDataInterval = (Integer) request.getAttribute(SCMPHeaderAttributeKey.NO_DATA_INTERVAL);
		reqMessage.removeHeader(SCMPHeaderAttributeKey.NO_DATA_INTERVAL);

		ISCMPSynchronousCallback callback = new CommandCallback(true);
		Server server = service.allocateServerAndSubscribe(reqMessage, callback, session, (Integer) request
				.getAttribute(SCMPHeaderAttributeKey.OP_TIMEOUT));
		SCMPMessage reply = callback.getMessageSync();

		if (reply.isFault() == false) {
			boolean rejectSessionFlag = reply.getHeaderFlag(SCMPHeaderAttributeKey.REJECT_SESSION);
			if (Boolean.FALSE.equals(rejectSessionFlag)) {
				// session has not been rejected, add server to session
				session.setServer(server);
				// finally add subscription to the registry
				SubscriptionSessionRegistry subscriptionSessionRegistry = SubscriptionSessionRegistry
						.getCurrentInstance();
				subscriptionSessionRegistry.addSession(session.getId(), session);

				SubscriptionQueue<SCMPMessage> subscriptionQueue = service.getSubscriptionQueue();

				IPublishTimerRun timerRun = new PublishTimerRun(subscriptionQueue, noDataInterval);
				subscriptionLogger.logSubscribe(serviceName, session.getId(), mask);
				IFilterMask<SCMPMessage> filterMask = new SCMPMessageFilterMask(mask);
				subscriptionQueue.subscribe(session.getId(), filterMask, timerRun);
			} else {
				// session has been rejected - remove session id from header
				reply.removeHeader(SCMPHeaderAttributeKey.SESSION_ID);
			}
		} else {
			reply.removeHeader(SCMPHeaderAttributeKey.SESSION_ID);
		}
		// forward reply to client
		reply.setIsReply(true);
		reply.setMessageType(getKey());
		response.setSCMP(reply);
	}

	/**
	 * The Class ClnSubscribeCommandValidator.
	 */
	private class ClnSubscribeCommandValidator implements ICommandValidator {

		/** {@inheritDoc} */
		@Override
		public void validate(IRequest request) throws Exception {
			SCMPMessage message = request.getMessage();

			try {
				// messageId
				String messageId = (String) message.getHeader(SCMPHeaderAttributeKey.MESSAGE_ID);
				if (messageId == null || messageId.equals("")) {
					throw new SCMPValidatorException(SCMPError.HV_WRONG_MESSAGE_ID, "messageId must be set");
				}
				// serviceName
				String serviceName = message.getServiceName();
				if (serviceName == null || serviceName.equals("")) {
					throw new SCMPValidatorException(SCMPError.HV_WRONG_SERVICE_NAME, "serviceName must be set");
				}
				// mask
				String mask = (String) message.getHeader(SCMPHeaderAttributeKey.MASK);
				ValidatorUtility.validateStringLength(1, mask, 256, SCMPError.HV_WRONG_MASK);
				if (mask.indexOf("%") != -1) {
					// percent sign in mask not allowed
					throw new SCMPValidatorException(SCMPError.HV_WRONG_MASK, "percent sign not allowed " + mask);
				}
				// operation timeout
				String otiValue = message.getHeader(SCMPHeaderAttributeKey.OP_TIMEOUT.getValue());
				int oti = ValidatorUtility.validateInt(1, otiValue, 3600, SCMPError.HV_WRONG_OPERATION_TIMEOUT);
				request.setAttribute(SCMPHeaderAttributeKey.OP_TIMEOUT, oti);
				// ipAddressList
				String ipAddressList = (String) message.getHeader(SCMPHeaderAttributeKey.IP_ADDRESS_LIST);
				ValidatorUtility.validateIpAddressList(ipAddressList);
				// sessionInfo
				String sessionInfo = (String) message.getHeader(SCMPHeaderAttributeKey.SESSION_INFO);
				ValidatorUtility.validateStringLength(1, sessionInfo, 256, SCMPError.HV_WRONG_SESSION_INFO);
				// noDataInterval
				String noDataIntervalValue = message.getHeader(SCMPHeaderAttributeKey.NO_DATA_INTERVAL);
				int noi = ValidatorUtility
						.validateInt(1, noDataIntervalValue, 3600, SCMPError.HV_WRONG_NODATA_INTERVAL);
				request.setAttribute(SCMPHeaderAttributeKey.NO_DATA_INTERVAL, noi);
			} catch (HasFaultResponseException ex) {
				// needs to set message type at this point
				ex.setMessageType(getKey());
				throw ex;
			} catch (Throwable ex) {
				logger.error("validate", ex);
				SCMPValidatorException validatorException = new SCMPValidatorException();
				validatorException.setMessageType(getKey());
				throw validatorException;
			}
		}
	}

	/**
	 * The Class PublishTimerRun. PublishTimerRun defines action to get in place when subscription times out.
	 */
	private class PublishTimerRun implements IPublishTimerRun {

		/** The timeout. */
		private int timeoutSeconds;
		/** The subscription queue. */
		private SubscriptionQueue<SCMPMessage> subscriptionQueue;
		/** The request. */
		private IRequest request;
		/** The response. */
		private IResponse response;

		/**
		 * Instantiates a new publish timer run.
		 * 
		 * @param subscriptionPlace
		 *            the subscription place
		 * @param timeoutSeconds
		 *            the timeout
		 */
		public PublishTimerRun(SubscriptionQueue<SCMPMessage> subscriptionPlace, int timeoutSeconds) {
			this.request = null;
			this.response = null;
			this.timeoutSeconds = timeoutSeconds;
			this.subscriptionQueue = subscriptionPlace;
		}

		/** {@inheritDoc} */
		@Override
		public int getTimeoutSeconds() {
			return this.timeoutSeconds;
		}

		/** {@inheritDoc} */
		@Override
		public void setRequest(IRequest request) {
			this.request = request;
		}

		/** {@inheritDoc} */
		@Override
		public void setResponse(IResponse response) {
			this.response = response;
		}

		/** {@inheritDoc} */
		@Override
		public void timeout() {
			// extracting sessionId from request message
			SCMPMessage reqMsg;
			try {
				reqMsg = request.getMessage();
			} catch (Exception e1) {
				SCMPFault fault = new SCMPFault(e1);
				response.setSCMP(fault);
				try {
					// send message back to client
					this.response.write();
				} catch (Exception ex) {
					logger.error("timeout", ex);
				}
				return;
			}
			String sessionId = reqMsg.getSessionId();

			// tries polling from queue
			SCMPMessage message = this.subscriptionQueue.getMessage(sessionId);
			if (message == null) {
				// no message found on queue - subscription timeout set up no data message
				reqMsg.setHeaderFlag(SCMPHeaderAttributeKey.NO_DATA);
				reqMsg.setIsReply(true);
				this.response.setSCMP(reqMsg);
			} else {
				// set up reply
				SCMPMessage reply = null;
				if (message.isPart()) {
					// incoming message is of type part - outgoing must be part too
					reply = new SCMPPart();
				} else {
					reply = new SCMPMessage();
				}
				reply.setServiceName((String) request.getAttribute(SCMPHeaderAttributeKey.SERVICE_NAME));
				reply.setSessionId(sessionId);
				reply.setMessageType((String) request.getAttribute(SCMPHeaderAttributeKey.MSG_TYPE));
				reply.setIsReply(true);

				// message polling successful
				reply.setBody(message.getBody());
				reply
						.setHeader(SCMPHeaderAttributeKey.MESSAGE_ID, message
								.getHeader(SCMPHeaderAttributeKey.MESSAGE_ID));
				String messageInfo = message.getHeader(SCMPHeaderAttributeKey.MSG_INFO);
				if (messageInfo != null) {
					reply.setHeader(SCMPHeaderAttributeKey.MSG_INFO, messageInfo);
				}
				reply.setHeader(SCMPHeaderAttributeKey.MASK, message.getHeader(SCMPHeaderAttributeKey.MASK));
				reply.setHeader(SCMPHeaderAttributeKey.ORIGINAL_MSG_ID, message
						.getHeader(SCMPHeaderAttributeKey.ORIGINAL_MSG_ID));
				reply.setBody(message.getBody());
				this.response.setSCMP(reply);
			}

			try {
				// send message back to client
				this.response.write();
			} catch (Exception ex) {
				logger.error("timeout", ex);
			}
		}
	}
}