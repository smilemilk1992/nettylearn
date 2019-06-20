package com.learn.netty3;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author haochen
 * @date 2019/6/10 17:31
 */
public class NettyRun {
    public static void main(String[] args) throws IOException {
        int port = 9528;
        int corePoolSize=5000;
        System.out.println("netty server is started! port is " + port+" corePoolSize is "+corePoolSize);
        OidDataRequestHandler handler=new OidDataRequestHandler();
        final HttpServer server = new HttpServer(new InetSocketAddress(port),handler,corePoolSize);
        server.start();
    }
}
