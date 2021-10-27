package kr.jadekim.jext.exposed.extensions

import org.jetbrains.exposed.sql.Query

fun Query.notEmpty() = !empty()
