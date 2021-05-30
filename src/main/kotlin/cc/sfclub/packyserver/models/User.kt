package cc.sfclub.packyserver.models

import org.ktorm.entity.Entity

interface User: Entity<User> {
    companion object : Entity.Factory<User>()
    val user_id: Int
    var user_name: String
    var user_join: String
    var user_bio: String?
    var user_email: String
    var user_perm: String
    var user_captcha: String
    var user_pass: String
    var user_checked_email: Boolean
    var user_published_pkgs: String
}