package cc.sfclub.tables

import org.ktorm.schema.*

object Users : Table<Nothing>("packy_users") {
    val user_id = varchar("user_id").primaryKey()
    val user_name = varchar("user_name")
    val user_join_time = datetime("user_join_time")
    val user_email = varchar("user_email")
    val user_perm = varchar("user_perm")
    val user_publish_pkgs = varchar("user_publish_pkgs")
    val user_bio = text("user_bio")
}