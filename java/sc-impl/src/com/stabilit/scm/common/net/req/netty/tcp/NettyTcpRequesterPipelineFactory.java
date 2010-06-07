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

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.handler.timeout.WriteTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import com.stabilit.scm.common.net.res.netty.tcp.SCMPBasedFrameDecoder;
import com.stabilit.scm.config.IConstants;

/**
 * A factory for creating NettyTcpRequesterPipelineFactory objects.
 * 
 * @author JTraber
 */
public class NettyTcpRequesterPipelineFactory implements ChannelPipelineFactory {

	/** The timer to observe timeouts. */
	private Timer timer;

	/**
	 * Instantiates a new NettyTcpRequesterPipelineFactory.
	 */
	public NettyTcpRequesterPipelineFactory() {
		this.timer = new HashedWheelTimer();
	}

	/** {@inheritDoc} */
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		// responsible for reading until SCMP frame is complete
		pipeline.addLast("framer", new SCMPBasedFrameDecoder());
		// responsible for observing read timeout - Netty
		pipeline.addLast("readTimeout", new ReadTimeoutHandler(this.timer, IConstants.READ_TIMEOUT));
		// responsible for observing write timeout - Netty
		pipeline.addLast("writeTimeout", new WriteTimeoutHandler(this.timer, IConstants.WRITE_TIMEOUT));
		// responsible for handling response
		pipeline.addLast("handler", new NettyTcpRequesterResponseHandler());
		return pipeline;
	}
}
