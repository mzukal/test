/*
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
 */
package org.serviceconnector.service;

import java.util.UUID;

import org.serviceconnector.server.Server;
import org.serviceconnector.util.TimerTaskWrapper;

public abstract class AbstractSession {

	/** The id. */
	private String id;
	/** The server. */
	protected Server server;
	/** The ip address list. */
	private String ipAddressList;
	/** The session info. */
	private String sessionInfo;

	/** The session timeouter - observes session timeout. */
	private TimerTaskWrapper sessionTimeouter;

	/**
	 * Instantiates a new session.
	 */
	public AbstractSession(String sessionInfo, String ipAddressList) {
		UUID uuid = UUID.randomUUID();
		this.id = uuid.toString();
		this.server = null;
		this.sessionTimeouter = null;
		this.ipAddressList = ipAddressList;
		this.sessionInfo = sessionInfo;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Sets the server.
	 * 
	 * @param server
	 *            the new server
	 */
	public void setServer(Server server) {
		this.server = server;
	}

	/**
	 * Gets the server.
	 * 
	 * @return the server
	 */
	public Server getServer() {
		return this.server;
	}

	/**
	 * Gets the session timeouter.
	 * 
	 * @return the session timeouter
	 */
	public TimerTaskWrapper getSessionTimeouter() {
		return sessionTimeouter;
	}

	/**
	 * Sets the session timeouter.
	 * 
	 * @param sessionTimeouter
	 *            the new session timeouter
	 */
	public void setSessionTimeouter(TimerTaskWrapper sessionTimeouter) {
		this.sessionTimeouter = sessionTimeouter;
	}

	/**
	 * Gets the ip address list.
	 *
	 * @return the ip address list
	 */
	public String getIpAddressList() {
		return ipAddressList;
	}

	/**
	 * Gets the session info.
	 *
	 * @return the session info
	 */
	public String getSessionInfo() {
		return sessionInfo;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return id + ":" + server;
	}
}
