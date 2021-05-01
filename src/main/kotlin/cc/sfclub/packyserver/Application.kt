package cc.sfclub.packyserver

import cc.sfclub.packyserver.enum.Permissions
import cc.sfclub.packyserver.enum.Type
import cc.sfclub.packyserver.exceptions.*
import cc.sfclub.packyserver.principals.UserInfo
import cc.sfclub.packyserver.tables.Packages
import cc.sfclub.packyserver.tables.Resources
import cc.sfclub.packyserver.tables.Users
import cc.sfclub.packyserver.utils.Encrypt
import cc.sfclub.packyserver.utils.GenerateCaptcha
import cc.sfclub.packyserver.utils.SendCaptcha
import com.sun.management.OperatingSystemMXBean
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.any
import org.ktorm.entity.sequenceOf
import java.io.File
import java.lang.management.ManagementFactory
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val user = environment.config.property("ktor.mysql.user").getString()
    val password = environment.config.property("ktor.mysql.password").getString()
    val sender = environment.config.property("ktor.captcha.sender").getString()
    val pass = environment.config.property("ktor.captcha.pass").getString()
    val host = environment.config.property("ktor.captcha.host").getString()
    val database = Database.connect("jdbc:mysql://localhost:3306/PACKY", user = user, password = password)
    val verifier = Auth.makeJwtVerifier()

    install(ContentNegotiation) {
        gson {
        }
    }

    install(Authentication) {
        jwt {
            verifier(verifier)
            validate {
                UserInfo(it.payload.getClaim("user_name").asString(), it.payload.getClaim("user_perm").asString())
            }
        }
    }

    install(StatusPages) {
        status(HttpStatusCode.Unauthorized) {
            call.respond(mapOf("message" to "You don't have the permission to do this", "type" to Type.PERMISSION_DENIED))
        }

        exception<RegisterException> { exception ->
            if(exception.message ?: "" == Type.USER_EXISTED.toString()) {
                call.respond(HttpStatusCode.Conflict, mapOf("message" to "This username has been registered",
                    ("type" to exception.message ?: "") as Pair<Any, Any>
                ))
            }
        }

        exception<LoginException> { exception ->
            if(exception.message ?: "" == Type.WRONG_PASSWORD_OR_NAME.toString()) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Username or password wrong",
                    ("type" to exception.message ?: "") as Pair<Any, Any>
                ))
            }
        }

        exception<UserInfoException> { exception ->
            if(exception.message ?: "" == Type.USER_NOT_FOUND.toString()) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "User not found",
                    ("type" to exception.message ?: "") as Pair<Any, Any>
                ))
            }
        }

        exception<PackageException> { exception ->
            if(exception.message ?: "" == Type.PACKAGE_FOUND.toString()) {
                call.respond(HttpStatusCode.Conflict, mapOf("message" to "This package has been added",
                    ("type" to exception.message ?: "") as Pair<Any, Any>
                ))
            }
        }

        exception<VerifyException> { exception ->
            if(exception.message ?: "" == Type.CAPTCHA_INCORRECT.toString()) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Captcha is not correct",
                    ("type" to exception.message ?: "") as Pair<Any, Any>
                ))
            }
        }
    }

    routing {
        route("/api/v1") {
            get("/") {
                call.respondText("Welcome to use Packy API!")
            }

            get("/status") {
                val status = environment.config.property("ktor.status").getString()
                val os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)
                val average = os.systemLoadAverage

                call.respond(mapOf("status" to status, "average" to average))
            }

            route("/user/{id}") {
                get {
                    if(!database.sequenceOf(Users).any { Users.user_id eq call.parameters["id"].toString() })
                        throw UserInfoException(Type.USER_NOT_FOUND.toString())

                    database
                        .from(Users)
                        .select()
                        .where { (Users.user_id eq call.parameters["id"].toString())}
                        .forEach { row ->
                            val userName = row[Users.user_name].toString()
                            val userJoin = row[Users.user_join_time].toString()
                            val userPublishedPackages = row[Users.user_join_time].toString()
                            val userBio = row[Users.user_bio].toString()
                            val userEmail = row[Users.user_email].toString()
                            val userPerm = row[Users.user_perm].toString()

                            call.respond(mapOf("name" to userName,
                                "joinTime" to userJoin,
                                "publishedPackages" to userPublishedPackages,
                                "bio" to userBio,
                                "email" to userEmail,
                                "permissionLevel" to userPerm))
                        }
                }

                authenticate {
                    post {
                        call.respond(mapOf("text" to call.receiveParameters()["name"]))
                    }
                }
            }

            post("/login") {
                val parameters = call.receiveParameters()
                val userName = parameters["user"].toString()
                val passWord = parameters["pass"].toString()

                if(!database.sequenceOf(Users).any { (Users.user_name eq userName) and (Users.user_pass eq Encrypt.sha256(passWord))})
                    throw LoginException(Type.WRONG_PASSWORD_OR_NAME.toString())

                database
                    .from(Users)
                    .select(Users.user_name, Users.user_pass, Users.user_perm, Users.user_id)
                    .where { Users.user_pass eq Encrypt.sha256(passWord) }
                    .forEach { row ->
                        call.respond(mapOf("type" to Type.SUCCESS, "token" to Auth.sign(row[Users.user_id].toString(), row[Users.user_perm].toString())))
                    }
            }

            post("/register") {
                val parameters = call.receiveParameters()
                val userName = parameters["name"].toString()
                val userPass = parameters["pass"].toString()
                val email = parameters["email"].toString()
                val joinTime = parameters["joinTime"].toString()
                val captcha = GenerateCaptcha.getCaptcha()

                if(database.sequenceOf(Users).any {Users.user_name eq userName})
                    throw RegisterException(Type.USER_EXISTED.toString())

                SendCaptcha.send(email, sender, pass, host, captcha, userName)

                database.insert(Users) {
                    set(it.user_name, userName)
                    set(it.user_email, email)
                    set(it.user_pass, Encrypt.sha256(userPass))
                    set(it.user_perm, Permissions.NORMAL.toString())
                    set(it.user_join_time, joinTime)
                    set(it.user_captcha, captcha)
                }

                call.respond(mapOf("message" to "Registered successfully", "type" to Type.SUCCESS))
            }

            get("/verifyEmail/{id}") {
                val captcha = call.parameters["id"].toString()

                if(!database.sequenceOf(Users).any {Users.user_captcha eq captcha})
                    throw VerifyException(Type.CAPTCHA_INCORRECT.toString())

                database.update(Users) {
                    set(it.user_checked_email, true)
                    where { it.user_captcha eq captcha }
                }

                call.respond(mapOf("message" to "Email Check Successfully", "type" to Type.SUCCESS))
            }

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

                                call.respond(mapOf("id" to fileName))
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

                        if(database.sequenceOf(Packages).any { Packages.pkg_name eq name})
                            throw PackageException(Type.PACKAGE_FOUND.toString())

                        database.insert(Packages) {
                            set(it.pkg_name, name)
                            set(it.pkg_arch, arch)
                            set(it.pkg_agreement, agreement)
                            set(it.pkg_authors, authors)
                            set(it.pkg_conflicts, conflicts)
                            set(it.pkg_depends, depends)
                            set(it.pkg_desc, desc)
                            set(it.pkg_icon, icon)
                            set(it.pkg_java_version, javaVersion)
                            set(it.pkg_last_update, lastUpdate)
                            set(it.pkg_verified, verified)
                            set(it.pkg_mc_version, mcVersion)
                        }

                        call.respond(mapOf("message" to "Add package successfully", "type" to Type.SUCCESS))
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

