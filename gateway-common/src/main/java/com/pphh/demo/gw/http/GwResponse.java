package com.pphh.demo.gw.http;

/**
 * 返回报文消息
 *
 * @author huangyinhuang
 * @date 2019/4/25
 */
public class GwResponse {

    private String resultCode;
    private String resultMsg;
    private Object responseData;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public Object getResponseData() {
        return responseData;
    }

    public void setResponseData(Object responseData) {
        this.responseData = responseData;
    }
}
