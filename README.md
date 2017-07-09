# modular-spring
利用spring实现工程的模块化

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
### 模块加载的前置处理和后置处理
通过ModuleLoadListener接口可以对模块加载做前置处理或者后置处理,例如想要在

### 后续计划
* 检测模块之间的环形依赖
* 模块支持扩展点,比如数据访问模块依赖的数据源在拼装系统时,由系统的主模块指定
* 扩展springmvc,支持controller的模块化