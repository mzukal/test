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
package com.stabilit.sc.cln.net.client.nio.tcp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.stabilit.sc.cln.client.IClientConnection;
import com.stabilit.sc.factory.IFactoryable;
import com.stabilit.sc.listener.ConnectionListenerSupport;
import com.stabilit.sc.net.EncoderDecoderFactory;
import com.stabilit.sc.net.FrameDecoderFactory;
import com.stabilit.sc.net.IEncoderDecoder;
import com.stabilit.sc.net.IFrameDecoder;
import com.stabilit.sc.scmp.SCMPMessage;
import com.stabilit.sc.scmp.SCMPError;
import com.stabilit.sc.srv.net.SCMPCommunicationException;

/**
 * The Class NioTcpClientConnection. Concrete client connection implementation on Nio base for Tcp.
 */
public class NioTcpClientConnection implements IClientConnection {

	/** The socket channel. */
	private SocketChannel socketChannel;
	/** The port. */
	private int port;
	/** The host. */
	private String host;
	/** The numberOfThreads. */
	private int numberOfThreads;
	/** The encoder decoder. */
	private IEncoderDecoder encoderDecoder;

	/**
	 * Instantiates a new nio tcp client connection.
	 */
	public NioTcpClientConnection() {
		this.socketChannel = null;
		this.port = 0;
		this.host = null;
		this.numberOfThreads = 10;
		this.encoderDecoder = null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.stabilit.sc.cln.client.IClientConnection#connect()
	 */
	@Override
	public void connect() throws Exception {
		socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(true);
		ConnectionListenerSupport.getInstance().fireDisconnect(this);
		socketChannel.connect(new InetSocketAddress(this.host, this.port));
	}

	/*
	 * (non-Javadoc)
	 * @see com.stabilit.sc.cln.client.IClientConnection#disconnect()
	 */
	@Override
	public void disconnect() throws Exception {
		ConnectionListenerSupport.getInstance().fireDisconnect(this);
		socketChannel.close();
	}

	/*
	 * (non-Javadoc)
	 * @see com.stabilit.sc.cln.client.IClientConnection#destroy()
	 */
	@Override
	public void destroy() {
	}

	/*
	 * (non-Javadoc)
	 * @see com.stabilit.sc.cln.client.IClientConnection#sendAndReceive(com.stabilit.sc.scmp.SCMP)
	 */
	@Override
	public SCMPMessage sendAndReceive(SCMPMessage scmp) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		encoderDecoder = EncoderDecoderFactory.getCurrentEncoderDecoderFactory().newInstance(scmp);
		encoderDecoder.encode(baos, scmp);
		byte[] byteWriteBuffer = baos.toByteArray();
		ByteBuffer writeBuffer = ByteBuffer.wrap(byteWriteBuffer);
		ConnectionListenerSupport.getInstance().fireWrite(this, byteWriteBuffer); // logs inside if registered
		socketChannel.write(writeBuffer);
		// read response
		ByteBuffer byteBuffer = ByteBuffer.allocate(1 << 12); // 8kb buffer
		int bytesRead = 0;
		try {
			bytesRead = socketChannel.read(byteBuffer);
		} catch (Throwable ex) {
			throw new SCMPCommunicationException(SCMPError.CONNECTION_LOST);
		}
		if (bytesRead < 0) {
			throw new SCMPCommunicationException(SCMPError.CONNECTION_LOST);
		}
		// parse headline
		IFrameDecoder scmpFrameDecoder = FrameDecoderFactory.getDefaultFrameDecoder();
		byte[] byteReadBuffer = byteBuffer.array();
		ConnectionListenerSupport.getInstance().fireRead(this, byteReadBuffer, 0, bytesRead);

		int scmpLengthHeadlineInc = scmpFrameDecoder.parseFrameSize(byteReadBuffer);
		baos = new ByteArrayOutputStream();
		baos.write(byteBuffer.array(), 0, bytesRead);
		// continues reading until http frame is complete
		while (scmpLengthHeadlineInc > bytesRead) {
			byteBuffer.clear();
			int read = 0;
			try {
				read = socketChannel.read(byteBuffer);
			} catch (Throwable ex) {
				throw new SCMPCommunicationException(SCMPError.CONNECTION_LOST);
			}
			if (read < 0) {
				throw new SCMPCommunicationException(SCMPError.CONNECTION_LOST);
			}
			bytesRead += read;
			baos.write(byteBuffer.array(), 0, read);
		}
		baos.close();
		byte[] buffer = baos.toByteArray();
		encoderDecoder = EncoderDecoderFactory.getCurrentEncoderDecoderFactory().newInstance(buffer);
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		SCMPMessage ret = (SCMPMessage) encoderDecoder.decode(bais);
		bais.close();
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.stabilit.sc.factory.IFactoryable#newInstance()
	 */
	@Override
	public IFactoryable newInstance() {
		return new NioTcpClientConnection();
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
	 * @see com.stabilit.sc.net.IConnection#setNumberOfThreads(int)
	 */
	public void setNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}

	/*
	 * (non-Javadoc)
	 * @see com.stabilit.sc.net.IConnection#setHost(java.lang.String)
	 */
	@Override
	public void setHost(String host) {
		this.host = host;
	}
}
