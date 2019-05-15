package com.pphh.demo.gw.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.pphh.demo.gw.constant.GwHttpHeader.X_AUTH_TOKEN;

/**
 * 网关过滤器 - 检验token的有效性
 *
 * @author huangyinhuang
 * @date 2019/5/8
 */
public class GwTokenCheckFilter implements GatewayFilter, Ordered {

    private final static Logger log = LoggerFactory.getLogger(GwTokenCheckFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // TODO: C端的令牌校验，校验算法待讨论
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String token = headers.getFirst(X_AUTH_TOKEN);
        log.info("http token = {}", token);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -14;
    }
}
