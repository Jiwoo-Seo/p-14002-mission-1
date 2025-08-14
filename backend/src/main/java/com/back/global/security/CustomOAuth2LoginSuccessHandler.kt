package com.back.global.security

import com.back.domain.member.member.service.MemberService
import com.back.global.rq.Rq
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class CustomOAuth2LoginSuccessHandler(
    private val memberService: MemberService,
    private val rq: Rq
) : AuthenticationSuccessHandler {

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val actor = rq.actorFromDb
        val accessToken = memberService.genAccessToken(actor)

        rq.setCookie("apiKey", actor.apiKey)
        rq.setCookie("accessToken", accessToken)

        val redirectUrl = extractRedirectUrl(request)
        rq.sendRedirect(redirectUrl)
    }

    private fun extractRedirectUrl(request: HttpServletRequest): String {
        val stateParam = request.getParameter("state") ?: return "/"

        return try {
            val decodedState = String(
                Base64.getUrlDecoder().decode(stateParam),
                StandardCharsets.UTF_8
            )
            decodedState.split("#", limit = 2).firstOrNull() ?: "/"
        } catch (e: Exception) {
            "/"
        }
    }
}