package com.back.domain.post.post.dto

import com.back.domain.post.post.entity.Post
import java.time.LocalDateTime

data class PostDto (
    val id: Long,
    val createDate: LocalDateTime,
    val modifyDate: LocalDateTime,
    val authorId: Long,
    val authorName: String,
    val title: String
) {
    constructor(post: Post) : this(
        id = post.id!!,
        createDate = post.createDate!!,
        modifyDate = post.modifyDate!!,
        authorId = post.author.id!!,
        authorName = post.author.name!!,
        title = post.title
    )
}
