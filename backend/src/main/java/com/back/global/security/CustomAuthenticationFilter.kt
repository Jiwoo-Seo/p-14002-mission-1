package com.back.global.security

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.service.MemberService
import com.back.global.exception.ServiceException
import com.back.global.rq.Rq
import com.back.standard.util.Ut.json.toString
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class CustomAuthenticationFilter(
    private val memberService: MemberService,
    private val rq: Rq
) : OncePerRequestFilter() {

    private companion object {
        val EXCLUDED_PATHS = setOf(
            "/api/v1/members/login",
            "/api/v1/members/logout",
            "/api/v1/members/join"
        )
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        logger.debug("Processing request for ${request.requestURI}")

        try {
            processAuthentication(request, response, filterChain)
        } catch (e: ServiceException) {
            handleServiceException(e, response)
        }
    }

    @Throws(ServletException::class, IOException::class)
    private fun processAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // API 요청이 아니거나 제외 경로라면 패스
        if (!request.requestURI.startsWith("/api/") ||
            request.requestURI in EXCLUDED_PATHS) {
            filterChain.doFilter(request, response)
            return
        }

        val (apiKey, accessToken) = extractCredentials()

        if (apiKey.isBlank() && accessToken.isBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        logger.debug("apiKey: $apiKey")
        logger.debug("accessToken: $accessToken")

        val member = authenticateUser(apiKey, accessToken)
        setSecurityContext(member)

        filterChain.doFilter(request, response)
    }

    private fun extractCredentials(): Pair<String, String> {
        val headerAuthorization = rq.getHeader("Authorization")

        return if (headerAuthorization.isNotBlank()) {
            if (!headerAuthorization.startsWith("Bearer ")) {
                throw ServiceException("401-2", "Authorization 헤더가 Bearer 형식이 아닙니다.")
            }

            val parts = headerAuthorization.split(" ", limit = 3)
            val apiKey = parts.getOrNull(1) ?: ""
            val accessToken = parts.getOrNull(2) ?: ""
            Pair(apiKey, accessToken)
        } else {
            val apiKey = rq.getCookieValue("apiKey")
            val accessToken = rq.getCookieValue("accessToken")
            Pair(apiKey, accessToken)
        }
    }

    private fun authenticateUser(apiKey: String, accessToken: String): Member {
        // Access Token으로 먼저 시도
        if (accessToken.isNotBlank()) {
            memberService.payload(accessToken)?.let { payload ->
                val id = payload["id"] as? Int ?: throw ServiceException("401-4", "유효하지 않은 토큰입니다.")
                val username = payload["username"] as? String
                val name = payload["name"] as? String
                return Member(id, username, name)
            }
        }

        // API Key로 인증
        return if (apiKey.isNotBlank()) {
            memberService.findByApiKey(apiKey)
                ?: throw ServiceException("401-3", "API 키가 유효하지 않습니다.")
        } else {
            throw ServiceException("401-5", "인증 정보가 없습니다.")
        }
    }

    private fun setSecurityContext(member: Member) {
        val user = SecurityUser(
            id = member.id!!,
            username = member.username!!,
            password = "",
            nickname = member.name ?: "",
            authorities = member.authorities
        )

        val authentication: Authentication = UsernamePasswordAuthenticationToken(
            user,
            user.password,
            user.authorities
        )

        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun handleServiceException(e: ServiceException, response: HttpServletResponse) {
        val rsData = e.rsData
        response.apply {
            contentType = "application/json;charset=UTF-8"
            status = rsData.statusCode
            writer.write(toString(rsData))
        }
    }
}