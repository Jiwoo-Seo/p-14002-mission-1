package com.back.domain.member.member.repository

import com.back.domain.member.member.entity.Member
import com.back.standard.search.MemberSearchKeywordTypeV1
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface MemberRepositoryCustom {
    fun findByKw(kwType: MemberSearchKeywordTypeV1, kw: String, pageable: Pageable): Page<Member>
}
