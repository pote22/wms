package com.cjlogistics.wms.common.service

import com.cjlogistics.wms.common.mapper.CommonMapper
import com.cjlogistics.wms.dto.Response
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CommonService(private val commonMapper: CommonMapper) {

    private val LOG = LoggerFactory.getLogger(CommonService::class.java)

    /** 사용자 로그 등록 */
    fun insertUserLog(paramMap: Map<String, Any>): Response {
        LOG.info("---> CommonService.insertUserLog : $paramMap")

        val map      = paramMap.toMutableMap()
        map["logId"] = UUID.randomUUID().toString()
        
        commonMapper.insertUserLog(map)

        return Response(
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = null
        )
    }

    fun getProdSearchList(paramMap: Map<String, Any>): Response {
        val list = commonMapper.selectProdSearchList(paramMap)

        return Response(
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }

    fun getVehicleSearchList(paramMap: Map<String, Any>): Response {
        val list = commonMapper.selectVehicleSearchList(paramMap)

        return Response(
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }

    fun getZoneSearchList(paramMap: Map<String, Any>): Response {
        val list = commonMapper.selectZoneSearchList(paramMap)

        return Response(
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }

    fun getClientSearchList(paramMap: Map<String, Any>): Response {
        val list = commonMapper.selectClientSearchList(paramMap)

        return Response(
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }

    fun getLocSearchList(paramMap: Map<String, Any>): Response {
        val list = commonMapper.selectLocSearchList(paramMap)

        return Response(
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }
}
