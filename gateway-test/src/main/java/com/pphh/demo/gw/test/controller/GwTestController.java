package com.pphh.demo.gw.test.controller;

import com.pphh.demo.gw.constant.GwResultCode;
import com.pphh.demo.gw.http.GwRequest;
import com.pphh.demo.gw.test.bo.GwTestResponse;
import org.springframework.web.bind.annotation.*;

/**
 * 网关 - 单元测试接口
 *
 * @author huangyinhuang
 * @date 2019/4/18
 */
@RestController
@RequestMapping("/api")
public class GwTestController {

    @RequestMapping(method = RequestMethod.GET, value = "/test/echo")
    public GwTestResponse echoGet(@RequestParam(required = false) String userName) {
        GwTestResponse response = new GwTestResponse();
        response.setResultCode(GwResultCode.S0000);
        if (userName != null) {
            response.addQuery("userName", userName);
        }
        response.setResultMsg("Hello, you have just sent a GET request");
        return response;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/test/echo")
    public GwTestResponse echoPost(@RequestParam(required = false) String userName,
                                   @RequestParam(required = false) String userPwd,
                                   @RequestBody GwRequest request) {
        GwTestResponse response = new GwTestResponse();
        response.setResultCode(GwResultCode.S0002);
        if (userName != null) {
            response.addQuery("userName", userName);
        }
        if (userPwd != null) {
            response.addQuery("userPwd", userPwd);
        }
        response.setResultMsg(String.format("Hello, you have just sent a POST request, pid = %s", request.getPid()));
        response.setResponseData(request.getRequestData());
        return response;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/test/error")
    public GwTestResponse errorGet() throws Exception {
        throw new Exception("An internal exception happened.");
    }

    @RequestMapping(method = RequestMethod.POST, value = "/test/error")
    public GwTestResponse errorPost(@RequestBody GwRequest request) throws Exception {
        throw new Exception("An internal exception happened.");
    }

}
