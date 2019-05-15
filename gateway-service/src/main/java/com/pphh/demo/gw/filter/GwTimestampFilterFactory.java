package com.pphh.demo.gw.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

/**
 * timestamp filter factory
 *
 * @author huangyinhuang
 * @date 2019/4/23
 */
@Component
public class GwTimestampFilterFactory extends AbstractGatewayFilterFactory<GwTimestampFilterFactory.Config> {

    public GwTimestampFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new GwTimestampFilter();
    }

    public static class Config {
        //Put the configuration properties for your filter here
        public Config() {
        }
    }

}
