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
package com.stabilit.sc.unit.test.session;

import org.junit.After;
import org.junit.Before;

import com.stabilit.sc.cln.scmp.SCMPSession;
import com.stabilit.sc.unit.test.register.SuperRegisterTestCase;

/**
 * @author JTraber
 */
public abstract class SuperSessionRegisterTestCase extends SuperRegisterTestCase {

	protected SCMPSession scmpSession = null;

	/**
	 * The Constructor.
	 * 
	 * @param fileName
	 *            the file name
	 */
	public SuperSessionRegisterTestCase(String fileName) {
		super(fileName);
	}

	@Before
	public void setup() throws Exception {
		super.setup();
		clnCreateSessionBefore();
	}

	@After
	public void tearDown() throws Exception {
		clnDeleteSessionAfter();
		super.tearDown();
	}

	public void clnCreateSessionBefore() throws Exception {
		this.scmpSession = new SCMPSession(this.client);
		this.scmpSession.setServiceName("simulation");
		this.scmpSession.setSessionInfo("SNBZHP - TradingClientGUI 10.2.7");
		this.scmpSession.createSession();
	}

	public void clnDeleteSessionAfter() throws Exception {
		this.scmpSession.deleteSession();
	}
}