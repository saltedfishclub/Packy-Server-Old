package cc.sfclub

import cc.sfclub.enum.Type
import cc.sfclub.tables.Resources
import cc.sfclub.tables.Users
import com.sun.management.OperatingSystemMXBean
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.gson.*
import io.ktor.features.*
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
                UserIdPrincipal(it.payload.getClaim("packy").asString())
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
                    if(database.sequenceOf(Users).any { Users.user_id eq call.parameters["id"].toString() }) {
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
                    } else {
                        call.respond(mapOf("message" to "User not found", "type" to Type.USER_NOT_FOUND))
                    }
                }

                post {  }
            }

            post("/login") {
                val parameters = call.receiveParameters()
                val userName = parameters["user"].toString()
                val passWord = parameters["pass"].toString()

                if(database.sequenceOf(Users).any {(Users.user_name eq userName) and (Users.user_pass eq passWord)}) {
                    database
                        .from(Users)
                        .select(Users.user_name, Users.user_pass)
                        .where {(Users.user_pass eq passWord) and (Users.user_name eq userName)}
                        .forEach { row ->
                            call.respond(mapOf("type" to Type.SUCCESS, "token" to Auth.sign(userName)))
                        }
                } else {
                    call.respond(mapOf("message" to "User or password wrong", "type" to Type.WRONG_PASSWORD_OR_NAME))
                }
            }

            authenticate {
                post("/resource") {
                    val multipartData = call.receiveMultipart()

                    multipartData.forEachPart { part ->
                        when (part) {
                            is PartData.FileItem -> {
                                val fileName = UUID.randomUUID().toString()
                                var fileBytes = part.streamProvider().readBytes()
                                File("resources/$fileName").writeBytes(fileBytes)
                                database.insert(Resources) {
                                    set(it.res_id, fileName)
                                }
                                call.respond(mapOf("id" to fileName))
                            }
                        }
                    }
                }
            }
        }
    }
}

