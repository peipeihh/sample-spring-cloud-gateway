package com.pphh.demo.gw.filter;

import com.alibaba.fastjson.JSON;
import com.pphh.demo.gw.constant.GwResultCode;
import com.pphh.demo.gw.constant.GwResultMsg;
import com.pphh.demo.gw.http.GwResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.pphh.demo.gw.constant.GwConstant.NONCE_EXPIRED_SECONDS;
import static com.pphh.demo.gw.constant.GwHttpHeader.X_AUTH_TIMESTAMP;

/**
 * 网关过滤器 - 检验请求的时间戳，其必须在指定有效时间段内有效
 *
 * @author huangyinhuang
 * @date 2019/4/17
 */
public class GwTimestampFilter implements GatewayFilter, Ordered {

    private final static Logger log = LoggerFactory.getLogger(GwTimestampFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String timestamp = headers.getFirst(X_AUTH_TIMESTAMP);

        Long requestTs = 0L;
        try {
            requestTs = Long.parseLong(timestamp);
        } catch (Exception ignored) {
            log.info("failed to parse the timestamp into a long value, ts = {}", timestamp);
        }

        /**
         * 若请求的时间戳和当前时间相差指定的时间（NONCE_EXPIRED_SECONDS）以上，则该请求无效
         * 时间戳和Nonce共同校验，一起确保所请求的有效性
         */
        Long nowTs = System.currentTimeMillis();
        Long diff = Math.abs(nowTs - requestTs);
        Boolean isValid = diff < 1000 * NONCE_EXPIRED_SECONDS;
        log.info("http timestamp = {}, is valid = {}", timestamp, isValid);
        if (!isValid) {
            GwResponse gwResponse = new GwResponse();
            if (requestTs == 0) {
                gwResponse.setResultCode(GwResultCode.S0006);
                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                gwResponse.setResultMsg(GwResultMsg.MISSING_TIMESTAMP);
            } else {
                gwResponse.setResultCode(GwResultCode.S0005);
                exchange.getResponse().setStatusCode(HttpStatus.REQUEST_TIMEOUT);
                gwResponse.setResultMsg(GwResultMsg.INVALID_TIMESTAMP);
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
        return -13;
    }

}
