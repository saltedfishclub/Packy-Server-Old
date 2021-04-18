package cc.sfclub.packyserver.tables

import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object Packages : Table<Nothing>("packy_pkgs") {
    val pkg_id = int("pkg_id").primaryKey()
    val pkg_name = varchar("pkg_name")
    val pkg_agreement = varchar("pkg_agreement")
    val pkg_arch = varchar("pkg_arch")
    val pkg_authors = varchar("pkg_author")
    val pkg_conflicts = varchar("pkg_conflict")
    val pkg_depends = varchar("pkg_depends")
    val pkg_desc = varchar("pkg_desc")
    val pkg_java_version = varchar("pkg_java_version")
    val pkg_last_update = varchar("pkg_last_update")
    val pkg_mc_version = varchar("pkg_mc_version")
    val pkg_verified = boolean("pkg_verified")
    val pkg_icon = varchar("pkg_icon")
    val pkg_vote = int("pkg_vote")
}