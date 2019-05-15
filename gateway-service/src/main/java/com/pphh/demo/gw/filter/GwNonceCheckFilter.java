package com.pphh.demo.gw.filter;

import com.alibaba.fastjson.JSON;
import com.pphh.demo.gw.constant.GwResultCode;
import com.pphh.demo.gw.constant.GwResultMsg;
import com.pphh.demo.gw.http.GwResponse;
import com.pphh.demo.gw.service.GwRedisCacheService;
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

import static com.pphh.demo.gw.constant.GwHttpHeader.X_AUTH_NONCE;

/**
 * 网关过滤器 - 检验nonce，防止重放攻击
 *
 * @author huangyinhuang
 * @date 2019/4/17
 */
public class GwNonceCheckFilter implements GatewayFilter, Ordered {

    private final static Logger log = LoggerFactory.getLogger(GwNonceCheckFilter.class);

    private GwRedisCacheService gwRedisCacheService;

    public GwNonceCheckFilter(GwRedisCacheService gwRedisCacheService) {
        this.gwRedisCacheService = gwRedisCacheService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String nonce = headers.getFirst(X_AUTH_NONCE);

        Boolean isValid = Boolean.FALSE;
        Boolean isExceptionThrown = Boolean.FALSE;
        if (nonce != null && !nonce.isEmpty()) {
            try {
                isValid = this.gwRedisCacheService.isNonceValid(nonce);
            } catch (Exception e) {
                isExceptionThrown = Boolean.TRUE;
                log.error("failed to check nonce by cache service, msg = {}", e.getMessage());
            }
        }
        log.info("http nonce = {}, is valid = {}", nonce, isValid);
        if (!isValid) {
            GwResponse gwResponse = new GwResponse();
            if (nonce == null || nonce.isEmpty()) {
                gwResponse.setResultCode(GwResultCode.S0006);
                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                gwResponse.setResultMsg(GwResultMsg.MISSING_NONCE);
            } else if (isExceptionThrown) {
                gwResponse.setResultCode(GwResultCode.S0001);
                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                gwResponse.setResultMsg(GwResultMsg.CHECK_NONCE_ERROR);
            } else {
                gwResponse.setResultCode(GwResultCode.S0005);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                gwResponse.setResultMsg(GwResultMsg.INVALID_NONCE);
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
        return -12;
    }

}
