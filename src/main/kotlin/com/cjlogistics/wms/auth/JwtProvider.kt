package com.cjlogistics.wms.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.Date
import org.springframework.stereotype.Component

@Component
class JwtProvider(private val jwtProperties: JwtProperties) {

    // JWT 서명에 사용할 비밀 키를 Key 객체로 변환
    private val secretKey: Key =
            Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))

    // JWT(Access Token) 토큰 생성
    fun generateToken(user: Map<String, Any?>): String {
        val now = Date()
        val expiresDt = Date(now.time + jwtProperties.expiration * 1000)

        return Jwts.builder()
                .setSubject(user["user_id"]?.toString() ?: "")
                .claim("userNm", user["user_nm"]?.toString())
                .claim("adminYn", user["admin_yn"]?.toString())
                .claim("role", user["role"]?.toString())
                .setIssuer(jwtProperties.issuer)
                .setIssuedAt(now)
                .setExpiration(expiresDt)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact()
    }

    // JWT 토큰에서 사용자 이름 추출
    fun getUsernameFromToken(token: String): String {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
                .subject
    }

    // JWT 토큰에서 만료 날짜 추출
    fun getExpireDt(token: String): Date {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
                .expiration
    }

    // JWT 토큰 유효성 검사
    fun validateToken(token: String): Boolean {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token)
            return true
        } catch (ex: Exception) {
            // 토큰이 유효하지 않거나 만료된 경우 예외 처리 (로그 등으로 기록 가능)
        }
        return false
    }
}
