package com.pphh.demo.gw.configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关 - 初始化配置
 *
 * @author huangyinhuang
 * @date 2019/4/18
 */
@Configuration
public class GwAutoConfiguration {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes().build();
//        return builder.routes()
//                .route("get_route", r -> r.path("/get")
//                        .uri("http://httpbin.org")
//                )
//                .route("post_router", r -> r.path("/test**").and().method(HttpMethod.POST)
//                        .filters(f -> f.filter(new GwTimestampFilter())
//                                //.filter(new GwSignatureFilter())
//                                .filter(new GwNonceCheckFilter())
//                                .filter((exchange, chain) -> {
//                                    ServerHttpRequest req = exchange.getRequest();
//                                    addOriginalRequestUrl(exchange, req.getURI());
//                                    ServerHttpRequest request = req.mutate()
//                                            .path("/api/test/echo")
//                                            .build();
//                                    exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, request.getURI());
//                                    return chain.filter(exchange.mutate().request(request).build());
//                                })
//                        )
//                        .uri(URI.create("http://localhost:8080"))
//                )
//                .build();
    }

}
