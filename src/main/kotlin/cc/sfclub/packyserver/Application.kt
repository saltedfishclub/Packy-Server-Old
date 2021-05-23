package cc.sfclub.packyserver

import cc.sfclub.packyserver.enum.Permissions
import cc.sfclub.packyserver.enum.Type
import cc.sfclub.packyserver.exceptions.*
import cc.sfclub.packyserver.principals.UserInfo
import cc.sfclub.packyserver.tables.Packages
import cc.sfclub.packyserver.tables.Resources
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
fun Application.home(testing: Boolean = false) {

    install(ContentNegotiation) {
        gson {
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

