package com.pphh.demo.gw.bo;

import java.util.List;

/**
 * 过滤器列表组
 *
 * @author huangyinhuang
 * @date 2019/4/24
 */
public class GwFilterGroupBO {

    String name;
    List<Class> filterClazzList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Class> getFilterClazzList() {
        return filterClazzList;
    }

    public void setFilterClazzList(List<Class> filterClazzList) {
        this.filterClazzList = filterClazzList;
    }

}
