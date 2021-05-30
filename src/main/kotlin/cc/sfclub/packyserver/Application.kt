package cc.sfclub.packyserver

import cc.sfclub.packyserver.enum.Type
import cc.sfclub.packyserver.models.UserInfo
import cc.sfclub.packyserver.utils.Auth
import com.sun.management.OperatingSystemMXBean
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import org.ktorm.jackson.KtormModule
import java.lang.management.ManagementFactory

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.home(testing: Boolean = false) {
    val verifier = Auth.makeJwtVerifier()

    install(ContentNegotiation) {
        jackson {
            registerModule(KtormModule())
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
            call.respond(mapOf("code" to "502", "message" to Type.FAILED))
        }

        status(HttpStatusCode.NotFound) {
            call.respond(mapOf("code" to "404", "message" to Type.FAILED))
        }

        status(HttpStatusCode.NotAcceptable) {
            call.respond(mapOf("code" to "404", "message" to Type.FAILED))
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

                call.respond(mapOf("code" to "200", "message" to Type.SUCCESS, "data" to mapOf("status" to status, "average" to average)))
            }
        }
    }
}

