package com.back.domain.member.member.controller

import com.back.domain.member.member.service.MemberService
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1MemberControllerTest(
    @Autowired private val memberService: MemberService,
    @Autowired private val mvc: MockMvc
) {

    @Test
    @DisplayName("회원가입")
    fun `회원가입이 성공한다`() {
        // When
        val resultActions = mvc.perform(
            post("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "username": "usernew",
                        "password": "1234",
                        "nickname": "무명"
                    }
                    """.trimIndent()
                )
        ).andDo(print())

        // Then
        val member = memberService.findByUsername("usernew").get()

        resultActions
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("join"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.resultCode").value("201-1"))
            .andExpect(jsonPath("$.msg").value("${member.name}님 환영합니다. 회원가입이 완료되었습니다."))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.id").value(member.id))
            .andExpect(jsonPath("$.data.createDate").value(startsWith(member.createDate.toString().substring(0, 20))))
            .andExpect(jsonPath("$.data.modifyDate").value(startsWith(member.modifyDate.toString().substring(0, 20))))
            .andExpect(jsonPath("$.data.name").value(member.name))
            .andExpect(jsonPath("$.data.isAdmin").value(member.isAdmin))
    }

    @Test
    @DisplayName("로그인")
    fun `로그인이 성공하고 쿠키가 설정된다`() {
        // When
        val resultActions = mvc.perform(
            post("/api/v1/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "username": "user1",
                        "password": "1234"
                    }
                    """.trimIndent()
                )
        ).andDo(print())

        // Then
        val member = memberService.findByUsername("user1").get()

        resultActions
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("login"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.msg").value("${member.name}님 환영합니다."))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.item.id").value(member.id))
            .andExpect(jsonPath("$.data.item.name").value(member.name))
            .andExpect(jsonPath("$.data.item.isAdmin").value(member.isAdmin))
            .andExpect(jsonPath("$.data.apiKey").value(member.apiKey))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty)
            .andExpect { result ->
                // 쿠키 검증
                val apiKeyCookie = result.response.getCookie("apiKey")!!
                assertThat(apiKeyCookie.value).isEqualTo(member.apiKey)
                assertThat(apiKeyCookie.path).isEqualTo("/")
                assertThat(apiKeyCookie.isHttpOnly).isTrue

                val accessTokenCookie = result.response.getCookie("accessToken")!!
                assertThat(accessTokenCookie.value).isNotBlank
                assertThat(accessTokenCookie.path).isEqualTo("/")
                assertThat(accessTokenCookie.isHttpOnly).isTrue
            }
    }

    @Test
    @DisplayName("내 정보")
    @WithUserDetails("user1")
    fun `내 정보 조회가 성공한다`() {
        // When
        val resultActions = mvc.perform(get("/api/v1/members/me"))
            .andDo(print())

        // Then
        val member = memberService.findByUsername("user1").get()

        resultActions
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("me"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(member.id))
            .andExpect(jsonPath("$.createDate").isNotEmpty)
            .andExpect(jsonPath("$.modifyDate").isNotEmpty)
            .andExpect(jsonPath("$.name").value(member.name))
            .andExpect(jsonPath("$.username").value(member.username))
            .andExpect(jsonPath("$.isAdmin").value(member.isAdmin))
    }

    @Test
    @DisplayName("내 정보, with apiKey Cookie")
    @WithUserDetails("user1")
    fun `API 키 쿠키로 내 정보 조회가 성공한다`() {
        // Given
        val actor = memberService.findByUsername("user1").get()

        // When
        val resultActions = mvc.perform(
            get("/api/v1/members/me")
                .cookie(Cookie("apiKey", actor.apiKey))
        ).andDo(print())

        // Then
        resultActions
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("me"))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("로그아웃")
    fun `로그아웃시 쿠키가 삭제된다`() {
        // When
        val resultActions = mvc.perform(delete("/api/v1/members/logout"))
            .andDo(print())

        // Then
        resultActions
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("logout"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.msg").value("로그아웃 되었습니다."))
            .andExpect { result ->
                val apiKeyCookie = result.response.getCookie("apiKey")!!
                assertThat(apiKeyCookie.value).isEmpty()
                assertThat(apiKeyCookie.maxAge).isEqualTo(0)
                assertThat(apiKeyCookie.path).isEqualTo("/")
                assertThat(apiKeyCookie.isHttpOnly).isTrue

                val accessTokenCookie = result.response.getCookie("accessToken")!!
                assertThat(accessTokenCookie.value).isEmpty()
                assertThat(accessTokenCookie.maxAge).isEqualTo(0)
                assertThat(accessTokenCookie.path).isEqualTo("/")
                assertThat(accessTokenCookie.isHttpOnly).isTrue
            }
    }

    @Test
    @DisplayName("엑세스 토큰이 만료되었거나 유효하지 않다면 apiKey를 통해서 재발급")
    @WithUserDetails("user1")
    fun `잘못된 액세스 토큰시 API 키로 재발급한다`() {
        // Given
        val actor = memberService.findByUsername("user1").get()

        // When
        val resultActions = mvc.perform(
            get("/api/v1/members/me")
                .header("Authorization", "Bearer ${actor.apiKey} wrong-access-token")
        ).andDo(print())

        // Then
        resultActions
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("me"))
            .andExpect(status().isOk)
            .andExpect { result ->
                val accessTokenCookie = result.response.getCookie("accessToken")
                assertThat(accessTokenCookie).isNotNull
                assertThat(accessTokenCookie!!.value).isNotBlank
                assertThat(accessTokenCookie.path).isEqualTo("/")
                assertThat(accessTokenCookie.isHttpOnly).isTrue

                val headerAuthorization = result.response.getHeader("Authorization")
                assertThat(headerAuthorization).isNotBlank
                assertThat(headerAuthorization).isEqualTo(accessTokenCookie.value)
            }
    }

    @Test
    @DisplayName("Authorization 헤더가 Bearer 형식이 아닐 때 오류")
    fun `잘못된 Authorization 헤더시 오류를 반환한다`() {
        // When
        val resultActions = mvc.perform(
            get("/api/v1/members/me")
                .header("Authorization", "key")
        ).andDo(print())

        // Then
        resultActions
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.resultCode").value("401-2"))
            .andExpect(jsonPath("$.msg").value("Authorization 헤더가 Bearer 형식이 아닙니다."))
    }
}
