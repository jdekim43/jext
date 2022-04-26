package kr.jadekim.jext.ktor.extension

import io.ktor.server.application.*
import io.ktor.server.request.*
import java.util.*

fun ApplicationCall.locale(
    support: List<Locale>,
    default: Locale = support.first(),
    cookieName: String? = "language"
): Locale {
    var languages = cookieName?.let { languageFromCookie(it) }

    if (languages.isNullOrEmpty()) {
        languages = acceptLanguage()
    }

    if (languages.isEmpty()) {
        return default
    }

    return Locale.lookup(languages, support) ?: default
}

fun ApplicationCall.acceptLanguage(): List<Locale.LanguageRange> {
    val text = request.acceptLanguage()

    if (text.isNullOrBlank()) {
        return emptyList()
    }

    return try {
        Locale.LanguageRange.parse(text)
    } catch (e: Exception) {
        parseLanguage(text)
    }
}

fun ApplicationCall.languageFromCookie(cookieName: String = "language"): List<Locale.LanguageRange> {
    val text = request.cookies[cookieName]

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
