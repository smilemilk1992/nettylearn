package com.learn.netty5;


import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author haochen
 * @date 2019/6/4 17:47
 */
public class NettyHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger LOG = Logger.getLogger(NettyHttpHandler.class);
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) throws Exception {
        if (request instanceof HttpRequest) {
            if(request.uri().contains("favicon.ico")){
                return;
            }
            Map<String, String> parameters=packRequest(request);
            parameters.put("uri", request.uri());
            parameters.put("HttpMethod", String.valueOf(request.method().name()));
            InetSocketAddress inetSocketAddress= (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
            String RemoteAddress = inetSocketAddress.getAddress().getHostAddress();
            parameters.put("RemoteAddress", RemoteAddress);
            String res = null;
            try {
//                res = String.valueOf(callMethod("doGet",parameters));
                res=RequestHandler.doGet(parameters);
                LOG.info("handlerResult="+res+" "+Thread.currentThread().getName());
            }catch (Exception e){
                LOG.error("handlerResult="+res,e);
                res=e.toString();
            }

            send(channelHandlerContext,res,HttpResponseStatus.OK);
        }

    }

    /**
     * 回调方法
     * @param method
     * @param map
     * @return
     * @throws Exception
     */
    protected Object callMethod(String method, Map<String, String> map) throws Exception {
        Method[] handlerMethods = RequestHandler.class.getMethods();
        Map<String, Method> m = new HashMap<String, Method>();
        for (Method md : handlerMethods)
            m.put(md.getName(), md);
        Method mtd = m.get(method);
        if (mtd == null)
            throw new IOException("No such method");
        return mtd.invoke(new RequestHandler(), map);//反射调用
    }


    /**
     * 发送的返回值
     * @param ctx     返回
     * @param context 消息
     * @param status 状态
     */
    private void send(ChannelHandlerContext ctx, String context,HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(context, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


    /**
     * 解析get请求
     * @param request
     * @return
     */
    public Map<String, String> parseQueryString(HttpRequest request) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        return traversalDecoder(decoder, null);
    }

    /**
     *
     * @param decoder
     * @param parameters
     * @return
     */
    private Map<String, String> traversalDecoder(QueryStringDecoder decoder, Map<String, String> parameters){
        if (null == parameters) {
            parameters = new HashMap<String, String>();
        }
        Iterator<Map.Entry<String, List<String>>> iterator = decoder.parameters().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> entry = iterator.next();
            parameters.put(entry.getKey(), entry.getValue().get(0));
        }
        return parameters;
    }

    /**
     *
     * @param request 支持post和get请求
     * @return
     * @throws Exception
     */
    public Map<String, String> packRequest(HttpRequest request) throws Exception {
        if (request.method().equals(HttpMethod.GET)) {
            Map<String, String> map = this.parseQueryString(request);
            return map;
        } else if (request.method().equals(HttpMethod.POST)) {
            Map<String, String> map = new HashMap();
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
            List<InterfaceHttpData> list = decoder.getBodyHttpDatas();
            Iterator var5 = list.iterator();
            while(var5.hasNext()) {
                InterfaceHttpData postData = (InterfaceHttpData)var5.next();
                Attribute attribute = (Attribute)postData;
                String name = attribute.getName();
                String v = attribute.getValue();
                map.put(name, v);
            }
            return map;
        }else {
            throw new IOException("Not support http method except GET！");
        }
    }
}
