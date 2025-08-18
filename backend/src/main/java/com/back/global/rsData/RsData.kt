package com.back.global.rsData

import com.fasterxml.jackson.annotation.JsonIgnore

class RsData<T>(
    val resultCode: String,
    @field:JsonIgnore val statusCode: Int,
    val msg: String,
    val data: T?
) {
    @JvmOverloads
    constructor(resultCode: String, msg: String, data: T? = null) : this(
        resultCode,
        resultCode.split("-", limit = 2)[0].toInt(),
        msg,
        data
    )
    
    fun <U> newDataOf(newData: U): RsData<U> {
        return RsData(resultCode, statusCode, msg, newData)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RsData<*>

        if (resultCode != other.resultCode) return false
        if (statusCode != other.statusCode) return false
        if (msg != other.msg) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = resultCode.hashCode()
        result = 31 * result + statusCode
        result = 31 * result + msg.hashCode()
        result = 31 * result + (data?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "RsData(resultCode='$resultCode', statusCode=$statusCode, msg='$msg', data=$data)"
    }
}
