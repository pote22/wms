package com.cjlogistics.wms.home.controller

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.home.service.WmsHome0010Service
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * - 홈 : 공지사항 관리
 * - 화면명 : WMS_HOME_0010
 * - 작성일자 : 2026-04-22
 * - 작성자 : pote22
 */
@RestController
@RequestMapping("/api/home")
class HomeController(private val wmsHome0010Service: WmsHome0010Service) {

    private val LOG = LoggerFactory.getLogger(HomeController::class.java)

    /** 공지사항 목록 조회 */
    @PostMapping("/getList")
    fun home(@RequestBody paramMap: Map<String, Any>): ResponseEntity<Response> {
        LOG.info("---> GET_LIST Controller 호출")

        return try {
            var response = wmsHome0010Service.getList(paramMap)
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

    /** 공지사항 내용 저장 */
    @PostMapping("/saveList")
    fun saveList(@RequestBody paramMap: Map<String, Any>): ResponseEntity<Response> {
        LOG.info("---> SAVE_LIST Controller 호출")

        return try {
            var response = wmsHome0010Service.saveList(paramMap)
            ResponseEntity.ok(response)
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

    /** 공지사항 내용 삭제 */
    @PostMapping("/deleteList")
    fun deleteList(@RequestBody paramMap: Map<String, Any>): ResponseEntity<Response> {
        LOG.info("---> DELETE_LIST Controller 호출")

        return try {
            var response = wmsHome0010Service.deleteList(paramMap)
            ResponseEntity.ok(response)
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
            LOG.error("deleteList error : ${e.message}", e)
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
