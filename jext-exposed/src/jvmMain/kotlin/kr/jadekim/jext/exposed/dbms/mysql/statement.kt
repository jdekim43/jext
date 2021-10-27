package kr.jadekim.jext.exposed.dbms.mysql

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

fun <T : Table> T.upsert(
    body: T.(InsertStatement<Number>) -> Unit,
) = UpsertStatement<Number>(this).apply {
    body(this)
    execute(TransactionManager.current())
}

fun <T : Table> T.upsert(
    body: T.(InsertStatement<Number>) -> Unit,
    updateBody: T.(UpdateStatement) -> Unit,
) = UpsertStatement<Number>(this, updateStatement = UpdateStatement(this, null).apply { updateBody(this) }).apply {
    body(this)
    execute(TransactionManager.current())
}

class UpsertStatement<Key : Any>(
    table: Table,
    isIgnore: Boolean = false,
    private val updateStatement: UpdateStatement? = null,
) : InsertStatement<Key>(table, isIgnore) {

    override fun prepareSQL(transaction: Transaction): String {
        val keys = values.keys.filterNot { table.primaryKey?.columns?.contains(it) == true }

        if (keys.isEmpty()) {
            return super.prepareSQL(transaction)
        }

        val onUpdate = if (updateStatement == null) {
            keys.joinToString { "${transaction.identity(it)} = VALUES(${transaction.identity(it)})" }
        } else {
            val builder = QueryBuilder(true)
            updateStatement.firstDataSet.appendTo(builder) { (col, value) ->
                append("${transaction.identity(col)}=")
                registerArgument(col, value)
            }
            builder.toString()
        }


        return "${super.prepareSQL(transaction)} ON DUPLICATE KEY UPDATE $onUpdate"
    }

    override fun arguments(): List<Iterable<Pair<IColumnType, Any?>>> {
        val updateArguments = updateStatement?.arguments()?.firstOrNull() ?: return super.arguments()
        val insertArguments = super.arguments().firstOrNull()

        val arguments = mutableListOf<Pair<IColumnType, Any?>>().apply {
            if (insertArguments != null) {
                addAll(insertArguments)
            }

            addAll(updateArguments)
        }

        return listOf(arguments)
    }
}

class Values(val column: Column<*>) : Function<String>(VarCharColumnType()) {

    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        +"VALUES("
        +column
        +")"
    }
}