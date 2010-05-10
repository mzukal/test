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
package com.stabilit.sc;

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.stabilit.sc.cmd.factory.impl.ServiceConnectorCommandFactory;
import com.stabilit.sc.listener.ExceptionListenerSupport;
import com.stabilit.sc.registry.ConnectionRegistry;
import com.stabilit.sc.registry.ServiceRegistry;
import com.stabilit.sc.registry.SessionRegistry;
import com.stabilit.sc.server.SCServerFactory;
import com.stabilit.sc.srv.cmd.factory.CommandFactory;
import com.stabilit.sc.srv.conf.ServerConfig;
import com.stabilit.sc.srv.conf.ServerConfig.ServerConfigItem;
import com.stabilit.sc.srv.config.IServerConfigItem;
import com.stabilit.sc.srv.server.IServer;

/**
 * The Class ServiceConnector. Starts the core (servers) of the Service Connector.
 * 
 * @author JTraber
 */
public final class ServiceConnector {

	/**
	 * Instantiates a new service connector.
	 */
	private ServiceConnector() {
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static void main(String[] args) throws Exception {
		ServiceConnector.run();
	}

	/**
	 * Run SC servers.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private static void run() throws Exception {
		ServerConfig config = new ServerConfig();
		config.load("sc.properties");

		CommandFactory commandFactory = CommandFactory.getCurrentCommandFactory();
		if (commandFactory == null) {
			CommandFactory.setCurrentCommandFactory(new ServiceConnectorCommandFactory());
		}

		// Necessary to make access for JMX client available
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName mxbeanNameConnReg = new ObjectName("com.stabilit.sc.registry:type=ConnectionRegistry");
		ObjectName mxbeanNameSessReg = new ObjectName("com.stabilit.sc.registry:type=SessionRegistry");
		ObjectName mxbeanNameServReg = new ObjectName("com.stabilit.sc.registry:type=ServiceRegistry");

		// Register the Queue Sampler MXBean
		mbs.registerMBean(ConnectionRegistry.getCurrentInstance(), mxbeanNameConnReg);
		mbs.registerMBean(SessionRegistry.getCurrentInstance(), mxbeanNameSessReg);
		mbs.registerMBean(ServiceRegistry.getCurrentInstance(), mxbeanNameServReg);

		List<ServerConfigItem> serverConfigList = config.getServerConfigList();
		SCServerFactory serverFactory = new SCServerFactory();
		for (IServerConfigItem serverConfig : serverConfigList) {
			IServer server = serverFactory.newInstance(serverConfig);
			try {
				server.create();
				server.runAsync();
			} catch (Exception e) {
				ExceptionListenerSupport.getInstance().fireException(ServiceConnector.class, e);
			}
		}
	}
}
