# WMS Project — Codex Agent Guide

## 새 환경 셋업 (AI 연동 재설치 절차)

> 새 PC에서 이 파일을 참고하여 Codex에게 "AGENTS.md 보고 AI 연동 진행해줘"라고 하면 아래 절차를 자동으로 수행합니다.

### 사전 조건
- Node.js 18.18 이상 설치 필요
- GEMINI_API_KEY, OPENAI_API_KEY 준비

### 1단계 — MCP 서버 패키지 설치
```bash
cd mcp-servers/gemini-server && npm install
cd mcp-servers/openai-server && npm install
```

### 2단계 — Codex CLI 전역 설치 및 인증
```bash
npm install -g @openai/codex
echo "OPENAI_API_KEY값" | codex login --with-api-key
```

### 3단계 — token-optimizer-mcp 전역 설치
```bash
npm install -g token-optimizer-mcp
```

### 4단계 — .mcp.json 생성
프로젝트 루트에 `.mcp.json` 파일 생성 (아래 형식 참고):
```json
{
  "mcpServers": {
    "gemini-research": {
      "type": "stdio",
      "command": "node",
      "args": ["mcp-servers/gemini-server/server.js"],
      "env": { "GEMINI_API_KEY": "실제_키_입력" }
    },
    "openai-coding": {
      "type": "stdio",
      "command": "node",
      "args": ["mcp-servers/openai-server/server.js"],
      "env": { "OPENAI_API_KEY": "실제_키_입력" }
    },
    "codex": {
      "type": "stdio",
      "command": "npx",
      "args": ["codex-mcp-server"],
      "env": { "OPENAI_API_KEY": "실제_키_입력" }
    },
    "token-optimizer": {
      "type": "stdio",
      "command": "token-optimizer-mcp"
    }
  }
}
```

### 5단계 — Codex CLI API 키 인증
```bash
echo "OPENAI_API_KEY값" | codex login --with-api-key
# 인증 정보는 ~/.codex/auth.json 에 저장됨
```

### 6단계 — VSCode 재시작
재시작 후 MCP 서버 4개(`gemini-research`, `openai-coding`, `codex`, `token-optimizer`) 자동 로드 확인

> ⚠️ `.mcp.json`에 API 키가 평문 저장됨 — 공개 저장소에 push 금지

### 연결 테스트
```bash
# OpenAI 연결 확인
cd mcp-servers/openai-server
OPENAI_API_KEY="키값" node -e "import OpenAI from 'openai'; const o = new OpenAI({apiKey: process.env.OPENAI_API_KEY}); const r = await o.chat.completions.create({model:'gpt-4o',messages:[{role:'user',content:'hi'}],max_tokens:10}); console.log('OK:', r.choices[0].message.content);"

# Gemini 연결 확인
cd mcp-servers/gemini-server
GEMINI_API_KEY="키값" node -e "import {GoogleGenerativeAI} from '@google/generative-ai'; const m = new GoogleGenerativeAI(process.env.GEMINI_API_KEY).getGenerativeModel({model:'gemini-2.0-flash'}); const r = await m.generateContent('hi'); console.log('OK:', r.response.text().slice(0,30));"

# token-optimizer 설치 확인
where token-optimizer-mcp
```

---

## 멀티 에이전트 역할 분담

이 프로젝트는 네 개의 AI 에이전트가 협력하여 작업을 처리합니다.

### 역할 정의

| 에이전트 | 역할 | 사용 도구 |
|----------|------|-----------|
| **Codex (나)** | 오케스트레이터 — 사용자 명령 해석, 작업 분배, 파일 편집, git 작업 | 기본 도구 전체 |
| **Gemini** (`gemini-research` MCP) | 리서치 담당 — 기술 조사, 개념 설명, 비교 분석 | `gemini_research`, `gemini_compare`, `gemini_analyze` |
| **OpenAI/Codex** (`openai-coding` MCP + `codex` MCP) | 코딩 담당 — 코드 생성, 리팩토링, 버그 수정, 테스트 작성, 코드 리뷰 | `codex_generate`, `codex_refactor`, `codex_fix_bug`, `codex_write_tests`, `codex`, `review` |
| **Token Optimizer** (`token-optimizer` MCP) | 토큰 최적화 — 캐싱·압축으로 컨텍스트 윈도우 절약 | 자동 동작 (60~95% 절감) |

---

## AI 연동 구성 (MCP 서버)

### 연동 방식

| MCP 서버 | 타입 | 실행 방식 | 절감 효과 | 상태 |
|----------|------|-----------|-----------|------|
| `gemini-research` | stdio | `node mcp-servers/gemini-server/server.js` | - | ✅ 활성 |
| `openai-coding` | stdio | `node mcp-servers/openai-server/server.js` | - | ✅ 활성 |
| `codex` | stdio | `npx codex-mcp-server` (Codex CLI 래퍼) | - | ✅ 활성 |
| `token-optimizer` | stdio | `token-optimizer-mcp` (전역 설치) | 60~95% | ✅ 활성 |

### 설정 파일 위치
- **MCP 등록**: `.mcp.json` (각 프로젝트 루트 — VSCode에서 해당 폴더를 열어야 로드됨)
- **Codex CLI 인증**: `~/.codex/auth.json`
- **MCP 서버 소스**: `mcp-servers/gemini-server/`, `mcp-servers/openai-server/`

> ⚠️ `.mcp.json`은 VSCode에서 **해당 폴더를 직접 열었을 때**만 로드됨
> - `c:\workspace` 열면 → `.mcp.json` 미로드
> - `c:\workspace\wms` 열면 → `.mcp.json` 정상 로드

### 적용 프로젝트
| 프로젝트 | 경로 | .mcp.json | 비고 |
|----------|------|-----------|------|
| WMS (백엔드) | `c:\workspace\wms` | ✅ 있음 | Kotlin + Spring Boot |
| WMS View (프론트) | `c:\workspace\wms_view` | ✅ 있음 | React 19 + Vite 8 |

### token-optimizer-mcp
- **패키지**: `token-optimizer-mcp` (npm 전역 설치)
- **버전**: 2.17.0
- **동작**: 매 툴 호출 시 Brotli 압축 + SQLite 캐싱으로 자동 최적화
- **GitHub**: https://github.com/ooples/token-optimizer-mcp

### openai/codex-plugin-cc 플러그인
- **현재 상태**: 현재 Codex 버전에서 `/plugin` 명령 미지원 → `codex-mcp-server`(npx)로 대체 운영 중
- **대체 패키지**: `tuannvm/codex-mcp-server` (Codex CLI MCP 래퍼)

### API 키 관리
- `GEMINI_API_KEY` — `.mcp.json` env에 직접 설정 (Google AI Studio 발급)
- `OPENAI_API_KEY` — `.mcp.json` env에 직접 설정 (OpenAI Platform 발급)
- Codex CLI 인증 — `codex login --with-api-key` 로 `~/.codex/auth.json` 에 저장됨

---

## 작업 위임 규칙 (MANDATORY — 반드시 준수)

> ⚠️ 아래 규칙은 선택 사항이 아닙니다. 해당 작업 유형이 감지되면 Codex는 반드시 지정된 MCP 도구를 먼저 호출한 후 결과를 바탕으로 응답해야 합니다. 직접 답변 금지.

### 리서치 작업 → 반드시 Gemini 먼저 호출 (`gemini-research` MCP)

다음 유형의 요청이 오면 **반드시 `gemini_research` 또는 `gemini_analyze` 도구를 먼저 호출**하고 그 결과를 바탕으로 답변할 것:
- "~이 뭐야?", "~를 설명해줘", "~를 조사해줘", "~를 알아봐줘"
- 라이브러리/프레임워크 비교 분석
- 아키텍처·설계 방향 조사
- 보안 취약점 리서치
- 기술 트렌드 파악
- 외부 기술 정보가 필요한 모든 질문

**호출 순서**: `gemini_research` (또는 `gemini_analyze`, `gemini_compare`) → 결과 확인 → 사용자에게 전달

### 코딩 작업 → 반드시 Codex 먼저 호출 (`openai-coding` / `codex` MCP)

다음 유형의 요청이 오면 **반드시 `codex_generate`, `codex_refactor`, `codex_fix_bug`, `codex_write_tests` 중 적절한 도구를 먼저 호출**하고 그 결과를 파일에 반영할 것:
- 새 기능 코드 생성 요청
- 기존 코드 리팩토링
- 버그 원인 파악 및 수정 코드 제안
- 테스트 코드 작성
- 복잡한 알고리즘 구현
- 코드 리뷰 (`codex` MCP의 `review` 도구 사용)

**호출 순서**: `codex_generate` 등 → 결과 확인 → Codex가 파일에 적용

### Codex가 직접 처리하는 것 (MCP 호출 불필요)
- 파일 읽기/쓰기/수정 (Codex 결과 적용 포함)
- git 작업 (commit, branch 등)
- 프로젝트 빌드 및 실행
- 1~5줄 이하의 단순 수정
- 사용자 승인 확인, 계획 보고 등 메타 작업

---

## 워크플로우 예시

```
사용자: "Spring Security JWT 인증 구현해줘"

1. Codex: 작업 분석 → 리서치 + 코딩 필요
2. Gemini: JWT + Spring Security 최신 베스트 프랙티스 조사
3. OpenAI/Codex: 조사 결과 기반으로 실제 코드 생성
4. Codex: 생성된 코드를 프로젝트 파일에 적용 + 빌드 확인
※ token-optimizer가 각 단계에서 자동으로 토큰 압축·캐싱
```

---

## 프로젝트 정보

### WMS (백엔드)
- **스택**: Kotlin + Spring Boot, MyBatis, Gradle
- **특성**: Backend 전용 프로젝트
- **DB**: (application.yml 참조)
- **패키지**: `com.cjlogistics.wms`

### WMS View (프론트엔드)
- **스택**: React 19, TypeScript, Vite 8, Tailwind CSS 4, Zustand, React Router 7, Axios
- **실행**: `npm run dev` → http://localhost:5173
- **패키지 설치**: `cd c:\workspace\wms_view && npm install`
