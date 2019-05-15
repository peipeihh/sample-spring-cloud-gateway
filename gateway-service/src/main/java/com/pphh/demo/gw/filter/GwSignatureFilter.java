package com.pphh.demo.gw.filter;

import com.alibaba.fastjson.JSON;
import com.pphh.demo.gw.bo.GwAppBO;
import com.pphh.demo.gw.constant.Constants;
import com.pphh.demo.gw.constant.GwResultCode;
import com.pphh.demo.gw.constant.GwResultMsg;
import com.pphh.demo.gw.http.GwResponse;
import com.pphh.demo.gw.util.SignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.RequestPath;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static com.pphh.demo.gw.constant.GwHttpHeader.*;

/**
 * 网关过滤器 - 检验签名
 *
 * @author huangyinhuang
 * @date 2019/4/17
 */
public class GwSignatureFilter implements GatewayFilter, Ordered {

    private final static Logger log = LoggerFactory.getLogger(GwSignatureFilter.class);

    private Map<String, GwAppBO> gwAppMap;

    public GwSignatureFilter(Map<String, GwAppBO> gwAppMap) {
        this.gwAppMap = gwAppMap;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Boolean isAuthorized = Boolean.FALSE;

        HttpMethod httpMethod = exchange.getRequest().getMethod();
        RequestPath path = exchange.getRequest().getPath();
        MultiValueMap<String, String> queryMap = exchange.getRequest().getQueryParams();

        HttpHeaders headers = exchange.getRequest().getHeaders();
        String appKey = headers.getFirst(X_AUTH_APPKEY);
        String signature = headers.getFirst(X_AUTH_SIGNATURE);
        String signatureMethod = headers.getFirst(X_AUTH_SIGNATURE_METHOD);
        String signatureHeaders = headers.getFirst(X_AUTH_SIGNATURE_HEADERS);
        String acceptContent = headers.getFirst(HTTP_HEADER_ACCEPT);
        String contentType = headers.getFirst(HTTP_HEADER_CONTENT_TYPE);
        String contentMd5 = headers.getFirst(HTTP_HEADER_CONTENT_MD5);
        String timestamp = headers.getFirst(X_AUTH_TIMESTAMP);
        String date = headers.getFirst(HTTP_HEADER_DATE);

        log.info("http method = {}, path = {}, appKey = {}, signature = {}, signatureMethod = {}, signatureHeaders = {}, acceptContent = {}, contentType = {}, contentMd5 = {}, ts = {}, date = {}",
                httpMethod.name(), path.value(), appKey,
                signature, signatureMethod, signatureHeaders,
                acceptContent, contentType, contentMd5, timestamp, date);

        GwAppBO app = (gwAppMap != null ? gwAppMap.get(appKey) : null);
        String appSecret = (app != null ? app.getAppSecret() : null);
        if (appSecret != null && signatureHeaders != null) {

            String[] signHeaders = signatureHeaders.split(Constants.SPE1);
            List<String> signHeaderList = new ArrayList<>(Arrays.asList(signHeaders));

            Map<String, String> headersMap = new HashMap<>();
            Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
            for (Map.Entry<String, List<String>> entry : entries) {
                String headerName = entry.getKey().toLowerCase();
                List<String> headerValues = entry.getValue();
                for (String headerVal : headerValues) {
                    headersMap.put(headerName, headerVal);
                }
            }

            Map<String, String> querysMap = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : queryMap.entrySet()) {
                String queryName = entry.getKey();
                List<String> queryValues = entry.getValue();
                for (String queryVal : queryValues) {
                    querysMap.put(queryName, queryVal);
                }
            }

            Map<String, String> bodysMap = new HashMap<>();

            String signResult = SignUtil.sign(appSecret, httpMethod.name(), path.value(),
                    headersMap, signHeaderList, querysMap, bodysMap);
            isAuthorized = (signature != null && signature.equals(signResult));
        }

        log.info("http signature = {}, is signature authorized = {}", signature, isAuthorized);
        if (!isAuthorized) {
            GwResponse gwResponse = new GwResponse();
            if (appKey == null) {
                gwResponse.setResultCode(GwResultCode.S0006);
                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                gwResponse.setResultMsg(GwResultMsg.MISSING_APP_KEY);
            } else if (appSecret == null) {
                log.error("the app secret could not be found by app key = [{}], please check configuration", appKey);
                gwResponse.setResultCode(GwResultCode.S0001);
                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                gwResponse.setResultMsg(GwResultMsg.MISSING_APP_SECRET);
            } else if (signature == null) {
                gwResponse.setResultCode(GwResultCode.S0006);
                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                gwResponse.setResultMsg(GwResultMsg.MISSING_SIGNATURE);
            } else {
                gwResponse.setResultCode(GwResultCode.S0005);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                gwResponse.setResultMsg(GwResultMsg.INVALID_SIGNATURE);
            }
            exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
            return exchange.getResponse().writeWith(
                    Flux.just(exchange.getResponse().bufferFactory().wrap(JSON.toJSONBytes(gwResponse)))
            );
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -10;
    }

}
