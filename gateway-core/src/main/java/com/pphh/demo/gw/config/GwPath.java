package com.pphh.demo.gw.config;

/**
 * Please add description here.
 *
 * @author huangyinhuang
 * @date 2019/4/24
 */
public class GwPath {

    public static String getBaseConfigPath() throws Exception {
        String basePath = null;
        if (System.getProperty("os.name").startsWith("Windows")) {
            basePath = "c:/app/gateway";
        } else if (System.getProperty("os.name").startsWith("Linux") ||
                System.getProperty("os.name").startsWith("Mac")) {
            basePath = "/app/gateway";
        } else {
            throw new Exception("failed to decide current OS platform.");
        }
        return basePath;
    }

    public static String getLocalConfigPath() throws Exception {
        String basePath = GwPath.getBaseConfigPath();
        return String.format("%s/%s", basePath, "config.properties");
    }

}
