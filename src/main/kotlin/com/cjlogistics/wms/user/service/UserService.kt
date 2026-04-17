package com.cjlogistics.wms.user.service

import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.auth.JwtProvider
import com.cjlogistics.wms.user.mapper.UserMapper
import org.springframework.stereotype.Service

@Service
class UserService(private val userMapper: UserMapper, private val jwtProvider: JwtProvider) {

    private val LOG = org.slf4j.LoggerFactory.getLogger(UserService::class.java)

    /**
     * 로그인 서비스
     * resultCode - 정상: 00 / 로그인 틀림: 02 / 기타에러: 99
     */
    fun login(paramMap : Map<String, Any>): Response {
        LOG.info("---> UserService.login 호출")
        LOG.info("---> UserService.login request: $paramMap")

        var user = userMapper.findByLogin(paramMap) ?: throw IllegalArgumentException("로그인 정보가 일치하지 않습니다.")

        //LOG.info("---> UserService.login user: $user")

        // JWT 토큰 생성
        val usernm      = user.get("USER_NM")?.toString() ?: user.get("userNm")?.toString() ?: ""
        var accessToken = jwtProvider.generateToken(usernm)
        var expireDate  = jwtProvider.getExpireDt(accessToken)

        return Response(
            resultCode      = "0000",
            resultMessage   = "로그인 성공",
            accessToken     = accessToken,
            expireDate      = expireDate,   
            data            = user
        )
    }
}
