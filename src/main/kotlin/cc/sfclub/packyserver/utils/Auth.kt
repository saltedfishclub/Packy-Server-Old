package cc.sfclub.packyserver.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object Auth {
    private val SECRET_KEY = UUID.randomUUID().toString().replace("-", "")
    private val algorithm = Algorithm.HMAC512(SECRET_KEY)
    private const val issuer = "pkg.sfclub.cc"
    private const val validityInMs = 3600*1000

    fun makeJwtVerifier(): JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    fun sign(name: String, perm: String): String {
        return makeToken(name, perm)
    }

    private fun makeToken(name: String, perm: String): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("user_name", name)
        .withClaim("user_perm", perm)
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)

}