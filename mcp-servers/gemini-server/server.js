import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { GoogleGenerativeAI } from "@google/generative-ai";
import { z } from "zod";

const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
if (!GEMINI_API_KEY) {
  console.error("[gemini-mcp] ERROR: GEMINI_API_KEY environment variable is not set");
  process.exit(1);
}

const genAI = new GoogleGenerativeAI(GEMINI_API_KEY);
const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });

const server = new McpServer({ name: "gemini-research", version: "1.0.0" });

server.tool(
  "gemini_research",
  "Google Gemini에게 리서치 질의를 보냅니다. 기술 조사, 개념 설명, 최신 트렌드 파악 등에 사용하세요.",
  {
    query: z.string().describe("리서치할 질문 또는 주제"),
    context: z.string().optional().describe("추가 컨텍스트 (선택사항)"),
  },
  async ({ query, context }) => {
    const prompt = context ? `Context: ${context}\n\nQuestion: ${query}` : query;
    const result = await model.generateContent(prompt);
    return { content: [{ type: "text", text: result.response.text() }] };
  }
);

server.tool(
  "gemini_compare",
  "두 가지 이상의 기술/라이브러리/접근법을 비교 분석합니다.",
  {
    items: z.array(z.string()).describe("비교할 항목 목록"),
    criteria: z.string().optional().describe("비교 기준"),
  },
  async ({ items, criteria }) => {
    const prompt = criteria
      ? `다음 항목들을 ${criteria} 기준으로 비교 분석해주세요:\n${items.join(", ")}`
      : `다음 항목들을 상세히 비교 분석해주세요:\n${items.join(", ")}`;
    const result = await model.generateContent(prompt);
    return { content: [{ type: "text", text: result.response.text() }] };
  }
);

server.tool(
  "gemini_analyze",
  "코드나 아키텍처를 리서치 관점에서 분석합니다.",
  {
    content: z.string().describe("분석할 코드 또는 아키텍처 설명"),
    focus: z.string().optional().describe("분석 초점 (예: '보안', '성능', '확장성')"),
  },
  async ({ content, focus }) => {
    const prompt = focus ? `다음을 ${focus} 관점에서 분석해주세요:\n\n${content}` : `다음을 분석해주세요:\n\n${content}`;
    const result = await model.generateContent(prompt);
    return { content: [{ type: "text", text: result.response.text() }] };
  }
);

const transport = new StdioServerTransport();
await server.connect(transport);
console.error("[gemini-mcp] Server started successfully");
