package kr.jadekim.jext.exposed

import kotlinx.coroutines.asCoroutineDispatcher
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

val THREAD_COUNT_AUTO = -1

interface ReadDB {

    suspend fun <T> read(transactionIsolation: Int? = null, statement: suspend Transaction.() -> T): T
}

fun ReadDB(
    dataSource: DataSource,
    threadCount: Int = THREAD_COUNT_AUTO
): ReadDB = DefaultReadDB(dataSource, threadCount)

open class DefaultReadDB(
    dataSource: DataSource,
    threadCount: Int = THREAD_COUNT_AUTO
) : ReadDB {

    protected val readDB = Database.connect(dataSource)
    protected val dispatcher = if (threadCount == THREAD_COUNT_AUTO) {
        ThreadPoolExecutor(1, Integer.MAX_VALUE, 10, TimeUnit.MINUTES, SynchronousQueue()).asCoroutineDispatcher()
    } else {
        Executors.newFixedThreadPool(threadCount).asCoroutineDispatcher()
    }

    override suspend fun <T> read(
        transactionIsolation: Int?,
        statement: suspend Transaction.() -> T
    ): T = newSuspendedTransaction(dispatcher, readDB, transactionIsolation, statement)
}

interface CrudDB : ReadDB {

    suspend fun <T> execute(transactionIsolation: Int? = null, statement: suspend Transaction.() -> T): T
}

fun CrudDB(
    dataSource: DataSource,
    readOnlyDataSource: DataSource = dataSource,
    threadCount: Int = THREAD_COUNT_AUTO
): CrudDB = DefaultCrudDB(dataSource, readOnlyDataSource, threadCount)

open class DefaultCrudDB(
    dataSource: DataSource,
    readOnlyDataSource: DataSource = dataSource,
    threadCount: Int = THREAD_COUNT_AUTO
) : CrudDB, DefaultReadDB(readOnlyDataSource, threadCount) {

    protected val crudDB = Database.connect(dataSource)

    override suspend fun <T> execute(
        transactionIsolation: Int?,
        statement: suspend Transaction.() -> T
    ): T = newSuspendedTransaction(dispatcher, crudDB, transactionIsolation, statement)
}

typealias DB = CrudDB