package com.back.domain.member.member.entity

import com.back.global.jpa.entity.BaseEntity
import com.back.standard.util.base64Decode
import com.back.standard.util.base64Encode
import com.back.standard.util.getOrThrow
import jakarta.persistence.Column
import jakarta.persistence.Entity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

@Entity
class Member() : BaseEntity() {

    @Column(unique = true)
    var username: String? = null
        protected set

    var password: String? = null
        protected set

    var name: String? = null
        protected set

    @Column(unique = true)
    var apiKey: String? = null
        protected set

    var profileImgUrl: String? = null
        protected set

    constructor(id: Long, username: String?, nickname: String?) : this() {
        setId(id)
        this.username = username
        this.name = nickname
    }

    constructor(username: String?, password: String?, nickname: String?, profileImgUrl: String? = null) : this() {
        this.username = username
        this.password = password
        this.name = nickname
        this.profileImgUrl = profileImgUrl
        this.apiKey = UUID.randomUUID().toString()
    }

    fun modifyApiKey(apiKey: String?) {
        this.apiKey = apiKey
    }

    val isAdmin: Boolean
        get() = username in listOf("system", "admin")

    val authorities: Collection<GrantedAuthority>
        get() = authoritiesAsStringList.map { SimpleGrantedAuthority(it) }

    private val authoritiesAsStringList: List<String>
        get() = buildList {
            if (isAdmin) add("ROLE_ADMIN")
        }

    fun modify(nickname: String?, profileImgUrl: String?) {
        this.name = nickname
        this.profileImgUrl = profileImgUrl
    }

    val profileImgUrlOrDefault: String
        get() = profileImgUrl ?: "https://placehold.co/600x600?text=U_U"


    val encodedApiKey: String?
        get() = apiKey?.base64Encode()

    fun decodeApiKey(encodedKey: String): String {
        return encodedKey.base64Decode()
    }

    fun getUsernameOrThrow(): String {
        return username.getOrThrow()
    }

    fun getNameOrThrow(): String {
        return name.getOrThrow()
    }

    fun generateEncodedApiKey() {
        val rawApiKey = UUID.randomUUID().toString()
        this.apiKey = rawApiKey.base64Encode()
    }
}
