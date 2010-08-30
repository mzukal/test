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
package com.stabilit.scm.common.net.req.netty.tcp;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.util.Timer;

import com.stabilit.scm.common.conf.Constants;
import com.stabilit.scm.common.listener.ConnectionPoint;
import com.stabilit.scm.common.log.IConnectionLogger;
import com.stabilit.scm.common.log.IExceptionLogger;
import com.stabilit.scm.common.log.Loggers;
import com.stabilit.scm.common.log.impl.ConnectionLogger;
import com.stabilit.scm.common.log.impl.ExceptionLogger;
import com.stabilit.scm.common.net.CommunicationException;
import com.stabilit.scm.common.net.EncoderDecoderFactory;
import com.stabilit.scm.common.net.IEncoderDecoder;
import com.stabilit.scm.common.net.SCMPCommunicationException;
import com.stabilit.scm.common.net.req.IConnection;
import com.stabilit.scm.common.net.req.IConnectionContext;
import com.stabilit.scm.common.net.req.netty.NettyOperationListener;
import com.stabilit.scm.common.scmp.ISCMPCallback;
import com.stabilit.scm.common.scmp.SCMPError;
import com.stabilit.scm.common.scmp.SCMPMessage;

/**
 * The Class NettyTcpConnection. Concrete connection implementation with JBoss Netty for Tcp.
 */
public class NettyTcpConnection implements IConnection {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(NettyTcpConnection.class);

	/** The Constant connectionLogger. */
	protected final static Logger connectionLogger = Logger.getLogger(Loggers.CONNECTION.getValue());
	
	/** The bootstrap. */
	private ClientBootstrap bootstrap;
	/** The channel. */
	private Channel channel;
	/** The port. */
	private int port;
	/** The host. */
	private String host;
	/** The operation listener. */
	private NettyOperationListener operationListener;
	/** The encoder decoder. */
	private IEncoderDecoder encoderDecoder;
	/** The local socket address. */
	private InetSocketAddress localSocketAddress;
	/** The channel pipeline factory. */
	private ChannelPipelineFactory pipelineFactory;
	/** The connection context. */
	private IConnectionContext connectionContext;
	/** state of connection. */
	private boolean isConnected;
	/** The idle timeout. */
	protected int idleTimeout;
	/** The number of idles, counts idle states. */
	private int nrOfIdles;
	/** The timer to observe timeouts, static because should be shared. */
	private static Timer timer;
	/*
	 * The channel factory. Configures client with Thread Pool, Boss Threads and Worker Threads. A boss thread accepts
	 * incoming connections on a socket. A worker thread performs non-blocking read and write on a channel.
	 */
	private static NioClientSocketChannelFactory channelFactory;

	/**
	 * Instantiates a new NettyTcpConnection.
	 */
	public NettyTcpConnection() {
		this.bootstrap = null;
		this.channel = null;
		this.port = 0;
		this.host = null;
		this.operationListener = null;
		this.encoderDecoder = null;
		this.localSocketAddress = null;
		this.isConnected = false;
		this.pipelineFactory = null;
		this.connectionContext = null;
	}

	/**
	 * Instantiates a new NettyTcpConnection.
	 */
	public NettyTcpConnection(NioClientSocketChannelFactory channelFactory, Timer timer) {
		this();
		NettyTcpConnection.channelFactory = channelFactory;
		NettyTcpConnection.timer = timer;
	}

	/** {@inheritDoc} */
	@Override
	public IConnectionContext getContext() {
		return this.connectionContext;
	}

	/** {@inheritDoc} */
	@Override
	public void setContext(IConnectionContext connectionContext) {
		this.connectionContext = connectionContext;
	}

	/** {@inheritDoc} */
	@Override
	public void connect() throws Exception {
		this.bootstrap = new ClientBootstrap(NettyTcpConnection.channelFactory);
		this.pipelineFactory = new NettyTcpRequesterPipelineFactory(this.connectionContext, NettyTcpConnection.timer);
		this.bootstrap.setPipelineFactory(this.pipelineFactory);
		// Start the connection attempt.
		this.localSocketAddress = new InetSocketAddress(host, port);
		ChannelFuture future = bootstrap.connect(this.localSocketAddress);
		operationListener = new NettyOperationListener();
		future.addListener(operationListener);
		try {
			this.channel = operationListener.awaitUninterruptibly(Constants.TECH_LEVEL_OPERATION_TIMEOUT_MILLIS)
					.getChannel();
			// complete localSocketAdress
			this.localSocketAddress = (InetSocketAddress) this.channel.getLocalAddress();
		} catch (CommunicationException ex) {
			IExceptionLogger exceptionLogger = ExceptionLogger.getInstance();
			exceptionLogger.logErrorException(logger, this.getClass().getName(), "connect", ex);
			throw new SCMPCommunicationException(SCMPError.CONNECTION_EXCEPTION, "connect failed to "
					+ this.localSocketAddress.toString());
		}
		IConnectionLogger connectionLogger = ConnectionLogger.getInstance();
		connectionLogger.logConnect(this.getClass().getName(), this.localSocketAddress.getPort());
		this.isConnected = true;
	}

	/** {@inheritDoc} */
	@Override
	public void disconnect() throws Exception {
		ChannelFuture future = this.channel.disconnect();
		future.addListener(operationListener);
		try {
			operationListener.awaitUninterruptibly(Constants.TECH_LEVEL_OPERATION_TIMEOUT_MILLIS);
		} catch (CommunicationException ex) {
			IExceptionLogger exceptionLogger = ExceptionLogger.getInstance();
			exceptionLogger.logErrorException(logger, this.getClass().getName(), "disconnect", ex);
			throw new SCMPCommunicationException(SCMPError.CONNECTION_EXCEPTION, "disconnect failed from "
					+ this.localSocketAddress.toString());
		}
		IConnectionLogger connectionLogger = ConnectionLogger.getInstance();
		connectionLogger.logDisconnect(this.getClass().getName(), this.localSocketAddress.getPort());		
	}

	/** {@inheritDoc} */
	@Override
	public void destroy() {
		ChannelFuture future = this.channel.close();
		future.addListener(operationListener);
		try {
			operationListener.awaitUninterruptibly(Constants.TECH_LEVEL_OPERATION_TIMEOUT_MILLIS);
		} catch (Exception ex) {
			IExceptionLogger exceptionLogger = ExceptionLogger.getInstance();
			exceptionLogger.logErrorException(logger, this.getClass().getName(), "destroy", ex);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void send(SCMPMessage scmp, ISCMPCallback callback) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		encoderDecoder = EncoderDecoderFactory.getCurrentEncoderDecoderFactory().newInstance(scmp);
		encoderDecoder.encode(baos, scmp);

		NettyTcpRequesterResponseHandler handler = channel.getPipeline().get(NettyTcpRequesterResponseHandler.class);
		handler.setCallback(callback);

		ChannelBuffer chBuffer = ChannelBuffers.buffer(baos.size());
		chBuffer.writeBytes(baos.toByteArray());
		ChannelFuture future = channel.write(chBuffer);
		future.addListener(operationListener);
		try {
			operationListener.awaitUninterruptibly(Constants.TECH_LEVEL_OPERATION_TIMEOUT_MILLIS);
		} catch (CommunicationException ex) {
			IExceptionLogger exceptionLogger = ExceptionLogger.getInstance();
			exceptionLogger.logErrorException(logger, this.getClass().getName(), "send", ex);
			throw new SCMPCommunicationException(SCMPError.CONNECTION_EXCEPTION, "send failed on "
					+ this.localSocketAddress);
		}
		IConnectionLogger connectionLogger = ConnectionLogger.getInstance();
		connectionLogger.logWrite(this.getClass().getName(), this.localSocketAddress.getPort(), 
				chBuffer.toByteBuffer().array(), 0, chBuffer.toByteBuffer().array().length);
		return;
	}

	/** {@inheritDoc} */
	@Override
	public IConnection newInstance() {
		return new NettyTcpConnection();
	}

	/** {@inheritDoc} */
	@Override
	public void setPort(int port) {
		this.port = port;
	}

	/** {@inheritDoc} */
	@Override
	public void setHost(String host) {
		this.host = host;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isConnected() {
		return this.isConnected;
	}

	@Override
	public void setIdleTimeout(int idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	@Override
	public int getNrOfIdlesInSequence() {
		return nrOfIdles;
	}

	@Override
	public void incrementNrOfIdles() {
		this.nrOfIdles++;
	}

	@Override
	public void resetNrOfIdles() {
		this.nrOfIdles = 0;
	}
}
