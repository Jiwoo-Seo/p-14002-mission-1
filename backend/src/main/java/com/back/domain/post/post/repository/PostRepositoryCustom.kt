package com.back.domain.post.post.repository

import com.back.domain.member.member.entity.Member
import com.back.domain.post.post.entity.Post
import com.back.standard.search.PostSearchKeywordTypeV1
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PostRepositoryCustom {
    fun findByKw(
        searchKeywordType: PostSearchKeywordTypeV1,
        searchKeyword: String,
        author: Member?,
        published: Boolean?,
        listed: Boolean?,
        pageable: Pageable
    ): Page<Post>
}
