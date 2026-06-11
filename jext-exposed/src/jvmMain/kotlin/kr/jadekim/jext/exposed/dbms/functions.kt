package kr.jadekim.jext.exposed.dbms

import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.Function
import org.jetbrains.exposed.v1.core.QueryBuilder
import org.jetbrains.exposed.v1.core.VarCharColumnType

class Concat(val expr: Expression<*>, val str: String) : Function<String>(VarCharColumnType()) {

    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        +"CONCAT("
        +expr
        +", "
        registerArgument(VarCharColumnType(), str)
        +")"
    }
}