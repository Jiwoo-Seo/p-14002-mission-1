package com.back.domain.post.post.dto

import com.back.domain.post.post.entity.Post
import lombok.Getter
import java.time.LocalDateTime

@Getter
class PostWithContentDto private constructor(
    id: Int,
    createDate: LocalDateTime,
    modifyDate: LocalDateTime,
    authorId: Int,
    authorName: String,
    title: String,
    val content: String
) : PostDto(
    id,
    createDate,
    modifyDate,
    authorId,
    authorName,
    title
) {
    constructor(post: Post) : this(
        id = post.id,
        createDate = post.createDate,
        modifyDate = post.modifyDate,
        authorId = post.author.id,
        authorName = post.author.name,
        title = post.title,
        content = post.content
    )
}