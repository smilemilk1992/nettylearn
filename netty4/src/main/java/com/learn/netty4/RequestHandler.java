package com.learn.netty4;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * @author haochen
 * @date 2019/6/13 10:22
 */
public class RequestHandler {
    private static final Logger LOG = Logger.getLogger(RequestHandler.class);

    public static String doGet(Map<String, String> map) {
        try {
            return getData(map);
        } catch (Exception e) {
            LOG.error("", e);
            return  "{\"status\":500,\"msg\":\"server has encountered an error\"}";
        }
    }

    /**
     * GET请求 POST请求都支持
     * @param map
     * @return
     */
    private static String getData(Map<String, String> map) {
        String result="";
        String uri=map.get("uri");
        if(uri.startsWith("/oidNidInfo") && map.containsKey("oid")){
            result = JSONObject.toJSONString(map);
        }else {
            result="接口路径错误";
        }
        return result;
    }


}
