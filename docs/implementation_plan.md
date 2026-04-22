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

### 프론트엔드 연동 시 주의사항

> Phase 2 주의사항과 동일한 이슈 — 이 API 소비 시 반드시 숙지

- `resultType="map"` 사용으로 `mapUnderscoreToCamelCase: true` 설정이 **적용되지 않음**
- PostgreSQL은 컬럼명을 소문자로 반환 → 응답 키가 `srvc_cd`, `srvc_nm`, `wh_cd`, `wh_nm`, `base_yn` (snake_case)
- 프론트엔드 TypeScript 인터페이스도 반드시 **snake_case** 로 정의해야 함
- camelCase(`srvcCd`)로 정의 시 모든 값이 `undefined`가 되어 드롭다운 미출력 (프론트엔드 연동 중 실제 발생한 이슈)

---

## Phase 6 — DB 스키마 확장 (WMS_SCHEMA.sql) ✅

**파일**: `src/main/resources/WMS_SCHEMA.sql`

### 추가된 테이블 목록

| 테이블 | 설명 | 주요 컬럼 |
|--------|------|-----------|
| `TB_BOARD` | 게시판 (공지사항) | BOARD_ID, CONTENT(TEXT), VW_CNT, BOARD_TYPE, USER_ID, USE_YN |
| `TB_COMM_BOARD_FILE` | 게시판 첨부파일 | FILE_ID, BOARD_ID, REF_TYPE, FILE_NM, FILE_SIZE, FILE_PATH |
| `TB_VEHICLE` | 차량 마스터 | VEHICLE_NO, WH_CD, SRVC_CD, DRV_NM, HP_NO, TON_CLS_CD |
| `TB_CLIENT` | 거래처 마스터 | CLIENT_CD, WH_CD, SRVC_CD, CLIENT_NM_KOR, BUSINESS_NO |
| `TB_ITEM` | 품번 마스터 | PROD_CD, WH_CD, SRVC_CD, PROD_NM, PROD_CATEGORY, FIFO_YN |
| `TB_ZONE` | 존 마스터 | ZONE_CD, WH_CD, SRVC_CD, ZONE_NM |
| `TB_LOC` | 로케이션 마스터 | LOC_ID, ZONE_CD, WH_CD, SRVC_CD, LOC_CLS_CD |
| `TB_STOCK_H` | 재고 헤더 | LOC_ID, ZONE_CD, WH_CD, SRVC_CD, PART_NO, STOCK_QTY |
| `TB_STOCK_D` | 재고 상세 | + LOT_NO, PLT_YN, PLT_QTY, CARTON_QTY |
| `TB_ITRN` | 트랜잭션 | ITRN_KEY, TRAN_TYPE(DP/WD/MV/AJ/TR), FROM/TO 정보 |
| `TB_RECEIPT_H` | 입고 헤더 | IN_NO, IN_EXPECTED_DATE, STATUS(00/01/09) |
| `TB_RECEIPT_D` | 입고 상세 | IN_EXPECTED_SEQ, OPEN_QTY, RECEIVED_QTY, LOT_NO |
| `TB_ORDER_H` | 출고 헤더 | OUT_NO, STATUS(00~09), ALLOC_QTY, PICK_QTY |
| `TB_ORDER_D` | 출고 상세 | OUT_EXPECTED_SEQ, INTEND_QTY, ALLOC_QTY |
| `TB_ORDER_ALLOC` | 출고 할당/피킹 | PICK_NO, OUT_ZONE_CD, OUT_LOC_CD, PDA_YN |

### 공지사항 관련 테이블 상세

**`TB_BOARD`** — 공지사항 화면(Phase 11~12) 연동 대상
```sql
BOARD_ID    INTEGER         PK (시퀀스 채번)
CONTENT     TEXT            본문 (Tiptap HTML 저장)
VW_CNT      INTEGER         조회수
BOARD_TYPE  VARCHAR(2)      게시분류 (00:일반, 99:긴급)
USER_ID     VARCHAR(50)     FK → TB_USER
USE_YN      VARCHAR(1)      게시여부 (Y/N)
```

**`TB_COMM_BOARD_FILE`** — 공지사항 첨부파일 연동 대상
```sql
FILE_ID     INTEGER         PK
BOARD_ID    INTEGER         FK → TB_BOARD
REF_TYPE    VARCHAR(10)     첨부타입
FILE_NM     VARCHAR(500)    파일명
FILE_SIZE   VARCHAR(50)     파일크기
FILE_PATH   VARCHAR(1000)   파일 저장 경로
```

---

## Phase 7 — 공지사항 API 구현 (WMS_HOME_0010) ✅

**작업일자**: 2026-04-22

### DB 사전 작업 (직접 실행 필요)

```sql
-- 1. TB_BOARD TITLE 컬럼 추가
ALTER TABLE WMS.TB_BOARD ADD COLUMN TITLE VARCHAR(500) NULL;
COMMENT ON COLUMN WMS.TB_BOARD.TITLE IS '제목';

-- 2. BOARD_ID 시퀀스 생성
CREATE SEQUENCE WMS.SEQ_TB_BOARD START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER TABLE WMS.TB_BOARD ALTER COLUMN BOARD_ID SET DEFAULT nextval('WMS.SEQ_TB_BOARD');
SELECT setval('WMS.SEQ_TB_BOARD', COALESCE((SELECT MAX(BOARD_ID) FROM WMS.TB_BOARD), 0));

-- TB_COMM_BOARD_FILE FILE_ID 시퀀스도 동일하게 필요
```

### 변경 파일

| 파일 | 작업 |
|------|------|
| `src/main/resources/application.yml` | mapper-locations `classpath:mapper/*.xml` → `classpath:mapper/**/*.xml` |
| `src/main/kotlin/.../home/controller/HomeController.kt` | 공지사항 Controller 신규 생성 |
| `src/main/kotlin/.../home/service/WmsHome0010Service.kt` | 공지사항 Service 신규 생성 |
| `src/main/kotlin/.../home/mapper/WmsHome0010Mapper.kt` | 공지사항 Mapper 인터페이스 신규 생성 |
| `src/main/resources/mapper/home/wmsHome0010Mapper.xml` | 공지사항 쿼리 XML 신규 생성 |

### API 엔드포인트

| 메서드 | URL | 설명 |
|--------|-----|------|
| POST | `/api/home/getList` | 공지사항 목록 조회 (CONTENT 포함) |
| POST | `/api/home/saveList` | 공지사항 저장/수정 (MERGE 처리) |
| POST | `/api/home/deleteList` | 공지사항 삭제 (논리 삭제, 다건) |

### 구현 주요 내용

**Mapper 메서드**
```kotlin
fun selectList(paramMap: Map<String, Any>): List<Map<String, Any>>
fun saveNotice(paramMap: Map<String, Any>): Int
fun deleteNotice(paramMap: Map<String, Any>): Int
```

**saveNotice — PostgreSQL MERGE 문 사용 (15+ 표준 문법)**
- `boardId` null 전송 시 → NOT MATCHED → INSERT (신규)
- `boardId` 값 전송 시 → MATCHED → UPDATE (수정)
- BOARD_ID 채번: `nextval('WMS.SEQ_TB_BOARD')`
- INSERT 시 `BOARD_TYPE` 고정값 `'NOTICE'` 적용, `USE_YN = 'Y'`

**deleteNotice — 논리 삭제 + 다건 처리**
- `USE_YN = 'N'` 으로 논리 삭제
- `foreach`로 `boardIds` 리스트 IN 절 처리

**삭제 예외 처리**
- Service에서 `boardIds` 누락/빈 리스트 시 `IllegalArgumentException` 발생
- Controller에서 별도 catch → HTTP 400 + `resultCode: "0002"` + 실제 메시지 반환
- 프론트 팝업에서 `resultCode !== "0000"` 시 `resultMessage` 출력

### 설계 결정 사항
- 상세 조회 API 미구현 — 목록 조회 시 CONTENT 포함하여 프론트에서 보유한 데이터 활용
- 조회수(VW_CNT) 기능 미사용 — 컬럼은 유지
- Insert/Update를 MERGE 단일 쿼리로 통합 (오라클 친화적 문법 채택)

---

## Phase 8 — 공지사항 프론트엔드 API 연동 (WMS_HOME_0010) ✅

**작업일자**: 2026-04-22

### 변경 파일

| 파일 | 작업 |
|------|------|
| `src/api/common/index.ts` | `API_HOME_ROOT` 상수 추가 |
| `src/api/home/home_0010Service.ts` | 공지사항 API 서비스 신규 작성 |
| `src/pages/Home/cj_wms_home_0010.tsx` | Mock 데이터 제거 → 실 API 연동 |

### API 서비스 (`home_0010Service.ts`)

| 함수 | URL | 설명 |
|------|-----|------|
| `getList` | `POST /api/home/getList` | 공지사항 목록 조회 |
| `saveNotice` | `POST /api/home/saveList` | 공지사항 저장/수정 |
| `deleteNotice` | `POST /api/home/deleteList` | 공지사항 삭제 |

**인터페이스 (`Notice`)**
```ts
interface Notice {
    board_id: number; title: string; content: string;
    vw_cnt: number; board_type: string; user_id: string;
    reg_id: string; reg_date: string; upd_id: string; upd_date: string;
}
```
> 응답 키는 snake_case (PostgreSQL 소문자 반환 + resultType="map" 특성)

### 화면 연동 주요 내용 (`cj_wms_home_0010.tsx`)

- **userId**: `getTokenPayload()?.userId` 로 JWT payload에서 추출
- **목록 조회**: 마운트 시 `useEffect`로 `getList` 호출, 첫 번째 항목 자동 선택
- **에디터 content**: `selectedNoticeId` 변경 시 별도 `useEffect`로 자동 업데이트
- **저장**: `titleInputRef`로 제목 읽기, `editor.getHTML()`로 본문 추출 → `saveNotice` 호출 (신규/수정 MERGE)
- **삭제**: `deleteNotice` 호출, 백엔드 오류 메시지 팝업 출력
- **조회 버튼**: `fetchList` 직접 호출로 목록 갱신
- **NEW 배지**: `reg_date` 기준 7일 이내 자동 계산
- **타입 변경**: `selectedNoticeId` / `checkedIds` → `number` 타입

### 주의사항
- `Response` 인터페이스의 데이터 필드명이 `resultData` (백엔드 `data`와 다름) — 프론트/백 필드명 일치 여부 실 테스트 시 확인 필요

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
