# 对springmvc的支持
## 需要解决的问题
需要解决的问题主要是url的mapping，例如用户请求/home/xxx/index.json，因为web模块中，
Controller不感知web模块名，需要框架自动将请求路由到home这个web模块中的的Controller上。
## 定制HandlerMapping
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
## 定制DispatcherServlet
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