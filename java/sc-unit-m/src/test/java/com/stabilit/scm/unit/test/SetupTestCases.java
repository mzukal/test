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
package com.stabilit.scm.unit.test;

import java.io.File;

import com.stabilit.scm.common.cmd.factory.CommandFactory;
import com.stabilit.scm.common.conf.ResponderConfigPool;
import com.stabilit.scm.common.listener.ConnectionPoint;
import com.stabilit.scm.common.listener.DefaultStatisticsListener;
import com.stabilit.scm.common.listener.ExceptionPoint;
import com.stabilit.scm.common.listener.IConnectionListener;
import com.stabilit.scm.common.listener.IExceptionListener;
import com.stabilit.scm.common.listener.ILoggerListener;
import com.stabilit.scm.common.listener.IPerformanceListener;
import com.stabilit.scm.common.listener.ISessionListener;
import com.stabilit.scm.common.listener.IStatisticsListener;
import com.stabilit.scm.common.listener.LoggerPoint;
import com.stabilit.scm.common.listener.PerformancePoint;
import com.stabilit.scm.common.listener.SessionPoint;
import com.stabilit.scm.common.listener.StatisticsPoint;
import com.stabilit.scm.common.log.Level;
import com.stabilit.scm.common.log.impl.ConnectionLogger;
import com.stabilit.scm.common.log.impl.ExceptionLogger;
import com.stabilit.scm.common.log.impl.LoggerFactory;
import com.stabilit.scm.common.log.impl.PerformanceLogger;
import com.stabilit.scm.common.log.impl.SessionLogger;
import com.stabilit.scm.common.log.impl.TopLogger;
import com.stabilit.scm.common.service.ISCMessage;
import com.stabilit.scm.sc.SC;
import com.stabilit.scm.srv.ISCServer;
import com.stabilit.scm.srv.ISCServerCallback;
import com.stabilit.scm.srv.SCServer;
import com.stabilit.scm.srv.ps.PublishServer;
import com.stabilit.scm.unit.UnitCommandFactory;

/**
 * @author JTraber
 */
public class SetupTestCases {

	private static SetupTestCases setupTestCases = null;
	public static IStatisticsListener statisticsListener = new DefaultStatisticsListener();

	private SetupTestCases() {
	}

	public static void init() throws Exception {
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
			LoggerPoint.getInstance().addListener((ILoggerListener) loggerFactory.newInstance(TopLogger.class));
			LoggerPoint.getInstance().setLevel(Level.DEBUG);
			PerformancePoint.getInstance().addListener(
					(IPerformanceListener) loggerFactory.newInstance(PerformanceLogger.class));
			PerformancePoint.getInstance().setOn(true);
			SessionPoint.getInstance().addListener((ISessionListener) loggerFactory.newInstance(SessionLogger.class));
			StatisticsPoint.getInstance().addListener(statisticsListener);
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

	public static void setupSCSessionServer() {
		if (setupTestCases == null) {
			try {
				init();
				setupTestCases = new SetupTestCases();
				CommandFactory.setCurrentCommandFactory(new UnitCommandFactory());
				SC.main(null);
				SetupTestCases.startSessionServer();
				// OldSessionServer.main(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void setupAll() {
		if (setupTestCases == null) {
			try {
				init();
				setupTestCases = new SetupTestCases();
				CommandFactory.setCurrentCommandFactory(new UnitCommandFactory());
				SC.main(null);
				SetupTestCases.startSessionServer();
				// OldSessionServer.main(null);
				PublishServer.main(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void setupSC() {
		if (setupTestCases == null) {
			try {
				init();
				setupTestCases = new SetupTestCases();
				CommandFactory.setCurrentCommandFactory(new UnitCommandFactory());
				SC.main(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void startSessionServer() throws Exception {
		ISCServer scSrv = new SCServer("localhost", 9000);

		// connect to SC as server
		scSrv.setMaxSessions(10);
		scSrv.setKeepAliveInterval(0);
		scSrv.setRunningPortNr(7000);
		scSrv.setImmediateConnect(true);
		scSrv.startServer("localhost");
		SessionServerCallback srvCallback = new SessionServerCallback();
		scSrv.registerService("simulation", srvCallback);
	}

	private static class SessionServerCallback implements ISCServerCallback {

		@Override
		public ISCMessage abortSession(ISCMessage message) {
			return message;
		}

		@Override
		public ISCMessage createSession(ISCMessage message) {
			return message;
		}

		@Override
		public ISCMessage deleteSession(ISCMessage message) {
			return message;
		}

		@Override
		public ISCMessage execute(ISCMessage message) {

			if (message.getData().toString().startsWith("large")) {
				StringBuilder sb = new StringBuilder();
				int i = 0;
				sb.append("large:");
				for (i = 0; i < 10000; i++) {
					if (sb.length() > 60000) {
						break;
					}
					sb.append(i);
				}
				if (i >= 10000) {
					message.setData(sb.toString());
				}
				return message;
			}
			message.setData("message data test case");
			return message;
		}
	}
}
