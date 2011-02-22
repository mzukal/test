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
package org.serviceconnector.web;

public interface IWebSession {

	/**
	 * Update access timestamp
	 */
	public abstract void access();

	/**
	 * Checks if is expired.
	 *
	 * @param timeoutMinutes the timeout minutes
	 * @return true, if is expired
	 */
	public boolean isExpired(long timeoutMinutes);
	
	/**
	 * Gets the session id.
	 *
	 * @return the session id
	 */
	public abstract String getSessionId();

	/**
	 * Gets the host.
	 *
	 * @return the host
	 */
	public abstract String getHost();

	/**
	 * Sets the host.
	 */
	public abstract void setHost(String host);
	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	public abstract int getPort();
	
	/**
	 * Sets the port.
	 *
	 * @param port the new port
	 */
	public abstract void setPort(int port);
	/**
	 * Gets the attribute.
	 *
	 * @param key the key
	 * @return the attribute
	 */
	public abstract Object getAttribute(String key);

	/**
	 * Sets the attribute.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public abstract void setAttribute(String key, Object value);

	/**
	 * Removes the attribute.
	 *
	 * @param key the key
	 * @return the object
	 */
	public abstract Object removeAttribute(String key);

}
