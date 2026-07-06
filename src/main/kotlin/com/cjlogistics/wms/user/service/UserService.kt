package com.cjlogistics.wms.user.service

import com.cjlogistics.wms.auth.JwtProvider
import com.cjlogistics.wms.common.mapper.CommonMapper
import com.cjlogistics.wms.dto.Response
import com.cjlogistics.wms.user.mapper.UserMapper
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userMapper: UserMapper,
    private val jwtProvider: JwtProvider,
    private val commonMapper: CommonMapper
) {

    private val LOG = org.slf4j.LoggerFactory.getLogger(UserService::class.java)

    /** 로그인 서비스 resultCode - 정상: 00 / 로그인 틀림: 02 / 기타에러: 99 */
    fun login(paramMap: Map<String, Any>): Response {
        LOG.info("---> UserService.login request: $paramMap")

        var user = userMapper.findByLogin(paramMap) ?: throw IllegalArgumentException("로그인 정보가 일치하지 않습니다.")
        // JWT 토큰 생성 (user Map을 그대로 전달 → claims에 userId/userNm/adminYn/role 포함)
        var accessToken = jwtProvider.generateToken(user)
        var expireDate  = jwtProvider.getExpireDt(accessToken)

        // 로그인 성공 로그 등록
        try {
            commonMapper.insertUserLog(mapOf(
                "logId"     to UUID.randomUUID().toString(),
                "userId"    to (paramMap["userId"] ?: ""),
                "progId"    to "LOGIN",
                "logDtl"    to "WMS 로그인",
                "ipAddr"    to (paramMap["ipAddr"] ?: ""),
                "userAgent" to (paramMap["userAgent"] ?: "")
            ))
        } catch (e: Exception) {
            LOG.warn("로그인 로그 등록 실패 (무시): ${e.message}")
        }

        return Response(
                resultCode = "0000",
                resultMessage = "로그인 성공",
                accessToken = accessToken,
                expireDate = expireDate,
                data = user
        )
    }

    /** 사용자 권한별 센터&고객사 리스트 조회 */
    fun getUserAuthWhList(paramMap: Map<String, Any>): Response {
        LOG.info("---> UserService.getUserAuthWhList request: $paramMap")

        var authWhList = userMapper.selectUserAuthWhList(paramMap)

        return Response(resultCode = "0000", resultMessage = "조회완료", data = authWhList)
    }
}
