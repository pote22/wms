# WMS Project — Claude Code Agent Guide

## 새 환경 셋업 (AI 연동 재설치 절차)

> 새 PC에서 이 파일을 참고하여 Claude Code에게 "CLAUDE.md 보고 AI 연동 진행해줘"라고 하면 아래 절차를 자동으로 수행합니다.

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

### 3단계 — .mcp.json API 키 설정
`.mcp.json` 파일의 아래 두 항목에 실제 키 입력:
```json
"GEMINI_API_KEY": "실제_키_입력"
"OPENAI_API_KEY": "실제_키_입력"
```

### 4단계 — VSCode 재시작
재시작 후 MCP 서버 3개(`gemini-research`, `openai-coding`, `codex`) 자동 로드 확인

### 연결 테스트
```bash
# OpenAI 연결 확인
cd mcp-servers/openai-server
OPENAI_API_KEY="키값" node -e "import OpenAI from 'openai'; const o = new OpenAI({apiKey: process.env.OPENAI_API_KEY}); const r = await o.chat.completions.create({model:'gpt-4o',messages:[{role:'user',content:'hi'}],max_tokens:10}); console.log('OK:', r.choices[0].message.content);"

# Gemini 연결 확인
cd mcp-servers/gemini-server
GEMINI_API_KEY="키값" node -e "import {GoogleGenerativeAI} from '@google/generative-ai'; const m = new GoogleGenerativeAI(process.env.GEMINI_API_KEY).getGenerativeModel({model:'gemini-2.0-flash'}); const r = await m.generateContent('hi'); console.log('OK:', r.response.text().slice(0,30));"
```

> ⚠️ `.mcp.json`에 API 키가 평문 저장됨 — 공개 저장소에 push 금지

---

## 멀티 에이전트 역할 분담

이 프로젝트는 세 개의 AI 에이전트가 협력하여 작업을 처리합니다.

### 역할 정의

| 에이전트 | 역할 | 사용 도구 |
|----------|------|-----------|
| **Claude Code (나)** | 오케스트레이터 — 사용자 명령 해석, 작업 분배, 파일 편집, git 작업 | 기본 도구 전체 |
| **Gemini** (`gemini-research` MCP) | 리서치 담당 — 기술 조사, 개념 설명, 비교 분석 | `gemini_research`, `gemini_compare`, `gemini_analyze` |
| **OpenAI/Codex** (`openai-coding` MCP + `codex` MCP) | 코딩 담당 — 코드 생성, 리팩토링, 버그 수정, 테스트 작성, 코드 리뷰 | `codex_generate`, `codex_refactor`, `codex_fix_bug`, `codex_write_tests`, `codex`, `review` |

---

## AI 연동 구성 (MCP 서버)

### 연동 방식

| MCP 서버 | 타입 | 실행 방식 | 상태 |
|----------|------|-----------|------|
| `gemini-research` | stdio | `node mcp-servers/gemini-server/server.js` | ✅ 활성 |
| `openai-coding` | stdio | `node mcp-servers/openai-server/server.js` | ✅ 활성 |
| `codex` | stdio | `npx codex-mcp-server` (Codex CLI 래퍼) | ✅ 활성 |

### 설정 파일 위치
- **MCP 등록**: `.mcp.json` (프로젝트 루트)
- **Codex CLI 설정**: `~/.codex/config.toml`
- **MCP 서버 소스**: `mcp-servers/gemini-server/`, `mcp-servers/openai-server/`

### openai/codex-plugin-cc 플러그인
- **공식 저장소**: https://github.com/openai/codex-plugin-cc
- **출시일**: 2026년 3월 30일 (v1.0.2)
- **설치 방법**: Claude Code `/plugin` 마켓플레이스 명령어 필요
  ```
  /plugin marketplace add openai/codex-plugin-cc
  /plugin install codex@openai-codex
  ```
- **현재 상태**: 현재 Claude Code 버전에서 `/plugin` 명령 미지원 → `codex-mcp-server`(npx)로 대체 운영 중
- **대체 패키지**: `tuannvm/codex-mcp-server` (Codex CLI MCP 래퍼)

### API 키 관리
- `GEMINI_API_KEY` — `.mcp.json` env에 직접 설정 (Google AI Studio 발급)
- `OPENAI_API_KEY` — `.mcp.json` env에 직접 설정 (OpenAI Platform 발급)
- Codex CLI 인증 — `codex login --with-api-key` 로 `~/.codex/` 에 저장됨

---

## 작업 위임 판단 기준

### Gemini에게 위임할 때 (`gemini-research` MCP 사용)
- "~이 뭐야?", "~를 설명해줘", "~를 조사해줘" 같은 리서치 요청
- 라이브러리/프레임워크 비교 분석
- 아키텍처 설계 방향 조사
- 보안 취약점 리서치
- 기술 트렌드 파악

### OpenAI/Codex에게 위임할 때 (`openai-coding` / `codex` MCP 사용)
- 새 기능 코드 생성 요청
- 기존 코드 리팩토링
- 버그 원인 파악 및 수정 코드 제안
- 테스트 코드 작성
- 복잡한 알고리즘 구현
- 코드 리뷰 (`/codex:review`)

### Claude Code가 직접 처리할 때
- 파일 읽기/쓰기/수정
- git 작업 (commit, branch 등)
- 프로젝트 빌드 및 실행
- 최종 코드 파일 적용 (Codex가 생성한 코드를 파일에 반영)
- 간단한 수정 (1~5줄 변경)

---

## 워크플로우 예시

```
사용자: "Spring Security JWT 인증 구현해줘"

1. Claude Code: 작업 분석 → 리서치 + 코딩 필요
2. Gemini: JWT + Spring Security 최신 베스트 프랙티스 조사
3. OpenAI/Codex: 조사 결과 기반으로 실제 코드 생성
4. Claude Code: 생성된 코드를 프로젝트 파일에 적용 + 빌드 확인
```

---

## 프로젝트 정보

- **스택**: Kotlin + Spring Boot, MyBatis, Gradle
- **특성**: Backend 전용 프로젝트 (frontend 없음)
- **DB**: (application.yml 참조)
- **패키지**: `com.cjlogistics.wms`
