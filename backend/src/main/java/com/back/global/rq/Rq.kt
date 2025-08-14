package com.back.global.rq

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.service.MemberService
import com.back.global.security.SecurityUser
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class Rq(
    private val req: HttpServletRequest,
    private val resp: HttpServletResponse,
    private val memberService: MemberService,
) {

    fun getActor(): Member = actor
    
    private val actor: Member
        get() = SecurityContextHolder.getContext()
            ?.authentication
            ?.principal
            ?.takeIf { it is SecurityUser }
            ?.let { securityUser ->
                val user = securityUser as SecurityUser
                Member(user.id, user.username, user.nickname ?: "")
            }
            ?: throw IllegalStateException("인증된 사용자가 없습니다.")


    fun getHeader(name: String, defaultValue: String = ""): String =
        req.getHeader(name) ?: defaultValue

    fun setHeader(name: String, value: String) {
        resp.setHeader(name, value)
    }

    fun getCookieValue(name: String, defaultValue: String = ""): String =
        req.cookies
            ?.find { it.name == name }
            ?.value
            ?.takeIf { it.isNotBlank() }
            ?: defaultValue

    fun setCookie(name: String, value: String?) {
        val cookie = Cookie(name, value ?: "").apply {
            path = "/"
            isHttpOnly = true
            domain = "localhost"
            secure = true
            setAttribute("SameSite", "Strict")
            maxAge = if (value.isNullOrBlank()) 0 else 60 * 60 * 24 * 365
        }
        resp.addCookie(cookie)
    }

    fun deleteCookie(name: String) = setCookie(name, null)

    fun sendRedirect(url: String) = resp.sendRedirect(url)
}