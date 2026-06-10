package com.cjlogistics.wms.stock.controller

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.stock.service.WmsStock0010Service
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * - 재고관리   : 재고현황
 * - 화면명     : WMS_STOCK_0010
 * - 작성일자   : 2026-06-10
 * - 작성자     : pote22
 */
@RestController
@RequestMapping("/api/stock/0010")
class WmsStock0010Controller(private val wmsStock0010Service: WmsStock0010Service) {

    val LOG = LoggerFactory.getLogger(WmsStock0010Controller::class.java)

    /** 재고현황 조회 */
    @PostMapping("/getList")
    fun getList(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> getList : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsStock0010Service.getList(paramMap))
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
}