package kr.jadekim.jext.exposed.dbms

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.VarCharColumnType

class Concat(val expr: Expression<*>, val str: String) : Function<String>(VarCharColumnType()) {

    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        +"CONCAT("
        +expr
        +", "
        registerArgument(VarCharColumnType(), str)
        +")"
    }
}