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
package org.serviceconnector.util;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TimerTaskWrapper. Wraps TimerTaks from JDK. Is used to time a process. TimerTaks times out and calls the target ITimerRun.
 */
public class TimerTaskWrapper extends TimerTask {

	/** The Constant LOGGER. */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(TimerTaskWrapper.class);

	/** The target to run when time is out. */
	private ITimeout target;

	/**
	 * Instantiates a TimerTaskWrapper.
	 *
	 * @param target the target
	 */
	public TimerTaskWrapper(ITimeout target) {
		this.target = target;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		if (target != null) {
			target.timeout();
			return;
		}
		throw new UnsupportedOperationException("no target specified");
	}
}
