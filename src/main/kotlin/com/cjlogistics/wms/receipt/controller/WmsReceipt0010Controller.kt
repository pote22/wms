package com.cjlogistics.wms.receipt.controller

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.receipt.service.WmsReceipt0010Service
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * - 입고관리   : 입고등록
 * - 화면명     : WMS_RECEIPT_0010
 * - 작성일자   : 2026-05-20
 * - 작성자     : pote22
 */
@RestController
@RequestMapping("/api/receipt/0010")
class WmsReceipt0010Controller(private val wmsReceipt0010Service: WmsReceipt0010Service) {

    val LOG = LoggerFactory.getLogger(WmsReceipt0010Controller::class.java)

    /** 품목목록 조회 */
    @PostMapping("/getList")
    fun getList(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> getList : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsReceipt0010Service.getList(paramMap))
        } catch (e: Exception) {
            LOG.error("getList error : ${e.message}")
            ResponseEntity.status(500).body(
                Response (
                    resultCode      = "9999",
                    resultMessage   = "서버 오류가 발생했습니다. 담당자에게 문의하여 주십시오.",
                    accessToken     = "",
                    expireDate      = null,
                    data            = null
                )
            )
        }   
    }

    @PostMapping("/getKeyInfo")
    fun getKeyInfo(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> getKeyInfo : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsReceipt0010Service.getKeyInfo(paramMap))
        } catch (e: Exception) {
            LOG.error("getList error : ${e.message}")
            ResponseEntity.status(500).body(
                Response (
                    resultCode      = "9999",
                    resultMessage   = "서버 오류가 발생했습니다. 담당자에게 문의하여 주십시오.",
                    accessToken     = "",
                    expireDate      = null,
                    data            = null
                )
            )
        }   
    }

    /** 엑셀 업로드 유효성 검증 */
    @PostMapping("/getCheckList")
    fun getCheckList(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> getCheckList : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsReceipt0010Service.getCheckList(paramMap))
        } catch (e : Exception) {
            LOG.error("getCheckList error : ${e.message}")
            ResponseEntity.status(500).body(
                Response(
                    resultCode      = "9999",
                    resultMessage   = "서버 오류가 발생했습니다. 담당자에게 문의하여 주십시오.",
                    accessToken     = "",
                    expireDate      = null,
                    data            = null
                )
            )
        }
    }

    /** 입고 등록 */
    @PostMapping("/saveReceiptList")
    fun saveReceipt(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> saveReceipt : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsReceipt0010Service.saveReceiptList(paramMap))
        } catch (ie : IllegalArgumentException) {
            ResponseEntity.status(400).body(
                Response(
                    resultCode      = "0001",
                    resultMessage   = ie.message,
                    accessToken     = "",
                    expireDate      = null,
                    data            = null
                )
            )
        } catch (e : Exception) {
            LOG.error("saveReceipt error : ${e.message}")
            ResponseEntity.status(500).body(
                Response(
                    resultCode      = "9999",
                    resultMessage   = "서버 오류가 발생했습니다. 담당자에게 문의하여 주십시오.",
                    accessToken     = "",
                    expireDate      = null,
                    data            = null
                )
            )
        }
    }
}