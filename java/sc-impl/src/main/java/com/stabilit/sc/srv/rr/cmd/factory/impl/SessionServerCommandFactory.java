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
package com.stabilit.sc.srv.rr.cmd.factory.impl;

import org.apache.log4j.Logger;

import com.stabilit.sc.common.cmd.ICommand;
import com.stabilit.sc.common.cmd.factory.CommandFactory;
import com.stabilit.sc.srv.cmd.impl.SrvAbortSessionCommand;
import com.stabilit.sc.srv.rr.cmd.impl.SrvCreateSessionCommand;
import com.stabilit.sc.srv.rr.cmd.impl.SrvDeleteSessionCommand;
import com.stabilit.sc.srv.rr.cmd.impl.SrvExecuteCommand;

/**
 * A factory for creating SessionServerCommand objects. Unifies commands used by session service.
 * 
 * @author JTraber
 */
public class SessionServerCommandFactory extends CommandFactory {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(SessionServerCommandFactory.class);

	/**
	 * Instantiates a new session server command factory.
	 */
	public SessionServerCommandFactory() {
		init(this);
	}

	/**
	 * Instantiates a new session server command factory.
	 * 
	 * @param commandFactory
	 *            the command factory
	 */
	public SessionServerCommandFactory(CommandFactory commandFactory) {
		init(commandFactory);
	}

	/**
	 * Initialize factory.
	 * 
	 * @param commandFactory
	 *            the command factory
	 */
	public void init(CommandFactory commandFactory) {
		ICommand srvCreateSessionCommand = new SrvCreateSessionCommand();
		commandFactory.addCommand(srvCreateSessionCommand.getKey(), srvCreateSessionCommand);
		ICommand srvDeleteSessionCommand = new SrvDeleteSessionCommand();
		commandFactory.addCommand(srvDeleteSessionCommand.getKey(), srvDeleteSessionCommand);
		ICommand srvExecuteCommand = new SrvExecuteCommand();
		commandFactory.addCommand(srvExecuteCommand.getKey(), srvExecuteCommand);
		ICommand srvAbortSessionCommand = new SrvAbortSessionCommand();
		commandFactory.addCommand(srvAbortSessionCommand.getKey(), srvAbortSessionCommand);
	}
}