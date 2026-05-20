package com.cjlogistics.wms.receipt.service

import com.cjlogistics.wms.common.mapper.CommonCodeMapper
import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.receipt.mapper.WmsReceipt0010Mapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WmsReceipt0010Service(private val wmsReceipt0010Mapper : WmsReceipt0010Mapper) {

    private val LOG = LoggerFactory.getLogger(WmsReceipt0010Service::class.java)

    /** 입고 리스트 조회 */
    fun getList(paramMap : Map<String, Any>) : Response {
        return Response (
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = null
        )
    }

    /** 키값 정보 조회 */
    fun getKeyInfo(paramMap : Map<String, Any>) : Response {
        val list = wmsReceipt0010Mapper.selectReceptKeyInfo(paramMap)

        return Response (
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }

    /** 입고 저장 */
    fun saveReceipt(paramMap : Map<String, Any>) : Response {
        return Response (
            resultCode = "0000",
            resultMessage = "저장되었습니다.",
            accessToken = "",
            expireDate = null,
            data = null
        )
    }
}