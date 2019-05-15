package com.pphh.demo.gw.test;

import com.pphh.demo.gw.constant.Constants;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 测试签名算法
 *
 * @author huangyinhuang
 * @date 2019/5/10
 */
public class TestSignature {

    @Test
    public void testSign() {
        try {
            Mac hmacSha256 = Mac.getInstance(Constants.HMAC_SHA256);

            String secret = "test";
            byte[] keyBytes = secret.getBytes(Constants.ENCODING);
            hmacSha256.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, Constants.HMAC_SHA256));

            byte[] bytes = hmacSha256.doFinal("hello,world".getBytes(Constants.ENCODING));

            byte[] base64 = Base64.encodeBase64(bytes);
            String result = new String(base64, Constants.ENCODING);

            System.out.println(result);
            Assert.assertEquals("hWNHSSaU6MiFxBwPQ/J2RsAbx3lGUfZKAYw+rope/vM=", result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
