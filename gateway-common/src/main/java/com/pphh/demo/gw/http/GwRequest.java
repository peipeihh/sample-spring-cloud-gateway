package com.pphh.demo.gw.http;

/**
 * Please add description here.
 *
 * @author huangyinhuang
 * @date 2019/5/7
 */
public class GwRequest {

    private String pid;
    private Object requestData;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public Object getRequestData() {
        return requestData;
    }

    public void setRequestData(Object requestData) {
        this.requestData = requestData;
    }
}
