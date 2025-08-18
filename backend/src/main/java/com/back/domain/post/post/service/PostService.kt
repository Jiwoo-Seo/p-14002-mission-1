package com.back.domain.post.post.service

import com.back.domain.member.member.entity.Member
import com.back.domain.post.post.entity.Post
import com.back.domain.post.post.repository.PostRepository
import com.back.domain.post.postComment.entity.PostComment
import com.back.global.rsData.RsData
import com.back.standard.search.PostSearchKeywordTypeV1
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicBoolean

@Service
class PostService(
    private val postRepository: PostRepository
) {

    fun count(): Long = postRepository.count()

    fun countByPublished(published: Boolean): Long {
        return postRepository.countByPublished(published)
    }

    fun countByListed(listed: Boolean): Long {
        return postRepository.countByListed(listed)
    }

    fun write(author: Member, title: String, content: String, published: Boolean, listed: Boolean): Post {
        val post = Post(author, title, content, published, listed)
        return postRepository.save(post)
    }

    fun findAllByOrderByIdDesc(): List<Post> {
        return postRepository.findAllByOrderByIdDesc()
    }

    fun findById(id: Long): Post? = postRepository.findById(id).orElse(null)

    fun delete(post: Post) {
        postRepository.delete(post)
    }

    fun modify(post: Post, title: String, content: String, published: Boolean, listed: Boolean) {
        val wasTemp = post.isTemp

        post.title = title
        post.content = content
        post.published = published
        post.listed = listed

        if (wasTemp && !post.isTemp) {
            post.setCreateDateNow()
        }
    }

    fun flush() {
        postRepository.flush() // em.flush(); 와 동일
    }

    fun findLatest(): Post? = postRepository.findFirstByOrderByIdDesc()

    fun findByListedPaged(listed: Boolean, page: Int, pageSize: Int): Page<Post> {
        return findByListedPaged(listed, PostSearchKeywordTypeV1.all, "", page, pageSize)
    }

    fun findByListedPaged(
        listed: Boolean,
        searchKeywordType: PostSearchKeywordTypeV1,
        searchKeyword: String,
        page: Int,
        pageSize: Int
    ): Page<Post> {
        val pageable: Pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("id")))
        return postRepository.findByKw(searchKeywordType, searchKeyword, null, null, listed, pageable)
    }

    fun findByAuthorPaged(author: Member, page: Int, pageSize: Int): Page<Post> {
        return findByAuthorPaged(author, PostSearchKeywordTypeV1.all, "", page, pageSize)
    }

    fun findByAuthorPaged(
        author: Member,
        searchKeywordType: PostSearchKeywordTypeV1,
        searchKeyword: String,
        page: Int,
        pageSize: Int
    ): Page<Post> {
        val pageable: Pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("id")))
        return postRepository.findByKw(searchKeywordType, searchKeyword, author, null, null, pageable)
    }

    fun findTempOrMake(author: Member): RsData<Post> {
        val isNew = AtomicBoolean(false)

        val post = postRepository.findTop1ByAuthorAndPublishedAndTitleOrderByIdAsc(
            author,
            false,
            "임시글"
        ) ?: run {
            isNew.set(true)
            write(author, "임시글", "", false, false)
        }

        if (isNew.get()) {
            return RsData(
                "201-1",
                "${post.id}번 임시글이 생성되었습니다.",
                post
            )
        }

        return RsData(
            "200-1",
            "${post.id}번 임시글을 불러옵니다.",
            post
        )
    }

    fun writeComment(author: Member, post: Post, content: String): PostComment {
        return post.addComment(author, content)
    }

    fun deleteComment(post: Post, postComment: PostComment): Boolean {
        return post.deleteComment(postComment)
    }

    fun modifyComment(postComment: PostComment, content: String) {
        postComment.modify(content)
    }
}
