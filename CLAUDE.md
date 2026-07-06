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

# Codex MCP (first-party — codex CLI 내장 서버. npx 공급망 위험 없음)
claude mcp add codex --scope user -- codex mcp-server

# PostgreSQL (Neon DB)
claude mcp add postgres --scope user -- npx -y @modelcontextprotocol/server-postgres "postgres://neondb_owner:<DB_PASSWORD>@ep-shiny-dust-amkn0p26-pooler.c-5.us-east-1.aws.neon.tech/neondb?sslmode=require"
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
| `@openai/codex` CLI | 0.142.2 | 전역 (MCP는 내장 `codex mcp-server` 사용) |
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
| `codex` | project scope (`.mcp.json`) | `codex mcp-server` (first-party) | ✅ 활성 (읽기·생성·리뷰용. 파일 쓰기는 ⚠️아래 참조) |
| `postgres` | user scope (`~/.claude.json`) | `npx @modelcontextprotocol/server-postgres` | ✅ 활성 |

### API 키 관리 (2026-06-10 보안 개선)
- `GEMINI_API_KEY` — `claude mcp add -e` 옵션으로 `~/.claude.json`에 저장 (`.mcp.json` 미포함)
- `OPENAI_API_KEY` — `claude mcp add -e` 옵션으로 `~/.claude.json`에 저장 (`.mcp.json` 미포함)
- Codex CLI 인증 — `codex login --with-api-key`로 `~/.codex/auth.json`에 저장
- `.mcp.json`에는 API 키 없음 → git 커밋 안전

### .mcp.json 현재 구성 (2026-06-29: first-party 서버로 변경)
```json
{
  "mcpServers": {
    "codex": {
      "type": "stdio",
      "command": "codex",
      "args": ["mcp-server"]
    }
  }
}
```
> 변경 이유: 서드파티 `codex-mcp-server@1.4.10`(최신이지만 codex CLI 0.142.2와 안 맞음) 대신 codex CLI 내장 서버 사용. npx 다운로드가 없어 공급망 위험도 더 낮음.

---

## 멀티 에이전트 역할 분담

이 프로젝트는 세 개의 AI 에이전트가 협력하여 작업을 처리합니다.

### 역할 정의

> ⚙️ 역할 분담은 2026-06-27 실측 벤치마크로 검증·갱신됨. (리서치: 클로드/Codex 웹검색 우위, 제미나이 MCP는 웹 불가 / 코딩: Codex 우위)

| 에이전트 | 역할 | 사용 도구 |
|----------|------|-----------|
| **Claude Code (나)** | 오케스트레이터 + **리서치 메인** — 명령 해석·작업 분배, 실시간 웹 리서치, 파일 편집, git 작업 | 기본 도구 전체 + `WebSearch` |
| **OpenAI/Codex** (`openai-coding` MCP + `codex` MCP) | **코딩 메인** + 리서치 교차검증 — 코드 생성·리팩토링·버그 수정·테스트·리뷰, 사실 대조 | `codex_generate`, `codex_refactor`, `codex_fix_bug`, `codex_write_tests`, `codex`, `review` |
| **로컬 Qwen** (Ollama: `qwen2.5-coder:7b` 등) | 일상·반복 코딩 보조 (무료, 1차 초안) — 결과는 반드시 테스트 검증 | `ollama run` (로컬) |
| **Gemini** (`gemini-research` MCP) | 보조 — 웹 불필요한 개념 설명/코드 분석만. **실시간 검색 불가**(그라운딩=결제 필요). 최신 리서치는 **제미나이 앱에서 수동** | `gemini_research`, `gemini_compare`, `gemini_analyze` |

---

## 작업 위임 규칙 (MANDATORY — 반드시 준수)

> ⚠️ 아래 규칙은 선택 사항이 아닙니다. 해당 작업 유형이 감지되면 Claude Code는 반드시 지정된 MCP 도구를 먼저 호출한 후 결과를 바탕으로 응답해야 합니다. 직접 답변 금지.

### 리서치 작업 → Claude WebSearch 우선, Codex로 교차검증

> ⚠️ 2026-06-27 변경: 기존 "리서치=제미나이 먼저" 규칙 폐기. 제미나이 MCP는 그라운딩(웹 검색)이 무료 티어에서 막혀(429) **최신 정보 리서치 불가**임이 실측으로 확인됨. 따라서 리서치는 웹 검색이 작동하는 Claude/Codex로 처리.

다음 유형의 요청이 오면 **Claude가 `WebSearch`로 직접 리서치**하고, 사실 정확성이 중요하면 **Codex로 교차검증**할 것:
- "~이 뭐야?", "~를 설명해줘", "~를 조사해줘", "~를 알아봐줘"
- 라이브러리/프레임워크 비교 분석
- 아키텍처·설계 방향 조사
- 보안 취약점 리서치 / 기술 트렌드 파악
- 외부·최신 기술 정보가 필요한 모든 질문

**호출 순서**: Claude `WebSearch` (1차) → 필요 시 Codex 교차검증 → 출처와 함께 전달
**예외**: 최신 웹 정보가 불필요한 순수 개념 설명은 `gemini_research`(또는 직접 답변)도 가능. **최신 사실 검색이 필요할 땐 제미나이 MCP 사용 금지**(웹 안 됨) — 굳이 제미나이를 쓰려면 **앱에서 수동**으로.

### 코딩 작업 → Codex에게 위임 (Claude는 오케스트레이터)

코드 생성/수정/리팩토링/버그수정/테스트는 Claude가 직접 작성하지 말고 **Codex에게 위임**한다. 대상: 새 기능, 리팩토링, 버그 수정, 테스트 작성, 복잡한 알고리즘, 코드 리뷰.

#### ⚠️ Codex로 파일을 쓰는 유일한 방법 = Bash로 `codex exec` 직접 실행 (2026-06-29 검증)
```bash
codex exec -C <작업dir> --dangerously-bypass-approvals-and-sandbox -c model_reasoning_effort="low" "<지시>"
```
- **codex MCP 도구(`mcp__codex__codex`)는 파일 쓰기 불가**: Claude Code의 MCP 호스트가 codex 세션을 read-only로 가둠. `sandbox=workspace-write`/`approval-policy=never`/`config` 오버라이드를 다 줘도 effective sandbox=read-only (third-party/first-party 동일).
- CLI도 `-s workspace-write`만으로는 read-only(외부 샌드박스=Bash 도구 감지). `--dangerously-bypass-approvals-and-sandbox`라야 codex 자체 샌드박스를 끄고 실제 쓰기됨.
- codex 콘솔 출력의 한글이 깨져 보여도 **실제 파일은 정상**(출력 인코딩 문제). 편집 후 반드시 Read로 검증.

#### Codex MCP / openai-coding MCP 가 여전히 유효한 용도
- 코드 **생성·분석**(텍스트 반환): `codex_generate`, `codex_refactor`, `codex_fix_bug`, `codex_write_tests`
- **코드 리뷰**: `codex` MCP / `review`
- (이 결과를 파일에 반영해야 하면 위 `codex exec`로 적용)

**호출 순서**: 작업 분석 → (필요 시 리서치) → `codex exec`로 파일 작성/수정 → Claude가 Read·빌드로 검증

### Qwen (로컬, 무료) 위임
- 간단·반복 코딩 1차 초안: `ollama run qwen2.5-coder:7b "<지시>"` (Bash)
- 텍스트만 반환 → 파일 반영은 위 `codex exec`로. 결과는 반드시 빌드/테스트 검증.

### Claude Code가 직접 처리하는 것 (위임 불필요)
- 파일 읽기/검색 (Read/Grep/Glob)
- 문서·설정·기억 파일 작성 (CLAUDE.md, .mcp.json, memory 등 비(非)코드 산출물)
- git 작업 (commit, branch 등)
- 프로젝트 빌드 및 실행, 위임 결과 검증
- 사용자 승인 확인, 계획 보고 등 메타 작업

---

## 워크플로우 예시

```
사용자: "Spring Security JWT 인증 구현해줘"

1. Claude Code: 작업 분석 → 리서치 + 코딩 필요
2. Claude WebSearch: JWT + Spring Security 최신 베스트 프랙티스 조사 (필요 시 Codex로 교차검증)
3. Codex: `codex exec --dangerously-bypass-approvals-and-sandbox` 로 실제 코드 파일 작성/수정
4. Claude Code: Read·빌드로 검증
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
