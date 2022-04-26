package kr.jadekim.jext.exposed.koin

import com.zaxxer.hikari.HikariDataSource
import kr.jadekim.jext.exposed.CrudDB
import kr.jadekim.jext.exposed.DB
import kr.jadekim.jext.exposed.ReadDB
import kr.jadekim.jext.koin.extension.getInt
import kr.jadekim.jext.koin.extension.getString
import org.koin.core.module.Module
import org.koin.core.module.dsl.binds
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.StringQualifier
import org.koin.core.scope.Scope
import org.koin.dsl.onClose
import java.io.Closeable
import java.lang.Integer.max
import java.time.Duration
import javax.sql.DataSource

private val DSQualifier.poolSizeQualifier
    get() = StringQualifier(this.value + "-poolSize")

fun Module.dataSource(
    qualifier: DSQualifier,
    autoConnect: Boolean = true,
    autoClose: Boolean = true,
    poolSizeHint: (Scope.() -> Int)? = null,
    dataSource: Scope.() -> DataSource
) {
    single(qualifier) {
        dataSource().apply {
            if (autoConnect) {
                //initial with startup
                connection.close()
            }
        }
    }.onClose {
        if (autoClose) {
            if (it is Closeable) {
                it.close()
            } else if (it is AutoCloseable) {
                it.close()
            }
        }
    }

    if (poolSizeHint != null) {
        single(qualifier.poolSizeQualifier) { poolSizeHint() }
    }
}

fun Module.dataSource(
    qualifier: DSQualifier,
    driver: String,
    url: String,
    username: String,
    password: String,
    isReadOnly: Boolean = false,
    name: String = qualifier.name,
    poolSize: Int = 5,
    autoConnect: Boolean = true,
    configure: HikariDataSource.() -> Unit = {}
) {
    dataSource(qualifier, autoConnect, true, { poolSize }) {
        HikariDataSource().apply {
            this.driverClassName = driver
            this.jdbcUrl = url
            this.username = username
            this.password = password
            this.poolName = name
            this.isReadOnly = isReadOnly
            this.maximumPoolSize = poolSize
            connectionTimeout = Duration.ofSeconds(5).toMillis()
            configure()
        }
    }
}

fun Module.dataSource(
    qualifier: DSQualifier,
    isReadOnly: Boolean = false,
    name: String = qualifier.name,
    propertyPrefix: String = if (isReadOnly) "db.$name.readonly." else "db.$name.",
    autoConnect: Boolean = true,
    configure: HikariDataSource.() -> Unit = {}
) {
    dataSource(qualifier, autoConnect, true, { getInt(propertyPrefix + "pool.size", 5) }) {
        HikariDataSource().apply {
            driverClassName = getString(propertyPrefix + "driver")
            jdbcUrl = getString(propertyPrefix + "url")
            username = getString(propertyPrefix + "username")
            password = getString(propertyPrefix + "password")
            poolName = name
            this.maximumPoolSize = getInt(propertyPrefix + "pool.size", 5)
            this.isReadOnly = isReadOnly
            connectionTimeout = Duration.ofSeconds(5).toMillis()
            configure()
        }
    }
}

fun Module.db(
    qualifier: DBQualifier,
    crudDsQualifier: DSQualifier,
    readDSQualifier: DSQualifier,
    threadCount: Int? = null,
    shareThread: Boolean = false,
    configure: (CrudDB) -> CrudDB = { it }
) {
    single(qualifier) {
        val crudDataSource = get<DataSource>(crudDsQualifier)
        val readDataSource = get<DataSource>(readDSQualifier)

        var threadCountValue = threadCount
        if (threadCountValue == null) {
            val crudPoolSize = get<Int>(crudDsQualifier.poolSizeQualifier)
            val readPoolSize = get<Int>(readDSQualifier.poolSizeQualifier)

            threadCountValue = if (shareThread) {
                max(crudPoolSize, readPoolSize)
            } else {
                crudPoolSize + readPoolSize
            }
        }

        configure(CrudDB(crudDataSource, readDataSource, threadCountValue))
    } withOptions {
        binds(listOf(ReadDB::class, DB::class))
    }
}

fun Module.db(
    qualifier: DBQualifier,
    createDataSource: Boolean = true,
    withReadOnly: Boolean = false,
    autoConnect: Boolean = true,
    threadCount: Int? = null,
    configureDataSource: HikariDataSource.() -> Unit = {},
    configure: (CrudDB) -> CrudDB = { it }
) {
    if (createDataSource) {
        dataSource(
            qualifier.crudDSQualifier,
            autoConnect = autoConnect,
            configure = configureDataSource
        )

        if (withReadOnly) {
            dataSource(
                qualifier.readDSQualifier,
                isReadOnly = true,
                autoConnect = autoConnect,
                configure = configureDataSource
            )
        } else {
            dataSource(
                qualifier.readDSQualifier,
                autoConnect,
                true,
                { get(qualifier.crudDSQualifier.poolSizeQualifier) },
                { get(qualifier.crudDSQualifier) }
            )
        }
    }

    db(qualifier, qualifier.crudDSQualifier, qualifier.readDSQualifier, threadCount, !withReadOnly, configure)
}

fun Module.readDB(
    qualifier: DBQualifier,
    dsQualifier: DSQualifier,
    threadCount: Int? = null,
    configure: (ReadDB) -> ReadDB = { it }
) {
    single(qualifier) {
        configure(ReadDB(get(dsQualifier), threadCount ?: get(dsQualifier.poolSizeQualifier)))
    }
}

fun Module.readDB(
    qualifier: DBQualifier,
    createDataSource: Boolean = true,
    autoConnect: Boolean = true,
    threadCount: Int? = null,
    configureDataSource: HikariDataSource.() -> Unit = {},
    configure: (ReadDB) -> ReadDB = { it }
) {
    if (createDataSource) {
        dataSource(
            qualifier.readDSQualifier,
            isReadOnly = true,
            autoConnect = autoConnect,
            configure = configureDataSource
        )
    }

    readDB(qualifier, qualifier.readDSQualifier, threadCount, configure)
}
