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
import org.serviceconnector.cache.Cache;
import org.serviceconnector.cache.CacheComposite;
import org.serviceconnector.cache.CacheId;
import org.serviceconnector.cache.CacheManager;
import org.serviceconnector.cache.CacheMessage;
import org.serviceconnector.cmd.SCMPCommandException;
import org.serviceconnector.cmd.SCMPValidatorException;
import org.serviceconnector.cmd.casc.ClnExecuteCommandCascCallback;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.log.CacheLogger;
import org.serviceconnector.log.SessionLogger;
import org.serviceconnector.net.connection.ConnectionPoolBusyException;
import org.serviceconnector.net.req.IRequest;
import org.serviceconnector.net.res.IResponderCallback;
import org.serviceconnector.net.res.IResponse;
import org.serviceconnector.scmp.HasFaultResponseException;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.scmp.SCMPPart;
import org.serviceconnector.server.CascadedSC;
import org.serviceconnector.server.StatefulServer;
import org.serviceconnector.service.CascadedSessionService;
import org.serviceconnector.service.Service;
import org.serviceconnector.service.Session;
import org.serviceconnector.util.ValidatorUtility;

/**
 * The Class CscExecuteCommand. Responsible for validation and execution of execute command. Execute command sends any data to the
 * server. Execute command runs asynchronously and passes through any parts messages.
 * 
 * @author JTraber
 */
public class CscExecuteCommand extends CommandAdapter {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(CscExecuteCommand.class);

	/** {@inheritDoc} */
	@Override
	public SCMPMsgType getKey() {
		return SCMPMsgType.CSC_EXECUTE;
	}

	/** {@inheritDoc} */
	@Override
	public void run(IRequest request, IResponse response, IResponderCallback responderCallback) throws Exception {
		SCMPMessage reqMessage = request.getMessage();
		String serviceName = reqMessage.getServiceName();
		int oti = reqMessage.getHeaderInt(SCMPHeaderAttributeKey.OPERATION_TIMEOUT);
		// check service is present
		Service abstractService = this.getService(serviceName);

		switch (abstractService.getType()) {
		case CASCADED_SESSION_SERVICE:
			this.executeCascadedService(request, response, responderCallback);
			return;
		}

		String sessionId = reqMessage.getSessionId();
		Session session = this.getSessionById(sessionId);
		if (session.hasPendingRequest() == true) {
			SessionLogger.error("session " + sessionId + "has pending request");
		}
		session.setPendingRequest(true);
		// cancel session timeout
		this.sessionRegistry.cancelSessionTimeout(session);

		CacheManager cacheManager = null;
		if (reqMessage.getCacheId() != null) {
			cacheManager = AppContext.getCacheManager();
		}

		if (cacheManager != null && cacheManager.isCacheEnabled()) {
			CacheLogger.debug("execute command with cache id = " + reqMessage.getCacheId() + ", cache part nr = "
					+ reqMessage.getCachePartNr() + ", part is = " + reqMessage.isPart() + ", poll is "
					+ reqMessage.isPollRequest());
			try {
				// try to load response from cache
				if (tryLoadingMessageFromCache(request, response, responderCallback, false)) {
					return;
				}
			} catch (Exception e) {
				session.setPendingRequest(false);
				this.sessionRegistry.scheduleSessionTimeout(session);
				throw e;
			}
		}
		ExecuteCommandCallback callback = null;
		StatefulServer server = session.getStatefulServer();
		int otiOnSCMillis = (int) (oti * basicConf.getOperationTimeoutMultiplier());
		int tries = (otiOnSCMillis / Constants.WAIT_FOR_FREE_CONNECTION_INTERVAL_MILLIS);
		// Following loop implements the wait mechanism in case of a busy connection pool
		int i = 0;
		do {
			callback = new ExecuteCommandCallback(request, response, responderCallback, sessionId);
			try {
				server.execute(reqMessage, callback, otiOnSCMillis - (i * Constants.WAIT_FOR_FREE_CONNECTION_INTERVAL_MILLIS));
				// no exception has been thrown - get out of wait loop
				break;
			} catch (ConnectionPoolBusyException ex) {
				if (i >= (tries - 1)) {
					session.setPendingRequest(false);
					// only one loop outstanding - don't continue throw current exception
					// schedule session timeout
					this.sessionRegistry.scheduleSessionTimeout(session);
					LOGGER.debug(SCMPError.NO_FREE_CONNECTION.getErrorText("service=" + reqMessage.getServiceName()));
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

	/**
	 * Execute cascaded service.
	 * 
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @param responderCallback
	 *            the responder callback
	 * @throws Exception
	 *             the exception
	 */
	private void executeCascadedService(IRequest request, IResponse response, IResponderCallback responderCallback)
			throws Exception {
		SCMPMessage reqMessage = request.getMessage();
		String serviceName = reqMessage.getServiceName();
		int oti = reqMessage.getHeaderInt(SCMPHeaderAttributeKey.OPERATION_TIMEOUT);
		CacheManager cacheManager = null;
		if (reqMessage.getCacheId() != null) {
			cacheManager = AppContext.getCacheManager();
		}
		if (cacheManager != null && cacheManager.isCacheEnabled()) {
			LOGGER.info("execute command with cache id = " + reqMessage.getCacheId() + ", cache part nr = "
					+ reqMessage.getCachePartNr());
			// try to load response from cache
			try {
				if (tryLoadingMessageFromCache(request, response, responderCallback, true)) {
					return;
				}
			} catch (Exception e) {
				throw e;
			}
		}
		Service abstractService = this.getService(serviceName);
		CascadedSC cascadedSC = ((CascadedSessionService) abstractService).getCascadedSC();
		ClnExecuteCommandCascCallback callback = new ClnExecuteCommandCascCallback(request, response, responderCallback);
		cascadedSC.execute(reqMessage, callback, oti);
		return;
	}

	/** {@inheritDoc} */
	@Override
	public void validate(IRequest request) throws Exception {
		try {
			SCMPMessage message = request.getMessage();
			// msgSequenceNr mandatory
			String msgSequenceNr = message.getMessageSequenceNr();
			ValidatorUtility.validateLong(1, msgSequenceNr, SCMPError.HV_WRONG_MESSAGE_SEQUENCE_NR);
			// serviceName mandatory
			String serviceName = message.getServiceName();
			ValidatorUtility.validateStringLengthTrim(1, serviceName, 32, SCMPError.HV_WRONG_SERVICE_NAME);
			// operation timeout mandatory
			String otiValue = message.getHeader(SCMPHeaderAttributeKey.OPERATION_TIMEOUT);
			ValidatorUtility.validateInt(1000, otiValue, 3600000, SCMPError.HV_WRONG_OPERATION_TIMEOUT);
			// sessionId mandatory
			String sessionId = message.getSessionId();
			ValidatorUtility.validateStringLengthTrim(1, sessionId, 256, SCMPError.HV_WRONG_SESSION_ID);
			// message info optional
			String messageInfo = message.getHeader(SCMPHeaderAttributeKey.MSG_INFO);
			ValidatorUtility.validateStringLengthIgnoreNull(1, messageInfo, 256, SCMPError.HV_WRONG_MESSAGE_INFO);
			// cacheId optional
			String cacheId = message.getHeader(SCMPHeaderAttributeKey.CACHE_ID);
			ValidatorUtility.validateStringLengthIgnoreNull(1, cacheId, 256, SCMPError.HV_WRONG_SESSION_INFO);
		} catch (HasFaultResponseException ex) {
			// needs to set message type at this point
			ex.setMessageType(getKey());
			throw ex;
		} catch (Throwable th) {
			LOGGER.error("validation error", th);
			SCMPValidatorException validatorException = new SCMPValidatorException();
			validatorException.setMessageType(getKey());
			throw validatorException;
		}
	}

	/**
	 * Try loading message from cache. This method tries to load the message from its cache. An exception is thrown if the message
	 * is not full part of the cache. In case of a successful cache load the method return true otherwise false.
	 * 
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @param responderCallback
	 *            the responder callback
	 * @param cascaded
	 *            the cascaded
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 */
	private boolean tryLoadingMessageFromCache(IRequest request, IResponse response, IResponderCallback responderCallback,
			boolean cascaded) throws Exception {
		SCMPMessage message = request.getMessage();
		if (message.getCacheId() == null) {
			CacheLogger.debug("message has no cache id, isReply = " + message.isReply() + ", isPart = " + message.isPart()
					+ ", message = " + message.isPollRequest());
			return false;
		}
		CacheManager cacheManager = AppContext.getCacheManager();
		String serviceName = message.getServiceName();
		Cache scmpCache = cacheManager.getCache(serviceName);
		if (scmpCache == null) {
			SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.CACHE_ERROR,
					"no cache instance, service=" + message.getServiceName());
			scmpCommandException.setMessageType(this.getKey());
			throw scmpCommandException;
		}
		CacheId cacheId = message.getFullCacheId();
		CacheLogger.debug("try loading message from cache, serviceName (" + serviceName + "), cacheId (" + cacheId.toString()
				+ "), sessionId (" + message.getSessionId() + ")");
		CacheComposite cacheComposite = scmpCache.getComposite(cacheId);
		if (cacheComposite == null) {
			// we synchronized this part and check if any other thread did start loading in the meantime
			synchronized (cacheManager) {
				cacheComposite = scmpCache.getComposite(cacheId);
				if (cacheComposite == null) {
					CacheLogger.debug("cache does not exist, start loading from server, request sessionId="
							+ message.getSessionId());
					// cache does not exist, this is the first request for it
					int oti = message.getHeaderInt(SCMPHeaderAttributeKey.OPERATION_TIMEOUT);
					scmpCache.startLoading(message, oti);
					return false;
				}
			}
		}
		if (cacheComposite != null) {
			synchronized (cacheManager) {
				// check if cache is loading
				if (cacheComposite.isLoading()) {
					// check if it is a part request and sequence nr in cache equals cache composite size
					CacheLogger.debug("cache is loading (" + cacheId + ") cacheComposite state=" + cacheComposite.getCacheState()
							+ ", loadingSessionId=" + cacheComposite.getLoadingSessionId());
					int size = cacheComposite.getSize();
					int sequenceNr = cacheId.getSequenceNrInt();
					if (!(message.isPart() && (sequenceNr == size)) && cacheComposite.isPartLoading() == false) {
						CacheLogger.info("cache is loading, retry later, service=" + message.getServiceName() + " cacheId="
								+ message.getCacheId());
						SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.CACHE_LOADING, "service="
								+ message.getServiceName() + " cacheId=" + message.getCacheId());
						scmpCommandException.setMessageType(this.getKey());
						throw scmpCommandException;
					}
					// check if this request belongs to same session id as loading cache session id
					if (cacheComposite.isLoadingSessionId(message.getSessionId()) == false) {
						CacheLogger.info("cache is loading (other sessionId), retry later, service=" + message.getServiceName()
								+ " cacheId=" + message.getCacheId() + ", cache loadingSessionId="
								+ cacheComposite.getLoadingSessionId() + ", message sessionId=" + message.getSessionId());
						SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.CACHE_LOADING, "service="
								+ message.getServiceName() + " cacheId=" + message.getCacheId());
						scmpCommandException.setMessageType(this.getKey());
						throw scmpCommandException;
					} else {
						CacheLogger.debug("cache is loading (same sessionId) service=" + message.getServiceName() + " cacheId="
								+ message.getCacheId() + ", cache loadingSessionId=" + cacheComposite.getLoadingSessionId()
								+ ", message sessionId=" + message.getSessionId());
					}
				}
			}
			if (cacheComposite.isLoaded() && cacheComposite.isExpired()) {
				// cache has been loaded but its content message is expired, in case of a full cache id we
				// must abort this communication, because we do not exactly know the state of the cache content
				// for given cache id
				if (cacheId.isCompositeId() == false) {
					CacheLogger.warn("cache is expired and has unknown state, retry later, service name = "
							+ message.getServiceName());
					SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.CACHE_LOADING,
							"cache is expired and has unknown state, retry later, service name = " + message.getServiceName());
					scmpCommandException.setMessageType(this.getKey());
					throw scmpCommandException;
				}
			}
			if (cacheComposite.isLoaded() && cacheComposite.isExpired() == false) {
				// check if this request message belongs to a part message and is not poll
				if (!(message.isPollRequest() == true || message.isPart() == false)) {
					// this request belongs to a client large message part, do not reply with any content, just a PAC
					CacheLogger.debug("cache composite (" + cacheId + ") found but ignored (part is=" + message.isPart()
							+ ", poll is " + message.isPollRequest() + ", expiration time is " + cacheComposite.getExpiration());
					SCMPPart scmpReply = null;
					scmpReply = new SCMPPart(true);
					scmpReply.setServiceName(message.getServiceName());
					scmpReply.setMessageType(message.getMessageType());
					scmpReply.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, message.getMessageSequenceNr());
					// scmpReply.setBody(message.getBody());
					response.setSCMP(scmpReply);
					responderCallback.responseCallback(request, response);
					return true;
				}
				CacheLogger.debug("cache composite (" + cacheId + ") found and loaded, expiration time is "
						+ cacheComposite.getExpiration());
				// cache has been loaded, try to get cache message, get the first one if cache id belongs to composite id
				// increment cache id sequence nr
				cacheId = cacheId.nextSequence();
				CacheMessage cacheMessage = scmpCache.getMessage(cacheId);
				if (cacheMessage == null) {
					// cache message is not part of this composite, check if message sequence number is valid or out of scope
					if (cacheComposite.isValidCacheId(cacheId)) {
						scmpCache.removeComposite(message.getSessionId(), cacheId.getCacheId());
						// cache id sequence nr is valid, but message does not exist, cache is invalid
						CacheLogger.error("cache has illegal state, loaded but message is not part of cache, cacheId="
								+ message.getCacheId());
						CacheLogger.error("cache has illegal state, cache composite [" + cacheId.getCacheId()
								+ " will be removed, retry again");
						SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.CACHE_ERROR,
								"cache has illegal state, cache composite [" + cacheId.getCacheId()
										+ " will be removed, retry again");
						scmpCommandException.setMessageType(this.getKey());
						throw scmpCommandException;
					}
					CacheLogger.error("cache has illegal state, loaded but no message, cacheId=" + message.getCacheId());
					SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.CACHE_ERROR,
							"cache has illegal state, loaded but no message, cacheId=" + message.getCacheId());
					scmpCommandException.setMessageType(this.getKey());
					throw scmpCommandException;
				}
				SCMPMessage scmpReply = null;
				if (cacheComposite.isLastMessage(cacheMessage)) {
					scmpReply = new SCMPMessage();
				} else {
					scmpReply = new SCMPPart();
				}
				// write cache composite header to scmp message, reply sessionId later
				cacheComposite.writeHeaderToMessage(scmpReply);
				// scmpReply.setServiceName(message.getServiceName()); not necessary, take service name from cache composite
				scmpReply.setSessionId(message.getSessionId()); // replace session id
				// scmpReply.setMessageType(getKey()); not necessary, take message type from cache composite
				cacheId = cacheMessage.getCacheId();
				if (cacheId == null) {
					CacheLogger.error("cache message has illegal state, cacheId=null");
					SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.CACHE_ERROR,
							"cache message has illegal state, cacheId=null");
					scmpCommandException.setMessageType(this.getKey());
					throw scmpCommandException;
				}
				scmpReply.setFullCacheId(cacheId);
				scmpReply.setHeader(SCMPHeaderAttributeKey.CACHE_EXPIRATION_DATETIME, cacheComposite.getExpiration());
				// give message sequence nr back to requester
				String messageSequenceNr = cacheMessage.getMessageSequenceNr();
				if (messageSequenceNr == null) {
					CacheLogger.error("cache message has illegal state, messageSequenceNr=null");
					SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.CACHE_ERROR,
							"cache message has illegal state, messageSequenceNr=null");
					scmpCommandException.setMessageType(this.getKey());
					throw scmpCommandException;
				}
				scmpReply.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, cacheMessage.getMessageSequenceNr());
				CacheLogger.debug("cache reply, cacheId=" + cacheId + ", messageSequenceNr=" + messageSequenceNr
						+ ", expirationDateTime=" + cacheComposite.getExpiration());
				if (cacheMessage.isCompressed()) {
					scmpReply.setHeaderFlag(SCMPHeaderAttributeKey.COMPRESSION);
				}
				scmpReply.setBody(cacheMessage.getBody());

				response.setSCMP(scmpReply);
				// schedule session timeout
				if (cascaded == false) {
					String sessionId = message.getSessionId();
					Session session = this.sessionRegistry.getSession(sessionId);
					this.sessionRegistry.scheduleSessionTimeout(session);
					session.setPendingRequest(false);
				}
				responderCallback.responseCallback(request, response);
				CacheLogger.debug("Sent a cache message to the client cacheId=" + cacheId + ", messageSequenceNr="
						+ messageSequenceNr + ", expirationDateTime=" + cacheComposite.getExpiration());
				return true; // message loaded from cache
			}
		}
		return false; // message not loaded from cache
	}
}