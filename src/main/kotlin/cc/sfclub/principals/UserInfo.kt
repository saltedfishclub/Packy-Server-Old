package cc.sfclub.principals

import io.ktor.auth.*

data class UserInfo(val user_name: String): Principal
