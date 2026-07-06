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
}
