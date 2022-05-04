package kr.jadekim.jext.apm.es

import co.elastic.apm.api.ElasticApm
import co.elastic.apm.api.Span
import co.elastic.apm.api.Transaction
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class EsApmContext(
    val transaction: Transaction = ElasticApm.currentTransaction(),
    val span: Span? = null,
) : AbstractCoroutineContextElement(Key) {

    companion object Key : CoroutineContext.Key<EsApmContext>

    fun withSpan(span: Span) = EsApmContext(transaction, span)
}

suspend inline fun apmContext() = coroutineContext[EsApmContext] ?: EsApmContext()

suspend inline fun <T> inApmTransaction(withEnd: Boolean = false, body: Transaction.() -> T): T {
    val transaction = apmContext().transaction

    return try {
        transaction.body()
    } catch (e: Exception) {
        transaction.captureException(e)

        throw e
    } finally {
        if (withEnd) {
            transaction.end()
        }
    }
}

suspend inline fun <T> inApmSpan(
    type: String,
    subtype: String? = null,
    action: String? = null,
    name: String? = null,
    crossinline body: (Span) -> T
): T {
    val context = apmContext()

    if (context.span != null) {
        return context.span.use(body)
    }

    val span = context.transaction.newSpan(type, subtype, action, name)

    return withContext(context.withSpan(span)) {
        span.use(body)
    }
}