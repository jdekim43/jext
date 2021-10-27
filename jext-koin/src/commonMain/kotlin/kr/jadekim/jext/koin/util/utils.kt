package kr.jadekim.jext.koin.util

expect fun shutdownHook(block: () -> Unit)

expect fun loadPropertiesFromArguments(arguments: Map<String, List<String>>): Map<String, String>
