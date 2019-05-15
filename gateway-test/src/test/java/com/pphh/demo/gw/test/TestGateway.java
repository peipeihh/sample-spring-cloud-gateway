package com.pphh.demo.gw.test;

import com.alibaba.fastjson.JSONObject;
import com.pphh.demo.gw.constant.GwResultCode;
import com.pphh.demo.gw.util.SignUtil;
import com.pphh.demo.gw.test.bo.GwTestResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.pphh.demo.gw.constant.GwHttpHeader.*;
import static com.pphh.demo.gw.constant.Constants.HMAC_SHA256;

/**
 * 一个简单独立的网关单元测试
 *
 * @author huangyinhuang
 * @date 2019/4/18
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestGateway {

    private WebTestClient client;
    private String gatewayHost = "http://localhost:8080";

    @Before
    public void setup() {
        client = WebTestClient.bindToServer()
                .baseUrl(gatewayHost)
                .responseTimeout(Duration.ofMillis(30000))
                .build();

        // 加载测试路由配置 reload the test configuration
        TestUtils.LoadLocalTestConfig(gatewayHost);
    }

    @Test
    public void simpleTest() {
        String uuid = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());

        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(HTTP_HEADER_ACCEPT.toLowerCase(), MediaType.APPLICATION_JSON_VALUE);
        headersMap.put(X_AUTH_APPKEY.toLowerCase(), TestUtils.TEST_APP_KEY);
        headersMap.put(X_AUTH_NONCE.toLowerCase(), uuid);
        headersMap.put(X_AUTH_SIGNATURE_METHOD.toLowerCase(), HMAC_SHA256);
        headersMap.put(X_AUTH_TIMESTAMP.toLowerCase(), timestamp);
        headersMap.put(HTTP_HEADER_DATE.toLowerCase(), timestamp);
        headersMap.put(HTTP_HEADER_CONTENT_MD5.toLowerCase(), "md5");
        headersMap.put(HTTP_HEADER_CONTENT_MD5.toLowerCase(), "md5");
        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/echo", headersMap);

        WebTestClient.ResponseSpec response = client.get().uri("/api/echo")
                .accept(MediaType.APPLICATION_JSON)
                .header(X_AUTH_APPKEY, TestUtils.TEST_APP_KEY)
                .header(X_AUTH_NONCE, uuid)
                .header(X_AUTH_SIGNATURE_METHOD, HMAC_SHA256)
                .header(X_AUTH_SIGNATURE, signature)
                .header(X_AUTH_SIGNATURE_HEADERS, "")
                .header(X_AUTH_TIMESTAMP, timestamp)
                .header(HTTP_HEADER_CONTENT_MD5, "md5")
                .header(HTTP_HEADER_DATE, timestamp)
                .exchange();
        response.expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println(result.getResponseBody());
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0000, resp.getResultCode());
                    Assert.assertEquals("Hello, you have just sent a GET request", resp.getResultMsg());
                    Map<String, String> queryMap = resp.getQueryMap();
                    Assert.assertNull(queryMap);
                });

    }

}
