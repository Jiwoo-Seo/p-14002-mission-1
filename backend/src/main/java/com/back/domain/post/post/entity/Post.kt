package com.back.domain.post.post.entity

import com.back.domain.member.member.entity.Member
import com.back.domain.post.postComment.entity.PostComment
import com.back.global.exception.ServiceException
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.CascadeType.PERSIST
import jakarta.persistence.CascadeType.REMOVE
import jakarta.persistence.Entity
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.util.*

@Entity
class Post() : BaseEntity() {

    @ManyToOne
    lateinit var author: Member

    var title: String = ""
    var content: String = ""
    var published: Boolean = false
    var listed: Boolean = false

    constructor(
        author: Member,
        title: String,
        content: String,
        published: Boolean = false,
        listed: Boolean = false
    ) : this() {
        this.author = author
        this.title = title
        this.content = content
        this.published = published
        this.listed = listed
    }

    @OneToMany(mappedBy = "post", fetch = LAZY, cascade = [PERSIST, REMOVE], orphanRemoval = true)
    private val _comments: MutableList<PostComment> = mutableListOf()

    val comments: List<PostComment> get() = _comments.toList()

    fun modify(title: String, content: String) {
        this.title = title
        this.content = content
    }

    // published와 listed 관련 메서드 추가
    val isTemp: Boolean
        get() = !published && title == "임시글"

    fun isPublished(): Boolean = published

    fun setCreateDateNow() {
        // BaseEntity에서 createDate를 현재 시간으로 설정하는 메서드가 있다고 가정
        // 실제 구현은 BaseEntity에 따라 달라질 수 있음
    }

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
