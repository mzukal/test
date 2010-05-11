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
package com.stabilit.sc.cln.net.client.netty.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.stabilit.sc.cln.client.IClientConnection;
import com.stabilit.sc.cln.net.client.netty.NettyOperationListener;
import com.stabilit.sc.config.IConstants;
import com.stabilit.sc.factory.IFactoryable;
import com.stabilit.sc.listener.ConnectionListenerSupport;
import com.stabilit.sc.net.EncoderDecoderFactory;
import com.stabilit.sc.net.IEncoderDecoder;
import com.stabilit.sc.scmp.SCMP;

/**
 * The Class NettyHttpClientConnection. Concrete client connection implementation with JBoss Netty for Http.
 * 
 * @author JTraber
 */
public class NettyHttpClientConnection implements IClientConnection {

	/** The url. */
	private URL url;
	/** The bootstrap. */
	private ClientBootstrap bootstrap;
	/** The channel. */
	private Channel channel;
	/** The port. */
	private int port;
	/** The host. */
	private String host;
	/** The numberOfThreads. */
	private int numberOfThreads;
	/** The operation listener. */
	private NettyOperationListener operationListener;
	/** The channel factory. */
	private NioClientSocketChannelFactory channelFactory;
	/** The encoder decoder. */
	private IEncoderDecoder encoderDecoder;

	/**
	 * Instantiates a new netty http client connection.
	 */
	public NettyHttpClientConnection() {
		this.url = null;
		this.bootstrap = null;
		this.channel = null;
		this.port = 0;
		this.numberOfThreads = 10;
		this.host = null;
		this.operationListener = null;
		this.channelFactory = null;
		this.encoderDecoder = null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.stabilit.sc.cln.client.IClientConnection#connect()
	 */
	@Override
	public void connect() throws Exception {
		/*
		 * Configures client with Thread Pool, Boss Threads and Worker Threads. A boss thread accepts incoming
		 * connections on a socket. A worker thread performs non-blocking read and write on a channel.
		 */
		channelFactory = new NioClientSocketChannelFactory(Executors.newFixedThreadPool(numberOfThreads),
				Executors.newFixedThreadPool(numberOfThreads / 4));
		this.bootstrap = new ClientBootstrap(channelFactory);
		this.bootstrap.setPipelineFactory(new NettyHttpClientPipelineFactory());
		// Starts the connection attempt.
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
		// waits until operation is done
		operationListener = new NettyOperationListener();
		future.addListener(operationListener);
		this.channel = operationListener.awaitUninterruptibly().getChannel();
		ConnectionListenerSupport.getInstance().fireConnect(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.stabilit.sc.cln.client.IClientConnection#disconnect()
	 */
	@Override
	public void disconnect() throws Exception {
		ChannelFuture future = this.channel.disconnect();
		future.addListener(operationListener);
		operationListener.awaitUninterruptibly();
		ConnectionListenerSupport.getInstance().fireDisconnect(this);
		this.bootstrap.releaseExternalResources();
	}

	/*
	 * (non-Javadoc)
	 * @see com.stabilit.sc.cln.client.IClientConnection#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		ChannelFuture future = this.channel.close();
		future.addListener(operationListener);
		operationListener.awaitUninterruptibly();
		this.bootstrap.releaseExternalResources();
	}

	/*
	 * (non-Javadoc)
	 * @see com.stabilit.sc.cln.client.IClientConnection#sendAndReceive(com.stabilit .sc.scmp.SCMP)
	 */
	@Override
	public SCMP sendAndReceive(SCMP scmp) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		encoderDecoder = EncoderDecoderFactory.getCurrentEncoderDecoderFactory().newInstance(scmp);
		encoderDecoder.encode(baos, scmp);
		url = new URL(IConstants.HTTP, host, port, IConstants.HTTP_FILE);
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, this.url.getPath());
		byte[] buffer = baos.toByteArray();
		// Http header fields
		// TODO complete the header fields
		request.addHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buffer.length));

		ChannelBuffer channelBuffer = ChannelBuffers.copiedBuffer(buffer);
		request.setContent(channelBuffer);
		ChannelFuture future = channel.write(request);
		future.addListener(operationListener);
		operationListener.awaitUninterruptibly();

		ConnectionListenerSupport.getInstance().fireWrite(this, buffer); // logs inside if registered
		// gets response message synchronous
		NettyHttpClientResponseHandler handler = channel.getPipeline().get(NettyHttpClientResponseHandler.class);
		ChannelBuffer content = handler.getMessageSync().getContent();
		buffer = new byte[content.readableBytes()];
		content.readBytes(buffer);
		ConnectionListenerSupport.getInstance().fireRead(this, buffer);// logs inside if registered
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		encoderDecoder = EncoderDecoderFactory.getCurrentEncoderDecoderFactory().newInstance(buffer);
		SCMP ret = (SCMP) encoderDecoder.decode(bais);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.stabilit.sc.net.IConnection#setPort(int)
	 */
	@Override
	public void setPort(int port) {
		this.port = port;
	}

	/*
	 * (non-Javadoc)
	 * @see com.stabilit.sc.net.IConnection#setHost(java.lang.String)
	 */
	@Override
	public void setHost(String host) {
		this.host = host;
	}

	/*
	 * (non-Javadoc)
	 * @see com.stabilit.sc.net.IConnection#setNumberOfThreads(int)
	 */
	public void setNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}

	/*
	 * (non-Javadoc)
	 * @see com.stabilit.sc.factory.IFactoryable#newInstance()
	 */
	@Override
	public IFactoryable newInstance() {
		return new NettyHttpClientConnection();
	}
}
