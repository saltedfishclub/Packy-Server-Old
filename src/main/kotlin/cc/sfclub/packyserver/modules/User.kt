package cc.sfclub.packyserver.modules

import cc.sfclub.packyserver.Auth
import cc.sfclub.packyserver.enum.Permissions
import cc.sfclub.packyserver.enum.Type
import cc.sfclub.packyserver.exceptions.LoginException
import cc.sfclub.packyserver.exceptions.RegisterException
import cc.sfclub.packyserver.exceptions.UserInfoException
import cc.sfclub.packyserver.exceptions.VerifyException
import cc.sfclub.packyserver.principals.UserInfo
import cc.sfclub.packyserver.tables.Users
import cc.sfclub.packyserver.utils.Encrypt
import cc.sfclub.packyserver.utils.GenerateCaptcha
import cc.sfclub.packyserver.utils.SendCaptcha
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.any
import org.ktorm.entity.sequenceOf
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
                val dNow = Date()
                val ft = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                val joinTime = ft.format(dNow)
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
        }
    }
}