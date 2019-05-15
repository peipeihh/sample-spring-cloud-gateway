package com.pphh.demo.gw.config;

import com.pphh.demo.gw.bo.GwApiBO;
import com.pphh.demo.gw.bo.GwAppBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Please add description here.
 *
 * @author huangyinhuang
 * @date 2019/4/24
 */
public class GwLocalConfig implements GwConfig {

    private static final Logger log = LoggerFactory.getLogger(GwLocalConfig.class);

    private static GwLocalConfig ourInstance = new GwLocalConfig();

    public static GwLocalConfig getInstance() {
        return ourInstance;
    }

    private GwLocalConfig() {
    }

    private Properties properties;

    private void loadConfig() {
        this.properties = new Properties();

        String configFile = null;
        try {
            configFile = GwPath.getLocalConfigPath();
        } catch (Exception e) {
            log.error("received an exception when trying to get config path.", e);
        }

        if (configFile != null) {
            FileInputStream in = null;
            try {
                log.info("try to load config from file: {}", configFile);
                in = new FileInputStream(configFile);
                this.properties.load(in);
            } catch (FileNotFoundException e) {
                log.error("The tool's config file is not found on the path: {}, details: {}", configFile, e.getMessage());
            } catch (IOException e) {
                log.error("a exception is thrown when trying to load config from file: {}, details: {}", configFile, e.getMessage());
                e.printStackTrace();
            } finally {
                if (in != null) try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    @Override
    public List<GwApiBO> loadApi() {
        List<GwApiBO> apiList = new ArrayList<>();

        loadConfig();
        String idList = this.properties.getProperty("gw.api.idlist", "");
        if (!idList.isEmpty()) {
            String[] apiIdList = idList.split(",");
            for (String apiId : apiIdList) {
                String apiPrefix = String.format("gw.api.%s.", apiId);
                String httpRequestMethod = this.properties.getProperty(apiPrefix + "httpRequestMethod", "");
                String httpRequestApiPath = this.properties.getProperty(apiPrefix + "httpRequestApiPath", "");
                String httpServiceUri = this.properties.getProperty(apiPrefix + "httpServiceUri", "");
                String httpServiceApiPath = this.properties.getProperty(apiPrefix + "httpServiceApiPath", "");
                String filterGroupName = this.properties.getProperty(apiPrefix + "filterGroupName", "");
                String apiOrderVal = this.properties.getProperty(apiPrefix + "order", "");

                log.info("gateway api id = {}, request method = {}, request path = {}, service uri = {}, service path = {}, filter group name = {}, apiOrder = {}",
                        apiId, httpRequestMethod, httpRequestApiPath, httpServiceUri, httpServiceApiPath, filterGroupName, apiOrderVal);
                if (apiId.isEmpty() || httpRequestMethod.isEmpty() || httpRequestApiPath.isEmpty()
                        || httpServiceUri.isEmpty() || httpServiceApiPath.isEmpty() || filterGroupName.isEmpty()) {
                    log.error("gateway api [{}] contains empty value, please check the config file.", apiId);
                    continue;
                }

                Integer apiOrder = null;
                try {
                    apiOrder = Integer.parseInt(apiOrderVal);
                } catch (Exception ignored) {
                    log.info("failed to parse the api order value = [{}]", apiOrderVal);
                }

                GwApiBO api = new GwApiBO();
                api.setRouteId(apiId);
                api.setHttpRequestMethod(httpRequestMethod);
                api.setHttpRequestApiPath(httpRequestApiPath);
                api.setHttpServiceUri(httpServiceUri);
                api.setHttpServiceApiPath(httpServiceApiPath);
                api.setFilterGroupName(filterGroupName);
                api.setOrder(apiOrder);
                apiList.add(api);
            }
        }

        return apiList;
    }

    @Override
    public List<GwAppBO> loadApp() {
        List<GwAppBO> appList = new ArrayList<>();

        loadConfig();
        String idList = this.properties.getProperty("gw.app.idlist", "");
        if (!idList.isEmpty()) {
            String[] appIdList = idList.split(",");
            for (String appId : appIdList) {
                String prefix = String.format("gw.app.%s.", appId);

                String appKey = this.properties.getProperty(prefix + "key", "");
                String appSecret = this.properties.getProperty(prefix + "secret", "");

                log.info("gateway app id = {}, key = {}, secret = {}", appId, appKey, appSecret);
                if (appId.isEmpty() || appKey.isEmpty() || appSecret.isEmpty()) {
                    log.error("gateway app [{}] contains empty value, please check the config file.", appId);
                    continue;
                }

                GwAppBO app = new GwAppBO();
                app.setAppId(appId);
                app.setAppKey(appKey);
                app.setAppSecret(appSecret);
                appList.add(app);
            }
        }

        return appList;
    }
}
