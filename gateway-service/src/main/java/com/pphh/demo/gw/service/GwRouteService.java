package com.pphh.demo.gw.service;

import com.pphh.demo.gw.bo.GwApiBO;
import com.pphh.demo.gw.bo.GwFilterGroupBO;
import com.pphh.demo.gw.filter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.MethodRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.InMemoryRouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 网关 - 路由服务
 *
 * @author huangyinhuang
 * @date 2019/4/22
 */
@Service
public class GwRouteService {

    private static final Logger log = LoggerFactory.getLogger(GwRouteService.class);

    @Autowired
    private RouteLocatorBuilder builder;
    @Autowired
    private InMemoryRouteDefinitionRepository inMemoryRouteDefinitionRepository;
    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private GwConfigService gwConfigService;

    private Map<String, GwFilterGroupBO> filterGroupMap;


    public GwRouteService() {
        filterGroupMap = new ConcurrentHashMap<>();

        GwFilterGroupBO gwFilterGroupB = new GwFilterGroupBO();
        gwFilterGroupB.setName("filter-group-b");
        List<Class> filterClazzListB = new ArrayList<>();
        filterClazzListB.add(GwNonceCheckFilterFactory.class);
        filterClazzListB.add(GwSignatureFilterFactory.class);
        filterClazzListB.add(GwTimestampFilterFactory.class);
        gwFilterGroupB.setFilterClazzList(filterClazzListB);

        GwFilterGroupBO gwFilterGroupC = new GwFilterGroupBO();
        gwFilterGroupC.setName("filter-group-c");
        List<Class> filterClazzListC = new ArrayList<>();
        filterClazzListC.add(GwTimestampFilterFactory.class);
        filterClazzListC.add(GwTokenCheckFilterFactory.class);
        gwFilterGroupC.setFilterClazzList(filterClazzListC);

        filterGroupMap.put(gwFilterGroupB.getName(), gwFilterGroupB);
        filterGroupMap.put(gwFilterGroupC.getName(), gwFilterGroupC);
    }

    public void loadByLocalConfig() {
        // clean up the in-memory routes
        Flux<RouteDefinition> inMemoryRoutes = inMemoryRouteDefinitionRepository.getRouteDefinitions();
        inMemoryRoutes.map(RouteDefinition::getId).collectList().subscribe(ids -> {
            for (String id : ids) {
                log.info("delete the in memory route, id = {}", id);
                delete(id);
            }
            refresh();
        });


        // load the routes by local config
        gwConfigService.loadByLocalConfig();
        List<GwApiBO> apiList = gwConfigService.getApiList();
        for (GwApiBO api : apiList) {
            createRoute(api);
        }
        refresh();
    }

    public Boolean createRoute(GwApiBO apiDefinition) {
        String routeId = apiDefinition.getRouteId();
        RouteDefinition route = new RouteDefinition("name=" + routeId);
        route.setId(routeId);

        if (apiDefinition.getOrder() != null) {
            route.setOrder(apiDefinition.getOrder());
        } else {
            route.setOrder(0);
        }

        URI serviceUri = URI.create(apiDefinition.getHttpServiceUri());
        route.setUri(serviceUri);

        // set the filter group
        List<FilterDefinition> filters = new ArrayList<>();
        String filterGroupName = apiDefinition.getFilterGroupName();
        GwFilterGroupBO filterGroup = filterGroupMap.get(filterGroupName);
        if (filterGroup != null) {
            List<Class> filterClazzList = filterGroup.getFilterClazzList();
            for (Class filterClazz : filterClazzList) {
                FilterDefinition filter = new FilterDefinition();
                filter.setName(NameUtils.normalizeFilterFactoryName(filterClazz));
                filters.add(filter);
            }
        } else {
            log.error("the filter group [{}] doesn't exist, please check gateway configuration.", filterGroupName);
        }

        String requestPath = apiDefinition.getHttpRequestApiPath();
        String servicePath = apiDefinition.getHttpServiceApiPath();
        FilterDefinition rewritePathFilter = new FilterDefinition();
        rewritePathFilter.setName(NameUtils.normalizeFilterFactoryName(RewritePathGatewayFilterFactory.class));
        rewritePathFilter.addArg("regexp", requestPath);
        rewritePathFilter.addArg("replacement", servicePath);
        filters.add(rewritePathFilter);

        route.setFilters(filters);

        // add the predicates for request's method and api path
        List<PredicateDefinition> predicates = new ArrayList<>();

        String requestMethod = apiDefinition.getHttpRequestMethod();
        PredicateDefinition methodPredicate = new PredicateDefinition();
        methodPredicate.setName(NameUtils.normalizeRoutePredicateName(MethodRoutePredicateFactory.class));
        methodPredicate.addArg("method", requestMethod);
        predicates.add(methodPredicate);

        String requestApiPath = apiDefinition.getHttpRequestApiPath();
        PredicateDefinition pathPredicate = new PredicateDefinition();
        pathPredicate.setName(NameUtils.normalizeRoutePredicateName(PathRoutePredicateFactory.class));
        pathPredicate.addArg("pattern", requestApiPath);
        predicates.add(pathPredicate);

        route.setPredicates(predicates);

        // save the route
        this.inMemoryRouteDefinitionRepository.save(Mono.just(route)).then().subscribe();

        return Boolean.TRUE;
    }

    public void delete(String id) {
        log.info("try to delete route, id = {}", id);
        this.inMemoryRouteDefinitionRepository.delete(Mono.just(id)).subscribe();
    }

    public void refresh() {
        log.info("try to refresh route event");
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }

}
