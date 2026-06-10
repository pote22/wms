# WMS Project — Claude Code Agent Guide

---

## ⚡ git pull 후 업데이트 절차 (일상 작업 시 확인)

> **집 ↔ 회사 작업 전환 시 MCP 서버 코드가 변경됐는지 먼저 확인하세요.**

```bash
# 변경 여부 확인
git diff HEAD~1 --name-only | grep mcp-servers
```

| 결과 | 조치 |
|------|------|
| 출력 없음 | 그냥 작업 시작 |
| 파일 목록 출력됨 | 아래 npm install 실행 후 VSCode 재시작 |

```bash
# MCP 서버 패키지 재설치
cd c:\workspace\wms\mcp-servers\gemini-server && npm install
cd c:\workspace\wms\mcp-servers\openai-server && npm install
```

---

## 새 PC 셋업 (AI 연동 재설치 절차)

> 새 PC에서 Claude Code에게 **"CLAUDE.md 보고 AI 연동 진행해줘"** 라고 하면 아래 절차를 자동으로 수행합니다.

### 사전 조건
- Node.js 18.18 이상
- Claude Code: `npm install -g @anthropic-ai/claude-code`
- GEMINI_API_KEY, OPENAI_API_KEY 준비

---

### 1단계 — MCP 서버 패키지 설치
```bash
cd c:\workspace\wms\mcp-servers\gemini-server && npm install
cd c:\workspace\wms\mcp-servers\openai-server && npm install
```

### 2단계 — 전역 패키지 설치
```bash
npm install -g @openai/codex
```

### 3단계 — Codex CLI API 키 인증
```bash
echo "실제_OPENAI_API_KEY" | codex login --with-api-key
# 인증 정보 저장 위치: ~/.codex/auth.json
```

### 4단계 — MCP 서버 사용자 레벨 등록

> ✅ `--scope user`로 등록하면 어떤 폴더를 열어도 항상 로드됩니다.
> ✅ `-e KEY=값` 옵션으로 API 키를 `~/.claude.json`에 안전하게 저장합니다.
> ✅ `.mcp.json`에 API 키가 평문으로 노출되지 않습니다.

```bash
# Gemini 리서치 (API 키를 -e 옵션으로 직접 등록)
claude mcp add gemini-research --scope user -e GEMINI_API_KEY=실제_GEMINI_키 -- node "c:\workspace\wms\mcp-servers\gemini-server\server.js"

# OpenAI 코딩 (API 키를 -e 옵션으로 직접 등록)
claude mcp add openai-coding --scope user -e OPENAI_API_KEY=실제_OPENAI_키 -- node "c:\workspace\wms\mcp-servers\openai-server\server.js"

# Codex MCP (버전 고정 — 공급망 공격 방지)
claude mcp add codex --scope user -- npx codex-mcp-server@1.4.10

# PostgreSQL (Neon DB)
claude mcp add postgres --scope user -- npx -y @modelcontextprotocol/server-postgres "postgres://neondb_owner:npg_76JnlAyBYdhu@ep-shiny-dust-amkn0p26-pooler.c-5.us-east-1.aws.neon.tech/neondb?sslmode=require"
```

> ⚠️ **중요**: `-e KEY=값` 방식을 반드시 사용할 것.
> Windows 환경변수를 별도로 등록해도 VSCode 프로세스에 즉시 반영되지 않아 MCP 서버가 Failed 됩니다.
> `-e` 옵션이 `~/.claude.json`에 직접 저장되어 가장 안정적입니다.

### 5단계 — VSCode 재시작 후 연결 확인
```bash
claude mcp list
# 아래 6개가 모두 ✓ Connected 이면 정상
# gemini-research / openai-coding / codex
# Gmail / Google Calendar / Google Drive
```

---

### 연결 테스트 (문제 발생 시)

```bash
# Gemini 서버 단독 실행 테스트
cd c:\workspace\wms\mcp-servers\gemini-server
$env:GEMINI_API_KEY="실제_키"; node server.js
# → "[gemini-mcp] Server started successfully" 출력되면 정상

# OpenAI 서버 단독 실행 테스트
cd c:\workspace\wms\mcp-servers\openai-server
$env:OPENAI_API_KEY="실제_키"; node server.js
# → "[openai-mcp] Server started successfully" 출력되면 정상

# 전역 패키지 설치 확인
codex --version
```

#### gemini-research / openai-coding이 Failed 뜰 때

가장 흔한 원인: API 키가 MCP 등록에 포함되지 않은 경우.
아래 명령어로 기존 등록을 삭제하고 `-e` 옵션으로 재등록합니다.

```bash
# 기존 등록 삭제
claude mcp remove gemini-research --scope user
claude mcp remove openai-coding --scope user

# API 키 포함하여 재등록
claude mcp add gemini-research --scope user -e GEMINI_API_KEY=실제_키 -- node "c:\workspace\wms\mcp-servers\gemini-server\server.js"
claude mcp add openai-coding --scope user -e OPENAI_API_KEY=실제_키 -- node "c:\workspace\wms\mcp-servers\openai-server\server.js"

# VSCode 재시작 후 확인
claude mcp list
```

---

## 현재 패키지 버전 (2026-06-10 기준)

| 패키지 | 버전 | 위치 |
|--------|------|------|
| `@google/genai` | 2.7.0 | gemini-server |
| Gemini 모델 | **gemini-3.5-flash** | gemini-server (`server.js` 내 model ID) |
| `@modelcontextprotocol/sdk` | 1.29.0 | gemini-server, openai-server |
| `openai` | 6.39.1 | openai-server |
| `@openai/codex` CLI | 0.135.0 | 전역 |
| `codex-mcp-server` | **1.4.10** (고정) | npx 실행 |
| `@anthropic-ai/claude-code` | 2.1.159 | 전역 |
| Claude Code 모델 | **claude-opus-4-8** | `~/.claude/settings.json` |

> ⚠️ Gemini SDK `@google/generative-ai` (deprecated) → `@google/genai` 마이그레이션 완료 (2026-06)
> ⚠️ MCP SDK `server.tool()` (deprecated) → `server.registerTool()` 마이그레이션 완료 (2026-06)
> ⚠️ `token-optimizer-mcp` 보안 위험으로 **제거** (2026-06-10): 8일에 29개 버전 배포, 의심스러운 패키지
> ✅ Gemini 모델 `gemini-2.5-flash` → `gemini-3.5-flash` 업그레이드 (2026-06-10)
> ✅ Claude Code 모델 `claude-sonnet-4-6` → `claude-opus-4-8` 업그레이드 (2026-06-10, PRO 구독)

---

## 설정 파일 위치 (git 미포함 — PC마다 별도 설정)

| 파일 | 위치 | 내용 |
|------|------|------|
| 사용자 MCP 등록 | `~/.claude.json` | gemini-research, openai-coding API 키 포함 등록 정보 |
| Claude 사용자 설정 | `~/.claude/settings.json` | 전역 설정, MCP 도구 허용 목록, **Claude Code 모델 지정** |
| Codex CLI 인증 | `~/.codex/auth.json` | OpenAI API 키 인증 정보 |
| 프로젝트 MCP 설정 | `C:\workspace\wms\.mcp.json` | codex 서버만 포함 (API 키 없음) |
| 프로젝트 MCP 설정 | `C:\workspace\.mcp.json` | codex 서버만 포함 (API 키 없음) |

> ⚠️ 위 파일들은 git에 포함되지 않아 새 PC마다 4단계 절차 필요
> ⚠️ `.mcp.json`에는 API 키가 없으므로 git 커밋해도 키 노출 없음

---

## AI 연동 구성 (MCP 서버)

### 연동 방식

| MCP 서버 | 등록 위치 | 실행 방식 | 상태 |
|----------|-----------|-----------|------|
| `gemini-research` | user scope (`~/.claude.json`) | `node mcp-servers/gemini-server/server.js` | ✅ 활성 |
| `openai-coding` | user scope (`~/.claude.json`) | `node mcp-servers/openai-server/server.js` | ✅ 활성 |
| `codex` | project scope (`.mcp.json`) | `npx codex-mcp-server@1.4.10` | ✅ 활성 |
| `postgres` | user scope (`~/.claude.json`) | `npx @modelcontextprotocol/server-postgres` | ✅ 활성 |

### API 키 관리 (2026-06-10 보안 개선)
- `GEMINI_API_KEY` — `claude mcp add -e` 옵션으로 `~/.claude.json`에 저장 (`.mcp.json` 미포함)
- `OPENAI_API_KEY` — `claude mcp add -e` 옵션으로 `~/.claude.json`에 저장 (`.mcp.json` 미포함)
- Codex CLI 인증 — `codex login --with-api-key`로 `~/.codex/auth.json`에 저장
- `.mcp.json`에는 API 키 없음 → git 커밋 안전

### .mcp.json 현재 구성
```json
{
  "mcpServers": {
    "codex": {
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "codex-mcp-server@1.4.10"]
    }
  }
}
```

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

## 작업 위임 규칙 (MANDATORY — 반드시 준수)

> ⚠️ 아래 규칙은 선택 사항이 아닙니다. 해당 작업 유형이 감지되면 Claude Code는 반드시 지정된 MCP 도구를 먼저 호출한 후 결과를 바탕으로 응답해야 합니다. 직접 답변 금지.

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

**호출 순서**: `codex_generate` 등 → 결과 확인 → Claude Code가 파일에 적용

### Claude Code가 직접 처리하는 것 (MCP 호출 불필요)
- 파일 읽기/쓰기/수정 (Codex 결과 적용 포함)
- git 작업 (commit, branch 등)
- 프로젝트 빌드 및 실행
- 1~5줄 이하의 단순 수정
- 사용자 승인 확인, 계획 보고 등 메타 작업

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

### WMS (백엔드)
- **스택**: Kotlin + Spring Boot, MyBatis, Gradle
- **특성**: Backend 전용 프로젝트
- **DB**: (application.yml 참조)
- **패키지**: `com.cjlogistics.wms`

### WMS View (프론트엔드)
- **스택**: React 19, TypeScript, Vite 8, Tailwind CSS 4, Zustand, React Router 7, Axios
- **실행**: `npm run dev` → http://localhost:5173
- **패키지 설치**: `cd c:\workspace\wms_view && npm install`
