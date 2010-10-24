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
import java.net.SocketAddress;

import org.apache.log4j.Logger;
import org.serviceconnector.Constants;
import org.serviceconnector.cmd.sc.CommandCallback;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.net.connection.ConnectionPool;
import org.serviceconnector.net.req.IRequester;
import org.serviceconnector.net.req.Requester;
import org.serviceconnector.net.req.RequesterContext;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.service.AbstractSession;

/**
 * The Class Server. Represents a server instance on a backend Server. Serves a service. Has control over the max of sessions and
 * holds a connection pool to communicate to backend server.
 * 
 * @author JTraber
 */
public abstract class Server {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(Server.class);

	/** The host. */
	protected String host;
	/** The port number. */
	protected int portNr;
	/** The socket address. */
	protected InetSocketAddress socketAddress;
	/** The service name. */
	protected String serviceName;
	/** The max connections. */
	private int maxConnections;
	/** The requester. */
	protected IRequester requester;
	/** The operation timeout multiplier. */
	protected double operationTimeoutMultiplier;
	/** The type. */
	private ServerType type;
	/** The server key. */
	protected String serverKey;

	/**
	 * Instantiates a new server.
	 * 
	 * @param type
	 *            the type
	 * @param socketAddress
	 *            the socket address
	 * @param serviceName
	 *            the service name
	 * @param portNr
	 *            the port number
	 * @param maxConnections
	 *            the max connections
	 * @param connectionType
	 *            the connection type
	 * @param keepAliveInterval
	 *            the keep alive interval
	 * @param operationTimeoutMultiplier
	 *            the operation timeout multiplier
	 */
	public Server(ServerType type, InetSocketAddress socketAddress, String serviceName, int portNr, int maxConnections,
			String connectionType, int keepAliveInterval, double operationTimeoutMultiplier) {
		this.serviceName = serviceName;
		this.socketAddress = socketAddress;
		this.type = type;
		this.portNr = portNr;
		this.maxConnections = maxConnections;
		this.operationTimeoutMultiplier = operationTimeoutMultiplier;
		this.host = socketAddress.getHostName();
		ConnectionPool connectionPool = new ConnectionPool(host, portNr, connectionType, keepAliveInterval);
		connectionPool.setMaxConnections(maxConnections);
		this.requester = new Requester(new RequesterContext(connectionPool, null));
		this.serverKey = serviceName + "_" + socketAddress.getHostName() + "/" + socketAddress.getPort();
	}

	/**
	 * Gets the socket address.
	 * 
	 * @return the socket address
	 */
	public SocketAddress getSocketAddress() {
		return socketAddress;
	}

	/**
	 * Immediate connect.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void immediateConnect() throws Exception {
		ConnectionPool connectionPool = this.requester.getContext().getConnectionPool();
		// set minimum connections to max for initial process
		connectionPool.setMinConnections(connectionPool.getMaxConnections());
		connectionPool.initMinConnections();
		// initial done - set it back to 1
		connectionPool.setMinConnections(1);
	}

	/**
	 * Abort session on server.
	 * 
	 * @param abortMessage
	 *            the abort message
	 */
	public abstract void abortSession(AbstractSession session);
	
	/**
	 * Destroy server.
	 */
	public void destroy() {
		this.requester.getContext().getConnectionPool().destroy();
		AppContext.getCurrentContext().getServerRegistry().removeServer(this.getServerKey());
		this.requester = null;
	}

	/**
	 * Gets the host.
	 * 
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Gets the port number.
	 * 
	 * @return the port number
	 */
	public int getPortNr() {
		return portNr;
	}

	/**
	 * Gets the max connections.
	 * 
	 * @return the max connections
	 */
	public int getMaxConnections() {
		return maxConnections;
	}

	/**
	 * Gets the service name.
	 * 
	 * @return the service name
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public ServerType getType() {
		return this.type;
	}

	public String getServerKey() {
		return serverKey;
	}

	/**
	 * Gets the requester.
	 * 
	 * @return the requester
	 */
	public IRequester getRequester() {
		return requester;
	}

	/** @{inheritDoc */
	@Override
	public String toString() {
		return this.getServerKey() + ":" + portNr;
	}
}