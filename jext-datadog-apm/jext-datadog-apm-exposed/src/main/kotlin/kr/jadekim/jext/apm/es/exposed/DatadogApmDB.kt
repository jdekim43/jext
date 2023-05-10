package kr.jadekim.jext.apm.es.exposed

import kr.jadekim.jext.exposed.CrudDB
import kr.jadekim.jext.exposed.ReadDB
import org.jetbrains.exposed.sql.Transaction

open class DatadogReadDB(
    private val delegate: ReadDB
) : ReadDB {

    override suspend fun <T> read(
        transactionIsolation: Int?,
        statement: suspend Transaction.() -> T
    ): T = delegate.read {
        installDatadogApmTracer()
        statement()
    }
}

open class DatadogCrudDB(
    private val delegate: CrudDB
) : CrudDB, DatadogReadDB(delegate) {

    override suspend fun <T> execute(
        transactionIsolation: Int?,
        statement: suspend Transaction.() -> T
    ): T = delegate.execute {
        installDatadogApmTracer()
        statement()
    }
}

fun ReadDB.tracingApm(): ReadDB = DatadogReadDB(this)

fun CrudDB.tracingApm(): CrudDB = DatadogCrudDB(this)