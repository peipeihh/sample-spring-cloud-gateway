package com.pphh.demo.gw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * 网关主程序
 *
 * @author huangyinhuang
 * @date 2019/4/18
 */
@SpringBootApplication
@ImportResource(locations = {"classpath:context/context-properties.xml"})
public class GwApplication {

    public static void main(String[] args) {
        SpringApplication.run(GwApplication.class, args);
    }

}
