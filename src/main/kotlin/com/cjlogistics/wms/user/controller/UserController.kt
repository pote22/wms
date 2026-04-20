package com.cjlogistics.wms.user.controller

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.user.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(private val userService: UserService) {

    private val LOG = LoggerFactory.getLogger(UserController::class.java)

    /** 사용자 로그인 */
    @PostMapping("/login")
    fun login(@RequestBody paramMap: Map<String, Any>): ResponseEntity<Response> {
        LOG.info("---> LOGIN Controller 호출")

        return try {
            val response = userService.login(paramMap)
            ResponseEntity.ok(response)
        } catch (ie: IllegalArgumentException) {
            LOG.warn("Login failed: ${ie.message}")
            ResponseEntity.status(401)
                    .body(
                            Response(
                                    resultCode = "0002",
                                    resultMessage = "로그인 정보가 일치하지 않습니다.",
                                    accessToken = "",
                                    expireDate = null,
                                    data = null
                            )
                    )
        } catch (e: Exception) {
            LOG.error("Login error: ${e.message}", e)
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

    /** 사용자 권한별 센터&고객사 리스트 조회 */
    @PostMapping("/getUserAuthWhList")
    fun getUserAuthWhList(@RequestBody paramMap: Map<String, Any>): ResponseEntity<Response> {
        LOG.info("---> GET_USER_AUTH_WH_LIST Controller 호출")

        return try {
            val response = userService.getUserAuthWhList(paramMap)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            LOG.error("Get user auth wh list error: ${e.message}", e)
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
