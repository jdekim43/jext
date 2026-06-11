package kr.jadekim.jext.exposed.extensions

import org.jetbrains.exposed.v1.core.AndOp
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.FieldSet
import org.jetbrains.exposed.v1.core.OrOp
import org.jetbrains.exposed.v1.jdbc.Query

fun FieldSet.select(where: List<Expression<Boolean>>): Query = Query(this, if (where.isEmpty()) null else AndOp(where))

fun FieldSet.selectOr(where: List<Expression<Boolean>>): Query = Query(this, if (where.isEmpty()) null else OrOp(where))

fun Query.notEmpty() = !empty()
