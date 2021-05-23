package cc.sfclub.packyserver

import cc.sfclub.packyserver.enum.Type
import cc.sfclub.packyserver.exceptions.*
import cc.sfclub.packyserver.principals.UserInfo
import com.sun.management.OperatingSystemMXBean
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.http.*
import java.lang.management.ManagementFactory

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.home(testing: Boolean = false) {
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
                call.respond(
                    HttpStatusCode.Conflict, mapOf("message" to "This username has been registered",
                        ("type" to exception.message ?: "") as Pair<Any, Any>
                    ))
            }
        }

        exception<LoginException> { exception ->
            if(exception.message ?: "" == Type.WRONG_PASSWORD_OR_NAME.toString()) {
                call.respond(
                    HttpStatusCode.NotFound, mapOf("message" to "Username or password wrong",
                        ("type" to exception.message ?: "") as Pair<Any, Any>
                    ))
            }
        }

        exception<UserInfoException> { exception ->
            if(exception.message ?: "" == Type.USER_NOT_FOUND.toString()) {
                call.respond(
                    HttpStatusCode.NotFound, mapOf("message" to "User not found",
                        ("type" to exception.message ?: "") as Pair<Any, Any>
                    ))
            }
        }

        exception<VerifyException> { exception ->
            if(exception.message ?: "" == Type.CAPTCHA_INCORRECT.toString()) {
                call.respond(
                    HttpStatusCode.NotFound, mapOf("message" to "Captcha is not correct",
                    ("type" to exception.message ?: "") as Pair<Any, Any>
                ))
            }
        }

        exception<PackageException> { exception ->
            if(exception.message ?: "" == Type.PACKAGE_FOUND.toString()) {
                call.respond(
                    HttpStatusCode.Conflict, mapOf("message" to "This package has been added",
                        ("type" to exception.message ?: "") as Pair<Any, Any>
                    )
                )
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
        }
    }
}

