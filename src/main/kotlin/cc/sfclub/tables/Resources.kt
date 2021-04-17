package cc.sfclub.tables

import org.ktorm.schema.Table
import org.ktorm.schema.text
import org.ktorm.schema.varchar

object Resources : Table<Nothing>("packy_resources") {
    val res_id = varchar("res_id").primaryKey()
    val res_pkg = varchar("res_pkg")
    val res_auth = varchar("res_auth")
    val res_version = varchar("res_version")
}