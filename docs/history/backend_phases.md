# WMS 백엔드 완료 Phase 상세 (아카이브)

> 메인 파일 → [`../implementation_plan.md`](../implementation_plan.md)

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
| `src/main/kotlin/.../user/mapper/UserMapper.kt` | `selectUserAuthWhList` 메서드 추가 |
| `src/main/resources/mapper/UserMapper.xml` | `selectUserAuthWhList` 쿼리 추가 |
| `src/main/kotlin/.../user/service/UserService.kt` | `getUserAuthWhList` 메서드 추가 |
| `src/main/kotlin/.../user/controller/UserController.kt` | `POST /api/user/getUserAuthWhList` 엔드포인트 추가 |
| `src/main/resources/WMS_FUNCTION.sql` | `fn_get_srvc_nm`, `fn_get_wh_nm` PostgreSQL 함수 정의 추가 |

### API 응답 구조
```json
{
  "resultCode": "0000",
  "resultMessage": "조회완료",
  "data": [
    { "srvc_cd": "GS01", "srvc_nm": "GS칼텍스", "wh_cd": "ICN01", "wh_nm": "인천GSC센터", "base_yn": "Y" }
  ]
}
```

### 프론트엔드 연동 주의사항
- `resultType="map"` 사용으로 `mapUnderscoreToCamelCase: true` 설정이 **적용되지 않음**
- PostgreSQL은 컬럼명을 소문자로 반환 → 응답 키가 snake_case
- 프론트엔드 TypeScript 인터페이스도 반드시 **snake_case** 로 정의해야 함

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

---

## Phase 7 — 공지사항 API 구현 (WMS_HOME_0010) ✅

**작업일자**: 2026-04-22

### DB 사전 작업

```sql
ALTER TABLE WMS.TB_BOARD ADD COLUMN TITLE VARCHAR(500) NULL;
CREATE SEQUENCE WMS.SEQ_TB_BOARD START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER TABLE WMS.TB_BOARD ALTER COLUMN BOARD_ID SET DEFAULT nextval('WMS.SEQ_TB_BOARD');
SELECT setval('WMS.SEQ_TB_BOARD', COALESCE((SELECT MAX(BOARD_ID) FROM WMS.TB_BOARD), 0));
```

### API 엔드포인트

| 메서드 | URL | 설명 |
|--------|-----|------|
| POST | `/api/home/getList` | 공지사항 목록 조회 (CONTENT 포함) |
| POST | `/api/home/saveList` | 공지사항 저장/수정 (MERGE 처리) |
| POST | `/api/home/deleteList` | 공지사항 삭제 (논리 삭제, 다건) |

### 구현 주요 내용
- `boardId` null 전송 시 → NOT MATCHED → INSERT (신규), `nextval('WMS.SEQ_TB_BOARD')` 채번
- `boardId` 값 전송 시 → MATCHED → UPDATE (수정)
- 논리 삭제: `USE_YN = 'N'`, `foreach`로 boardIds 리스트 IN 절 처리
- `boardIds` 누락/빈 리스트 시 `IllegalArgumentException` → HTTP 400

---

## Phase 8 — 공지사항 프론트엔드 API 연동 (WMS_HOME_0010) ✅

**작업일자**: 2026-04-22

### API 서비스 인터페이스
```ts
interface Notice {
    board_id: number; title: string; content: string;
    vw_cnt: number; board_type: string; user_id: string;
    reg_id: string; reg_date: string; upd_id: string; upd_date: string;
}
```
> 응답 키는 snake_case (PostgreSQL 소문자 반환 + resultType="map" 특성)

---

## Phase 9 — 공지사항 첨부파일 백엔드 구현 (WMS_HOME_0010) ✅

**작업일자**: 2026-04-23

### DB 사전 작업

```sql
CREATE SEQUENCE WMS.SEQ_TB_COMM_BOARD_FILE START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER TABLE WMS.TB_COMM_BOARD_FILE ALTER COLUMN FILE_ID SET DEFAULT nextval('WMS.SEQ_TB_COMM_BOARD_FILE');
SELECT setval('WMS.SEQ_TB_COMM_BOARD_FILE', COALESCE((SELECT MAX(FILE_ID) FROM WMS.TB_COMM_BOARD_FILE), 0));
```

### API 엔드포인트

| 메서드 | URL | 설명 |
|--------|-----|------|
| POST | `/api/home/getFileList` | 공지사항 첨부파일 목록 조회 |
| POST | `/api/home/uploadFile` | 첨부파일 업로드 (multipart/form-data) |
| GET  | `/api/home/downloadFile/{fileId}` | 첨부파일 다운로드 |
| POST | `/api/home/deleteFile` | 첨부파일 삭제 (물리 삭제 + DB 삭제) |

### 파일 저장 전략
- `FileStorageService` 인터페이스로 저장 방식 추상화
- 개발: `LocalFileStorageService` → `c:/workspace/wms_view/uploads/board/{boardId}/`
- 저장 파일명: `{UUID}_{원본파일명}` (중복 방지)
- 운영 전환: `application.yml`에서 `file.storage: s3` 변경만으로 구현체 전환

---

## Phase 10 — 공지사항 첨부파일 프론트엔드 연동 (WMS_HOME_0010) ✅

**작업일자**: 2026-04-23

### 연동 흐름
- 공지 선택 시: `loadFileList(boardId)` → `getFileList` API → `attachedFiles` 갱신
- 파일 추가 시: `pendingFile` 속성이 있는 항목으로 상태에 추가, UI에 `(저장 대기)` 표시
- 저장 버튼 클릭: 공지 저장 → `onSaved(boardId)` 콜백 → 대기 파일 `uploadFile` → `loadFileList` 재조회
- 파일명 클릭: `fileId` 있는 파일만 `downloadFile` 호출 → Blob 브라우저 다운로드

---

## Phase 11 — 차량관리 백엔드 구현 (WMS_MASTER_0010) ✅

**작업일자**: 2026-04-29 ~ 2026-04-30

### API 엔드포인트

| 메서드 | URL | 설명 |
|--------|-----|------|
| POST | `/api/master/vehicle/getList` | 차량 목록 조회 |
| POST | `/api/master/vehicle/saveVehicle` | 차량 저장/수정 (MERGE) |
| POST | `/api/master/vehicle/removeVehicle` | 차량 삭제 (다건) |
| POST | `/api/master/vehicle/getCheckList` | 엑셀 업로드 유효성 검사 |
| POST | `/api/common/getTonList` | 톤급 코드 목록 조회 |

### 저장 MERGE 키
- 복합 PK: `SRVC_CD + WH_CD + VEHICLE_NO`
- WHEN MATCHED → UPDATE / WHEN NOT MATCHED → INSERT

### 엑셀 유효성 검사 항목
1. vehicleNo 필수값 여부
2. tonClsCd — TB_COMM_CODE (SYS_MC_CD = 'WM10001') 허용값 여부
3. hpNo — 정규식 포맷 체크 (`^0\d{1,2}-\d{3,4}-\d{4}$`)

### 트러블슈팅

| 문제 | 원인 | 해결 |
|------|------|------|
| MyBatis XML id 불일치 | `id="getTonList"` vs Mapper `getTonCdList()` | XML id를 `getTonCdList`로 수정 |
| 톤급 유효성 항상 실패 | `SYS_MC_CD = 'WM1001'` (오타) | `WM10001`로 수정 |
| 콘솔 한글 깨짐 | Windows JVM 기본 charset MS949 (DB 데이터는 정상) | IntelliJ VM options에 `-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8` 추가 |
