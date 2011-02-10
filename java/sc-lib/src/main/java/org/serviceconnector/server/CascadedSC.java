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
package org.serviceconnector.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.serviceconnector.Constants;
import org.serviceconnector.call.SCMPClnCreateSessionCall;
import org.serviceconnector.call.SCMPClnDeleteSessionCall;
import org.serviceconnector.call.SCMPClnExecuteCall;
import org.serviceconnector.call.SCMPClnUnsubscribeCall;
import org.serviceconnector.call.SCMPCscSubscribeCall;
import org.serviceconnector.call.SCMPCscUnsubscribeCall;
import org.serviceconnector.call.SCMPEchoCall;
import org.serviceconnector.call.SCMPReceivePublicationCall;
import org.serviceconnector.casc.CascadedClient;
import org.serviceconnector.casc.CscSubscribeActiveCascClientCallback;
import org.serviceconnector.casc.CscSubscribeInactiveCascClientCallback;
import org.serviceconnector.casc.ISubscriptionCallback;
import org.serviceconnector.cmd.sc.CommandCallback;
import org.serviceconnector.conf.RemoteNodeConfiguration;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.net.req.netty.IdleTimeoutException;
import org.serviceconnector.registry.SubscriptionRegistry;
import org.serviceconnector.scmp.ISCMPMessageCallback;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.service.AbstractSession;
import org.serviceconnector.service.CascadedPublishService;
import org.serviceconnector.service.Subscription;
import org.serviceconnector.service.SubscriptionMask;

public class CascadedSC extends Server implements IStatefulServer {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(CascadedSC.class);

	/** The subscriptions, list of subscriptions allocated on cascaded SC. */
	private List<AbstractSession> subscriptions;
	private static SubscriptionRegistry subscriptionRegistry = AppContext.getSubscriptionRegistry();

	public CascadedSC(RemoteNodeConfiguration remoteNodeConfiguration, InetSocketAddress socketAddress) {
		super(remoteNodeConfiguration, socketAddress);
		this.serverKey = remoteNodeConfiguration.getName();
		this.subscriptions = Collections.synchronizedList(new ArrayList<AbstractSession>());
	}

	public void createSession(SCMPMessage msgToForward, ISCMPMessageCallback callback, int timeoutMillis) {
		SCMPClnCreateSessionCall createSessionCall = new SCMPClnCreateSessionCall(this.requester, msgToForward);
		try {
			createSessionCall.invoke(callback, (int) (this.operationTimeoutMultiplier * timeoutMillis));
		} catch (Exception e) {
			// create session failed
			callback.receive(e);
		}
	}

	public void deleteSession(SCMPMessage msgToForward, ISCMPMessageCallback callback, int timeoutMillis) {
		SCMPClnDeleteSessionCall deleteSessionCall = new SCMPClnDeleteSessionCall(this.requester, msgToForward);
		try {
			deleteSessionCall.invoke(callback, (int) (this.operationTimeoutMultiplier * timeoutMillis));
		} catch (Exception e) {
			// delete session failed
			callback.receive(e);
		}
	}

	public void execute(SCMPMessage msgToForward, ISCMPMessageCallback callback, int timeoutMillis) {
		SCMPClnExecuteCall executeCall = new SCMPClnExecuteCall(this.requester, msgToForward);
		try {
			executeCall.invoke(callback, (int) (this.operationTimeoutMultiplier * timeoutMillis));
		} catch (Exception e) {
			// send data failed
			callback.receive(e);
		}
	}

	public void echo(SCMPMessage msgToForward, ISCMPMessageCallback callback, int timeoutMillis) {
		SCMPEchoCall echoCall = new SCMPEchoCall(this.requester, msgToForward);
		try {
			echoCall.invoke(callback, (int) (this.operationTimeoutMultiplier * timeoutMillis));
		} catch (Exception e) {
			// echo failed
			callback.receive(e);
		}
	}

	/**
	 * Try acquire permit on cascaded client semaphore. This method is used to get permit to continue. Only one permit is available
	 * per cascaded client. If no permit is available thread waits inside the semaphore. Operation breaks if OTI times out. After
	 * receiving a valid permit cascaded client will be checked to be active. Operation stops if client got destroyed in the
	 * meantime.
	 * Important: Pay attention an acquired permit must be released as fast as possible!!! Other threads are blocked.
	 * 
	 * @param cascClient
	 *            the cascaded client
	 * @param oti
	 *            the operation timeout
	 * @param callback
	 *            the callback
	 * @return true, if successful
	 */
	public boolean tryAcquirePermitOnCascClientSemaphore(CascadedClient cascClient, int oti, ISCMPMessageCallback callback) {
		boolean permit = false;
		Semaphore cascClientSemaphore = cascClient.getCascClientSemaphore();
		try {
			logger.trace("acquire permit callback=" + callback.getClass());
			permit = cascClientSemaphore.tryAcquire(oti, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			// thread interrupted during acquire a permit on semaphore
			callback.receive(ex);
			logger.warn("thread interrupted during acquire a permit on semaphore service=" + cascClient.getServiceName(), ex);
			return false;
		}
		if (permit == false) {
			// thread didn't get a permit in time
			callback.receive(new IdleTimeoutException("oti expired. operation - could not be completed."));
			logger.warn("thread didn't get a permit in time service=" + cascClient.getServiceName());
			return false;
		}
		if (cascClient.isDestroyed() == true) {
			// cascaded client got destroyed in the meantime, stop operation
			callback.receive(new IdleTimeoutException("oti expired. operation - could not be completed."));
			// release permit
			cascClientSemaphore.release();
			logger.warn("cascaded client got destroyed in the meantime, stop operation service=" + cascClient.getServiceName());
			return false;
		}
		return permit;
	}

	public void cascadedSCUnsubscribe(CascadedClient cascClient, SCMPMessage msgToForward, ISCMPMessageCallback callback,
			int timeoutMillis) {
		int oti = (int) (this.operationTimeoutMultiplier * timeoutMillis);
		long boforeAcquireTime = System.currentTimeMillis();
		if (this.tryAcquirePermitOnCascClientSemaphore(cascClient, oti, callback) == false) {
			// could not get permit to process - response done inside method
			return;
		}
		// calculate new OTI, reduce original by wait time in acquire
		oti = oti - (int) (System.currentTimeMillis() - boforeAcquireTime);
		// try catch block to assure releasing permit in case of any error - very important!
		try {
			// remove (cascaded SC / client) from cascaded client list
			cascClient.removeClientSubscriptionId(msgToForward.getHeader(SCMPHeaderAttributeKey.CASCADED_SUBSCRIPTION_ID));
			cascClient.removeClientSubscriptionId(msgToForward.getSessionId());

			Map<String, SubscriptionMask> clnSubscriptions = cascClient.getClientSubscriptionIds();
			if (clnSubscriptions.size() == 0) {
				// cascaded client can unsubscribe himself
				SCMPCscUnsubscribeCall cscUnsubscribeCall = new SCMPCscUnsubscribeCall(this.requester, msgToForward);
				// set cascaded client subscriptonId
				msgToForward.setHeader(SCMPHeaderAttributeKey.CASCADED_SUBSCRIPTION_ID, cascClient.getSubscriptionId());
				try {
					cscUnsubscribeCall.invoke(callback, oti);
				} catch (Exception e) {
					cascClient.destroy();
					throw e;
				}
				return;
			}
			// more than one client subscription left - unsubscribe only cascaded SC, change subscription for cascaded client
			this.unsubscribeCascadedSCWithActiveCascadedClient(cascClient, msgToForward, callback, oti);
		} catch (Exception e) {
			// release permit in case of an error
			cascClient.getCascClientSemaphore().release();
			callback.receive(e);
		}
	}

	private void unsubscribeCascadedSCWithActiveCascadedClient(CascadedClient cascClient, SCMPMessage msgToForward,
			ISCMPMessageCallback callback, int oti) throws Exception {
		// change mask of cascaded client in callback or keep it in case of an error
		// set cascaded subscriptonId
		msgToForward.setHeader(SCMPHeaderAttributeKey.CASCADED_SUBSCRIPTION_ID, cascClient.getSubscriptionId());
		String cascadedMask = cascClient.evalSubscriptionMaskFromClientSubscriptions();
		msgToForward.setHeader(SCMPHeaderAttributeKey.CASCADED_MASK, cascadedMask);
		SCMPCscUnsubscribeCall cscUnsubscribeCall = new SCMPCscUnsubscribeCall(this.requester, msgToForward);
		cscUnsubscribeCall.invoke(callback, oti);
	}

	public void cascadedSCSubscribe(CascadedClient cascClient, SCMPMessage msgToForward, ISubscriptionCallback callback,
			int timeoutMillis) {
		int oti = (int) (this.operationTimeoutMultiplier * timeoutMillis);
		long boforeAcquireTime = System.currentTimeMillis();
		if (this.tryAcquirePermitOnCascClientSemaphore(cascClient, oti, callback) == false) {
			// could not get permit to process - response done inside method
			return;
		}
		// calculate new OTI, reduce original by wait time in acquire
		oti = oti - (int) (System.currentTimeMillis() - boforeAcquireTime);
		// try catch block to assure releasing permit in case of any error - very important!
		SubscriptionMask currentSubscriptionMask = cascClient.getSubscriptionMask();
		try {
			// thread got permit to continue
			if (cascClient.isSubscribed() == false) {
				// cascaded client not subscribed - subscribe
				CascadedPublishService cascPublishService = cascClient.getPublishService();
				// adapt NO_DATA_INTERVAL for cascaded client
				msgToForward.setHeader(SCMPHeaderAttributeKey.NO_DATA_INTERVAL, cascPublishService.getNoDataIntervalSeconds());
				SCMPCscSubscribeCall cscSubscribeCall = new SCMPCscSubscribeCall(this.requester, msgToForward);

				// set cascaded mask in message
				String tmpCscMask = msgToForward.getHeader(SCMPHeaderAttributeKey.CASCADED_MASK);
				if (currentSubscriptionMask != null) {
					// cascaded client already has subscribed clients, figure out combined mask
					tmpCscMask = SubscriptionMask.masking(currentSubscriptionMask, tmpCscMask);
				}
				msgToForward.setHeader(SCMPHeaderAttributeKey.CASCADED_MASK, tmpCscMask);
				CscSubscribeInactiveCascClientCallback cscCallback = new CscSubscribeInactiveCascClientCallback(callback,
						cascClient, tmpCscMask);
				cscSubscribeCall.invoke(cscCallback, oti);
				return;
			}
			CscSubscribeActiveCascClientCallback cascCallback = new CscSubscribeActiveCascClientCallback(cascClient, callback
					.getRequest(), callback);
			this.subscribeCascadedSCWithActiveCascadedClient(cascClient, msgToForward, cascCallback, oti);
		} catch (Exception e) {
			// set the old mask in case of an error
			cascClient.setSubscriptionMask(currentSubscriptionMask);
			// release permit in case of an error
			cascClient.getCascClientSemaphore().release();
			callback.receive(e);
		}
	}

	private void subscribeCascadedSCWithActiveCascadedClient(CascadedClient cascClient, SCMPMessage msgToForward,
			ISCMPMessageCallback callback, int oti) throws Exception {
		// set cascaded subscriptonId and cascadedMask
		msgToForward.setHeader(SCMPHeaderAttributeKey.CASCADED_SUBSCRIPTION_ID, cascClient.getSubscriptionId());
		String clientMaskString = msgToForward.getHeader(SCMPHeaderAttributeKey.MASK);
		SubscriptionMask cascClientMask = cascClient.getSubscriptionMask();
		String cascadedMask = SubscriptionMask.masking(cascClientMask, clientMaskString);
		msgToForward.setHeader(SCMPHeaderAttributeKey.CASCADED_MASK, cascadedMask);
		SCMPCscSubscribeCall subscribeCall = new SCMPCscSubscribeCall(this.requester, msgToForward);
		subscribeCall.invoke(callback, oti);
	}

	public void unsubscribeCascadedClientInErrorCases(CascadedClient cascClient) {
		try {
			SCMPMessage msg = new SCMPMessage();
			msg.setSessionId(cascClient.getSubscriptionId());
			msg.setServiceName(cascClient.getServiceName());
			msg.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, "100");
			msg.removeHeader(SCMPHeaderAttributeKey.CASCADED_MASK);
			SCMPClnUnsubscribeCall unsubscribeCall = new SCMPClnUnsubscribeCall(this.requester, msg);
			unsubscribeCall.invoke(new CommandCallback(false), Constants.DEFAULT_OPERATION_TIMEOUT_SECONDS
					* Constants.SEC_TO_MILLISEC_FACTOR);
		} catch (Exception e) {
			logger.warn("unsubscribing cascaded client failed service=" + cascClient.getServiceName(), e);
		}
	}

	public void receivePublication(String serviceName, String subscriptionId, ISCMPMessageCallback callback, int timeoutMillis) {
		SCMPReceivePublicationCall receivePublicationCall = new SCMPReceivePublicationCall(this.requester, serviceName,
				subscriptionId);
		// TODO JOT/JAN what msg number for cascaded client RCP its only between SC's
		receivePublicationCall.getRequest().setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, "400");
		try {
			receivePublicationCall.invoke(callback, timeoutMillis);
		} catch (Exception e) {
			// receive publication failed
			callback.receive(e);
			return;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void abortSession(AbstractSession session, String reason) {
		// TODO JOT
		// delete subscription on casc, change subscription on cascadeSC
	}

	/** {@inheritDoc} */
	@Override
	public void addSession(AbstractSession subscription) {
		this.subscriptions.add(subscription);
	}

	/** {@inheritDoc} */
	@Override
	public void removeSession(AbstractSession subscription) {
		if (this.subscriptions == null) {
			// might be the case if server got already destroyed
			return;
		}
		this.subscriptions.remove(subscription);
	}

	public void removeSession(String subscriptionId) {
		Subscription subscription = CascadedSC.subscriptionRegistry.getSubscription(subscriptionId);
		this.removeSession(subscription);
	}

	/** {@inheritDoc} */
	@Override
	public List<AbstractSession> getSessions() {
		return this.subscriptions;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasFreeSession() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public int getMaxSessions() {
		return Integer.MAX_VALUE;
	}
}