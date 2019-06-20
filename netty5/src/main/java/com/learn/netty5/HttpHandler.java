package com.learn.netty5;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.io.UnsupportedEncodingException;

/**
 * @author haochen
 * @date 2019/6/4 17:47
 */
public class HttpHandler extends ChannelHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("server channel active... ");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {

        if (msg instanceof HttpRequest) {

            // 请求，解码器将请求转换成HttpRequest对象
            HttpRequest request = (HttpRequest) msg;

            // 获取请求参数
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
            String name = "netty";
            if(queryStringDecoder.parameters().get("name") != null) {
                name = queryStringDecoder.parameters().get("name").get(0);
            }

            JSONObject jsonObject=new JSONObject();
            jsonObject.put("name",name);
            String responseHtml = jsonObject.toString();

            // 创建http响应
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(responseHtml, CharsetUtil.UTF_8));
            // 设置头信息
            response.headers().set("Content-Type", "text/html; charset=UTF-8");


            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            ReferenceCountUtil.release(msg); //释放消息
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
