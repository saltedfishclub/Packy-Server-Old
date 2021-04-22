package cc.sfclub.packyserver.utils

import java.util.*

object GenerateCaptcha {
    fun getCaptcha(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }
}