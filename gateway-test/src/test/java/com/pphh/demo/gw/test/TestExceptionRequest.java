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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;


import static com.pphh.demo.gw.test.TestUtils.getHeadersMap;
import static com.pphh.demo.gw.constant.GwHttpHeader.HTTP_HEADER_CONTENT_TYPE;
import static com.pphh.demo.gw.constant.GwHttpHeader.X_AUTH_SIGNATURE;

/**
 * 网关测试：异常请求
 *
 * @author huangyinhuang
 * @date 2019/4/28
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestExceptionRequest {

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
    public void testNoneExistRouteByGet() {
        Map<String, String> headersMap = getHeadersMap();

        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/none-api", headersMap);

        // build the request with incorrect signature
        WebTestClient.RequestHeadersSpec request = client.get().uri("/api/none-api").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }

        WebTestClient.ResponseSpec response = request.exchange();
        checkApiException(response);
    }


    @Test
    public void testNoneExistRouteByPost() {
        Map<String, String> headersMap = getHeadersMap();
        headersMap.put(HTTP_HEADER_CONTENT_TYPE.toLowerCase(), MediaType.APPLICATION_JSON_UTF8_VALUE);
        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.POST.name(), "/api/none-api", headersMap);

        WebTestClient.RequestHeadersSpec request = client.post()
                .uri("/api/none-api")
                .header(X_AUTH_SIGNATURE, signature)
                .body(Mono.just(this.reqBody), GwTestRequest.class);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = request.exchange();
        checkApiException(response);
    }

    private void checkApiException(WebTestClient.ResponseSpec response) {
        response.expectStatus().is4xxClientError()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println(result.getResponseBody());
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S1001, resp.getResultCode());
                    Assert.assertEquals(GwResultMsg.API_ERROR_EXCEPTION, resp.getResultMsg());
                });
    }

    @Test
    public void testErrorRouteByGet() {
        Map<String, String> headersMap = getHeadersMap();

        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/error", headersMap);

        // build the request with incorrect signature
        WebTestClient.RequestHeadersSpec request = client.get().uri("/api/error").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }

        WebTestClient.ResponseSpec response = request.exchange();
        checkApiError(response);
    }

    @Test
    public void testErrorRouteByPost() {
        Map<String, String> headersMap = getHeadersMap();
        headersMap.put(HTTP_HEADER_CONTENT_TYPE.toLowerCase(), MediaType.APPLICATION_JSON_UTF8_VALUE);
        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.POST.name(), "/api/error", headersMap);

        WebTestClient.RequestHeadersSpec request = client.post()
                .uri("/api/error")
                .header(X_AUTH_SIGNATURE, signature)
                .body(Mono.just(this.reqBody), GwTestRequest.class);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = request.exchange();
        checkApiError(response);
    }

    private void checkApiError(WebTestClient.ResponseSpec response) {
        response.expectStatus().is5xxServerError()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println(result.getResponseBody());
                    JSONObject resp = JSONObject.parseObject(result.getResponseBody());
                    Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), resp.get("status"));
                    Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), resp.get("error"));
                });
    }

}
