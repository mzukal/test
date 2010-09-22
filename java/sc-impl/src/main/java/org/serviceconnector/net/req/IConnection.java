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
package org.serviceconnector.net.req;

import org.serviceconnector.factory.IFactoryable;
import org.serviceconnector.scmp.ISCMPCallback;
import org.serviceconnector.scmp.SCMPMessage;


/**
 * The Interface IConnection abstracts any connection to a responder.
 * 
 * @author JTraber
 */
public interface IConnection extends IFactoryable {

	public abstract IConnectionContext getContext();

	public abstract void setContext(IConnectionContext connectionContext);

	/**
	 * Sets the host.
	 * 
	 * @param host
	 *            the host
	 */
	
	public void setHost(String host);

	/**
	 * Sets the port.
	 * 
	 * @param port
	 *            the port
	 */
	public void setPort(int port);
	
	/**
	 * Connect.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public abstract void connect() throws Exception;

	/**
	 * Send and receive asynchronous operation.
	 * 
	 * @param scmp
	 *            the scmp
	 * @param timeoutMillis
	 *            the timeout milliseconds
	 * @param callback
	 *            the callback
	 * @return the scmp
	 * @throws Exception
	 *             the exception
	 */
	public abstract void send(SCMPMessage scmp, ISCMPCallback callback) throws Exception;

	/**
	 * Disconnect.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public abstract void disconnect() throws Exception;

	/**
	 * Destroys connection.
	 */
	public abstract void destroy();

	/**
	 * Checks if is connected.
	 * 
	 * @return true, if is connected
	 */
	public abstract boolean isConnected();

	/**
	 * Sets the idle timeout.
	 * 
	 * @param idleTimeout
	 *            the new idle timeout
	 */
	public abstract void setIdleTimeout(int idleTimeout);

	public abstract void incrementNrOfIdles();

	public abstract void resetNrOfIdles();

	public abstract int getNrOfIdlesInSequence();
}
