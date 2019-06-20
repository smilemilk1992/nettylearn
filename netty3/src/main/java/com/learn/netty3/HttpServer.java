package com.learn.netty3;


import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

/**
 * @author haochen
 * @date 2019/6/4 17:47
 */
public class HttpServer extends Server{
    protected final InetSocketAddress addr;
    protected final ServerBootstrap bootstrap;
    protected final NioServerSocketChannelFactory factory;
    protected Channel ch;
    private String addressIP;

    public HttpServer(InetSocketAddress addr, Object userHandler, int corePoolSize) {
        this.addr = addr;
        this.factory = new NioServerSocketChannelFactory(Executors
                .newCachedThreadPool(), Executors.newCachedThreadPool());
        bootstrap = new ServerBootstrap(factory);
        bootstrap.setOption("reuseAddress", true);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setPipelineFactory(new HttpServerPipelineFactory(userHandler, false,corePoolSize));
        try {
            this.addressIP=InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


    }

    public synchronized void start() throws IOException {
        ch = bootstrap.bind(addr);
    }

    public synchronized void stop() {

        if (ch != null)
            ch.close().awaitUninterruptibly();
        factory.releaseExternalResources();
    }
}
