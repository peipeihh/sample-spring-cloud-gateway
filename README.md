
## 1. 简介

网关作为一个基础架构组件，在流量入口实现统一的安全、权限和流量控制，使得能够快速、安全、高效的发布内部API，提供给外部用户和合作伙伴使用。

## 2. 项目结构

```
- pom.xml
+ doc
  - config.properties 网关API路由配置
+ gateway-common 通用类库：为所有网关java项目共享类库，提供包括签名方法/md5/请求头常量等通用工具类、常量类
+ gateway-core 核心类库：网关dao/service接口
+ gateway-service 网关
  - src/main/java
    + com.pphh.demo.gw
      + controller
      + filter
      + configuration
      + service
      - GatewayApplication
  - src/main/resources
  - src/test
+ gateway-web 网关后台管理（待开发）
+ gateway-sample 网关使用样例
  - gateway-sample-java java样例
  - gateway-sample-python python样例
+ gateway-test 网关单元测试
```

## 3. 项目构建

### 3.1 运行平台要求

- Java 8 + Maven 3
- Mysql 5.7

### 3.2 技术栈

- Spring Cloud Gateway 
- Spring Boot Webflux
- Netty NIO
- Reactor(Non-Blocking Reactive Foundation for the JVM)

### 3.3 项目编译


执行编译构建命令，
```
mvn clean package -DskipTests
```

生成的可执行jar包位于，
```
./gateway-service/target/app-gateway.jar
```

## 4. 运行和单元测试

### 4.1 运行网关


运行网关的命令如下，

```
java -jar ./gateway-service/target/app-gateway.jar
```

上述命令将启动网关在8080端口。

可以观察到如下的启动日志，

```
$ > java -jar ./gateway-service/target/app-gateway.jar
2019-05-15 11:35:12.909  INFO 3732 --- [           main] s.c.a.AnnotationConfigApplicationContext : Refreshing ...

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.0.9.RELEASE)

2019-05-15 11:35:25.167  INFO 3732 --- [ctor-http-nio-1] r.ipc.netty.tcp.BlockingNettyContext     : Started HttpServer on /0:0:0:0:0:0:0:0:8080
2019-05-15 11:35:25.169  INFO 3732 --- [           main] o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port(s): 8080
2019-05-15 11:35:25.173  INFO 3732 --- [           main] com.pphh.demo.gw.GwApplication           : Started GwApplication in 14.167 seconds (JVM running for 15.627)
```

### 4.2 执行单元测试

单元测试项目：.\gateway-test

执行单元测试的命令如下，

```
mvn test
```

上述命令将运行gateway-test的单元测试，其运行一个后端服务在8090端口。

测试用例将请求网关（端口8080），网关将根据加载配置转向后端服务（端口8090），完成网关测试。

查看日志，可以看到单元测试的执行结果如下，

```

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.0.9.RELEASE)

...
Results :

Tests run: 31, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO]
[INFO] sample-spring-cloud-gateway ........................ SUCCESS [  0.006 s]
[INFO] gateway-common ..................................... SUCCESS [  0.960 s]
[INFO] gateway-core ....................................... SUCCESS [  0.033 s]
[INFO] gateway-service .................................... SUCCESS [  0.575 s]
[INFO] gateway-test ....................................... SUCCESS [ 19.196 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 21.552 s
[INFO] Finished at: 2019-05-15T11:39:15+08:00
[INFO] Final Memory: 24M/315M
[INFO] ------------------------------------------------------------------------
```

### 4.3 网关API路由配置

目前网关的API路由配置位于，
- Windows平台 C:\app\gateway\config.properties
- Linux平台 /app/gateway/config.properties

配置样例见，
- ./doc/config.properties

## 5. 项目规范

### 5.1 代码提交

- 在代码提交前，必须运行单元测试，全部通过后才能提交到代码仓库。