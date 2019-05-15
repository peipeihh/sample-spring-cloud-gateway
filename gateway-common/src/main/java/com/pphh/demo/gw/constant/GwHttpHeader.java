package com.pphh.demo.gw.constant;

/**
 * 网关HTTP头常量
 *
 * @author huangyinhuang
 * @date 2019/4/18
 */
public class GwHttpHeader {

    // 请求Header Accept
    public static final String HTTP_HEADER_ACCEPT = "Accept";
    // 请求Body内容MD5 Header
    public static final String HTTP_HEADER_CONTENT_MD5 = "Content-MD5";
    // 请求Header Content-Type
    public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    // 请求Header UserAgent
    public static final String HTTP_HEADER_USER_AGENT = "User-Agent";
    // 请求Header Date
    public static final String HTTP_HEADER_DATE = "Date";

    // APP KEY
    public static final String X_AUTH_APPKEY = "X-Auth-AppKey";
    // 签名戳
    public static final String X_AUTH_SIGNATURE = "X-Auth-Signature";
    // 签名方法
    public static final String X_AUTH_SIGNATURE_METHOD = "X-Auth-Signature-Method";
    // 所有参与签名的Header
    public static final String X_AUTH_SIGNATURE_HEADERS = "X-Auth-Signature-Headers";
    // 请求时间戳
    public static final String X_AUTH_TIMESTAMP = "X-Auth-Timestamp";
    // 请求放重放Nonce,15分钟内保持唯一,建议使用UUID
    public static final String X_AUTH_NONCE = "X-Auth-Nonce";
    // 请求令牌
    public static final String X_AUTH_TOKEN = "X-Auth-Token";

    //参与签名的系统Header前缀,只有指定前缀的Header才会参与到签名中
    public static final String X_HEADER_TO_SIGN_PREFIXM = "X-Auth-";

}
