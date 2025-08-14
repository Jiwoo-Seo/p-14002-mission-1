package com.back.global.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class CustomOAuth2AuthorizationRequestResolver(
    private val clientRegistrationRepository: ClientRegistrationRepository
) : OAuth2AuthorizationRequestResolver {

    private val defaultResolver: DefaultOAuth2AuthorizationRequestResolver by lazy {
        DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository,
            OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
        )
    }

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? =
        defaultResolver.resolve(request)?.let { customizeState(it, request) }

    override fun resolve(
        request: HttpServletRequest,
        clientRegistrationId: String
    ): OAuth2AuthorizationRequest? =
        defaultResolver.resolve(request, clientRegistrationId)?.let { customizeState(it, request) }

    private fun customizeState(
        authRequest: OAuth2AuthorizationRequest,
        request: HttpServletRequest
    ): OAuth2AuthorizationRequest {
        val redirectUrl = request.getParameter("redirectUrl") ?: "/"
        val originState = UUID.randomUUID().toString()
        val rawState = "$redirectUrl#$originState"
        val encodedState = Base64.getUrlEncoder()
            .encodeToString(rawState.toByteArray(StandardCharsets.UTF_8))

        return OAuth2AuthorizationRequest.from(authRequest)
            .state(encodedState)
            .build()
    }
}