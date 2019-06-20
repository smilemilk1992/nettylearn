/*
 * Copyright 2012 sohu.com All right reserved. This software is the
 * confidential and proprietary information of sohu.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with sohu.com.
 */
package com.learn.netty3;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;

import java.util.Map;


public class OidDataRequestHandler implements Handler {
	private static final Logger LOG = Logger.getLogger(OidDataRequestHandler.class);

    public String doGet(Map<String, String> map) {
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
    private String getData(Map<String, String> map) {
        String result="";
        String uri=map.get("uri");
	    if(uri.startsWith("/oidNidInfo") && map.containsKey("oid")){
	        result = JSONObject.toJSONString(map);
        }else {
	        result="接口路径错误";
        }
    	return result;
    }

    /**
     * POST请求
     * @param map
     * @param content
     * @return
     */
    public String doPost(Map<String, String> map, String content) {
	    return "doPost";
    }
}
