package com.cjlogistics.wms.dto

data class Response (
    var resultCode      : String? = null,
    var resultMessage   : String? = null,
    var accessToken     : String? = null,
    var expireDate      : Any?    = null,
    var data            : Any?    = null
)