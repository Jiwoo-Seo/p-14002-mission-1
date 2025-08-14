package com.back.global.exception

import com.back.global.rsData.RsData

class ServiceException(
    private val resultCode: String,
    private val msg: String
) : RuntimeException("$resultCode : $msg") {

    fun getRsData(): RsData<Void> {
        return RsData(resultCode, msg, null)
    }
}
