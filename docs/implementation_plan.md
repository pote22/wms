# WMS 백엔드 구현 계획 (Backend Implementation Plan)

## 진행 현황 요약

| 단계 | 내용 | 상태 |
|------|------|------|
| Phase 1 | UserMapper.xml 쿼리 개선 | ✅ 완료 |
| Phase 2 | JWT Claims 확장 (JwtProvider) | ✅ 완료 |
| Phase 3 | UserService generateToken 인자 수정 | ✅ 완료 |
| Phase 4 | Spring Security 필터 체인 구성 (JWT 검증) | 🔲 미완료 |
| Phase 5 | 사용자 고객사&센터 목록 조회 API | ✅ 완료 |
| Phase 6 | DB 스키마 확장 (WMS_SCHEMA.sql — 전체 테이블 정의) | ✅ 완료 |
| Phase 7 | 공지사항 API 구현 (WMS_HOME_0010) | ✅ 완료 |
| Phase 8 | 공지사항 프론트엔드 API 연동 | ✅ 완료 |
| Phase 9 | 공지사항 첨부파일 백엔드 구현 | ✅ 완료 |
| Phase 10 | 공지사항 첨부파일 프론트엔드 연동 | ✅ 완료 |
| Phase 11 | 차량관리 백엔드 구현 (WMS_MASTER_0010) | ✅ 완료 |

> 완료된 Phase 상세 → [`docs/history/backend_phases.md`](history/backend_phases.md)

---

## Phase 4 — Spring Security 필터 체인 구성 🔲

### 작업 목록
- [ ] `JwtAuthenticationFilter.kt` 생성
  - `Authorization: Bearer <token>` 헤더에서 토큰 추출
  - `JwtProvider.validateToken()` 으로 유효성 검사
  - 만료/위변조 시 401 응답 반환
  - 유효 시 `SecurityContextHolder`에 인증 정보 등록
- [ ] `SecurityConfig.kt` 생성 (Spring Security 설정)
  - `/api/user/login` — 인증 없이 허용 (permitAll)
  - 나머지 `/api/**` — JWT 인증 필요 (authenticated)
  - CSRF 비활성화 (REST API 특성상)
  - CORS 설정 (`WebConfig.kt`와 일원화 검토)
- [ ] `build.gradle` 의존성 확인 (`spring-boot-starter-security` 포함 여부)

### 참고
- 현재 `WebConfig.kt`에 CORS 설정이 있으므로 Security 필터와 중복 설정 주의
- 필터 순서: `JwtAuthenticationFilter` → `UsernamePasswordAuthenticationFilter` 앞에 위치

---

## 미완료 항목 (Phase 11 — 차량관리)

### 엑셀 업로드 후 저장 미반영

- 증상: API 200 응답 + "저장되었습니다." 정상 출력, DB 신규 행 없음
- 가설 1: MERGE WHEN MATCHED UPDATE 경로 실행 (동일 SRVC_CD+WH_CD+VEHICLE_NO 기존 행 존재)
- 가설 2: 엑셀의 srvcCd/whCd와 검색 드롭다운 값 불일치로 저장 후 조회 시 미노출
- 재개 시 확인사항:
  1. `SELECT * FROM WMS.TB_VEHICLE ORDER BY UPD_DATE DESC LIMIT 10` — UPDATE 여부 확인
  2. 브라우저 Network 탭 saveVehicle 요청 페이로드 확인 (srvcCd/whCd 값)
