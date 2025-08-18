package com.back.standard.search

enum class PostSearchKeywordTypeV1 {
    all,      // 전체 검색 (제목 + 내용)
    title,    // 제목만 검색
    content,  // 내용만 검색
    author    // 작성자명 검색
}
