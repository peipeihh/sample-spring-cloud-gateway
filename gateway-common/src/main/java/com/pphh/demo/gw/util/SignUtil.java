package com.pphh.demo.gw.util;

import com.pphh.demo.gw.constant.Constants;
import com.pphh.demo.gw.constant.GwHttpHeader;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

/**
 * Http签名工具
 *
 * @author huangyinhuang
 * @date 2019/4/17
 */
public class SignUtil {

    /*
     Http签名算法如下，
     ```
     StringToSign =
     {HTTPMethod} + "\n" +
     {Accept} + "\n" +
     {Content-MD5} + "\n"
     {Content-Type} + "\n" +
     {Date} + "\n" +
     {SignatureHeaders} + "\n" +
     {UrlToSign}
     ```

     注1：Accept、Content-MD5、Content-Type、Date 如果为空也需要添加换行符"\n"。
     注2：只有From为非表单的方式才需要计算Content-MD5，计算方法为base64Encode(md5(body.getBytes("UTF-8"))。
     注3：SignatureHeaders: 以{HeaderName}:{HeaderValue} + "\n"的方式按照字符串顺序从小到大顺序添加, 建议加入签名的头为Content-Md5、X-Auth-AppKey、X-Auth-RequestId、X-Auth-Timestamp，其他头客户端实现可自行选择是否加入签名。
     注4：QueryString字段放在一起按照Name进行排序，将排序好的键值对加到Path后面得到UrlToSign，例如请求/demo?c=1&a=2，则UrlToSign=/demo?a=2&c=1。
     注5：目前Content-Type只支持application/json类型（若有Content-Type的话）

     使用HMacSHA256算法计算签名，签名的计算需要appSecret，计算方法为：
     ```
     signature = base64(hmacSHA256(stringToSign.getBytes("UTF-8"), appSecret))
     ```

     计算完毕后还需要添加以下Http Headers:
     - X-Auth-Sign: {signature}
     - X-Auth-Sign-Method: HmacSHA256
     - X-Auth-Sign-Headers: Content-Md5,X-Auth-AppKey,X-Auth-RequestId,X-Auth-Timestamp
     */

    /**
     * 计算Http签名
     *
     * @param secret        APP密钥
     * @param method        Http Method
     * @param path          Http Path
     * @param headers       Http Headers
     * @param headersToSign 参与签名Header前缀
     * @param querys        Http Querys
     * @param bodys         Http Bodys
     * @return 签名后的字符串
     */
    public static String sign(String secret, String method, String path,
                              Map<String, String> headers,
                              List<String> headersToSign,
                              Map<String, String> querys,
                              Map<String, String> bodys) {
        try {
            Mac hmacSha256 = Mac.getInstance(Constants.HMAC_SHA256);
            byte[] keyBytes = secret.getBytes(Constants.ENCODING);
            hmacSha256.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, Constants.HMAC_SHA256));
            String strToSign = buildStringToSign(method, path, headers, querys, bodys, headersToSign);
            return new String(Base64.encodeBase64(
                    hmacSha256.doFinal(strToSign.getBytes(Constants.ENCODING))),
                    Constants.ENCODING);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 计算Http签名
     *
     * @param secret  APP密钥
     * @param method  Http Method
     * @param path    Http Path
     * @param headers Http Headers
     * @return 签名后的字符串
     */
    public static String sign(String secret, String method, String path,
                              Map<String, String> headers) {

        List<String> headersToSign = new ArrayList<>();
        Map<String, String> querysMap = new HashMap<>();
        Map<String, String> bodysMap = new HashMap<>();
        return sign(secret, method, path, headers, headersToSign, querysMap, bodysMap);
    }

    /**
     * 计算Http签名
     *
     * @param secret  APP密钥
     * @param method  Http Method
     * @param path    Http Path
     * @param headers Http Headers
     * @param headersToSign 参与签名Header前缀
     * @return 签名后的字符串
     */
    public static String sign(String secret, String method, String path,
                              Map<String, String> headers,
                              List<String> headersToSign) {

        Map<String, String> querysMap = new HashMap<>();
        Map<String, String> bodysMap = new HashMap<>();
        return sign(secret, method, path, headers, headersToSign, querysMap, bodysMap);
    }

    /**
     * 计算Http签名
     *
     * @param secret  APP密钥
     * @param method  Http Method
     * @param path    Http Path
     * @param headers Http Headers
     * @param querys  Http Querys
     * @return 签名后的字符串
     */
    public static String sign(String secret, String method, String path,
                              Map<String, String> headers,
                              Map<String, String> querys) {
        Map<String, String> bodysMap = new HashMap<>();
        List<String> headersToSign = new ArrayList<>();
        return sign(secret, method, path, headers, headersToSign, querys, bodysMap);
    }

    /**
     * 计算Http签名
     *
     * @param secret  APP密钥
     * @param method  Http Method
     * @param path    Http Path
     * @param headers Http Headers
     * @param headersToSign 参与签名Header前缀
     * @param querys  Http Querys
     * @return 签名后的字符串
     */
    public static String sign(String secret, String method, String path,
                              Map<String, String> headers,
                              List<String> headersToSign,
                              Map<String, String> querys) {
        Map<String, String> bodysMap = new HashMap<>();
        return sign(secret, method, path, headers, headersToSign, querys, bodysMap);
    }

    /**
     * 构建待签名字符串
     *
     * @param method        Http Method
     * @param path          Http Path
     * @param headers       Http Headers
     * @param querys        Http Querys
     * @param bodys         Http Bodys
     * @param headersToSign 参与签名Header前缀
     * @return 构建待签名字符串
     */
    private static String buildStringToSign(String method, String path,
                                            Map<String, String> headers,
                                            Map<String, String> querys,
                                            Map<String, String> bodys,
                                            List<String> headersToSign) {
        StringBuilder sb = new StringBuilder();

        sb.append(method.toLowerCase()).append(Constants.LF);
        if (null != headers) {
            if (null != headers.get(GwHttpHeader.HTTP_HEADER_ACCEPT.toLowerCase())) {
                sb.append(headers.get(GwHttpHeader.HTTP_HEADER_ACCEPT.toLowerCase()));
            }
            sb.append(Constants.LF);
            if (null != headers.get(GwHttpHeader.HTTP_HEADER_CONTENT_MD5.toLowerCase())) {
                sb.append(headers.get(GwHttpHeader.HTTP_HEADER_CONTENT_MD5.toLowerCase()));
            }
            sb.append(Constants.LF);
            if (null != headers.get(GwHttpHeader.HTTP_HEADER_CONTENT_TYPE.toLowerCase())) {
                sb.append(headers.get(GwHttpHeader.HTTP_HEADER_CONTENT_TYPE.toLowerCase()));
            }
//            sb.append(Constants.LF);
//            if (null != headers.get(GwHttpHeader.HTTP_HEADER_DATE.toLowerCase())) {
//                sb.append(headers.get(GwHttpHeader.HTTP_HEADER_DATE.toLowerCase()));
//            }
        }
        sb.append(Constants.LF);
        sb.append(buildHeaders(headers, headersToSign));
        sb.append(buildResource(path, querys, bodys));

        return sb.toString();
    }

    /**
     * 构建待签名Path+Query+BODY
     *
     * @param path   Http Path
     * @param querys Http Querys
     * @param bodys  Http Bodys
     * @return 构建待签名字符串
     */
    private static String buildResource(String path, Map<String, String> querys, Map<String, String> bodys) {
        StringBuilder sb = new StringBuilder();

        if (!StringUtils.isBlank(path)) {
            sb.append(path);
        }
        Map<String, String> sortMap = new TreeMap<String, String>();
        if (null != querys) {
            for (Map.Entry<String, String> query : querys.entrySet()) {
                if (!StringUtils.isBlank(query.getKey())) {
                    sortMap.put(query.getKey(), query.getValue());
                }
            }
        }

        if (null != bodys) {
            for (Map.Entry<String, String> body : bodys.entrySet()) {
                if (!StringUtils.isBlank(body.getKey())) {
                    sortMap.put(body.getKey(), body.getValue());
                }
            }
        }

        StringBuilder sbParam = new StringBuilder();
        for (Map.Entry<String, String> item : sortMap.entrySet()) {
            if (!StringUtils.isBlank(item.getKey())) {
                if (0 < sbParam.length()) {
                    sbParam.append(Constants.SPE3);
                }
                sbParam.append(item.getKey());
                if (!StringUtils.isBlank(item.getValue())) {
                    sbParam.append(Constants.SPE4).append(item.getValue());
                }
            }
        }
        if (0 < sbParam.length()) {
            sb.append(Constants.SPE5);
            sb.append(sbParam);
        }

        return sb.toString();
    }

    /**
     * 构建待签名Http头
     *
     * @param headers       请求中所有的Http头
     * @param headersToSign 参与签名Header前缀
     * @return 待签名Http头
     */
    private static String buildHeaders(Map<String, String> headers, List<String> headersToSign) {
        StringBuilder sb = new StringBuilder();

        if (null != headersToSign) {
            headersToSign.remove(GwHttpHeader.X_AUTH_SIGNATURE.toLowerCase());
            headersToSign.remove(GwHttpHeader.HTTP_HEADER_ACCEPT.toLowerCase());
            headersToSign.remove(GwHttpHeader.HTTP_HEADER_CONTENT_MD5.toLowerCase());
            headersToSign.remove(GwHttpHeader.HTTP_HEADER_CONTENT_TYPE.toLowerCase());
            headersToSign.remove(GwHttpHeader.HTTP_HEADER_DATE.toLowerCase());
            Collections.sort(headersToSign);
            if (null != headers) {
                Map<String, String> sortMap = new TreeMap<String, String>();
                sortMap.putAll(headers);
                StringBuilder signHeadersStringBuilder = new StringBuilder();
                for (Map.Entry<String, String> header : sortMap.entrySet()) {
                    if (isHeaderToSign(header.getKey(), headersToSign)) {
                        sb.append(header.getKey());
                        sb.append(Constants.SPE2);
                        if (!StringUtils.isBlank(header.getValue())) {
                            sb.append(header.getValue());
                        }
                        sb.append(Constants.LF);
                        if (0 < signHeadersStringBuilder.length()) {
                            signHeadersStringBuilder.append(Constants.SPE1);
                        }
                        signHeadersStringBuilder.append(header.getKey());
                    }
                }
                headers.put(GwHttpHeader.X_AUTH_SIGNATURE_HEADERS.toLowerCase(), signHeadersStringBuilder.toString());
            }
        }

        return sb.toString();
    }


    /**
     * 判断Http头是否参与签名
     *
     * @param headerName    Http头
     * @param headersToSign 参与签名Header前缀
     * @return True表示需要签名，否则不需要
     */
    private static boolean isHeaderToSign(String headerName, List<String> headersToSign) {
        if (StringUtils.isBlank(headerName)) {
            return false;
        }

        if (headerName.startsWith(Constants.AUTH_HEADER_TO_SIGN_PREFIX_SYSTEM.toLowerCase()) &&
                !headerName.startsWith(GwHttpHeader.X_AUTH_SIGNATURE.toLowerCase())) {
            return true;
        }

        if (null != headersToSign) {
            for (String headerToSign : headersToSign) {
                if (headerName.equalsIgnoreCase(headerToSign)) {
                    return true;
                }
            }
        }

        return false;
    }
}
