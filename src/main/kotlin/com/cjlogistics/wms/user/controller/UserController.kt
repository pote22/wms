package com.cjlogistics.wms.user.controller

import com.cjlogistics.wms.user.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(private val userService: UserService) {

    private val LOG = LoggerFactory.getLogger(UserController::class.java)

    @PostMapping("/login")
    fun login(@RequestBody param : Map<String, Any>): Map<String, Any> {

        LOG.info("---> UserController 호출")
        
        return userService.login(param)
    }
}