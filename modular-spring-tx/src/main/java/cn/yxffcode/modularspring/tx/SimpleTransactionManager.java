package cn.yxffcode.modularspring.tx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.InvalidTimeoutException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * As the same as {@link org.springframework.jdbc.datasource.DataSourceTransactionManager},but
 * SimpleTransactionManager cannot invoke TransactionSynchronizations.TransactionSynchronizations
 * are been invoked actually in {@link MultiDataSourcesTransactionManager}
 * <p/>
 * Do not to extend from {@link org.springframework.transaction.support.AbstractPlatformTransactionManager}
 * because of the TransactionSynchronizations will be invoke before or after commit/rollback methods
 * and exception will be thrown when MultiDataSourcesTransactionManager commit on the next
 * PlatformTransactionManager
 *
 * @author gaohang on 15/12/31.
 */
class SimpleTransactionManager implements PlatformTransactionManager {
  private static final Logger logger = LoggerFactory.getLogger(SimpleTransactionManager.class);

  private final DataSource dataSource;
  private boolean nestedTransactionAllowed = false;
  private boolean globalRollbackOnParticipationFailure = true;
  private boolean failEarlyOnGlobalRollbackOnly = false;
  private boolean rollbackOnCommitFailure = false;
  private int defaultTimeout = TransactionDefinition.TIMEOUT_DEFAULT;

  public SimpleTransactionManager(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override public TransactionStatus getTransaction(TransactionDefinition definition)
      throws TransactionException {
    Object transaction = doGetTransaction();

    if (definition.getTimeout() < TransactionDefinition.TIMEOUT_DEFAULT) {
      throw new InvalidTimeoutException("Invalid transaction timeout", definition.getTimeout());
    }

    if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY) {
      throw new IllegalTransactionStateException(
          "No existing transaction found for transaction marked with propagation 'mandatory'");
    } else if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||
        definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW ||
        definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
      doBegin(transaction, definition);
    }
    return newTransactionStatus(definition, transaction, true, false, true, null);
  }

  @Override public void commit(TransactionStatus status) throws TransactionException {
    if (status.isCompleted()) {
      throw new IllegalTransactionStateException(
          "Transaction is already completed - do not call commit or rollback more than once per transaction");
    }
    DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
    if (defStatus.isLocalRollbackOnly()) {
      processRollback(defStatus);
      return;
    }
    if (defStatus.isGlobalRollbackOnly()) {
      if (status.isNewTransaction() || isFailEarlyOnGlobalRollbackOnly()) {
        throw new UnexpectedRollbackException(
            "Transaction rolled back because it has been marked as rollback-only");
      }
      return;
    }
    processCommit(defStatus);
  }

  @Override public void rollback(TransactionStatus status) throws TransactionException {
    if (status.isCompleted()) {
      throw new IllegalTransactionStateException(
          "Transaction is already completed - do not call commit or rollback more than once per transaction");
    }

    DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
    processRollback(defStatus);
  }

  public boolean isNestedTransactionAllowed() {
    return nestedTransactionAllowed;
  }

  public void setNestedTransactionAllowed(boolean nestedTransactionAllowed) {
    this.nestedTransactionAllowed = nestedTransactionAllowed;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public boolean isGlobalRollbackOnParticipationFailure() {
    return globalRollbackOnParticipationFailure;
  }

  public void setGlobalRollbackOnParticipationFailure(
      boolean globalRollbackOnParticipationFailure) {
    this.globalRollbackOnParticipationFailure = globalRollbackOnParticipationFailure;
  }

  public boolean isFailEarlyOnGlobalRollbackOnly() {
    return failEarlyOnGlobalRollbackOnly;
  }

  public void setFailEarlyOnGlobalRollbackOnly(boolean failEarlyOnGlobalRollbackOnly) {
    this.failEarlyOnGlobalRollbackOnly = failEarlyOnGlobalRollbackOnly;
  }

  public boolean isRollbackOnCommitFailure() {
    return rollbackOnCommitFailure;
  }

  public void setRollbackOnCommitFailure(boolean rollbackOnCommitFailure) {
    this.rollbackOnCommitFailure = rollbackOnCommitFailure;
  }

  public int getDefaultTimeout() {
    return defaultTimeout;
  }

  public void setDefaultTimeout(int defaultTimeout) {
    this.defaultTimeout = defaultTimeout;
  }

  private void doBegin(Object transaction, TransactionDefinition definition) {
    DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
    Connection con = null;

    try {
      if (txObject.getConnectionHolder() == null || txObject.getConnectionHolder()
          .isSynchronizedWithTransaction()) {
        Connection newCon = this.dataSource.getConnection();
        txObject.setConnectionHolder(new ConnectionHolder(newCon), true);
      }

      txObject.getConnectionHolder().setSynchronizedWithTransaction(true);
      con = txObject.getConnectionHolder().getConnection();

      Integer previousIsolationLevel =
          DataSourceUtils.prepareConnectionForTransaction(con, definition);
      txObject.setPreviousIsolationLevel(previousIsolationLevel);

      // Switch to manual commit if necessary. This is very expensive in some JDBC drivers,
      // so we don't want to do it unnecessarily (for example if we've explicitly
      // configured the connection pool to set it already).
      if (con.getAutoCommit()) {
        txObject.setMustRestoreAutoCommit(true);
        con.setAutoCommit(false);
      }

      Field field = ReflectionUtils.findField(ConnectionHolder.class, "transactionActive");
      field.setAccessible(true);
      ReflectionUtils.setField(field, txObject.getConnectionHolder(), true);

      int timeout = determineTimeout(definition);
      if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
        txObject.getConnectionHolder().setTimeoutInSeconds(timeout);
      }

      // Bind the session holder to the thread.
      if (txObject.isNewConnectionHolder()) {
        TransactionSynchronizationManager
            .bindResource(getDataSource(), txObject.getConnectionHolder());
      }
    } catch (Throwable ex) {
      if (txObject.isNewConnectionHolder()) {
        DataSourceUtils.releaseConnection(con, this.dataSource);
        txObject.setConnectionHolder(null, false);
      }
      throw new CannotCreateTransactionException("Could not open JDBC Connection for transaction",
          ex);
    }
  }

  private int determineTimeout(TransactionDefinition definition) {
    if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
      return definition.getTimeout();
    }
    return this.defaultTimeout;
  }

  private void processCommit(DefaultTransactionStatus status) throws TransactionException {
    try {
      boolean globalRollbackOnly = false;
      if (status.isNewTransaction() || isFailEarlyOnGlobalRollbackOnly()) {
        globalRollbackOnly = status.isGlobalRollbackOnly();
      }
      if (status.hasSavepoint()) {
        status.releaseHeldSavepoint();
      } else if (status.isNewTransaction()) {
        doCommit(status);
      }
      // Throw UnexpectedRollbackException if we have a global rollback-only
      // marker but still didn't get a corresponding exception from commit.
      if (globalRollbackOnly) {
        throw new UnexpectedRollbackException(
            "Transaction silently rolled back because it has been marked as rollback-only");
      }
    } catch (UnexpectedRollbackException ex) {
      throw ex;
    } catch (TransactionException ex) {
      // can only be caused by doCommit
      if (isRollbackOnCommitFailure()) {
        doRollbackOnCommitException(status);
      }
      throw ex;
    } catch (Exception ex) {
      doRollbackOnCommitException(status);
      throw ex;
    } finally {
      if (status.isNewTransaction()) {
        doCleanupAfterCompletion(status.getTransaction());
      }
    }
  }

  protected void doCleanupAfterCompletion(Object transaction) {
    DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;

    // Remove the connection holder from the thread, if exposed.
    if (txObject.isNewConnectionHolder()) {
      TransactionSynchronizationManager.unbindResource(this.dataSource);
    }

    // Reset connection.
    Connection con = txObject.getConnectionHolder().getConnection();
    try {
      if (txObject.isMustRestoreAutoCommit()) {
        con.setAutoCommit(true);
      }
      DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel());
    } catch (Throwable ex) {
      logger.debug("Could not reset JDBC Connection after transaction", ex);
    }

    if (txObject.isNewConnectionHolder()) {
      logger.debug("Releasing JDBC Connection [{}] after transaction", con);
      DataSourceUtils.releaseConnection(con, this.dataSource);
    }

    txObject.getConnectionHolder().clear();
  }

  private void doRollbackOnCommitException(DefaultTransactionStatus status)
      throws TransactionException {
    if (status.isNewTransaction()) {
      doRollback(status);
    } else if (status.hasTransaction() && isGlobalRollbackOnParticipationFailure()) {
      doSetRollbackOnly(status);
    }
  }

  private void doCommit(DefaultTransactionStatus status) {
    DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
    Connection con = txObject.getConnectionHolder().getConnection();
    try {
      con.commit();
    } catch (SQLException ex) {
      throw new TransactionSystemException("Could not commit JDBC transaction", ex);
    }
  }

  private void processRollback(DefaultTransactionStatus status) {
    try {
      if (status.hasSavepoint()) {
        status.rollbackToHeldSavepoint();
      } else if (status.isNewTransaction()) {
        doRollback(status);
      } else if (status.hasTransaction()) {
        if (status.isLocalRollbackOnly() || isGlobalRollbackOnParticipationFailure()) {
          doSetRollbackOnly(status);
        }
      }
    } finally {
      if (status.isNewTransaction()) {
        doCleanupAfterCompletion(status.getTransaction());
      }
    }
  }

  private void doRollback(DefaultTransactionStatus status) {
    DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
    Connection con = txObject.getConnectionHolder().getConnection();
    try {
      con.rollback();
    } catch (SQLException ex) {
      throw new TransactionSystemException("Could not roll back JDBC transaction", ex);
    }
  }

  private void doSetRollbackOnly(DefaultTransactionStatus status) {
    DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
    txObject.setRollbackOnly();
  }

  private Object doGetTransaction() {
    DataSourceTransactionObject txObject = new DataSourceTransactionObject();
    txObject.setSavepointAllowed(isNestedTransactionAllowed());
    ConnectionHolder conHolder =
        (ConnectionHolder) TransactionSynchronizationManager.getResource(this.dataSource);
    txObject.setConnectionHolder(conHolder, false);
    return txObject;
  }

  private DefaultTransactionStatus newTransactionStatus(TransactionDefinition definition,
                                                        Object transaction, boolean newTransaction,
                                                        boolean newSynchronization, boolean debug,
                                                        Object suspendedResources) {

    boolean actualNewSynchronization =
        newSynchronization && !TransactionSynchronizationManager.isSynchronizationActive();
    return new DefaultTransactionStatus(transaction, newTransaction, actualNewSynchronization,
        definition.isReadOnly(), debug, suspendedResources);
  }

  private static class DataSourceTransactionObject extends JdbcTransactionObjectSupport {

    private boolean newConnectionHolder;

    private boolean mustRestoreAutoCommit;

    public void setConnectionHolder(ConnectionHolder connectionHolder,
                                    boolean newConnectionHolder) {
      super.setConnectionHolder(connectionHolder);
      this.newConnectionHolder = newConnectionHolder;
    }

    public boolean isNewConnectionHolder() {
      return this.newConnectionHolder;
    }

    public boolean isMustRestoreAutoCommit() {
      return this.mustRestoreAutoCommit;
    }

    public void setMustRestoreAutoCommit(boolean mustRestoreAutoCommit) {
      this.mustRestoreAutoCommit = mustRestoreAutoCommit;
    }

    public void setRollbackOnly() {
      getConnectionHolder().setRollbackOnly();
    }

    @Override public boolean isRollbackOnly() {
      return getConnectionHolder().isRollbackOnly();
    }
  }

}
