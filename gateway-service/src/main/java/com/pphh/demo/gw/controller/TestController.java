package com.pphh.demo.gw.controller;

import com.pphh.demo.gw.service.GwRedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网关 - 测试接口
 *
 * @author huangyinhuang
 * @date 2019/5/6
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private GwRedisCacheService gwRedisCacheService;

    @RequestMapping(method = RequestMethod.POST, value = "/checkNonce")
    public Boolean test(@RequestParam String nonce) {
        Boolean isValid = Boolean.FALSE;
        try {
            isValid = gwRedisCacheService.isNonceValid(nonce);
        } catch (Exception ignored) {
        }
        return isValid;
    }

}
