package com.pphh.demo.gw.test;

import com.alibaba.fastjson.JSONObject;
import com.pphh.demo.gw.constant.GwResultCode;
import com.pphh.demo.gw.util.SignUtil;
import com.pphh.demo.gw.test.bo.GwTestResponse;
import com.pphh.demo.gw.test.bo.GwTestRequest;
import com.pphh.demo.gw.constant.GwResultMsg;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pphh.demo.gw.constant.GwHttpHeader.*;
import static com.pphh.demo.gw.test.TestUtils.getHeadersMap;


/**
 * 网关测试：GET请求的路由
 *
 * @author huangyinhuang
 * @date 2019/4/28
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestGetRequest {

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
    public void testGetRequest() {
        Map<String, String> headersMap = getHeadersMap();
        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/echo", headersMap);

        WebTestClient.RequestHeadersSpec request = client.get().uri("/api/echo").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = request.exchange();
        response.expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println("testGetRequest");
                    System.out.println(result.getResponseBody());
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0000, resp.getResultCode());
                    Assert.assertEquals("Hello, you have just sent a GET request", resp.getResultMsg());
                    Map<String, String> queryMap = resp.getQueryMap();
                    Assert.assertNull(queryMap);
                });
    }

    @Test
    public void testGetRequestWithQuery() {
        Map<String, String> headersMap = getHeadersMap();
        Map<String, String> querysMap = new HashMap<>();
        querysMap.put("userName", "tester");
        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/echo", headersMap, querysMap);

        WebTestClient.RequestHeadersSpec request = client.get().uri("/api/echo?userName=tester").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = request.exchange();
        response.expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println("testGetRequestWithQuery");
                    System.out.println(result.getResponseBody());
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0000, resp.getResultCode());
                    Assert.assertEquals("Hello, you have just sent a GET request", resp.getResultMsg());
                    Map<String, String> queryMap = resp.getQueryMap();
                    Assert.assertEquals(1, queryMap.size());
                    Assert.assertEquals("tester", queryMap.get("userName"));
                });
    }

    @Test
    public void testGetRequestWithMultipleQuery() {
        Map<String, String> headersMap = getHeadersMap();
        Map<String, String> querysMap = new HashMap<>();
        querysMap.put("userName", "tester");
        querysMap.put("userPwd", "123");
        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/echo", headersMap, querysMap);

        WebTestClient.RequestHeadersSpec request = client.get().uri("/api/echo?userName=tester&userPwd=123").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = request.exchange();
        response.expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println("testGetRequestWithMultipleQuery");
                    System.out.println(result.getResponseBody());
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0000, resp.getResultCode());
                    Assert.assertEquals("Hello, you have just sent a GET request", resp.getResultMsg());
                    Map<String, String> queryMap = resp.getQueryMap();
                    Assert.assertEquals(1, queryMap.size());
                    Assert.assertEquals("tester", queryMap.get("userName"));
                });
    }

    @Test
    public void testGetRequestWithAdditionalHeader() {
        Map<String, String> headersMap = getHeadersMap();
        Map<String, String> querysMap = new HashMap<>();
        List<String> headersToSign = new ArrayList<>();
        headersMap.put("a", "a-test");
        headersMap.put("c", "c-test");
        headersMap.put("b", "b-test");
        headersToSign.add("a");
        headersToSign.add("c");
        headersToSign.add("b");

        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/echo", headersMap, headersToSign, querysMap);

        WebTestClient.RequestHeadersSpec request = client.get().uri("/api/echo").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = request.exchange();
        response.expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println("testGetRequestWithAdditionalHeader");
                    System.out.println(result.getResponseBody());
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0000, resp.getResultCode());
                    Assert.assertEquals("Hello, you have just sent a GET request", resp.getResultMsg());
                });
    }

    @Test
    public void testGetRequestWithInvalidSignatureHeader() {
        Map<String, String> headersMap = getHeadersMap();
        Map<String, String> querysMap = new HashMap<>();
        List<String> headersToSign = new ArrayList<>();
        headersMap.put("a", "a-test");
        headersMap.put("c", "c-test");
        headersMap.put("b", "b-test");

        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/echo", headersMap, headersToSign, querysMap);

        // modify the signature header which make its invalid
        System.out.println("[before] signature header = " + headersMap.get(X_AUTH_SIGNATURE_HEADERS.toLowerCase()));
        headersMap.put(X_AUTH_SIGNATURE_HEADERS.toLowerCase(), "a,b,c");
        System.out.println("[after] signature header = " + headersMap.get(X_AUTH_SIGNATURE_HEADERS.toLowerCase()));

        WebTestClient.RequestHeadersSpec request = client.get().uri("/api/echo").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = request.exchange();
        response.expectStatus().is4xxClientError()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println("testGetRequestWithInvalidSignatureHeader");
                    System.out.println(result.getResponseBody());
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0005, resp.getResultCode());
                    Assert.assertEquals(GwResultMsg.INVALID_SIGNATURE, resp.getResultMsg());
                });
    }

}
