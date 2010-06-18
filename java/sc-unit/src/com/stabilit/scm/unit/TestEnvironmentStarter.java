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
package com.stabilit.scm.unit;

import java.io.File;

import com.stabilit.scm.common.cmd.factory.CommandFactory;
import com.stabilit.scm.common.conf.ResponderConfigPool;
import com.stabilit.scm.common.log.Level;
import com.stabilit.scm.common.log.impl.ConnectionLogger;
import com.stabilit.scm.common.log.impl.ExceptionLogger;
import com.stabilit.scm.common.log.impl.LoggerFactory;
import com.stabilit.scm.common.log.impl.PerformanceLogger;
import com.stabilit.scm.common.log.impl.RuntimeLogger;
import com.stabilit.scm.common.log.impl.SessionLogger;
import com.stabilit.scm.common.log.listener.ConnectionPoint;
import com.stabilit.scm.common.log.listener.ExceptionPoint;
import com.stabilit.scm.common.log.listener.IConnectionListener;
import com.stabilit.scm.common.log.listener.IExceptionListener;
import com.stabilit.scm.common.log.listener.IPerformanceListener;
import com.stabilit.scm.common.log.listener.IRuntimeListener;
import com.stabilit.scm.common.log.listener.ISessionListener;
import com.stabilit.scm.common.log.listener.LoggerPoint;
import com.stabilit.scm.common.log.listener.PerformancePoint;
import com.stabilit.scm.common.log.listener.RuntimePoint;
import com.stabilit.scm.common.log.listener.SessionPoint;
import com.stabilit.scm.sc.SC;
import com.stabilit.scm.srv.rr.Old_SessionServer;

public class TestEnvironmentStarter {

	public static void main(String[] args) throws Exception {
		TestEnvironmentStarter starter = new TestEnvironmentStarter();
		starter.startUpServer();
	}

	private void startUpServer() throws Exception {
//		this.init();
		CommandFactory.setCurrentCommandFactory(new UnitCommandFactory());
		SC.main(null);
		Old_SessionServer.main(null);
	}

	public void init() throws Exception {
		ResponderConfigPool config = new ResponderConfigPool();
		config.load("sc.properties");

		deleteLog();
		// setup loggers
		try {
			LoggerFactory loggerFactory = LoggerFactory.getCurrentLoggerFactory(config.getLoggerKey());
			ConnectionPoint.getInstance().addListener(
					(IConnectionListener) loggerFactory.newInstance(ConnectionLogger.class));
			ExceptionPoint.getInstance().addListener(
					(IExceptionListener) loggerFactory.newInstance(ExceptionLogger.class));
			RuntimePoint.getInstance().addListener((IRuntimeListener) loggerFactory.newInstance(RuntimeLogger.class));
			LoggerPoint.getInstance().setLevel(Level.DEBUG);
			PerformancePoint.getInstance().addListener(
					(IPerformanceListener) loggerFactory.newInstance(PerformanceLogger.class));
			PerformancePoint.getInstance().setOn(true);
			SessionPoint.getInstance().addListener((ISessionListener) loggerFactory.newInstance(SessionLogger.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteLog() {
		File logDir = new File("log");

		for (File file : logDir.listFiles()) {
			if (file.isFile()) {
				if (file.getAbsolutePath().endsWith(".log")) {
					file.delete();
				}
			}
		}
	}
}
