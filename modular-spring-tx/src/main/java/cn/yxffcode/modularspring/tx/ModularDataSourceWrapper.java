package cn.yxffcode.modularspring.tx;

import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * 事务管理器通过DataSource作为key绑定事务，所以这里
 * 覆盖hashcode和tostring方法，保证各模块中一致，防止
 * 因为代理对象导致跨连接访问DB
 *
 * @author gaohang
 */
public class ModularDataSourceWrapper extends DelegatingDataSource {
  private DataSource target;

  public ModularDataSourceWrapper(DataSource target) {
    super(target);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ModularDataSourceWrapper that = (ModularDataSourceWrapper) o;
    return Objects.equals(target, that.target);
  }

  @Override
  public int hashCode() {
    return Objects.hash(target);
  }
}
