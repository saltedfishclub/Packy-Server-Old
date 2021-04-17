package cc.sfclub.principals

import io.ktor.auth.*

data class UserInfo(val userId: String): Principal
