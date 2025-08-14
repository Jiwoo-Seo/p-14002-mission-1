package com.back.domain.member.member.controller

import com.back.domain.member.member.service.MemberService
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1AdmMemberControllerTest(
    @Autowired private val memberService: MemberService,
    @Autowired private val mvc: MockMvc
) {

    @Test
    @DisplayName("관리자 - 회원 목록 조회")
    @WithUserDetails("admin")
    fun `관리자는 모든 회원 목록을 조회할 수 있다`() {
        // When
        val resultActions = mvc.perform(get("/api/v1/adm/members"))
            .andDo(print())

        // Then
        val members = memberService.findAll()

        resultActions
            .andExpect(handler().handlerType(ApiV1AdmMemberController::class.java))
            .andExpect(handler().methodName("getItems"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(members.size))

        // 첫 번째 회원 정보 검증
        if (members.isNotEmpty()) {
            val firstMember = members[0]
            resultActions
                .andExpect(jsonPath("$[0].id").value(firstMember.id))
                .andExpect(jsonPath("$[0].name").value(firstMember.name))
                .andExpect(jsonPath("$[0].username").value(firstMember.username))
                .andExpect(jsonPath("$[0].isAdmin").value(firstMember.isAdmin))
        }
    }

    @Test
    @DisplayName("일반 사용자 - 회원 목록 조회 권한 없음")
    @WithUserDetails("user1")
    fun `일반 사용자는 회원 목록을 조회할 수 없다`() {
        // When
        val resultActions = mvc.perform(get("/api/v1/adm/members"))
            .andDo(print())

        // Then
        resultActions
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.msg").value("권한이 없습니다."))
    }

    @Test
    @DisplayName("관리자 - 회원 단건 조회")
    @WithUserDetails("admin")
    fun `관리자는 특정 회원 정보를 조회할 수 있다`() {
        // Given
        val memberId = 1L

        // When
        val resultActions = mvc.perform(get("/api/v1/adm/members/$memberId"))
            .andDo(print())

        // Then
        val member = memberService.findById(memberId).get()

        resultActions
            .andExpect(handler().handlerType(ApiV1AdmMemberController::class.java))
            .andExpect(handler().methodName("getItem"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(member.id))
            .andExpect(jsonPath("$.createDate").value(startsWith(member.createDate.toString().substring(0, 20))))
            .andExpect(jsonPath("$.modifyDate").value(startsWith(member.modifyDate.toString().substring(0, 20))))
            .andExpect(jsonPath("$.name").value(member.name))
            .andExpect(jsonPath("$.username").value(member.username))
            .andExpect(jsonPath("$.isAdmin").value(member.isAdmin))
    }

    @Test
    @DisplayName("일반 사용자 - 회원 단건 조회 권한 없음")
    @WithUserDetails("user1")
    fun `일반 사용자는 다른 회원 정보를 조회할 수 없다`() {
        // Given
        val memberId = 1L

        // When
        val resultActions = mvc.perform(get("/api/v1/adm/members/$memberId"))
            .andDo(print())

        // Then
        resultActions
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.msg").value("권한이 없습니다."))
    }
}
