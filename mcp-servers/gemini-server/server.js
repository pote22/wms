import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { GoogleGenAI } from "@google/genai";
import { z } from "zod";

const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
if (!GEMINI_API_KEY) {
  console.error("[gemini-mcp] ERROR: GEMINI_API_KEY environment variable is not set");
  process.exit(1);
}

const ai = new GoogleGenAI({ apiKey: GEMINI_API_KEY });

// NOTE: Google Search grounding (tools: [{ googleSearch: {} }]) requires a
// billing-enabled API key — it returns 429 RESOURCE_EXHAUSTED on the free tier
// (verified 2026-06-27). Re-add the `config.tools` block below if billing is enabled.
async function generate(prompt) {
  const response = await ai.models.generateContent({
    model: "gemini-3.5-flash",
    contents: prompt,
  });
  return response.text;
}

const server = new McpServer({ name: "gemini-research", version: "1.0.0" });

server.registerTool(
  "gemini_research",
  {
    description: "Google Gemini에게 리서치 질의를 보냅니다. 기술 조사, 개념 설명, 최신 트렌드 파악 등에 사용하세요.",
    inputSchema: {
      query: z.string().describe("리서치할 질문 또는 주제"),
      context: z.string().optional().describe("추가 컨텍스트 (선택사항)"),
    },
  },
  async ({ query, context }) => {
    const prompt = context ? `Context: ${context}\n\nQuestion: ${query}` : query;
    return { content: [{ type: "text", text: await generate(prompt) }] };
  }
);

server.registerTool(
  "gemini_compare",
  {
    description: "두 가지 이상의 기술/라이브러리/접근법을 비교 분석합니다.",
    inputSchema: {
      items: z.array(z.string()).describe("비교할 항목 목록"),
      criteria: z.string().optional().describe("비교 기준"),
    },
  },
  async ({ items, criteria }) => {
    const prompt = criteria
      ? `다음 항목들을 ${criteria} 기준으로 비교 분석해주세요:\n${items.join(", ")}`
      : `다음 항목들을 상세히 비교 분석해주세요:\n${items.join(", ")}`;
    return { content: [{ type: "text", text: await generate(prompt) }] };
  }
);

server.registerTool(
  "gemini_analyze",
  {
    description: "코드나 아키텍처를 리서치 관점에서 분석합니다.",
    inputSchema: {
      content: z.string().describe("분석할 코드 또는 아키텍처 설명"),
      focus: z.string().optional().describe("분석 초점 (예: '보안', '성능', '확장성')"),
    },
  },
  async ({ content, focus }) => {
    const prompt = focus ? `다음을 ${focus} 관점에서 분석해주세요:\n\n${content}` : `다음을 분석해주세요:\n\n${content}`;
    return { content: [{ type: "text", text: await generate(prompt) }] };
  }
);

const transport = new StdioServerTransport();
await server.connect(transport);
console.error("[gemini-mcp] Server started successfully");
