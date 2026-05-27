package com.cjlogistics.wms.receipt.service

import com.cjlogistics.wms.common.mapper.CommonCodeMapper
import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.receipt.mapper.WmsReceipt0010Mapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.collections.emptyList

@Service
class WmsReceipt0010Service(private val wmsReceipt0010Mapper : WmsReceipt0010Mapper) {

    private val LOG = LoggerFactory.getLogger(WmsReceipt0010Service::class.java)

    /** 입고 리스트 조회 */
    fun getList(paramMap : Map<String, Any>) : Response {
        val receiptHdrList = wmsReceipt0010Mapper.selectRcptHdrList(paramMap)
        val receiptDtlList = receiptHdrList.takeIf { it.isNotEmpty() }
            ?.let { wmsReceipt0010Mapper.selectRcptDtlList(paramMap) }
            ?: emptyList()
         
        return Response (
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = mapOf("receiptHdrList" to receiptHdrList, "receiptDtlList" to receiptDtlList)
        )
    }

    /** 키값 정보 조회 */
    fun getKeyInfo(paramMap : Map<String, Any>) : Response {
        val list = wmsReceipt0010Mapper.selectRcptKeyInfo(paramMap)

        return Response (
            resultCode      = "0000",
            resultMessage   = "정상적으로 처리되었습니다.",
            accessToken     = "",
            expireDate      = null,
            data            = list
        )
    }

    /** 입고 저장 */
    fun saveReceiptList(paramMap : Map<String, Any>) : Response {
        val hdrList = paramMap["hdrList"] as? List<Map<String, Any>> ?: emptyList()
        val dtlList = paramMap["dtlList"] as? List<Map<String, Any>> ?: emptyList()

        if (hdrList.isEmpty()) throw IllegalArgumentException("저장할 헤더 데이터가 없습니다.")

        // 1. 입고번호 중복 확인
        val status = wmsReceipt0010Mapper.selectRcptStatusInfo(hdrList[0])

        if (!status.isNullOrEmpty()) {
            throw IllegalArgumentException("이미 작업된 입고오더입니다.")
        }

        // 2. 헤더 저장
        wmsReceipt0010Mapper.insertRcptHdrInfo(hdrList[0])

        // 3. 디테일 저장 (순번 1부터 자동 부여)
        dtlList.forEach { 
            dtl -> wmsReceipt0010Mapper.insertRcptDtlInfo(dtl) 
        }

        return Response(
            resultCode    = "0000",
            resultMessage = "저장되었습니다.",
            accessToken   = "",
            expireDate    = null,
            data          = null
        )
    }
}