package com.cjlogistics.wms.stock.controller

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.stock.service.WmsStock0090Service
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * - 재고관리   : 트랜잭션관리
 * - 화면명     : WMS_STOCK_0090
 * - 작성일자   : 2026-08-11
 * - 작성자     : pote22
 */
@RestController
@RequestMapping("/api/stock/0090")
class WmsStock0090Controller(private val wmsStock0090Service: WmsStock0090Service) {

    val LOG = LoggerFactory.getLogger(WmsStock0010Controller::class.java)

    /** 트랜잭션 조회 */
    @PostMapping("/getList")
    fun getList(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> getList : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsStock0090Service.getList(paramMap))
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
