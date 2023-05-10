package kr.jadekim.jext.exposed.extensions

import org.jetbrains.exposed.sql.*

fun FieldSet.select(where: List<Expression<Boolean>>): Query = Query(this, if (where.isEmpty()) null else AndOp(where))

fun FieldSet.selectOr(where: List<Expression<Boolean>>): Query = Query(this, if (where.isEmpty()) null else OrOp(where))

fun Query.notEmpty() = !empty()
