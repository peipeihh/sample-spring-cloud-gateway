package com.pphh.demo.gw.test.bo;


import com.pphh.demo.gw.http.GwRequest;

/**
 * Please add description here.
 *
 * @author huangyinhuang
 * @date 2019/5/7
 */
public class GwTestRequest extends GwRequest {

    public GwTestRequest(String pid, String reqData) {
        this.setPid(pid);
        this.setRequestData(reqData);
    }
}
