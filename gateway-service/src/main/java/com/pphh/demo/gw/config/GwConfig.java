package com.pphh.demo.gw.config;

import com.pphh.demo.gw.bo.GwApiBO;
import com.pphh.demo.gw.bo.GwAppBO;

import java.util.List;

/**
 * Please add description here.
 *
 * @author huangyinhuang
 * @date 2019/4/24
 */
public interface GwConfig {

    List<GwApiBO> loadApi();

    List<GwAppBO> loadApp();

}
