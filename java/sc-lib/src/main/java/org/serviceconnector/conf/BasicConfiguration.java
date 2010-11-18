/*
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
 */
package org.serviceconnector.conf;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.log4j.Logger;
import org.serviceconnector.Constants;

/**
 * root.writePID=true root.operationTimeoutMultiplier=0.8 root.echoIntervalMultiplier=1.2 root.connectionTimeoutMillis=10000
 * 
 * @author Daniel Schmutz
 */
public class BasicConfiguration {
	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(BasicConfiguration.class);
	/** The write pid. */
	private boolean writePID = true;
	/**
	 * Multiplier to calculate the operation timeout.<br>
	 * SC must adapt (shorten) the timeout passed from client to get the right timeout.
	 */
	private double operationTimeoutMultiplier = Constants.DEFAULT_OPERATION_TIMEOUT_MULTIPLIER;
	/**
	 * Multiplier to calculate the echo timeout of a session. <br>
	 * SC must adapt (extend) echo interval passed from client to get the right interval for echo messages.
	 */
	private double echoIntervalMultiplier = Constants.DEFAULT_ECHO_INTERVAL_MULTIPLIER;
	/** Timeout to prevent stocking in technical connect process. */
	private int connectionTimeoutMillis = Constants.DEFAULT_CONNECT_TIMEOUT_MILLIS;
	/** The subscription timeout. */
	private int subscriptionTimeout = Constants.DEFAULT_SUBSCRIPTION_TIMEOUT_MILLIS;
	/** The command validation. */
	private boolean commandValidation = Constants.COMMAND_VALIDATION_ENABLED;
	/**
	 * Used to observe the reply of a keep alive message. <br>
	 * If the peer does not reply within this time, connection will be cleaned up.
	 */
	private int keepAliveTimeout = Constants.DEFAULT_KEEP_ALIVE_TIMEOUT;
	/**
	 * Used to observe the reply of a abort session. <br>
	 * If server does not reply within this time, the server will be cleaned up.
	 */
	private int srvAbortTimeout = Constants.DEFAULT_SERVER_ABORT_OTI_MILLIS;

	/**
	 * Instantiates a new basic configuration.
	 */
	public BasicConfiguration() {
	}

	/**
	 * Checks if is write pid.
	 * 
	 * @return true, if is write pid
	 */
	public boolean isWritePID() {
		return writePID;
	}

	/**
	 * Gets the operation timeout multiplier.
	 * 
	 * @return the operation timeout multiplier
	 */
	public double getOperationTimeoutMultiplier() {
		return operationTimeoutMultiplier;
	}

	/**
	 * Gets the echo interval multiplier.
	 * 
	 * @return the echo interval multiplier
	 */
	public double getEchoIntervalMultiplier() {
		return echoIntervalMultiplier;
	}

	/**
	 * Gets the connection timeout millis.
	 * 
	 * @return the connection timeout millis
	 */
	public int getConnectionTimeoutMillis() {
		return connectionTimeoutMillis;
	}

	/**
	 * Gets the subscription timeout.
	 * 
	 * @return the subscription timeout
	 */
	public int getSubscriptionTimeout() {
		return subscriptionTimeout;
	}

	/**
	 * Checks if is command validation.
	 * 
	 * @return true, if is command validation
	 */
	public boolean isCommandValidation() {
		return commandValidation;
	}

	/**
	 * Gets the keep alive timeout.
	 * 
	 * @return the keep alive timeout
	 */
	public int getKeepAliveTimeout() {
		return keepAliveTimeout;
	}

	/**
	 * Gets the srv abort timeout.
	 * 
	 * @return the srv abort timeout
	 */
	public int getSrvAbortTimeout() {
		return srvAbortTimeout;
	}

	/**
	 * inits the configuration.
	 * 
	 * @param compositeConfiguration
	 *            the composite configuration
	 */
	public void init(CompositeConfiguration compositeConfiguration) {
		try {
			this.writePID = compositeConfiguration.getBoolean(Constants.ROOT_WRITEPID);
		} catch (Exception e) {
			logger.info(e.toString());
		}
		logger.info("basic configuration: writePID is " + this.writePID);
		try {
			this.operationTimeoutMultiplier = compositeConfiguration.getDouble(Constants.ROOT_OPERATION_TIMEOUT_MULTIPLIER);
		} catch (Exception e) {
			logger.info(e.toString());
		}
		logger.info("basic configuration: operationTimeoutMultiplier is " + this.operationTimeoutMultiplier);
		try {
			this.echoIntervalMultiplier = compositeConfiguration.getDouble(Constants.ROOT_ECHO_INTERVAL_MULTIPLIER);
		} catch (Exception e) {
			logger.info(e.toString());
		}
		logger.info("basic configuration: echoIntervalMultiplier is " + this.echoIntervalMultiplier);
		try {
			this.connectionTimeoutMillis = compositeConfiguration.getInt(Constants.ROOT_CONNECTION_TIMEOUT);
		} catch (Exception e) {
			logger.info(e.toString());
		}
		logger.info("basic configuration: connectionTimeoutMillis is " + this.connectionTimeoutMillis);
		try {
			this.subscriptionTimeout = compositeConfiguration.getInt(Constants.ROOT_SUBSCRIPTION_TIMEOUT);
		} catch (Exception e) {
			logger.info(e.toString());
		}
		logger.info("basic configuration: subscriptionTimeout is " + this.subscriptionTimeout);
		try {
			this.commandValidation = compositeConfiguration.getBoolean(Constants.ROOT_COMMAND_VALIDATION_ENABLED);
		} catch (Exception e) {
			logger.info(e.toString());
		}
		logger.info("basic configuration: commandValidation is " + this.commandValidation);
		try {
			this.keepAliveTimeout = compositeConfiguration.getInt(Constants.ROOT_KEEP_ALIVE_TIMEOUT);
		} catch (Exception e) {
			logger.info(e.toString());
		}
		logger.info("basic configuration: keepAliveTimeout is " + this.keepAliveTimeout);
		try {
			this.srvAbortTimeout = compositeConfiguration.getInt(Constants.ROOT_COMMAND_VALIDATION_ENABLED);
		} catch (Exception e) {
			logger.info(e.toString());
		}
		logger.info("basic configuration: srvAbortTimeout is " + this.srvAbortTimeout);
	}
}