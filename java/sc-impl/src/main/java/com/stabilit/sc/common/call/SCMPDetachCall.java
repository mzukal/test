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
package com.stabilit.sc.common.call;

import org.apache.log4j.Logger;

import com.stabilit.sc.cln.call.ISCMPCall;
import com.stabilit.sc.cln.call.SCMPCallAdapter;
import com.stabilit.sc.common.net.req.IRequester;
import com.stabilit.sc.common.scmp.SCMPMsgType;

/**
 * The Class SCMPDetachCall. Call detaches on SCMP level.
 * 
 * @author JTraber
 */
public class SCMPDetachCall extends SCMPCallAdapter {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(SCMPDetachCall.class);
	
	/**
	 * Instantiates a new SCMPDetachCall.
	 */
	public SCMPDetachCall() {
		this(null);
	}

	/**
	 * Instantiates a new SCMPDetachCall.
	 * 
	 * @param req
	 *            the client to use when invoking call
	 */
	public SCMPDetachCall(IRequester req) {
		super(req);
	}

	/** {@inheritDoc} */
	@Override
	public ISCMPCall newInstance(IRequester req) {
		return new SCMPDetachCall(req);
	}

	/** {@inheritDoc} */
	@Override
	public SCMPMsgType getMessageType() {
		return SCMPMsgType.DETACH;
	}
}