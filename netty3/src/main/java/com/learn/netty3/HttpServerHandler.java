package com.learn.netty3;


import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.codec.http.multipart.Attribute;
import org.jboss.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.jboss.netty.util.CharsetUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

//import com.sohu.mrd.topic.cache.TopicWordsCacheProvider;


/**
 * 
 * 类HttpServerHandler.java的实现描述：TODO 类实现描述 
 * @author hongfengwang
 */
public class HttpServerHandler extends SimpleChannelUpstreamHandler {

    private static final Logger LOG = Logger.getLogger(HttpServerHandler.class);
    protected final Object handler;
    protected final String CONTENT_TYPE_V;
    protected Map<String, Method> methodMap;
    

    public HttpServerHandler(Object handler) {
        super();
        this.handler = handler;
        this.CONTENT_TYPE_V = "text/plain; charset=UTF-8";
        Method[] handlerMethods = handler.getClass().getMethods();
        Map<String, Method> m = new HashMap<String, Method>();
        for (Method method : handlerMethods)
            m.put(method.getName(), method);
        this.methodMap = Collections.unmodifiableMap(m);
    }

    public HttpServerHandler(Object handler, String CONTENT_TYPE) {
        super();
        this.handler = handler;
        this.CONTENT_TYPE_V = CONTENT_TYPE;
        Method[] handlerMethods = handler.getClass().getMethods();
        Map<String, Method> m = new HashMap<String, Method>();
        for (Method method : handlerMethods)
            m.put(method.getName(), method);
        this.methodMap = Collections.unmodifiableMap(m);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent ev) {
        try {
            ev.getChannel().close();
        } catch (Exception e) {
        	LOG.error(e.toString());
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        HttpRequest request = (HttpRequest) e.getMessage();
        if (request == null) return;
        if(request.getUri().contains("favicon.ico")) return;
        Map<String,String> map = packRequest(request);  //判断是否是get post
        String uri=request.getUri();
        map.put("uri", uri);
        map.put("RemoteAddress", e.getRemoteAddress().toString());
        map.put("HttpMethod", request.getMethod().getName());
        processOneMessage(e, request, map);
    }

    protected void processOneMessage(MessageEvent e, HttpRequest request, Map<String, String> map) throws Exception {
        // Decide whether to close the connection or not.
        boolean keepAlive = isKeepAlive(request);

        Object handlerResult = null;
        try {
            handlerResult = callMethod("doGet", map);
        } catch (Exception e1) {
        	LOG.error("handlerResult="+handlerResult,e1);
        	handlerResult=e1.toString();
        }


        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        response.setContent(ChannelBuffers.copiedBuffer((String) handlerResult, CharsetUtil.getEncoder(Charset.forName("utf-8")).charset()));
        response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");//oyq:for access by ajax
        
        if (keepAlive) {
            response.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
            response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
        }
        ChannelFuture future = e.getChannel().write(response);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }


    protected Object callMethod(String method, Map<String, String> map) throws Exception {
        Method m = methodMap.get(method);
        if (m == null)
            throw new IOException("No such method");
        return m.invoke(handler, map);//反射调用
    }


    /**
     * 解析get请求
     * @param request
     * @return
     */
    public Map<String, String> parseQueryString(HttpRequest request) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
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
        Iterator<Map.Entry<String, List<String>>> iterator = decoder.getParameters().entrySet().iterator();
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
        if (request.getMethod().equals(HttpMethod.GET)) {
            Map<String, String> map = this.parseQueryString(request);
            return map;
        } else if (!request.getMethod().equals(HttpMethod.POST)) {
            throw new IOException("Not support http method except GET！");
        } else {
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
        }
    }
}
