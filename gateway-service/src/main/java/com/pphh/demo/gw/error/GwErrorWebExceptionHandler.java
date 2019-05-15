package com.pphh.demo.gw.error;

import com.pphh.demo.gw.constant.GwResultCode;
import com.pphh.demo.gw.http.GwResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.*;

import java.util.HashMap;
import java.util.Map;

import static com.pphh.demo.gw.constant.GwResultMsg.API_ERROR_EXCEPTION;

/**
 * 网关：请求异常处理handler
 *
 * @author huangyinhuang
 * @date 2019/5/7
 */
public class GwErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GwErrorWebExceptionHandler.class);

    public GwErrorWebExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties, ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @Override
    protected HttpStatus getHttpStatus(Map<String, Object> errorAttributes) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        try {
            Map<String, Object> errorInfo = (Map<String, Object>) errorAttributes.get("responseData");
            Integer statusCode = ((Integer) errorInfo.get("status")).intValue();
            status = HttpStatus.valueOf(statusCode);
        } catch (Exception e) {
            log.error("failed to parse the status code from error info.", e);
        }
        return status;
    }

    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        GwResponse response = new GwResponse();
        response.setResultCode(GwResultCode.S1001);
        response.setResultMsg(API_ERROR_EXCEPTION);
        Map<String, Object> errorInfo = super.getErrorAttributes(request, includeStackTrace);
        response.setResponseData(errorInfo);
        return convert(response);
    }

    private static Map<String, Object> convert(GwResponse response) {
        Map<String, Object> map = new HashMap<>();
        map.put("resultCode", response.getResultCode());
        map.put("resultMsg", response.getResultMsg());
        map.put("responseData", response.getResponseData());
        return map;
    }

}
