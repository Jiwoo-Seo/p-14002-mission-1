package com.back.standard.util

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
// String 확장 함수: Base64 URL-safe 인코딩
fun String.base64Encode(): String {
    // URL-safe 방식으로 인코딩 (padding 포함)
    return Base64.UrlSafe.encode(this.toByteArray())
}

@OptIn(ExperimentalEncodingApi::class)
// String 확장 함수: Base64 URL-safe 디코딩
fun String.base64Decode(): String {
    // URL-safe 방식으로 디코딩
    return Base64.UrlSafe.decode(this).decodeToString()
}

fun <T : Any> T?.getOrThrow(): T {
    return this ?: throw NoSuchElementException()
}