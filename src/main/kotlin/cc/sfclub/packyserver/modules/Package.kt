package cc.sfclub.packyserver.modules

import cc.sfclub.packyserver.enum.Type
import cc.sfclub.packyserver.models.Package
import cc.sfclub.packyserver.models.UserInfo
import cc.sfclub.packyserver.tables.Packages
import cc.sfclub.packyserver.tables.Resources
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.add
import org.ktorm.entity.any
import org.ktorm.entity.sequenceOf
import java.io.File
import java.util.*

@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.pkg(testing: Boolean = false) {
    val user = environment.config.property("ktor.mysql.user").getString()
    val password = environment.config.property("ktor.mysql.password").getString()
    val database = Database.connect("jdbc:mysql://localhost:3306/PACKY", user = user, password = password)

    routing {
        route("/api/v1") {
            authenticate {
                post("/resource") {
                    val multipartData = call.receiveMultipart()

                    multipartData.forEachPart { part ->
                        when (part) {
                            is PartData.FileItem -> {
                                val fileName = UUID.randomUUID().toString()
                                val fileBytes = part.streamProvider().readBytes()

                                File("resources/$fileName").writeBytes(fileBytes)

                                database.insert(Resources) {
                                    set(it.res_id, fileName)
                                }

                                call.respond(mapOf("code" to "200", "message" to Type.SUCCESS, "data" to mapOf("fileId" to fileName)))
                            }
                        }
                    }
                }

                route("/package/{name}") {
                    post {
                        val reqBody = call.receiveParameters()
                        val name = call.parameters["name"].toString()
                        val agreement = reqBody["agreement"].toString()
                        val arch = reqBody["arch"].toString()
                        val authors = reqBody["authors"].toString()
                        val conflicts = reqBody["conflicts"].toString()
                        val depends = reqBody["conflicts"].toString()
                        val desc = reqBody["description"].toString()
                        val javaVersion = reqBody["javaVersion"].toString()
                        val lastUpdate = reqBody["lastUpdated"].toString()
                        val mcVersion = reqBody["mcver"].toString()
                        val verified = reqBody["verified"].toBoolean()
                        val icon = reqBody["icon"].toString()

                        if(!database.sequenceOf(Packages).any { Packages.pkg_name eq name})
                            call.respond(HttpStatusCode.NotFound)

                        val pkg = Package {
                            pkg_name = name
                            pkg_agreement = agreement
                            pkg_arch = arch
                            pkg_authors = authors
                            pkg_conflicts = conflicts
                            pkg_depends = depends
                            pkg_desc = desc
                            pkg_java_version = javaVersion
                            pkg_last_update = lastUpdate
                            pkg_mc_version = mcVersion
                            pkg_verified = verified
                            pkg_icon = icon
                        }
                        database.sequenceOf(Packages).add(pkg)

                        call.respond(mapOf("code" to "200", "message" to Type.SUCCESS))
                    }
                }

                post("/test") {
                    val info = call.authentication.principal<UserInfo>()

                    if (info != null) {
                        call.respond(info.user_perm)
                    }
                }
            }
        }
    }
}

