package com.cjlogistics.wms.user.service

import com.cjlogistics.wms.user.mapper.UserMapper
import org.springframework.stereotype.Service

@Service
class UserService(private val userMapper: UserMapper) {

    /**
     * 로그인 서비스
     * resultCode - 정상: 00 / 로그인 틀림: 02 / 기타에러: 99
     */
    fun login(request: Map<String, Any>): Map<String, Any> {
        return try {
            // 로그인 수행
            val result = userMapper.findByLogin(request)
            
            /*
                로그인 결과처리
                - 로그인 성공   : 00 + 사용자 정보
                - 로그인 실패   : 02
                - 기타 에러     : 99
             */
            if (result != null) {
                result + mapOf("resultCode" to "00")
            } else {
                mapOf("resultCode" to "02", "resultMsg" to "로그인 정보가 일치하지 않습니다.")
            }
        } catch (e: Exception) {
            mapOf("resultCode" to "99", "resultMsg" to "에러가 발생하였습니다. 담당자에게 연락하여 문의하여 주십시오.")
        }
    }
}
