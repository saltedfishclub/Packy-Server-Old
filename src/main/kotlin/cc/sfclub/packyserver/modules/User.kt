package cc.sfclub.packyserver.modules

import cc.sfclub.packyserver.Auth
import cc.sfclub.packyserver.enum.Type
import cc.sfclub.packyserver.exceptions.LoginException
import cc.sfclub.packyserver.exceptions.RegisterException
import cc.sfclub.packyserver.exceptions.UserInfoException
import cc.sfclub.packyserver.exceptions.VerifyException
import cc.sfclub.packyserver.models.User
import cc.sfclub.packyserver.tables.Users
import cc.sfclub.packyserver.utils.Encrypt
import cc.sfclub.packyserver.utils.GenerateCaptcha
import cc.sfclub.packyserver.utils.SendCaptcha
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.text.SimpleDateFormat
import java.util.*

@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.user(testing: Boolean = false) {
    val user = environment.config.property("ktor.mysql.user").getString()
    val password = environment.config.property("ktor.mysql.password").getString()
    val sender = environment.config.property("ktor.captcha.sender").getString()
    val pass = environment.config.property("ktor.captcha.pass").getString()
    val host = environment.config.property("ktor.captcha.host").getString()
    val database = Database.connect("jdbc:mysql://localhost:3306/PACKY", user = user, password = password)

    routing {
        route("/api/v1") {
            route("/user/{id}") {
                get {
                    if(!database.sequenceOf(Users).any { Users.user_id eq call.parameters["id"].toString().toInt() })
                        throw UserInfoException("User Not Found")

                    val user = database
                        .from(Users)
                        .select(Users.user_id, Users.user_name, Users.user_join_time, Users.user_bio, Users.user_email)
                        .where { Users.user_id eq call.parameters["id"].toString().toInt() }
                        .map {
                                row -> Users.createEntity(row)
                        }

                    call.respond(mapOf("message" to "Query User Successfully", "type" to Type.SUCCESS, "user" to user))
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
                    throw LoginException("Password or Username Wrong")

                val user = database
                    .from(Users)
                    .select(Users.user_id, Users.user_name, Users.user_join_time, Users.user_bio, Users.user_email)
                    .where { Users.user_name eq userName }
                    .map { row ->
                        Users.createEntity(row)
                    }
                call.respond(mapOf("message" to "Login Successfully", "type" to Type.SUCCESS, "token" to Auth.sign(user.get(0).user_id.toString(), user.get(0).user_perm), "user" to user))
            }

            post("/register") {
                val parameters = call.receiveParameters()
                val userName = parameters["name"].toString()
                val userPass = parameters["pass"].toString()
                val email = parameters["email"].toString()
                val dNow = Date()
                val ft = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                val joinTime = ft.format(dNow)
                val captcha = GenerateCaptcha.getCaptcha()

                if(database.sequenceOf(Users).any {Users.user_name eq userName})
                    throw RegisterException("This Username is existed")

                SendCaptcha.send(email, sender, pass, host, captcha, userName)

                val user = User {
                    user_name = userName
                    user_pass = Encrypt.sha256(userPass)
                    user_email = email
                    user_join = joinTime
                    user_captcha = captcha
                }
                database.sequenceOf(Users).add(user)

                call.respond(mapOf("message" to "Registered successfully", "type" to Type.SUCCESS))
            }

            get("/verifyEmail/{id}") {
                val captcha = call.parameters["id"].toString()

                if(!database.sequenceOf(Users).any { Users.user_captcha eq captcha })
                    throw VerifyException("Captcha Wrong")

                val user = User {
                    user_captcha = captcha
                    user_checked_email = true
                }
                database.sequenceOf(Users).update(user)

                call.respond(mapOf("message" to "Email Check Successfully", "type" to Type.SUCCESS))
            }
        }
    }
}
