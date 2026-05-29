package com.cjlogistics.wms.receipt.service

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.receipt.mapper.WmsReceipt0020Mapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WmsReceipt0020Service(private val wmsReceipt0020Mapper : WmsReceipt0020Mapper) {

    private val LOG = LoggerFactory.getLogger(WmsReceipt0020Service::class.java)

    /** 입고예정/확정 리스트 조회 */
    fun getList(paramMap : Map<String, Any>) : Response {
        val list = wmsReceipt0020Mapper.selectReceiptList(paramMap)

        return Response(
            resultCode    = "0000",
            resultMessage = "정상적으로 처리되었습니다.",
            accessToken   = "",
            expireDate    = null,
            data          = list
        )
    }

    /** 비고 저장 */
    fun saveRemarkInfo(paramMap : Map<String, Any>) : Response {
        val list = paramMap["list"] as? List<Map<String, Any>> ?: emptyList()

        if (list.isEmpty()) {
            throw IllegalArgumentException("저장할 데이터가 없습니다.")
        }

        list.forEach { info -> 
            wmsReceipt0020Mapper.updateReceiptRmkInfo(info)
        }

        return Response(
            resultCode    = "0000",
            resultMessage = "저장 되었습니다.",
            accessToken   = "",
            expireDate    = null,
            data          = null
        )
    }

    /** 입고확정 처리 */
    @Transactional
    fun saveReceiptConfirm(paramMap : Map<String, Any>) : Response {
        val list = paramMap["list"] as? List<Map<String, Any>> ?: emptyList();

        if (list.isEmpty()) {
            throw IllegalArgumentException("입고 확정 데이터가 없습니다.")
        }

        list.forEach { info ->
            wmsReceipt0020Mapper.updateReceiptConfirmInfo(info)
        }

        return Response(
            resultCode    = "0000",
            resultMessage = "저장 되었습니다.",
            accessToken   = "",
            expireDate    = null,
            data          = null
        )
    }
}