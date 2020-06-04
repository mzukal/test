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
package org.serviceconnector.net.req.netty.http;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.URL;

import org.serviceconnector.Constants;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.log.ConnectionLogger;
import org.serviceconnector.net.CommunicationException;
import org.serviceconnector.net.SCMPCommunicationException;
import org.serviceconnector.net.req.netty.NettyConnectionAdpater;
import org.serviceconnector.net.req.netty.NettyOperationListener;
import org.serviceconnector.scmp.ISCMPMessageCallback;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.Timer;

/**
 * The Class NettyHttpClientConnection. Concrete connection implementation with
 * JBoss Netty for Http.
 *
 * @author JTraber
 */
public class NettyHttpConnection extends NettyConnectionAdpater {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyHttpConnection.class);

	/** The url. */
	private URL url;

	/**
	 * Instantiates a new netty http connection.
	 *
	 * @param channelFactory the channel factory
	 * @param timer          the timer
	 */
	public NettyHttpConnection(EventLoopGroup workerGroup, Timer timer) {
		super(workerGroup, timer);
		this.url = null;
	}

	/** {@inheritDoc} */
	@Override
	public void connect() throws Exception {
		this.bootstrap = new Bootstrap();
		this.bootstrap.group(NettyConnectionAdpater.workerGroup);
		this.bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, baseConf.getConnectionTimeoutMillis());
		this.bootstrap.option(ChannelOption.TCP_NODELAY, true);
		this.bootstrap.channel(NioSocketChannel.class);

		if (baseConf.getTcpKeepAliveInitiator() != null) {
			// TCP keep alive is configured - set it!
			this.bootstrap.option(ChannelOption.SO_KEEPALIVE, baseConf.getTcpKeepAliveInitiator());
		}
		this.bootstrap.handler(new NettyHttpRequesterPipelineFactory(this.connectionContext));
		// Starts the connection attempt.
		this.remotSocketAddress = new InetSocketAddress(host, port);
		ChannelFuture future = bootstrap.connect(this.remotSocketAddress);
		this.operationListener = new NettyOperationListener();
		future.addListener(this.operationListener);
		try {
			// waits until operation is done
			this.channel = future.channel();
			this.operationListener.awaitUninterruptibly(baseConf.getConnectionTimeoutMillis()).channel();
			// complete localSocketAdress
			this.remotSocketAddress = (InetSocketAddress) this.channel.remoteAddress();
		} catch (CommunicationException ex) {
			LOGGER.error("connect", ex);
			throw new SCMPCommunicationException(SCMPError.CONNECTION_EXCEPTION,
					"connect to IP=" + this.remotSocketAddress.toString());
		}
		if (ConnectionLogger.isEnabled()) {
			ConnectionLogger.logConnect(this.getClass().getSimpleName(), this.remotSocketAddress.getHostName(),
					this.remotSocketAddress.getPort());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void send(SCMPMessage scmp, ISCMPMessageCallback callback) throws Exception {
		// LOGGER.info("send cache id = " + scmp.getCacheId());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		encoderDecoder = AppContext.getEncoderDecoderFactory().createEncoderDecoder(scmp);
		encoderDecoder.encode(baos, scmp);
		url = new URL(Constants.HTTP, host, port, scmp.getHttpUrlFileQualifier());

		byte[] buffer = baos.toByteArray();
		ByteBuf channelBuffer = Unpooled.copiedBuffer(buffer);
		HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, this.url.getPath(),
				channelBuffer);
		// Http header fields
		request.headers().add(HttpHeaderNames.USER_AGENT, System.getProperty("java.runtime.version"));
		request.headers().add(HttpHeaderNames.HOST, host);
		request.headers().add(HttpHeaderNames.ACCEPT, Constants.HTTP_ACCEPT_PARAMS);
		request.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		request.headers().add(HttpHeaderNames.CONTENT_TYPE, scmp.getBodyType().getMimeType());
		request.headers().add(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(buffer.length));
		request.headers().add(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.NO_CACHE);
		request.headers().add(HttpHeaderNames.PRAGMA, HttpHeaderValues.NO_CACHE);

		NettyHttpRequesterResponseHandler handler = channel.pipeline().get(NettyHttpRequesterResponseHandler.class);
		handler.setCallback(callback);
		if (ConnectionLogger.isEnabledFull()) {
			ConnectionLogger.logWriteBuffer(this.getClass().getSimpleName(), this.remotSocketAddress.getHostName(),
					this.remotSocketAddress.getPort(), buffer, 0, buffer.length);
		}
		channel.writeAndFlush(request);
		return;

	}

	@Override
	public void setQuietDisconnect() throws Exception {
		// this avoids receiving messages (outstanding replies) in disconnecting procedure
		ChannelPipeline pipeline = channel.pipeline();

		// following null checks are necessary. In case the connection is already destroyed everything is nulled under the hood
		if (pipeline != null) {
			NettyHttpRequesterResponseHandler handler = channel.pipeline().get(NettyHttpRequesterResponseHandler.class);

			if (handler != null) {
				handler.connectionDisconnect();
			}
		}
	}
}
