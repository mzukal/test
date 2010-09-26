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
package org.serviceconnector.registry;

import org.apache.log4j.Logger;
import org.serviceconnector.sc.service.Server;


/**
 * The Class ServerRegistry. Stores an entry for every registered server in system.
 * 
 * @author JTraber
 */
public class ServerRegistry extends Registry<String, Server> {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(ServerRegistry.class);
	
	/** The instance. */
	private static ServerRegistry instance = new ServerRegistry();

	/**
	 * Instantiates a new server registry.
	 */
	private ServerRegistry() {
	}

	/**
	 * Gets the current instance of server registry.
	 * 
	 * @return the current instance
	 */
	public static ServerRegistry getCurrentInstance() {
		return instance;
	}

	/**
	 * Adds an entry of a server.
	 * 
	 * @param key
	 *            the key
	 * @param server
	 *            the server
	 */
	public void addServer(String key, Server server) {
		this.put(key, server);
	}

	/**
	 * Gets the server.
	 * 
	 * @param key
	 *            the key
	 * @return the server
	 */
	public Server getServer(String key) {
		return super.get(key);
	}

	/**
	 * Removes the server.
	 * 
	 * @param key
	 *            the key
	 */
	public void removeServer(String key) {
		super.remove(key);
	}
}