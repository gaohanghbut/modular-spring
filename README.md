# modular-spring
Spring框架是一把灵活锋利的瑞士军刀，这里通过扩展Spring，实现工程的模块化，modular-spring的功能如下：

* modular-spring-core：提供模块化的基础功能，比如模块化的ApplicationContext，模块的扩展接口，服务的发布与引用，服务的自动打桩等
* modular-spring-boot：提供模块化工程的加载功能
* modular-spring-webmvc：modular-spring对springmvc的支持
* modular-spring-springboot：在springboot中使用modular-spring
* modular-spring-tx：对spring的事务管理器的扩展，支持一个事务管理器同时管理多个数据源
* modular-spring-http：通过类似于mybatis的注解和映射接口的方式提供http服务的集成api并实现了按模块加载和发布模块间的服务
* modular-spring-plugin-api：开发插件的api包
* modular-spring-mybatis：模块化后，对mybatis映射接口的支持

测试工程地址：https://github.com/gaohanghbut/modular-spring-test


## 模块化
面向对象中的封装是指利用抽象数据类型将数据和基于数据的操作封装在一起，
使其构成一个不可分割的独立实体，数据被保护在抽象数据类型的内部，
尽可能地隐藏内部的细节，只保留一些对外接口使之与外部发生联系

进一步将提供某种服务的代码封装成模块或者组件,隐藏服务的实现细节,对外暴露服务接口,
模块之间通过暴露接口的方式实现交互,不依赖模块的内部结构.

模块化的设计可让开发人员专注于模块本身,提高认知效率.不同的模块可以非常方便的进行集成,
通过组装不同模块使得软件易于裁剪,模块内部结构可自由修改.

## 模块化的方式
1.工程中通过不同的package实现模块化

2.通过依赖注入容器实现模块化

3.通过classLoader单独加载模块实现模块化(OSGI)

第一种方式依赖人工维护模块之间的隔离,难以控制,第三种方式通过classloader做隔离,
但模块之间的调用需要将参数,返回值以及异常做序列化和反序列化,成本比较大,第二种方式
没有类之间的隔离,模块间的调用开销小,但无法解决模块间依赖不兼容的问题
(模块1与模块2依赖了某个jar的不同版本).这里选择使用Spring做模块隔离.

## modular-spring

![modular-spring-arch](docs/img/modular-spring-arch.png)

* core：modular-spring提供的核心服务
    * 提供Spring架构的扩展，支持单个模块的应用上下文加载能力
    * 模块化扩展点的支持，支持不同的模块之间的依赖倒置能力
    * ClassLoader的扩展，支持插件的classloader隔离
    * 插件api，用于实现插件开发的简单接口
* boot：提供模块化装载与插件装载能力，主要由三个组件组成
    * ModuleLoader：用于装载系统的各个模块，创建各模块的应用上下文
    * PluginLoader：用于装载插件
    * ApplicationManager：用于管理应用的生命周期
* extension：框架的扩展功能
* app：应用层，由各种不同的插件和模块化的应用组成
    

## modular-spring的模块的约定
包含如下两类文件的jar或者子工程被认为是一个模块
* META-INF/module.json(一个模块只能有一个)
* META-INF/spring/xxx.xml(spring配置文件,一个模块可以有多个,命名随意)

module.json文件是对模块的描述,包含模块名,模块依赖的其它模块列表,例如:
```json
{
  "moduleName": "cn.yxffcode.test.coreservice",
  "dependenceModules": ["cn.yxffcode.test.commondal", "cn.yxffcode.test.commonsal"]
}
```
moduleName表示模块名,dependenceModules表示依赖的模块列表,依赖的模块会在此模块之前被加载
spring配置文件与普通spring工程相同

## 服务接口
可以在spring的配置文件中暴露服务接口,例如:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:modular="http://www.yxffcode.cn/modular"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.yxffcode.cn/modular http://www.yxffcode.cn/modular.xsd">

  <bean name="testService" class="cn.yxffcode.modularspring.test.TestServiceImpl"/>

  <modular:service ref="testService" interface="cn.yxffcode.modularspring.test.TestService"/>

</beans>
```
modular:service表示服务接口的暴露,意味着此接口能被其它模块所使用

可以在spring的配置文件中引用其它模块暴露的服务,例如:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:modular="http://www.yxffcode.cn/modular"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.yxffcode.cn/modular
       http://www.yxffcode.cn/modular.xsd">

  <modular:reference name="testService" interface="cn.yxffcode.modularspring.test.TestService"/>

  <bean class="cn.yxffcode.modularspring.test.TestCoreService" init-method="init">
    <property name="testService" ref="testService"/>
  </bean>
</beans>
```

modular:reference表示引用一个服务,如果此模块对引用的服务有初始化依赖,则需要在module.json文件中配置模块
依赖,即将服务所在的模块名配置到此模块的dependenceModules中.

## 服务引用的自动代理
在开发模块时，如果模块依赖其它模块的服务，而服务尚未实现或服务提供模块尚未集成进来，可以给服务的引用加上代理，让模块
能正常工作，可以使用auto-stub标签打开服务引用自动代理功能。
```xml
<modular:auto-stub/>
```
auto-stub标签在modular:reference的基础上加了一层检查，如果引用的服务不存在，则使用代理对象代替服务对象.
可以通过stub标签定义服务引用代理对象的InvocationHandler。
```xml
<modular:stub interface="cn.yxffcode.modularspring.service.TestStubService"
              invocation-handler="cn.yxffcode.modularspring.test.TestStubInvocationHandler"/>
```
可以使用invocation-handler-ref指定一个bean的name来代替invocation-handler，如果invocation-handler和
invocation-handler-ref都没有指定，则使用默认stub，默认stub将所有的方法调用都返回null。

开启auto-stub功能后，服务引用接口的stub是可选的，如果没有配置，则和原有逻辑一样，找不到服务提供对象会出错
## 注解
### 发布服务
可使用@ModularService发布服务,使用ModularService标记的bean会被spring托管,并作为模块的服务发布,例如:
```java
package cn.yxffcode.modularspring.service;

import cn.yxffcode.modularspring.core.annotation.ModularService;

/**
 * @author gaohang on 7/2/17.
 */
@ModularService
public class TestServiceImpl implements TestService {
  public void test() {
    System.out.println("test service dal");
  }
}
```
### 引用服务
不同模块之间通过服务接口交互,可使用@ModularReference引用其它模块的服务,例如:
```java
package cn.yxffcode.modularspring.test;

import cn.yxffcode.modularspring.core.annotation.ModularReference;
import cn.yxffcode.modularspring.service.TestService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * @author gaohang on 7/7/17.
 */
@Service
public class TestCoreService implements InitializingBean {

  @ModularReference
  private TestService testService;

  public void init() {
    testService.test();
  }

  public TestService getTestService() {
    return testService;
  }

  public void setTestService(TestService testService) {
    this.testService = testService;
  }

  public void afterPropertiesSet() throws Exception {
    init();
  }
}
```
### 使用context:component-scan
因为框架没有对各模块做classloader的隔离,为了防止当前模块扫描到其它模块里的bean,需要修改配置:
```xml
<context:component-scan base-package="cn.yxffcode.modularspring">
    <context:exclude-filter type="custom" expression="cn.yxffcode.modularspring.boot.spring.ModuleTypeFilter"/>
</context:component-scan>
```
### 使用modular:component-scan
待实现
## 模块加载的前置处理和后置处理
通过ModuleLoadListener接口可以对模块加载做前置处理或者后置处理,例如想要在模块加载前向模块中添加某些Bean,
可以在ModuleLoadListener中对spring注册BeanDefinitionRegistryPostProcessor:
```java
package cn.yxffcode.modularspring.service;

import cn.yxffcode.modularspring.boot.ModuleConfig;
import cn.yxffcode.modularspring.boot.listener.ModuleLoadListener;
import cn.yxffcode.modularspring.core.context.ModuleApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * @author gaohang on 7/9/17.
 */
public class PostFactoryBeanModuleLoadListener implements ModuleLoadListener {

  public void beforeModuleLoad(ModuleConfig moduleConfig, ModuleApplicationContext applicationContext) {
    applicationContext.addBeanFactoryPostProcessor(new BeanDefinitionRegistryPostProcessor() {
      public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        //do something
      }

      public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //do something
      }
    });
  }

  public void afterModuleLoad(ModuleConfig moduleConfig, ModuleApplicationContext applicationContext) {

  }
}
```

## 扩展点
扩展点提供三种使用方式：
* 指定接口，其它模块中提供接口的实现，仅限于接口只有一个实现的情况
* 使用扩展点容器，只需要将扩展点接口配置成ExtensionContainer即哥，适用于一个接口有多个实现的情况
* Extension-Handler，适用于有复杂的扩展点注入逻辑的情况
### 扩展接口
有时候,一个模块中需要使用的接口的实现不由此模块决定,比如数据访问模块需要使用DataSource,而DataSource由集成此模块的系统主模块决定.
需要通过一种扩展机制,将数据源注入到模块中,在modular-spring中支持扩展点接口,可以将一个接口声明为扩展点,在其它模块中声明接口的实现bean来达到扩展的目的.
例如声明数据源为扩展点:
```xml
<modular:extension name="dataSource" interface="javax.sql.DataSource"/>
```
在系统主模块中,提供扩展点提实现:
```xml
  <bean name="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource"/>
  <modular:extension-point extension-name="dataSource" ref="dataSource"/>
```
### 扩展点容器
有时候，模块中依赖一个接口的多个实现，则可以使用ExtensionContainer，用法与扩展接口类似，只需要将扩展点接口改为ExtensionContainer：
```xml
  <modular:extension name="testExtensionContainer" interface="cn.yxffcode.modularspring.core.ext.ExtensionContainer"/>
```
其它模块中提供扩展点实例：
```xml
  <bean name="ext1" class="java.lang.Object"/>
  <modular:extension-point extension-name="testExtensionContainer" ref="ext1"/>
  
  <bean name="ext2" class="java.lang.Object"/>
  <modular:extension-point extension-name="testExtensionContainer" ref="ext2"/>
```
同一个ExtensionContainer的扩展点实例可以在不同的模块中
### Extension-Handler
对于复杂的扩展点处理逻辑，需要提供一个extension-handler类，使用方式如下：
```xml
<bean name="myExtensionHandler" class="xxx"/>
<modular:extension-handler name="myExtensionHandler" handler-bean-ref="myExtensionHandler">
    <modular:listener-method name="methodName1" extension-type="class1"/>
    <modular:listener-method name="methodName2" extension-type="class2"/>
</modular:extension-handler>
```
注入扩展点的模块中的使用方式与前两种一样：
```xml
<bean name="ext2" class="class1"/>
<modular:extension-point extension-name="myExtensionHandler" ref="ext2"/>
```

### 注解的方式使用扩展点
支持extension-handler，提供了@ExtensionHandler和@ExtensionListener两个注解，用来取代extension-handler的xml配置，使用方式如下：
```java
@ExtensionHandler("testAnnotationExtensionHandler")//指定扩展点名，如果不指定，则默认使用bean name
@Component//如果没有开启component-scan，则需要在spring的xml中配置TestAnnotationExtensionHandler bean，开启方式见使用context:component-scan的介绍
public class TestAnnotationExtensionHandler {

  @ExtensionListener
  public void addDataSource(final DataSource dataSource) {
    System.out.println("testAnnotationExtensionHandler add dataSource = " + dataSource);
  }

  @ExtensionListener(Object.class)
  public void addExtension(final Object extension) {
    System.out.println("testAnnotationExtensionHandler add extension = " + extension);
  }
}
```

extension-point的配置提供了@ExtensionPoint注解的支持，使用方式如下：
```java
@ExtensionPoint("testAnnotationExtensionHandler")//指定扩展点名
@Component/如果没有开启component-scan，则需要在spring的xml中配置TestAnnotationExtensionHandler bean，开启方式见使用context:component-scan的介绍
public class AnoTestExtensionPoint {
}

```

## 插件
对于通过类加载器做隔离的场景，可能过插件来实现，比如：
* 复杂的库的接入，这种情况比较多，例如接入不同的中间件，每个中间件的依赖比较多，需要花大量精力处理包冲突等依赖问题，中间件升级困难
* 依赖的不同库的包冲突，例如不同中间件依赖了同一个兼容性差的库的不同版本
### 插件开发
* 工程结构：工程结构与普通的maven工程无异，建议最少分为以下两个子工程
    * api:应用中使用的插件提供的服务接口
    * core:插件的服务接口实现
    例如：
    ![modular-spring-plugin-project](docs/img/modular-spring-plugin-project.png)
* 插件打包：插件使用zip包的方式，需要将工程中的jar及其依赖的jar打到zip包中，zip包中包含一个名为${pluginName}的目录，打包可参考如下配置：
    * pom文件中的maven插件配置，各插件的作用这里不做细说
        ```xml
        <build>
            <plugins>
              <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.5.1</version>
                <configuration>
                  <compilerArgs>
                    <arg>-verbose</arg>
                    <arg>-Xlint:all,-options,-path</arg>
                  </compilerArgs>
                  <source>1.8</source>
                  <target>1.8</target>
                  <generatedSourcesDirectory>${project.build.directory}</generatedSourcesDirectory>
                </configuration>
              </plugin>
              <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <version>2.6</version>
                <executions>
                  <execution>
                    <id>copy-dependencies</id>
                    <phase>compile</phase>
                    <goals>
                      <goal>copy-dependencies</goal>
                    </goals>
                    <configuration>
                      <!-- ${project.build.directory}为Maven内置变量，缺省为target -->
                      <outputDirectory>${project.build.directory}/lib</outputDirectory>
                      <!-- 表示是否不包含间接依赖的包 -->
                      <excludeTransitive>false</excludeTransitive>
                      <!-- 表示复制的jar文件去掉版本信息 -->
                      <stripVersion>false</stripVersion>
                    </configuration>
                  </execution>
                  <execution>
                    <id>copy</id>
                    <phase>package</phase>
                    <goals>
                      <goal>copy</goal>
                    </goals>
                    <configuration>
                      <!-- ${project.build.directory}为Maven内置变量，缺省为target -->
                      <outputDirectory>${project.build.directory}/lib</outputDirectory>
                      <artifactItems>
                        <artifactItem>
                          <groupId>cn.yxffcode</groupId>
                          <artifactId>modular-spring-plugin-test-core</artifactId>
                        </artifactItem>
                      </artifactItems>
                    </configuration>
                  </execution>
                </executions>
              </plugin>
              <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <executions>
                  <execution>
                    <id>make-assembly</id>
                    <phase>package</phase>
                    <goals>
                      <goal>single</goal>
                    </goals>
                    <configuration>
                      <finalName>testplugin</finalName>
                      <descriptor>assembly/assembly.xml</descriptor><!--配置描述文件路径-->
                    </configuration>
                  </execution>
                </executions>
              </plugin>
            </plugins>
          </build>
        ```
    * assembly/assembly.xml文件:
        ```xml
        <assembly>
          <id></id>
          <formats>
            <format>zip</format>
          </formats>
          <includeBaseDirectory>true</includeBaseDirectory>
          <dependencySets>
            <dependencySet>
              <useProjectArtifact>true</useProjectArtifact>
              <outputDirectory>/</outputDirectory>
              <scope>runtime</scope>
            </dependencySet>
          </dependencySets>
          <fileSets>
            <fileSet>
              <directory>${project.build.directory}/lib</directory>
              <outputDirectory>/</outputDirectory>
            </fileSet>
          </fileSets>
        </assembly>
        ```
    * 插件配置META-INF/plugin.json文件:此文件可放在插件的core工程的resources目录下，其中的内容如下：
        ```json
        {
          "pluginName": "testplugin",
          "activator": "plugintest.impl.TestPluginActivator",
          "exportPackages": ["plugintest.api"]
        }
        ```
        * pluginName表示插件名
        * activator表示插件初始化入口，后面详细介绍
        * exportPackages表示导出的类的包，一般为api包及其依赖的包，这里有如下两种处理方式
            * 应用打包的时候，不将插件的api包打进去，应用可通过exportPackages使用插件中的服务
            * 应用打包时，将插件的api包打进去，插件打包时，不将自己的api包打进去，上面的例子使用的这种方式（maven-dependency-plugin插件没有复杂api工程打出来的jar）
* 应用中依赖插件，两种方式：
    * 在META-INF/plugin/${pluginName}.zip
    * classpath下的.msp文件（实际上是zip格式），可以打包成zip后，将zip部署到maven仓库并命名为${pluginName}.msp，应用中通过maven引用插件即可
* 插件初始化入口示例：
    * Activator的实现：
        ```java
        public class TestPluginActivator implements PluginActivator {
          public void onLoad() {
            PluginTools.registryBeanDefinitionParser(new TestBeanDefinitionParser());
          }
        
          public void onDestroy() {
          }
        }

        ```
    * spring的xml标签解析
        TestPluginActivator注册了一个TestBeanDefinitionParser，代码如下：
        ```java
        public class TestBeanDefinitionParser extends PluginDefBeanDefinitionParser {
          public String getTagName() {
            return "test";
          }
        
          public BeanDefinition parse(Element element, ParserContext parserContext) {
            final RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
            rootBeanDefinition.setBeanClass(TestServiceImpl.class);
            final String name = element.getAttribute("name");
            parserContext.registerBeanComponent(new BeanComponentDefinition(new BeanDefinitionHolder(rootBeanDefinition, name)));
            return null;
          }
        }
        ```
        PluginDefBeanDefinitionParser是一个抽象类，其getTagName方法返回xml标签名，这里是test，表示应用的spring中需要通过如下xml引用插件服务
        ```xml
        <modular:plugin>
            <test name="testPluginService"/>
        </modular:plugin>
        ```
        testPluginService是一个普通的spring-bean，可正常注入到其它bean中
## webmvc
### springmvc对controller的模块化
在web.xml中配置servlet
```xml
<servlet>
    <servlet-name>web</servlet-name>
    <servlet-class>cn.yxffcode.modularspring.webmvc.ModularDispatcherServlet</servlet-class>
    <init-param>
        <param-name>webModuleNamePrefix</param-name>
        <param-value>cn.yxffcode.test.web</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>web</servlet-name>
    <url-pattern>*.json</url-pattern>
</servlet-mapping>
```
约定名词模块的simpleName：假如controller所在的模块名为cn.xxx.web.home，
cn.xxx.web是在ModularDispatcherServlet中配置的webModuleNamePrefix,则模块的simpleName是home

配置后，controller可以在不同的模块中，参数webModuleNamePrefix表示所有controller的模块名的共同前缀,
请求的url和视图渲染会有一定的变化，url会自动加上模块的simpleName，例如:
```java
@RequestMapping("/")
@Controller
public class HomeController {

  @ModularReference
  private TestService testService;

  @RequestMapping(value = "index", method = RequestMethod.GET)
  public String index() {
    return "index.html";
  }
}
```
则index的正确访问方式是/home/index.json，而返回的"index.html"的路径是/home/view/index.html
### webmvc的公共ApplicationContext;
ModularDispatcherServlet会创建一个ApplicationContext用于配置各controller模块需要的公共组件。
比如可以将公共的视图渲染器或者文件上传的配置写在此公共的ApplicationContext中，使用方式很简单，给ModularDispatcherServlet
指定参数commonApplicationContext表示公共的applicationContext.xml的位置，如：
```xml
  <servlet>
    <servlet-name>web</servlet-name>
    <servlet-class>cn.yxffcode.modularspring.webmvc.ModularDispatcherServlet</servlet-class>
    <init-param>
      <param-name>webModuleNamePrefix</param-name>
      <param-value>cn.yxffcode.test.web</param-value>
    </init-param>
    <init-param>
      <param-name>commonApplicationContext</param-name>
      <param-value>/WEB-INF/applicationContext.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
  </servlet>
  <servlet-mapping>
    <servlet-name>web</servlet-name>
    <url-pattern>*.json</url-pattern>
  </servlet-mapping>
```

### 对拦截器的支持
controller层实现模块化后，springmvc支持的拦截器有两种使用形式
* 在各个模块中单独配置 
     
        这种实现方式会自动在拦截器的url前拼上/$simpleModuleName.
        
    例如在home模块中配置拦截器：
    ```xml
    <mvc:interceptors>
        <mvc:interceptor>
            <mvc:mapping path="/index.json"/>
            <bean class="cn.yxffcode.test.web.home.TestHandlerIntercepter"/>
        </mvc:interceptor>
    </mvc:interceptors>
    ```
    拦截器会拦截/home/index.json请求
* 在webmvc的公共组件容器中配置
        
        这种方式与非模块化的方式使用无异。
        
    例如在commonApplicationContext中配置拦截器：
    ```xml
      <mvc:interceptors>
        <mvc:interceptor>
          <mvc:mapping path="/home/index.json"/>
          <bean class="cn.yxffcode.test.TestHandlerIntercepter"/>
        </mvc:interceptor>
      </mvc:interceptors>
    ```
    
## spring-boot
对mvc模块化工程提供了spring-boot的支持，但是未完全实现基于注解的配置，还是需要xml。

使用方式：
```java
@ModularApplication
@ModularDispatcherConfig(webModuleNamePrefix = "cn.yxffcode.test.web")
@UrlPattern("*.json")
public class Application extends ModularServletInitializer {
  public static void main(String[] args) {
    final ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
  }
}
```
* @ModularApplication注解类似于@SpringBootApplication注解的功能
* @ModularDispatcherConfig配置模块化的springmvc工程
* @UrlPattern指定ModularDispatcherServlet拦截什么样的url，不配置则默认为"/"

## 事务管理器对多数据源的支持
当不同的模块需要使用不同的数据源时，可使用MultiDataSourcesTransactionManager作为事务管理器
```xml
  <bean name="dataSource1"
        class="org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy">
    <constructor-arg>
      <bean class="org.springframework.jdbc.datasource.DriverManagerDataSource"></bean>
    </constructor-arg>
  </bean>
  <bean name="dataSource2"
        class="org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy">
    <constructor-arg>
        <bean class="org.springframework.jdbc.datasource.DriverManagerDataSource"></bean>
    </constructor-arg>
  </bean>
  <bean name="transactionManager"
        class="cn.yxffcode.modularspring.tx.MultiDataSourcesTransactionManager">
    <property name="dataSources">
      <map>
        <entry key="dataSource1" value-ref="dataSource1"/>
        <entry key="dataSource2" value-ref="dataSource2"/>
      </map>
    </property>
  </bean>

  <tx:annotation-driven transaction-manager="transactionManager"/>
```
## mybatis
使用如下配置，可自动为mybatis映射接口创建模块间的服务绑定，spring与mybatis集成的其它配置不变，只需要将MapperScannerConfigurer修改为ModularSpringMapperScannerConfigurer
```xml
  <bean class="cn.yxffcode.modularspring.mybatis.ModularSpringMapperScannerConfigurer">
    <property name="createModularService" value="true"/>
    <property name="basePackage" value="cn.yxffcode.modularspring.mybatis"/>
    <property name="annotationClass" value="org.springframework.stereotype.Repository"/>
  </bean>
```
其它模块可通过服务引用的方式使用DAO模块中的映射接口
```xml
  <modular:reference name="testMapperRef" interface="cn.yxffcode.modularspring.mybatis.TestMapper"/>
```
也可以使用@ModularReference注解.
## http服务集成
modular-spring-http是对httpasyncclient的简单封装，用于发送http请求并将返回数据转换成对象，以mapper接口的方式使用http-client，应用代码只需要写个interface的声明即可

### 使用方式
声明mapper接口：
```java
public interface TestServiceFacade {

  /**
   * 异步调用
   */
  @Request("http://localhost:8080/home/index.json?name=#{name}&test=1")
  Future<JsonResult<TestBean>> get(@HttpParam("name") String name);

  /**
   * 同步调用
   */
  @Request("http://localhost:8080/home/index.json?name=#{name}")
  @POST
  JsonResult<TestBean> post(@HttpParam("name") String name);
}
```
说明：modular-spring-http默认使用fastjson做json反序列化，JsonResult可换成任意对象（json可转成对应的对象类型）

初始化接口（后文会有与Spring集成的方式）：
```java
public void handleInvocation() throws Exception {

    final Configuration configuration = Configuration.newBuilder()
        .parse(TestServiceFacade.class)
        .build();
    
    final TestServiceFacade testServiceFacade = configuration.newMapper(TestServiceFacade.class);
    JsonResult<TestBean> result = testServiceFacade.get("name").get();
    System.out.println(result);
    
    result = testServiceFacade.post("name").get();
    System.out.println(result);

}

```
http请求返回的内容在默认情况下会使用FastJson做反序列化，支持自定义反序列化方式（见下文）

异步调用还可以用ResponseCallback接口，例如：
```java
public interface TestServiceFacade {

  /**
   * 异步调用，通过回调的方式
   */
  @Request("http://localhost:8080/home/index.json?name=#{name}&test=1")
  void getString(@HttpParam("name") String name, ResponseCallback<JsonResult<TestBean>> callback);
}

```
### 设置post请求的HttpEntity
@POST注解中的entity属性可指定使用哪种entity，目前支持，FORM, JSON_STRING和SERIALIZER
三种，如果需要自定义，可以使用ReqeuestPostProcessor（下文会有介绍）
### 自定义反序列化HttpResponse
#### 设置默认的反序列化方式
可使用Configuration.setDefaultResponseHandler()方法设置默认反序列化，例如：
```java
configuration.setDefaultResponseHandler(new FastJsonResponseHandler());
```
或者：
```java
configuration.setDefaultResponseHandler(new ToStringResponseHandler());
```
#### 为请求定制ResponseHandler
可使用@Response注解为请求定制ResponseHandler，@Response注解提供两种使用方式：
* 将@Response标记在接口上，表示此接口默认使用的ResponseHandler，例如：
```java
@Response(FastJsonResponseHandler.class)
public interface TestServiceFacade {
}
```
* 将@Response注解标记在方法上，表示此方法使用的ResponseHandler，例如：
```java
public interface TestServiceFacade {
  
  @Request("http://localhost:8080/home/index.json?name=#{name}&test=1")
  @Response(ToStringResponseHandler.class)
  String getString(@HttpParam("name") String name);
}

```
两种方式可同时使用:
```java
@Response(FastJsonResponseHandler.class)
public interface TestServiceFacade {

  @Request("http://localhost:8080/home/index.json?name=#{name}&test=1")
  @PostProcessors({KeepHeaderPostProcessor.class})
  Future<JsonResult<TestBean>> get(@HttpParam("name") String name);

  @Request("http://localhost:8080/home/index.json?name=#{name}&test=1")
  @Response(ToStringResponseHandler.class)
  String getString(@HttpParam("name") String name);

  @Request("http://localhost:8080/home/index.json?name=#{name}")
  @POST
  JsonResult<TestBean> post(@HttpParam("name") String name);

}

```
如果即没有在接口上标记@Response注解，也没有在方法上标记@Response注解，则使用Configuration.getDefaultResponseHandler()
### 定制HttpClient
可以调用Configuration.setHttpClientFactory()方法提供一个HttpClientFactory
来替代默认的DefaultHttpClientFactory，用于定制HttpClient对象

### 使用RequestPostProcessor
RequestPostProcessor可对请求进行拦截，接口定义如下：
```java
public interface RequestPostProcessor {
  boolean postProcessRequest(HttpUriRequest request, MappedRequest mr, Map<String, Object> params);

  void postProcessResponse(HttpResponse response, MappedRequest mr);
}

```
在RequestPostProcessor中可对HttpRequest和HttpResponse做任何的定制.
例如：
```java
public class KeepHeaderPostProcessor implements RequestPostProcessor {

  @Override
  public boolean postProcessRequest(HttpUriRequest request, MappedRequest mr, Map<String, Object> params) {
    request.addHeader("testHeader", "testHeader");
    return true;
  }

  @Override
  public void postProcessResponse(HttpResponse response, MappedRequest mr) {
  }
}

```
有了RequestPostProcessor后，可以通过@PostProcessors在mapper接口上使用，三种级别的使用，

1.全局的RequestPostProcessor

使用Configuration设置全局的RequestPostProcessor，全局的RequestPostProcessor对所有请求有效

2.接口级别的RequestPostProcessor

将@RequestPostProcessors标记在接口上
```java
@PostProcessors({KeepHeaderPostProcessor.class})
public interface TestServiceFacade {

  /**
   * 异步调用
   */
  @Request("http://localhost:8080/home/index.json?name=#{name}&test=1")
  Future<JsonResult<TestBean>> get(@HttpParam("name") String name);

  /**
   * 同步调用
   */
  @Request("http://localhost:8080/home/index.json?name=#{name}")
  @POST
  JsonResult<TestBean> post(@HttpParam("name") String name);
}
```

2.方法级别的RequestPostProcessor

将@RequestPostProcessors标记在方法上
```java
public interface TestServiceFacade {

  @Request("http://localhost:8080/home/index.json?name=#{name}&test=1")
  @PostProcessors({KeepHeaderPostProcessor.class})
  Future<JsonResult<TestBean>> get(@HttpParam("name") String name);

  @Request("http://localhost:8080/home/index.json?name=#{name}")
  @POST
  Future<JsonResult<TestBean>> post(@HttpParam("name") String name);
}

```
一个mapper接口的方法在发送请求时使用的RequestPostProcessor为：
全局 + 接口级别 + 方法级别

### 与Spring集成
#### xml配置方式
在Spring中添加配置:
```xml
  <bean class="cn.yxffcode.modularspring.http.HttpMapperAutoConfigurer">
    <property name="basePackages">
      <array>
        <value>cn.yxffcode.xxx</value>
      </array>
    </property>
    <!--是否创建modular-spring的模块服务，默认不创建-->
    <property name="createModularService" value="true"/>
    <property name="annotation" value="org.springframework.stereotype.Component"/>
  </bean>
```
可配置DefaultResponseHandler, 全局的RequestPostProcessors以及HttpClientFactory
```xml
  <bean class="cn.yxffcode.modularspring.http.HttpMapperAutoConfigurer">
    <property name="basePackages">
      <array>
        <value>cn.yxffcode.xxx</value>
      </array>
    </property>
    <property name="annotation" value="cn.yxffcode.modularspring.http.HttpMapper"/>

    <!--以下是可选-->
    <property name="createModularService" value="true"/>
    <property name="commonRequestPostProcessors">
      <list>
        <ref bean="keepHeaderPostProcessor"/>
      </list>
    </property>
    <property name="defaultResponseHandler" ref="fastJsonResponseHandler"/>
    <property name="httpClientFactory" ref="defaultHttpClientFactory"/>
  </bean>
```
#### 注解方式
使用@EnableModularSpringHttp，使用之前要先让spring支持注解配置：
```xml
<context:annotation-config/>
```
```java
@EnableModularSpringHttp(
    basePackages = {"com.xxx.xxx"},
    createModularService = true
)
public class Configure {
}

```
## 模块化工程结构参考

## 实现细节
### 如何做到按照模块加载

#### 按模块获取spring配置
在初始化每个模块的Spring时，需要保证每个模块只加载此模块自己的spring配置，那么创建Spring
依赖注入容器的步骤应该是：

* 获取META-INF/spring/目录下的所有xml文件
* 通过上一步中获取的xml文件

在modular-spring中，各模块之间通过Spring的ApplicationContext做隔离，但ClassLoader
没有隔离。因为模块中的spring配置文件不需要（也不能）通过配置的方式指定，所以加载时需要识别
出类路径下哪些配置属于一个模块，因此，在初始化spring时，不能通过spring提供的ResourceLoader
加载spring配置，因为ResourceLoader会将类路径下所有spring的配置取出，而Resource名都是
以META-INF/spring开头，无法区分出每个模块的配置，可以使用ClassLoader遍历类路径，通过路径
来判断哪些配置属于同一个模块：
```java
private void scanDirectory(
          File directory, ClassLoader classloader, String packagePrefix,
          ImmutableSet<File> ancestors, Predicate<String> accepter) throws IOException {
    File canonical = directory.getCanonicalFile();
    if (ancestors.contains(canonical)) {
      // A cycle in the filesystem, for example due to a symbolic link.
      return;
    }
    File[] files = directory.listFiles();
    if (files == null) {
      logger.warn("Cannot read directory {}", directory);
      // IO error, just skip the directory
      return;
    }
    ImmutableSet<File> newAncestors = ImmutableSet.<File>builder()
            .addAll(ancestors)
            .add(canonical)
            .build();
    for (File f : files) {
      String name = f.getName();
      if (f.isDirectory()) {
        scanDirectory(f, classloader, packagePrefix + name + "/", newAncestors, accepter);
      } else {
        String resourceName = packagePrefix + name;
        if (!resourceName.equals(JarFile.MANIFEST_NAME) && accepter.apply(resourceName)) {
          resources.add(ResourceInfo.of("file:" + f.getAbsolutePath(), classloader));
        }
      }
    }
  }

  private void scanJar(File file, ClassLoader classloader, Predicate<String> acceptor) throws IOException {
    JarFile jarFile;
    try {
      jarFile = new JarFile(file);
    } catch (IOException e) {
      // Not a jar file
      return;
    }
    try {
      for (URI uri : getClassPathFromManifest(file, jarFile.getManifest())) {
        scan(uri, classloader, acceptor);
      }
      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if (entry.isDirectory() || entry.getName().equals(JarFile.MANIFEST_NAME)) {
          continue;
        }
        if (acceptor.apply(entry.getName())) {
          resources.add(ResourceInfo.of("jar:" + jarFile.getName() + "!/" + entry.getName(), classloader));
        }
      }
    } finally {
      try {
        jarFile.close();
      } catch (IOException ignored) {
      }
    }
  }
```
详细代码见ClasspathScanner类。

#### 按模块初始化Spring
获取到每个模块的spring配置后，按照每个模块加载配置，创建ApplicationContext对象。
但是获取到的spring配置有以下两种存在形式：
* 存在于一个目录中，比如在本地运行测试代码时，工程目录下的配置
* 存在于jar包中，模块部署前会被打成jar

对于这两种形式的配置，读取的方式不一样，可扩展spring的AbstractXmlApplicationContext
来读取不同存在形式的配置，例如读取目录中的配置：
```java
public class ModuleFileSystemApplicationContext extends AbstractModuleApplicationContext {
  @Override
  protected Resource getResourceByPath(String path) {
    if (path != null && path.startsWith("/")) {
      path = path.substring(1);
    }
    return new FileSystemResource(path);
  }
}
```
读取jar中的配置创建ApplicationContext
```java
public class ModuleJarEntryXmlApplicationContext extends AbstractModuleApplicationContext {
  @Override
  protected Resource getResourceByPath(String path) {
    return new JarEntryResource(path);
  }
}
```
AbstractXmlApplicationContext的作用后文再提.

### 模块加载的勾子

#### 为什么需要勾子
框架本身不可能满足所有场景，想要做到不修改框架本身的代码也能满足很多特征需求，可以提供
勾子在框架的核心流程中做拦截操作。例如mybatis的拦截器，spring的PostBeanProcessor。
#### modular-spring中的勾子接口
modular-spring提供了以下两个勾子接口:
```java
public interface ModuleLoadListener {

  /**
   * 在调用{@link ModuleApplicationContext#refresh()}前调用
   */
  void beforeModuleLoad(ModuleConfig moduleConfig, ModuleApplicationContext applicationContext);

  /**
   * 在调用{@link ModuleApplicationContext#refresh()}后调用
   */
  void afterModuleLoad(ModuleConfig moduleConfig, ModuleApplicationContext applicationContext);
}

```
```java
public interface ApplicationStartupCallback {
  void action();
}
```
ModuleLoadListener提供了在一个模块初始化前和初始化后的处理逻辑的调用能力，而ApplicationStartupCallback提供了应用启动后的回调能力。
### 如何实现模块中的服务接口的发布与引用
#### 基本思想
因为模块之间的ApplicationContext之间相互隔离，所以一个模块发布的服务不能被直接注入
到另一个模块的bean中，面服务的发布入引用的模块之间可能无依赖关系，这意味着服务引用方
初始化时，服务发布方可能还没有初始化，所以需要在服务引用方使用代理来达到目的。

如何使用代理达到跨模块之间的服务引用呢？通过modular:service标签在spring中配置的服务
接口会被一个全局的ServiceManager托管，而通过modular:reference标签在spring中配置
的服务引用会通过动态代理创建一个代理对象，并被spring注入到需要使用此服务的bean中，初始
时代理对象中的目标对象（即服务提供对象）是null，当第一次在代理方法上执行方法调用时，代理
对象会从ServiceManager中获取服务提供对象

#### modular命名空间的xml配置的实现方式
spring支持自定义命名空间，每个命名空间对应一个xsd文件和一个NamespaceHandler，如果
需要自定义命名空间则需要按照spring的规范提供xsd文件和NamespaceHandler的实现。例如
modular-spring-core中的自定义标签实现：

![modular-spring-core-xmlns](docs/img/modular-spring-core-xmlns.png)

* modular.xds文件中是对modular:service和modular:reference标签的定义
* spring.handlers中是命名空间和NamespaceHandler之间的对应关系
* spring.schemas中是命名空间对应的xds文件路径与实际路径之间的关系

spring.handlers中的内容：
```properties
http\://www.yxffcode.cn/modular=cn.yxffcode.modularspring.core.config.ModularNamespaceHandler
```
spring.schemas中的内容:
```properties
http\://www.yxffcode.cn/modular.xsd=META-INF/modular.xsd
```

说完了spring对自定义xmlns的支持，再来看看具体的标签解析.

在Spring中，一个bean会被装配成一个BeanDefinition对象，BeanDefinition对象是对一个bean
的描述，包括bean的类型，bean的依赖等。而xmlns中，一个标签对应BeanDefinitionParser，用于
向Spring中注册BeanDefinition对象。BeanDefinitionParser对象通过NamespaceHandler注册。
例如ModularNamespaceHandler:
```java
public class ModularNamespaceHandler extends NamespaceHandlerSupport {
  @Override
  public void init() {
    registerBeanDefinitionParser("service", new ServiceBeanDefinitionParser());
    registerBeanDefinitionParser("reference", new ReferenceBeanDefinitionParser());
    registerBeanDefinitionParser("extension", new ExtensionBeanDefinitionParser());
    registerBeanDefinitionParser("extension-point", new ExtensionPointBeanDefinitionParser());
  }
}
```
具体的BeanDefinition对象的注册逻辑可以看看上面的几个BeanDefinitionParser的实现类。

#### 服务的发布与引用的实现
modular:service标签会向spring中注册一个ServiceBean对象，ServiceBean的部分代码如下:
```java
public class ServiceBean implements ApplicationContextAware {
  private String ref;
  private String interfaceName;
  private String uniqueId;
  private ApplicationContext applicationContext;

  public ServiceBean(String beanRef, String interfaceName, String uniqueId) {
    this.interfaceName = interfaceName;
    this.uniqueId = Strings.nullToEmpty(uniqueId);
    this.ref = beanRef;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
    ServiceManager.registryService(this);
  }
}
```
可以看到，在模块的Spring初始化后，会向ServiceManager中注册一个ServiceBean

modular:reference标签则是通过向spring注册一个代理来实现服务的引用，部分代码如下:
```java
public final class ServiceReference implements FactoryBean<Object> {
  private final Class<?> targetClass;
  private final String uniqueId;

  public ServiceReference(Class<?> targetClass, String uniqueId) {
    this.targetClass = targetClass;
    this.uniqueId = Strings.nullToEmpty(uniqueId);
  }

  @Override
  public Object getObject() throws Exception {
    return Reflection.newProxy(targetClass, new AbstractInvocationHandler() {
      private Object delegate;

      private void initDelegate() {
        final ServiceBean service = ServiceManager.getService(targetClass.getName(), uniqueId);
        if (service == null) {
          throw new ServiceLocatingException("服务 " + targetClass + " 没有找到,请检查是否是模块依赖不正确");
        }
        final ApplicationContext ctx = service.getApplicationContext();
        this.delegate = ctx.getBean(service.getRef());
      }

      @Override
      protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        if (delegate == null) {
          synchronized (this) {
            if (delegate == null) {
              initDelegate();
            }
          }
        }
        return method.invoke(delegate, args);
      }
    });
  }
}

```
ServiceReference是一个FactoryBean，Spring在初始化服务引用对象时，会调用ServiceReference.getObject()
方法获取一个动态代理对象，可以看到在代理对象上第一次调用方法时会从ServiceManager中获取真实的服务对象。
### 如何实现注解方式的服务发布与引用
在`如何实现模块中的服务接口的发布与引用`中讲述了通过自定义xmlns的方式支持发布与引用服务，
以注解的形式发布与引用服务重用了`如何实现模块中的服务接口的发布与引用`中提到的ServiceBean
和ServiceReference这两个类，只不过注册到spring中的方式发生了变化.

#### 注解配置bean与注入依赖对象的原理与实践
使用过spring的都知道context:component-scan标签可以让spring支持通过注解的方式配置，常用
的注解有@Component, @Service, @Repository等，那么spring如何实现注解的注入呢？可以通过
ContextNamespaceHandler找到，spring通过ClassPathBeanDefinitionScanner,ClassPathBeanDefinitionScanner
类会扫描标记了@Component的类，同时如果一个类上面标记了使用@Component作为元注解的注解，则此
类也会被注册到spring中，例如@Service注解的源码：
```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Service {
	String value() default "";
}
```
可以看到@Service被@Component标记了。那么，可以实现注解@ModularService：
```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ModularService {
  String value() default "";

  Class<?> interfaceClass() default Object.class;
}

```
则使用@ModularService标记的类也会被注册到Spring中，但是还有一个问题，只配置了bean还不够，
还需要向Spring中注册ServiceBean来暴露服务，回过头来看看AbstractModuleApplicationContext
的实现（此处为部分代码，完整代码可打开类文件查看）：
```java
abstract class AbstractModuleApplicationContext extends AbstractXmlApplicationContext implements ModuleApplicationContext {
  @Override
  protected final void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
    // Create a new XmlBeanDefinitionReader for the given BeanFactory.
    final XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(new ModularBeanDefinitionRegistry(beanFactory));

    beanDefinitionReader.setEnvironment(this.getEnvironment());
    beanDefinitionReader.setResourceLoader(this);
    beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

    initBeanDefinitionReader(beanDefinitionReader);
    loadBeanDefinitions(beanDefinitionReader);
  }
}
```
可以看到，在读取xml时使用了ModularBeanDefinitionRegistry类，此类的作用则是处理@ModularService的服务暴露和@ModularReference的服务引用:
```java

  private void registerServiceBeanIfNeed(String beanName, Class<?> beanClass) {
    final ModularService modularService = beanClass.getDeclaredAnnotation(ModularService.class);
    if (modularService != null) {
      Class<?> serviceInterface = modularService.interfaceClass();
      if (serviceInterface == null || serviceInterface == Object.class) {
        final Class<?>[] interfaces = beanClass.getInterfaces();
        if (ArrayUtils.isEmpty(interfaces)) {
          throw new BeanDefinitionStoreException("服务定义出错",
                  new ServiceDeclarationException("modular service定义异常,bean 没有实现接口,bean的类型:" + serviceInterface.getName()));
        }
        serviceInterface = interfaces[0];
      }
      final String uniqueId = modularService.uniqueId();
      final RootBeanDefinition rootBeanDefinition = ModularBeanUtils.buildServiceBean(beanName, serviceInterface.getName(), uniqueId);
      delegate.registerBeanDefinition(beanNameGenerator.generateBeanName(rootBeanDefinition, delegate), rootBeanDefinition);
    }
  }

  private void registerServiceReferenceIfNeed(Class<?> beanClass) {
    final Field[] declaredFields = beanClass.getDeclaredFields();
    if (ArrayUtils.isEmpty(declaredFields)) {
      return;
    }

    for (Field field : declaredFields) {
      final ModularReference modularReference = field.getDeclaredAnnotation(ModularReference.class);
      if (modularReference == null) {
        continue;
      }
      final Class<?> type = field.getType();
      final String referenceBeanName = getReferenceBeanName(type);
      if (delegate.containsBeanDefinition(referenceBeanName)) {
        continue;
      }
      final RootBeanDefinition rootBeanDefinition = ModularBeanUtils.buildReferenceBean(type, modularReference.uniqueId());
      delegate.registerBeanDefinition(referenceBeanName, rootBeanDefinition);
    }
  }

```
通过反射获取@ModularService和@ModularReference，并注册,具体见代码文件。但是这里还有一个问题，
如何对标记了@ModularReference的属性实现@Autowired的自动注入的功能呢，这里需要先了解一下@Autowired
的实现原理：

`Spring提供了初始化过程中的各种勾子，可以达到很多自定义功能的目的，比如BeanPostProcessor, BeanFactoryPostProcessor等，
而@Autowired注解是由AutowiredAnnotationBeanPostProcessor处理，它是Spring的勾子的一个实现，对于@ModularReference的
自动注入，我这里为了方便以后对@ModularReference的定制化，实现了ModularReferenceInjectProcessor`

ModularReferenceInjectProcessor目前非常简单，没有任何逻辑，后继对@ModularReference做功能加强时会增加其它逻辑:
```java
public class ModularReferenceInjectProcessor extends AutowiredAnnotationBeanPostProcessor {
  public ModularReferenceInjectProcessor() {
    super();
    setAutowiredAnnotationType(ModularReference.class);
  }
}
```
然后回到上面的ModularBeanDefinitionRegistry类的构造器:
```java
  public ModularBeanDefinitionRegistry(BeanDefinitionRegistry delegate) {
    this.delegate = checkNotNull(delegate);
    final RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(ModularReferenceInjectProcessor.class);
    delegate.registerBeanDefinition(ModularBeanDefinitionRegistry.class.getName(), rootBeanDefinition);
  }
```
将ModularReferenceInjectProcessor注册到Spring中即可。
### 对springmvc的支持
#### 需要解决的问题
需要解决的问题主要是url的mapping，例如用户请求/home/xxx/index.json，因为web模块中，
Controller不感知web模块名，需要框架自动将请求路由到home这个web模块中的的Controller上。
#### 定制HandlerMapping
Spring通过HandlerMapping来决定Controller中的每个方法对应的请求url，modular-spring-webmvc
实现了ModuleRequestMappingHandlerMapping，用于自动将Controller的RequestMapping前
加上模块名，核心代码:
```java
  protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType, String webModuleSimpleName) {
    final RequestMappingInfo requestMappingInfo = createRequestMappingInfo(new ModuleRequestMapping(webModuleSimpleName), null);
    return requestMappingInfo.combine(super.getMappingForMethod(method, handlerType));
  }
```
这里用到了注解的本质：注解就是接口，@interface就是interface。
```java
class ModuleRequestMapping implements RequestMapping {
  @Override
  public Class<? extends Annotation> annotationType() {
    return RequestMapping.class;
  }
}
```
上面是ModuleRequestMapping的核心代码，具体代码见代码文件。ModuleRequestMapping实现了
RequestMapping这个注解，而真正起作用的是initHandlerMethods方法，会将每个web模块中的
Controller与url关联上:
```java
protected final void initHandlerMethods() {
  if (logger.isDebugEnabled()) {
    logger.debug("Looking for request mappings in application context: " + getApplicationContext());
  }

  final String[] beanNames = getBeanNames(getApplicationContext());

  for (String beanName : beanNames) {
    if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX) &&
        isHandler(getApplicationContext().getType(beanName))) {
      detectHandlerMethods(beanName);
    } else {
      final Class<?> type = getApplicationContext().getType(beanName);
      if (!ApplicationContext.class.isAssignableFrom(type)) {
        continue;
      }
      final ApplicationContext ctx = getApplicationContext().getBean(beanName, ApplicationContext.class);
      currentApplicationContext = ctx;
      final String[] moduleBeanNames = getBeanNames(ctx);
      for (String moduleBeanName : moduleBeanNames) {
        if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX) &&
            isHandler(ctx.getType(moduleBeanName))) {
          detectHandlerMethods(moduleBeanName, beanName);
        }
      }
      currentApplicationContext = null;
    }
  }
  handlerMethodsInitialized(getHandlerMethods());
}
```
#### 定制DispatcherServlet
有了HandlerMapping后，还需要定制DispatcherServlet。

因为原始的DispatcherServlet需要一个WebApplicationContext，但按照模块加载后，每个web模块
都有一个WebApplicationContext，所以这里需要为DispatcherServlet实现一个facade来管理每个
web模块的ApplicationContext。

ModularDispatcherServlet类扩展自DispatcherServlet,并通过GenericWebApplicationContext
管理每个web模块的ApplicationContext，核心代码:
```java
private static class WebModuleRegistryListener implements ModuleLoadListener {
    private final WebModulePredicate webModulePredicate;
    private final GenericWebApplicationContext wac;
    private final WebApplicationContext commonApplicationContext;

    public WebModuleRegistryListener(WebModulePredicate webModulePredicate, GenericWebApplicationContext wac,
                                     WebApplicationContext commonApplicationContext) {
      this.commonApplicationContext = commonApplicationContext;
      this.webModulePredicate = webModulePredicate;
      this.wac = wac;
    }

    @Override
    public void beforeModuleLoad(ModuleConfig moduleConfig, ModuleApplicationContext applicationContext) {
      if (!webModulePredicate.apply(moduleConfig)) {
        return;
      }
      if (commonApplicationContext != null) {
        applicationContext.setParent(commonApplicationContext);
      }
    }

    @Override
    public void afterModuleLoad(ModuleConfig moduleConfig, ModuleApplicationContext applicationContext) {
      if (!webModulePredicate.apply(moduleConfig)) {
        return;
      }
      final String moduleName = moduleConfig.getModuleName();
      final int i = moduleName.lastIndexOf('.');
      final String contextBeanName = i < 0 ? moduleName : moduleName.substring(i + 1);
      if (StringUtils.isBlank(contextBeanName)) {
        throw new IllegalStateException("web模块的模块名不合法, 模块名:" + moduleName);
      }
      final RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
      rootBeanDefinition.setBeanClass(ModuleApplicationContextBean.class);
      rootBeanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, applicationContext);
      wac.registerBeanDefinition(contextBeanName, rootBeanDefinition);
    }
  }
```
这里使用到了前文提到的ModuleLoadListener
