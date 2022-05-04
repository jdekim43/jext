package kr.jadekim.jext.apm.es

import co.elastic.apm.api.Span
import co.elastic.apm.api.Transaction

inline fun <T> Transaction.use(body: Transaction.() -> T): T = try {
    body()
} catch (e: Exception) {
    captureException(e)

    throw e
} finally {
    end()
}

fun Transaction.newSpan(
    type: String,
    subtype: String? = null,
    action: String? = null,
    name: String? = null,
): Span {
    val span = startSpan(type, subtype, action)

    if (name != null) {
        span.setName(name)
    }

    return span
}

inline fun <T> Span.use(body: (Span) -> T): T = try {
    body(this)
} catch (e: Exception) {
    captureException(e)

    throw e
} finally {
    end()
}