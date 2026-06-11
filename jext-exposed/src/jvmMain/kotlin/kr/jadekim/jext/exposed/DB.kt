package kr.jadekim.jext.exposed

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import javax.sql.DataSource
import kotlin.coroutines.CoroutineContext

interface ReadDB {

    val database: Database

    suspend fun <T> read(transactionIsolation: Int? = null, statement: suspend Transaction.() -> T): T
}

fun ReadDB(
    dataSource: DataSource,
    name: String? = null,
    context: CoroutineContext = Dispatchers.IO + CoroutineName("exposed-read${if (name == null) "" else "/$name"}"),
    databaseConfig: DatabaseConfig? = null,
): ReadDB = DefaultReadDB(dataSource, name, context, databaseConfig)

open class DefaultReadDB(
    dataSource: DataSource,
    val name: String?,
    private val context: CoroutineContext,
    databaseConfig: DatabaseConfig?,
) : ReadDB {

    override val database = Database.connect(dataSource, databaseConfig = databaseConfig)

    override suspend fun <T> read(
        transactionIsolation: Int?,
        statement: suspend Transaction.() -> T,
    ): T = withContext(context) {
        suspendTransaction(database, transactionIsolation, true, statement)
    }
}

interface CrudDB : ReadDB {

    suspend fun <T> execute(transactionIsolation: Int? = null, statement: suspend Transaction.() -> T): T
}

fun CrudDB(
    dataSource: DataSource,
    readOnlyDataSource: DataSource = dataSource,
    name: String? = null,
    context: CoroutineContext = Dispatchers.IO + CoroutineName("exposed-crud${if (name == null) "" else "/$name"}"),
    databaseConfig: DatabaseConfig? = null,
): CrudDB = DefaultCrudDB(dataSource, readOnlyDataSource, name, context, databaseConfig)

open class DefaultCrudDB(
    dataSource: DataSource,
    readOnlyDataSource: DataSource = dataSource,
    name: String?,
    private val context: CoroutineContext,
    databaseConfig: DatabaseConfig? = null,
) : CrudDB, DefaultReadDB(readOnlyDataSource, name, context, databaseConfig) {

    override suspend fun <T> execute(
        transactionIsolation: Int?,
        statement: suspend Transaction.() -> T,
    ): T = withContext(context) {
        suspendTransaction(database, transactionIsolation, false, statement)
    }
}

typealias DB = CrudDB