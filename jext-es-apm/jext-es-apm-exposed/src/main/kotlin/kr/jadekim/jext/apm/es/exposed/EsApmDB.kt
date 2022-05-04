package kr.jadekim.jext.apm.es.exposed

import kr.jadekim.jext.exposed.CrudDB
import kr.jadekim.jext.exposed.ReadDB
import org.jetbrains.exposed.sql.Transaction

open class ESReadDB(
    private val delegate: ReadDB
) : ReadDB {

    override suspend fun <T> read(
        transactionIsolation: Int?,
        statement: suspend Transaction.() -> T
    ): T = delegate.read {
        installEsApmTracer()
        statement()
    }
}

open class ESCrudDB(
    private val delegate: CrudDB
) : CrudDB, ESReadDB(delegate) {

    override suspend fun <T> execute(
        transactionIsolation: Int?,
        statement: suspend Transaction.() -> T
    ): T = delegate.execute {
        installEsApmTracer()
        statement()
    }
}

fun ReadDB.tracingApm(): ReadDB = ESReadDB(this)

fun CrudDB.tracingApm(): CrudDB = ESCrudDB(this)