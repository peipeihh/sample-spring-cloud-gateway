package com.pphh.demo.gw.constant;

/**
 * 返回报文的消息
 *
 * @author huangyinhuang
 * @date 2019/4/28
 */
public class GwResultMsg {

    public static final String MISSING_APP_SECRET = "无法找到指定AppSecret，请检查AppKey/AppSecret是否正确颁发和配置。";
    public static final String MISSING_APP_KEY = "无法获取指定AppKey，请求无效。";
    public static final String MISSING_SIGNATURE = "无法获取指定签名，请求无效。";
    public static final String MISSING_TIMESTAMP = "无法获取指定时间戳，请求无效。";
    public static final String MISSING_NONCE = "无法获取指定NONCE，请求无效。";
    public static final String INVALID_SIGNATURE = "签名无效，请正确使用签名算法。";
    public static final String INVALID_TIMESTAMP = "时间戳校验失败，请求无效，请重新发送。";
    public static final String INVALID_NONCE = "NONCE已使用，请求无效，请重新发送。";
    public static final String CHECK_NONCE_ERROR = "无法识别和鉴别指定NONCE，内部错误。";
    public static final String API_ERROR_EXCEPTION = "请求发生错误或异常。";
}
