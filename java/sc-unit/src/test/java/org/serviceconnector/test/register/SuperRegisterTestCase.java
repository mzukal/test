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
package org.serviceconnector.test.register;

import org.junit.After;
import org.junit.Before;
import org.serviceconnector.call.SCMPCallFactory;
import org.serviceconnector.call.SCMPDeRegisterServerCall;
import org.serviceconnector.call.SCMPRegisterServerCall;
import org.serviceconnector.conf.ICommunicatorConfig;
import org.serviceconnector.conf.RequesterConfigPool;
import org.serviceconnector.conf.ResponderConfigPool;
import org.serviceconnector.net.req.IRequester;
import org.serviceconnector.net.req.IRequesterContext;
import org.serviceconnector.net.req.SCRequester;
import org.serviceconnector.scmp.SCMPMessageId;
import org.serviceconnector.test.SCTest;
import org.serviceconnector.test.attach.SuperAttachTestCase;
import org.serviceconnector.test.pool.TestContext;



/**
 * @author JTraber
 */
public abstract class SuperRegisterTestCase extends SuperAttachTestCase {

	protected IRequester registerRequester;
	private IRequesterContext registerContext;
	private String registerFileName = "session-server.properties";
	private RequesterConfigPool registerConfig = null;
	private ResponderConfigPool responderConfig = null;

	/**
	 * The Constructor.
	 * 
	 * @param fileName
	 *            the file name
	 * @throws Exception
	 */
	public SuperRegisterTestCase(String fileName) {
		super(fileName);
	}

	@Before
	public void setup() throws Exception {
		super.setup();
		this.registerConfig = new RequesterConfigPool();
		this.responderConfig = new ResponderConfigPool();
		this.registerConfig.load(registerFileName);
		this.responderConfig.load(registerFileName);
		this.registerContext = new RegisterServerContext(registerConfig.getRequesterConfig(), this.msgId);
		this.registerRequester = new SCRequester(registerContext);
		registerServerBefore();
	}

	@After
	public void tearDown() throws Exception {
		deRegisterServerAfter();
		super.tearDown();
	}

	public void registerServerBefore() throws Exception {
		SCMPRegisterServerCall registerServerCall = (SCMPRegisterServerCall) SCMPCallFactory.REGISTER_SERVER_CALL
				.newInstance(registerRequester, "publish-simulation");
		registerServerCall.setMaxSessions(10);
		registerServerCall.setMaxConnections(10);
		registerServerCall.setPortNumber(this.responderConfig.getResponderConfigList().get(0).getPort());
		registerServerCall.setImmediateConnect(true);
		registerServerCall.setKeepAliveInterval(360);
		registerServerCall.invoke(this.attachCallback, 1000);
		SCTest.checkReply(this.attachCallback.getMessageSync());
	}

	public void deRegisterServerAfter() throws Exception {
		this.deRegisterServerAfter("publish-simulation");
	}

	public void deRegisterServerAfter(String serviceName) throws Exception {
		SCMPDeRegisterServerCall deRegisterServerCall = (SCMPDeRegisterServerCall) SCMPCallFactory.DEREGISTER_SERVER_CALL
				.newInstance(registerRequester, serviceName);
		deRegisterServerCall.invoke(this.attachCallback, 1000);
		SCTest.checkReply(this.attachCallback.getMessageSync());
	}

	private class RegisterServerContext extends TestContext {

		public RegisterServerContext(ICommunicatorConfig config, SCMPMessageId msgId) {
			super(config, msgId);
			// for register only 1 connection is allowed
			this.connectionPool.setMaxConnections(1);
		}
	}
}