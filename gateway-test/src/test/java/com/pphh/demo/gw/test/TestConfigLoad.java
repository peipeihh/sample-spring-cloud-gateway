package com.pphh.demo.gw.test;

import com.pphh.demo.gw.constant.GwResultCode;
import com.pphh.demo.gw.util.SignUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.Map;

import static com.pphh.demo.gw.test.TestUtils.getHeadersMap;
import static com.pphh.demo.gw.constant.GwHttpHeader.*;

/**
 * 测试加载不同路由配置文件下的网关功能
 *
 * @author huangyinhuang
 * @date 2019/4/28
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestConfigLoad {

    private WebTestClient client;
    private String gatewayHost = "http://localhost:8080";

    @Before
    public void setup() {
        client = WebTestClient.bindToServer()
                .baseUrl(gatewayHost)
                .responseTimeout(Duration.ofMillis(30000))
                .build();
    }

    @Test
    public void loadEmptyConfigTest() {
        // 加载空路由配置
        TestUtils.LoadEmptyTestConfig(this.gatewayHost);

        Map<String, String> headersMap = getHeadersMap();
        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/echo", headersMap);

        WebTestClient.RequestHeadersSpec request = client.get().uri("/api/echo").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = request.exchange();
        response.expectStatus().is4xxClientError()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println(result.getResponseBody());
                    Assert.assertTrue(result.getResponseBody().contains("/api/echo"));
                    Assert.assertTrue(result.getResponseBody().contains("Not Found"));
                });
    }

    @Test
    public void loadLocalConfigTest() {
        // 加载测试路由配置
        TestUtils.LoadLocalTestConfig(this.gatewayHost);

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
                    System.out.println(result.getResponseBody());
                    Assert.assertTrue(result.getResponseBody().contains(GwResultCode.S0000));
                    Assert.assertTrue(result.getResponseBody().contains("Hello"));
                });
    }

    @Test
    public void switchConfigLoadTest() {
        loadEmptyConfigTest();
        loadLocalConfigTest();
        loadEmptyConfigTest();
        loadLocalConfigTest();
    }

}
