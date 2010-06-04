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
package com.stabilit.scm.registry;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.stabilit.scm.cln.call.SCMPCallFactory;
import com.stabilit.scm.cln.call.SCMPClnEchoCall;
import com.stabilit.scm.cln.call.SCMPClnSystemCall;
import com.stabilit.scm.cln.call.SCMPSrvCreateSessionCall;
import com.stabilit.scm.cln.call.SCMPSrvDataCall;
import com.stabilit.scm.cln.call.SCMPSrvDeleteSessionCall;
import com.stabilit.scm.cln.call.SCMPSrvEchoCall;
import com.stabilit.scm.cln.call.SCMPSrvSystemCall;
import com.stabilit.scm.cln.net.CommunicationException;
import com.stabilit.scm.cln.req.ConnectionException;
import com.stabilit.scm.cln.req.IRequester;
import com.stabilit.scm.factory.IFactoryable;
import com.stabilit.scm.listener.ExceptionPoint;
import com.stabilit.scm.scmp.IRequest;
import com.stabilit.scm.scmp.SCMPHeaderAttributeKey;
import com.stabilit.scm.scmp.SCMPMessage;
import com.stabilit.scm.srv.client.SCClientFactory;
import com.stabilit.scm.srv.config.IServerConfigItem;
import com.stabilit.scm.srv.ctx.IServerContext;
import com.stabilit.scm.srv.net.SCMPCommunicationException;
import com.stabilit.scm.util.MapBean;

/**
 * The Class ServiceRegistryItem. Provides access to a service. Gets initialized when service registers and saves
 * service information. Holds a <code>ServiceRegistryItemPool</code> to manage incoming requests.
 * 
 * @author JTraber
 */
public class ServiceRegistryItem extends MapBean<String> implements IFactoryable {

	/** The client. */
	private IRequester client;
	/** The register request, initial request from service. */
	private IRequest registerRequest; // TODO (TRN) (Done JOT) how does the scmp message relate to the service or
										// to service registry??
	/** The my item pool. */
	protected ServiceRegistryItemPool myParentPool;
	/** The allocated. */
	private boolean allocated;
	/** The obsolete. */
	private boolean obsolete;
	private SCClientFactory clientFactory;
	private int serverPort;
	private String serverHost;
	private String serverConnection;
	private int numberOfThreads;

	/**
	 * Instantiates a new service registry item.
	 * 
	 * @param serverContext
	 *            the server context
	 * @param request
	 *            the request
	 */
	public ServiceRegistryItem(IRequest request, IServerContext serverContext) {
		this.registerRequest = request; // TODO (TRN) (Done JOT) the parameters are crazy
		this.allocated = false;
		this.myParentPool = null;
		this.obsolete = false;

		SCMPMessage scmpMessage = request.getMessage();
		this.setAttributeMap(scmpMessage.getHeader());
		// setting up client to connect backend server
		this.clientFactory = new SCClientFactory();
		this.serverPort = Integer.parseInt(scmpMessage.getHeader(SCMPHeaderAttributeKey.PORT_NR));
		SocketAddress socketAddress = request.getLocalSocketAddress();
		this.serverHost = ((InetSocketAddress) socketAddress).getHostName();
		IServerConfigItem serverConfig = serverContext.getServer().getServerConfig();
		this.serverConnection = serverConfig.getConnection();
		this.numberOfThreads = serverConfig.getNumberOfThreads();
	}

	/**
	 * Srv create session. Creates a session on a backend server.
	 * 
	 * @param message
	 *            the scmp message
	 * @throws Exception
	 *             the exception
	 */
	public void srvCreateSession(SCMPMessage message) throws Exception { // TODO (TRN) This is weird!
		try {
			client = clientFactory.newInstance(serverHost, serverPort, serverConnection, numberOfThreads);
			client.connect();
		} catch (ConnectionException ex) {
			ExceptionPoint.getInstance().fireException(this, ex);
		}
		try {
			SCMPSrvCreateSessionCall createSessionCall = (SCMPSrvCreateSessionCall) SCMPCallFactory.SRV_CREATE_SESSION_CALL
					.newInstance(client);
			createSessionCall.setHeader(message.getHeader());
			createSessionCall.invoke();
			this.allocated = true;
		} catch (Exception ex) {
			ExceptionPoint.getInstance().fireException(this, ex);
		}
	}

	/**
	 * Srv delete session. Deletes a session on a server.
	 * 
	 * @param message
	 *            the scmp message
	 * @throws Exception
	 *             the exception
	 */
	public void srvDeleteSession(SCMPMessage message) throws Exception { // TODO (TRN) This is weird!
		checkServiceAlive();
		SCMPSrvDeleteSessionCall deleteSessionCall = (SCMPSrvDeleteSessionCall) SCMPCallFactory.SRV_DELETE_SESSION_CALL
				.newInstance(client);
		deleteSessionCall.setHeader(message.getHeader());
		try {
			deleteSessionCall.invoke();
		} catch (SCMPCommunicationException ex) {
			ExceptionPoint.getInstance().fireException(this, ex);
		}
		client.disconnect();
		client.destroy();
		this.allocated = false;
	}

	/**
	 * Checks if is allocated.
	 * 
	 * @return true, if is allocated
	 */
	public boolean isAllocated() {
		return this.allocated;
	}

	/**
	 * Cln echo. Executes an echo call on a service.
	 * 
	 * @param message
	 *            the scmp message
	 * @return the sCMP
	 * @throws Exception
	 *             the exception
	 */
	public SCMPMessage clnEcho(SCMPMessage message) throws Exception { // TODO (TRN) This is weird!
		checkServiceAlive();
		SCMPClnEchoCall echoCall = (SCMPClnEchoCall) SCMPCallFactory.CLN_ECHO_CALL.newInstance(client);
		echoCall.setHeader(message.getHeader());
		echoCall.setRequestBody(message.getBody());
		return echoCall.invoke();
	}

	/**
	 * Srv echo. Forwards echo call to next server node.
	 * 
	 * @param scmp
	 *            the scmp
	 * @return the sCMP
	 * @throws Exception
	 *             the exception
	 */
	public SCMPMessage srvEcho(SCMPMessage scmp) throws Exception { // TODO (TRN) This is weird!
		checkServiceAlive();
		SCMPSrvEchoCall echoCall = (SCMPSrvEchoCall) SCMPCallFactory.SRV_ECHO_CALL.newInstance(client, scmp);
		echoCall.setHeader(scmp.getHeader());
		echoCall.setHeader(SCMPHeaderAttributeKey.SERVICE_REGISTRY_ID, this.hashCode());
		echoCall.setRequestBody(scmp.getBody());
		return echoCall.invoke();
	}

	/**
	 * Srv data. Sends any data to a service.
	 * 
	 * @param scmp
	 *            the scmp
	 * @return the sCMP
	 * @throws Exception
	 *             the exception
	 */
	public SCMPMessage srvData(SCMPMessage scmp) throws Exception { // TODO (TRN) This is weird!
		checkServiceAlive();
		SCMPSrvDataCall srvDataCall = (SCMPSrvDataCall) SCMPCallFactory.SRV_DATA_CALL.newInstance(client);
		srvDataCall.setHeader(scmp.getHeader());
		srvDataCall.setRequestBody(scmp.getBody());
		try {
			return srvDataCall.invoke();
		} catch (SCMPCommunicationException ex) {
			ExceptionPoint.getInstance().fireException(this, ex);
			throw new CommunicationException("Connection lost");
		}
	}

	/**
	 * Srv system. Executes system call on a service.
	 * 
	 * @param scmp
	 *            the scmp
	 * @return the sCMP
	 * @throws Exception
	 *             the exception
	 */
	public SCMPMessage srvSystem(SCMPMessage scmp) throws Exception { // TODO (TRN) This is weird!
		checkServiceAlive();
		SCMPSrvSystemCall srvSystemCall = (SCMPSrvSystemCall) SCMPCallFactory.SRV_SYSTEM_CALL.newInstance(client);
		srvSystemCall.setHeader(scmp.getHeader());
		srvSystemCall.setRequestBody(scmp.getBody());
		return srvSystemCall.invoke();
	}

	/**
	 * Cln system. Forwards system call to next server node.
	 * 
	 * @param scmp
	 *            the scmp
	 * @return the sCMP
	 * @throws Exception
	 *             the exception
	 */
	public SCMPMessage clnSystem(SCMPMessage scmp) throws Exception { // TODO (TRN) This is weird!
		checkServiceAlive();
		SCMPClnSystemCall clnSystemCall = (SCMPClnSystemCall) SCMPCallFactory.CLN_SYSTEM_CALL.newInstance(client);
		clnSystemCall.setHeader(scmp.getHeader());
		clnSystemCall.setRequestBody(scmp.getBody());
		return clnSystemCall.invoke();
	}

	/**
	 * New instance.
	 * 
	 * @return the factoryable
	 */
	@Override
	public IFactoryable newInstance() {
		return this;
	}

	/**
	 * Mark service obsolete. Marks if server has connection to service. Obsolete means connection lost earlier.
	 */
	public void markObsolete() { // TODO (TRN) What is this ?
		this.obsolete = true;
	}

	/**
	 * Check service alive. If service is obsolete exception is thrown.
	 * 
	 * @throws CommunicationException
	 *             the communication exception
	 */
	private void checkServiceAlive() throws CommunicationException {
		if (obsolete) {
			throw new CommunicationException("Connection lost");
		}
	}
}
