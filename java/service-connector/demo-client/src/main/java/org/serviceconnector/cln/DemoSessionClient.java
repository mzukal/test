/*-----------------------------------------------------------------------------*
 *                                                                             *
 *       Copyright © 2010 STABILIT Informatik AG, Switzerland                  *
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
package org.serviceconnector.cln;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.api.cln.SCMessageCallback;
import org.serviceconnector.api.cln.SCSessionService;
import org.serviceconnector.net.ConnectionType;

/**
 * The Class DemoSessionClient.
 */
@SuppressWarnings("unused")
public class DemoSessionClient {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DemoSessionClient.class);

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		DemoSessionClient demoSessionClient = new DemoSessionClient();
		demoSessionClient.run();
	}

	/**
	 * Run.
	 */
	public void run() {

		// Connection to SC over HTTP
		SCClient sc = new SCClient("localhost", 7000, ConnectionType.NETTY_HTTP);
		SCSessionService service = null;

		try {
			sc.setMaxConnections(20); // can be set before attach, default 100 Connections
			sc.setKeepAliveIntervalSeconds(10); // can be set before attach, default 0 -> inactive
			sc.attach(); // attaching client to SC , communication starts

			String serviceName = "session-1";
			service = sc.newSessionService(serviceName); // name of the service to use
			service.setEchoIntervalSeconds(10); // can be set before create session
			service.setEchoTimeoutSeconds(2); // can be set before create session

			SCMessage msg = new SCMessage();
			msg.setSessionInfo("session-info"); // optional
			msg.setData("certificate or what so ever"); // optional
			SCMessageCallback cbk = new DemoSessionClientCallback(service); // callback on service!!
			SCMessage reply = service.createSession(10, msg, cbk); // create a session within 10 seconds
			Object body = reply.getData();

			String sid = service.getSessionId();

			SCMessage requestMsg = new SCMessage();
			SCMessage responseMsg = new SCMessage();

			for (int i = 0; i < 5; i++) {
				requestMsg.setData("body nr : " + i);
				responseMsg = service.execute(requestMsg); // regular synchronous call
				LOGGER.info("Message sent sync=" + requestMsg.getData());
				LOGGER.info("Message received sync=" + responseMsg.getData());
				Thread.sleep(2000);
			}
			for (int i = 0; i < 5; i++) {
				requestMsg.setData("body nr : " + i);
				service.send(requestMsg); // regular asynchronous call
				LOGGER.info("Message sent async=" + requestMsg.getData());
				Thread.sleep(2000);
			}

			requestMsg.setData("cache message body");
			requestMsg.setCacheId("700");
			responseMsg = service.execute(requestMsg); // regular synchronous call
			LOGGER.info("Message sent to put in cache=" + requestMsg.getData());
			LOGGER.info("Message received=" + responseMsg.getData());

			responseMsg = service.execute(requestMsg); // regular synchronous call
			LOGGER.info("Message sent with cacheId=" + requestMsg.getData());
			LOGGER.info("Message received from cache=" + responseMsg.getData());
		} catch (Exception e) {
			LOGGER.error("run", e);
		} finally {
			try {
				SCMessage msg = new SCMessage();
				msg.setSessionInfo("kill server");
				service.deleteSession(5, msg);
				sc.detach(2); // detaches from SC, stops communication
			} catch (Exception e) {
				LOGGER.error("cleanup", e);
			}
		}
	}

	/**
	 * The Class DemoSessionClientCallback.
	 */
	private class DemoSessionClientCallback extends SCMessageCallback {

		/** The reply message. */
		private SCMessage replyMessage;

		/**
		 * Instantiates a new demo session client callback.
		 *
		 * @param service the service
		 */
		public DemoSessionClientCallback(SCSessionService service) {
			super(service);
		}

		/** {@inheritDoc} */
		@Override
		public void receive(SCMessage reply) {
			System.out.println("DemoSessionClient.DemoSessionClientCallback.receive() async" + reply.toString());
			this.replyMessage = reply;
		}

		/** {@inheritDoc} */
		@Override
		public void receive(Exception e) {
			System.out.println("DemoSessionClient.DemoSessionClientCallback.receive() " + e.getMessage());
		}
	}
}
