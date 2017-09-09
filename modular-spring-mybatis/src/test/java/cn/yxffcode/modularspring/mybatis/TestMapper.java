package cn.yxffcode.modularspring.mybatis;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author gaohang on 9/10/17.
 */
@Repository
public interface TestMapper {

  @Select("select count(*) from user_info")
  int count();

}
