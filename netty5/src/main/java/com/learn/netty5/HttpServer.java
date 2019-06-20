package com.learn.netty5;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;

/**
 * @author haochen
 * @date 2019/6/4 17:47
 */
public class HttpServer{
    private final int port;

//    private static final DefaultEventExecutorGroup e1 =  new DefaultEventExecutorGroup(128);
    public HttpServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port=9526;
        System.out.println("netty server start..........");
        new HttpServer(port).start1();
    }

    /**
     * netty5写法
     * @throws Exception
     */
    public void start1() throws Exception {
        // 1 创建线两个事件循环组
        // 一个是用于处理服务器端接收客户端连接的
        // 一个是进行网络通信的（网络读写的）
        //‘worker’是用来处理那些已经完成‘3次握手’的连接，而‘boss’是处理那些尚未完成的连接。boss 的EventloopGroup 做的主要事情是接受请求，然后把请求分发给worker 的EventloopGroup， 让worker 的EventloopGroup 去。
        //EventLoopGroup 说白了，就是一个死循环，不停地检测IO事件，处理IO事件，执行任务

        //我们不应该在NioEventLoop里执行耗时的操作（比如数据库操作），这样会卡死NioEventLoop，降低程序的响应性。
        // netty5创建的并不是连接池，而是用本身封装好的方法，其实现是ScheduledExecutor连接池
        EventLoopGroup bossgroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(128);
        try {
            // 2 创建辅助工具类ServerBootstrap，用于服务器通道的一系列配置
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossgroup,workerGroup)
                    //option()是提供给NioServerSocketChannel用来接收进来的连接。childOption()是提供给由父管道ServerChannel接收到的连接
                    //因为处理器是有顺序的。对于进站事件来说，先添加的先执行。 对于出站事件来说，后添加的先执行。
                    .channel(NioServerSocketChannel.class)  //指定使用Nio模式。
                    .option(ChannelOption.SO_BACKLOG, 2048)// 设置TCP缓冲区,//设置服务器最大连接数
                    .option(ChannelOption.SO_SNDBUF, 32 * 1024) // 设置发送缓冲大小
                    .option(ChannelOption.SO_RCVBUF, 32 * 1024) // 这是接收缓冲大小
                    .option(ChannelOption.TCP_NODELAY,true) // //设置tcp延迟状态
                    .childOption(ChannelOption.SO_KEEPALIVE, true)// 保持连接,/设置激活状态，2小时清除
                    .handler(new LoggingHandler(LogLevel.INFO))//设置日志
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(5,5,10));
//                            pipeline.addLast(new HttpServerCodec()); //即 HttpRequestDecoder 和 HttpResponseEncoder 的结合。
                            pipeline.addLast( new HttpResponseEncoder());//【编码】服务端往客户端发送数据的行为是Response，所以这边要使用HttpResponseEncoder将数据进行编码操作
                            pipeline.addLast( new HttpRequestDecoder());//【解码】服务端接收到数据的行为是Request，所以要使用HttpRequestDecoder进行解码操作
                            pipeline.addLast(new HttpObjectAggregator(512 * 1024*1024));//聚合的消息内容长度不超过512kb。把单个http请求转为FullHttpReuest或FullHttpResponse
                            pipeline.addLast(new HttpContentCompressor()); //HTTP压缩
                            //超时handler（当服务器端与客户端在指定时间以上没有任何进行通信，则会关闭通道）
//                            pipeline.addLast(new ReadTimeoutHandler(5));  // 时限, 读客户端超时没数据则断开
                            // 3 在这里配置 通信数据的处理逻辑, 可以addLast多个...
                            pipeline.addLast(new NettyHttpHandler()); //自定义的数据处理类,主要处理业务逻辑,服务端业务逻辑
                        }
                    });
            // 4 绑定端口, bind返回future(异步), 加上sync阻塞在获取连接处
            ChannelFuture f = b.bind(new InetSocketAddress(port)).sync();//绑定端口
            if(f.isSuccess()){
                System.out.println("启动 Netty 服务端成功！");
            }
            // 5 等待关闭, 加上sync阻塞在关闭请求处
            f.channel().closeFuture().sync();

        }finally {
            System.out.println("=========================close");
            //关闭两组死循环
            workerGroup.shutdownGracefully();
            bossgroup.shutdownGracefully();

        }
    }
}
