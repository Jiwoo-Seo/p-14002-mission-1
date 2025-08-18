package com.back.domain.post.post.repository

import com.back.domain.member.member.entity.Member
import com.back.domain.post.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository

interface PostRepository : JpaRepository<Post, Long>, PostRepositoryCustom {
    fun findAllByOrderByIdDesc(): List<Post>
    fun findFirstByOrderByIdDesc(): Post?
    fun findTop1ByAuthorAndPublishedAndTitleOrderByIdAsc(
        author: Member,
        published: Boolean,
        title: String
    ): Post?
    fun countByPublished(published: Boolean): Long
    fun countByListed(listed: Boolean): Long
}
