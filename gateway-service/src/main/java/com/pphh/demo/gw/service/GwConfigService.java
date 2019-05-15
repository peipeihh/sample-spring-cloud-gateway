package com.pphh.demo.gw.service;

import com.pphh.demo.gw.bo.GwApiBO;
import com.pphh.demo.gw.bo.GwAppBO;
import com.pphh.demo.gw.config.GwLocalConfig;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Please add description here.
 *
 * @author huangyinhuang
 * @date 2019/4/25
 */
@Service
public class GwConfigService {

    private List<GwAppBO> appList;
    private List<GwApiBO> apiList;

    private GwLocalConfig localConfig = GwLocalConfig.getInstance();

    public void loadByLocalConfig() {
        this.appList = localConfig.loadApp();
        this.apiList = localConfig.loadApi();
    }

    public List<GwApiBO> getApiList() {
        return this.apiList;
    }

    public List<GwAppBO> getAppList() {
        return this.appList;
    }

    public List<GwAppBO> getAppListBySecure() {
        List<GwAppBO> appTargetList = new ArrayList<>();
        for (GwAppBO app : this.appList) {
            GwAppBO appTarget = new GwAppBO();
            BeanUtils.copyProperties(app, appTarget);
            appTarget.setAppSecret("*");
            appTargetList.add(appTarget);
        }
        return appTargetList;
    }

    public GwAppBO getAppInfoBy(String appKey) {
        GwAppBO appTarget = null;
        for (GwAppBO app : this.appList) {
            if (app.getAppKey().equals(appKey)) {
                appTarget = app;
            }
        }
        return appTarget;
    }
}
