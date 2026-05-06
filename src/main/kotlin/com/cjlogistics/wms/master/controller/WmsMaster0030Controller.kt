package com.cjlogistics.wms.master.controller

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.master.service.WmsMaster0030Service
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * - 마스터관리 : 품목관리
 * - 화면ID     : CJ_WMS_MASTER_0030
 * - 작성일자   : 2026-04-30
 * - 작성자     : 허민(pote22)
 */
@RestController
@RequestMapping("/api/master/part")
class WmsMaster0030Controller(private val wmsMaster0030Service : WmsMaster0030Service) {

    private val LOG = LoggerFactory.getLogger(WmsMaster0030Controller::class.java)

    /** 품목목록 조회 */
    @PostMapping("/getList")
    fun getList(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> getList : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsMaster0030Service.getList(paramMap))
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

    /** 품목정보 저장 */
    @PostMapping("/saveProdInfo")
    fun savePartInfo(@RequestBody paramMap : Map<String, Any>): ResponseEntity<Response> {
        LOG.info("---> savePartInfo : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsMaster0030Service.saveProdInfo(paramMap))
        } catch (ie : IllegalArgumentException) {
            ResponseEntity.status(400).body(
                Response (
                    resultCode      = "0001",
                    resultMessage   = ie.message,
                    accessToken     = "",
                    expireDate      = null,
                    data            = null
                )
            )
        } catch (e : Exception) {
            LOG.error("savePartInfo error : ${e.message}")
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

    /** 품목정보 삭제 */
    @PostMapping("/removeProdInfo")
    fun removePartInfo(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> removePartInfo : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsMaster0030Service.removeProdInfo(paramMap))
        } catch (ie : IllegalArgumentException) {
            ResponseEntity.status(400).body(
                Response (
                    resultCode      = "0001",
                    resultMessage   = ie.message,
                    accessToken     = "",
                    expireDate      = null,
                    data            = null
                )
            )
        } catch (e : Exception) {
            LOG.error("removePartInfo error : ${e.message}")
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