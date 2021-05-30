package cc.sfclub.packyserver.tables

import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import cc.sfclub.packyserver.models.Package

object Packages : Table<Package>("packy_pkgs") {
    val pkg_id = int("pkg_id").primaryKey().bindTo { it.pkg_id }
    val pkg_name = varchar("pkg_name").bindTo { it.pkg_name }
    val pkg_agreement = varchar("pkg_agreement").bindTo { it.pkg_agreement }
    val pkg_arch = varchar("pkg_arch").bindTo { it.pkg_arch }
    val pkg_authors = varchar("pkg_author").bindTo { it.pkg_authors }
    val pkg_conflicts = varchar("pkg_conflict").bindTo { it.pkg_conflicts }
    val pkg_depends = varchar("pkg_depends").bindTo { it.pkg_depends }
    val pkg_desc = varchar("pkg_desc").bindTo { it.pkg_desc }
    val pkg_java_version = varchar("pkg_java_version").bindTo { it.pkg_java_version }
    val pkg_last_update = varchar("pkg_last_update").bindTo { it.pkg_last_update }
    val pkg_mc_version = varchar("pkg_mc_version").bindTo { it.pkg_mc_version }
    val pkg_verified = boolean("pkg_verified").bindTo { it.pkg_verified }
    val pkg_icon = varchar("pkg_icon").bindTo { it.pkg_icon }
    val pkg_vote = int("pkg_vote").bindTo { it.pkg_vote }
}