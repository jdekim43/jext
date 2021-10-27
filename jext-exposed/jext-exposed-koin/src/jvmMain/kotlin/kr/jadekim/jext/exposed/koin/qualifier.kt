package kr.jadekim.jext.exposed.koin

import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue

class DSQualifier(val name: String, val isReadOnly: Boolean = false) : Qualifier {

    override val value: QualifierValue = if (isReadOnly) {
        "$name-readonly-datasource"
    } else {
        "$name-datasource"
    }
}

class DBQualifier(val name: String) : Qualifier {

    val crudDSQualifier: DSQualifier by lazy { DSQualifier(name) }
    val readDSQualifier: DSQualifier by lazy { DSQualifier(name, true) }

    override val value: QualifierValue = "$name-db"
}
