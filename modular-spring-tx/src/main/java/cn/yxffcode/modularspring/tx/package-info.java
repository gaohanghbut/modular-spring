/**
 * 对Spring的事务管理器的扩展,用于需要同时打开多个数据库连接的场景.
 * {@link cn.yxffcode.modularspring.tx.SimpleTransactionManager}作为
 * Spring中提供的{@link org.springframework.jdbc.datasource.DataSourceTransactionManager}类
 * 的替代,{@link cn.yxffcode.modularspring.tx.MultiDataSourcesTransactionManager}
 * 作为多个数据库的事务管理器的包装.
 * <p/>
 * 在一次完整的事务中,
 * mybatis会会在{@link org.springframework.transaction.support.TransactionSynchronizationManager}
 * 中绑定一个{@link org.springframework.transaction.support.TransactionSynchronization}对象,同时,
 * Connection对象会与DataSource绑定,在需要打开多个数据库的场景下,TransactionSynchronization应该在
 * MultiDataSourcesTransactionManager中做处理,Connection需要在SimpleTransactionManager中做处理,保证
 * 多个DB的连接关闭后才执行并清理TransactionSynchronization
 * <p/>
 *
 * @author gaohang on 9/3/17.
 */
package cn.yxffcode.modularspring.tx;