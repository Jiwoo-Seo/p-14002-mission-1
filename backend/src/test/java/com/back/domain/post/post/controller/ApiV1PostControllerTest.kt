package com.back.domain.post.post.controller

import com.back.domain.member.member.service.MemberService
import com.back.domain.post.post.service.PostService
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
class ApiV1PostControllerTest(
    @Autowired private val mvc: MockMvc,
    @Autowired private val postService: PostService,
    @Autowired private val memberService: MemberService
) {

    @Test
    @DisplayName("글 쓰기")
    @WithUserDetails("user1")
    fun `글 작성이 성공한다`() {
        // When
        val resultActions = mvc.perform(
            post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "title": "제목",
                        "content": "내용"
                    }
                    """.trimIndent()
                )
        ).andDo(print())

        // Then
        val post = postService.findLatest().get()

        resultActions
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("write"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.resultCode").value("201-1"))
            .andExpect(jsonPath("$.msg").value("${post.id}번 글이 작성되었습니다."))
            .andExpect(jsonPath("$.data.id").value(post.id))
            .andExpect(jsonPath("$.data.title").value("제목"))
    }

    @Test
    @DisplayName("글 쓰기 - 액세스 토큰으로 인증")
    fun `유효한 액세스 토큰으로 글 작성이 성공한다`() {
        // Given
        val actor = memberService.findByUsername("user1").get()
        val accessToken = memberService.genAccessToken(actor)

        // When
        val resultActions = mvc.perform(
            post("/api/v1/posts")
                .header("Authorization", "Bearer wrong-api-key $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "title": "제목",
                        "content": "내용"
                    }
                    """.trimIndent()
                )
        ).andDo(print())

        // Then
        resultActions
            .andExpect(handler().methodName("write"))
            .andExpect(status().isCreated)
    }

    @Test
    @DisplayName("글 쓰기 - 제목 누락시 오류")
    @WithUserDetails("user1")
    fun `제목이 없으면 글 작성이 실패한다`() {
        // When
        val resultActions = mvc.perform(
            post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "title": "",
                        "content": "내용"
                    }
                    """.trimIndent()
                )
        ).andDo(print())

        // Then
        resultActions
            .andExpect(handler().methodName("write"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.resultCode").value("400-1"))
    }

    @Test
    @DisplayName("글 수정")
    @WithUserDetails("user1")
    fun `글 수정이 성공한다`() {
        // Given
        val postId = 1L

        // When
        val resultActions = mvc.perform(
            put("/api/v1/posts/$postId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "title": "제목 new",
                        "content": "내용 new"
                    }
                    """.trimIndent()
                )
        ).andDo(print())

        // Then
        resultActions
            .andExpect(handler().methodName("modify"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.msg").value("${postId}번 글이 수정되었습니다."))
    }

    @Test
    @DisplayName("글 수정 - 권한 없음")
    fun `권한이 없는 사용자는 글을 수정할 수 없다`() {
        // Given
        val postId = 1L
        val actor = memberService.findByUsername("user3").get()

        // When
        val resultActions = mvc.perform(
            put("/api/v1/posts/$postId")
                .header("Authorization", "Bearer ${actor.apiKey}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "title": "제목 new",
                        "content": "내용 new"
                    }
                    """.trimIndent()
                )
        ).andDo(print())

        // Then
        resultActions
            .andExpect(handler().methodName("modify"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.msg").value("${postId}번 글 수정권한이 없습니다."))
    }

    @Test
    @DisplayName("글 삭제")
    @WithUserDetails("user1")
    fun `글 삭제가 성공한다`() {
        // Given
        val postId = 1L

        // When
        val resultActions = mvc.perform(delete("/api/v1/posts/$postId"))
            .andDo(print())

        // Then
        resultActions
            .andExpect(handler().methodName("delete"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.msg").value("${postId}번 글이 삭제되었습니다."))
    }

    @Test
    @DisplayName("글 단건 조회")
    fun `글 단건 조회가 성공한다`() {
        // Given
        val postId = 1L

        // When
        val resultActions = mvc.perform(get("/api/v1/posts/$postId"))
            .andDo(print())

        // Then
        val post = postService.findById(postId).get()

        resultActions
            .andExpect(handler().methodName("getItem"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(post.id))
            .andExpect(jsonPath("$.title").value(post.title))
            .andExpect(jsonPath("$.content").value(post.content))
            .andExpect(jsonPath("$.authorId").value(post.author.id))
            .andExpect(jsonPath("$.authorName").value(post.author.name))
    }

    @Test
    @DisplayName("글 다건 조회")
    fun `글 목록 조회가 성공한다`() {
        // When
        val resultActions = mvc.perform(get("/api/v1/posts"))
            .andDo(print())

        // Then
        val posts = postService.findAll()

        resultActions
            .andExpect(handler().methodName("getItems"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(posts.size))
    }

    @Test
    @DisplayName("인증 없이 글 작성 시도")
    fun `인증 없이는 글을 작성할 수 없다`() {
        // When
        val resultActions = mvc.perform(
            post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "title": "제목",
                        "content": "내용"
                    }
                    """.trimIndent()
                )
        ).andDo(print())

        // Then
        resultActions
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.resultCode").value("401-1"))
    }
}
