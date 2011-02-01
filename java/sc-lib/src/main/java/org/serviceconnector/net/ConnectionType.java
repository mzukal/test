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
package org.serviceconnector.net;

import org.serviceconnector.util.IReversibleEnum;
import org.serviceconnector.util.ReverseEnumMap;

public enum ConnectionType implements IReversibleEnum<String, ConnectionType>{

	NETTY_TCP("netty.tcp"), 	//
	NETTY_HTTP("netty.http"),	//
	NETTY_WEB("netty.web"),		//
	NETTY_PROXY_HTTP("netty-proxy.http"),	//
	DEFAULT_CLIENT_CONNECTION_TYPE(ConnectionType.NETTY_TCP.getValue()), //
	DEFAULT_SERVER_CONNECTION_TYPE(ConnectionType.NETTY_TCP.getValue()), //
	UNDEFINED("undefined");

	/** The value. */
	private String value;

	/** The reverseMap, to get access to the enum constants by string value. */
	private static final ReverseEnumMap<String, ConnectionType> reverseMap = new ReverseEnumMap<String, ConnectionType>(ConnectionType.class);

	
	/** The Connection type. */
	private ConnectionType(String value) {
		this.value = value;
	}

	public static ConnectionType getType(String typeString) {
		ConnectionType type = reverseMap.get(typeString);
		if (type == null) {
			// typeString doesn't match to a valid type
			return ConnectionType.UNDEFINED;
		}
		return type;
	}
	
	public String getValue() {
		return this.value;
	}
	
	@Override
	public ConnectionType reverse(String typeString) {
		return ConnectionType.getType(typeString);
	}
}
