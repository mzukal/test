/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.stabilit.sc.app.client.jboss.netty.http;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;

import com.stabilit.sc.app.client.IClient;


/**
 * @author The Netty Project (netty-dev@lists.jboss.org)
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author Trustin Lee (trustin@gmail.com)
 *
 * @version $Rev: 1858 $, $Date: 2009-11-02 10:21:13 +0100 (Mo, 02 Nov 2009) $
 */
public class HttpClientPipelineFactory implements ChannelPipelineFactory {
	
	private NettyHttpClient client; 
	public HttpClientPipelineFactory(NettyHttpClient client) {
		this.client = client;
	}
	
    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("decoder", new HttpResponseDecoder());
        // Remove the following line if you don't want automatic content decompression.
//        pipeline.addLast("inflater", new HttpContentDecompressor());
        // Uncomment the following line if you don't want to handle HttpChunks.
        //pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
        pipeline.addLast("encoder", new HttpRequestEncoder());
        pipeline.addLast("handler", new HttpResponseHandler(this.client));
        return pipeline;
    }
}
