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
| Phase 13 | 존&로케이션 관리 기능 구현 (WMS_MASTER_0040) | 🔲 진행중 |

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

## Phase 13 — 존&로케이션 관리 기능 구현 (WMS_MASTER_0040) 🔲

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

### 프론트엔드 (2개 파일 수정) 🔲 미완료

#### 5. `master_0040Service.ts` (기존 스텁 → 구현) 🔲
```typescript
// Zone, Location, ApiResponse 인터페이스 정의
export const getZoneList = (data, onSuccess, onError) => POST /api/master/zone/getZoneList
export const getLocList  = (data, onSuccess, onError) => POST /api/master/zone/getLocList
export const saveInfo    = (data, onSuccess, onError) => POST /api/master/zone/saveInfo
```

#### 6. `cj_wms_master_0040.tsx` (기능 구현) 🔲

**체크박스 제거**
- 삭제 기능 없음 → 체크박스 불필요
- `ZoneRow`, `LocRow` 인터페이스에서 `chk` 필드 제거
- `handleZoneSelectAll`, `handleZoneRowClick`, `handleLocSelectAll`, `handleLocRowClick` 핸들러 제거
- 테이블 체크박스 컬럼(thead th + tbody td) 제거
- 저장 시 chk 필터 없이 `isNew || isDirty` 행 전체 수집

**추가 state**
- `searchZoneCd`, `searchZoneNm`, `searchLocCd`, `searchUseYn`
- `isSaving`, usePopup

**핸들러**
| 핸들러 | 동작 |
|--------|------|
| `handleSearch` | getZoneList 호출 → zoneItems 세팅, locItems/selectedZoneCd 초기화 |
| `handleZoneSelect` | 행 클릭 시 srvcCd·whCd·zoneCd 추출 → getLocList 호출 → locItems 세팅 |
| `handleSave` | `isNew\|\|isDirty` 존+로케이션 전체 수집 → confirm → saveInfo → handleSearch 재조회 |
| `handleExcel` | ExcelJS로 zoneItems 엑셀 다운로드 (존 리스트만) |

**JSX 변경**
- 조회/저장/엑셀 버튼 onClick 연결
- 존·로케이션·사용여부 조회조건 value/onChange 바인딩
- `<Popup>` 컴포넌트 추가

---

## 미완료 항목 (Phase 11 — 차량관리)

### 엑셀 업로드 후 저장 미반영

- 증상: API 200 응답 + "저장되었습니다." 정상 출력, DB 신규 행 없음
- 가설 1: MERGE WHEN MATCHED UPDATE 경로 실행 (동일 SRVC_CD+WH_CD+VEHICLE_NO 기존 행 존재)
- 가설 2: 엑셀의 srvcCd/whCd와 검색 드롭다운 값 불일치로 저장 후 조회 시 미노출
- 재개 시 확인사항:
  1. `SELECT * FROM WMS.TB_VEHICLE ORDER BY UPD_DATE DESC LIMIT 10` — UPDATE 여부 확인
  2. 브라우저 Network 탭 saveVehicle 요청 페이로드 확인 (srvcCd/whCd 값)
