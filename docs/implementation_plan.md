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
| Phase 12 | 품목관리 백엔드 구현 (WMS_MASTER_0030) | ✅ 완료 |
| Phase 13 | 존&로케이션 관리 기능 구현 (WMS_MASTER_0040) | ✅ 완료 |
| Phase 14 | 거래처관리 기능 구현 (WMS_MASTER_0020) | ✅ 완료 |
| Phase 15 | 입고등록 백엔드 구현 (WMS_RECEIPT_0010) | ✅ 완료 |
| Phase 16 | 입고예정/확정 백엔드 구현 (WMS_RECEIPT_0020) | ✅ 완료 |
| Phase 17 | 입고확정 트리거 구현 (WMS_TRIGGER.sql) | ✅ 완료 |
| Phase 18 | 재고현황 백엔드 구현 (WMS_STOCK_0010) | 🔲 미완료 |
| Phase 19 | 트랜잭션관리 백엔드 구현 (WMS_STOCK_0090) | 🔲 미완료 |

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

## Phase 15 — 입고등록 백엔드 구현 (WMS_RECEIPT_0010) ✅

### 신규 파일

#### `WmsReceipt0010Controller.kt`
- `@RequestMapping("/api/receipt/0010")`
- `POST /getList` — 헤더+디테일 조회
- `POST /getKeyInfo` — 입고번호 채번 (SEQ)
- `POST /saveReceiptList` — 헤더+디테일 저장
- `POST /getCheckList` — 엑셀업로드 유효성 검증

#### `WmsReceipt0010Service.kt`
| 메서드 | 설명 |
|--------|------|
| `getList` | selectRcptHdrList → 결과 있을 때만 selectRcptDtlList 조회 |
| `getKeyInfo` | SEQ nextval → 오늘날짜 + 순번 조합으로 입고번호 생성 |
| `saveReceiptList` | ① 입고번호 중복 확인 → ② 품목/존/로케이션 재검증 → ③ 헤더 저장 → ④ 디테일 저장 |
| `getCheckList` | `setCheckRows` 호출 — 행 단위 유효성 검증 후 결과 반환 |
| `setCheckRows` (private) | 5개 항목 검증 (아래 참조) |

#### `WmsReceipt0010Mapper.kt`
```kotlin
fun selectRcptHdrList(map: Map<String, Any>)    : List<Map<String, Any>>
fun selectRcptDtlList(map: Map<String, Any>)    : List<Map<String, Any>>
fun selectRcptKeyInfo(map: Map<String, Any>)    : List<Map<String, Any>>
fun selectRcptStatusInfo(map: Map<String, Any>) : String?
fun insertRcptHdrInfo(map: Map<String, Any>)    : Int
fun insertRcptDtlInfo(map: Map<String, Any>)    : Int
```

#### `wmsReceipt0010Mapper.xml`
| 쿼리 ID | 설명 |
|---------|------|
| `selectRcptHdrList` | TB_RECEIPT_H 조회 (srvcCd · whCd · inNo 조건) |
| `selectRcptDtlList` | TB_RECEIPT_D 조회 + FN_GET_PROD_NM · FN_GET_ZONE_NM 함수 적용 |
| `selectRcptKeyInfo` | `nextval('WMS.SEQ_TB_RECEIPT_IN_EXPECTED_NO_SEQ')` + TODAY |
| `selectRcptStatusInfo` | 입고번호 중복 여부 확인 |
| `insertRcptHdrInfo` | TB_RECEIPT_H INSERT |
| `insertRcptDtlInfo` | TB_RECEIPT_D INSERT (IN_EXPECTED_SEQ: MAX+1 서브쿼리 자동 채번) |

### 공통 파일 수정

#### `CommonCodeMapper.kt` — 3개 메서드 추가
```kotlin
fun selectProdCheck(map: Map<String, Any>) : String?   // TB_ITEM 존재 여부
fun selectZoneCheck(map: Map<String, Any>) : String?   // TB_ZONE 존재 여부
fun selectLocCheck(map: Map<String, Any>)  : String?   // TB_LOC  존재 여부
```

#### `commonCodeMapper.xml` — 3개 SQL 추가
- `selectProdCheck` — `TB_ITEM WHERE PROD_CD = #{prodCd} AND USE_YN = 'Y'`
- `selectZoneCheck` — `TB_ZONE WHERE ZONE_CD = #{zoneCd} AND USE_YN = 'Y'`
- `selectLocCheck`  — `TB_LOC  WHERE ZONE_CD = #{zoneCd} AND LOC_CD = #{locCd} AND USE_YN = 'Y'`

### 엑셀업로드 유효성 검증 규칙 (getCheckList — setCheckRows)
| 항목 | 규칙 | 에러 메시지 |
|------|------|-------------|
| 품목코드 | 필수 + TB_ITEM 존재 여부 | `품목코드` |
| 존코드 | 필수 + TB_ZONE 존재 여부 | `존코드` |
| 로케이션 | 필수 + TB_LOC 존재 여부 | `로케이션` |
| 수량 | `^[0-9]+$` 정수 형식 + 0 초과 | `수량` |
| 입고일자 | 필수 + `^\d{8}$` (yyyyMMdd) | `입고일자` |

### 저장 재검증 (saveReceiptList)
- 헤더 저장 **전** 모든 디테일 행에 대해 품목코드·존코드·로케이션 DB 재검증
- 실패 시 `IllegalArgumentException("N번째 행: ...")` → Controller 400 응답
- 검증 통과 후 헤더 → 디테일 순 저장

---

## Phase 14 — 거래처관리 기능 구현 (WMS_MASTER_0020) ✅

### 구현 내용
- `WmsMaster0020Controller.kt` — `@RequestMapping("/api/master/0020")`, POST `/getList` · `/saveClientInfo` · `/removeClientInfo` · `/getCheckList`
- `WmsMaster0020Service.kt` — getList / saveClientInfo / removeClientInfo / getCheckList(엑셀 업로드 유효성 검증)
- `WmsMaster0020Mapper.kt` — selectClientList / mergeClientInfo / deleteClientInfo
- `wmsMaster0020Mapper.xml` — selectClientList(동적 조건) / mergeClientInfo(UPSERT) / deleteClientInfo(다건 IN)

### 엑셀 업로드 유효성 검증 규칙 (getCheckList)
- 거래처코드 필수
- 사업자번호: 입력 시 10자리 이상
- 연락처: `^0\d{1,2}-\d{3,4}-\d{4}$` 정규식
- 이메일: `^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$` 정규식

---

## Phase 12 — 품목관리 백엔드 구현 (WMS_MASTER_0030) ✅

### 구현 내용
- `WmsMaster0030Controller.kt` — `@RequestMapping("/api/master/part")`, POST `/getList` · `/savePart` · `/removePart` · `/getCheckList`
- `WmsMaster0030Service.kt` — getList / saveProdInfo / deleteProdInfo / getCheckList(엑셀 업로드 유효성 검증)
- `WmsMaster0030Mapper.kt` — selectPartList / mergePartInfo / deletePartInfo
- `wmsMaster0030Mapper.xml` — selectPartList(동적 조건) / mergePartInfo(UPSERT) / deletePartInfo(다건 IN)
- `CommonCodeController.kt` — POST `/api/common/getProdSearchList` 추가
- `CommonCodeService.kt` — getProdSearchList 추가
- `CommonCodeMapper.kt` — selectProdSearchList 추가
- `commonCodeMapper.xml` — selectProdSearchList(LIKE 검색) 추가

### 엑셀 업로드 유효성 검증 규칙 (getCheckList)
- 품목번호 필수
- 단가 · 용기수량: `^[0-9]+$` (정수)
- 중량 · 실중량: `^[0-9]*\.?[0-9]*$` (실수)

---

---

## Phase 13 — 존&로케이션 관리 기능 구현 (WMS_MASTER_0040) ✅

### 구현 방향
- 조회: 존 리스트 조회 → 행 선택 시 해당 존의 로케이션 목록 조회 (마스터-디테일)
- 저장: 존 마스터 + 로케이션 마스터 동시 저장 (신규/수정 행)
- 엑셀: 존 리스트만 ExcelJS 다운로드

---

### 백엔드 (신규 4개 파일) ✅ 완료

#### 1. `WmsMaster0040Mapper.kt`
```kotlin
@Mapper
interface WmsMaster0040Mapper {
    fun selectZoneList(map: Map<String, Any>): List<Map<String, Any>>
    fun mergeZoneInfo(map: Map<String, Any>): Int
    fun selectLocList(map: Map<String, Any>): List<Map<String, Any>>
    fun mergeLocInfo(map: Map<String, Any>): Int
}
```

#### 2. `wmsMaster0040Mapper.xml`

**selectZoneList** — 조회조건: srvcCd(필수), whCd(필수), zoneCd(선택 LIKE), useYn(선택)
```sql
SELECT '0' AS CHK, SRVC_CD, WH_CD
     , COALESCE(NULLIF(WMS.FN_GET_WH_NM(SRVC_CD, WH_CD),''),'') AS WH_NM
     , ZONE_CD, ZONE_NM, USE_YN
     , COALESCE(NULLIF(WMS.FN_GET_USER_NM(REG_ID),''),'') AS REG_ID
     , TO_CHAR(REG_DATE,'YYYY-MM-DD') AS REG_DATE
     , COALESCE(NULLIF(WMS.FN_GET_USER_NM(UPD_ID),''),'') AS UPD_ID
     , TO_CHAR(UPD_DATE,'YYYY-MM-DD') AS UPD_DATE
  FROM WMS.TB_ZONE
 WHERE SRVC_CD = #{srvcCd} AND WH_CD = #{whCd}
   <if test="zoneCd != null and zoneCd != ''">AND ZONE_CD LIKE '%'||#{zoneCd}||'%'</if>
   <if test="useYn != null and useYn != ''">AND USE_YN = #{useYn}</if>
 ORDER BY ZONE_CD
```

**mergeZoneInfo** — MERGE INTO WMS.TB_ZONE ON (SRVC_CD, WH_CD, ZONE_CD)
- MATCHED: UPDATE ZONE_NM, USE_YN, UPD_ID, UPD_DATE
- NOT MATCHED: INSERT 전체 컬럼

**selectLocList** — 조회조건: srvcCd, whCd, zoneCd(필수 — 선택된 존 행)
```sql
SELECT '0' AS CHK, SRVC_CD, WH_CD, ZONE_CD
     , LOC_CD, LOC_TYPE, REMARK, USE_YN
     , REPL_YN, REPL_PROD_CD, LOC_GROUP
     , REG_ID, REG_DATE, UPD_ID, UPD_DATE
  FROM WMS.TB_LOC
 WHERE SRVC_CD = #{srvcCd} AND WH_CD = #{whCd} AND ZONE_CD = #{zoneCd}
 ORDER BY LOC_CD
```

**mergeLocInfo** — MERGE INTO WMS.TB_LOC ON (SRVC_CD, WH_CD, ZONE_CD, LOC_CD)
- MATCHED: UPDATE LOC_TYPE, REMARK, USE_YN, REPL_YN, REPL_PROD_CD, LOC_GROUP, UPD_ID, UPD_DATE
- NOT MATCHED: INSERT 전체 컬럼

#### 3. `WmsMaster0040Service.kt`
| 메서드 | 설명 |
|--------|------|
| `getZoneList(paramMap)` | selectZoneList 호출 → Response 반환 |
| `getLocList(paramMap)` | selectLocList 호출 → Response 반환 |
| `saveInfo(paramMap)` | zones 리스트 forEach mergeZoneInfo → locations 리스트 forEach mergeLocInfo |

#### 4. `WmsMaster0040Controller.kt`
- `@RequestMapping("/api/master/zone")`
- `POST /getZoneList` — 존 목록 조회
- `POST /getLocList` — 로케이션 목록 조회 (선택된 존 기준)
- `POST /saveInfo` — 존+로케이션 동시 저장
- 예외처리: IllegalArgumentException → 400, Exception → 500

---

### 프론트엔드 ✅ 완료 (wms_view Phase 18 참조)

- `master_0040Service.ts` — getZoneList / getLocList / saveInfo / getCheckList API 구현
- `cj_wms_master_0040.tsx` — 조회·저장·인라인 편집·엑셀다운·양식다운·엑셀업로드 전체 구현
- `ZoneSearchPopup.tsx` + `ZoneSearchPopup.module.css` — 신규 공통 팝업 컴포넌트

#### 백엔드 공통 수정 사항
- `commonCodeMapper.xml` — `selectCommonCodeList` 파라미터 `#{sysGrpCd}` → `#{sys_grp_cd}` (프론트 snake_case 요청 대응)
- `WmsMaster0010Service.kt` — `selectCommonCodeCheck` 호출 파라미터 camelCase 유지 (`mapOf("sysGrpCd" to ...)`)

---

## Phase 17 — 입고확정 트리거 구현 (WMS_TRIGGER.sql) ✅

### 신규 파일
- `src/main/resources/WMS_TRIGGER.sql`

### 구현 내용

#### 시퀀스
- `wms.seq_itrn_key` — TB_ITRN ITRN_KEY 채번용 (YYYYMMDD + 9자리)

#### 트리거 함수 (`wms.fn_receipt_confirm_trigger`)
- 발동 조건: `OLD.EXPECTED_QTY IS DISTINCT FROM NEW.EXPECTED_QTY AND NEW.STATUS = '09'`
- Step 1 — TB_STOCK_H UPSERT: 로케이션 재고 수량 증가 (PK: LOC_ID, ZONE_CD, WH_CD, SRVC_CD)
- Step 2 — TB_STOCK_D UPSERT: LOT별 재고 상세 수량 증가 (PK: ZONE_CD, LOC_ID, SRVC_CD, WH_CD)
- Step 3 — TB_ITRN INSERT: 입고 트랜잭션 이력 적재 (TRAN_TYPE='DP', SOURCE_KEY=IN_NO)
- Step 4 — TB_RECEIPT_H STATUS 갱신: 전체 완료 시 '09', 부분 완료 시 '01' + RECEIVED_QTY 합산

#### 트리거 등록
- `trg_receipt_confirm` AFTER UPDATE ON wms.TB_RECEIPT_D FOR EACH ROW

---

## Phase 16 — 입고예정/확정 백엔드 구현 (WMS_RECEIPT_0020) ✅

### 신규 파일

#### `WmsReceipt0020Controller.kt`
- `@RequestMapping("/api/receipt/0020")`
- `POST /getList` — 입고예정 목록 조회
- `POST /saveRemarkInfo` — 비고 저장
- `POST /saveReceiptConfirm` — 입고확정 처리

#### `WmsReceipt0020Service.kt`
| 메서드 | 설명 |
|--------|------|
| `getList` | TB_RECEIPT_H+D JOIN 조회 (다중 동적 조건) |
| `saveRemarkInfo` | TB_RECEIPT_D RMK 다건 UPDATE |
| `saveReceiptConfirm` | TB_RECEIPT_D STATUS→'09' + EXPECTED_QTY/RECEIVED_QTY UPDATE (트리거 발동) |

#### `WmsReceipt0020Mapper.kt` / `wmsReceipt0020Mapper.xml`
| 쿼리 ID | 설명 |
|---------|------|
| `selectReceiptList` | TB_RECEIPT_H+D JOIN, FN_GET_PROD_NM/FN_GET_ZONE_NM/FN_GET_USER_NM 함수 사용 |
| `updateReceiptRmkInfo` | RMK, UPD_ID, UPD_DATE UPDATE |
| `updateReceiptConfirmInfo` | STATUS='09', EXPECTED_QTY, RECEIVED_QTY, NOT_RSN_CD, RECEIPT_DATE UPDATE (STATUS='00' 조건) |

### 버그 수정
- `WmsReceipt0010Service.kt` `saveReceiptList` — `@Transactional` 누락 추가
- `TB_RECEIPT_H.REG_DATE` 타입 DATE → TIMESTAMP 변경 (시간 누락 문제)
- `application.yml` HikariCP `connection-init-sql` — `SET TIME ZONE 'Asia/Seoul'` 추가 (UTC→KST)

---

## Phase 18 — 재고현황 백엔드 구현 (WMS_STOCK_0010) 🔲

### 작업 예정
- `WmsStock0010Controller.kt` — `@RequestMapping("/api/stock/0010")`
- `WmsStock0010Service.kt`
- `WmsStock0010Mapper.kt` / `wmsStock0010Mapper.xml`
- 조회: TB_STOCK_H + TB_STOCK_D JOIN, 고객사·센터·품목·존·로케이션 조건

---

## Phase 19 — 트랜잭션관리 백엔드 구현 (WMS_STOCK_0090) 🔲

### 작업 예정
- `WmsStock0090Controller.kt` — `@RequestMapping("/api/stock/0090")`
- `WmsStock0090Service.kt`
- `WmsStock0090Mapper.kt` / `wmsStock0090Mapper.xml`
- 조회: TB_ITRN, 고객사·센터·TRAN_TYPE·기간·품목 조건

---

## 미완료 항목 (Phase 11 — 차량관리)

### 엑셀 업로드 후 저장 미반영

- 증상: API 200 응답 + "저장되었습니다." 정상 출력, DB 신규 행 없음
- 가설 1: MERGE WHEN MATCHED UPDATE 경로 실행 (동일 SRVC_CD+WH_CD+VEHICLE_NO 기존 행 존재)
- 가설 2: 엑셀의 srvcCd/whCd와 검색 드롭다운 값 불일치로 저장 후 조회 시 미노출
- 재개 시 확인사항:
  1. `SELECT * FROM WMS.TB_VEHICLE ORDER BY UPD_DATE DESC LIMIT 10` — UPDATE 여부 확인
  2. 브라우저 Network 탭 saveVehicle 요청 페이로드 확인 (srvcCd/whCd 값)
