package com.pphh.demo.gw.filter;

import com.pphh.demo.gw.bo.GwAppBO;
import com.pphh.demo.gw.service.GwConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Please add description here.
 *
 * @author huangyinhuang
 * @date 2019/4/25
 */
@Component
public class GwSignatureFilterFactory extends AbstractGatewayFilterFactory<GwSignatureFilterFactory.Config> {

    @Autowired
    private GwConfigService configService;

    public GwSignatureFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        /**
         * TODO: refresh the gwAppMap in a async push way, which will be more efficient
         */
        List<GwAppBO> appBOList = configService.getAppList();
        Map<String, GwAppBO> gwAppMap = new HashMap<>();
        for (GwAppBO app : appBOList) {
            gwAppMap.put(app.getAppKey(), app);
        }
        return new GwSignatureFilter(gwAppMap);
    }

    public static class Config {
        //Put the configuration properties for your filter here
        public Config() {
        }
    }

}
