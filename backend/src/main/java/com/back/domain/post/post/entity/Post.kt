package com.back.domain.post.post.entity

import com.back.domain.member.member.entity.Member
import com.back.domain.post.postComment.entity.PostComment
import com.back.global.exception.ServiceException
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.*
import jakarta.persistence.CascadeType.PERSIST
import jakarta.persistence.CascadeType.REMOVE
import jakarta.persistence.FetchType.LAZY
import java.util.*

@Entity
class Post(
    @ManyToOne
    val author: Member,
    
    var title: String,
    var content: String
) : BaseEntity() {

    @OneToMany(mappedBy = "post", fetch = LAZY, cascade = [PERSIST, REMOVE], orphanRemoval = true)
    private val _comments: MutableList<PostComment> = mutableListOf()
    
    val comments: List<PostComment> get() = _comments.toList()

    fun modify(title: String, content: String) {
        this.title = title
        this.content = content
    }

    fun getTitle(): String = title
    fun getContent(): String = content

    fun addComment(author: Member, content: String): PostComment {
        val postComment = PostComment(author, this, content)
        _comments.add(postComment)
        return postComment
    }

    fun findCommentById(id: Long): Optional<PostComment> {
        return _comments
            .firstOrNull { it.id == id }
            ?.let { Optional.of(it) }
            ?: Optional.empty()
    }

    fun deleteComment(postComment: PostComment?): Boolean {
        return postComment?.let { _comments.remove(it) } ?: false
    }

    fun checkActorCanModify(actor: Member) {
        if (author != actor) {
            throw ServiceException("403-1", "${id}번 글 수정권한이 없습니다.")
        }
    }

    fun checkActorCanDelete(actor: Member) {
        if (author != actor) {
            throw ServiceException("403-2", "${id}번 글 삭제권한이 없습니다.")
        }
    }
}
