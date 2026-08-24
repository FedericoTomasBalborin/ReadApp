package ar.edu.unsam.phm.utils

import ar.edu.unsam.phm.domain.User
import java.security.MessageDigest
import java.util.Base64

val SECRET = "my_super_secret"

fun generateToken(user: User): String {
    val raw = "${user.email}:${user.id}:${System.currentTimeMillis()}:$SECRET"

    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(raw.toByteArray())

    return Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
}