package com.pphh.demo.gw.controller;

import com.pphh.demo.gw.bo.GwApiBO;
import com.pphh.demo.gw.bo.GwAppBO;
import com.pphh.demo.gw.service.GwConfigService;
import com.pphh.demo.gw.service.GwRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 网关 - 路由服务接口
 *
 * @author huangyinhuang
 * @date 2019/4/22
 */
@RestController
@RequestMapping("/api/route")
public class GwRouterController {

    @Autowired
    private GwRouteService routeService;
    @Autowired
    private GwConfigService configService;

    @RequestMapping(method = RequestMethod.POST, value = "/create")
    public Boolean createRoute(@RequestBody GwApiBO apiDefinition) {
        return routeService.createRoute(apiDefinition);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/delete/{id}")
    public Boolean deleteRoute(@PathVariable String id) {
        routeService.delete(id);
        return Boolean.TRUE;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/update/{id}")
    public Boolean updateRoute() {
        return Boolean.FALSE;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/list")
    public Boolean listRoute() {
        return Boolean.FALSE;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/refresh")
    public Boolean refreshRoute() {
        routeService.refresh();
        return Boolean.TRUE;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/load/local")
    public String loadLocalConfig() {
        routeService.loadByLocalConfig();
        return "the route/appId/appKey/appSecret has been loaded from local config file.";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/config/api")
    public List<GwApiBO> getApiList() {
        return configService.getApiList();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/config/app")
    public List<GwAppBO> getAppList() {
        return configService.getAppListBySecure();
    }
}
