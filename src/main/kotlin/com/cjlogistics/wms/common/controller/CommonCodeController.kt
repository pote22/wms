package com.cjlogistics.wms.common.controller

  import com.cjlogistics.wms.common.service.CommonCodeService
  import com.cjlogistics.wms.dto.Response
  import org.slf4j.LoggerFactory
  import org.springframework.http.ResponseEntity
  import org.springframework.web.bind.annotation.PostMapping
  import org.springframework.web.bind.annotation.RequestBody
  import org.springframework.web.bind.annotation.RequestMapping
  import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/common")
class CommonCodeController(private val commonCodeService: CommonCodeService) {

    private val LOG = LoggerFactory.getLogger(CommonCodeController::class.java)

    /** 챠랑별 톤급 리스트 조회 */
    @PostMapping("/getCommonCodeList")
    fun getCommonCodeList(@RequestBody paramMap: Map<String, Any>) : ResponseEntity<Response> {
        return try {
            ResponseEntity.ok(commonCodeService.getCommonCodeList(paramMap))
        } catch (e: Exception) {
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
            ResponseEntity.ok(commonCodeService.getProdSearchList(paramMap))
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
            ResponseEntity.ok(commonCodeService.getVehicleSearchList(paramMap))
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
            ResponseEntity.ok(commonCodeService.getZoneSearchList(paramMap))
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
            ResponseEntity.ok(commonCodeService.getClientSearchList(paramMap))
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
            ResponseEntity.ok(commonCodeService.getLocSearchList(paramMap))
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