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
package org.serviceconnector.api.cln;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.serviceconnector.Constants;
import org.serviceconnector.call.SCMPInspectCall;
import org.serviceconnector.call.SCMPManageCall;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.service.SCServiceException;
import org.serviceconnector.service.ServiceState;
import org.serviceconnector.util.URLCallString;
import org.serviceconnector.util.URLParameterString;

/**
 * Management client to an SC.
 * 
 * @author JTrnka
 */
public class SCMgmtClient extends SCClient {
	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(SCMgmtClient.class);

	public SCMgmtClient(String host, int port) {
		super(host, port);
	}

	public SCMgmtClient(String host, int port, ConnectionType connectionType) {
		super(host, port, connectionType);
	}

	/**
	 * Disable service on SC.
	 * 
	 * @param serviceName
	 *            the service name
	 */
	public void disableService(String serviceName) throws SCServiceException {
		if (this.attached == false) {
			// disableService not possible - client not attached
			throw new SCServiceException("client not attached - disableService not possible.");
		}
		String body = this.manageCall(Constants.DISABLE + Constants.EQUAL_SIGN + serviceName);
		if (body != null) {
			throw new SCServiceException(body);
		}
	}

	/**
	 * Enable service on SC.
	 * 
	 * @param serviceName
	 *            the service name
	 */
	public void enableService(String serviceName) throws SCServiceException {
		if (this.attached == false) {
			// enableService not possible - client not attached
			throw new SCServiceException("client not attached - enableService not possible.");
		}
		String body = this.manageCall(Constants.ENABLE + Constants.EQUAL_SIGN + serviceName);
		if (body != null) {
			throw new SCServiceException(body);
		}
	}

	/**
	 * Checks if service is enabled on SC.
	 * 
	 * @param serviceName
	 *            the service name
	 * @return true, if is service enabled
	 */
	public boolean isServiceEnabled(String serviceName) throws SCServiceException {
		if (this.attached == false) {
			throw new SCServiceException("client not attached - isServiceEnabled not possible.");
		}
		String body = this.inspectCall(Constants.STATE + Constants.EQUAL_SIGN + serviceName);
		if (ServiceState.ENABLED.toString().equalsIgnoreCase(body)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the number of available and allocated sessions for given service name.
	 * 
	 * @param serviceName
	 *            the service name
	 * @return string containing the available and allocated sessions, e.g. "4/2".
	 * @throws SCServiceException
	 *             the SC service exception
	 */
	public String getWorkload(String serviceName) throws SCServiceException {
		if (this.attached == false) {
			throw new SCServiceException("client not attached - isServiceEnabled not possible.");
		}
		return this.inspectCall(Constants.SESSIONS + Constants.EQUAL_SIGN + serviceName);
	}

	/**
	 * inspects the cache for given service name and cacheId.
	 *
	 * @param serviceName the service name
	 * @param cacheId the cache id
	 * @throws SCServiceException the SC service exception
	 */
	public URLParameterString inspectCache(String serviceName, String cacheId) throws SCServiceException {
		if (this.attached == false) {
			throw new SCServiceException("client not attached - inspectCache not possible.");
		}
		URLCallString callString = new URLCallString(Constants.INSPECT_CACHE, serviceName, cacheId);
		String body = this.inspectCall(callString.toString());
		if (body == null) {
			throw new SCServiceException(body);
		}
		try {
			return new URLParameterString(body);
		} catch (UnsupportedEncodingException e) {
			throw new SCServiceException(e.toString());
		}
	}

	/**
	 * Clears the cache for given service name.
	 * 
	 * @param serviceName
	 *            the service name
	 * @throws SCServiceException
	 *             the SC service exception
	 */
	public void clearCache(String serviceName) throws SCServiceException {
		if (this.attached == false) {
			throw new SCServiceException("client not attached - clearCache not possible.");
		}
		String body = this.manageCall(Constants.CLEAR_CACHE + Constants.EQUAL_SIGN + serviceName);
		if (body != null) {
			throw new SCServiceException(body);
		}
	}
	
	/**
	 * Request dump.
	 * 
	 * @throws SCServiceException
	 *             the SC service exception
	 */
	public void dump() throws SCServiceException {
		if (this.attached == false) {
			throw new SCServiceException("client not attached - dump not possible.");
		}
		this.manageCall(Constants.DUMP);
	}

	
	/**
	 * Kill SC.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void killSC() throws SCServiceException {
		if (this.attached == false) {
			throw new SCServiceException("client not attached - killSC not possible.");
		}
		this.manageCall(Constants.KILL);
		try {
			// sleep to assure kill is sent
			Thread.sleep(1000);
		} catch (Exception e) {
			// ignore exception
		}
		this.attached = false;
		// destroy connection pool
		this.requester.destroy();
		synchronized (AppContext.communicatorsLock) {
			AppContext.attachedCommunicators.decrementAndGet();
			// release resources
			AppContext.destroy();
		}
	}

	/**
	 * Inspect call.
	 * 
	 * @param instruction
	 *            the instruction
	 * @return the string
	 * @throws SCServiceException
	 *             the sC service exception
	 */
	private String inspectCall(String instruction) throws SCServiceException {
		SCMPInspectCall inspectCall = new SCMPInspectCall(this.requester);
		SCServiceCallback callback = new SCServiceCallback(true);
		try {
			inspectCall.setRequestBody(instruction);
			inspectCall.invoke(callback, Constants.DEFAULT_OPERATION_TIMEOUT_SECONDS * Constants.SEC_TO_MILLISEC_FACTOR);
		} catch (Exception e) {
			this.requester.destroy();
			throw new SCServiceException("inspect request failed", e);
		}
		if (instruction.equalsIgnoreCase(Constants.KILL)) {
			// on KILL SC cannot reply a message
			return null;
		}
		SCMPMessage reply = callback.getMessageSync(Constants.DEFAULT_OPERATION_TIMEOUT_SECONDS * Constants.SEC_TO_MILLISEC_FACTOR);
		if (reply.isFault()) {
			SCServiceException ex = new SCServiceException("inspect failed");
			ex.setSCErrorCode(reply.getHeader(SCMPHeaderAttributeKey.SC_ERROR_CODE));
			ex.setSCErrorText(reply.getHeader(SCMPHeaderAttributeKey.SC_ERROR_TEXT));
			throw ex;
		}
		return (String) reply.getBody();
	}

	/**
	 * Process a manage call.
	 * 
	 * @param instruction
	 *            the instruction
	 * @throws SCServiceException
	 *             the SC service exception
	 */
	private String manageCall(String instruction) throws SCServiceException {
		SCMPManageCall manageCall = new SCMPManageCall(this.requester);
		SCServiceCallback callback = new SCServiceCallback(true);
		try {
			manageCall.setRequestBody(instruction);
			manageCall.invoke(callback, Constants.DEFAULT_OPERATION_TIMEOUT_SECONDS * Constants.SEC_TO_MILLISEC_FACTOR);
		} catch (Exception e) {
			this.requester.destroy();
			throw new SCServiceException(instruction + " SC failed", e);
		}
		if (instruction.equalsIgnoreCase(Constants.KILL)) {
			// kill SC doesn't reply a message
			return null;
		}
		SCMPMessage reply = callback.getMessageSync(Constants.DEFAULT_OPERATION_TIMEOUT_SECONDS * Constants.SEC_TO_MILLISEC_FACTOR);
		if (reply.isFault()) {
			SCServiceException ex = new SCServiceException("manage failed");
			ex.setSCErrorCode(reply.getHeader(SCMPHeaderAttributeKey.SC_ERROR_CODE));
			ex.setSCErrorText(reply.getHeader(SCMPHeaderAttributeKey.SC_ERROR_TEXT));
			throw ex;
		}
		return (String) reply.getBody();
	}
}
