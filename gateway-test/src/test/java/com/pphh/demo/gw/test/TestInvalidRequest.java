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
import java.util.HashMap;
import java.util.Map;

import static com.pphh.demo.gw.test.TestUtils.getHeadersMap;
import static com.pphh.demo.gw.constant.GwHttpHeader.*;


/**
 * 网关测试：路由过滤器，包括签名、时间戳、Nonce
 *
 * @author huangyinhuang
 * @date 2019/4/28
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestInvalidRequest {

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
    public void testInvalidSignature() {
        Map<String, String> headersMap = getHeadersMap();

        String signature = "invalid-signature";

        // build the request with incorrect signature
        WebTestClient.RequestHeadersSpec request = client.get().uri("/api/echo").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }

        WebTestClient.ResponseSpec response = request.exchange();
        response.expectStatus().is4xxClientError()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println(result.getResponseBody());
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0005, resp.getResultCode());
                    Assert.assertEquals(GwResultMsg.INVALID_SIGNATURE, resp.getResultMsg());
                });
    }

    @Test
    public void testEmptySignature() {
        Map<String, String> headersMap = getHeadersMap();

        String signature = "invalid-signature";

        // build the request with incorrect signature
        WebTestClient.RequestHeadersSpec request = client.get().uri("/api/echo");
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }

        WebTestClient.ResponseSpec response = request.exchange();
        checkMissingSignaure(response);
    }

    @Test
    public void testMissingSignature() {
        Map<String, String> headersMap = getHeadersMap();

        // build the request without adding the signature
        WebTestClient.RequestHeadersSpec spec = client.get().uri("/api/echo");
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            spec.header(entry.getKey(), entry.getValue());
        }

        WebTestClient.ResponseSpec response = spec.exchange();
        checkMissingSignaure(response);
    }

    private void checkMissingSignaure(WebTestClient.ResponseSpec response) {
        response.expectStatus().is4xxClientError()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println(result.getResponseBody());
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0006, resp.getResultCode());
                    Assert.assertEquals(GwResultMsg.MISSING_SIGNATURE, resp.getResultMsg());
                });
    }

    @Test
    public void testMissingAppKey() {
        Map<String, String> headersMap = getHeadersMap();
        Map<String, String> querysMap = new HashMap<>();
        querysMap.put("userName", "tester");

        // remove the app key header
        headersMap.remove(X_AUTH_APPKEY.toLowerCase());

        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/echo", headersMap, querysMap);

        WebTestClient.RequestHeadersSpec spec = client.get().uri("/api/echo").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            spec.header(entry.getKey(), entry.getValue());
        }

        WebTestClient.ResponseSpec response = spec.exchange();
        response.expectStatus().is4xxClientError()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println(result.getResponseBody());
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0006, resp.getResultCode());
                    Assert.assertEquals(GwResultMsg.MISSING_APP_KEY, resp.getResultMsg());
                });
    }

    @Test
    public void testInvalidTimestamp() {
        Map<String, String> headersMap = getHeadersMap();
        Map<String, String> querysMap = new HashMap<>();
        querysMap.put("userName", "tester");

        // reset the timestamp with incorrect value
        headersMap.put(X_AUTH_TIMESTAMP.toLowerCase(), "1000");

        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/echo", headersMap, querysMap);

        WebTestClient.RequestHeadersSpec spec = client.get().uri("/api/echo?userName=tester").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            spec.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = spec.exchange();
        response.expectStatus().is4xxClientError()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println(result.getResponseBody());
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0005, resp.getResultCode());
                    Assert.assertEquals(GwResultMsg.INVALID_TIMESTAMP, resp.getResultMsg());
                });
    }

    @Test
    public void testMissingTimestamp() {
        Map<String, String> headersMap = getHeadersMap();
        Map<String, String> querysMap = new HashMap<>();
        querysMap.put("userName", "tester");

        // remove the timestamp
        headersMap.remove(X_AUTH_TIMESTAMP.toLowerCase());

        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/echo", headersMap, querysMap);

        WebTestClient.RequestHeadersSpec spec = client.get().uri("/api/echo?userName=tester").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            spec.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = spec.exchange();
        checkMissingTimestamp(response);
    }

    @Test
    public void testNoneExistingTimestamp() {
        Map<String, String> headersMap = getHeadersMap();
        Map<String, String> querysMap = new HashMap<>();
        querysMap.put("userName", "tester");

        // set the timestamp as a none-existing value
        headersMap.put(X_AUTH_TIMESTAMP.toLowerCase(), "none-existing-timestamp");

        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/echo", headersMap, querysMap);

        WebTestClient.RequestHeadersSpec spec = client.get().uri("/api/echo?userName=tester").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            spec.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = spec.exchange();
        checkMissingTimestamp(response);
    }

    @Test
    public void testEmptyTimestamp() {
        Map<String, String> headersMap = getHeadersMap();
        Map<String, String> querysMap = new HashMap<>();
        querysMap.put("userName", "tester");

        // set the timestamp as an empty value
        headersMap.put(X_AUTH_TIMESTAMP.toLowerCase(), "");

        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/echo", headersMap, querysMap);

        WebTestClient.RequestHeadersSpec spec = client.get().uri("/api/echo?userName=tester").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            spec.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = spec.exchange();
        checkMissingTimestamp(response);
    }

    private void checkMissingTimestamp(WebTestClient.ResponseSpec response) {
        response.expectStatus().is4xxClientError()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println(result.getResponseBody());
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0006, resp.getResultCode());
                    Assert.assertEquals(GwResultMsg.MISSING_TIMESTAMP, resp.getResultMsg());
                });
    }

    @Test
    public void testInvalidNonce() {
        Map<String, String> headersMap = getHeadersMap();
        Map<String, String> querysMap = new HashMap<>();
        querysMap.put("userName", "tester");

        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/echo", headersMap, querysMap);

        WebTestClient.RequestHeadersSpec spec = client.get().uri("/api/echo?userName=tester").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            spec.header(entry.getKey(), entry.getValue());
        }

        // send the request twice, which the nonce will be invalid at the second time
        WebTestClient.ResponseSpec response = spec.exchange();
        response.expectStatus().isOk();
        response = spec.exchange();
        response.expectStatus().is4xxClientError()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println(result.getResponseBody());
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0005, resp.getResultCode());
                    Assert.assertEquals(GwResultMsg.INVALID_NONCE, resp.getResultMsg());
                });
    }

    @Test
    public void testMissingNonce() {
        Map<String, String> headersMap = getHeadersMap();
        Map<String, String> querysMap = new HashMap<>();
        querysMap.put("userName", "tester");

        // remove the nonce value
        headersMap.remove(X_AUTH_NONCE.toLowerCase());

        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/echo", headersMap, querysMap);

        WebTestClient.RequestHeadersSpec spec = client.get().uri("/api/echo?userName=tester").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            spec.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = spec.exchange();
        checkMissingNonce(response);
    }

    @Test
    public void testEmptyNonce() {
        Map<String, String> headersMap = getHeadersMap();
        Map<String, String> querysMap = new HashMap<>();
        querysMap.put("userName", "tester");

        // set the nonce value as an empty value
        headersMap.put(X_AUTH_NONCE.toLowerCase(), "");

        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/echo", headersMap, querysMap);

        WebTestClient.RequestHeadersSpec spec = client.get().uri("/api/echo?userName=tester").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            spec.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = spec.exchange();
        checkMissingNonce(response);
    }

    private void checkMissingNonce(WebTestClient.ResponseSpec response) {
        response.expectStatus().is4xxClientError()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println(result.getResponseBody());
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0006, resp.getResultCode());
                    Assert.assertEquals(GwResultMsg.MISSING_NONCE, resp.getResultMsg());
                });
    }

}
