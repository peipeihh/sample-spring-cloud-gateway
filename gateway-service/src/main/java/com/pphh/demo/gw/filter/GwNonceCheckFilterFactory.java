package com.pphh.demo.gw.filter;

import com.pphh.demo.gw.service.GwRedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

/**
 * Please add description here.
 *
 * @author huangyinhuang
 * @date 2019/4/25
 */
@Component
public class GwNonceCheckFilterFactory extends AbstractGatewayFilterFactory<GwNonceCheckFilterFactory.Config> {

    @Autowired
    private GwRedisCacheService gwRedisCacheService;

    public GwNonceCheckFilterFactory() {
        super(GwNonceCheckFilterFactory.Config.class);
    }

    @Override
    public GatewayFilter apply(GwNonceCheckFilterFactory.Config config) {
        return new GwNonceCheckFilter(this.gwRedisCacheService);
    }

    public static class Config {
        //Put the configuration properties for your filter here
        public Config() {
        }
    }

}
