@file:Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package com.back.domain.post.post.controller

import com.back.domain.member.member.service.MemberService
import com.back.domain.post.post.service.PostService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1AdmPostControllerTest {
    @Autowired
    private val mvc: MockMvc? = null

    @Autowired
    private val postService: PostService? = null

    @Autowired
    private val memberService: MemberService? = null

    @Test
    @DisplayName("count")
    @WithUserDetails("admin")
    @Throws(Exception::class)
    fun t1() {
        val resultActions = mvc!!
            .perform(
                MockMvcRequestBuilders.get("/api/v1/adm/posts/count")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1AdmPostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("count"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.all").value(postService!!.count()))
    }
}
