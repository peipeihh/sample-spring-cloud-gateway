package com.pphh.demo.gw.bo;


/**
 * 网关路由 - API的定义
 *
 * @author huangyinhuang
 * @date 2019/4/22
 */
public class GwApiBO {

    /**
     * 路由Id
     */
    String routeId;

    /**
     * API请求定义
     */
    String httpRequestMethod;
    String httpRequestApiPath;

    /**
     * API后端服务定义
     */
    String httpServiceUri;
    String httpServiceApiPath;

    /**
     * 过滤器组
     */
    String filterGroupName;

    Integer order;

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getHttpRequestMethod() {
        return httpRequestMethod;
    }

    public void setHttpRequestMethod(String httpRequestMethod) {
        this.httpRequestMethod = httpRequestMethod;
    }

    public String getHttpRequestApiPath() {
        return httpRequestApiPath;
    }

    public void setHttpRequestApiPath(String httpRequestApiPath) {
        this.httpRequestApiPath = httpRequestApiPath;
    }

    public String getHttpServiceUri() {
        return httpServiceUri;
    }

    public void setHttpServiceUri(String httpServiceUri) {
        this.httpServiceUri = httpServiceUri;
    }

    public String getHttpServiceApiPath() {
        return httpServiceApiPath;
    }

    public void setHttpServiceApiPath(String httpServiceApiPath) {
        this.httpServiceApiPath = httpServiceApiPath;
    }

    public String getFilterGroupName() {
        return filterGroupName;
    }

    public void setFilterGroupName(String filterGroupName) {
        this.filterGroupName = filterGroupName;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

}
