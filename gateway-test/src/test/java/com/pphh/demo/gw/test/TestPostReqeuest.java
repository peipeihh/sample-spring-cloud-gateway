package com.pphh.demo.gw.test;

import com.alibaba.fastjson.JSONObject;
import com.pphh.demo.gw.test.bo.GwTestRequest;
import com.pphh.demo.gw.test.bo.GwTestResponse;
import com.pphh.demo.gw.constant.GwResultCode;
import com.pphh.demo.gw.constant.GwResultMsg;
import com.pphh.demo.gw.util.SignUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

import static com.pphh.demo.gw.test.TestUtils.getHeadersMap;
import static com.pphh.demo.gw.constant.GwHttpHeader.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 网关测试：POST请求的路由
 *
 * @author huangyinhuang
 * @date 2019/4/28
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestPostReqeuest {

    private WebTestClient client;
    private String gatewayHost = "http://localhost:8080";

    private GwTestRequest reqBody = new GwTestRequest("1", "no data");


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
    public void testPostRequest() {
        Map<String, String> headersMap = getHeadersMap();
        headersMap.put(HTTP_HEADER_CONTENT_TYPE.toLowerCase(), MediaType.APPLICATION_JSON_UTF8_VALUE);
        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.POST.name(), "/api/echo", headersMap);

        WebTestClient.RequestHeadersSpec request = client.post()
                .uri("/api/echo")
                .header(X_AUTH_SIGNATURE, signature)
                .body(Mono.just(this.reqBody), GwTestRequest.class);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = request.exchange();
        response.expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println(result.getResponseBody());
                    assertThat(result.getResponseBody()).isNotEmpty();
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0002, resp.getResultCode());
                    String expected = "Hello, you have just sent a POST request, pid = " + this.reqBody.getPid();
                    Assert.assertEquals(expected, resp.getResultMsg());
                    Assert.assertEquals(this.reqBody.getRequestData(), resp.getResponseData());
                    Map<String, String> queryMap = resp.getQueryMap();
                    Assert.assertNull(queryMap);
                });
    }

    @Test
    public void testPostRequestWithQuery() {
        Map<String, String> headersMap = getHeadersMap();
        headersMap.put(HTTP_HEADER_CONTENT_TYPE.toLowerCase(), MediaType.APPLICATION_JSON_UTF8_VALUE);
        Map<String, String> querysMap = new HashMap<>();
        querysMap.put("userName", "tester");
        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.POST.name(), "/api/echo", headersMap, querysMap);

        WebTestClient.RequestHeadersSpec request = client.post()
                .uri("/api/echo?userName=tester")
                .header(X_AUTH_SIGNATURE, signature)
                .body(Mono.just(this.reqBody), GwTestRequest.class);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = request.exchange();
        response.expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println(result.getResponseBody());
                    assertThat(result.getResponseBody()).isNotEmpty();
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0002, resp.getResultCode());
                    String expected = "Hello, you have just sent a POST request, pid = " + this.reqBody.getPid();
                    Assert.assertEquals(expected, resp.getResultMsg());
                    Assert.assertEquals(this.reqBody.getRequestData(), resp.getResponseData());
                    Map<String, String> queryMap = resp.getQueryMap();
                    Assert.assertEquals(1, queryMap.size());
                    Assert.assertEquals("tester", queryMap.get("userName"));
                });
    }

    @Test
    public void testPostRequestWithMultipleQuery() {
        Map<String, String> headersMap = getHeadersMap();
        headersMap.put(HTTP_HEADER_CONTENT_TYPE.toLowerCase(), MediaType.APPLICATION_JSON_UTF8_VALUE);
        Map<String, String> querysMap = new HashMap<>();
        querysMap.put("userName", "tester");
        querysMap.put("userPwd", "123");

        /**
         * 发送post请求，URI中有两个个查询参数，顺序为userName + userPwd
         */
        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.POST.name(), "/api/echo", headersMap, querysMap);

        WebTestClient.RequestHeadersSpec request = client.post()
                .uri("/api/echo?userName=tester&userPwd=123")
                .header(X_AUTH_SIGNATURE, signature)
                .body(Mono.just(this.reqBody), GwTestRequest.class);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec responseSpec = request.exchange();
        checkPostResultWithMultipleQuery(responseSpec);

        /**
         * 发送post请求，URI中有两个查询参数，顺序为userPwd + userName
         */
        headersMap.put(X_AUTH_NONCE.toLowerCase(), UUID.randomUUID().toString());
        signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.POST.name(), "/api/echo", headersMap, querysMap);

        WebTestClient.RequestHeadersSpec request2 = client.post()
                .uri("/api/echo?userPwd=123&userName=tester")
                .header(X_AUTH_SIGNATURE, signature)
                .body(Mono.just(this.reqBody), GwTestRequest.class);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request2.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response2 = request2.exchange();
        checkPostResultWithMultipleQuery(response2);
    }

    private void checkPostResultWithMultipleQuery(WebTestClient.ResponseSpec response) {
        response.expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println(result.getResponseBody());
                    assertThat(result.getResponseBody()).isNotEmpty();
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0002, resp.getResultCode());
                    String expected = "Hello, you have just sent a POST request, pid = " + this.reqBody.getPid();
                    Assert.assertEquals(expected, resp.getResultMsg());
                    Assert.assertEquals(this.reqBody.getRequestData(), resp.getResponseData());
                    Map<String, String> queryMap = resp.getQueryMap();
                    Assert.assertEquals(2, queryMap.size());
                    Assert.assertEquals("tester", queryMap.get("userName"));
                    Assert.assertEquals("123", queryMap.get("userPwd"));
                });
    }

    @Test
    public void testPostRequestWithAdditionalHeader() {
        Map<String, String> headersMap = getHeadersMap();
        headersMap.put(HTTP_HEADER_CONTENT_TYPE.toLowerCase(), MediaType.APPLICATION_JSON_UTF8_VALUE);
        Map<String, String> querysMap = new HashMap<>();
        List<String> headersToSign = new ArrayList<>();
        headersMap.put("a", "a-test");
        headersMap.put("c", "c-test");
        headersMap.put("b", "b-test");
        headersToSign.add("a");
        headersToSign.add("c");
        headersToSign.add("b");
        querysMap.put("userName", "tester");
        querysMap.put("userPwd", "123");

        /**
         * 发送post请求，URI中有两个个查询参数，顺序为userName + userPwd
         */
        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.POST.name(), "/api/echo", headersMap, headersToSign, querysMap);

        WebTestClient.RequestHeadersSpec request = client.post()
                .uri("/api/echo?userName=tester&userPwd=123")
                .header(X_AUTH_SIGNATURE, signature)
                .body(Mono.just(this.reqBody), GwTestRequest.class);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = request.exchange();
        checkPostResultWithMultipleQuery(response);

        /**
         * 发送post请求，URI中有两个查询参数，顺序为userPwd + userName
         */
        headersMap.put(X_AUTH_NONCE.toLowerCase(), UUID.randomUUID().toString());
        signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.POST.name(), "/api/echo", headersMap, headersToSign, querysMap);

        WebTestClient.RequestHeadersSpec request2 = client.post()
                .uri("/api/echo?userPwd=123&userName=tester")
                .header(X_AUTH_SIGNATURE, signature)
                .body(Mono.just(this.reqBody), GwTestRequest.class);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request2.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response2 = request2.exchange();
        checkPostResultWithMultipleQuery(response2);

    }

    @Test
    public void testPostRequestWithInvalidSignatureHeader() {
        Map<String, String> headersMap = getHeadersMap();
        headersMap.put(HTTP_HEADER_CONTENT_TYPE.toLowerCase(), MediaType.APPLICATION_JSON_UTF8_VALUE);
        Map<String, String> querysMap = new HashMap<>();
        List<String> headersToSign = new ArrayList<>();
        headersMap.put("a", "a-test");
        headersMap.put("c", "c-test");
        headersMap.put("b", "b-test");
        querysMap.put("userName", "tester");
        querysMap.put("userPwd", "123");

        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.POST.name(), "/api/echo", headersMap, headersToSign, querysMap);

        // modify the signature header which make its invalid
        System.out.println("[before] signature header = " + headersMap.get(X_AUTH_SIGNATURE_HEADERS.toLowerCase()));
        headersMap.put(X_AUTH_SIGNATURE_HEADERS.toLowerCase(), "a,b,c");
        System.out.println("[after] signature header = " + headersMap.get(X_AUTH_SIGNATURE_HEADERS.toLowerCase()));


        /**
         * 发送post请求，URI中有两个个查询参数，顺序为userName + userPwd
         */
        WebTestClient.RequestHeadersSpec request = client.post()
                .uri("/api/echo?userName=tester&userPwd=123")
                .header(X_AUTH_SIGNATURE, signature)
                .body(Mono.just(this.reqBody), GwTestRequest.class);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = request.exchange();
        response.expectStatus().is4xxClientError()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println("testPostRequestWithInvalidSignatureHeader");
                    System.out.println(result.getResponseBody());
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0005, resp.getResultCode());
                    Assert.assertEquals(GwResultMsg.INVALID_SIGNATURE, resp.getResultMsg());
                });

    }

}
