package com.back.global.security

import com.back.domain.member.member.service.MemberService
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOAuth2UserService(
    private val memberService: MemberService
) : DefaultOAuth2UserService() {

    @Transactional
    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val providerType = userRequest.clientRegistration.registrationId.uppercase()

        val userInfo = extractUserInfo(oAuth2User, providerType)
        val username = "${providerType}__${userInfo.oauthUserId}"

        val member = memberService.modifyOrJoin(
            username = username,
            password = "",
            nickname = userInfo.nickname,
            profileImgUrl = userInfo.profileImgUrl
        ).data ?: throw OAuth2AuthenticationException("회원 정보 처리 실패")

        return SecurityUser(
            id = member.id!!,
            username = member.username!!,
            password = member.password ?: "",
            nickname = member.name ?: "",
            authorities = member.authorities
        )
    }

    private fun extractUserInfo(oAuth2User: OAuth2User, providerType: String): UserInfo {
        val attributes = oAuth2User.attributes
        val oauthUserId = oAuth2User.name

        return when (providerType) {
            "KAKAO" -> {
                val properties = attributes["properties"] as? Map<String, Any>
                    ?: throw OAuth2AuthenticationException("Kakao 사용자 정보가 없습니다")

                UserInfo(
                    oauthUserId = oauthUserId,
                    nickname = properties["nickname"] as? String,
                    profileImgUrl = properties["profile_image"] as? String
                )
            }

            "GOOGLE" -> {
                UserInfo(
                    oauthUserId = oauthUserId,
                    nickname = attributes["name"] as? String,
                    profileImgUrl = attributes["picture"] as? String
                )
            }

            "NAVER" -> {
                val response = attributes["response"] as? Map<String, Any>
                    ?: throw OAuth2AuthenticationException("Naver 사용자 정보가 없습니다")

                UserInfo(
                    oauthUserId = response["id"] as? String ?: oauthUserId,
                    nickname = response["nickname"] as? String,
                    profileImgUrl = response["profile_image"] as? String
                )
            }

            else -> throw OAuth2AuthenticationException("지원하지 않는 OAuth 제공자입니다: $providerType")
        }
    }

    private data class UserInfo(
        val oauthUserId: String,
        val nickname: String?,
        val profileImgUrl: String?
    )
}