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

package com.stabilit.scm.srv;


/**
 * The Interface ISCPublishServer. Top interface for any publish service.
 * 
 * @author JTraber
 */
public interface ISCPublishServer extends ISCServer {

	/**
	 * Publish data.
	 * 
	 * @param serviceName
	 *            the service name
	 * @param mask
	 *            the mask
	 * @param data
	 *            the data
	 * @throws Exception
	 *             the exception
	 */
	public abstract void publish(String serviceName, String mask, Object data) throws Exception;

	/**
	 * Register service on SC.
	 * 
	 * @param scHost
	 *            the sc host
	 * @param scPort
	 *            the sc port
	 * @param serviceName
	 *            the service name
	 * @param maxSessions
	 *            the max sessions
	 * @param maxConnections
	 *            the max connections
	 * @param scCallback
	 *            the sc callback
	 * @throws Exception
	 *             the exception
	 */
	public abstract void registerService(String scHost, int scPort, String serviceName, int maxSessions,
			int maxConnections, ISCPublishServerCallback scCallback) throws Exception;
}
