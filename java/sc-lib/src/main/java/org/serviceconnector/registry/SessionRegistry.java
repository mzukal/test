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
package org.serviceconnector.registry;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.serviceconnector.Constants;
import org.serviceconnector.log.SessionLogger;
import org.serviceconnector.server.Server;
import org.serviceconnector.service.Session;
import org.serviceconnector.util.ITimerRun;
import org.serviceconnector.util.TimerTaskWrapper;

/**
 * The Class SessionRegistry. Registry stores entries for properly created sessions. Registry is also responsible for observing the
 * session timeout and initiating clean up in case of a broken session.
 * 
 * @author JTraber
 */
public class SessionRegistry extends Registry<String, Session> {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(SessionRegistry.class);

	/** The Constant sessionLogger. */
	private final static SessionLogger sessionLogger = SessionLogger.getInstance();
	/** The timer. Timer instance is responsible to observe session timeouts. */
	private Timer timer;

	/**
	 * Instantiates a SessionRegistry.
	 */
	public SessionRegistry() {
		this.timer = new Timer("SessionRegistryTimer");
	}

	/**
	 * Adds the session.
	 * 
	 * @param key
	 *            the key
	 * @param session
	 *            the session
	 */
	public void addSession(String key, Session session) {
		sessionLogger.logCreateSession(this.getClass().getName(), session.getId());
		this.put(key, session);
		if (session.getSessionTimeoutSeconds() != 0) {
			// TODO TRN handle = session timeout necessary needs to be set up
			this.scheduleSessionTimeout(session);
		}
	}

	/**
	 * Removes the session.
	 * 
	 * @param session
	 *            the session
	 */
	public void removeSession(Session session) {
		this.removeSession(session.getId());
	}

	/**
	 * Removes the session.
	 * 
	 * @param key
	 *            the key
	 */
	public void removeSession(String key) {
		Session session = super.get(key);
		if (session == null) {
			return;
		}
		this.cancelSessionTimeout(session);
		super.remove(key);
		sessionLogger.logDeleteSession(this.getClass().getName(), session.getId());
	}

	/**
	 * Gets the session.
	 * 
	 * @param key
	 *            the key
	 * @return the session
	 */
	public Session getSession(String key) {
		return super.get(key);
	}

	/**
	 * Gets all sessions.
	 * 
	 * @return the sessions
	 */
	public Session[] getSessions() {
		try {
			Set<Entry<String, Session>> entries = this.registryMap.entrySet();
			Session[] sessions = new Session[entries.size()];
			int index = 0;
			for (Entry<String, Session> entry : entries) {
				String key = entry.getKey();
				Session session = entry.getValue();
				sessions[index++] = session;
			}
			return sessions;
		} catch (Exception e) {
			logger.error("getSessions", e);
		}
		return null;
	}

	/**
	 * Schedule session timeout.
	 * 
	 * @param session
	 *            the session
	 */
	public void scheduleSessionTimeout(Session session) {
		if (session == null || session.getSessionTimeoutSeconds() == 0) {
			// no scheduling of session timeout
			return;
		}
		// always cancel old timeouter before setting up a new one
		this.cancelSessionTimeout(session);
		TimerTaskWrapper sessionTimeouter = session.getSessionTimeouter();

		// sets up session timeout
		sessionTimeouter = new TimerTaskWrapper(new SessionTimerRun(session));
		session.setSessionTimeouter(sessionTimeouter);
		// schedule sessionTimeouter in registry timer
		this.timer.schedule(sessionTimeouter, (int) session.getSessionTimeoutSeconds() * Constants.SEC_TO_MILLISEC_FACTOR);
	}

	/**
	 * Cancel session timeout.
	 * 
	 * @param session
	 *            the session
	 */
	public void cancelSessionTimeout(Session session) {
		if (session == null) {
			return;
		}
		TimerTask sessionTimeouter = session.getSessionTimeouter();
		if (sessionTimeouter == null) {
			// no session timeout has been set up for this session
			return;
		}
		sessionTimeouter.cancel();
		// important to set timeouter null - rescheduling of same instance not possible
		session.setSessionTimeouter(null);
	}

	/**
	 * The Class SessionTimerRun. Gets control when a session times out. Responsible for cleaning up when session gets broken.
	 */
	private class SessionTimerRun implements ITimerRun {
		/** The session. */
		private Session session;
		/** The timeout. */
		private double timeoutSeconds;

		/**
		 * Instantiates a new session timer run.
		 * 
		 * @param session
		 *            the session
		 */
		public SessionTimerRun(Session session) {
			this.session = session;
			this.timeoutSeconds = session.getSessionTimeoutSeconds();
		}

		/**
		 * Timeout. Session timeout run out.
		 */
		@Override
		public void timeout() {
			/**
			 * broken session procedure<br>
			 * 1. remove session from session registry<br>
			 * 2. abort session on backend server<br>
			 */
			SessionRegistry.this.removeSession(session);
			Server server = session.getServer();
			// aborts session on server
			server.abortSession(session);
			// TODO for jan.. log session timeout
		}

		/** {@inheritDoc} */
		@Override
		public int getTimeoutMillis() {
			return (int) (this.timeoutSeconds * Constants.SEC_TO_MILLISEC_FACTOR);
		}
	}
}