package cc.sfclub.packyserver.tables

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object Packages : Table<Nothing>("packy_pkgs") {
    val pkg_id = int("pkg_id").primaryKey()
    val pkg_name = varchar("pkg_name")
    val pkg_vote = int("pkg_vote")
}