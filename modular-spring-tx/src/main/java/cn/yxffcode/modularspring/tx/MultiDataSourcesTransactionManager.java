package cn.yxffcode.modularspring.tx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

/**
 * To hold all db connections' lazy wrapper object,a target connection cannot be hold unless execute
 * sql on DAO,so we needn't care about resources wastes.{@link SimpleTransactionManager}s are
 * included by this class.
 * <p/>
 * Concentrate on the data binds, all data are based on ThreadLocal,but some have binding objects.
 * DataSource independent resources should be unbind specially by {@link SimpleTransactionManager}
 * such as unbind the resources related to the DataSource and release the connection.
 * {@link SimpleTransactionManager#doCleanupAfterCompletion(Object)} unbinding the each resource
 * DataSource and common resources are been unbind automatically by
 * {@link AbstractPlatformTransactionManager#cleanupAfterCompletion(DefaultTransactionStatus)}
 *
 * @author gaohang on 15/12/28.
 */
public class MultiDataSourcesTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(MultiDataSourcesTransactionManager.class);

  private List<PlatformTransactionManager> transactionManagers = new ArrayList<>();

  private Map<String, DataSource> dataSources = Collections.emptyMap();

  protected Object doGetTransaction() throws TransactionException {
    TransactionHolder transactionHolder =
        (TransactionHolder) TransactionSynchronizationManager.getResource(dataSources);
    return transactionHolder == null ? new TransactionObject(true, new TransactionHolder()) :
        new TransactionObject(false, transactionHolder);
  }

  @Override
  protected void doBegin(Object transactionObject, TransactionDefinition transactionDefinition)
      throws TransactionException {

    TransactionObject tx = (TransactionObject) transactionObject;
    if (tx.transactionHolder.statuses == null) {
      tx.transactionHolder.statuses = new ArrayList<>(transactionManagers.size());
    }
    for (PlatformTransactionManager transactionManager : transactionManagers) {
      DefaultTransactionStatus element =
          (DefaultTransactionStatus) transactionManager.getTransaction(transactionDefinition);
      tx.transactionHolder.statuses.add(element);
    }
    tx.transactionHolder.transactionInActive = true;
    if (tx.newTransaction) {
      TransactionSynchronizationManager.bindResource(dataSources, tx.transactionHolder);
    }
  }

  protected void prepareSynchronization(DefaultTransactionStatus status,
                                        TransactionDefinition definition) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      super.prepareSynchronization(status, definition);
    }
  }

  @Override protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
    @SuppressWarnings("unchecked") TransactionObject tx =
        (TransactionObject) status.getTransaction();
    checkState(tx.transactionHolder.statuses != null &&
        tx.transactionHolder.statuses.size() <= this.getTransactionManagers().size());

    TransactionException lastException = null;
    for (int i = tx.transactionHolder.statuses.size() - 1; i >= 0; i--) {
      PlatformTransactionManager transactionManager = this.getTransactionManagers().get(i);
      TransactionStatus localTransactionStatus = tx.transactionHolder.statuses.get(i);

      try {
        transactionManager.commit(localTransactionStatus);
      } catch (TransactionException e) {
        lastException = e;
        LOGGER.error("Error in commit", e);

      }
    }
    if (lastException != null) {
      throw lastException;
    }

  }

  @Override protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
    @SuppressWarnings("unchecked") TransactionObject tx =
        (TransactionObject) status.getTransaction();

    checkState(tx.transactionHolder.statuses != null &&
        tx.transactionHolder.statuses.size() <= this.getTransactionManagers().size());

    TransactionException lastException = null;
    for (int i = tx.transactionHolder.statuses.size() - 1; i >= 0; i--) {
      PlatformTransactionManager transactionManager = this.getTransactionManagers().get(i);
      TransactionStatus localTransactionStatus = tx.transactionHolder.statuses.get(i);

      try {
        transactionManager.rollback(localTransactionStatus);
      } catch (TransactionException e) {
        // Log exception and try to complete rollback
        lastException = e;
        LOGGER.error("error occured when rolling back the transaction. \n{}", e);
      }
    }

    if (lastException != null) {
      throw lastException;
    }
  }

  @Override protected boolean isExistingTransaction(Object transaction)
      throws TransactionException {
    TransactionObject tx = (TransactionObject) transaction;
    return tx.transactionHolder.statuses != null && tx.transactionHolder.transactionInActive;
  }

  @Override protected void doCleanupAfterCompletion(Object transaction) {
    //unbind resource.
    TransactionObject tx = (TransactionObject) transaction;
    if (tx.newTransaction) {
      TransactionSynchronizationManager.unbindResource(dataSources);
    }
  }

  public void afterPropertiesSet() throws Exception {
    for (DataSource dataSource : getDataSources()) {
      PlatformTransactionManager txManager = this.createTransactionManager(dataSource);
      getTransactionManagers().add(txManager);
    }
  }

  private Iterable<DataSource> getDataSources() {
    return dataSources.values();
  }

  public void setDataSources(Map<String, DataSource> dataSources) {
    this.dataSources = dataSources;
  }

  protected PlatformTransactionManager createTransactionManager(DataSource dataSource) {
    return new SimpleTransactionManager(dataSource);
  }

  public List<PlatformTransactionManager> getTransactionManagers() {
    return transactionManagers;
  }

  /**
   * 如果两个TransactionObject的transactionHolder指向同一个对象,表示有事务的嵌套
   */
  private static final class TransactionObject {
    private final boolean newTransaction;
    private final TransactionHolder transactionHolder;

    public TransactionObject(boolean newTransaction, TransactionHolder transactionHolder) {
      this.newTransaction = newTransaction;
      this.transactionHolder = transactionHolder;
    }
  }


  private static final class TransactionHolder {
    private List<TransactionStatus> statuses;
    private boolean transactionInActive;
  }
}
