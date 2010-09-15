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
package com.stabilit.sc.unit.test.register;

import org.junit.After;
import org.junit.Before;

import com.stabilit.sc.common.call.SCMPCallFactory;
import com.stabilit.sc.common.call.SCMPDeRegisterServiceCall;
import com.stabilit.sc.common.call.SCMPRegisterServiceCall;
import com.stabilit.sc.common.conf.ICommunicatorConfig;
import com.stabilit.sc.common.conf.RequesterConfigPool;
import com.stabilit.sc.common.conf.ResponderConfigPool;
import com.stabilit.sc.common.net.req.IRequester;
import com.stabilit.sc.common.net.req.IRequesterContext;
import com.stabilit.sc.common.net.req.Requester;
import com.stabilit.sc.common.scmp.SCMPMessageId;
import com.stabilit.sc.unit.TestContext;
import com.stabilit.sc.unit.test.SCTest;
import com.stabilit.sc.unit.test.attach.SuperAttachTestCase;

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
		this.registerContext = new RegisterServiceContext(registerConfig.getRequesterConfig(), this.msgId);
		this.registerRequester = new Requester(registerContext);
		registerServiceBefore();
	}

	@After
	public void tearDown() throws Exception {
		deRegisterServiceAfter();
		super.tearDown();
	}

	public void registerServiceBefore() throws Exception {
		SCMPRegisterServiceCall registerServiceCall = (SCMPRegisterServiceCall) SCMPCallFactory.REGISTER_SERVICE_CALL
				.newInstance(registerRequester, "publish-simulation");
		registerServiceCall.setMaxSessions(10);
		registerServiceCall.setMaxConnections(10);
		registerServiceCall.setPortNumber(this.responderConfig.getResponderConfigList().get(0).getPort());
		registerServiceCall.setImmediateConnect(true);
		registerServiceCall.setKeepAliveInterval(360);
		registerServiceCall.invoke(this.attachCallback, 1000);
		SCTest.checkReply(this.attachCallback.getMessageSync());
	}

	public void deRegisterServiceAfter() throws Exception {
		this.deRegisterServiceAfter("publish-simulation");
	}

	public void deRegisterServiceAfter(String serviceName) throws Exception {
		SCMPDeRegisterServiceCall deRegisterServiceCall = (SCMPDeRegisterServiceCall) SCMPCallFactory.DEREGISTER_SERVICE_CALL
				.newInstance(registerRequester, serviceName);
		deRegisterServiceCall.invoke(this.attachCallback, 1000);
		SCTest.checkReply(this.attachCallback.getMessageSync());
	}

	private class RegisterServiceContext extends TestContext {

		public RegisterServiceContext(ICommunicatorConfig config, SCMPMessageId msgId) {
			super(config, msgId);
			// for register only 1 connection is allowed
			this.connectionPool.setMaxConnections(1);
		}
	}
}