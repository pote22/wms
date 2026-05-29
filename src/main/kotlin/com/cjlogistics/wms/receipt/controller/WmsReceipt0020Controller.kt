package com.cjlogistics.wms.receipt.controller

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.receipt.service.WmsReceipt0020Service
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * - 입고관리   : 입고예정/확정
 * - 화면명     : WMS_RECEIPT_0020
 * - 작성일자   : 2026-05-29
 * - 작성자     : pote22
 */
@RestController
@RequestMapping("/api/receipt/0020")
class WmsReceipt0020Controller(private val wmsReceipt0020Service : WmsReceipt0020Service) {

    val LOG = LoggerFactory.getLogger(WmsReceipt0020Controller::class.java)

    /** 입고목록 조회 */
    @PostMapping("/getList")
    fun getList(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> getList : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsReceipt0020Service.getList(paramMap))
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

    /** 입고상세 : 비고정보 저장 */
    @PostMapping("/saveRemarkInfo")
    fun saveRemarkInfo(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> saveRemarkInfo : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsReceipt0020Service.saveRemarkInfo(paramMap))
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
            LOG.error("saveRemarkInfo error : ${e.message}")
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

    /** 입고확정 */
    @PostMapping("/saveReceiptConfirm")
    fun saveReceiptConfirm(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> saveReceiptConfirm : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsReceipt0020Service.saveReceiptConfirm(paramMap))
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
            LOG.error("saveReceiptConfirm error : ${e.message}")
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