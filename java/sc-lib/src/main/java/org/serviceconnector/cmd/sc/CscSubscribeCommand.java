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
package org.serviceconnector.cmd.sc;

import org.apache.log4j.Logger;
import org.serviceconnector.Constants;
import org.serviceconnector.cmd.SCMPCommandException;
import org.serviceconnector.cmd.SCMPValidatorException;
import org.serviceconnector.cmd.casc.CscChangeSubscriptionCallbackForCasc;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.net.connection.ConnectionPoolBusyException;
import org.serviceconnector.net.req.IRequest;
import org.serviceconnector.net.res.IResponderCallback;
import org.serviceconnector.net.res.IResponse;
import org.serviceconnector.scmp.HasFaultResponseException;
import org.serviceconnector.scmp.ISubscriptionCallback;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.server.CascadedSC;
import org.serviceconnector.server.StatefulServer;
import org.serviceconnector.service.CascadedPublishService;
import org.serviceconnector.service.NoFreeServerException;
import org.serviceconnector.service.PublishService;
import org.serviceconnector.service.Service;
import org.serviceconnector.service.Subscription;
import org.serviceconnector.service.SubscriptionMask;
import org.serviceconnector.util.ValidatorUtility;

public class CscSubscribeCommand extends CommandAdapter {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(CscSubscribeCommand.class);

	/** {@inheritDoc} */
	@Override
	public SCMPMsgType getKey() {
		return SCMPMsgType.CSC_SUBSCRIBE;
	}

	/** {@inheritDoc} */
	@Override
	public void run(IRequest request, IResponse response, IResponderCallback responderCallback) throws Exception {
		SCMPMessage reqMessage = request.getMessage();
		String serviceName = reqMessage.getServiceName();

		// check service is present and enabled
		Service abstractService = this.getService(serviceName);
		if (abstractService.isEnabled() == false) {
			SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.SERVICE_DISABLED, "service="
					+ abstractService.getName() + " is disabled");
			scmpCommandException.setMessageType(getKey());
			throw scmpCommandException;
		}

		// enhance ipAddressList
		String ipAddressList = (String) reqMessage.getHeader(SCMPHeaderAttributeKey.IP_ADDRESS_LIST);
		ipAddressList = ipAddressList + request.getRemoteSocketAddress().getAddress();
		reqMessage.setHeader(SCMPHeaderAttributeKey.IP_ADDRESS_LIST, ipAddressList);

		int oti = reqMessage.getHeaderInt(SCMPHeaderAttributeKey.OPERATION_TIMEOUT);
		// create temporary Subscription for cascaded SC
		String sessionInfo = (String) reqMessage.getHeader(SCMPHeaderAttributeKey.SESSION_INFO);
		int noi = reqMessage.getHeaderInt(SCMPHeaderAttributeKey.NO_DATA_INTERVAL);
		String cscSCMaskString = reqMessage.getHeader(SCMPHeaderAttributeKey.CASCADED_MASK);
		String cascSubscriptionId = reqMessage.getHeader(SCMPHeaderAttributeKey.CASCADED_SUBSCRIPTION_ID);
		Subscription cscSubscription = this.subscriptionRegistry.getSubscription(cascSubscriptionId);
		SubscriptionMask cscMask = new SubscriptionMask(cscSCMaskString);
		Subscription tmpCascSCSubscription = new Subscription(cscMask, sessionInfo, ipAddressList, noi, AppContext
				.getBasicConfiguration().getSubscriptionTimeoutMillis());
		tmpCascSCSubscription.setService(abstractService);

		switch (abstractService.getType()) {
		case CASCADED_PUBLISH_SERVICE:
			// publish service is cascaded
			CascadedPublishService cascadedPublishService = (CascadedPublishService) abstractService;
			CascadedSC cascadedSC = cascadedPublishService.getCascadedSC();
			// add server to subscription
			tmpCascSCSubscription.setServer(cascadedSC);

			ISubscriptionCallback callback = null;
			if (cscSubscription == null) {
				// cascaded SC not subscribed yet
				callback = new SubscribeCommandCallback(request, response, responderCallback, tmpCascSCSubscription);
			} else {
				// subscribe is made by an active cascaded SC
				callback = new CscChangeSubscriptionCallbackForCasc(request, response, responderCallback, cscSubscription,
						cscSCMaskString);
			}
			cascadedSC.cascadedSCSubscribe(cascadedPublishService.getCascClient(), reqMessage, callback, oti);
			return;
		}
		// modify message only if it goes to server
		reqMessage.removeHeader(SCMPHeaderAttributeKey.NO_DATA_INTERVAL);
		reqMessage.removeHeader(SCMPHeaderAttributeKey.CASCADED_MASK);
		reqMessage.removeHeader(SCMPHeaderAttributeKey.CASCADED_SUBSCRIPTION_ID);
		// check service is present
		PublishService service = this.validatePublishService(abstractService);
		int otiOnSCMillis = (int) (oti * basicConf.getOperationTimeoutMultiplier());
		int tries = (otiOnSCMillis / Constants.WAIT_FOR_FREE_CONNECTION_INTERVAL_MILLIS);
		int i = 0;
		// Following loop implements the wait mechanism in case of a busy connection pool
		do {
			try {
				if (cscSubscription != null) {
					// cascaded subscribe made by an active cascaded SC
					CscChangeSubscriptionCallbackForCasc cascCallback = new CscChangeSubscriptionCallbackForCasc(request, response,
							responderCallback, cscSubscription, cscSCMaskString);
					((StatefulServer) cscSubscription.getServer()).subscribe(reqMessage, cascCallback, otiOnSCMillis);
					break;
				}
				// cascaded subscribe made by an inactive cascaded SC - forward client subscribe to server
				SubscribeCommandCallback callback = new SubscribeCommandCallback(request, response, responderCallback,
						tmpCascSCSubscription);
				service.allocateServerAndSubscribe(reqMessage, callback, tmpCascSCSubscription, otiOnSCMillis
						- (i * Constants.WAIT_FOR_FREE_CONNECTION_INTERVAL_MILLIS));
				// no exception has been thrown - get out of wait loop
				break;
			} catch (NoFreeServerException ex) {
				logger.debug("NoFreeServerException caught in wait mec of subscribe");
				if (i >= (tries - 1)) {
					// only one loop outstanding - don't continue throw current exception
					throw ex;
				}
			} catch (ConnectionPoolBusyException ex) {
				logger.debug("ConnectionPoolBusyException caught in wait mec of subscribe");
				if (i >= (tries - 1)) {
					// only one loop outstanding - don't continue throw current exception
					logger.warn(SCMPError.NO_FREE_CONNECTION.getErrorText("service=" + reqMessage.getServiceName()));
					SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.NO_FREE_CONNECTION, "service="
							+ reqMessage.getServiceName());
					scmpCommandException.setMessageType(this.getKey());
					throw scmpCommandException;
				}
			}
			// sleep for a while and then try again
			Thread.sleep(Constants.WAIT_FOR_FREE_CONNECTION_INTERVAL_MILLIS);
		} while (++i < tries);
	}

	/** {@inheritDoc} */
	@Override
	public void validate(IRequest request) throws Exception {
		SCMPMessage message = request.getMessage();

		try {
			// msgSequenceNr mandatory
			String msgSequenceNr = message.getMessageSequenceNr();
			ValidatorUtility.validateLong(1, msgSequenceNr, SCMPError.HV_WRONG_MESSAGE_SEQUENCE_NR);
			// serviceName mandatory
			String serviceName = message.getServiceName();
			ValidatorUtility.validateStringLengthTrim(1, serviceName, 32, SCMPError.HV_WRONG_SERVICE_NAME);
			// operation timeout mandatory
			String otiValue = message.getHeader(SCMPHeaderAttributeKey.OPERATION_TIMEOUT);
			ValidatorUtility.validateInt(1000, otiValue, 3600000, SCMPError.HV_WRONG_OPERATION_TIMEOUT);
			// ipAddressList mandatory
			String ipAddressList = message.getHeader(SCMPHeaderAttributeKey.IP_ADDRESS_LIST);
			ValidatorUtility.validateIpAddressList(ipAddressList);
			// sessionId mandatory
			String sessionId = message.getSessionId();
			ValidatorUtility.validateStringLengthTrim(1, sessionId, 256, SCMPError.HV_WRONG_SESSION_ID);
			// mask mandatory
			String mask = message.getHeader(SCMPHeaderAttributeKey.MASK);
			ValidatorUtility.validateStringLength(1, mask, 256, SCMPError.HV_WRONG_MASK);
			// noDataInterval mandatory
			String noDataIntervalValue = message.getHeader(SCMPHeaderAttributeKey.NO_DATA_INTERVAL);
			ValidatorUtility.validateInt(10, noDataIntervalValue, 3600, SCMPError.HV_WRONG_NODATA_INTERVAL);
			// sessionInfo is optional
			String sessionInfo = message.getHeader(SCMPHeaderAttributeKey.SESSION_INFO);
			ValidatorUtility.validateStringLengthIgnoreNull(1, sessionInfo, 256, SCMPError.HV_WRONG_SESSION_INFO);
			// cascadedMask
			String cascadedMask = message.getHeader(SCMPHeaderAttributeKey.CASCADED_MASK);
			ValidatorUtility.validateStringLengthTrim(1, cascadedMask, 256, SCMPError.HV_WRONG_MASK);
		} catch (HasFaultResponseException ex) {
			// needs to set message type at this point
			ex.setMessageType(getKey());
			throw ex;
		} catch (Throwable th) {
			logger.error("validation error", th);
			SCMPValidatorException validatorException = new SCMPValidatorException();
			validatorException.setMessageType(getKey());
			throw validatorException;
		}
	}
}