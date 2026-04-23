package com.cjlogistics.wms.master.controller

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.master.service.WmsMaster0010Service
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * - 마스터관리 : 차량 관리
 * - 화면명 : WMS_MASTER_0010
 * - 작성일자 : 2026-04-23
 * - 작성자 : pote22
 */
@RestController
@RequestMapping("/api/master/vehicle")
class WmsMaster0010Controller(private val wmsMaster0010Service: WmsMaster0010Service) {

    private val LOG = LoggerFactory.getLogger(WmsMaster0010Controller::class.java)

    /** 차량관리 목록 조회 */
    @PostMapping("/getList")
    fun getList(@RequestBody paramMap: Map<String, Any>): ResponseEntity<Response> {
        LOG.info("---> selectVehicle : " + paramMap)
        return try {
            var response = wmsMaster0010Service.getList(paramMap)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            LOG.error("getList error : ${e.message}", e)
            ResponseEntity.status(500)
                    .body(
                            Response(
                                    resultCode = "9999",
                                    resultMessage = "서버 오류가 발생했습니다. 담당자에게 문의하여 주십시오.",
                                    accessToken = "",
                                    expireDate = null,
                                    data = null
                            )
                    )
        }
    }

    /** 차량관리 저장 */
    @PostMapping("/saveVehicle")
    fun saveVehicle(@RequestBody paramMap: Map<String, Any>): ResponseEntity<Response> {
        LOG.info("---> saveVehicle : " + paramMap)
        return try {
            ResponseEntity.ok(wmsMaster0010Service.saveVehicle(paramMap))
        } catch (ie: IllegalArgumentException) {
            ResponseEntity.status(400)
                    .body(
                            Response(
                                    resultCode = "0001",
                                    resultMessage = ie.message,
                                    accessToken = "",
                                    expireDate = null,
                                    data = null
                            )
                    )
        } catch (e: Exception) {
            LOG.error("saveList error : ${e.message}", e)
            ResponseEntity.status(500)
                    .body(
                            Response(
                                    resultCode = "9999",
                                    resultMessage = "서버 오류가 발생했습니다. 담당자에게 문의하여 주십시오.",
                                    accessToken = "",
                                    expireDate = null,
                                    data = null
                            )
                    )
        }
    }

    /** 차량관리 삭제 */
    @PostMapping("/removeVehicle")
    fun removeVehicle(@RequestBody paramMap: Map<String, Any>): ResponseEntity<Response> {
        LOG.info("---> removeVehicle : " + paramMap)
        return try {
            ResponseEntity.ok(wmsMaster0010Service.removeVehicle(paramMap))
        } catch (ie: IllegalArgumentException) {
            ResponseEntity.status(400)
                    .body(
                            Response(
                                    resultCode = "0001",
                                    resultMessage = ie.message,
                                    accessToken = "",
                                    expireDate = null,
                                    data = null
                            )
                    )
        } catch (e: Exception) {
            LOG.error("removeVehicle error : ${e.message}", e)
            ResponseEntity.status(500)
                    .body(
                            Response(
                                    resultCode = "9999",
                                    resultMessage = "서버 오류가 발생했습니다. 담당자에게 문의하여 주십시오.",
                                    accessToken = "",
                                    expireDate = null,
                                    data = null
                            )
                    )
        }
    }
}
