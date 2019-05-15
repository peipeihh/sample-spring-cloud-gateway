package com.pphh.demo.gw.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Please add description here.
 *
 * @author huangyinhuang
 * @date 2019/5/7
 */
@Service
public class GwRedisCacheService {

    private final static Logger log = LoggerFactory.getLogger(GwRedisCacheService.class);
    private final static String REDIS_KEY_PREFIX_NONCE = "common:gateway:";

    private Map<String, String> cacheMap = new ConcurrentHashMap<>();

    /**
     * 检查nonce值的唯一性，
     * - 若nonce已记录在redis中，则该nonce已被使用过，该nonce为无效
     * - 若nonce没有记录在redis中，则该nonce值还未使用过，该nonce为有效，与此同时需记录在redis中，待下次检验
     * 注：每个nonce有过期时间，超过过期时间，则nonce值注销。对于过期时间之外的请求，由TIMESTAMP过滤器校验其的有效性。
     *
     * @param nonce Number Once，即只能使用一次的值
     * @return True 则该nonce未被使用过，该nonce有效，否则为无效。
     * @throws Exception 若无法和redis server通信，获取和设置相应的值，则抛出异常
     */
    public Boolean isNonceValid(String nonce) throws Exception {
        Boolean isValid = Boolean.FALSE;

        String nonceKey = REDIS_KEY_PREFIX_NONCE + nonce;
        String nonceValue = cacheMap.get(nonceKey);
        if (nonceValue == null) {
            isValid = Boolean.TRUE;
            cacheMap.put(nonceKey, nonce);
        }

        return isValid;
    }


}
