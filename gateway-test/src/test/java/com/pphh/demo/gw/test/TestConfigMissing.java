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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.Map;

import static com.pphh.demo.gw.test.TestUtils.getHeadersMap;
import static com.pphh.demo.gw.constant.GwHttpHeader.X_AUTH_SIGNATURE;
import static com.pphh.demo.gw.constant.GwResultMsg.MISSING_APP_SECRET;

/**
 * 测试配置文件中有配置项缺失下的网关功能
 *
 * @author huangyinhuang
 * @date 2019/5/8
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestConfigMissing {

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
    public void loadMissingAppKeyConfigTest() {
        // 加载路由配置
        TestUtils.LoadMissingAppKeyConfig(this.gatewayHost);

        Map<String, String> headersMap = getHeadersMap();
        String signature = SignUtil.sign(TestUtils.TEST_APP_SECRET, HttpMethod.GET.name(), "/api/echo", headersMap);

        WebTestClient.RequestHeadersSpec request = client.get().uri("/api/echo").header(X_AUTH_SIGNATURE, signature);
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        WebTestClient.ResponseSpec response = request.exchange();
        response.expectStatus().is5xxServerError()
                .expectBody(String.class)
                .consumeWith(result -> {
                    System.out.println(result.getResponseBody());
                    GwTestResponse resp = JSONObject.parseObject(result.getResponseBody(), GwTestResponse.class);
                    Assert.assertEquals(GwResultCode.S0001, resp.getResultCode());
                    Assert.assertEquals(MISSING_APP_SECRET, resp.getResultMsg());
                });
    }
}
