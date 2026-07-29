package com.cjlogistics.wms.common.controller

import com.cjlogistics.wms.common.service.CommonService
import com.cjlogistics.wms.dto.Response
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/common")
class CommonController(private val commonService : CommonService) {

    private val LOG = LoggerFactory.getLogger(CommonController::class.java)

    /** 사용자 로그 등록 (페이지 접근 / 로그아웃) */
    @PostMapping("/insertUserLog")
    fun insertUserLog(@RequestBody paramMap : Map<String, Any>, request : HttpServletRequest): ResponseEntity<Response> {
        return try {
            val map = paramMap.toMutableMap()
            map["ipAddr"]    = request.remoteAddr               ?: ""
            map["userAgent"] = request.getHeader("User-Agent")  ?: ""

            ResponseEntity.ok(commonService.insertUserLog(map))
        } catch (e: Exception) {
            LOG.error("insertUserLog error : ${e.message}")
            ResponseEntity.status(500).body(
                Response(
                    resultCode    = "9999",
                    resultMessage = "서버 오류가 발생했습니다. 담당자에게 문의하여 주십시오.",
                    accessToken   = "",
                    expireDate    = null,
                    data          = null
                )
            )
        }
    }

    /** 품목 검색 팝업 조회 */
    @PostMapping("/getProdSearchList")
    fun getProdSearchList(@RequestBody paramMap: Map<String, Any>): ResponseEntity<Response> {
        LOG.info("---> getProdSearchList : $paramMap")
        return try {
            ResponseEntity.ok(commonService.getProdSearchList(paramMap))
        } catch (e: Exception) {
            LOG.error("getProdSearchList error : ${e.message}")
            ResponseEntity.status(500).body(
                Response(
                    resultCode    = "9999",
                    resultMessage = "서버 오류가 발생했습니다. 담당자에게 문의하여 주십시오.",
                    accessToken   = "",
                    expireDate    = null,
                    data          = null
                )
            )
        }
    }

    /** 차량 검색 팝업 조회 */
    @PostMapping("/getVehicleSearchList")
    fun getVehicleSearchList(@RequestBody paramMap: Map<String, Any>): ResponseEntity<Response> {
        LOG.info("---> getVehicleSearchList : $paramMap")
        return try {
            ResponseEntity.ok(commonService.getVehicleSearchList(paramMap))
        } catch (e: Exception) {
            LOG.error("getVehicleSearchList error : ${e.message}")
            ResponseEntity.status(500).body(
                Response(
                    resultCode    = "9999",
                    resultMessage = "서버 오류가 발생했습니다. 담당자에게 문의하여 주십시오.",
                    accessToken   = "",
                    expireDate    = null,
                    data          = null
                )
            )
        }
    }

    /** 존 검색 팝업 조회 */
    @PostMapping("/getZoneSearchList")
    fun getZoneSearchList(@RequestBody paramMap: Map<String, Any>): ResponseEntity<Response> {
        LOG.info("---> getZoneSearchList : $paramMap")
        return try {
            ResponseEntity.ok(commonService.getZoneSearchList(paramMap))
        } catch (e: Exception) {
            LOG.error("getZoneSearchList error : ${e.message}")
            ResponseEntity.status(500).body(
                Response(
                    resultCode    = "9999",
                    resultMessage = "서버 오류가 발생했습니다. 담당자에게 문의하여 주십시오.",
                    accessToken   = "",
                    expireDate    = null,
                    data          = null
                )
            )
        }
    }

    /** 거래처 검색 팝업 조회 */
    @PostMapping("/getClientSearchList")
    fun getClientSearchList(@RequestBody paramMap: Map<String, Any>): ResponseEntity<Response> {
        LOG.info("---> getClientSearchList : $paramMap")
        return try {
            ResponseEntity.ok(commonService.getClientSearchList(paramMap))
        } catch (e: Exception) {
            LOG.error("getClientSearchList error : ${e.message}")
            ResponseEntity.status(500).body(
                Response(
                    resultCode    = "9999",
                    resultMessage = "서버 오류가 발생했습니다. 담당자에게 문의하여 주십시오.",
                    accessToken   = "",
                    expireDate    = null,
                    data          = null
                )
            )
        }
    }

    /** 로케이션 검색 팝업 조회 */
    @PostMapping("/getLocSearchList")
    fun getLocSearchList(@RequestBody paramMap: Map<String, Any>): ResponseEntity<Response> {
        LOG.info("---> getLocSearchList : $paramMap")
        return try {
            ResponseEntity.ok(commonService.getLocSearchList(paramMap))
        } catch (e: Exception) {
            LOG.error("getLocSearchList error : ${e.message}")
            ResponseEntity.status(500).body(
                Response(
                    resultCode    = "9999",
                    resultMessage = "서버 오류가 발생했습니다. 담당자에게 문의하여 주십시오.",
                    accessToken   = "",
                    expireDate    = null,
                    data          = null
                )
            )
        }
    }
}
