package com.pphh.demo.gw.test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.pphh.demo.gw.config.GwPath;
import org.junit.Assert;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.pphh.demo.gw.constant.Constants.HMAC_SHA256;
import static com.pphh.demo.gw.constant.GwHttpHeader.*;
import static com.pphh.demo.gw.constant.GwHttpHeader.HTTP_HEADER_CONTENT_MD5;

/**
 * 单元测试工具类
 *
 * @author huangyinhuang
 * @date 2019/4/28
 */
public class TestUtils {

    public static final String TEST_APP_KEY = "appkey";
    public static final String TEST_APP_SECRET = "appsecret";

    public static void LoadLocalTestConfig(String gatewayHost) {
        LoadTestConfig(gatewayHost, "/test-config.properties");
    }


    public static void LoadEmptyTestConfig(String gatewayHost) {
        LoadTestConfig(gatewayHost, "/config-empty-api.properties");
    }

    public static void LoadMissingAppKeyConfig(String gatewayHost) {
        LoadTestConfig(gatewayHost, "/config-missing-appkey.properties");
    }

    private static void LoadTestConfig(String gatewayHost, String configFileName) {
        try {
            String configFile = GwPath.getLocalConfigPath();
            FileOutputStream os = new FileOutputStream(new File(configFile));
            Resource resource = new ClassPathResource(configFileName);
            FileCopyUtils.copy(resource.getInputStream(), os);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("failed to copy the test configuration file to loading place.");
        }

        try {
            HttpResponse<String> response = Unirest.post(gatewayHost + "/api/route/load/local").asString();
            if (response.getStatus() != 200) {
                Assert.fail("failed to send a request to gateway as to reload the test configuration.");
            }
        } catch (UnirestException e) {
            e.printStackTrace();
            Assert.fail("received an exception when trying to load the test configuration by gateway.");
        }
    }

    public static Map<String, String> getHeadersMap() {
        String uuid = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());

        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(HTTP_HEADER_ACCEPT.toLowerCase(), MediaType.APPLICATION_JSON_VALUE);
        headersMap.put(X_AUTH_APPKEY.toLowerCase(), TEST_APP_KEY);
        headersMap.put(X_AUTH_NONCE.toLowerCase(), uuid);
        headersMap.put(X_AUTH_SIGNATURE_METHOD.toLowerCase(), HMAC_SHA256);
        headersMap.put(X_AUTH_TIMESTAMP.toLowerCase(), timestamp);
        headersMap.put(HTTP_HEADER_DATE.toLowerCase(), timestamp);
        headersMap.put(HTTP_HEADER_CONTENT_MD5.toLowerCase(), "md5");
        return headersMap;
    }
}
