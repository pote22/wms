package com.cjlogistics.wms.master.controller

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.master.service.WmsMaster0040Service
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * - 마스터관리 : 존&로케이션 관리
 * - 화면ID     : CJ_WMS_MASTER_0040
 * - 작성일자   : 2026-05-08
 * - 작성자     : 허민(pote22)
 */
@RestController
@RequestMapping("/api/master/0040")
class WmsMaster0040Controller(private val wmsMaster0040Service : WmsMaster0040Service) {

    private val LOG = LoggerFactory.getLogger(WmsMaster0040Controller::class.java)

    /** 존 목록 조회 */
    @PostMapping("/getZoneList")
    fun getZoneList(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> getZoneList : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsMaster0040Service.getZoneList(paramMap))
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

    /** 로케이션 목록 조회 */
    @PostMapping("/getLocList")
    fun getLocList(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> getLocList : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsMaster0040Service.getLocList(paramMap))
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

    /** 존&로케이션 정보 저장 */
    @PostMapping("/saveInfo")
    fun saveInfo(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> saveInfo : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsMaster0040Service.saveInfo(paramMap))
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

    /** 엑셀업로드 : 유효성 체크 */
    @PostMapping("/getCheckList")
    fun getCheckList(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> getCheckList : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsMaster0040Service.getCheckList(paramMap))
        } catch (e : Exception) {
            LOG.error("getCheckList error : ${e.message}")
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

