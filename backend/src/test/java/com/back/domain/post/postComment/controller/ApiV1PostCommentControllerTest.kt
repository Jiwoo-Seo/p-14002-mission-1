@file:Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package com.back.domain.post.postComment.controller

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
class ApiV1PostCommentControllerTest(
    @Autowired private val mvc: MockMvc,
    @Autowired private val postService: PostService,
    @Autowired private val memberService: MemberService
) {

    @Test
    @DisplayName("댓글 단건 조회")
    fun `댓글 단건 조회가 성공한다`() {
        // Given
        val postId = 1L
        val commentId = 1L

        // When
        val resultActions = mvc.perform(get("/api/v1/posts/$postId/comments/$commentId"))
            .andDo(print())

        // Then
        val post = postService.findById(postId)!!
        val comment = post.findCommentById(commentId)!!

        resultActions
            .andExpect(handler().handlerType(ApiV1PostCommentController::class.java))
            .andExpect(handler().methodName("getItem"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(comment.id))
            .andExpect(jsonPath("$.authorId").value(comment.author.id))
            .andExpect(jsonPath("$.authorName").value(comment.author.name))
            .andExpect(jsonPath("$.postId").value(comment.post.id))
            .andExpect(jsonPath("$.content").value(comment.content))
    }

    @Test
    @DisplayName("댓글 목록 조회")
    fun `댓글 목록 조회가 성공한다`() {
        // Given
        val postId = 1L

        // When
        val resultActions = mvc.perform(get("/api/v1/posts/$postId/comments"))
            .andDo(print())

        // Then
        val post = postService.findById(postId)!!
        val comments = post.comments

        resultActions
            .andExpect(handler().methodName("getItems"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(comments.size))
    }

    @Test
    @DisplayName("댓글 작성")
    @WithUserDetails("user1")
    fun `댓글 작성이 성공한다`() {
        // Given
        val postId = 1L

        // When
        val resultActions = mvc.perform(
            post("/api/v1/posts/$postId/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "content": "새로운 댓글 내용"
                    }
                    """.trimIndent()
                )
        ).andDo(print())

        // Then
        resultActions
            .andExpect(handler().methodName("write"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.resultCode").value("201-1"))
            .andExpect(jsonPath("$.data.content").value("새로운 댓글 내용"))
    }

    @Test
    @DisplayName("댓글 수정")
    @WithUserDetails("user1")
    fun `댓글 수정이 성공한다`() {
        // Given
        val postId = 1L
        val commentId = 1L

        // When
        val resultActions = mvc.perform(
            put("/api/v1/posts/$postId/comments/$commentId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "content": "수정된 댓글 내용"
                    }
                    """.trimIndent()
                )
        ).andDo(print())

        // Then
        resultActions
            .andExpect(handler().methodName("modify"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.msg").value("${commentId}번 댓글이 수정되었습니다."))
    }

    @Test
    @DisplayName("댓글 삭제")
    @WithUserDetails("user1")
    fun `댓글 삭제가 성공한다`() {
        // Given
        val postId = 1L
        val commentId = 1L

        // When
        val resultActions = mvc.perform(delete("/api/v1/posts/$postId/comments/$commentId"))
            .andDo(print())

        // Then
        resultActions
            .andExpect(handler().methodName("delete"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.msg").value("${commentId}번 댓글이 삭제되었습니다."))
    }

    @Test
    @DisplayName("댓글 수정 - 권한 없음")
    fun `권한이 없는 사용자는 댓글을 수정할 수 없다`() {
        // Given
        val postId = 1L
        val commentId = 1L
        val actor = memberService.findByUsername("user3")!!

        // When
        val resultActions = mvc.perform(
            put("/api/v1/posts/$postId/comments/$commentId")
                .header("Authorization", "Bearer ${actor.apiKey}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "content": "수정 시도"
                    }
                    """.trimIndent()
                )
        ).andDo(print())

        // Then
        resultActions
            .andExpect(handler().methodName("modify"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.msg").value("${commentId}번 댓글 수정권한이 없습니다."))
    }

    @Test
    @DisplayName("댓글 삭제 - 권한 없음")
    fun `권한이 없는 사용자는 댓글을 삭제할 수 없다`() {
        // Given
        val postId = 1L
        val commentId = 1L
        val actor = memberService.findByUsername("user3")!!

        // When
        val resultActions = mvc.perform(
            delete("/api/v1/posts/$postId/comments/$commentId")
                .header("Authorization", "Bearer ${actor.apiKey}")
        ).andDo(print())

        // Then
        resultActions
            .andExpect(handler().methodName("delete"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.resultCode").value("403-2"))
            .andExpect(jsonPath("$.msg").value("${commentId}번 댓글 삭제권한이 없습니다."))
    }
}
