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
package com.stabilit.scm.unit.test.attach;

import org.junit.After;
import org.junit.Before;

import com.stabilit.scm.common.call.SCMPAttachCall;
import com.stabilit.scm.common.call.SCMPCallFactory;
import com.stabilit.scm.common.call.SCMPDetachCall;
import com.stabilit.scm.common.scmp.SCMPHeaderAttributeKey;
import com.stabilit.scm.common.util.SynchronousCallback;
import com.stabilit.scm.unit.test.SuperTestCase;

/**
 * @author JTraber
 */
public abstract class SuperAttachTestCase extends SuperTestCase {

	protected String localDateTimeOfConnect;
	protected SuperAttachCallback attachCallback;

	/**
	 * The Constructor.
	 * 
	 * @param fileName
	 *            the file name
	 */
	public SuperAttachTestCase(String fileName) {
		super(fileName);
		this.attachCallback = new SuperAttachCallback();
	}

	@Before
	public void setup() throws Exception {
		super.setup();
		clnAttachBefore();
	}

	@After
	public void tearDown() throws Exception {
		clnDetachAfter();
		super.tearDown();
	}

	public void clnAttachBefore() throws Exception {
		SCMPAttachCall attachCall = (SCMPAttachCall) SCMPCallFactory.ATTACH_CALL.newInstance(req);
		attachCall.setKeepAliveInterval(0);
		attachCall.invoke(this.attachCallback);
		this.attachCallback.getMessageSync();
		localDateTimeOfConnect = attachCall.getRequest().getHeader(SCMPHeaderAttributeKey.LOCAL_DATE_TIME);
	}

	public void clnDetachAfter() throws Exception {
		SCMPDetachCall detachCall = (SCMPDetachCall) SCMPCallFactory.DETACH_CALL.newInstance(req);
		detachCall.invoke(this.attachCallback);
		this.attachCallback.getMessageSync();
	}

	protected class SuperAttachCallback extends SynchronousCallback {
	}
}