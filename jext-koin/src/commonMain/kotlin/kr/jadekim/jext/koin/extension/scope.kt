package kr.jadekim.jext.koin.extension

import org.koin.core.scope.Scope

fun Scope.getInt(key: String): Int? {
    return getKoin().getProperty<String>(key)?.toIntOrNull()
}

fun Scope.getInt(key: String, defaultValue: Int): Int {
    return getKoin().getProperty<String>(key)?.toInt() ?: defaultValue
}

fun Scope.getBoolean(key: String): Boolean? {
    return getKoin().getProperty<String>(key)?.toBoolean()
}

fun Scope.getBoolean(key: String, defaultValue: Boolean): Boolean {
    return getKoin().getProperty<String>(key)?.toBoolean() ?: defaultValue
}

fun Scope.getString(key: String): String? {
    return getKoin().getProperty(key)
}

fun Scope.getString(key: String, defaultValue: String): String {
    return getKoin().getProperty(key, defaultValue)
}
