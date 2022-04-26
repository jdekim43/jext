package kr.jadekim.jext.koin.extension

import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.scope.Scope

open class Properties(data: Map<String, String> = emptyMap()) : Map<String, String> by data {

    fun getInt(key: String): Int? {
        return get(key)?.toIntOrNull()
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return get(key)?.toInt() ?: defaultValue
    }

    fun getBoolean(key: String): Boolean? {
        return get(key)?.toBoolean()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return get(key)?.toBoolean() ?: defaultValue
    }

    fun getString(key: String): String? {
        return get(key)
    }

    fun getString(key: String, defaultValue: String): String {
        return get(key) ?: defaultValue
    }
}

fun Koin.getInt(key: String): Int? {
    return getProperty<String>(key)?.toIntOrNull()
}

fun Koin.getInt(key: String, defaultValue: Int): Int {
    return getProperty<String>(key)?.toInt() ?: defaultValue
}

fun Koin.getBoolean(key: String): Boolean? {
    return getProperty<String>(key)?.toBoolean()
}

fun Koin.getBoolean(key: String, defaultValue: Boolean): Boolean {
    return getProperty<String>(key)?.toBoolean() ?: defaultValue
}

fun Koin.getString(key: String): String? {
    return getProperty(key)
}

fun Koin.getString(key: String, defaultValue: String): String {
    return getProperty(key, defaultValue)
}

fun Scope.getInt(key: String): Int? {
    return getKoin().getInt(key)
}

fun Scope.getInt(key: String, defaultValue: Int): Int {
    return getKoin().getInt(key, defaultValue)
}

fun Scope.getBoolean(key: String): Boolean? {
    return getKoin().getBoolean(key)
}

fun Scope.getBoolean(key: String, defaultValue: Boolean): Boolean {
    return getKoin().getBoolean(key, defaultValue)
}

fun Scope.getString(key: String): String? {
    return getKoin().getString(key)
}

fun Scope.getString(key: String, defaultValue: String): String {
    return getKoin().getString(key, defaultValue)
}

fun KoinComponent.getInt(key: String): Int? {
    return getKoin().getInt(key)
}

fun KoinComponent.getInt(key: String, defaultValue: Int): Int {
    return getKoin().getInt(key, defaultValue)
}

fun KoinComponent.getBoolean(key: String): Boolean? {
    return getKoin().getBoolean(key)
}

fun KoinComponent.getBoolean(key: String, defaultValue: Boolean): Boolean {
    return getKoin().getBoolean(key, defaultValue)
}

fun KoinComponent.getString(key: String): String? {
    return getKoin().getString(key)
}

fun KoinComponent.getString(key: String, defaultValue: String): String {
    return getKoin().getString(key, defaultValue)
}
