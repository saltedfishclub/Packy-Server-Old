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
            call.respond(mapOf("message" to "You don't have the permission to do this", "type" to Type.FAILED))
        }

        exception<RegisterException> { exception ->
            call.respond(mapOf(("message" to exception.message ?: "") as Pair<Any, Any>, "type" to Type.FAILED))
        }

        exception<LoginException> { exception ->
            call.respond(mapOf(("message" to exception.message ?: "") as Pair<Any, Any>, "type" to Type.FAILED))
        }

        exception<PermissionException> { exception ->
            call.respond(mapOf(("message" to exception.message ?: "") as Pair<Any, Any>, "type" to Type.FAILED))
        }

        exception<UserInfoException> { exception ->
            call.respond(mapOf(("message" to exception.message ?: "") as Pair<Any, Any>, "type" to Type.FAILED))
        }

        exception<VerifyException> { exception ->
            call.respond(mapOf(("message" to exception.message ?: "") as Pair<Any, Any>, "type" to Type.FAILED))
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

