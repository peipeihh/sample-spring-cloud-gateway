package com.pphh.demo.gw.test.bo;


import com.pphh.demo.gw.http.GwResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * 单元测试接口的报文返回对象
 *
 * @author huangyinhuang
 * @date 2019/4/28
 */
public class GwTestResponse extends GwResponse {

    private Map<String, String> queryMap;

    public Map<String, String> getQueryMap() {
        return queryMap;
    }

    public void setQueryMap(Map<String, String> queryMap) {
        this.queryMap = queryMap;
    }

    public void addQuery(String key, String value) {
        if (key != null) {
            if (queryMap == null) queryMap = new HashMap<>();
            queryMap.put(key, value);
        }
    }
}
