package com.back.domain.member.member.service

import com.back.domain.member.member.entity.Member
import com.back.standard.util.Ut
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AuthTokenService(
    @Value("\${custom.jwt.secretKey}")
    private val jwtSecretKey: String,

    @Value("\${custom.accessToken.expirationSeconds}")
    private val accessTokenExpirationSeconds: Int
) {

    fun genAccessToken(member: Member): String {
        val id = member.id!!
        val username = member.username!!
        val name = member.name!!

        return Ut.jwt.toString(
            jwtSecretKey,
            accessTokenExpirationSeconds,
            mapOf("id" to id as Any, "username" to username as Any, "name" to name as Any)
        )
    }

    fun payload(accessToken: String): Map<String, Any>? {
        val parsedPayload = Ut.jwt.payload(jwtSecretKey, accessToken) ?: return null

        val id = (parsedPayload["id"] as Number).toLong()
        val username = parsedPayload["username"] as String
        val name = parsedPayload["name"] as String

        return mapOf("id" to id, "username" to username, "name" to name)
    }
}
