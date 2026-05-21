package com.cjlogistics.wms.common.service

import com.cjlogistics.wms.common.mapper.CommonCodeMapper
import com.cjlogistics.wms.dto.Response
import org.springframework.stereotype.Service

@Service
class CommonCodeService(private val commonCodeMapper: CommonCodeMapper) {
    fun getCommonCodeList(paramMap: Map<String, Any>) : Response {
        val list = commonCodeMapper.selectCommonCodeList(paramMap)

        return Response (
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }

    fun getProdSearchList(paramMap: Map<String, Any>): Response {
        val list = commonCodeMapper.selectProdSearchList(paramMap)

        return Response(
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }

    fun getVehicleSearchList(paramMap: Map<String, Any>): Response {
        val list = commonCodeMapper.selectVehicleSearchList(paramMap)

        return Response(
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }

    fun getZoneSearchList(paramMap: Map<String, Any>): Response {
        val list = commonCodeMapper.selectZoneSearchList(paramMap)

        return Response(
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }

    fun getClientSearchList(paramMap: Map<String, Any>): Response {
        val list = commonCodeMapper.selectClientSearchList(paramMap)

        return Response(
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }

    fun getLocSearchList(paramMap: Map<String, Any>): Response {
        val list = commonCodeMapper.selectLocSearchList(paramMap)

        return Response(
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }
}