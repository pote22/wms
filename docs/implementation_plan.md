# WMS 백엔드 구현 계획 (Backend Implementation Plan)

## 진행 현황 요약

| 단계 | 내용 | 상태 |
|------|------|------|
| Phase 1 | UserMapper.xml 쿼리 개선 | ✅ 완료 |
| Phase 2 | JWT Claims 확장 (JwtProvider) | ✅ 완료 |
| Phase 3 | UserService generateToken 인자 수정 | ✅ 완료 |
| Phase 4 | Spring Security 필터 체인 구성 (JWT 검증) | 🔲 미완료 |
| Phase 5 | 사용자 고객사&센터 목록 조회 API | ✅ 완료 |

---

## Phase 1 — UserMapper.xml 쿼리 개선 ✅

**파일**: `src/main/resources/mapper/UserMapper.xml`

### 변경 내용
- 로그인 쿼리 반환 컬럼 추가: `USER_ID`, `USER_NM`, `ADMIN_YN`, `USER_STS`, `USE_YN`, `ROLE`, `PROFILE_IMG_URL`
- 상태 필터 추가: `USER_STS != '99'` (탈퇴 제외), `USE_YN = 'Y'` (사용 중인 계정만)
- XML 특수문자 이슈: `<>` 연산자 → `!=` 로 교체 (XML 파싱 오류 방지)

---

## Phase 2 — JWT Claims 확장 ✅

**파일**: `src/main/kotlin/com/cjlogistics/wms/auth/JwtProvider.kt`

### 변경 내용
- `generateToken` 시그니처 변경: `String` → `Map<String, Any?>`
- JWT payload에 아래 claims 추가:
  - `sub` (subject): `user_id`
  - `userNm`: `user_nm`
  - `adminYn`: `admin_yn`
  - `role`: `role`
- 프론트엔드가 `accessToken` 하나만으로 사용자 정보를 디코딩할 수 있도록 설계

### 주의사항 — MyBatis Map 키 규칙
- `resultType="map"` 사용 시 `mapUnderscoreToCamelCase` 설정이 적용되지 않음
- PostgreSQL은 컬럼명을 소문자로 반환 → Map 키가 `user_id`, `user_nm`, `admin_yn` 형태
- `role`처럼 언더스코어 없는 컬럼만 그대로 사용 가능
- JwtProvider에서 반드시 **snake_case 키**로 Map 조회해야 함

---

## Phase 3 — UserService generateToken 인자 수정 ✅

**파일**: `src/main/kotlin/com/cjlogistics/wms/user/service/UserService.kt`

### 변경 내용
- 기존: `jwtProvider.generateToken(usernm)` — USER_NM 문자열만 전달 (JwtProvider 시그니처 불일치로 컴파일 오류)
- 수정: `jwtProvider.generateToken(user)` — user Map 전체 전달

---

## Phase 5 — 사용자 고객사&센터 목록 조회 API ✅

### 변경 파일

| 파일 | 작업 |
|------|------|
| `src/main/kotlin/com/cjlogistics/wms/user/mapper/UserMapper.kt` | `selectUserAuthWhList` 메서드 추가 |
| `src/main/resources/mapper/UserMapper.xml` | `selectUserAuthWhList` 쿼리 추가 |
| `src/main/kotlin/com/cjlogistics/wms/user/service/UserService.kt` | `getUserAuthWhList` 메서드 추가 |
| `src/main/kotlin/com/cjlogistics/wms/user/controller/UserController.kt` | `POST /api/user/getUserAuthWhList` 엔드포인트 추가 |
| `src/main/resources/WMS_FUNCTION.sql` | `fn_get_srvc_nm`, `fn_get_wh_nm` PostgreSQL 함수 정의 추가 |

### 구현 내용

**UserMapper.kt**
```kotlin
fun selectUserAuthWhList(request: Map<String, Any>): List<Map<String, Any>>
```

**UserMapper.xml — selectUserAuthWhList**
```sql
SELECT SRVC_CD
     , COALESCE(NULLIF(FN_GET_SRVC_NM(SRVC_CD), ''), '') AS SRVC_NM
     , WH_CD
     , COALESCE(NULLIF(FN_GET_WH_NM(SRVC_CD, WH_CD), ''), '') AS WH_NM
     , BASE_YN
  FROM TB_USER_AUTH
 WHERE USER_ID = #{userId}
 ORDER BY BASE_YN DESC, SRVC_CD, WH_CD
```

**UserController.kt**
```kotlin
@PostMapping("/getUserAuthWhList")
fun getUserAuthWhList(@RequestBody paramMap: Map<String, Any>): ResponseEntity<Response>
```

### DB 함수 (WMS_FUNCTION.sql)

| 함수 | 설명 | 파라미터 |
|------|------|----------|
| `wms.fn_get_srvc_nm(p_srvc_cd)` | TB_SRVC에서 고객사명 조회 | SRVC_CD |
| `wms.fn_get_wh_nm(p_srvc_cd, p_wh_cd)` | TB_WH에서 센터명 조회 | SRVC_CD, WH_CD |

- 두 함수 모두 `WMS.` 스키마 prefix 명시
- 데이터 없거나 예외 발생 시 `NULL` 반환 (plpgsql EXCEPTION 처리)

### API 응답 구조
```json
{
  "resultCode": "0000",
  "resultMessage": "조회완료",
  "data": [
    { "srvc_cd": "GS01", "srvc_nm": "GS칼텍스", "wh_cd": "ICN01", "wh_nm": "인천GSC센터", "base_yn": "Y" },
    { "srvc_cd": "GS01", "srvc_nm": "GS칼텍스", "wh_cd": "BUS01", "wh_nm": "부산센터", "base_yn": "N" }
  ]
}
```

---

## Phase 4 — Spring Security 필터 체인 구성 🔲

> JWT 발급은 완료. 다음 단계로 API 요청마다 토큰을 검증하는 필터가 필요합니다.

### 작업 목록
- [ ] `JwtAuthenticationFilter.kt` 생성
  - `Authorization: Bearer <token>` 헤더에서 토큰 추출
  - `JwtProvider.validateToken()` 으로 유효성 검사
  - 만료/위변조 시 401 응답 반환
  - 유효 시 `SecurityContextHolder`에 인증 정보 등록
- [ ] `SecurityConfig.kt` 생성 (Spring Security 설정)
  - `/api/user/login` — 인증 없이 허용 (permitAll)
  - 나머지 `/api/**` — JWT 인증 필요 (authenticated)
  - CORS 설정 (`WebConfig.kt`와 일원화 검토)
  - CSRF 비활성화 (REST API 특성상)
- [ ] `build.gradle` 의존성 확인
  - `spring-boot-starter-security` 포함 여부 확인

### 참고
- 현재 `WebConfig.kt`에 CORS 설정이 있으므로 Security 필터와 중복 설정 주의
- 필터 순서: `JwtAuthenticationFilter` → `UsernamePasswordAuthenticationFilter` 앞에 위치
