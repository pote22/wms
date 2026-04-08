package com.cjlogistics.wms.auth

import org.springframework.context.annotation.Configuration
import org.springframework.boot.context.properties.ConfigurationProperties

@Configuration
@ConfigurationProperties(prefix = "security.jwt")
data class JwtProperties(
    val secret : String             = "CJLOGISTICS_ACCESS_SECRET_TOKEN_KEY_2026_1234567890",   // JWT 서명에 사용할 비밀 키 (실제 운영에서는 안전하게 관리해야 함)
    val issuer : String             = "wms",                                                   // JWT 발급자 정보 (예: 애플리케이션 이름)
    val expirationSeconds : Long    = 3600L                                                    // JWT 토큰의 유효 기간 (초 단위, 예: 3600초 = 1시간)
)