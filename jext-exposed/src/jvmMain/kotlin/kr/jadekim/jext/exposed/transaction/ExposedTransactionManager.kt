package kr.jadekim.jext.exposed.transaction

import kr.jadekim.common.dsl.TransactionManager
import kr.jadekim.jext.exposed.ReadDB
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class ExposedTransactionManager(val db: Database) : TransactionManager<TransactionManager.TransactionOptions> {

    override fun createDefaultOptions(): TransactionManager.TransactionOptions = TransactionManager.TransactionOptions()

    override suspend fun <T> inTransaction(options: TransactionManager.TransactionOptions?, block: suspend () -> T): T =
        suspendTransaction(db, options?.isolation, options?.readOnly) {
            block()
        }
}

fun ReadDB.registerTransactionManager(name: String) {
    TransactionManager.register(name, ExposedTransactionManager(database))
}
