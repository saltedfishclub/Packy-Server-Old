package cc.sfclub.packyserver.models

import org.ktorm.entity.Entity

interface Package: Entity<Package> {
    companion object : Entity.Factory<Package>()
    val pkg_id: Int
    var pkg_name: String
    var pkg_agreement: String
    var pkg_arch: String
    var pkg_authors: String
    var pkg_conflicts: String
    var pkg_depends: String
    var pkg_desc: String
    var pkg_java_version: String
    var pkg_last_update: String
    var pkg_mc_version: String
    var pkg_verified: Boolean
    var pkg_icon: String
    var pkg_vote: Int
}