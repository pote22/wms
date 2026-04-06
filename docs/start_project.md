# WMS 프로젝트 IndexController - Frontend(React/Vite) 연동 계획

`IndexController`가 frontend 폴더의 `main.jsx`(또는 `main.tsx`)를 "호출"한다는 것은, 백엔드 서버(Spring Boot)가 React 애플리케이션의 진입점 역할을 하는 `index.html`을 클라이언트에게 전달하고, 그 HTML이 React 번들을 로드하도록 설정하는 것을 의미합니다.

현재 `IndexController`는 `"index"` 뷰를 반환하도록 되어 있으나, 백엔드 템플릿 폴더(`src/main/resources/templates`)가 비어 있는 상태입니다.

## Proposed Changes

### 1. Spring Boot 백엔드 설정

#### [NEW] [index.html](file:///c:/workspace/wms/src/main/resources/templates/index.html)
`IndexController`가 반환하는 `"index"` 뷰에 해당하는 템플릿 파일을 생성합니다.

- 개발 시: `http://localhost:5173/src/main.jsx`를 참조하도록 설정

#### [MODIFY] [IndexController.kt](file:///c:/workspace/wms/src/main/kotlin/com/cjlogistics/wms/root/controller/IndexController.kt)
이미 `"index"`를 반환하고 있으므로, 템플릿 파일이 생기면 자동으로 연결됩니다.

### 2. Frontend 설정 (Vite)

- 현재 `frontend/package.json`에 `proxy` 설정이 되어 있으므로, React에서 API 호출 시 백엔드로 전달됩니다.

---

## Verification Plan

### Manual Verification
1. `npm run dev` 명령어로 Vite 개발 서버 실행 확인 (현재 실행 중으로 보임)
2. Spring Boot 애플리케이션 실행
3. `http://localhost:8080/` 접속 시 React 애플리케이션 화면(Vite 로고 등)이 정상적으로 노출되는지 확인
