package com.back.standard.search

enum class MemberSearchKeywordTypeV1 {
    all,      // 전체 검색 (사용자명 + 이름)
    username, // 사용자명만 검색
    nickname  // 이름(nickname)만 검색
}
