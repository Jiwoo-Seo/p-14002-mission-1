package com.back.domain.member.member.dto

import com.back.domain.member.member.entity.Member
import java.time.LocalDateTime

data class MemberWithUsernameDto(
    val id: Long,
    val createDate: LocalDateTime,
    val modifyDate: LocalDateTime,
    val isAdmin: Boolean,
    val username: String,
    val name: String,
    val profileImageUrl: String,
) {
    constructor(member: Member) : this(
        id = member.id!!,
        createDate = member.createDate ?: LocalDateTime.now(),
        modifyDate = member.modifyDate ?: LocalDateTime.now(),
        isAdmin = member.isAdmin,
        username = member.username!!,
        name = member.name ?: "",
        profileImageUrl = member.profileImgUrlOrDefault
    )
}
