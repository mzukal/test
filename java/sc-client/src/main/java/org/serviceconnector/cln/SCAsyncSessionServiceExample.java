/*
 *-----------------------------------------------------------------------------*
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
 *-----------------------------------------------------------------------------*
/*
/**
 * 
 */
package org.serviceconnector.cln;

import org.apache.log4j.Logger;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.SCMessageCallback;
import org.serviceconnector.api.cln.ISCClient;
import org.serviceconnector.api.cln.IService;
import org.serviceconnector.api.cln.ISessionService;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.service.ISC;
import org.serviceconnector.service.ISCMessageCallback;
import org.serviceconnector.service.IServiceContext;


/**
 * The Class SCAsyncSessionServiceExample. Demonstrates use of session service in asynchronous mode.
 */
public class SCAsyncSessionServiceExample {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(SCAsyncSessionServiceExample.class);
	
	private static boolean messageReceived = false;

	public static void main(String[] args) {
		SCAsyncSessionServiceExample example = new SCAsyncSessionServiceExample();
		example.runExample();
	}

	public void runExample() {
		ISCClient sc = null;
		try {
			sc = new SCClient();
			sc.setMaxConnections(100);

			// connects to SC, checks connection to SC
			sc.attach("localhost", 7000);

			ISessionService sessionServiceA = sc.newSessionService("simulation");
			// creates a session
			sessionServiceA.createSession("sessionInfo", 300, 60);

			SCMessage requestMsg = new SCMessage();
			requestMsg.setData("Hello World");
			requestMsg.setCompressed(false);
			ISCMessageCallback callback = new ExampleCallback(sessionServiceA);
			sessionServiceA.execute(requestMsg, callback);

			// wait until message received
			while (SCAsyncSessionServiceExample.messageReceived == false);
			// deletes the session
			sessionServiceA.deleteSession();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// disconnects from SC
				sc.detach();
			} catch (Exception e) {
				sc = null;
			}
		}
	}

	/**
	 * The Class ExampleCallback. Callback used for asynchronously execution.
	 */
	private class ExampleCallback extends SCMessageCallback {

		public ExampleCallback(IService service) {
			super(service);
		}

		@Override
		public void callback(SCMessage msg) {
			IServiceContext serviceContext = (IServiceContext) this.getService().getContext();
			ISC serviceConnector = serviceContext.getServiceConnector();
			System.out.println(msg);
			SCAsyncSessionServiceExample.messageReceived = true;
		}

		@Override
		public void callback(Exception ex) {
			logger.error("callback", ex);
		}
	}
}