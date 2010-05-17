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
package com.stabilit.sc.cln.call;

import com.stabilit.sc.cln.client.IClient;
import com.stabilit.sc.scmp.SCMPHeaderAttributeKey;
import com.stabilit.sc.scmp.SCMPMessage;
import com.stabilit.sc.scmp.internal.SCMPPart;

/**
 * The Class SCMPCallAdapter. Provides basic functionality for calls.
 * 
 * @author JTraber
 */
public abstract class SCMPServerCallAdapter extends SCMPCallAdapter {

	public SCMPServerCallAdapter() {
		this(null, null);
	}

	public SCMPServerCallAdapter(IClient client, SCMPMessage clientMessage) {
		this.client = client;
		this.scmpSession = null;
		if (clientMessage != null) {
			if (clientMessage.isPart()) {
				// on SC scmpSession might be a part - call to server must be a part too
				this.requestMessage = new SCMPPart();
				this.requestMessage.setHeader(clientMessage.getHeader());
			} else {
				this.requestMessage = new SCMPMessage();
			}
			this.requestMessage.setSessionId(clientMessage.getSessionId());
			this.requestMessage.setHeader(SCMPHeaderAttributeKey.SERVICE_NAME, clientMessage
					.getHeader(SCMPHeaderAttributeKey.SERVICE_NAME));
		}

		if (this.requestMessage == null) {
			this.requestMessage = new SCMPMessage();
		}		
	}

	@Override
	public abstract ISCMPCall newInstance(IClient client, SCMPMessage clientMessage);
	
}