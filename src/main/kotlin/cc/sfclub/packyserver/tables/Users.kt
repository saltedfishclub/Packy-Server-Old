package cc.sfclub.packyserver.tables

import org.ktorm.schema.*

object Users : Table<Nothing>("packy_users") {
    val user_id = varchar("user_id").primaryKey()
    val user_name = varchar("user_name")
    val user_join_time = varchar("user_join_time")
    val user_email = varchar("user_email")
    val user_perm = varchar("user_perm")
    val user_publish_pkgs = varchar("user_publish_pkgs")
    val user_bio = text("user_bio")
    val user_pass = varchar("user_pass")
    val user_captcha = varchar("user_captcha")
    val user_checked_email = boolean("user_checked_email")
}