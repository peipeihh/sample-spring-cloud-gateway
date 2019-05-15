package com.pphh.demo.gw.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

/**
 * Please add description here.
 *
 * @author huangyinhuang
 * @date 2019/5/8
 */
public class GwTokenCheckFilterFactory extends AbstractGatewayFilterFactory<GwTokenCheckFilterFactory.Config> {

    public GwTokenCheckFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new GwTokenCheckFilter();
    }

    public static class Config {
        //Put the configuration properties for your filter here
        public Config() {
        }
    }

}
