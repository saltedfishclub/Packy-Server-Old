package cc.sfclub.packyserver.tables

import cc.sfclub.packyserver.models.User
import org.ktorm.schema.*

object Users : Table<User>("packy_users") {
    val user_id = int("user_id").primaryKey().bindTo { it.user_id }
    val user_name = varchar("user_name").bindTo { it.user_name }
    val user_join_time = varchar("user_join_time").bindTo { it.user_join }
    val user_email = varchar("user_email").bindTo { it.user_email }
    val user_perm = varchar("user_perm").bindTo { it.user_perm }
    val user_publish_pkgs = varchar("user_publish_pkgs").bindTo { it.user_published_pkgs }
    val user_bio = text("user_bio").bindTo { it.user_bio }
    val user_pass = varchar("user_pass").bindTo { it.user_pass }
    val user_captcha = varchar("user_captcha").bindTo { it.user_captcha }
    val user_checked_email = boolean("user_checked_email").bindTo { it.user_checked_email }
}