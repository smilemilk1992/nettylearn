package com.learn.netty3;


import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.MemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;

import static org.jboss.netty.channel.Channels.pipeline;

/**
 * 
 * 类HttpServerPipelineFactory.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 
 */
public class HttpServerPipelineFactory implements ChannelPipelineFactory {
    private final HttpServerHandler handler;

    @SuppressWarnings("unused")
    private final boolean isStream;
    private final int corePoolSize;
    private final ExecutionHandler executionHandler;
    private final long maxMemorySize=10*1048576;
    
    public HttpServerPipelineFactory(Object userHandler, boolean isStream, int corePoolSize) {
        this.handler = new HttpServerHandler(userHandler);
        this.isStream = isStream;
        this.corePoolSize=corePoolSize;
//        this.executionHandler = new ExecutionHandler(new MemoryAwareThreadPoolExecutor(corePoolSize>0?corePoolSize:1, maxMemorySize, maxMemorySize));
        this.executionHandler = new ExecutionHandler(new MemoryAwareThreadPoolExecutor(corePoolSize>0?corePoolSize:1, maxMemorySize, maxMemorySize));
    }

    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast("idle",new IdleStateHandler(new HashedWheelTimer(),5,5,10));
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        if(this.corePoolSize > 0){
        	pipeline.addLast("executionHandler", executionHandler);
        }	
        pipeline.addLast("handler", handler);
        return pipeline;
    }
}
