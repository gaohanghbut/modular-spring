package cn.yxffcode.modularspring.core;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author gaohang on 6/24/17.
 */
final class ServiceManager {
  private ServiceManager() {
  }

  private static final Table<String, String, ServiceBean> serviceBeanTable = HashBasedTable.create();

  public static void registryService(ServiceBean serviceBean) {
    checkNotNull(serviceBean);
    serviceBeanTable.put(serviceBean.getUniqueId(), serviceBean.getInterfaceName(), serviceBean);
  }

  public static ServiceBean getService(String interfaceName, String uniqueId) {
    return serviceBeanTable.get(uniqueId, interfaceName);
  }


}
