package com.cjlogistics.wms.master.controller

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.master.service.WmsMaster0020Service
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/master/0020")
class WmsMaster0020Controller(private val wmsMaster0020Service : WmsMaster0020Service) {

    private val LOG = LoggerFactory.getLogger(WmsMaster0020Controller::class.java)

    /** 거래처 목록 조회 */
    @PostMapping("/getList")
    fun getList(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> getList : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsMaster0020Service.getList(paramMap))
        } catch (e : Exception) {
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

    /** 거래처 정보 저장&업데이트 */
    @PostMapping("/saveClientInfo")
    fun saveClientInfo(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> saveClientInfo : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsMaster0020Service.saveClientInfo(paramMap))
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
            LOG.error("saveClientInfo error : ${e.message}")
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

    /** 거래처정보 삭제 */
    @PostMapping("/removeClientInfo")
    fun removeClientInfo(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> saveClientInfo : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsMaster0020Service.removeClientInfo(paramMap))
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
            LOG.error("removeClientInfo error : ${e.message}")
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

    /** 엑셀업로드 */
    @PostMapping("/getCheckList")
    fun getCheckList(@RequestBody paramMap : Map<String, Any>) : ResponseEntity<Response> {
        LOG.info("---> getCheckList : ${ paramMap }")

        return try {
            ResponseEntity.ok(wmsMaster0020Service.getCheckList(paramMap))
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
