package kr.jadekim.jext.apm.es.exposed

import io.opentracing.Scope
import io.opentracing.Span
import kr.jadekim.jext.apm.datadog.apmContext
import kr.jadekim.jext.apm.datadog.span
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.StatementInterceptor

suspend fun Transaction.installDatadogApmTracer() {
    registerInterceptor(datadogApmTracer())
}

suspend fun datadogApmTracer(): StatementInterceptor {
    val apm = apmContext()

    return object : StatementInterceptor {

        private var span: Span? = null
        private var scope: Scope? = null

        override fun beforeExecution(transaction: Transaction, context: StatementContext) {
            span = apm.tracer.span(context.statement.prepareSQL(transaction)) {
                apm.span?.let { asChildOf(it) }
                withTag("dbms", transaction.db.vendor)
                withTag("url", transaction.db.url)
                withTag("type", context.statement.type.name)
            }
            scope = apm.tracer.activateSpan(span)
        }

        override fun afterCommit(transaction: Transaction) {
            span?.setTag("result", "commit")
            scope?.close()
        }

        override fun afterRollback(transaction: Transaction) {
            span?.setTag("result", "rollback")
            scope?.close()
        }
    }
}