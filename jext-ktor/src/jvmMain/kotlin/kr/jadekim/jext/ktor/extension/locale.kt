package kr.jadekim.jext.ktor.extension

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.util.pipeline.*
import java.util.*

fun PipelineContext<*, ApplicationCall>.locale(
    support: List<Locale>,
    default: Locale = support.first(),
    cookieName: String? = "language"
): Locale {
    var languages = cookieName?.let { languageFromCookie(it) }

    if (languages.isNullOrEmpty()) {
        languages = acceptLanguage()
    }

    if (languages.isNullOrEmpty()) {
        return default
    }

    return Locale.lookup(languages, support) ?: default
}

fun PipelineContext<*, ApplicationCall>.acceptLanguage(): List<Locale.LanguageRange> {
    val text = context.request.acceptLanguage()

    if (text.isNullOrBlank()) {
        return emptyList()
    }

    return try {
        Locale.LanguageRange.parse(text)
    } catch (e: Exception) {
        parseLanguage(text)
    }
}

fun PipelineContext<*, ApplicationCall>.languageFromCookie(cookieName: String = "language"): List<Locale.LanguageRange> {
    val text = context.request.cookies[cookieName]

    if (text.isNullOrBlank()) {
        return emptyList()
    }

    return try {
        Locale.LanguageRange.parse(text)
    } catch (e: Exception) {
        parseLanguage(text)
    }
}

private fun parseLanguage(text: String): List<Locale.LanguageRange> = text.split(',').flatMap {
    val weightIndex = it.indexOf(';')
    val dropIndex = it.indexOf(';', weightIndex + 1)

    if (weightIndex < 0 || dropIndex < 0) {
        return@flatMap Locale.LanguageRange.parse(it)
    }

    Locale.LanguageRange.parse(it.substring(0, dropIndex))
}
