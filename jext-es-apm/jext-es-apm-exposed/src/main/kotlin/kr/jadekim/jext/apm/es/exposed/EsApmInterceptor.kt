package kr.jadekim.jext.apm.es.exposed

import co.elastic.apm.api.Span
import kr.jadekim.jext.apm.es.apmContext
import kr.jadekim.jext.apm.es.newSpan
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.StatementInterceptor

suspend fun Transaction.installEsApmTracer() {
    registerInterceptor(esApmTracer())
}

suspend fun esApmTracer(): StatementInterceptor {
    val apm = apmContext()

    return object : StatementInterceptor {

        private var span: Span? = null

        override fun beforeExecution(transaction: Transaction, context: StatementContext) {
            span = apm.transaction.newSpan(
                "db",
                transaction.db.vendor,
                context.statement.type.name,
                context.statement.prepareSQL(transaction),
            )
                .setLabel("url", transaction.db.url)
        }

        override fun afterCommit() {
            span?.setLabel("result", "commit")?.end()
        }

        override fun afterRollback() {
            span?.setLabel("result", "rollback")?.end()
        }
    }
}