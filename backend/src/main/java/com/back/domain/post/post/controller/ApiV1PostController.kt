package com.back.domain.post.post.controller

import com.back.domain.member.member.service.MemberService
import com.back.domain.post.post.dto.PostDto
import com.back.domain.post.post.dto.PostWithContentDto
import com.back.domain.post.post.service.PostService
import com.back.global.exception.ServiceException
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "ApiV1PostController", description = "API 글 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
class ApiV1PostController(
    private val postService: PostService,
    private val memberService: MemberService,
    private val rq: Rq
) {

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "다건 조회")
    fun getItems(): List<PostDto> {
        val items = postService.findAllByOrderByIdDesc()
        return items.map { PostDto(it) }
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "단건 조회")
    fun getItem(@PathVariable id: Long): PostWithContentDto {
        val post = postService.findById(id) ?: throw ServiceException("404-1", "${id}번 글을 찾을 수 없습니다.")
        return PostWithContentDto(post)
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "삭제")
    fun delete(@PathVariable id: Long): RsData<Void> {
        val actor = rq.getActor()
        val post = postService.findById(id) ?: throw ServiceException("404-1", "${id}번 글을 찾을 수 없습니다.")

        post.checkActorCanDelete(actor)
        postService.delete(post)

        return RsData("200-1", "${id}번 글이 삭제되었습니다.")
    }

    data class PostWriteReqBody(
        @field:NotBlank
        @field:Size(min = 2, max = 100)
        val title: String,
        
        @field:NotBlank
        @field:Size(min = 2, max = 5000)
        val content: String
    )

    @PostMapping
    @Transactional
    @Operation(summary = "작성")
    fun write(@Valid @RequestBody reqBody: PostWriteReqBody): RsData<PostDto> {
        val actor = rq.getActor()
        val post = postService.write(actor, reqBody.title, reqBody.content, true, true)

        return RsData(
            "201-1",
            "${post.id}번 글이 작성되었습니다.",
            PostDto(post)
        )
    }

    data class PostModifyReqBody(
        @field:NotBlank
        @field:Size(min = 2, max = 100)
        val title: String,
        
        @field:NotBlank
        @field:Size(min = 2, max = 5000)
        val content: String
    )

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "수정")
    fun modify(
        @PathVariable id: Long,
        @Valid @RequestBody reqBody: PostModifyReqBody
    ): RsData<Void> {
        val actor = rq.getActor()
        val post = postService.findById(id) ?: throw ServiceException("404-1", "${id}번 글을 찾을 수 없습니다.")

        post.checkActorCanModify(actor)
        postService.modify(post, reqBody.title, reqBody.content, true, true)

        return RsData("200-1", "${post.id}번 글이 수정되었습니다.")
    }
}
