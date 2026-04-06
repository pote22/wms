# 로그인 검증 및 UI 최적화 통합 계획

로그인 시 아이디와 비밀번호 입력 여부를 확인하여 팝업 메시지를 출력하고, 복잡한 MUI 임포트 구문을 축약하여 코드를 더 깔끔하게 정리하는 계획입니다.

## 사용자 검토 필요 사항

> [!IMPORTANT]
> 이 작업은 `Login.tsx` 파일의 구조를 개선하며, 오류 메시지뿐만 아니라 향후 다양한 사용자 피드백을 제공할 수 있는 기반(Snackbar)을 마련합니다.

## 제안된 변경 사항

### 1. 공통 UI 모듈 구축 [Phase 1]

임포트 구문을 축약하고 코드 가독성을 높이기 위해 공통 UI 컴포넌트 모듈을 생성합니다.

#### [신규] [mui.ts](file:///c:/workspace/wms/frontend/src/components/common/mui.ts)
- `Box`, `Typography`, `TextField`, `Button`, `Snackbar`, `Alert`, `Checkbox` 등 자주 사용하는 MUI 컴포넌트를 통합 배포합니다.
- `Security`, `AccountCircle`, `Lock`, `Visibility`, `VisibilityOff`, `ArrowForward` 등 아이콘을 `Icons` 네임스페이스로 통합 배포합니다.

### 2. 로그인 로직 및 UI 개선 [Phase 2]

#### [수정] [Login.tsx](file:///c:/workspace/wms/frontend/src/login/Login.tsx)
- **임포트 최적화**: 수십 줄의 임포트 코드를 단 한 줄(`import { Mui, Icons } from '../components/common/mui'`)로 축약합니다.
- **검증 로직 추가**: `handleLogin` 함수에서 아이디/비밀번호 입력 여부를 확인하는 기능을 추가합니다.
- **팝업 알림 추가**: MUI `Snackbar`와 `Alert` 컴포넌트를 사용하여 하단에 3초간 오류 메시지를 띄우는 기능을 구현합니다.
- **상태 관리**: 팝업 노출 여부(`open`)와 메시지 내용(`message`)을 관리하는 상태 변수를 추가합니다.

## 질문 사항

1. 팝업 메시지는 3초 후에 자동으로 사라지게 설정할 예정입니다. 더 긴 시간이 필요하신가요?
2. 입력 필드 검증 실패 시 테두리를 빨간색(error 상태)으로 강조하는 기능도 함께 구현할까요?

## 검증 계획

### 자동 테스트
- `npm run lint` 실행 결과, 모든 파일의 참조와 경로가 정상임을 확인합니다.
- `npm run dev` 실행 중 화면이 정상적으로 렌더링되는지 확인합니다.

### 수동 검증
- 아이디나 비밀번호를 입력하지 않고 로그인 버튼을 클릭했을 때, "사용자 아이디를 입력해주세요" 또는 "비밀번호를 입력해주세요"라는 팝업 메시지가 하단 중앙에 나타나는지 확인합니다.
